package inverted_index;
import java.util.*;
import java.lang.reflect.*;
import configs.scanner_config;;


// This class invokes corresponding plugins to conduct the operations on each unit on posting lists

public class scanner {
	index idx = index.get_instance();
	keeper kpr = keeper.get_instance();
	
	
	public void visit_next_unit (posting_unit pUnitCurrent, Class operationOnPostingList, ArrayList<Long> affectedUnits) throws Exception { // use the reference to visit the unit directly instead of searching in the HashMap
		
		if(pUnitCurrent != null) {

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
		posting_unit pUnitStarter = idx.postUnitMap.get(postUnitIds.get(0));
		
		// load the unit operations
		try {
			visit_next_unit(pUnitStarter, operationOnPostingList, affectedUnits);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return processedTerm;
	}
	
	
	// TODO: method with single thread uses this method 
	// not using the multi-threading here, instead, use the multi-threading outside to invoke this method
	// this is for the convenience of collecting different types of running result
	public ArrayList<Long> scan(String[] targetTerms, Class operationOnPostingList){ // input parameter better be not dynamic
		ArrayList<Long> affectedUnits = new ArrayList<Long> (); // collect Ids of units which are affected
		idx.load_posting(targetTerms); // load the corresponding posting list into memory
		
		for(String term : targetTerms) {
			scan_posting_list(term, operationOnPostingList, affectedUnits);
		}
		return affectedUnits;
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
	
	
}
