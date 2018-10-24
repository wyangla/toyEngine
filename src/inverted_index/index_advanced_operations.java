package inverted_index;
import entities.scanner_plugins.*;
import java.util.*;
import utils.*;
import java.lang.reflect.*;
import configs.*;
import entities.scanner;
import entities.scanner_plugins.delete_doc;
import entities.scanner_plugins.search_term;


// operatiosn demands scanning the posting list
public class index_advanced_operations {
	
	scanner snr = new scanner();
	
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
	

	
	public counter search(String[] targetTerms) {
		counter totalDocumentScoreCounter = new counter(); // used for merging all the result of searching each term
		ArrayList<scanner.scan_term_thread> threadList = new ArrayList<scanner.scan_term_thread>();
		ArrayList<counter> counterList = new ArrayList<counter>();
		
		ArrayList<String[]> workLoads = task_spliter.get_workLoads_terms(general_config.cpuNum, targetTerms);
		
		for(String[] workLoad : workLoads) {
			counter documentScoreCounter = new counter();
			scanner.scan_term_thread st = new scanner.scan_term_thread(snr, search_term.class, documentScoreCounter, workLoad);
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
	
}
