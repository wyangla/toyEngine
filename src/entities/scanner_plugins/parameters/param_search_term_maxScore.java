package entities.scanner_plugins.parameters;

import data_structures.posting_unit;
import entities.scorer;
import utils.counter;

public class param_search_term_maxScore {
	// use this class to pass multiple data structures into the scanner
	counter docUB;
	counter docSC;	// changing
	int tpK;

	public param_search_term_maxScore(scorer scr, counter docUpperBounds, counter documentScoreCounter, int topK){
		docUB = docUpperBounds;
		docSC = documentScoreCounter;
		tpK = topK;
	}
	
	// use the current docId replace the one with min score
	public long try_to_replace_min_score_doc(posting_unit pUnit){
		long newlyAddedUnitId = -1L; 
		scorer scr = scorer.getInstance();
		
		// in order to be efficient enough, here does not have the synchronisation,
		// thus could be more than K scores
		// if total number of docs is smaller than tpK, they will be all returned
		if(docSC.size() < tpK) {
			docSC.increase(pUnit.docId, scr.cal_score(pUnit));
		}else {
			// not using clipping here, as could loose sub score contributions from terms 
			Double docUpperBound = docUB.get(pUnit.docId);
			String topKthDocId = docSC.get_topKth_key(tpK);
			Double topKthScore = docSC.get(topKthDocId);
			
			// != null prevent new docs added during the ranking after the upper bound calculation
			// only when the upper bound bigger than the current min score, calculate the actual value
			// not using remove, as could loose sub score
			if(docUpperBound != null && docUpperBound > topKthScore) {
				Double subScore = scr.cal_score(pUnit);		// score contribute by one term to the doc
				docSC.increase(pUnit.docId, subScore);	// could increase more than K
				newlyAddedUnitId = pUnit.currentId;
			}
		}
		
	return newlyAddedUnitId;
	}
	
}
