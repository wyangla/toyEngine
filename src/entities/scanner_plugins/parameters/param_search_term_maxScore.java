package entities.scanner_plugins.parameters;

import data_structures.posting_unit;
import entities.scorer;
import utils.counter;

public class param_search_term_maxScore {
	// use this class to pass multiple data structures into the scanner
	scorer scr;
	counter docUB;
	counter docSC;	// changing
	int tpK;

	public param_search_term_maxScore(scorer scorer, counter docUpperBounds, counter documentScoreCounter, int topK){
		scr = scorer;
		docUB = docUpperBounds;
		docSC = documentScoreCounter;
		tpK = topK;
	}
	
	// use the current docId replace the one with min score
	public long try_to_score_add_doc(posting_unit pUnit){
		long newlyAddedUnitId = -1L; 
		Double topKthScore = null;

		// not using clipping here, as could loose sub score contributions from terms 
		Double docUpperBound = docUB.get(pUnit.docId);
		
		// atomic
		// when there are not enough scores in the docSC for getting the threshold
		synchronized(param_search_term_maxScore.class) {
			try {
				topKthScore = docSC.get(docSC.get_topKth_key(tpK));
			}catch(Exception e) {
				topKthScore = 0.0;
			}
		}

		// != null prevent new docs added during the ranking after the upper bound calculation
		// only when the upper bound bigger than the current min score, calculate the actual value
		// not using remove, as could loose sub score
		if(docUpperBound != null && docUpperBound > topKthScore) {
			Double subScore = scr.cal_score(pUnit);		// score contribute by one term to the doc
			docSC.increase("" + pUnit.docId, subScore);	// could increase more than K
			newlyAddedUnitId = pUnit.currentId;
		}
		
	return newlyAddedUnitId;
	}
	
}
