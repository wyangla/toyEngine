package inverted_index;
import entities.scanner_plugins.*;
import java.util.*;
import utils.*;
import java.lang.reflect.*;
import configs.*;
import entities.*;
import entities.scanner_plugins.*;
import entities.information_manager_plugins.*;
import data_structures.*;


public class index_advanced_operations {
	
	private scanner snr = new scanner();
	private index idx = index.get_instance();
	private information_manager infoManager = information_manager.get_instance();
	
	
	// delete a specific document from the inverted-index
	public ArrayList<Long> delete_doc(String[] containedTerms, String targetDocName) throws Exception { // the containedTerms are generated and provided by the engine operator
		ArrayList<Long> totalAffectedUnitIds = new ArrayList<Long>();
		ArrayList<scanner.scan_term_thread> threadList = new ArrayList<scanner.scan_term_thread>();
		
		ArrayList<String[]> workLoads = task_spliter.get_workLoads_terms(general_config.cpuNum, containedTerms);
		
		for(String[] workLoad : workLoads ) {
			scanner.scan_term_thread st = new scanner.scan_term_thread(snr, delete_doc.class, targetDocName, workLoad);
			st.run();
			threadList.add(st);
		}
		
		for(scanner.scan_term_thread st : threadList) {
			st.join();
			totalAffectedUnitIds.addAll(st.get_affectedUnitIds());
		}
		
		idx.docMap.remove(targetDocName); // remove from the doc map
		
		return totalAffectedUnitIds;
	}
	
	
//	// single thread method, fast when the posting list is short?
//	// delete a specific document from the inverted-index
//	public ArrayList<Long> delete_doc(String[] containedTerms, String targetDocName) throws Exception { // the containedTerms are generated and provided by the engine operator
//		ArrayList<Long> affectedUnitIds = new ArrayList<Long>();
//		
//		// here not using the path to find the plugins, as the design for scanner is not for command input 
//		delete_doc.set_parameters(targetDocName);
//		affectedUnitIds = snr.scan(containedTerms, delete_doc.class); // input the class which contains the parameters
//		
//		return affectedUnitIds;
//	}

	
//	public counter search(String[] targetTerms) {
//		counter totalDocumentScoreCounter = new counter(); // used for merging all the result of searching each term
//		ArrayList<scanner.scan_term_thread> threadList = new ArrayList<scanner.scan_term_thread>();
//		ArrayList<counter> counterList = new ArrayList<counter>();
//		
//		ArrayList<String[]> workLoads = task_spliter.get_workLoads_terms(general_config.cpuNum, targetTerms);
//		
//		for(String[] workLoad : workLoads) {
//			counter documentScoreCounter = new counter();
//			scanner.scan_term_thread st = new scanner.scan_term_thread(snr, search_term.class, documentScoreCounter, workLoad);
//			st.run();
//			threadList.add(st); 
//			counterList.add(documentScoreCounter);
//		}
//		
//		for(scanner.scan_term_thread st : threadList) {
//			try {
//				st.join();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		
//		// merging all the searching result
//		for(counter c : counterList) {
//			System.out.println(c);
//			totalDocumentScoreCounter = totalDocumentScoreCounter.update(c);
//		}
//		System.out.println("--");
//		System.out.println(totalDocumentScoreCounter);
//		return totalDocumentScoreCounter;
//	}
	
	
	public counter search(String[] targetTerms) {
		counter totalDocumentScoreCounter = new counter(); // used for merging all the result of searching each term
		ArrayList<scanner.scan_term_thread> threadList = new ArrayList<scanner.scan_term_thread>();
		ArrayList<counter> counterList = new ArrayList<counter>();
		
		// DAAT
		for(String term : targetTerms) {
			counter documentScoreCounter = new counter();
			scanner.scan_term_thread st = new scanner.scan_term_thread(snr, search_term.class, documentScoreCounter, new String[] {term});
			st.run();
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
			System.out.println(c);
			totalDocumentScoreCounter = totalDocumentScoreCounter.update(c);
		}
		System.out.println("--");
		System.out.println(totalDocumentScoreCounter);
		return totalDocumentScoreCounter;
	}
	
	
	// get the upper bound score of documents, currently infact is the upper bound tf
	private counter get_upper_bounds(String[] targetTerms) {
		counter documentUpperBoundScores = new counter(); // used for merging all the result of searching each term
		ArrayList<scanner.scan_term_thread> threadList = new ArrayList<scanner.scan_term_thread>();
		ArrayList<counter> counterList = new ArrayList<counter>();
		
		for(String term : targetTerms) {
			counter documentScoreCounter = new counter();
			scanner.scan_term_thread st = new scanner.scan_term_thread(snr, get_upper_bound_score.class, documentScoreCounter, new String[] {term});
			st.run();
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
		for(counter c : counterList) {
			documentUpperBoundScores = documentUpperBoundScores.update(c);
		}
		return documentUpperBoundScores;
	}
	
	
	private class param_maxScore{
		counter docUpBounds;
		counter totalDocSC;	// changing
		int tK;
		
		param_maxScore(counter documentUpperBoundScores, counter totalDocumentScoreCounter, int topK){
			docUpBounds = documentUpperBoundScores;
			totalDocSC = totalDocumentScoreCounter;
			tK = topK;
		}
		
		// use the current docId replace the one with min score
		synchronized String try_to_replace_min_score_doc(posting_unit p){
			String minScoreDocId = "";
			// use counter min
			// compare the current with the min
			return minScoreDocId;
		}
	}
	
	// MaxScore searching
	public counter search_MaxScore(String[] targetTerms, int topK) {
		counter documentUpperBoundScores = get_upper_bounds(targetTerms);	// pass
		counter totalDocumentScoreCounter = new counter();	// pass
		
		
		ArrayList<scanner.scan_term_thread> threadList = new ArrayList<scanner.scan_term_thread>();
		ArrayList<counter> counterList = new ArrayList<counter>();
		
//		for(String term : targetTerms) {
//			scanner.scan_term_thread st = new scanner.scan_term_thread(snr, get_upper_bound_score.class, totalDocumentScoreCounter, new String[] {term});
//			st.run();
//			threadList.add(st); 
//			counterList.add(totalDocumentScoreCounter);
//		}
//		for(scanner.scan_term_thread st : threadList) {
//			try {
//				st.join();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		
		return totalDocumentScoreCounter;
	}
	
	
}
