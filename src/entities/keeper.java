package entities;

import java.util.ArrayList;
import java.util.HashMap;
import configs.keeper_config;
import entities.keeper_plugins.*;

import java.lang.reflect.*;
import java.util.concurrent.locks.ReentrantLock;



// ref: ZooKeeper, maintaining the lock map, 
// apply operations on the lexiconInfoMap and lexiconMap provide by locker plugins
// locks are released by the owner thread or overwritten by other thread when expired
public class keeper {

	// singleton
	// ref: http://www.runoob.com/design-pattern/singleton-pattern.html
	private static keeper kpr = new keeper();
	private keeper() {}
	public static keeper get_instance() {
		return kpr;
	}
	
	
	// get the lockInfoMap from locker
	public HashMap<String, HashMap<String, Long>> get_lockInfoMap(Class<lexicon_locker> lockerClass) {
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
	public HashMap<String, ReentrantLock> get_lockMap(Class<lexicon_locker> lockerClass) {
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
	public void clear_maps(Class<?> lockerClass) {
		try {
			Method clear_maps = lockerClass.getMethod("clear_maps");
			clear_maps.invoke(null);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// initialize the lock for term in lexicon
	public void add_target(Class<lexicon_locker> lockerClass, String targetName) {
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
	public void del_target(Class<lexicon_locker> lockerClass, String targetName) {
		get_lockInfoMap(lockerClass).remove(targetName);
		get_lockMap(lockerClass).remove(targetName);
	}
	
	
	
	
	/*
	 * naive mutual exclusive
	 * */	
	
	// require the lock for specific thread
	private int require_lock(Class<lexicon_locker> lockerClass, String targetName, String threadNum) {
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
	private int release_lock(Class<lexicon_locker> lockerClass, String targetName, String threadNum) {
		int released = 0; // 0 not released, 1 released
		
		HashMap<String, Long> infoMap = get_lockInfoMap(lockerClass).get(targetName);
		ReentrantLock targetLock = get_lockMap(lockerClass).get(targetName);
		
		// does not need to check the thread here, 
		// as the lock and unlock are paired,
		// so that the lock will always be the one hold by the current thread
		try {
			targetLock.unlock();    // here may raise exception when try to unlock the unacquired lock, TODO: add try catch?
			infoMap.put("lockStatus", 0L);
			infoMap.put("threadNum", -1L);
			released = 1;
			
		}catch(IllegalMonitorStateException e) {
			e.printStackTrace();    // if the lock is not successfully acquired by the thread in the first place
		}

		return released;
	}
	
	
	// aims at being used in the scenario of waiting for web response
	public int check_lock_expiration(Class<lexicon_locker> lockerClass, String targetName, String threadNum) {
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

	
	
	/*
	 * 1:m mutual exclusive:
	 * 1 * deactivate||adding_unit : m * scanning
	 * */
	
	
	
	// must be conducted
	public interface callback{
		public int conduct();
	}
	
	// for recording the visiting thread names, {name:1}
	private static HashMap<String, HashMap<String, Integer>> notebooks = new HashMap<String, HashMap<String, Integer>> ();
	
	
	
	// for scanner usage
	class eliminate_name_callback implements callback{
		String thName;
		String tarName;
		
		public eliminate_name_callback(String targetName, String threadName) {
			tarName = targetName;
			thName = threadName;
		}
		public int conduct() {
			HashMap<String, Integer> notebook = notebooks.get(tarName);
			Integer nameRemoved = notebook.remove(thName);
			if(nameRemoved == null) {
				nameRemoved = 0;    // thread name is not existing, could means the name is not successfully added
			}
			return nameRemoved;
		}
	}
	
	public callback add_note(Class<?> lockerClass, String targetName, String threadNum) throws NoSuchMethodException, SecurityException {
		
		eliminate_name_callback eliminate_name = null;
		
		try {
			int required = 0;
			while(required != 1) {    // keep trying until get the lock
				required = require_lock(lexicon_locker.class, targetName, threadNum);
			}
			
			HashMap<String, Integer> notebook = notebooks.get(targetName);
			if (notebook == null) {    // if the notebook for a term is not existing, create it dynamically, so does not require a explicit initialisation
				notebook = new HashMap<String, Integer>();
				notebooks.put(targetName, notebook);
			}else {
				notebook.put(threadNum, 1); // add thread name to the notebook, stands for visiting the term corresponding to the lock	
			}
			eliminate_name = new eliminate_name_callback(targetName, threadNum);
			
		}catch(Exception e) {
			System.out.print(e);
		}finally {
			release_lock(lexicon_locker.class, targetName, threadNum);
		}
		
		return eliminate_name;		
	}
	
	
	
	
	class release_lock_call_back implements callback{
		Class<lexicon_locker> lClass;
		String tarName;
		String thName;
		
		public release_lock_call_back(Class<lexicon_locker> lockerClass, String targetName, String threadNum) {
			lClass = lockerClass;
			tarName = targetName;
			thName = threadNum;
		}
		
		public int conduct() {
			int released = release_lock(lClass, tarName, thName);
			return released;
		}
		
	}
	
	
	// for deactivator usage	
	public callback require_lock_check_notebook(Class<lexicon_locker> lockerClass, String targetName, String threadNum) {
		release_lock_call_back release_lock = null;
		
		try {
			// only try once
			int required = require_lock(lexicon_locker.class, targetName, threadNum);
			
			if(required == 1) {
				HashMap<String, Integer> notebook = notebooks.get(targetName);
				
				if (notebook == null) {
					notebook = new HashMap<String, Integer>();
					notebooks.put(targetName, notebook);
				}
				
				// check if the notebook is empty
				// empty, could be 1. newly created notebook; 2. all visiting threads are gone
				// if empty, directly return the release_lock
				// if not empty, release the required lock immediately
				if(!notebook.isEmpty()) {
					release_lock(lexicon_locker.class, targetName, threadNum);    // if release here, the returned callback still needs to be invoked
				}
			}
			
			release_lock = new release_lock_call_back(lockerClass, targetName, threadNum);
			
		}catch(Exception e) {
			System.out.print(e);
		}
		
		return release_lock;
	}
	


	// for add_posting_unit usage
	public callback require_lock_check_notebook_wait(Class<lexicon_locker> lockerClass, String targetName, String threadNum) {
		release_lock_call_back release_lock = null;
		
		try {
			// only try once
			int required = 0;
			
			// wait to get the lock
			while(required != 1) {
				require_lock(lexicon_locker.class, targetName, threadNum);    // keep trying until get the lock
			}
			
			if(required == 1) {
				HashMap<String, Integer> notebook = notebooks.get(targetName);
				
				if (notebook == null) {
					notebook = new HashMap<String, Integer>();
					notebooks.put(targetName, notebook);
				}
				
				// wait the notebook to be empty, then return to the invoker to conduct the adding operations, etc.
				while(!notebook.isEmpty()) {}
			}
			
			release_lock = new release_lock_call_back(lockerClass, targetName, threadNum);
			
		}catch(Exception e) {
			System.out.print(e);
		}
		
		return release_lock;
	}
	
	
	
}
