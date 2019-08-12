package test;
import java.sql.Time;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import entities.keeper;
import entities.keeper.callback;
import entities.keeper_plugins.lexicon_locker;
import utils.counter;
import utils.name_generator;




class keeper_test_2{
	
	static keeper kpr = keeper.get_instance();
	
	public void print_notebook() {
		System.out.println("\n--notebook");
		for(String term : kpr.notebooks.keySet()) {
			counter notebook = kpr.notebooks.get(term);
			System.out.println(term + " -- " + notebook.toString());
		}
//		System.out.println();
	}
	
	// print the entries of lexicon lock map
	public void print_lockMaps() {
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
	
	
	// prepare the lexiconLockMap
	public void fill_lexiconLockMap() {
		
		ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> lockInfoMap = kpr.get_lockInfoMap(lexicon_locker.class);
		ConcurrentHashMap<String, ReentrantLock> lockMap = kpr.get_lockMap(lexicon_locker.class);
		
		kpr.add_target(lexicon_locker.class, "a");
		kpr.add_target(lexicon_locker.class, "b");
		kpr.add_target(lexicon_locker.class, "c");
		
		ConcurrentHashMap<String, Long> infoMap = lockInfoMap.get("c");
		infoMap.put("lockStatus", 0L); // expired
		print_notebook();
		print_lockMaps();
	}
	
	
	
	/* 
	 * threads make use of different locks
	 * */
	
	// thread 1, use the add_note
	public static class thread_1 extends Thread {
		keeper_test_2 t2 = new keeper_test_2();
		
		public thread_1() {
		}
		
		public void run() {
			try {
				while(true) {
					System.out.println("---t1---\n\n\n");
					
					callback eliminate_name = kpr.add_note(lexicon_locker.class, "e", "001");
					if(eliminate_name != null) {
						System.out.print("__t1__>>>");
						t2.print_notebook();
						
						Thread.sleep(6000);
						eliminate_name.conduct();
						
						t2.print_notebook();
						System.out.print("<<<__t1__\n\n\n");
					}else {    // term not existing
						System.out.println("term not existing in __t1__ thread related lockMap");
					}
					
					Thread.sleep(1000);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	// thread 2, use the require_lock_check_notebook
	public static class thread_2 extends Thread {
		keeper_test_2 t2 = new keeper_test_2();
		
		public thread_2() {
		}
		
		public void run() {
			try {
				while(true) {
					System.out.print("---t2---\n\n\n");
					
					callback release_lock = kpr.require_lock_check_notebook(lexicon_locker.class, "b", "002");
					
					if(release_lock != null) {
						System.out.print("__t2__>>>");
						
						t2.print_notebook();
						t2.print_lockMaps();
						Thread.sleep(4000);
						
						System.out.print("<<<__t2__\n\n\n");
						
						release_lock.conduct();	
					}
					
					Thread.sleep(1000);
				}

			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// thread 3, use the require_lock_check_notebook_wait
	public static class thread_3 extends Thread {
		keeper_test_2 t2 = new keeper_test_2();
		
		public thread_3() {
		}
		
		public void run() {
			try {
				while(true) {
					System.out.print("---t3---\n\n\n");
					
					callback release_lock = kpr.require_lock_check_notebook_wait(lexicon_locker.class, "b", "003");
					
					if(release_lock != null) {
						System.out.print("__t3__>>>");
						
						t2.print_notebook();
						t2.print_lockMaps();    // the lock map can only hold 3 or 2 or 1, 3 should be more frequent than 2, as wait
						Thread.sleep(6000);
						
						System.out.print("<<<__t3__\n\n\n");
						
						release_lock.conduct();
					}
					
					Thread.sleep(1000);
				}

			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		keeper_test_2 t2 = new keeper_test_2();
		t2.fill_lexiconLockMap();
		
		thread_1 th1 = new thread_1();
		thread_2 th2 = new thread_2();
		thread_3 th3 = new thread_3();
		
		th1.start();
		th2.start();
		th3.start();
		
		System.out.print("---------started----------\n\n\n");
		
		
		
	}
	
}