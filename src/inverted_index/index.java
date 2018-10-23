package inverted_index;

import java.io.*;
import java.util.*;

import configs.index_config;
import utils.name_generator;
import exceptions.*;

import utils.*;
import inverted_index.keepe_plugins.*;
import probes.*;


// this should be a singleton instance, contains a posting unit counter
public class index {
	
	// singleton
	// ref: http://www.runoob.com/design-pattern/singleton-pattern.html
	private static index index_ins = new index();
	private index() {}
	public static index get_instance() {
		return index_ins;
	}
	

	
	public HashMap<Long, posting_unit> postUnitMap = new HashMap<Long, posting_unit>(); // {postingUnitId : postingUnitIns}, store all the posting units, for convenience of persistance
	public HashMap<String, ArrayList<Long>> lexicon = new HashMap<String, ArrayList<Long>>(); // {term : [postingUnitIds]}, the inside HashMap is for the convenience of adding more meta data
	private keeper kpr = keeper.get_instance(); // get the keeper instance, so as to get the lexiconLockMap
	
	// for generating the unique posting unit id s
	private class counters {
		long postingId = 0L;
	}
	counters pc = new counters();  
	static long lastPostUnitId = 0; 
	
	public long get_pc() {
		return pc.postingId;
	}
	
	

	// initialise the posting list for one term
	private long ini_posting_list(String term) {
		posting_unit postUnit = new posting_unit();
		
		// TODO: need a global id generator?
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
	public void del_term(String term) {
		ArrayList<Long> postUnitList = lexicon.get(term);
		lexicon.remove(term); // delete from lexicon
		
		kpr.del_target(lexicon_locker.class, term);
		
		for(long postUnitId : postUnitList) {
			postUnitMap.remove(postUnitId); // delete specific posting unites
		}
		
	}
	
	
	// the analysing of doc and find the term:postUnit pair is handled by a higher level
	// used as the sequentially add, the posting list and lexicon growing at the same time
	private long _add_posting_unit(String term, posting_unit postUnit) {
		String threadNum = "" + name_generator.thread_name_gen();
		long addedUnitId = -1L;
		
		if (kpr.require_lock(lexicon_locker.class, term, threadNum) == 1) { // if could not require the lock, will not try to execute and release the lock
			try {				
				// eliminating the retrying logic here, just block
				  // successfully required the lock
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
					addedUnitId = postUnit.currentId;
					lastPostUnitId = addedUnitId; // for setting the pc in load lexicon, making sure the old units are not overwritten when new units added
					
			} catch(Exception e) {
				e.printStackTrace();
			} 
			finally {
				kpr.release_lock(lexicon_locker.class, term, threadNum);
			}
		}

				
		if (addedUnitId == -1) { // if all retries are all failed, print the customised exception
			new unit_add_fail_exception(String.format("Unit %s added failed", "" + postUnit.currentId)).printStackTrace(); 
		}
		
		return addedUnitId;
	}
	
	
	// provided in APIs
	// [term] currentId nextId previousId {uProp} docId status
	// e.g. place 2 -1 -1 {} -- 1
	public long add_posting_unit(String persistedUnit) {
		String[] tempList = persistedUnit.split(" ");
		String term = tempList[0];
		long starterPUnitId = add_term(term); // no matter what try to add the term firstly
		if(starterPUnitId == -1) { // means the term is already existing
			load_posting(new String[] {term}); // load the whole posting list before add new things in
		}
		posting_unit postUnit = posting_unit.deflatten(persistedUnit.replaceFirst(term + " ", ""));
		long addedUnitId = _add_posting_unit(term, postUnit);
		return addedUnitId;
	}
	
	
	// delete the some posting units
	// just set flags instead of directly remove, ref: SSTable
	// need an independent process scanning and cleaning the postUnitMap & lexicon
	// the starter unit should never be deleted
	public long del_posting_unit(long postingUnitId) {
		// TODO: + require lock
		posting_unit delUnit = postUnitMap.get(postingUnitId);
		delUnit.status = 0;
		return delUnit.currentId;
	}
	

	
	// persist the inverted index on to local hard disk, 
	// with the posting units written in line in the order of posting list
	// this method doesnt load all postings in to memory, so that cannot found all the units in the lexicon
	private void _persist_index() {
		try {
			// does not need to lock up the index, as the inverted-index is not dynamically adding on real time
			// 1. generate, 2. persist, 3. lazily load and serve
			// such that the generation process will consume the biggest amount of memory
			
			// TODO: persist the offset of terms instead of lexicon??
			
			// persist lexicon
			FileWriter lf = new FileWriter(configs.index_config.lexicon_persistance_path);
			try {
				ArrayList<String> termStrings = new ArrayList<String>();
				for(String term : lexicon.keySet()) {
					Long[] termPosting = lexicon.get(term).toArray(new Long[0]); // ArrayList -> String
					String termString = ""; 
					// concatenate all the posting unit ids of one term together
					for(long pUId : termPosting) {
						termString += " " + pUId;
					}
					termStrings.add(term + termString + "\r\n");
					}
				for(String tS : termStrings) {
					lf.write(tS); // write posting units into file, each line per unit
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				lf.flush();
				lf.close();
			}
			
			// persist posting list
			long curPUnitId = 0L; // TODO: for testing
			FileWriter pf = new FileWriter(configs.index_config.posting_persistance_path);
			try {
				for(String term : lexicon.keySet()) {
					ArrayList<String> pUnitStrings = new ArrayList<String>(); // the flattened posting units of one term in lexicon
					ArrayList<Long> postingUnitIds = lexicon.get(term);
					for(Long pUnitId : postingUnitIds) {
						String pUnitString = postUnitMap.get(pUnitId).flatten();
						pUnitStrings.add(term + " " + pUnitString + "\r\n"); // [term] currentId nextId previousId {uProp}
						curPUnitId = pUnitId;
					}
					
					for(String uS : pUnitStrings) {
						pf.write(uS); // write posting units into file, each line per unit
					}	
				}
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println("--> error pUnitId: " + curPUnitId);
			} finally {
				pf.flush();
				pf.close();
			}
			
			// persist last post unit id
			FileWriter idf = new FileWriter(configs.index_config.last_post_unit_id_path);
			try {
				idf.write("" + lastPostUnitId);
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				idf.flush();
				idf.close();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	// different from add_posting_unit
	// does not generate new postingId
	// does not operate lexicon
	// only operate the postUnitMap and link the units
	// thus the persisted posting needs to be correct, 
	// if one unit miss its previous one, it will lead to error
	public long load_posting_unit(String term, posting_unit postUnit) {
		long addedUnitId = -1L;
		
		try {
			// only one thread sequentially scanning the posting list, does not need the locks
			postUnitMap.put(postUnit.currentId, postUnit);
			
			// link units
			long previousUnitId = postUnit.previousId;
			posting_unit prevUnit = postUnitMap.get(previousUnitId); // get the instance of previous unit
			
			postUnit.link_to_previous(prevUnit);
			if (prevUnit != null) {
				prevUnit.link_to_next(postUnit);
			}
			addedUnitId = postUnit.currentId;
			
		} catch (Exception e) {
			e.printStackTrace();
			new unit_add_fail_exception(String.format("Unit %s added failed", "" + postUnit.currentId)).printStackTrace();
		}
		return addedUnitId;
	}
	
	
	public void load_lexicon() {
		
		try {
			// load the whole lexicon firstly, for the early stop of loading posting units
			FileReader lf = new FileReader(configs.index_config.lexicon_persistance_path);
			BufferedReader lb = new BufferedReader(lf);
			try {			
				String termString;
				do {
					termString = lb.readLine();
					
					if (termString != null) {
						termString = termString.trim();
						String[] tempList = termString.split(" "); // term p1 p2 p3 ...
						String term = tempList[0];
						String[] pUnitIds = Arrays.asList(tempList).subList(1, tempList.length).toArray(new String[0]); // loading from the file, due to not using json, its strings
						
						// prepare the arrayList of posting Ids for each term
						// not initialising the starter units for term
						ArrayList<Long> postingUnitIds = new ArrayList<Long>();
						lexicon.put(term, postingUnitIds);  
						
						for (String pUnitId : pUnitIds) {
							lexicon.get(term).add(Long.parseLong(pUnitId)); // String -> Long
						}
						
						// create lock in keeper
						kpr.add_target(lexicon_locker.class, term);
					}
				} while (termString != null);
				
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				lb.close();
				lf.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getClass().equals(java.io.FileNotFoundException.class)) {
				file_creater.create_file(configs.index_config.lexicon_persistance_path);
			};
		}
		
		
		try {
			// load last post unit id
			FileReader idf = new FileReader(configs.index_config.last_post_unit_id_path);
			BufferedReader idfb = new BufferedReader(idf);
			try {
				String idString = idfb.readLine();
					
				if (idString != null) {
					idString = idString.trim();
					lastPostUnitId = Long.parseLong(idString);
					pc.postingId = lastPostUnitId + 10; // set the pc so that will not overwrite the old units
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				idf.close();
				idfb.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getClass().equals(java.io.FileNotFoundException.class)) {
				file_creater.create_file(configs.index_config.last_post_unit_id_path);
			};
		}
		
	}
		
	
	
	// check if the last posting unit is existing in the posting list of term, to check if the term is loaded 
	private boolean checkTermLoaded(String term) {
		boolean loadedFlag = false;
		ArrayList<Long> pUnitIds = lexicon.get(term);
		if(pUnitIds.size() > 1) {
			// cannot use the last UnitId to check if the term is loaded, as if persist after a new adding, this will checking the newly added unit
			// cannot use the second, as if the idx is cleaned to empty, only starters left, the [1] will always be newly added ones, thus starters will never be loaded
			// TODO: use [0], as one generated, it will always in the local file?
			long pUnitSecondId = pUnitIds.get(0);  
			loadedFlag = postUnitMap.containsKey(pUnitSecondId);
		}
		return loadedFlag;
	}
	
	
	// lazily load the posting list of target terms
	public long[] load_posting(String[] targetTerms) {
		long[] loaded_units = new long[] {};
		HashSet<String> targetTermsSet = new HashSet<String>(Arrays.asList(targetTerms));

		// check if a term is loaded, if it is, remove from the targetTermsSet
		for(String term : targetTerms) {
			if(checkTermLoaded(term)) { // if loaded
				targetTermsSet.remove(term);
			}
		}
		
		if(targetTermsSet.size() != 0) {
			try {
				FileReader pf = new FileReader(configs.index_config.posting_persistance_path);
				BufferedReader pb = new BufferedReader(pf);
				
				try {
					// calculate how many units need to be loaded in total
					long totalUnits = 0L;
					for (String term : targetTerms) {
						totalUnits += lexicon.get(term).size();
					}
					
					// load the posting lists of targetTerms
					long addedUnits = 0L; // counting how many units have already been added, if > the totalUnits, stop scanning
					
					String pUnitString;
					do {
						pUnitString = pb.readLine();
						
						if (pUnitString != null) {
							pUnitString = pUnitString.trim();
							String term = pUnitString.split(" ")[0];
							String persistedUnit = pUnitString.substring(term.length() + 1, pUnitString.length()); // term currentId nextId ..., sub string from the "c.."
							
							if (targetTermsSet.contains(term)) { // check if the term is in one of the targets					
								load_posting_unit(term,  posting_unit.deflatten(persistedUnit)); 
								addedUnits ++;
							}
							
							// early stop
							// so that do not scan the whole posting list each time load the posting list into memory
							// TODO: this in fact is not a very efficient early stopping strategy, use offset?
							if (addedUnits >= totalUnits) {
								break;
							}
							
						}
					} while(pUnitString != null);
					
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					pf.close();
					pb.close();
				}
			} catch(Exception e) {
				e.printStackTrace();
				if(e.getClass().equals(java.io.FileNotFoundException.class)) {
					file_creater.create_file(configs.index_config.posting_persistance_path);
				};
			}
		}
		
		return loaded_units;
	}
	
	
	// load all the postings into memory, 
	// used before the persistence, and reload
	public void load_all_posting() {
		String[] allTerms = lexicon.keySet().toArray(new String[0]);
		load_posting(allTerms);
	}
	
	
	// reset index
	// TODO: when use this method need to be very careful, as it will lead to the pc -> 0
	public void clear_index() {
		postUnitMap = new HashMap<Long, posting_unit>();
		lexicon = new HashMap<String, ArrayList<Long>>();
		kpr.clear_maps(lexicon_locker.class);
		pc = new counters();
	}
	
	
	// before persist load all the postings into memory
	public void persist_index() {
		load_all_posting();
		// TODO: TEST
		index_probe idxProb = new index_probe();
		idxProb.show();
		_persist_index();
	}
	
	
	// re-generate the posint unit ids
	// fully scan the persisted posting
	// single threading one, as only processing the persisted posting instead of the docs
	public void reload_index() {
		
		// if not cleaned by cleaner, this reload will not work as expected,
		// as the deleted unit are still in the postUnitMap
		// TODO: alternatively, the cleaner could be running in an independent process
		cleaner clr = cleaner.getInstance();
		
		// load all the postings into memory; 
		// without this step, due to the lazy loading of posting list, 
		// delete_doc will only load small part of units into memory, 
		// the reload_index will erase the unloaded units from local file
		load_all_posting();
		clr.clean();
		
		_persist_index(); // at this time the ids are not corrected
		clear_index();
		
		try {
			FileReader pf = new FileReader(configs.index_config.posting_persistance_path);
			BufferedReader pb = new BufferedReader(pf);
			
			try {
				// load the posting lists of targetTerms

				
				String pUnitString;
				do {
					pUnitString = pb.readLine();
					
					if (pUnitString != null) {
						pUnitString = pUnitString.trim();
						String term = pUnitString.split(" ")[0];
						String persistedUnit = pUnitString.replaceFirst(term + " ", ""); // substring(term.length() + 1, pUnitString.length()); // term currentId nextId ..., sub string from the "c.."
						
						if (lexicon.containsKey(term) == false) { // add the term into lexicon for the first time it was seen
							add_term(term);
						}
						posting_unit pUnit = posting_unit.deflatten(persistedUnit);
						if(pUnit.previousId != -1) { // skip the starter unit, as they are regenerated when add term
							_add_posting_unit(term, pUnit); // re assign the ids, and link the units; when the idx is empty, only starters left, they are not going to be loaded into memory, so that the lastUnitId will not be updated
						}
					}
				} while(pUnitString != null);
			
				_persist_index(); // the ids are corrected
				
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				pf.close();
				pb.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}	
	}
}





