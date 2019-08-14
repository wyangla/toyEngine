package entities.scanner_plugins;

import java.util.*;
import data_structures.posting_unit;
import utils.*;
import entities.*;
import entities.scanner_plugins.parameters.*;;


// wrapper of using the scanner
public class get_doc_upper_bound_score implements scanner_plugin_interface{
	public param_get_doc_upper_bound_score param; // need to be shared by all threads, not merged at the end
	
	public void set_parameters (Object paramDocUpperBound) {
		param = (param_get_doc_upper_bound_score)paramDocUpperBound;
	}
	
	// operations are done by methods provided by param
	public long conduct(posting_unit pUnit) {
		long relatedUnitId = -1L; 
		relatedUnitId = param.cal_upper_bound_from_one_term(pUnit);
		return relatedUnitId;
	}
}
