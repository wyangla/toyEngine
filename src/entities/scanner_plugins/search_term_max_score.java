package entities.scanner_plugins;

import java.util.*;

import data_structures.posting_unit;
import utils.*;
import entities.*;
import entities.information_manager_plugins.*;


public class search_term_max_score {
	public static counter docScoreCounter; // passed from the outside method which makes use of the scanner
	private static information_manager infoManager = information_manager.get_instance();
	
	
	public static void set_parameters (counter documentScoreCounter) {
		docScoreCounter = documentScoreCounter;
	}
	
	// load the max tf of target term here
	// the filed related tfs are merged by scorer_plugin
	public static long conduct(posting_unit pUnit) {
		long relatedUnitId = -1L; 
		docScoreCounter.increase(pUnit.docId, infoManager.get_info(term_max_tf.class, pUnit.term));
		return relatedUnitId;
	}
}
