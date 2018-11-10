package entities.scanner_plugins.parameters;

import data_structures.posting_unit;
import entities.scorer;
import utils.*;
import java.util.*;



public class param_search_term_WAND {
	// use this class to pass multiple data structures into the scanner
	counter tMaxSC;
	HashMap<String, HashSet<String>> tDocSetMap;
	counter docSC;	// changing
	int tpK;

	public param_search_term_WAND(scorer scr, counter termMaxScore, HashMap<String, HashSet<String>> termDocIdSetMap, counter documentScoreCounter, int topK){
		tMaxSC = termMaxScore;
		tDocSetMap = termDocIdSetMap;
		docSC = documentScoreCounter;
		tpK = topK;
	}
	
	// use the current docId replace the one with min score
	public long try_to_score_add_doc(posting_unit pUnit){
		long newlyAddedUnitId = -1L; 
		scorer scr = scorer.getInstance();
		
		// docSC could be more than K scores
		if(docSC.size() < tpK) {
			docSC.increase(pUnit.docId, scr.cal_score(pUnit));
		}else {
			// not using clipping here
			String topKthDocId = docSC.get_topKth_key(tpK);
			Double topKthScore = docSC.get(topKthDocId);
			
			// get the necessary terms for combining together to over the threshold
			ArrayList<String> necessaryTerms = new ArrayList<String>();
			for(int i = 1; i < tMaxSC.size(); i ++) {
				necessaryTerms = tMaxSC.get_topK_keys(i);
				
				double upperScore = 0.0; 
				for(String term : necessaryTerms) {
					upperScore = upperScore + tMaxSC.get(term);
				}
				
				if(upperScore > topKthScore) {
					break;
				}
			}

			// get the potential set of docs that having the necessary terms, if docs are not inside, skip the scoring
			HashSet<String> validDocs = new HashSet<String>(); 
			for(int i = 0; i < necessaryTerms.size(); i ++) {
				String term = necessaryTerms.get(i);
				HashSet<String> termDocSet = tDocSetMap.get(term);
				
				if(termDocSet != null) {	// prevent new terms are added during the searching, e.g. sdadsasas which initially not existing and currently added
					if(i == 0) {
						validDocs.addAll(termDocSet);	// add the first set, for doing the following intersection, without modifying the termDocSets
					}else {
						validDocs.retainAll(termDocSet);
					}
				}
			}
			
			
			if(validDocs.contains(pUnit.docId)) {
				Double subScore = scr.cal_score(pUnit);		// score contribute by one term to the doc
				docSC.increase(pUnit.docId, subScore);	// could increase more than K
				newlyAddedUnitId = pUnit.currentId;
			}
		}
		
	return newlyAddedUnitId;
	}
	
}
