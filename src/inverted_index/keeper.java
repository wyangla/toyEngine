package inverted_index;

import java.util.ArrayList;
import java.util.HashMap;

// ref: ZooKeeper, maintaining the lock map, 
// store the meta informations of the lexicon, especially the global lock
public class keeper {
	// {term : metaMap}
	// metaMap - {termLock : 0 or timeStamp, thread : threadNum}
	// TODO: threadNum = Integer(timeStamp + randomNum * 1000)
	public HashMap<String, HashMap<String, Long>> lexiconLockMap = new HashMap<String, HashMap<String, Long>>(); 
	
	// singleton
	// ref: http://www.runoob.com/design-pattern/singleton-pattern.html
	private static keeper keeper_ins = new keeper();
	private keeper() {}
	public static keeper get_instance() {
		return keeper_ins;
	}
	
	// initialize the lock for term in lexicon
	public void add_term(String term) {
		HashMap<String, Long> metaMap = new HashMap<String, Long>();
		metaMap.put("termLock", 0L); // 0 not locked; timeStampe locked
		metaMap.put("threadNum", -1L); // the default thread name
		this.lexiconLockMap.put(term, metaMap);
	}
	
	// delte the lock of term
	public void del_term(String term) {
		this.lexiconLockMap.remove(term);
	}
	
	// require the lock for specific thread
	public int require_lock(String term, String threadNum) {
		int required;  // 0 not required, 1 required
		if (this.lexiconLockMap.get(term).get("termLock") == 0) { // if one term is not being modifying, like adding / deleting units
			this.lexiconLockMap.get(term).put("termLock", System.currentTimeMillis()); // add lock, record locking time
			this.lexiconLockMap.get(term).put("threadNum", Long.parseLong(threadNum)); // record the thread that required the lock, for update and automatically release
			required = 1;
		}else {
			required = 0;
		}
		return required;		
	}
	
	// release the lock
	public int release_lock(String term, String threadNum) {
		int released; // 0 not released, 1 released
		if (this.lexiconLockMap.get(term).get("threadNum") == Long.parseLong(threadNum)) {
			this.lexiconLockMap.get(term).put("termLock", 0L);
			this.lexiconLockMap.get(term).put("threadNum", -1L);
			released = 1;
		} else {
			released = 0;
		}
		return released;
	}
	
	
	
}
