package test;
import java.util.*;

import inverted_index.*;
import entities.keeper_plugins.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data_structures.posting_unit;
import entities.cleaner;
import entities.keeper;
import entities.keeper_plugins.lexicon_locker;



public class cleaner_test {
	index idx = index.get_instance();
	cleaner clr = cleaner.getInstance();
	keeper kpr= keeper.get_instance();
	Logger lgr = LoggerFactory.getLogger(cleaner_test.class);
	
	
	// prepare the inverted-index
	public void fill_index() {
		String[] termList = {"a", "b", "c", "d"};
		for(String t : termList) {
			idx.add_term(t);
		}
		
		for (String term : termList) {
			for(int i = 0; i < 2; i ++) {
				posting_unit postUnit = new posting_unit();
				idx.add_posting_unit(term + " " + postUnit.flatten());
			}
		}
		
		idx.del_posting_unit(0); // end of posting list 
		idx.del_posting_unit(6); // middle of posting list
		idx.del_posting_unit(7); // start of posting list
		
		System.out.println("postUnitMap: " + idx.postUnitMap.entrySet());
		System.out.println("lexicon: " + idx.lexicon.entrySet());
		System.out.println("lexiconKeeper: " + kpr.get_lockInfoMap(lexicon_locker.class).entrySet());
		System.out.println("lexiconKeeper: " + kpr.get_lockMap(lexicon_locker.class));
		
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
		System.out.println("lexiconKeeper: " + kpr.get_lockInfoMap(lexicon_locker.class).entrySet());
		System.out.println("lexiconKeeper: " + kpr.get_lockMap(lexicon_locker.class));
		
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
		System.out.println("lexiconKeeper: " + kpr.get_lockInfoMap(lexicon_locker.class).entrySet());
		System.out.println("lexiconKeeper: " + kpr.get_lockMap(lexicon_locker.class));
	}
	
	public static void main(String[] args) {
		// prepare the inverted index
		cleaner_test clr_test = new cleaner_test();
		clr_test.fill_index();
		
//		clr_test.test_5();
		clr_test.test_6();
		
		clr_test.lgr.warn("cleaner");
	}
}
