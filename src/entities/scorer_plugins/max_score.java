package entities.scorer_plugins;
import inverted_index.*;
import java.util.*;

import data_structures.*;
import entities.information_manager;
import entities.information_manager_plugins.*;


public class max_score {
	private static information_manager infoManager = information_manager.get_instance();
	
	public static double cal_score(posting_unit pUnit) {
		Double score = infoManager.get_info(term_max_tf.class, pUnit.term);
		if(score == null) {
			score = 0.0;
		}
		return score;
	}
}
