package test;
import inverted_index.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import data_structures.posting_unit;
import entities.keeper;
import entities.keeper_plugins.lexicon_locker;
import probes.*;



public class index_test {
	// test basic functionalities
	
	index idx = index.get_instance();
	keeper kpr = keeper.get_instance();
	index_probe idxProb = new index_probe();
	
	// add_term
	// ini_posting_list
	public void test_1() {
		String[] termList = {"a", "b", "c"};
		for(String t : termList) {
			idx.add_term(t);
		}
		idxProb.display_content("Sure");
	}
	
	// del_term
	public void test_2() {
		idx.del_term("b");
		idxProb.display_content("Sure");
	}
	
	// add_posting_unit
	public void test_3() {
		posting_unit postUnit = new posting_unit();
		postUnit.term = "a";
		idx.add_posting_unit(postUnit.flatten());
		idxProb.display_content("Sure");
	}
	
	// del_posting_unit
	public void test_4() {
		idx.del_posting_unit(3);
		idxProb.display_content("Sure");
		
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
		ConcurrentHashMap<String, Long> metaMap_1 = new ConcurrentHashMap<String, Long> ();
		metaMap_1.put("lockStatus", 0L); // the lock can be required
		metaMap_1.put("threadNum", -1L);
		kpr.get_lockInfoMap(lexicon_locker.class).put("a", metaMap_1);
		
		ConcurrentHashMap<String, Long> metaMap_2 = new ConcurrentHashMap<String, Long> ();
		metaMap_2.put("lockStatus", System.currentTimeMillis() - 5000); // the lock status here is not 0, so that the following add will failed
		metaMap_2.put("threadNum", 2L);
		kpr.get_lockInfoMap(lexicon_locker.class).put("c", metaMap_2);
		
		idxProb.display_content("Sure");
		
		posting_unit postUnit = new posting_unit();
		postUnit.uProp.put("tfidf", 3.33);
		postUnit.term = "a";
		idx.add_posting_unit(postUnit.flatten()); // successful
		
		posting_unit postUnit_2 = new posting_unit();
		postUnit_2.uProp.put("tfidf", 3.35);
		postUnit_2.term = "c";
		idx.add_posting_unit(postUnit_2.flatten());
		
		idxProb.display_content("Sure");
	}
	
	
	// test persist_index
	public void test_6() {
		index_io_operations.get_instance().persist_index();
	}
	
	
	// clear index
	public void test_7() {
		idx.clear_index();
		idxProb.display_content("Sure");
	}
	
	
	// test load lexicon
	public void test_8() {
		index_io_operations.get_instance().load_lexicon();
		idxProb.display_content("Sure");
	}
	
	
	// test load posting
	public void test_9() {
		idx.postUnitMap.put(3L, null); // 3 not the end of a, so "a" should be loaded 
		index_io_operations.get_instance().load_posting(new String[] {"a", "c"});
		idxProb.display_content("Sure");
		
//		idx.postUnitMap.put(4L, new posting_unit()); // 4 is the end of a, so "a" should not be loaded 
//		idx.load_posting(new String[] {"a", "c"});
//		idxProb.display_content("Sure");
		
		for (Long pUnitId : idx.postUnitMap.keySet()) {
			posting_unit pUnit = idx.postUnitMap.get(pUnitId);
			System.out.println("cId: " + pUnit.currentId + " nId: " + pUnit.nextId + " pId: " + pUnit.previousId + " uProp: " + pUnit.uProp.toString() + " docId: " + pUnit.docId + " status: " + pUnit.status);
			
		}
		System.out.println("");
	}
	
	
	// test reload the index
	public void test_10() {
		// idxProb.display_content("Sure");
		idx.reload_index();
		idxProb.display_content("Sure");

		for (Long pUnitId : idx.postUnitMap.keySet()) {
			posting_unit pUnit = idx.postUnitMap.get(pUnitId);
			System.out.println("cId: " + pUnit.currentId + " nId: " + pUnit.nextId + " pId: " + pUnit.previousId + " uProp: " + pUnit.uProp.toString() + " docId: " + pUnit.docId + " status: " + pUnit.status);
			
		}
		System.out.println("");
	}
	
	public void test_11() {
		idx.add_doc(new String[] {"your 2499 5131 2203 {\"tf\":1,\"oh\":1} /test_1/EKAN4jw3LsE3631feSaA_g 1"}, "/test_1/EKAN4jw3LsE3631feSaA_g");
		idxProb.display_content("Sure");
	}
	
	
	
	public static void main(String[] args) {
		index_test idx_test = new index_test();
//		idx_test.test_1();
//		idx_test.test_2();
//		idx_test.test_3();
//		idx_test.test_4();
//		idx_test.test_5();
//		idx_test.test_6();
//		idx_test.test_7();
//		idx_test.test_8();
//		idx_test.test_9();
//		idx_test.test_10();
		idx_test.test_11();
	}
}
