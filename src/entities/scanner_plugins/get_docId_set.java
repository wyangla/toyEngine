package entities.scanner_plugins;

import data_structures.posting_unit;
import java.util.*;

public class get_docId_set {
	public static HashSet<String> docIdSet;
	
	public static void set_parameters (HashSet<String> documentIdSet) {
		docIdSet = documentIdSet;
	}
	
	public static long conduct(posting_unit pUnit) {
		long relatedUnitId = -1L; 
		docIdSet.add(pUnit.docId);
		return relatedUnitId;
	}
}
