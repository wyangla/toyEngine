package probes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import entities.keeper;
import entities.keeper_plugins.lexicon_locker;


public class lockMaps_probe{
	
	// print the entries of lexicon lock map
	public static void print_lockMaps() {
		keeper kpr = keeper.get_instance();
		
		System.out.println(("\n--lockMaps"));
		ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> LockInfoMap = kpr.get_lockInfoMap(lexicon_locker.class);
		ConcurrentHashMap<String, ReentrantLock> lockMap = kpr.get_lockMap(lexicon_locker.class);
		
		// print out the lexiconLockMap
		for (String term : LockInfoMap.keySet()) {
			System.out.println(term + "--");
			System.out.println("INFO: " + LockInfoMap.get(term).entrySet());
			System.out.println("LOCK: " + lockMap.get(term));
		}
		System.out.println("");
	}
	
}