package test;
import java.util.*;
import inverted_index.*;



public class cleaner_test {
	index idx = index.get_instance();
	cleaner clr = new cleaner();
	
	// prepare the inverted-index
	public void fill_index() {
		String[] termList = {"a", "b", "c"};
		for(String t : termList) {
			idx.add_term(t);
		}
		
		for (String term : termList) {
			for(int i = 0; i < 2; i ++) {
				posting_unit postUnit = new posting_unit();
				idx.add_posting_unit(term, postUnit);
			}
		}
		
		idx.del_posting_unit(0); // end of posting list 
		idx.del_posting_unit(6); // middle of posting list
		idx.del_posting_unit(7); // start of posting list
		
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconKeeper: " + idx.lexiconKeeper.entrySet());
		
		// print out all the status
		HashMap<Long, Integer> postUnitStatusMap = new HashMap<Long, Integer>();
		for (Long pUnitId : idx.postUnitMap.keySet()) {
			posting_unit pUnit = idx.postUnitMap.get(pUnitId);
			postUnitStatusMap.put(pUnitId, pUnit.status);
		}
		System.out.println("" + postUnitStatusMap.entrySet());
		System.out.println("");

	}
	
	// clean_unit
	public void test_5() {
		ArrayList<Long> delPostUnitList = clr.clean_unit(new String[]{"a", "b", "c"});
		
		System.out.println("delPostUnitList: " + delPostUnitList);
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconKeeper: " + idx.lexiconKeeper.entrySet());
		
		System.out.println("postUnitMap.get(0L): " + idx.postUnitMap.get(0L).previousId + " " + idx.postUnitMap.get(0L).nextId);
		System.out.println("postUnitMap.get(2L): " + idx.postUnitMap.get(2L).previousId + " " + idx.postUnitMap.get(2L).nextId);
		System.out.println("");
	}
	
	// multiprocess
	public void test_6() {
		clr.clean();
		
		try {
			Thread.sleep(2);
		} catch(Exception e) {}
		
		System.out.println("\npostUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconKeeper: " + idx.lexiconKeeper.entrySet());

	}
	
	public static void main(String[] args) {
		
		// prepare the inverted index
		cleaner_test clr_test = new cleaner_test();
		clr_test.fill_index();
		
//		clr_test.test_5();
		clr_test.test_6();
	}
}
