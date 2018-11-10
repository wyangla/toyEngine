package entities.scanner_plugins.parameters;

import data_structures.posting_unit;
import entities.scorer;
import utils.counter;

public class param_search_term_maxScore {
	// use this class to pass multiple data structures into the scanner
	counter docUB;
	counter tpKDocSC;	// changing
	int tpK;

	public param_search_term_maxScore(scorer scr, counter docUpperBounds, counter topKDocumentScoreCounter, int topK){
		docUB = docUpperBounds;
		tpKDocSC = topKDocumentScoreCounter;
		tpK = topK;
	}
	
	// use the current docId replace the one with min score
	public long try_to_replace_min_score_doc(posting_unit pUnit){
		long newlyAddedUnitId = -1L; 
		scorer scr = scorer.getInstance();
		
		// in order to be efficient enough, here does not have the synchronisation,
		// thus could be more than K scores
		if(tpKDocSC.size() < tpK) {
			tpKDocSC.increase(pUnit.docId, scr.cal_score(pUnit));
		}else {
			
			// not always do clipping, only when it is too long
			// faster than ensure topK
			// also prevent sorting the longer and longer arrayList when not care about the over adding
			if(tpKDocSC.size() > tpK) {	
				synchronized(param_search_term_maxScore.class) {
					tpKDocSC.remove_after_topK(tpK); // remove the docs lower than topK
				}
			}
			
			Double docUpperBound = docUB.get(pUnit.docId);
			String minScoreDocId = tpKDocSC.get_min_key();
			Double minScore = tpKDocSC.get(minScoreDocId);
			
			// only when the upper bound bigger than the current min score, calculate the actual value
			// != null prevent new docs added during the ranking after the upper bound calculation
			if(docUpperBound != null && docUpperBound > minScore) {
				Double curScore = scr.cal_score(pUnit);
				if(curScore > minScore) {
					tpKDocSC.remove(minScoreDocId);		// here could remove the same id for multiple times
					tpKDocSC.increase(pUnit.docId, curScore);	// could increase more than K
					newlyAddedUnitId = pUnit.currentId;
				}
			}
		}
		
	return newlyAddedUnitId;
	}
	
}
