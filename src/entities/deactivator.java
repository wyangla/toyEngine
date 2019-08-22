package entities;

import java.util.ArrayList;

import configs.deactivator_config;
import entities.information_manager_plugins.posting_loaded_status;
import entities.scanner_plugins.delete_posting;
import inverted_index.*;
import utils.task_spliter;
import utils.callback;
import java.util.concurrent.locks.ReentrantLock;;


public class deactivator {
	private static information_manager infoManager = information_manager.get_instance();
	private static index idx = index.get_instance();
	private static index_io_operations idxIOOp = index_io_operations.get_instance();
	private static scanner snr = new scanner();
	private static ReentrantLock pauseLock = new ReentrantLock();
	
	
	private static deactivator dac;
	private deactivator() {}
	public static deactivator get_instance() {
		if(dac == null) {
			dac = new deactivator();
		}
		return dac;
	}
	
	
	
	public boolean check_expired(String targetTerm) {
		boolean expiredFlag = false;
		Double vistTimeStamp = infoManager.get_info(posting_loaded_status.class, targetTerm);	// the last visit time stamp
		Double curTimeStamp = (double)System.currentTimeMillis();
		
		// only the loaded can be expired
		if(vistTimeStamp != -1 && curTimeStamp > vistTimeStamp + (double)deactivator_config.loadExpireTime) {
			expiredFlag = true;
		}
		return expiredFlag;
	}
	
	
	// deactivate the posting list of one term 
	public ArrayList<Long> deactivate() throws Exception{
		ArrayList<Long> affectedUnitIds = new ArrayList<Long>();
		ArrayList<String> deactivatedTerms = new ArrayList<String>();
		
		ArrayList<String> expiredTerms = new ArrayList<String>();
		ArrayList<scanner.scan_term_thread_deactivator> threadList = new ArrayList<scanner.scan_term_thread_deactivator>();
		
		// get the expired terms firstly
		for (String term : idx.lexicon_2.keySet()) {
			if(check_expired(term)) {
				expiredTerms.add(term);
			}
		}
		
		// delete the posting units of one term
		ArrayList<String[]> workLoads = task_spliter.get_workLoads_terms(deactivator_config.workerNum, expiredTerms.toArray(new String[0]));

		for(String[] workLoad : workLoads ) {
			scanner.scan_term_thread_deactivator st = new scanner.scan_term_thread_deactivator(snr, new delete_posting(), "", workLoad);
			st.start();
			threadList.add(st);
		}
		
		for(scanner.scan_term_thread_deactivator st : threadList) {
			st.join();
			affectedUnitIds.addAll(st.get_affectedUnitIds());
			deactivatedTerms.addAll(st.get_scannedTerms());
		}

		// delete posting_loaded_status of handled terms
		// not touching the term_max_tf here, as it is not removing the term from index
		for(String term : deactivatedTerms) {
			infoManager.del_info(posting_loaded_status.class, term);
		}
		
		// TODO: for testing
		System.out.println("-- deactivation --");
		System.out.println("deactivated: " + deactivatedTerms.toString());
		
		return affectedUnitIds;
	}
	
	
	public class deactivator_thread extends Thread {
		public void run() {
			try {
				while(true) {
					pauseLock.lock();
					
					System.out.println("-- monitoring --");
					idxIOOp.persist_index();	// persist before deactivating, otherwise the newly added units will be lost
					deactivate();    // TODO: testing
					
					pauseLock.unlock();
					Thread.sleep(deactivator_config.monitoringInterval);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	/* pause the deactivator, so that engine could be stoped safely */
	// make use of the pause lock to block the deactivator thread
	public callback resumeCallback = null;
	
	
	public class resume_callback implements callback{
		public double conduct() {
			double resumed = 0;
			try {
				pauseLock.unlock();
				resumed = 1;
				
			}catch(Exception e) {
				e.printStackTrace();
			}
			return resumed;
		}
	}
	
	// strong pause
	public void pause_deactivator() {
		if(resumeCallback == null) {
			pauseLock.lock();
			resumeCallback = new resume_callback();
			System.out.println("---deactivator is paused---");
			
		}else {
			System.out.println("---deactivator is already paused---");
		}

	}
	
	
	public void resume_deactivator() {
		if(resumeCallback != null) {
			resumeCallback.conduct();
			resumeCallback = null;
			System.out.println("---deactivator is resumed---");
			
		}else {
			System.out.println("---deactivator is not paused yet---");
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void start_monitoring() throws Exception{
		deactivator_thread dt = new deactivator_thread();
		dt.setDaemon(true);	// so that will exit after the main exit
		dt.start();
	}
}
