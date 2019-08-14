package entities.scanner_plugins;
import data_structures.posting_unit;
import inverted_index.*;



public class delete_posting implements scanner_plugin_interface {
	private index idx = index.get_instance();
	
	public void set_parameters (Object placeHolder) { // each task is a copy of such class
	}
	
	public long conduct(posting_unit pUnit) { // the input parameters can only be like this
		long affectedUnitId = pUnit.currentId;
		idx.postUnitMap.remove(affectedUnitId);
		return affectedUnitId; // affected post unit Ids
	}
}
