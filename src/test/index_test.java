package test;
import inverted_index.*;
import java.util.*;


public class index_test {
	// test basic functionalities
	
	index idx = index.get_instance();
	keeper kpr = keeper.get_instance();
	
	// add_term
	// ini_posting_list
	public void test_1() {
		String[] termList = {"a", "b", "c"};
		for(String t : termList) {
			idx.add_term(t);
		}
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconLockMap: " + kpr.lexiconLockMap.entrySet());
		System.out.println("");
	}
	
	// del_term
	public void test_2() {
		idx.del_term("b");
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconLockMap: " + kpr.lexiconLockMap.entrySet());
		System.out.println("");
	}
	
	// add_posting_unit
	public void test_3() {
		posting_unit postUnit = new posting_unit();
		idx.add_posting_unit("a", postUnit.flatten());
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconLockMap: " + kpr.lexiconLockMap.entrySet());
		System.out.println("");
		
	}
	
	// del_posting_unit
	public void test_4() {
		idx.del_posting_unit(3);
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconLockMap: " + kpr.lexiconLockMap.entrySet());
		
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
	
	
	// test the retry mechanism of add_posting_unit
	public void test_5() {
		HashMap<String, Long> metaMap_1 = new HashMap<String, Long> ();
		metaMap_1.put("termLock", 0L); // the lock can be required
		metaMap_1.put("threadNum", -1L);
		kpr.lexiconLockMap.put("a", metaMap_1);
		
		HashMap<String, Long> metaMap_2 = new HashMap<String, Long> ();
		metaMap_2.put("termLock", System.currentTimeMillis() - 5000); // without -5000 the lock cannot be required, otherwise successful
		metaMap_2.put("threadNum", 2L);
		kpr.lexiconLockMap.put("c", metaMap_2);
		
		posting_unit postUnit = new posting_unit();
		postUnit.uProp.put("tfidf", 3.33);
		idx.add_posting_unit("a", postUnit.flatten()); // successful
		
		posting_unit postUnit_2 = new posting_unit();
		postUnit_2.uProp.put("tfidf", 3.35);
		idx.add_posting_unit("c", postUnit_2.flatten()); // without - 5000 failed
		
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconLockMap: " + kpr.lexiconLockMap.entrySet());
		System.out.println("");
	}
	
	
	// test persist_index
	public void test_6() {
		idx.persist_index();
	}
	
	// clear index
	public void test_7() {
		idx.clear_index();
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconLockMap: " + kpr.lexiconLockMap.entrySet());
		System.out.println("");
	}
	
	
	// test load index
	public void test_8() {
		long t1 = System.currentTimeMillis();
		idx.load_index(new String[] {"a", "c"});
		
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconLockMap: " + kpr.lexiconLockMap.entrySet());
		long t2 = System.currentTimeMillis();
		System.out.println("" + (t2 - t1));
		
		for (Long pUnitId : idx.postUnitMap.keySet()) {
			posting_unit pUnit = idx.postUnitMap.get(pUnitId);
			System.out.println("cId: " + pUnit.currentId + " nId: " + pUnit.nextId + " pId: " + pUnit.previousId + " uProp: " + pUnit.uProp.toString() + " docId: " + pUnit.docId + " status: " + pUnit.status);
			
		}
		System.out.println("");
	}
	
	
	// test reload the index
	public void test_9() {
		long t1 = System.currentTimeMillis();
		
		idx.reload_index();
		idx.display_content();
		
		long t2 = System.currentTimeMillis();
		System.out.println("" + (t2 - t1));
		
		for (Long pUnitId : idx.postUnitMap.keySet()) {
			posting_unit pUnit = idx.postUnitMap.get(pUnitId);
			System.out.println("cId: " + pUnit.currentId + " nId: " + pUnit.nextId + " pId: " + pUnit.previousId + " uProp: " + pUnit.uProp.toString() + " docId: " + pUnit.docId + " status: " + pUnit.status);
			
		}
		System.out.println("");
	}
	
	
	
	public static void main(String[] args) {
		index_test idx_test = new index_test();
		idx_test.test_1();
		idx_test.test_2();
		idx_test.test_3();
		idx_test.test_4();
		idx_test.test_5();
		idx_test.test_6();
		idx_test.test_7();
		idx_test.test_8();
		idx_test.test_9();
		
	}
}
