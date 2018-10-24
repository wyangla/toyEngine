package entities;

import java.util.ArrayList;
import java.util.HashMap;
import configs.keeper_config;
import java.lang.reflect.*;
import java.util.concurrent.locks.ReentrantLock;



// ref: ZooKeeper, maintaining the lock map, 
// apply operations on the lexiconInfoMap and lexiconMap provide by locker plugins
// locks are released by the owner thread or overwritten by other thread when expired
public class keeper {

	// singleton
	// ref: http://www.runoob.com/design-pattern/singleton-pattern.html
	private static keeper keeper_ins = new keeper();
	private keeper() {}
	public static keeper get_instance() {
		return keeper_ins;
	}
	
	
	// get the lockInfoMap from locker
	public HashMap<String, HashMap<String, Long>> get_lockInfoMap(Class lockerClass) {
		HashMap<String, HashMap<String, Long>> lockInfoMap = new HashMap<String, HashMap<String, Long>>();
		try {
			Method get_lockInfoMapMethod = lockerClass.getMethod("get_lockInfoMap");
			lockInfoMap = (HashMap<String, HashMap<String, Long>>) get_lockInfoMapMethod.invoke(null);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return lockInfoMap;
	}
	
	
	// get the lockMap from locker 
	public HashMap<String, ReentrantLock> get_lockMap(Class lockerClass) {
		HashMap<String, ReentrantLock> lockMap = new HashMap<String, ReentrantLock>();
		try {
			Method get_lockMapMethod = lockerClass.getMethod("get_lockMap");
			lockMap = (HashMap<String, ReentrantLock>) get_lockMapMethod.invoke(null);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return lockMap;
	} 
	
	
	// clear lockInfoMap and lockMap
	public void clear_maps(Class lockerClass) {
		try {
			Method clear_maps = lockerClass.getMethod("clear_maps");
			clear_maps.invoke(null);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// initialize the lock for term in lexicon
	public void add_target(Class lockerClass, String targetName) {
		HashMap<String, Long> infoMap = new HashMap<String, Long>();
		infoMap.put("lockStatus", 0L); // 0 not locked; timeStampe locked
		infoMap.put("threadNum", -1L); // the default thread name
		
		get_lockInfoMap(lockerClass).put(targetName, infoMap);
		// create lock for each target object, 
		// the lock is not differed by the target object (essentially by the lock object it self) 
		// so that here we could just use the name to get the lock, 
		// as long as we invoke lock around the operations about target object
		get_lockMap(lockerClass).put(targetName, new ReentrantLock()); 
	}
	
	
	// delete the lock of term
	public void del_target(Class lockerClass, String targetName) {
		get_lockInfoMap(lockerClass).remove(targetName);
		get_lockMap(lockerClass).remove(targetName);
	}
	
	
	// require the lock for specific thread
	public int require_lock(Class lockerClass, String targetName, String threadNum) {
		int required;  // 0 not required, 1 required
		
		HashMap<String, Long> infoMap = get_lockInfoMap(lockerClass).get(targetName);
		ReentrantLock targetLock = get_lockMap(lockerClass).get(targetName);
		
		// TODO: test
		// System.out.println(term + "!");
		if (infoMap.get("lockStatus") == 0) { // if one target is not being modifying, e.g. term with adding / deleting units
			
			targetLock.lock(); // add the lock, here not using try.. as there will be a try.. logic in the application
			infoMap.put("lockStatus", System.currentTimeMillis()); // change lock status, record locking time
			infoMap.put("threadNum", Long.parseLong(threadNum)); // record the thread that required the lock, for update and automatically release
			required = 1;

		}else {
			// does not consider the lock expiration here, the infoMap is used by the operation methods which will use finally{release...}
			required = 0;
		}
		return required;		
	}
	
	
	// release the lock
	public int release_lock(Class lockerClass, String targetName, String threadNum) {
		int released; // 0 not released, 1 released
		
		HashMap<String, Long> infoMap = get_lockInfoMap(lockerClass).get(targetName);
		ReentrantLock targetLock = get_lockMap(lockerClass).get(targetName);
		
		// does not need to check the thread here, 
		// as the lock and unlock are paired,
		// so that the lock will always be the one hold by the current thread 
		targetLock.unlock(); // here may raise exception when try to unlock the unlocked lock, TODO: add try catch?
		infoMap.put("lockStatus", 0L);
		infoMap.put("threadNum", -1L);
		released = 1;

		return released;
	}
	
	
	// aims at being used in the scenario of waiting for web response
	public int check_lock_expiration(Class lockerClass, String targetName, String threadNum) {
		int expired = 1;
		HashMap<String, Long> infoMap = get_lockInfoMap(lockerClass).get(targetName);
		ReentrantLock targetLock = get_lockMap(lockerClass).get(targetName);
		
		if (infoMap.get("lockStatus") + keeper_config.lockExpireTime <= System.currentTimeMillis()) { // if the previous lock is expired
			expired = 1;
		} else {
			expired = 0;
		}
		return expired;
	}

	

		
}
