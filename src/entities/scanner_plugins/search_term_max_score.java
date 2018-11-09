package entities.scanner_plugins;

import java.util.*;
import data_structures.posting_unit;
import utils.*;
import entities.*;


public class search_term_max_score {
	public static counter docScoreCounter; // passed from the outside method which makes use of the scanner
	
	public static void set_parameters (counter documentScoreCounter) {
		docScoreCounter = documentScoreCounter;
	}
	
	// the filed related tfs are merged by scorer_plugin
	public static long conduct(posting_unit pUnit) {
		long relatedUnitId = -1L; 
		double score = scorer.getInstance().cal_score(new String[] {"max_score"}, new double[] {1.0}, pUnit);
		docScoreCounter.increase(pUnit.docId, score);
		return relatedUnitId;
	}
}
