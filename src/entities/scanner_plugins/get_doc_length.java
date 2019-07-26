package entities.scanner_plugins;

import java.util.*;

import data_structures.posting_unit;
import utils.*;
import entities.*;



// effectively the same with search_term, only difference is the scanning trace is difference
// the trace is determined by scanner
// each thread only update the length of one document
// and the length of different document will be collected by the input docLengthCounter
public class get_doc_length {
	public static counter docLenCounter;
	
	public static void set_parameters (counter docLengthCounter) {
		docLenCounter = docLengthCounter;
	}
	
	public static long conduct(posting_unit pUnit) {
		long relatedUnitId = -1L; 
		double score = scorer.getInstance().cal_score(pUnit);
		docLenCounter.increase("" + pUnit.docId, score * score);    // each socre corresponding to one unique term in document
		return relatedUnitId;
	}
}
