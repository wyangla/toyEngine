package data_structures;
import java.util.*;


// abstraction of terms in lexicon
public class term {
	public static String termString = "";
	public static int status = -1;		// the posting list is loaded or not
	public static HashMap<String, Double> prop = new HashMap<String, Double> (); // additional information like max score
	public static ArrayList<Long> postUnitIds = new ArrayList<Long>();		// the posting list of one term

	
	
	public int get_posting_length() {
		return postUnitIds.size();	
	}
	
	
	public int add_unit(long postUnitId) {
		int addedFlag = -1;	// -1 denotes failed
		if(postUnitIds.add(postUnitId)) {
			addedFlag = 1;
		}
		return addedFlag;
	}
	
	public int remove_unit(long postUnitId) {
		int removedFlag = -1; // -1 denotes failed
		if(postUnitIds.remove(postUnitIds)) {
			removedFlag = 1;
		}
		return removedFlag;
	}
	
}
