package entities.scanner_plugins;

import java.util.*;

import data_structures.posting_unit;
import utils.*;
import entities.*;


public class search_term {
	public static counter docScoreCounter; // passed from the outside method which makes use of the scanner
	
	public static void set_parameters (counter documentScoreCounter) {
		docScoreCounter = documentScoreCounter;
	}
	
	public static long conduct(posting_unit pUnit) { // conduct the operation on each post unit
		long relatedUnitId = -1L; 
		double score = scorer.getInstance().cal_score(pUnit);
		docScoreCounter.increase("" + pUnit.docId, score);    // long -> string, docScoreCounter is the final step, not related to the inner consistency
		return relatedUnitId; // affected post unit Ids
	}
}
