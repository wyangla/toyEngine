package entities.scanner_plugins;
import data_structures.posting_unit;
import inverted_index.*;
import data_structures.*;



public class delete_doc implements scanner_plugin_interface{
	public String docName;
	public index idx = index.get_instance();
	
	public void set_parameters (Object targetDocName) { // each task is a copy of such class
		docName = (String)targetDocName;
	}
	
	public long conduct(posting_unit pUnit) { // the input parameters can only be like this
		long affectedUnitId = -1L; 
		doc docIns = idx.docMap.get(docName);
		
		if(pUnit.docId == docIns.docId) {
			pUnit.status = 0; // lazily delete
			affectedUnitId = pUnit.currentId;
		} 
		return affectedUnitId; // affected post unit Ids
	}
}
