package inverted_index.scanner_plugins;

import inverted_index.posting_unit;
import java.util.*;


public class search_doc {
	public static HashMap<String, Double> docScoreMap; // passed from the outside method which makes use of the scanner
	
	public static void set_parameters (HashMap<String, Double> documentScoreMap) {
		docScoreMap = documentScoreMap;
	}
	
//	public static long conduct(posting_unit pUnit) { // conduct the operation on each post unit
//		long relatedUnitId = -1L; 
//		if(pUnit.docId.matches(docName)) {
//			pUnit.status = 0; // lazily delete
//			affectedUnitId = pUnit.currentId;
//		} 
//		return relatedUnitId; // affected post unit Ids
//	}
}
