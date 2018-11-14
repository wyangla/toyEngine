package entities.scanner_plugins;

import data_structures.*;
import entities.scanner_plugins.parameters.*;

public class search_term_maxScore {
	public static param_search_term_maxScore param; // need to be shared by all threads, not merged at the end
	
	public static void set_parameters (param_search_term_maxScore paramSearchTermMaxScore) {
		param = paramSearchTermMaxScore;
	}
	
	// operations are done by methods provided by param
	public static long conduct(posting_unit pUnit) {
		long relatedUnitId = -1L; 
		relatedUnitId = param.try_to_score_add_doc(pUnit);
		return relatedUnitId;
	}
}
