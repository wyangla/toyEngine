package entities.scanner_plugins;

import data_structures.posting_unit;
import java.util.*;

public class get_docId_set implements scanner_plugin_interface{
	public HashSet<Long> docIdSet;    // String -> Long, for inner consistency
	
	public void set_parameters (Object documentIdSet) {
		docIdSet = (HashSet<Long>)documentIdSet;
	}
	
	public long conduct(posting_unit pUnit) {
		long relatedUnitId = -1L; 
		docIdSet.add(pUnit.docId);
		return relatedUnitId;
	}
}
