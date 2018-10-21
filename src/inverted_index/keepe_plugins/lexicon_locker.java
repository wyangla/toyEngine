package inverted_index.keepe_plugins;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class lexicon_locker {
	
	// {targetName : infoMap}
	// targetName - term here
	// infoMap - {lockStatus : 0 or timeStamp, threadNum : -1 or threadNum}
	// threadNum - Integer(timeStamp + randomNum * 1000)
	private static HashMap<String, HashMap<String, Long>> lexiconLockInfoMap = new HashMap<String, HashMap<String, Long>>(); // store the lock holder information
	private static HashMap<String, ReentrantLock> lexiconLockMap = new HashMap<String, ReentrantLock>(); // store lock for each target object
	
	public static HashMap<String, HashMap<String, Long>> get_lockInfoMap() {
		return lexiconLockInfoMap;
	}
	
	public static HashMap<String, ReentrantLock> get_lockMap() {
		return lexiconLockMap;
	}
	
	public static void clear_maps() {
		lexiconLockInfoMap = new HashMap<String, HashMap<String, Long>>();
		lexiconLockMap = new HashMap<String, ReentrantLock>();
	}

}
