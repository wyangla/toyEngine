package _bak;

import java.util.ArrayList;
import java.util.HashMap;
import configs.keeper_config;

// ref: ZooKeeper, maintaining the lock map, 
// store the meta informations of the lexicon, especially the global lock
// locks are released by the owner thread or overwritten by other thread when expired
public class keeper_bak {
	
	// {term : metaMap}
	// metaMap - {termLock : 0 or timeStamp, threadNum : -1 or threadNum}
	// TODO: threadNum = Integer(timeStamp + randomNum * 1000)
	public HashMap<String, HashMap<String, Long>> lexiconLockMap = new HashMap<String, HashMap<String, Long>>(); 
	
	// singleton
	// ref: http://www.runoob.com/design-pattern/singleton-pattern.html
	private static keeper_bak keeper_ins = new keeper_bak();
	private keeper_bak() {}
	public static keeper_bak get_instance() {
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
		// TODO: test
		// System.out.println(term + "!");
		if (this.lexiconLockMap.get(term).get("termLock") == 0) { // if one term is not being modifying, like adding / deleting units
			this.lexiconLockMap.get(term).put("termLock", System.currentTimeMillis()); // add lock, record locking time
			this.lexiconLockMap.get(term).put("threadNum", Long.parseLong(threadNum)); // record the thread that required the lock, for update and automatically release
			required = 1;
		}else {
			if (lexiconLockMap.get(term).get("termLock") + keeper_config.lockExpireTime <= System.currentTimeMillis()) { // if the previous lock is expired
				this.lexiconLockMap.get(term).put("termLock", System.currentTimeMillis());
				this.lexiconLockMap.get(term).put("threadNum", Long.parseLong(threadNum));
				required = 1;
			} else {
				required = 0;
			}
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
