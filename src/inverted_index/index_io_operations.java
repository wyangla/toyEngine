package inverted_index;

import java.io.*;
import java.util.*;

import configs.*;
import data_structures.*;
import entities.*;
import entities.information_manager_plugins.*;
import entities.keeper_plugins.*;
import entities.scanner_plugins.*;
import exceptions.*;
import probes.*;
import utils.*;



// reason the methods in this class are most static, is for the convenience of usage in index
public class index_io_operations {
	
	private static index idx = index.get_instance();
	private static keeper kpr = keeper.get_instance();
	private static information_manager infoManager = information_manager.get_instance();
	
	
	// here use the lazy loading singleton is for the convenience of reuse in index 
	private index_io_operations() {}
	private static index_io_operations idxIOOps = null;
	public static index_io_operations get_instance() {
		if(idxIOOps == null) {
			idxIOOps = new index_io_operations();
		}
		return idxIOOps;
	}
	
	
	
//	private void persist_lexicon() {
//		try {
//			// persist lexicon
//			FileWriter lf = new FileWriter(configs.index_config.lexiconPersistancePath);
//			try {
//				ArrayList<String> termStrings = new ArrayList<String>();
//				for(String term : idx.lexicon.keySet()) {
//					Long[] termPosting = idx.lexicon.get(term).toArray(new Long[0]); // ArrayList -> String
//					String termString = ""; 
//					// concatenate all the posting unit ids of one term together
//					for(long pUId : termPosting) {
//						termString += " " + pUId;
//					}
//					termStrings.add(term + termString + "\r\n");
//					}
//				for(String tS : termStrings) {
//					lf.write(tS); // write posting units into file, each line per unit
//				}
//			} catch(Exception e) {
//				e.printStackTrace();
//			} finally {
//				lf.flush();
//				lf.close();
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	
	// persist the new version of lexicon
	private void persist_lexicon_2() {
		try {
			// persist lexicon2
			FileWriter lf = new FileWriter(configs.index_config.lexicon2PersistancePath);
			try {
				ArrayList<String> termStrings = new ArrayList<String>();
				for(String term : idx.lexicon_2.keySet()) {
					String termString = idx.lexicon_2.get(term).flatten();
					termStrings.add(termString + "\r\n");
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
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
//	// TODO: change to using lexicon_2
//	private void persist_postings() {
//		try {
//			for(String term : idx.lexicon.keySet()) {
//				
//				if(check_term_loaded(term)) { // only try to persist the posting of loaded terms, so that does not need to load all postings before persistance 
//					
//					ArrayList<String> pUnitStrings = new ArrayList<String>(); // the flattened posting units of one term in lexicon
//					ArrayList<Long> postingUnitIds = idx.lexicon.get(term);
//
//					// check the directory of one term is existing or not
//					File postingDir = new File(configs.index_config.postingsPersistancePath + '/' + term);
//					if(!postingDir.exists()) {
//						postingDir.mkdirs();
//					}
//					
//					long curPUnitId = 0L;
//					FileWriter pf = new FileWriter(postingDir.getPath() + "/posting");
//					try {
//						for(Long pUnitId : postingUnitIds) {
//							String pUnitString = idx.postUnitMap.get(pUnitId).flatten();
//							pUnitStrings.add(pUnitString + "\r\n"); // [term] currentId nextId previousId {uProp}
//							curPUnitId = pUnitId;
//						}
//						for(String uS : pUnitStrings) {
//							pf.write(uS); // write posting units into file, each line per unit
//						}	
//
//					} catch(Exception e) {
//						e.printStackTrace();
//						System.out.println("--> error pUnitId: " + curPUnitId);
//					} finally {
//						pf.flush();
//						pf.close();
//					}
//				} 
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	
	private void persist_postings_2() {
		try {
			scanner snr = new scanner();
			ArrayList<scanner.scan_term_thread_no_loading> threadList = new ArrayList<scanner.scan_term_thread_no_loading>();
			ArrayList<String> loadedTerms = new ArrayList<String>();
			ArrayList<String> emptyTerms = new ArrayList<String>();
			
			for(String term: idx.lexicon_2.keySet()) {
				term termIns = idx.lexicon_2.get(term);
				if(check_term_loaded(term)) {
					if(termIns.firstPostUnitId != -1) {
						loadedTerms.add(term);
					}else {
						emptyTerms.add(term);    // if the posting list of one term is empty
					}
				}
			}
			
			// persisting existing posting lists
			ArrayList<String[]> workloads = task_spliter.get_workLoads_terms(index_config.persistWorkNum, loadedTerms.toArray(new String[0]));
			for(String[] workload: workloads) {
				scanner.scan_term_thread_no_loading st = new scanner.scan_term_thread_no_loading(
						snr, 
						new persist_postings(), 
						null, 
						workload,
						false);    // not updating the term visiting status, as this method is also used in deactivator
				
				threadList.add(st);
			}

			for(scanner.scan_term_thread_no_loading st: threadList) {
				st.start();
				st.join();
			}
			
			// empty the content of file of empty posting lists
			for(String term: emptyTerms) {
				FileWriter eliminatePostingPath = new FileWriter(String.format(configs.index_config.postingsPersistancePath + "/%s/posting", term));
				eliminatePostingPath.write("");
				eliminatePostingPath.flush();
				eliminatePostingPath.close();
			}
					
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void persist_lastPostUnitId() {
		try {
			// persist last post unit id
			FileWriter idf = new FileWriter(configs.index_config.lastPostUnitIdPath);
			try {
				idf.write("" + idx.pc.view());
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				idf.flush();
				idf.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	private void persist_lastDocId() {
		try {
			// persist last doc id
			FileWriter idf = new FileWriter(configs.index_config.lastDocIdPath);
			try {
				idf.write("" + idx.dc.view());
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				idf.flush();
				idf.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
		
	private void persist_docMap() {    // the doc_len_cal_time is stored here
		try {
			// persist docMap
			FileWriter dm = new FileWriter(configs.index_config.docsPath);
			try {
				dm.write(""); // when the docMap is empty, make sure that the docInfo file is emptied
				for(String docName : idx.docMap.keySet()) {
					dm.write(idx.docMap.get(docName).flatten() + "\r\n");
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				dm.flush();
				dm.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void persist_lastTermId() {
		try {
			FileWriter idf = new FileWriter(configs.index_config.lastTermIdPath);
			try {
				idf.write("" + idx.tc.view());
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				idf.flush();
				idf.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void persist_info() {
		// record mtf, df, idf information into the lexicon_2
		infoManager.persist_info(term_idf_cal_time.class);
	}
	

	// stop loading all the postings into memory before persist
	// as the persist_index contains check loading status of posting list
	
	// persist the inverted index on to local hard disk, 
	// with the posting units written in line in the order of posting list
	// this method doesnt load all postings in to memory, so that cannot found all the units in the lexicon
	public void persist_index() {
		// 1. generate, 2. persist, 3. lazily load and serve
		// such that the generation process will consume the biggest amount of memory
		
		persist_lexicon_2();    // TODO: test
		persist_postings_2();    // TODO: uncomment
		persist_lastPostUnitId();
		persist_lastDocId();
		persist_docMap();
		persist_lastTermId();
		persist_info();
	}
	
	
	
	
	
	
	
	
	
	
	
	// different from add_posting_unit
	// does not generate new postingId
	// does not operate lexicon
	// only operate the postUnitMap and link the units
	// thus the persisted posting needs to be correct, 
	// if one unit miss its previous one, it will lead to error
	public long load_posting_unit(posting_unit postUnit) {
		long addedUnitId = -1L;
		
		// TODO: change t ousing lexicon_2
		try {
			// only one thread sequentially scanning the posting list, does not need the locks
			idx.postUnitMap.put(postUnit.currentId, postUnit);
			
			// link units
			long previousUnitId = postUnit.previousId;
			posting_unit prevUnit = idx.postUnitMap.get(previousUnitId); // get the instance of previous unit
			
			postUnit.link_to_previous(prevUnit);
			if (prevUnit != null) {
				prevUnit.link_to_next(postUnit);
			}
			
			addedUnitId = postUnit.currentId;
			
			
		} catch (Exception e) {
			e.printStackTrace();
			new unit_add_fail_exception(String.format("Unit %s added failed", "" + postUnit.currentId)).printStackTrace();
		}
		
		// set the high level information
		infoManager.set_info(posting_loaded_status.class, postUnit);
		
		return addedUnitId;
	}
		
	
//	public void load_lexicon() {
//		try {
//			// load the whole lexicon firstly, for the early stop of loading posting units
//			FileReader lf = new FileReader(configs.index_config.lexiconPersistancePath);
//			BufferedReader lb = new BufferedReader(lf);
//			try {			
//				String termString;
//				do {
//					termString = lb.readLine();
//					
//					if (termString != null) {
//						termString = termString.trim();
//						String[] tempList = termString.split(" "); // term p1 p2 p3 ...
//						String term = tempList[0];
//						String[] pUnitIds = Arrays.asList(tempList).subList(1, tempList.length).toArray(new String[0]); // loading from the file, due to not using json, its strings
//						
//						// prepare the arrayList of posting Ids for each term
//						// not initialising the starter units for term
//						ArrayList<Long> postingUnitIds = new ArrayList<Long>();
//						idx.lexicon.put(term, postingUnitIds);  
//						
//						for (String pUnitId : pUnitIds) {
//							idx.lexicon.get(term).add(Long.parseLong(pUnitId)); // String -> Long
//						}
//						// create lock in keeper
//						kpr.add_target(lexicon_locker.class, term);
//					}
//				} while (termString != null);
//				
//				System.out.println("lexicon loaded");
//				
//			} catch(Exception e) {
//				e.printStackTrace();
//			} finally {
//				lb.close();
//				lf.close();
//			}
//			
//		} catch(Exception e) {
//			e.printStackTrace();
//			if(e.getClass().equals(java.io.FileNotFoundException.class)) {
//				file_creater.create_file(configs.index_config.lexiconPersistancePath);
//			};
//		}
//	}
	
	public void load_lexicon_2() {
		try {
			FileReader lf = new FileReader(configs.index_config.lexicon2PersistancePath);
			BufferedReader lb = new BufferedReader(lf);
			try {			
				String termString;
				do {
					termString = lb.readLine();
					
					if (termString != null) {
						termString = termString.trim();
						term termIns = term.deflatten(termString);
						idx.lexicon_2.put(termIns.termName, termIns);  
						kpr.add_target(lexicon_locker.class, termIns.termName);
					}
				} while (termString != null);
				
				System.out.println("lexicon2 loaded");
				
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				lb.close();
				lf.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getClass().equals(java.io.FileNotFoundException.class)) {
				file_creater.create_file(configs.index_config.lexicon2PersistancePath);
			};
		}
	}
		
	
	// each time the engine is restart, the lastId is added with 10, for safety
	public void load_lastPostUnitId() {
		try {
			// load last post unit id
			FileReader idf = new FileReader(configs.index_config.lastPostUnitIdPath);
			BufferedReader idfb = new BufferedReader(idf);
			try {
				String idString = idfb.readLine();
					
				if (idString != null) {
					idString = idString.trim();
					idx.pc.set(Long.parseLong(idString) + 10); // set the pc so that will not overwrite the old units
				}
				
				System.out.println("lastId loaded");
				
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				idf.close();
				idfb.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getClass().equals(java.io.FileNotFoundException.class)) {
				file_creater.create_file(configs.index_config.lastPostUnitIdPath);
			};
		}
	}
	
	// each time the engine is restart, the lastDocId is added with 10
	public void load_lastDocId() {
		try {
			// load last post unit id
			FileReader idf = new FileReader(configs.index_config.lastDocIdPath);
			BufferedReader idfb = new BufferedReader(idf);
			try {
				String idString = idfb.readLine();
					
				if (idString != null) {
					idString = idString.trim();
					idx.dc.set(Long.parseLong(idString) + 10);
				}
				
				System.out.println("lastDocId loaded");
				
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				idf.close();
				idfb.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getClass().equals(java.io.FileNotFoundException.class)) {
				file_creater.create_file(configs.index_config.lastDocIdPath);
			};
		}
	}
	
	// each time the engine is restart, the lastDocId is added with 10
	public void load_lastTermId() {
		try {
			// load last post unit id
			FileReader idf = new FileReader(configs.index_config.lastTermIdPath);
			BufferedReader idfb = new BufferedReader(idf);
			try {
				String idString = idfb.readLine();
					
				if (idString != null) {
					idString = idString.trim();
					idx.tc.set(Long.parseLong(idString) + 10);
				}
				
				System.out.println("lastTermId loaded");
				
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				idf.close();
				idfb.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getClass().equals(java.io.FileNotFoundException.class)) {
				file_creater.create_file(configs.index_config.lastTermIdPath);
			};
		}
	}
	
	// check if the last posting unit is existing in the posting list of term, to check if the term is loaded
	private boolean check_term_loaded(String term) {
		boolean loadedFlag = false;
		Double lf = infoManager.get_info(posting_loaded_status.class, term);
		// TODO: test
		// System.out.println("-->" + lf);
		if(lf != -1) {	// as using the termIns to record the loaded status now
			loadedFlag = true;
		}
		return loadedFlag;
	}
	
	
	// link term units after all related units are loaded
	// append to the load_posting_unit, instead of written inside of it,
	// as when load_doc_related_posting, the order of posting units loaded will different from how they are generated, 
	// i.e. the order of the cached doc terms, is different form the feed in pUnits order, 
	// so potentially previous units could not been loaded yet
	// then the chain will be broken
	
	public void link_term_chain(long docId) {
		for(long postUnitId : idx.postUnitMap.keySet()) {
			posting_unit postUnit = idx.postUnitMap.get(postUnitId);
			
			// only care about the doc related units, otherwise when try to chain other documents, can not be sure all the necessary units are loaded
			if(postUnit.docId == docId) {
				long previousTermId = postUnit.previousTermId;
				posting_unit prevTermUnit = idx.postUnitMap.get(previousTermId);
				
				postUnit.link_to_previous_term(prevTermUnit);
				if (prevTermUnit != null) {
					prevTermUnit.link_to_next_term(postUnit);
				}
				
				// TODO: test
				if(postUnit.docId == 0) {
					if (prevTermUnit != null) {
						System.out.println( previousTermId + " : " + prevTermUnit.term + "--> " + postUnit.term);
					}else {
						System.out.println( previousTermId + " : null --> " + postUnit.term);
					}
				}
			}

		}
	}
	

	// lazily load the posting list of target terms
	public long[] load_posting(String[] targetTerms) {
		long[] loaded_units = new long[] {};

		for(String term : targetTerms) {
			if(!check_term_loaded(term) && idx.lexicon_2.containsKey(term)) { // if not loaded and term existing in lexicon, TODO: change to lexicon_2
				
				String postingPath = configs.index_config.postingsPersistancePath + "/" + term + "/posting";
				try {
					FileReader pf = new FileReader(postingPath);
					BufferedReader pb = new BufferedReader(pf);
					try {
						// load the posting lists of one term
						String pUnitString;
						do {
							pUnitString = pb.readLine();
							if (pUnitString != null) {
								pUnitString = pUnitString.trim();
								load_posting_unit(posting_unit.deflatten(pUnitString)); 
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
						file_creater.create_file(postingPath);
					};
				}
				
			}
		}
		
		return loaded_units;
	}
	

	// status among [load_doc_related_postings -> scan_doc_thread finished] needs to be consistent
	public void load_doc_related_postings(long docId) {
		doc docIns = idx.docIdMap.get(docId);
		String docPath = general_config.cachedDocPath + '/' + docIns.docName;
		String processedDoc = "";
		
		try {
			FileReader lf = new FileReader(docPath);
			BufferedReader lb = new BufferedReader(lf);
			try {			
				processedDoc = lb.readLine();    // the processed document only contains one line
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				lb.close();
				lf.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		String[] targetTerms = processedDoc.split(" ");
		
		load_posting(targetTerms);
		link_term_chain(docId);
	}
	
	
	// load all the postings into memory, 
	// used before the persistence, and reload
	public void load_all_posting() {
		String[] allTerms = idx.lexicon_2.keySet().toArray(new String[0]);    // TODO: change to lexicon_2
		load_posting(allTerms);
	}
	
	
	public void load_docMap() {
		
		try {
			// load the whole lexicon firstly, for the early stop of loading posting units
			FileReader df = new FileReader(configs.index_config.docsPath);
			BufferedReader db = new BufferedReader(df);
			try {			
				String docString;
				do {
					docString = db.readLine();
					if (docString != null) {
						docString = docString.trim();
						doc docIns = doc.deflatten(docString);
						idx.docMap.put(docIns.docName, docIns);
					}
				} while (docString != null);
				
				System.out.println("doc map loaded");
				
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
				df.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getClass().equals(java.io.FileNotFoundException.class)) {
				file_creater.create_file(configs.index_config.docsPath);
			};
		}		
	}
	
	
	public void reconstruct_docIdMap() {
//		System.out.print(idx.lastDocId);  // TODO: test
		for (String docName : idx.docMap.keySet()) {
			doc docIns = idx.docMap.get(docName);
			idx.docIdMap.put(docIns.docId, docIns);
		}
		
		System.out.println("docIdMap reconstructed");
	}
	
	
	public void load_info() {
		// loading maxTf, df, idf can be eliminated here, as they are recorded in lexicon_2
		infoManager.load_info(term_idf_cal_time.class);    // the invoking of cal_term_idf needs to be invoked after all adding finished
		System.out.println("info loaded");
	}
	
	
	public void load_index() {
		load_lexicon_2();
		load_lastPostUnitId();
		load_lastDocId();
		load_docMap();
		load_lastTermId();  // TODO: test
		reconstruct_docIdMap();
		load_info();
	}
	
}
