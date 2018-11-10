package inverted_index;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import data_structures.*;
import entities.*;
import entities.keeper_plugins.*;
import entities.information_manager_plugins.*;
import utils.name_generator;
import exceptions.*;



// this should be a singleton instance, contains a posting unit counter
public class index {
	
	// singleton
	// ref: http://www.runoob.com/design-pattern/singleton-pattern.html
	private static index idx = new index();
	private index() {}
	public static index get_instance() {
		return idx;
	}
	
	public HashMap<Long, posting_unit> postUnitMap = new HashMap<Long, posting_unit>(); // {postingUnitId : postingUnitIns}, store all the posting units, for convenience of persistance
	public HashMap<String, ArrayList<Long>> lexicon = new HashMap<String, ArrayList<Long>>(); // {term : [postingUnitIds]}, the inside HashMap is for the convenience of adding more meta data
	private keeper kpr = keeper.get_instance(); // get the keeper instance, so as to get the lexiconLockMap
	public HashMap<String, doc> docMap = new HashMap<String, doc>();
	private information_manager infoManager = information_manager.get_instance();	// only used when adding/removing new posting unit into/from index
	
	// for generating the unique posting unit id s
	public class counters {
		long postingId = 0L;
	}
	
	public counters pc = new counters();
	public long get_pc() {
		return pc.postingId;
	}
	
	public long lastPostUnitId = 0; // static -> public
	

	// initialise the posting list for one term
	private long ini_posting_list(String term) {
		posting_unit postUnit = new posting_unit();
		
		// TODO: need a global id generator?
		postUnit.term = term;
		postUnit.currentId = pc.postingId;
		postUnitMap.put(postUnit.currentId, postUnit);
		lastPostUnitId = postUnit.currentId;
		pc.postingId ++;

		// initialize the posting list for one term
		ArrayList<Long> postingUnitIds = new ArrayList<Long>();
		postingUnitIds.add(postUnit.currentId);
		lexicon.put(term, postingUnitIds);  
		
		// initialize the lock for each term in lexicon
		kpr.add_target(lexicon_locker.class, term);
		
		// record the current max tf and posting list loaded status
		infoManager.set_info(term_max_tf.class, postUnit);
		infoManager.set_info(posting_loaded_status.class, postUnit);
		
		return postUnit.currentId;
	}
	
	
	// for checking if one term is already existing in the inverted index 
	private int check_term_existance(String term) {
		int notExistanceFlag = 1; // 1 term not existing, -1 term existing 
		if(lexicon.containsKey(term) == true) {
			notExistanceFlag = -1;
		}
		return notExistanceFlag;
	}
	
	
	// add a new term to the inverted index, include add a new term to the lexicon and add add new 
	// return -1 means the term is already existing in the inverted-index
	public long add_term(String term) {
		long firstUnitId;
		int notExistanceFlag = check_term_existance(term);
		if(notExistanceFlag == 1) {
			firstUnitId = ini_posting_list(term); // when the term is not existing, initialise the posting list for it
		} else {
			firstUnitId = -1; // when the term is existing
		}
		return firstUnitId;
	}
	
	
	// delete a posting list
	// TODO: firstly require the lock, after releasing the lock delete the lock
	// TODO: load posting firstly
	public void del_term(String term) {
		ArrayList<Long> postUnitList = lexicon.get(term);
		lexicon.remove(term); // delete from lexicon
		
		kpr.del_target(lexicon_locker.class, term);
		
		for(long postUnitId : postUnitList) {
			postUnitMap.remove(postUnitId); // delete specific posting unites
		}
		
		infoManager.del_info(term_max_tf.class, term);
		infoManager.del_info(posting_loaded_status.class, term);
		
	}
	
	
	// the analysing of doc and find the term:postUnit pair is handled by a higher level
	// used as the sequentially add, the posting list and lexicon growing at the same time
	private posting_unit _add_posting_unit(String term, posting_unit postUnit) {
		String threadNum = "" + name_generator.thread_name_gen();
		posting_unit addedPostUnit = null;
		
		if (kpr.require_lock(lexicon_locker.class, term, threadNum) == 1) { // if could not require the lock, will not try to execute and release the lock
			try {				
				// eliminating the retrying logic here, just block
				  // successfully required the lock
					postUnit.term = term;
					postUnit.currentId = pc.postingId; // even if the old unit has the id it will be reset
					postUnitMap.put(postUnit.currentId, postUnit); // add to the overall posting units table
					pc.postingId ++; // TODO: here the id is being updated, should I use a different method to only load the persisted ids?
					
					ArrayList<Long> postingUnitIds = lexicon.get(term); // get the posting list
					long previousUnitId = postingUnitIds.get(postingUnitIds.size() - 1);
					posting_unit prevUnit = postUnitMap.get(previousUnitId); // get the instance of previous unit
					postingUnitIds.add(postUnit.currentId); // add to the lexicon, in fact is adding to the posting list
					
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
					
			} catch(Exception e) {
				e.printStackTrace();
			} 
			finally {
				kpr.release_lock(lexicon_locker.class, term, threadNum);
			}
		}

		// TODO: add retry for muti-threading situation
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
	
	
	// delete the some posting units
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
		// TODO: + require lock
		posting_unit delUnit = postUnitMap.get(postingUnitId);
		delUnit.status = 0;
		return delUnit.currentId;
	}

	
	public doc add_doc(String[] persistedUnits, String targetDocName) {
		doc addedDoc = new doc();
		addedDoc.docId = targetDocName;
		docMap.put(targetDocName, addedDoc);
		
		for(String persistedUnit : persistedUnits) {
			posting_unit addedPostUnit = add_posting_unit(persistedUnit);
			if(addedPostUnit != null) {
				addedDoc.docLength ++;
			}
		}
		return addedDoc;
	}
	
	
	// reset index
	// TODO: when use this method need to be very careful, as it will lead to the pc -> 0
	public void clear_index() {
		postUnitMap = new HashMap<Long, posting_unit>();
		lexicon = new HashMap<String, ArrayList<Long>>();
		kpr.clear_maps(lexicon_locker.class);
		pc = new counters();
		
		// clear high level information
		infoManager.clear_info(posting_loaded_status.class);
		infoManager.clear_info(term_max_tf.class);
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
			
			System.out.println("--->" + postingPathes.size()); // TODO: for testing
			
			for (String postingPath : postingPathes) {
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
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		// re-persist the inverted index
		index_io_operations.get_instance().persist_index(); // the ids are corrected now
	}
}





