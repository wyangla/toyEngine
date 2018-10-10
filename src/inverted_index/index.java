package inverted_index;
import java.util.*;



// this should be a singleton instance, contains a posting unit counter
public class index {
	
	// singleton
	// ref: http://www.runoob.com/design-pattern/singleton-pattern.html
	private static index index_ins = new index();
	private index() {}
	public static index get_instance() {
		return index_ins;
	}
	
	public HashMap<Long, posting_unit> postUnitMap = new HashMap<Long, posting_unit>(); // {postingUnitId : postingUnitIns}, store all the posting units, for convenience of persistance
	public HashMap<String, ArrayList<Long>> lexicon = new HashMap<String, ArrayList<Long>>(); // {term : [postingUnitIds]}, the inside HashMap is for the convenience of adding more meta data
	private keeper kpr = keeper.get_instance(); // get the keeper instance, so as to get the lexiconLockMap
	
	// for generating the unique posting unit id s
	private class counters {
		long postingId = 0L;
	}
	counters pc = new counters();  
	
	
	
	
	
	// initialise the posting list for one term
	private long ini_posting_list(String term) {
		posting_unit postUnit = new posting_unit();
		
		// TODO: need a global id generator?
		postUnit.currentId = pc.postingId;
		postUnitMap.put(postUnit.currentId, postUnit);
		pc.postingId ++;

		// initialize the posting list for one term
		ArrayList<Long> postingUnitIds = new ArrayList<Long>();
		postingUnitIds.add(postUnit.currentId);
		lexicon.put(term, postingUnitIds);  
		
		// initialize the lock for each term in lexicon
		kpr.add_term(term);
		
		return postUnit.currentId;
	}
	
	// add a new term to the inverted index, include add a new term to the lexicon and add add new 
	public long add_term(String term) {
		long firstUnitId = ini_posting_list(term);
		return firstUnitId;
	}
	
	// delete a posting list
	public void del_term(String term) {
		ArrayList<Long> postUnitList = lexicon.get(term);
		lexicon.remove(term); // delete from lexicon
		
		kpr.del_term(term);
		
		for(long postUnitId : postUnitList) {
			postUnitMap.remove(postUnitId); // delete specific posting unites
		}
		
	}
	
	// the analysing of doc and find the term:postUnit pair is handled by a higher level
	public long add_posting_unit(String term, posting_unit postUnit) {
		postUnit.currentId = pc.postingId;
		postUnitMap.put(postUnit.currentId, postUnit); // add to the overall posting units table
		pc.postingId ++;
		
		ArrayList<Long> postingUnitIds = lexicon.get(term); // get the posting list
		long previousUnitId = postingUnitIds.get(postingUnitIds.size() - 1);
		posting_unit prevUnit = postUnitMap.get(previousUnitId); // get the instance of previous unit
		postingUnitIds.add(postUnit.currentId); // add to the lexicon, in fact is adding to the posting list
		
		// link units, for the convenience of scanning
		postUnit.link_to_previous(prevUnit);
		prevUnit.link_to_next(postUnit);
		
		return postUnit.currentId;
	}
	
	// delete the some posting units
	// just set flags instead of directly remove, ref: SSTable
	// need an independent process scanning and cleaning the postUnitMap & lexicon
	// the starter unit should never be deleted
	public long del_posting_unit(long postingUnitId) {
		posting_unit delUnit = postUnitMap.get(postingUnitId);
		delUnit.status = 0;
		return delUnit.currentId;
	}
	

	
	public static void main(String[] args) {
		index idx = new index();		
	}
}
