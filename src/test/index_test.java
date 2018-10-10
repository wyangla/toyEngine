package test;
import inverted_index.*;
import java.util.*;


public class index_test {
	// test basic functionalities
	
	index idx = index.get_instance();
	
	// add_term
	// ini_posting_list
	public void test_1() {
		String[] termList = {"a", "b", "c"};
		for(String t : termList) {
			idx.add_term(t);
		}
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconKeeper: " + idx.lexiconKeeper.entrySet());
		System.out.println("");
	}
	
	// del_term
	public void test_2() {
		idx.del_term("b");
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconKeeper: " + idx.lexiconKeeper.entrySet());
		System.out.println("");
	}
	
	// add_posting_unit
	public void test_3() {
		posting_unit postUnit = new posting_unit();
		idx.add_posting_unit("a", postUnit);
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconKeeper: " + idx.lexiconKeeper.entrySet());
		System.out.println("");
		
	}
	
	// del_posting_unit
	public void test_4() {
		idx.del_posting_unit(3);
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
//		List<String> lexiconKeySet_temp = new ArrayList<String>();
//		lexiconKeySet_temp.addAll(idx.lexicon.keySet());
//		for (String k : lexiconKeySet_temp) {
//			System.out.println(k);
//		}
		
	}
	
	public static void main(String[] args) {
		index_test idx_test = new index_test();
		idx_test.test_1();
		idx_test.test_2();
		idx_test.test_3();
		idx_test.test_4();
		
	}
}
