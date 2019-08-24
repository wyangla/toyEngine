package entities;

import java.util.*;
import configs.*;
import data_structures.*;
import entities.keeper_plugins.lexicon_locker;
import entities.scanner_plugins.*;
import utils.*;
import inverted_index.index;
import entities.keeper_plugins.*;
import java.util.concurrent.*;


// cleaner should only be used on the off-line inverted-index
// clean it and replace the on-line one
// the index is currently being hosted by a single machine
// so that only need to use task splitting instead of the global lock?
public class cleaner {
	
	// singleton
	// ref: http://www.runoob.com/design-pattern/singleton-pattern.html
	private static cleaner clr = new cleaner();
	private cleaner() {};
	public static cleaner getInstance() {
		return clr;
	}
	
	
	// TODO: change to the web reference in future
	private static index idx = index.get_instance();
	private static keeper kpr = keeper.get_instance();
	private static scanner snr = new scanner();
	

	public static class scan_term_thread_cleaner extends Thread {
		private scanner_plugin_interface opOnList;
		private Object opOnListParam;
		private String[] tTerms;
		private ArrayList<Long> affectedUnitIds = new ArrayList<Long>();
		private scanner snr;
		
		private ArrayList<callback> callbacks = new ArrayList<callback>();
		
		public scan_term_thread_cleaner(scanner scannerIns, scanner_plugin_interface operationOnPostingList, Object operationOnPostingListParameter, String[] targetTerms) {
			opOnList = operationOnPostingList;
			opOnListParam = operationOnPostingListParameter; // in order to collect all the 
			tTerms = targetTerms;
			snr = scannerIns;
		}
		
		public void run() {
			try {
				String threadName = "" + name_generator.thread_name_gen();
				ArrayList<String> targetTerms = new ArrayList<String>();
				
				for(String term : tTerms) {
					if(idx.lexicon_2.get(term).firstPostUnitId != -1) {    // exclude the empty posting list
						
						callback release_lock = kpr.require_lock_check_notebook_wait(lexicon_locker.class, term, threadName);
						if(release_lock != null) {    // when target term is not existing in the lock map, release_lock will be null
							callbacks.add(release_lock);
							targetTerms.add(term);
						}	
					}
				}
				
				try {
					opOnList.set_parameters(opOnListParam);
					affectedUnitIds = snr.scan(targetTerms.toArray(new String[0]), opOnList);
					
				}catch(Exception e) {
					System.out.println(e);
				}finally {
					for(callback release_lock: callbacks) {
						release_lock.conduct();
					}
				}

				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		// invoke after the threads ends to collect the affected posting units' ids
		public ArrayList<Long> get_affectedUnitIds(){
			return affectedUnitIds;
		}
	}
	
	
	// need to load all postings into memory firstly, otherwise, the linking of term_chain will lead to problem
	public void clean() {
		// split the lexicon into workload for each clean worker
		ArrayList<String[]> workloads = task_spliter.get_workLoads_terms(cleaner_config.cleanerWorkerNum, idx.lexicon_2.keySet().toArray(new String[0]));
		ArrayList<scan_term_thread_cleaner> threadList = new ArrayList<scan_term_thread_cleaner>();
		
		ArrayList<ConcurrentHashMap<Long, Double>> subElPUnitLists = new ArrayList<ConcurrentHashMap<Long, Double>>();
		ConcurrentHashMap<Long, Double> totalEliminatPostUnit = new ConcurrentHashMap<Long, Double>();
		
		for(String[] workload : workloads) {
			ConcurrentHashMap<Long, Double> eliminatePostUnitIdList = new ConcurrentHashMap<Long, Double>();    // use as a list
			scan_term_thread_cleaner ct = new scan_term_thread_cleaner(
					snr, 
					new clean_posting(), 
					eliminatePostUnitIdList, 
					workload);
			
			threadList.add(ct);
			subElPUnitLists.add(eliminatePostUnitIdList);
		}
		
		for(scan_term_thread_cleaner ct : threadList) {
			try {
				ct.start();
				ct.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for(ConcurrentHashMap<Long, Double> elPUnitList: subElPUnitLists) {
			System.out.println(elPUnitList);    // TODO: tests
			totalEliminatPostUnit.putAll(elPUnitList);
		}
		
		// eliminate the pUnit all in once, instead of during the scanning, otherwise will effect the integrity of the posting list
		for(Long pUnitId: totalEliminatPostUnit.keySet()) {
			idx.postUnitMap.remove(pUnitId);
		}
		
	}
	
	public void main(String[] args) {}
}
