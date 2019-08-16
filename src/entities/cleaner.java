package entities;

import java.util.*;
import configs.*;
import data_structures.posting_unit;
import entities.keeper_plugins.lexicon_locker;
import utils.*;
import inverted_index.index;
import entities.keeper.callback;
import entities.keeper_plugins.*;


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
	
	
	// TODO: change to the web reference in future?
	private index idx = index.get_instance();
	private keeper kpr = keeper.get_instance();
	
	
	// independent threads scanning and cleaning the postUnitMap & lexicon
	public ArrayList<Long> clean_unit(String[] targetTerms) {

		ArrayList<Long> delPostUnitList = new ArrayList<Long>();
		
		// scanning through the assigned terms
		for (String term : targetTerms) {
			ArrayList<Long> postingUnitIds = idx.lexicon.get(term);    // TODO: change to term object with pointer
			
			if(postingUnitIds != null) {
				
				// scanning through the posting units list of corresponding term
				for (long pUnitId : postingUnitIds) {
					int pUnitIndex = postingUnitIds.indexOf(pUnitId);
					
					posting_unit curUnit = idx.postUnitMap.get(pUnitId);
					int pStatus = curUnit.status;
					if(pStatus == 0) {
						
						// relink the posting chain
						posting_unit prevUnit = (pUnitIndex != 0) ? idx.postUnitMap.get(postingUnitIds.get(pUnitIndex - 1)) : null; // skip the first unit of posting list
						posting_unit nextUnit = (pUnitIndex != postingUnitIds.size() - 1) ? idx.postUnitMap.get(postingUnitIds.get(pUnitIndex + 1)) : null; // skip the last unit of posting list
						
						if (prevUnit != null) { // when current unit is not the starter
							prevUnit.link_to_next(nextUnit);	
						}
						if (nextUnit != null) { // when current unit is not the ender
							nextUnit.link_to_previous(prevUnit);	
						}
						
						// relink the term posting chain
						posting_unit prevTermUnit = (curUnit.previousTermId != -1) ? idx.postUnitMap.get(curUnit.previousTermId) : null; // skip the first unit of term posting chain
						posting_unit nextTermUnit = (curUnit.nextTermId != -1) ? idx.postUnitMap.get(curUnit.nextTermId) : null; // skip the last unit of term posting chain

						if (prevTermUnit != null) { // when current unit is not the first term unit
							prevTermUnit.link_to_next_term(nextTermUnit);	
						}
						if (nextTermUnit != null) { // when current unit is not the last term unit
							nextTermUnit.link_to_previous_term(prevTermUnit);	
						}
						
						delPostUnitList.add(pUnitId);
					}
				}
				
				// avoid the ArrayList is changing when iterating it
				for (Long pUnitId : delPostUnitList) {
					// delete from lexicon and postUnitMap
					postingUnitIds.remove(pUnitId);
					idx.postUnitMap.remove(pUnitId);
				}
			}

		}
		return delPostUnitList;
	}
	
	// define the thread object, ref: https://www.runoob.com/java/java-multithreading.html
	class thread_clean extends Thread {
		private Thread t;
		private String[] targetTerms;
		private ArrayList<String> availableTargetTerms_temp = new ArrayList<String>();
		private String[] availableTargetTerms; // which are not being accessing
		
		private ArrayList<callback> callbacks = new ArrayList<callback> ();    // for holding the release lock callbacks from keeper
		
		public thread_clean(String[] targetTerms4Clean, String threadNum) { // parameters for assign tasks
			targetTerms = targetTerms4Clean;
			this.setName(threadNum);
			// System.out.println(String.format("--> thread %s started", thNum));
		}
		
		public void run() {		
			// require locks firstly
			for (String term : targetTerms) {
				
				callback release_lock = kpr.require_lock_check_notebook_wait(lexicon_locker.class, term, this.getName());
				
				if(release_lock != null) {    // if null, means the target term is not existing, indeed not needed here, as the target terms come from lexicon, should always exists in lock maps
					availableTargetTerms_temp.add(term);
					callbacks.add(release_lock);
				}
			}
			
			try {	
				// TODO: testing
				// System.out.println("--> lexiconLockMap: " + kpr.lexiconLockMap.entrySet());
				availableTargetTerms = availableTargetTerms_temp.toArray(new String[0]); // ArrayList<String> -> String[]
				ArrayList<Long> delPostUnitList = clean_unit( availableTargetTerms );
				System.out.println("deleted units: " + delPostUnitList);
				
			} catch(Exception e) {
				e.printStackTrace();
				
			} finally {
				for (callback release_lock : callbacks) { // release the locks that successfully required
					release_lock.conduct();	
				}
			}
		}
	}
	
	// multiprocessing
	public void clean() {
		int workerNum = cleaner_config.cleanerWorkerNum;
		
		// split the lexicon into workload for each clean worker
		ArrayList<String> terms = new ArrayList<String>();
		terms.addAll(idx.lexicon.keySet()); // get the terms
		
		int lexiconLength = terms.size();
		int loadPerWorker = (int) Math.ceil((double)lexiconLength / (double)workerNum); // workload for each worker
		int noMoreTerms = 0; // flag for indicating no more terms for clean, 0 still have rest, 1 no more rest
		
		// TODO: testing
		System.out.println("<loadPerWorker> "+ loadPerWorker);
		
		Iterator<String> termsIter = terms.iterator();
		List<String> load_temp = new ArrayList<String>(); // for containing the workload of per worker
		
		
		ArrayList<thread_clean> allThreads = new ArrayList<thread_clean>(); // for control the threads all together
		for (int i = 0; i < lexiconLength + 1; i++) { 
			// +1 is for allowing the iterator.next to over the end, 
			// when there are workload == n * loadPerWorker, there will be one more extra thread created and do nothing
			// this extra thread will be cheaper than the load_temp.isempty() check every loop?
			
			try {
				load_temp.add( termsIter.next() );
			} catch(Exception e) {
				// System.out.println(e);
				noMoreTerms = 1; // when get to the end of the lexicon, no more terms
			}
			
			if(load_temp.size() >= loadPerWorker || noMoreTerms == 1) { // one workload is ready or no more terms
				// TODO: testing
				System.out.println(">" + load_temp);
				try {
					thread_clean ct = new thread_clean( load_temp.toArray(new String[0]), "" + name_generator.thread_name_gen() );
					allThreads.add(ct);
					ct.start();
					load_temp.clear();
					
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	
		// block until all threads are finished
		for(thread_clean ct : allThreads) {
			try {
				ct.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public void main(String[] args) {}
}
