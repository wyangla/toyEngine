package test;
import inverted_index.*;
import java.util.*;
import configs.*;
import entities.keeper;
import entities.keeper_plugins.lexicon_locker;
import entities.keeper_plugins.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


public class keeper_test {
	
	static keeper kpr = keeper.get_instance();
	
	
	
	// print the entries of lexicon lock map
	public void print_lexiconLockRelatedMaps() {
		System.out.println(("--print_lexiconLockRelatedMaps"));
		ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> LockInfoMap = kpr.get_lockInfoMap(lexicon_locker.class);
		ConcurrentHashMap<String, ReentrantLock> lockMap = kpr.get_lockMap(lexicon_locker.class);
		
		// print out the lexiconLockMap
		for (String term : LockInfoMap.keySet()) {
			System.out.println("\n" + term + "--");
			System.out.println("INFO: " + LockInfoMap.get(term).entrySet());
			System.out.println("LOCK: " + lockMap.get(term));
		}
		System.out.println("");
	}
	
	
	// print the entries of lexicon lock map
	public void print_originalMaps() {
		System.out.println(("--print_originalMaps"));
		ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> LockInfoMap = lexicon_locker.get_lockInfoMap();
		ConcurrentHashMap<String, ReentrantLock> lockMap = lexicon_locker.get_lockMap();
		
		// print out the lexiconLockMap
		for (String term : LockInfoMap.keySet()) {
			System.out.println("\n" + term + "--");
			System.out.println("INFO: " + LockInfoMap.get(term).entrySet());
			System.out.println("LOCK: " + lockMap.get(term));
		}
		System.out.println("");
	}
	
	
	// prepare the lexiconLockMap
	public void fill_lexiconLockMap() {
		
		ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> lockInfoMap = kpr.get_lockInfoMap(lexicon_locker.class);
		ConcurrentHashMap<String, ReentrantLock> lockMap = kpr.get_lockMap(lexicon_locker.class);
		
		kpr.add_target(lexicon_locker.class, "a");
		kpr.add_target(lexicon_locker.class, "b");
		kpr.add_target(lexicon_locker.class, "c");
		
		ConcurrentHashMap<String, Long> infoMap = lockInfoMap.get("c");
		infoMap.put("lockStatus", System.currentTimeMillis() - 5000); // expired
		
		print_lexiconLockRelatedMaps();
	}
	

	
	
	public static void main(String[] args) {
		keeper_test kTest = new keeper_test();
		kTest.fill_lexiconLockMap();
		
		ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> lockInfoMap = kpr.get_lockInfoMap(lexicon_locker.class);
		ConcurrentHashMap<String, ReentrantLock> lockMap = kpr.get_lockMap(lexicon_locker.class);
		
//		System.out.println(kpr.require_lock(lexicon_locker.class, "a", "1")); // required expected
//		System.out.println(kpr.require_lock(lexicon_locker.class, "b", "1")); // required expected
//		System.out.println(kpr.require_lock(lexicon_locker.class, "a", "1")); // not required expected
//		
//		for (String term : lockMap.keySet()) {
//			try {
//				System.out.println(lockMap.get(term));
//				System.out.println("->" + kpr.check_lock_expiration(lexicon_locker.class, term, "1"));
//				System.out.println(kpr.release_lock(lexicon_locker.class, term, "1"));
//				
//			} catch(Exception e) {
//				e.printStackTrace();
//			}
//
//		} // release lock of c will raise exception for its not locked == not locked by main thread
		kTest.print_lexiconLockRelatedMaps();
		kTest.print_originalMaps();
	
		// only reset set the reference instead of the original hashmap
		kpr.clear_maps(lexicon_locker.class);
		kTest.print_lexiconLockRelatedMaps();
		kTest.print_originalMaps();
	}
	
}
