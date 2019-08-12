package entities.keeper_plugins;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class lexicon_locker {
	
	// {targetName : infoMap}
	// targetName - term here
	// infoMap - {lockStatus : 0 or timeStamp, threadNum : -1 or threadNum}
	// threadNum - Integer(timeStamp + randomNum * 1000)
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> lexiconLockInfoMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, Long>>(); // store the lock holder information
	private static ConcurrentHashMap<String, ReentrantLock> lexiconLockMap = new ConcurrentHashMap<String, ReentrantLock>(); // store lock for each target object
	
	public static ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> get_lockInfoMap() {
		return lexiconLockInfoMap;
	}
	
	public static ConcurrentHashMap<String, ReentrantLock> get_lockMap() {
		return lexiconLockMap;
	}
	
	public static void clear_maps() {
		lexiconLockInfoMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, Long>>();
		lexiconLockMap = new ConcurrentHashMap<String, ReentrantLock>();
	}

}
