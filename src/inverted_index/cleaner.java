package inverted_index;

import java.util.*;
import configs.*;
import utils.*;

// cleaner should only be used on the off-line inverted-index
// clean it and replace the on-line one
// the index is currently being hosted by a single machine
// so that only need to use task splitting instead of the global lock?
public class cleaner {
	// TODO: change to the web reference in future
	private index idx = index.get_instance();
	private keeper kpr = keeper.get_instance();
	
	// independent threads scanning and cleaning the postUnitMap & lexicon
	public ArrayList<Long> clean_unit(String[] targetTerms) {

		ArrayList<Long> delPostUnitList = new ArrayList<Long>();
		
		// scanning through the assigned terms
		for (String term : targetTerms) {
			ArrayList<Long> postingUnitIds = idx.lexicon.get(term);
			
			if(postingUnitIds != null) {
				
				// scanning through the posting units list of corresponding term
				for (long pUnitId : postingUnitIds) {
					int pUnitIndex = postingUnitIds.indexOf(pUnitId);
					
					posting_unit curUnit = idx.postUnitMap.get(pUnitId);
					int pStatus = curUnit.status;
					if(pStatus == 0) {
						posting_unit prevUnit = (pUnitIndex != 0) ? idx.postUnitMap.get(postingUnitIds.get(pUnitIndex - 1)) : null; // skip the first unit of posting list
						posting_unit nextUnit = (pUnitIndex != postingUnitIds.size() - 1) ? idx.postUnitMap.get(postingUnitIds.get(pUnitIndex + 1)) : null; // skip the last unit of posting list
						
						// relink
						if (prevUnit != null) { // when current unit is not the starter
							prevUnit.link_to_next(nextUnit);	
						}
						if (nextUnit != null) { // when current unit is not the ender
							nextUnit.link_to_previous(prevUnit);	
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
		private String thNum;
		
		public thread_clean(String[] targetTerms4Clean, String threadNum) { // parameters for assign tasks
			targetTerms = targetTerms4Clean;
			thNum = threadNum;
			System.out.println(String.format("--> thread %s started", thNum));
		}
		
		public void run() {		
			// lock operations
			try {
				for (String term : targetTerms) {
						if(kpr.require_lock(term, thNum) == 1) { // if required the lock on term successfully
							availableTargetTerms_temp.add(term);	
						}
				}
				
				// TODO: testing
				System.out.println("--> lexiconLockMap: " + kpr.lexiconLockMap.entrySet());
				availableTargetTerms = availableTargetTerms_temp.toArray(new String[0]); // ArrayList<String> -> String[]
				ArrayList<Long> delPostUnitList = clean_unit( availableTargetTerms );
				System.out.println("deleted units: " + delPostUnitList);
				
			} catch(Exception e) {
				System.out.println(e);
				
			} finally {
				for (String term : availableTargetTerms) { // no matter what, release the lock at the end
					kpr.release_lock(term, thNum);	
				}
			}
		}
		
		public void start() {
			t = new Thread(this, thNum);
			t.start();
		}
	}
	
	// multiprocessing
	public void clean() {
		cleaner clr = new cleaner();
		int workerNum = cleaner_config.cleanerWorkerNum;
		
		// split the lexicon into workload for each clean worker
		ArrayList<String> terms = new ArrayList<String>();
		terms.addAll(clr.idx.lexicon.keySet()); // get the terms
		
		int lexiconLength = terms.size();
		int loadPerWorker = (int) Math.ceil((double)lexiconLength / (double)workerNum); // workload for each worker
		int noMoreTerms = 0; // flag for indicating no more terms for clean, 0 still have rest, 1 no more rest
		
		Iterator<String> termsIter = terms.iterator();
		List<String> load_temp = new ArrayList<String>(); // for containing the workload of per worker
		int j = 0;
		for (int i = 0; i < lexiconLength + 1; i++) { // +1 is for allowing the iterator.next to over the end
			
			try {
				load_temp.add( termsIter.next() );
				j++;
			} catch(Exception e) {
				// System.out.println(e);
				noMoreTerms = 1; // when get to the end of the lexicon, no more terms
			}
			
			if(j >= loadPerWorker || noMoreTerms == 1) { // one workload is ready or no more terms
				// TODO: testing
				// System.out.println("" + load_temp);
				
				try {
					thread_clean ct = new thread_clean( load_temp.toArray(new String[0]), "" + name_generator.thread_name_gen() );
					ct.start();
					j = 0;
					load_temp.clear();
					
				} catch(Exception e) {
					System.out.println(e);
				}


			}
		}
	}
	
	
	
	public void main(String[] args) {}

}
