package entities.scanner_plugins;
import data_structures.posting_unit;
import inverted_index.*;



public class delete_posting {
	private static index idx = index.get_instance();
	
	public static void set_parameters (String placeHolder) { // each task is a copy of such class
	}
	
	public static long conduct(posting_unit pUnit) { // the input parameters can only be like this
		long affectedUnitId = pUnit.currentId;
		idx.postUnitMap.remove(affectedUnitId);
		return affectedUnitId; // affected post unit Ids
	}
}
