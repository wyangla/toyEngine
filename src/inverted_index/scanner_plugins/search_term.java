package inverted_index.scanner_plugins;

import inverted_index.posting_unit;
import java.util.*;
import utils.*;


public class search_term {
	public static counter docScoreCounter; // passed from the outside method which makes use of the scanner
	
	public static void set_parameters (counter documentScoreCounter) {
		docScoreCounter = documentScoreCounter;
	}
	
	public static long conduct(posting_unit pUnit) { // conduct the operation on each post unit
		long relatedUnitId = -1L; 
		docScoreCounter.increase(pUnit.docId, 1.0); // TODO: make use of the scorer here
		return relatedUnitId; // affected post unit Ids
	}
}
