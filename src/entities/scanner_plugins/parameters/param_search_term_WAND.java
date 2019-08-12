package entities.scanner_plugins.parameters;

import data_structures.posting_unit;
import entities.scorer;
import utils.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// max length == top_min{topK, len(posting of maxScoreTerm)}
// not using the pre-filling so as to prevent
	// some non-expected score occur due to the pre-filling of topK docs into docSC, 
	// its the fake random scanning of the posting lists

public class param_search_term_WAND {
	// use this class to pass multiple data structures into the scanner
	scorer scr;										// scorer
	Iterator<Map.Entry<String, Double>> tMaxSI;		// term - upper bound entry iterator
	HashSet<Long> vDocSet;						// the intersection set
	counter curUB;									// current upper bound of the vDocSet - i.e. the sum the scores of necessary terms
	ConcurrentHashMap<String, HashSet<Long>> tDocSetMap;	// term - docId set map
	counter docSC;									// doc score counter, the rank counter
	int tpK;										// top K

	public param_search_term_WAND(scorer scorer, 
									Iterator<Map.Entry<String, Double>> termMaxScoresIterator, 
									HashSet<Long> validDocSet, 
									counter currentUpperBound,
									ConcurrentHashMap<String, HashSet<Long>> termDocIdSetMap, 
									counter documentScoreCounter, 
									int topK){
		scr = scorer;
		tMaxSI = termMaxScoresIterator;
		vDocSet = validDocSet;
		curUB = currentUpperBound;
		tDocSetMap = termDocIdSetMap;
		docSC = documentScoreCounter;
		tpK = topK;
	}
	
	// use the current docId replace the one with min score
	public long try_to_score_add_doc(posting_unit pUnit){
		long newlyAddedUnitId = -1L; 

		boolean reachMinSize = false;	
		Double topKthScore = null;
		boolean doCalculation = false;
		
		// not doing pre-filling here, as want to at most only contains the max score terms' related docs.
		
		// not using clipping here
		// atomic
		synchronized(param_search_term_WAND.class) {
			// TODO: testing
			// System.out.println(docSC);
			try {
				topKthScore = docSC.get(docSC.get_topKth_key(tpK));
			}catch(Exception e) {		// when there are not enough scores in the docSC for getting the threshold
				topKthScore = 0.0;
			}
				
		}
		
		// get the necessary terms for combining together to over the threshold
		while(true) {
			
			// only one thread can check the upper bound and operate the intersection set vDocSet
			synchronized(param_search_term_WAND.class) {
				if(curUB.get("currentUpperBound") > topKthScore) { // when does not need to consider more necessary terms, break out
					doCalculation = vDocSet.contains(pUnit.docId);
					break;
				}else {
					try {
						// TODO: the ideal way is get all the possible combinations of terms which could exceed the topKthScore, 
						// within combination do set intersection, among combinations do set union
						Map.Entry<String, Double> necessaryTermScore = tMaxSI.next();
						curUB.increase("currentUpperBound", necessaryTermScore.getValue());
						
						HashSet<Long> termDocSet = tDocSetMap.get(necessaryTermScore.getKey());
						if(termDocSet != null) {	// prevent new terms are added during the searching, e.g. sdadsasas which initially not existing and currently added
							if(vDocSet.isEmpty()) {
								vDocSet.addAll(termDocSet);
							}else {
								vDocSet.retainAll(termDocSet);
							}
						}
					}catch(Exception e) {
						doCalculation = vDocSet.contains(pUnit.docId);
						break;		// when there are no more terms could be used
					}
				}
			}
		}			
		
		// put vDocSet.contains(pUnit.docId) here, means could be affected by other thread's operations of the intersetion
		// should put in the synchronization body, thus this status is fixed
		// here could be a little bit waste of the computations, 
		// as topKthScore could be already raised again, and set already be smaller
		if(doCalculation) {
			Double subScore = scr.cal_score(pUnit);		// score contribute by one term to the doc
			docSC.increase("" + pUnit.docId, subScore);		// could increase more than K
			newlyAddedUnitId = pUnit.currentId;
		}

		
	return newlyAddedUnitId;
	}
	
}
