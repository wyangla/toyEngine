package entities.scanner_plugins;

import data_structures.posting_unit;
import entities.scanner_plugins.parameters.*;

public class search_term_WAND implements scanner_plugin_interface{
	public param_search_term_WAND param; // need to be shared by all threads, not merged at the end
	
	public void set_parameters (Object paramSearchTermWAND) {
		param = (param_search_term_WAND)paramSearchTermWAND;
	}
	
	// operations are done by methods provided by param
	public long conduct(posting_unit pUnit) {
		long relatedUnitId = -1L; 
		relatedUnitId = param.try_to_score_add_doc(pUnit);
		return relatedUnitId;
	}
}
