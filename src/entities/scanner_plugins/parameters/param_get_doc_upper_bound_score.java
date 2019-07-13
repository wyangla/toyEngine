package entities.scanner_plugins.parameters;

import data_structures.posting_unit;
import utils.counter;


// when using the instance as parameter of scanner_plugin, 
// the operations usually directly write in the instance
public class param_get_doc_upper_bound_score {
	counter tMaxScores;
	counter docUpperBounds;
	
	public param_get_doc_upper_bound_score(counter termMaxScores, counter documentUpperBoundScores){
		tMaxScores = termMaxScores;
		docUpperBounds = documentUpperBoundScores;
	}
	
	public long cal_upper_bound_from_one_term(posting_unit pUnit) {
		long processedUnitId = -1L;
		Double score = tMaxScores.get(pUnit.term);
		if(score == null) {
			score = 0.0;
		}
		docUpperBounds.put("" + pUnit.docId, score);
		processedUnitId = pUnit.currentId;
	return processedUnitId;
	}
}
