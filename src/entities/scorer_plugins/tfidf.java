package entities.scorer_plugins;
import inverted_index.*;
import java.util.*;

import data_structures.posting_unit;


public class tfidf {
	private static index idx = index.get_instance();
	
	public static double cal_score(posting_unit postUnit) {
		double score = 0.;
		String term = postUnit.term;
		Object tfTemp = postUnit.uProp.get("tf");
		ArrayList<Long> postUnitIds = idx.lexicon.get(term);
		
		if(tfTemp != null && postUnitIds != null) {
			double tf = (double) tfTemp;
			double df = (double) postUnitIds.size();
			score = tf * Math.log(1. / df); 
		}
		
		return score;
	}
}
