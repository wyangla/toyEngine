package entities.scanner_plugins;
import data_structures.posting_unit;
import inverted_index.*;
import data_structures.*;
import java.util.*;



public class collect_term_units {
	public static ArrayList<posting_unit> drUnits;
	public static index idx = index.get_instance();
	
	public static void set_parameters (ArrayList<posting_unit> docRelatedUnits) {
		drUnits = docRelatedUnits;
	}
	
	public static long conduct(posting_unit termUnit) {
		long affectedUnitId = -1L; 
		drUnits.add(termUnit);
		return affectedUnitId;
	}
}
