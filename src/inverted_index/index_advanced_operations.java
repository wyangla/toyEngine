package inverted_index;
import entities.scanner_plugins.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import utils.*;
import java.lang.reflect.*;
import configs.*;
import entities.*;
import entities.scanner_plugins.*;
import entities.scanner_plugins.parameters.*;
import entities.information_manager_plugins.*;
import data_structures.*;



public class index_advanced_operations {
	
	private scanner snr = new scanner();
	private index idx = index.get_instance();
	private information_manager infoManager = information_manager.get_instance();
	
	
	// delete a specific document from the inverted-index -- using scanner
	public ArrayList<Long> delete_doc(String[] containedTerms, String targetDocName) throws Exception { // the containedTerms are generated and provided by the engine operator
		ArrayList<Long> totalAffectedUnitIds = new ArrayList<Long>();
		ArrayList<scanner.scan_term_thread> threadList = new ArrayList<scanner.scan_term_thread>();
		
		ArrayList<String[]> workLoads = task_spliter.get_workLoads_terms(general_config.cpuNum, containedTerms);
		
		for(String[] workLoad : workLoads ) {
			scanner.scan_term_thread st = new scanner.scan_term_thread(snr, delete_doc.class, targetDocName, workLoad);
			st.start();
			threadList.add(st);
		}
		
		for(scanner.scan_term_thread st : threadList) {
			st.join();
			totalAffectedUnitIds.addAll(st.get_affectedUnitIds());
		}
		
		idx.docMap.remove(targetDocName); // remove from the doc map
		
		return totalAffectedUnitIds;
	}
	
	
	
	// this is a common methods, as the score calculating method is set by config.scorer_config
	// all fast scoring method are try to calculating fewer documents
	// but when the calculation process is done, the normalisation procedure are the same
	public counter normalise_doc_scores(counter docScoreCounter) {
		 System.out.println(docScoreCounter);    // TODO: test
		 System.out.println("---");    // TODO: test
		counter docNormScoreCounter = new counter();
		counter docNameNormScoreCounter = new counter();
		
		ArrayList<scanner.scan_doc_thread> threadList = new ArrayList<scanner.scan_doc_thread> ();
		ArrayList<counter> counterList = new ArrayList<counter>();
		counter docLenCounter = new counter();
		
		// term_idf_cal should be calculated before the doc len is calcualted
		Double term_idf_cal_time = infoManager.get_info(term_idf_cal_time.class, "term_idf_cal_time");
		if(term_idf_cal_time == null) {
			idx.cal_termIdf();
		}
		
		// calculate the document length 
		for(String docIdStr : docScoreCounter.keySet()) {
			doc docIns = idx.docIdMap.get(Long.parseLong(docIdStr));
			
			if(docIns != null) {				
								
				Double doc_len_cal_time = docIns.docProp.get("doc_len_cal_time");
				
				// check if the doc_len is not calculated or expired
				if (doc_len_cal_time == null || doc_len_cal_time < term_idf_cal_time) {  
//					System.out.println("doc_len_cal_time -> " + doc_len_cal_time);    // TODO: test
//					System.out.println("term_idf_cal_time -> " + term_idf_cal_time);    // TODO: test
//					System.out.println();    // TODO: test
					
					counter subDocLenCounter = new counter();
					scanner.scan_doc_thread st = new scanner.scan_doc_thread(
							snr, 
							get_doc_length.class, 
							subDocLenCounter, 
							new String[] {docIdStr});
					
					st.start();
					threadList.add(st); 
					counterList.add(subDocLenCounter);
				}
			}	
		}
		
		for(scanner.scan_doc_thread st : threadList) {
			try {
				st.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		for(counter subDocLenCounter : counterList) {
			System.out.println(subDocLenCounter);    // TODO: test
			docLenCounter = docLenCounter.update(subDocLenCounter);
		}
		System.out.println();    // TODO: test
		
		
		// update the docIns.docProp.doc_len
		for(String docIdStr : docLenCounter.keySet()) {
			doc docIns = idx.docIdMap.get(Long.parseLong(docIdStr));
			
			// not using the info manager here, as its directly set info to object
			docIns.docProp.put("doc_len", Math.sqrt(docLenCounter.get(docIdStr)));
			docIns.docProp.put("doc_len_cal_time", (double)System.currentTimeMillis());
		}
		
		// normalisation
		for(String docIdStr : docScoreCounter.keySet()) {
			doc docIns = idx.docIdMap.get(Long.parseLong(docIdStr));			
			if (docIns != null) {
				Double docLen = docIns.docProp.get("doc_len");
				Double docNormScore = docScoreCounter.get(docIdStr) / docLen;
				docNormScoreCounter.put(docIdStr, docNormScore);
				docNameNormScoreCounter.put(docIns.docName, docNormScore);
			}
		}
		
		docNameNormScoreCounter.sort();
		return docNameNormScoreCounter;
	}
	
	
	public counter search(String[] targetTerms) {
		counter totalDocumentScoreCounter = new counter(); // used for merging all the result of searching each term
		ArrayList<scanner.scan_term_thread> threadList = new ArrayList<scanner.scan_term_thread>();
		ArrayList<counter> counterList = new ArrayList<counter>();
		
		// DAAT
		for(String term : targetTerms) {
			counter documentScoreCounter = new counter();
			scanner.scan_term_thread st = new scanner.scan_term_thread(snr, search_term.class, documentScoreCounter, new String[] {term});
			st.start();
			threadList.add(st); 
			counterList.add(documentScoreCounter);
		}
		
		for(scanner.scan_term_thread st : threadList) {
			try {
				st.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// merging all the searching result
		for(counter c : counterList) {
			// System.out.println(c);
			totalDocumentScoreCounter = totalDocumentScoreCounter.update(c);
		}
		// System.out.println("--");
		// System.out.println(totalDocumentScoreCounter);
		totalDocumentScoreCounter.sort();
		return totalDocumentScoreCounter;
	}
	
	
	public counter search_normalized(String[] targetTerms) {
		counter docNormScoreCounter = normalise_doc_scores(search(targetTerms));
		return docNormScoreCounter;
	}
	
	
	/*
	 * 
	 * MaxScore
	 * 
	 * */
	
	
	// get the upper bound score of term
	// pUnit with maxTf, (term: scorer.cal_score())
	public counter get_term_upper_bounds(String[] targetTerms) {
		scorer scr = scorer.getInstance();
		counter termMaxScores = new counter();
		for(String term : targetTerms) {
			Double maxTf = infoManager.get_info(term_max_tf.class, term);
			if(maxTf == null) {
				maxTf = 0.0;
			}
			posting_unit maxTfPUnit = new posting_unit();
			maxTfPUnit.term = term;		// term need to be set as it is used in tfidf to get the df value
			maxTfPUnit.uProp.put("tf", maxTf);
			Double score = scr.cal_score(maxTfPUnit);
			
			termMaxScores.put(term, score);

		}
		return termMaxScores;
	}
	
	
	// fist scan, using the termMaxScore to ge the upper bound instead of the actual caluclation of score
	public counter get_doc_upper_bounds(String[] targetTerms) {
		counter totalDocumentUpperBoundScores = new counter(); // used for merging all the result of searching each term
		ArrayList<scanner.scan_term_thread> threadList = new ArrayList<scanner.scan_term_thread>();
		ArrayList<counter> counterList = new ArrayList<counter>();
		
		counter termMaxScores = get_term_upper_bounds(targetTerms);
		
		for(String term : targetTerms) {
			counter documentUpperBoundScores = new counter();
			scanner.scan_term_thread st = new scanner.scan_term_thread(
					snr, 
					get_doc_upper_bound_score.class, 
					new param_get_doc_upper_bound_score(termMaxScores, documentUpperBoundScores), 
					new String[] {term});
			
			st.start();
			threadList.add(st); 
			counterList.add(documentUpperBoundScores);
		}
		for(scanner.scan_term_thread st : threadList) {
			try {
				st.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for(counter c : counterList) {
			totalDocumentUpperBoundScores = totalDocumentUpperBoundScores.update(c);
		}
		return totalDocumentUpperBoundScores;	
	}
	
	
	// MaxScore searching
	// totalDocumentScoreCounter is shared here, so that need synchronisation
	public counter search_maxScore(String[] targetTerms, int topK) {
		counter docUpperBounds = get_doc_upper_bounds(targetTerms);	// pass
		counter totalDocumentScoreCounter = new counter();	// pass
		
		ArrayList<scanner.scan_term_thread> threadList = new ArrayList<scanner.scan_term_thread>();
		
		for(String term : targetTerms) {
			scanner.scan_term_thread st = new scanner.scan_term_thread(
					snr, 
					search_term_maxScore.class, 
					new param_search_term_maxScore(scorer.getInstance(), docUpperBounds, totalDocumentScoreCounter, topK),
					new String[] {term});
			st.start();
			threadList.add(st); 
		}
		for(scanner.scan_term_thread st : threadList) {
			try {
				st.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		totalDocumentScoreCounter.sort();	// sort the searching result big -> small, topK are ensured to be good result
		return totalDocumentScoreCounter;
	}
	
	
	/*
	 * 
	 * WAND
	 * 
	 * */
	
	// return the sets filled by the docIds of terms
	public ConcurrentHashMap<String, HashSet<Long>> get_term_docId_set(String[] targetTerms) {
		ConcurrentHashMap<String, HashSet<Long>> termDocIdSetMap = new ConcurrentHashMap<String, HashSet<Long>>();
		
		ArrayList<scanner.scan_term_thread> threadList = new ArrayList<scanner.scan_term_thread>();
		for(String term : targetTerms) {
			HashSet<Long> docIdSet = new HashSet<Long>();
			scanner.scan_term_thread st = new scanner.scan_term_thread(
					snr, 
					get_docId_set.class, 
					docIdSet, 
					new String[] {term});
			
			st.start();
			threadList.add(st); 
			termDocIdSetMap.put(term, docIdSet);	// put all docId set into the map
		}
		for(scanner.scan_term_thread st : threadList) {
			try {
				st.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return termDocIdSetMap;
	}
	
	
	// WAND searching
	// totalDocumentScoreCounter is shared here, so that need synchronisation
	public counter search_WAND(String[] targetTerms, int topK) {
		Iterator<Map.Entry<String, Double>> termMaxScoresIterator = get_term_upper_bounds(targetTerms).sort().iterator(); // pass
		HashSet<Long> validDocSet = new HashSet<Long>(); // pass, intersections of docId sets of terms
		counter currentUpperBound = new counter(); // pass, current upper bound
		currentUpperBound.put("currentUpperBound", 0.0);
		
		ConcurrentHashMap<String, HashSet<Long>> termDocIdSetMap = get_term_docId_set(targetTerms); // pass, map from term to docId set
		counter totalDocumentScoreCounter = new counter();	// pass
		
		ArrayList<scanner.scan_term_thread> threadList = new ArrayList<scanner.scan_term_thread>();
		
		for(String term : targetTerms) {
			scanner.scan_term_thread st = new scanner.scan_term_thread(
					snr, 
					search_term_WAND.class, 
					new param_search_term_WAND(scorer.getInstance(), termMaxScoresIterator, validDocSet, currentUpperBound, termDocIdSetMap, totalDocumentScoreCounter, topK),
					new String[] {term});
			st.start();
			threadList.add(st); 
		}
		for(scanner.scan_term_thread st : threadList) {
			try {
				st.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		totalDocumentScoreCounter.sort();	// sort the searching result big -> small, topK are ensured to be good result
		return totalDocumentScoreCounter;
	}
	
	
	
	
}
