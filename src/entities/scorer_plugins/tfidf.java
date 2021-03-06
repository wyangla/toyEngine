package entities.scorer_plugins;
import inverted_index.*;
import java.util.*;

import data_structures.*;
import entities.information_manager;
import entities.information_manager_plugins.*;


public class tfidf {
	private static index idx = index.get_instance();
	private static information_manager infoManager = information_manager.get_instance();
	
	public static double cal_score(posting_unit postUnit) {
		double score = 0.;
		String term = postUnit.term;
		Object tfTemp = postUnit.uProp.get("tf");
		
		Double idf = infoManager.get_info(term_idf.class, term);
		
		if(tfTemp != null && idf != null) {
			double tf = (double) tfTemp;
			score = tf * idf; 
		}
		
		return score; // for testing the scorer
	}
}
