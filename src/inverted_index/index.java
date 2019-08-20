package inverted_index;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.*;

import configs.index_config;
import data_structures.*;
import entities.*;
import utils.callback;
import entities.keeper_plugins.*;
import entities.scanner_plugins.delete_doc;
import entities.scanner_plugins.delete_posting;
import entities.information_manager_plugins.*;
import utils.name_generator;
import exceptions.*;
import probes.*;



// this should be a singleton instance, contains a posting unit counter
public class index {
	
	// singleton
	// ref: http://www.runoob.com/design-pattern/singleton-pattern.html
	private static index idx = new index();
	private index() {}
	public static index get_instance() {
		return idx;
	}
	
	public ConcurrentHashMap<Long, posting_unit> postUnitMap = new ConcurrentHashMap<Long, posting_unit>(); // {postingUnitId : postingUnitIns}, store all the posting units, for convenience of persistance
	public ConcurrentHashMap<String, ArrayList<Long>> lexicon = new ConcurrentHashMap<String, ArrayList<Long>>(); // {term : [postingUnitIds]}, the inside HashMap is for the convenience of adding more meta data
	private keeper kpr = keeper.get_instance(); // get the keeper instance, so as to get the lexiconLockMap
	public ConcurrentHashMap<String, doc> docMap = new ConcurrentHashMap<String, doc>();
	public ConcurrentHashMap<Long, doc> docIdMap = new ConcurrentHashMap<Long, doc>();    // not persisted, generated from docMap when loading from local
	private information_manager infoManager = information_manager.get_instance();	// only used when adding/removing new posting unit into/from index
	
	public ConcurrentHashMap<String, term> lexicon_2 = new ConcurrentHashMap<String, term>(); // {term : termIns}

	
	// for generating the unique id s
	public class counters {
		long id = 0L;
		public void _inc() {    // invoked each time the val() is used, internal increment
			id ++;
		}
		public synchronized long val() {
			long retId = id;
			_inc();
			return retId;
		}
		public synchronized void set(long curId) {
			id = curId;
		}
		public synchronized long view() {
			return id;
		}
	}
	
	public counters pc = new counters();
	public long lastPostUnitId = 0;
	
	public counters dc = new counters();
	public long lastDocId = 0;
	
	public counters tc = new counters();
	public long lastTermId = 0;
	

	// initialise the posting list for one term
	private long ini_posting_list(String term) {
		posting_unit postUnit = new posting_unit();
		
		// TODO: need a global id generator?
		postUnit.term = term;
		postUnit.currentId = pc.val();
		postUnitMap.put(postUnit.currentId, postUnit);
		lastPostUnitId = postUnit.currentId;

		// initialise the posting list for one term
		ArrayList<Long> postingUnitIds = new ArrayList<Long>();
		postingUnitIds.add(postUnit.currentId);
		lexicon.put(term, postingUnitIds);  
		
		// use head / tail pointers to view the posting list
		term termIns = new term();
		termIns.termId = tc.val();
		termIns.termName = term;
		lexicon_2.put(term, termIns);
		
		// record the current max tf and posting list loaded status
		// infoManager.set_info(term_max_tf.class, postUnit);
		infoManager.set_info(posting_loaded_status.class, postUnit);
		
		return postUnit.currentId;
	}
	
	
	// for checking if one term is already existing in the inverted index 
	private int check_term_existance(String term) {
		int notExistanceFlag = 1; // 1 term not existing, -1 term existing 
		if(lexicon.containsKey(term) == true) {    // TODO: -> lexicon_2
			notExistanceFlag = -1;
		}
		return notExistanceFlag;
	}
	
	
	// add a new term to the inverted index, include add a new term to the lexicon and add add new 
	// return -1 means the term is already existing in the inverted-index
	// can be used by multi-threads
	public long add_term(String term) {
		long firstUnitId = -1;
		String threadNum = "" + name_generator.thread_name_gen();
		
		// initialise the lock for each term in lexicon
		kpr.add_target(lexicon_locker.class, term);
		callback release_lock = kpr.require_lock_check_notebook_wait(lexicon_locker.class, term, threadNum);
		if (release_lock != null) {
			try {
				
				int notExistanceFlag = check_term_existance(term);
				if(notExistanceFlag == 1) {
					firstUnitId = ini_posting_list(term); // when the term is not existing, initialise the posting list for it
				}
				
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				release_lock.conduct();
			}
		}
		return firstUnitId;
	}
	
	
	// TODO: moved to adv_ops, to be depreciated
	// delete a posting list
	public void del_term(String term) {
		String threadNum = "" + name_generator.thread_name_gen();
		
		callback release_lock = kpr.require_lock_check_notebook_wait(lexicon_locker.class, term, threadNum);
		if (release_lock != null) {
			try {
				// load related posting units into memory
				index_io_operations.get_instance().load_posting(new String[] {term});
				
				ArrayList<Long> postUnitList = lexicon.get(term);
				lexicon.remove(term); // delete from lexicon
				
				kpr.del_target(lexicon_locker.class, term);    // remove the lock's references in docMaps
				
				for(long postUnitId : postUnitList) {
					postUnitMap.remove(postUnitId); // delete specific posting unites
				}
				
				infoManager.del_info(term_max_tf.class, term);
				infoManager.del_info(posting_loaded_status.class, term);
				
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				release_lock.conduct();
			}
		}
	}
	
	
	// invoked by operator, each time after the newly added files are scanned
	// record the IDF calculated time
	// TODO: modify the term_idf and ter_idf_cal_time to operate the term.termProp
	public int cal_termIdf() {
		int calDoneFlag = infoManager.set_info(term_idf.class, new posting_unit());
		infoManager.set_info(term_idf_cal_time.class, new posting_unit());
		return calDoneFlag;
	}

	
	
	// the analysing of doc and find the term:postUnit pair is handled by a higher level
	// used as the sequentially add, the posting list and lexicon growing at the same time
	private posting_unit _add_posting_unit(String term, posting_unit postUnit) {
		String threadNum = "" + name_generator.thread_name_gen();
		posting_unit addedPostUnit = null;
		
		callback release_lock = kpr.require_lock_check_notebook_wait(lexicon_locker.class, term, threadNum);
		if (release_lock != null) { // if could not require the lock, will not try to execute and release the lock
			try {				
				// eliminating the retrying logic here, just block
				  // successfully required the lock
					postUnit.term = term;
					postUnit.currentId = pc.val(); // even if the old unit has the id it will be reset
					postUnitMap.put(postUnit.currentId, postUnit); // add to the overall posting units table
					
					ArrayList<Long> postingUnitIds = lexicon.get(term); // get the posting list
					long previousUnitId = postingUnitIds.get(postingUnitIds.size() - 1);
					posting_unit prevUnit = postUnitMap.get(previousUnitId); // get the instance of previous unit
					postingUnitIds.add(postUnit.currentId); // add to the lexicon, in fact is adding to the posting list
					
					
					// TODO: adding to the lexicon_2
					term termIns = lexicon_2.get(term);
					long lastPostUnitId = termIns.lastPostUnitId;
					posting_unit lastPostUnit = idx.postUnitMap.get(lastPostUnitId);
					termIns.lastPostUnitId = postUnit.currentId; // move the tail pointer
					
					
					// link units, for scanning
					postUnit.link_to_previous(prevUnit);
					if (prevUnit != null) {
						prevUnit.link_to_next(postUnit);
					}
					
					lastPostUnitId = postUnit.currentId; // for setting the pc in load lexicon, making sure the old units are not overwritten when new units added
					addedPostUnit = postUnit;
					
					// here does not need to set the posting_loaded_status is because
					// _add_posting_unit is always occur with add_term
					// but for the potential single usage, here still use the status setting
					infoManager.set_info(term_max_tf.class, postUnit);
					infoManager.set_info(posting_loaded_status.class, postUnit);
					infoManager.set_info(term_df.class, postUnit);
					
			} catch(Exception e) {
				e.printStackTrace();
			} 
			finally {
				release_lock.conduct();
			}
		}

		// TODO: add retry for muti-threading situation
		// as some locking attempts will be blocked by the lock status,
		// so use a recursive adding? -- retry in add_doc
		if (addedPostUnit == null) { // if all retries are all failed, print the customised exception
			new unit_add_fail_exception(String.format("Unit %s added failed", "" + postUnit.currentId)).printStackTrace(); 
		}
		
		return addedPostUnit;
	}
	
	
	// provided in APIs
	// [term] currentId nextId previousId {uProp} docId status
	// e.g. place 2 -1 -1 {} -- 1
	// TODO: add a ArrayList<posting_unit> to collect adding failed units and retry, until success
	public posting_unit add_posting_unit(String persistedUnit) {
		String[] tempList = persistedUnit.split(" ");
		String term = tempList[0];
		long starterPUnitId = add_term(term); // no matter what try to add the term firstly
		if(starterPUnitId == -1) { // means the term is already existing
			index_io_operations.get_instance().load_posting(new String[] {term}); // load the whole posting list before add new things in
		}
		posting_unit postUnit = posting_unit.deflatten(persistedUnit);
		posting_unit addedPostUnit = _add_posting_unit(term, postUnit);
		return addedPostUnit;
	}
	
	
	// not for productive usage
	
	// delete the posting units
	// just set flags instead of directly remove, ref: SSTable
	// need an independent process scanning and cleaning the postUnitMap & lexicon
	// the starter unit should never be deleted
	
	// not using infoManager here to set the high level information
	// and infoManager not implementing the tf decreasing
	// is because of the units are not really removed from index until the cleaner.clean the index
	// the cleaner is only used in index.reload, so that no need of changing the information when setting the status here
	
	// if manually used the cleaner.clean, the posting_loaded_status is not affected, as cleaner will not affect the starter unit
	// the term_max_tf could be over estimated, but still have no big harm as the scoring will always care about the large tf more
	public long del_posting_unit(long postingUnitId) {
		posting_unit delUnit = postUnitMap.get(postingUnitId);
		delUnit.status = 0;
		return delUnit.currentId;
	}

	
	public posting_unit _add_term_unit(doc targetDoc, posting_unit postUnit) {
		posting_unit addedPostUnit = null;
		
		try {
			if(targetDoc.firstTermUnitId == -1) {
				targetDoc.firstTermUnitId = postUnit.currentId;
				targetDoc.lastTermUnitId = postUnit.currentId;    // when there is only one term in the doc
			}else {
				// not need the lock here, as this link will only be created once when the doc is added
				posting_unit lastTermUnit = postUnitMap.get(targetDoc.lastTermUnitId);
				postUnit.link_to_previous_term(lastTermUnit);
				lastTermUnit.link_to_next_term(postUnit);
				targetDoc.lastTermUnitId = postUnit.currentId;    // move the tail pointer
			}
			
			addedPostUnit = postUnit;
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return addedPostUnit;
	}
	
	
	public posting_unit add_term_unit(posting_unit postUnit) {
		posting_unit addedPostUnit = null;
		String threadNum = "" + name_generator.thread_name_gen();
		
		// use the wait lock, so that this one term chain can be handled by multi-threads
		callback release_lock = kpr.require_lock_check_notebook_wait(lexicon_locker.class, postUnit.term, threadNum);
		if (release_lock != null) {
			try {
				doc targetDoc = docIdMap.get(postUnit.docId);
				addedPostUnit = _add_term_unit(targetDoc, postUnit);
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				release_lock.conduct();
			}
		}

		return addedPostUnit;
	}
	
	
	// adding doc is a transaction, atomic, short time operation, 
	// thus do not consider the interleave with deactivator thread
	public ArrayList<String> _add_doc(ArrayList<String> persistedUnits, doc targetDoc, int retryTime) {
		ArrayList<String> failedPersistedUnits = new ArrayList<String>();
		
		// try to add unit
		for(String persistedUnit : persistedUnits) {
			posting_unit addedPostUnit = add_posting_unit(persistedUnit);
			
			if(addedPostUnit != null) {
				
				targetDoc.docLength ++;
				addedPostUnit.docId = targetDoc.docId;    // dynamically assign the docId
				addedPostUnit = _add_term_unit(targetDoc, addedPostUnit);
				
			}else {
				failedPersistedUnits.add(persistedUnit);
			}
		}
		
		if(!failedPersistedUnits.isEmpty()) {	// if some units are failed
			if(retryTime < configs.index_config.addingDocRetryTimes) {	// if not reach the max retryTime
				retryTime ++;
				failedPersistedUnits = _add_doc(failedPersistedUnits, targetDoc, retryTime);
			}
		}

		return failedPersistedUnits;
	}
	
	
//	// initialise the firtTermUnit for each addedDoc
//	public void ini_firstTermUnit(doc addedDoc) {
//		posting_unit postUnit= new posting_unit(); 
//		
//		postUnit.docId = addedDoc.docId;
//		postUnit.currentId = pc.postingId;
//		postUnitMap.put(postUnit.currentId, postUnit);
//		lastPostUnitId = postUnit.currentId;
//		pc.postingId ++;
//		
//		addedDoc.firstTermUnit = postUnit;
//	}
//	
	
	public ArrayList<String> add_doc(String[] persistedUnits, String targetDocName) {
		ArrayList<String> failedPersistedUnits = null;
		
		if(!docMap.containsKey(targetDocName)) {    // leave the version control to the operator, not allowing the doc with same name be added here directly
			doc addedDoc = new doc();
			addedDoc.docId = dc.val();
			addedDoc.docName = targetDocName;
			
			lastDocId = addedDoc.docId;
			
			docMap.put(addedDoc.docName, addedDoc);
			docIdMap.put(addedDoc.docId, addedDoc);
			
			// return the failed units
			int retryTime = 1;
			ArrayList<String> persistedUnitList = new ArrayList<String>();
			persistedUnitList.addAll(Arrays.asList(persistedUnits));
			failedPersistedUnits = _add_doc(persistedUnitList, addedDoc, retryTime);
		}else {
			System.out.println(String.format("doc <%s> already existing", targetDocName));
			// not return the failed units here, as it is not the normal adding situation, does not require the retry
		}
		return failedPersistedUnits;
	}
	
	
	// reset index
	// TODO: when use this method need to be very careful, as it will lead to the pc -> 0
	public void clear_index() {
		postUnitMap = new ConcurrentHashMap<Long, posting_unit>();
		lexicon = new ConcurrentHashMap<String, ArrayList<Long>>();
		lexicon_2 = new ConcurrentHashMap<String, term>();
		kpr.clear_maps(lexicon_locker.class);
		pc = new counters();
		
		// clear doc term chain points
		for(Long docId: docIdMap.keySet()) {
			doc docIns = docIdMap.get(docId);
			docIns.firstTermUnitId = -1;
			docIns.lastTermUnitId = -1;
		}
		
		// clear high level information
		infoManager.clear_info(posting_loaded_status.class);
		infoManager.clear_info(term_max_tf.class);
		infoManager.clear_info(term_df.class);
		infoManager.clear_info(term_idf.class);
	}

	
	
	public class reload_thread extends Thread{
		index idx = index.get_instance();
		String[] pPathes;
		
		public reload_thread(String[] postingPathes) {
			pPathes = postingPathes;
		}
		
		public void run() {
			for (String postingPath : pPathes) {
				try {
					FileReader pf = new FileReader(postingPath);
					BufferedReader pb = new BufferedReader(pf);
					
					try {
						// load the posting lists of one term
						String pUnitString;
						long i = 0;
						do {
							pUnitString = pb.readLine();
							
							if (pUnitString != null) {
								pUnitString = pUnitString.trim();
								String term = pUnitString.split(" ")[0];
								
								// check loading status and adding the term into lexicon for the first time it was seen
								if (i == 0 && lexicon.containsKey(term) == false) { 
									add_term(term);
								}
								
								posting_unit pUnit = posting_unit.deflatten(pUnitString);
								
								if(pUnit.previousId != -1) { // skip the starter unit, as they are regenerated when add term
									_add_posting_unit(term, pUnit); // re assign the ids, and link the units; when the idx is empty, only starters left, they are not going to be loaded into memory, so that the lastUnitId will not be updated					
									
									// reset the term chain
									pUnit.previousTermId = -1;
									pUnit.nextTermId = -1;
									add_term_unit(pUnit);
								}
								
							}
							
							i++;
						} while(pUnitString != null);
						
					} catch(Exception e) {
						e.printStackTrace();
					} finally {
						pf.close();
						pb.close();
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	
	
	// re-generate the posint unit ids
	// fully scan the persisted posting
	// single threading one, as only processing the persisted posting instead of the docs
	// TODO: use multi-threading and add lock to pc?
	public void reload_index() {
		
		// if not cleaned by cleaner, this reload will not work as expected,
		// as the deleted unit are still in the postUnitMap
		// TODO: alternatively, the cleaner could be running in an independent process
		cleaner clr = cleaner.getInstance();
		
		// load all the postings into memory; 
		// without this step, due to the lazy loading of posting list, 
		// delete_doc will only load small part of units into memory, 
		// the reload_index will erase the unloaded units from local file
		index_io_operations.get_instance().load_all_posting();
		clr.clean();
		
		index_io_operations.get_instance().persist_index(); // at this time the ids are not corrected
		clear_index();
		
		try {
			// collect the pathes of posting files
			// ref: https://stackoverrun.com/cn/q/12079625
			List<String> postingPathes =  Files.walk(Paths.get(configs.index_config.postingsPersistancePath), 2)
					.filter(path -> Files.isRegularFile(path))
					.map(path -> path.toString())
					.filter(path -> path.endsWith("posting"))
					.collect(Collectors.toList());
			
			System.out.println("posting amount --->" + postingPathes.size()); // TODO: for testing
			
			ArrayList<String[]> workloads = utils.task_spliter.get_workLoads_terms(configs.index_config.reloadWorkNum, postingPathes.toArray(new String[0]));
			ArrayList<reload_thread> threadList = new ArrayList<reload_thread>();
			
			for (String[] workload : workloads) {
				reload_thread rt = new reload_thread(workload);
				threadList.add(rt);
			}
			
			for(reload_thread rt : threadList) {
				rt.start();
				rt.join();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		cal_termIdf();
		
		// re-persist the inverted index
		index_io_operations.get_instance().persist_index(); // the ids are corrected now
	}
	
	
	
	// TODO: using lexicon_2
	// delete a posting list
	public void del_term_2(String term) {
		String threadNum = "" + name_generator.thread_name_gen();
		
		callback release_lock = kpr.require_lock_check_notebook_wait(lexicon_locker.class, term, threadNum);
		if (release_lock != null) {
			try {
				// load related posting units into memory
				index_io_operations.get_instance().load_posting(new String[] {term});
				idx.lexicon_2.remove(term); // delete from lexicon
				
				kpr.del_target(lexicon_locker.class, term);    // remove the lock's references in docMaps
				
				scanner.scan_term_thread sd = new scanner.scan_term_thread(
						new scanner(), 
						new delete_posting(), 
						null , 
						new String[] {term});
				
				sd.start();
				
				try {
					sd.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// TODO: does not need to operate the information here, as maxTf, postingLoadedStatus are both recorded in lexicon
//				infoManager.del_info(term_max_tf.class, term);
//				infoManager.del_info(posting_loaded_status.class, term);
				
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				release_lock.conduct();
			}
		}
	}
	

	// delete a specific document using term chain
	public ArrayList<Long> delete_doc(String targetDocName) throws Exception {
		ArrayList<Long> totalAffectedUnitIds = new ArrayList<Long>();
		
		doc docIns = idx.docMap.get(targetDocName);
		if(docIns != null) {
			Long docId = docIns.docId;
			// idxOps.load_doc_related_postings(docId);
			scanner.scan_doc_thread sd = new scanner.scan_doc_thread(
					new scanner(), 
					new delete_doc(), 
					null , 
					new String[] {"" + docId});
			
			sd.start();
			
			try {
				sd.join();
			} catch (Exception e) {
				e.printStackTrace();
			}

			totalAffectedUnitIds.addAll(sd.get_affectedUnitIds());
			
			idx.docMap.remove(targetDocName);
			idx.docIdMap.remove(docId);
		}
		
		return totalAffectedUnitIds;
	}
	
	
}





