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
	
	
	public ArrayList<Long> scan(String[] targetTerms, Class operationOnPostingList){ // input parameter better be not dynamic
		ArrayList<Long> affectedUnits = new ArrayList<Long> (); // collect Ids of units which are affected
		idx.load_index(targetTerms); // load the corresponding posting list into memory
		
		for(String term : targetTerms) {
			scan_posting_list(term, operationOnPostingList, affectedUnits);
		}
		return affectedUnits;
	}
}
