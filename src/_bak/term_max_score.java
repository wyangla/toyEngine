package _bak;

import java.util.HashMap;

import inverted_index.index;


// the max score of each term
// filled long with the generation of posting list
public class term_max_score {
	private static index idx = index.get_instance();
	private static HashMap<String, Double> lexiconMaxScore = new HashMap<String, Double> ();
			
	public static double get_info(String term) {
		return 0.0;
	}
	
	public static int set_info(String term) {
		int setFlag = -1;	
		return setFlag;
	}
		
}



