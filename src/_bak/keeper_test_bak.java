package _bak;
import inverted_index.*;
import java.util.*;
import configs.*;

public class keeper_test_bak {
	
	static keeper_bak kpr = keeper_bak.get_instance();
	
	
	
	// print the entries of lexicon lock map
	public void print_lexiconLockMap() {
		// print out the lexiconLockMap
		for (String term : kpr.lexiconLockMap.keySet()) {
			System.out.print(term + "--");
			System.out.println("" + kpr.lexiconLockMap.get(term).entrySet());
		}
		System.out.println("");
	}
	
	// prepare the lexiconLockMap
	public void fill_lexiconLockMap() {
		HashMap<String, Long> metaMap_1 = new HashMap<String, Long>();
		metaMap_1.put("termLock", 1L); // expired time, the lock can be required successfully
		metaMap_1.put("threadNum", 1L);
		
		HashMap<String, Long> metaMap_2 = new HashMap<String, Long>();
		metaMap_2.put("termLock", System.currentTimeMillis() - keeper_config.lockExpireTime); // just expired time, lock can be required
		metaMap_2.put("threadNum", 2L);

		HashMap<String, Long> metaMap_3 = new HashMap<String, Long>();
		metaMap_3.put("termLock", System.currentTimeMillis() + 3000); // not expired time, lock cannot be required
		metaMap_3.put("threadNum", 3L);
		
		kpr.lexiconLockMap.put("t1", metaMap_1);
		kpr.lexiconLockMap.put("t2", metaMap_2);
		kpr.lexiconLockMap.put("t3", metaMap_3);
		
		print_lexiconLockMap();
	}
	

	
	
	public static void main(String[] args) {
		keeper_test_bak kTest = new keeper_test_bak();
		kTest.fill_lexiconLockMap();
		kpr.require_lock("t1", "5"); // required expected
		kpr.require_lock("t2", "5"); // required expected
		kpr.require_lock("t3", "5"); // not required expected
		
		for (String term : kpr.lexiconLockMap.keySet()) {
			kpr.release_lock(term, "0005"); // one thread can only release its own lock, so t1 t2 are released
		}
		kTest.print_lexiconLockMap();
	}
	
}
