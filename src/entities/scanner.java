package entities;
import java.util.*;
import java.lang.reflect.*;
import java.io.*;
import configs.*;
import entities.keeper_plugins.lexicon_locker;
import entities.scanner_plugins.delete_doc;
import inverted_index.*;
import utils.name_generator;
import utils.task_spliter;
import entities.information_manager_plugins.*;
import data_structures.*;



// This class invokes corresponding plugins to conduct the operations on each unit on posting lists

public class scanner {
	private static index idx = index.get_instance();
	private static index_io_operations idxIOOps = index_io_operations.get_instance();
	private static keeper kpr = keeper.get_instance();
	private static information_manager infoManager = information_manager.get_instance();
	
	
	
	public void visit_next_unit (posting_unit pUnitCurrent, Class operationOnPostingList, ArrayList<Long> affectedUnits) throws Exception { // use the reference to visit the unit directly instead of searching in the HashMap
		
		if(pUnitCurrent != null) {    // TODO: skip the starter unit?

			Method conduct = operationOnPostingList.getMethod("conduct", posting_unit.class); // the class object already provide the necessary parameters
			long affectedUnitId = (long)conduct.invoke(operationOnPostingList, pUnitCurrent);// object -> long
			if(affectedUnitId != -1) { // -1 denotes the processed unit was not affected
				affectedUnits.add(affectedUnitId); 
			}
			visit_next_unit(pUnitCurrent.nextUnit, operationOnPostingList, affectedUnits);
		}
	}
	
	
	public String scan_posting_list(String term, Class operationOnPostingList, ArrayList<Long> affectedUnits) {
		String processedTerm = term;
		
		// get the starter post unit id
		ArrayList<Long> postUnitIds = idx.lexicon.get(term);
		
		if(postUnitIds != null) {
			posting_unit pUnitStarter = idx.postUnitMap.get(postUnitIds.get(0));
			infoManager.set_info(posting_loaded_status.class, pUnitStarter);	// update the visiting time in posting_load_status, TODO: move to position after visit_next_unit?
			
			// load the unit operations
			try {
				visit_next_unit(pUnitStarter, operationOnPostingList, affectedUnits);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
		return processedTerm;
	}
	
	
	// TODO: method with single thread uses this method 
	// not using the multi-threading here, instead, use the multi-threading outside to invoke this method
	// this is for the convenience of collecting different types of running result
	public ArrayList<Long> scan(String[] targetTerms, Class operationOnPostingList){ // input parameter better be not dynamic
		ArrayList<Long> affectedUnits = new ArrayList<Long> (); // collect Ids of units which are affected
		index_io_operations.get_instance().load_posting(targetTerms); // load the corresponding posting list into memory
		
		for(String term : targetTerms) {
			scan_posting_list(term, operationOnPostingList, affectedUnits);
		}
		return affectedUnits;
	}
	
	
	// set parameter to the plugin class
	public static Class set_param(Class operationClass, Object operationClassParameter) {
		Class opCls = null;
		try {
			Method setParamMethod = operationClass.getMethod("set_parameters", operationClassParameter.getClass()); // get the set_parameter from the operation class
			setParamMethod.invoke(operationClass, operationClassParameter); // use this method to set parameter to the class
			opCls = operationClass;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return opCls;
	}
	
	
	
	// TODO: method with multi-thread uses this class
	// general purpose thread class
	// each thread scanning the posting list of one term
	public static class scan_term_thread extends Thread {
		private Class opCls;
		private Object opClsParam;
		private String[] tTerms;
		private ArrayList<Long> affectedUnitIds = new ArrayList<Long>();
		private scanner snr;
		
		public scan_term_thread(scanner scannerIns, Class operationClass, Object operationClassParameter, String[] targetTerms) {
			opCls = operationClass;
			opClsParam = operationClassParameter; // in order to collect all the 
			tTerms = targetTerms;
			snr = scannerIns;
		}
		
		public void run() {
			try {
				Method setParamMethod = opCls.getMethod("set_parameters", opClsParam.getClass()); // get the set_parameter from the operation class
				setParamMethod.invoke(opCls, opClsParam); // use this method to set parameter to the class
				affectedUnitIds = snr.scan(tTerms, opCls); // pass the class to scanner
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		// invoke after the threads ends to collect the affected posting units' ids
		public ArrayList<Long> get_affectedUnitIds(){
			return affectedUnitIds;
		}
	}
	
	
	// used in deactivator
	public static class scan_term_thread_with_lock extends Thread {
		private Class opCls;
		private Object opClsParam;
		private String[] tTerms;
		private ArrayList<Long> affectedUnitIds = new ArrayList<Long>();
		private ArrayList<String> scannedTerms = new ArrayList<String>();
		private scanner snr;
		
		public scan_term_thread_with_lock(scanner scannerIns, Class operationClass, Object operationClassParameter, String[] targetTerms) {
			opCls = operationClass;
			opClsParam = operationClassParameter; // in order to collect all the 
			tTerms = targetTerms;
			snr = scannerIns;
		}
		
		public void run() {
			try {
				
				for(String term : tTerms) {
					String threadName = "" + name_generator.thread_name_gen();
					if(kpr.require_lock(lexicon_locker.class, term, threadName) == 1) {
						scannedTerms.add(term);		// even if the term is not not totally processed and scanning terminated, it will be regarded as being processed, aggressive
						try {
							opCls = set_param(opCls, opClsParam);
							snr.scan_posting_list(term, opCls, affectedUnitIds);
						}catch(Exception e) {
							e.printStackTrace();
						}finally {
							kpr.release_lock(lexicon_locker.class, term, threadName);
						}
					}
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		// invoke after the threads ends to collect the affected posting units' ids
		public ArrayList<Long> get_affectedUnitIds(){
			return affectedUnitIds;
		}
		
		public ArrayList<String> get_scannedTerms(){
			return scannedTerms;
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// TODO: move to?
	// scan through the term units chain to collect all the related units
	public void visit_next_term_unit (posting_unit curTermUnit, HashMap<String, posting_unit> docRelatedUnits) {
		
		if(curTermUnit != null && curTermUnit.previousTermId != -1) {    // skip the first term unit
			docRelatedUnits.put("" + curTermUnit.currentId, curTermUnit);
			visit_next_term_unit(curTermUnit.nextTermUnit, docRelatedUnits);
		}
	}
	
	
	public void load_doc_related_postings(long docId) {
		doc docIns = idx.docIdMap.get(docId);
		String docPath = general_config.processedDocPath + docIns.docName;
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
		long[] loaded_units = idxIOOps.load_posting(targetTerms);
		for(long unitId : loaded_units) {
			infoManager.set_info(posting_loaded_status.class, idx.postUnitMap.get(unitId));    // update the re-visit time, as load_posting only update the loading time
		}
	}
	
	
	public double cal_doc_length(long docId, Method cal_score){
		HashMap<String, posting_unit> docRelatedUnits = new HashMap<String, posting_unit> (); // for collect the related untis, for convenience of using task splitter
		
		load_doc_related_postings(docId);    // load the posting lists contain the document related terms
		doc docIns = idx.docIdMap.get(docId);
		visit_next_term_unit(docIns.firstTermUnit, docRelatedUnits);    // a little bit wasteful
		
		String[] docRelatedUnitIdStrings = docRelatedUnits.keySet().toArray(new String[0]);
		ArrayList<String[]> workLoads = task_spliter.get_workLoads_terms(general_config.cpuNum, docRelatedUnitIdStrings);
		
		double docLength = 0.0;
		
		
		// use thread to calculate sub doc score 
		class opThread extends Thread{
			String[] drUnitIdStrings;
			ArrayList<Double> subScoreList = new ArrayList<Double> ();
			
			public opThread (String[] workLoad) {
				drUnitIdStrings = workLoad;
			}
			public void run() {
				try {
					for(String drUnitId: drUnitIdStrings) {
						posting_unit drUnitIns = docRelatedUnits.get(drUnitId);    // TODO: here use the cal_score to calculate sub document score on posting 
						double docSubScore = (double)cal_score.invoke(drUnitIns);
						subScoreList.add(docSubScore);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			public ArrayList<Double> get_subScoreList() {
				return subScoreList;
			}
		}
		
		
		// start the threads and collect the sub scores 
		ArrayList<opThread> threadList = new ArrayList<opThread>(); 
				
		for(String[] workload : workLoads) {
			opThread st = new opThread(workload);
			st.run();
			threadList.add(st);
		}
		
		for(opThread st : threadList) {
			try {
				st.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(double subscore : st.get_subScoreList()) {
				docLength += subscore * subscore;
			}
		}
		
		docLength = Math.sqrt(docLength);
		return docLength;
	}
	
	
}
