package entities.information_manager_plugins;

import data_structures.*;
import entities.*;
import inverted_index.*;
import utils.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import configs.general_config;
import configs.information_manager_config;



// each time the input documents are scanned, this info map is set once
public class term_idf{
	public static ConcurrentHashMap<String, Double> infoMap = null; // new ConcurrentHashMap<String, Double>();
	public static String persistingPath = ""; // information_manager_config.persistingDir + "/term_idf";
	public static index idx = index.get_instance();
	
	public static class idf_cal_thread extends Thread{
		private String[] tTerms;
		
		public idf_cal_thread(String[] targetTerms) {
			tTerms = targetTerms;
		}
		
		public void run() {
			for (String targetTerm : tTerms) {
				double df = term_df.get_info(targetTerm);
				double totalDocNum = (double) index.get_instance().docMap.size();
				double idf = Math.log(totalDocNum / df); 
				// infoMap.put(targetTerm, idf);
				idx.lexicon_2.get(targetTerm).termProp.put("idf", idf);
			}
			
		}
	}
	
	
	public static int set_info(posting_unit fakePostUnit) {
		int calDoneFlag = -1;
		try {
			
			// String[] targetTerms = term_df.infoMap.keySet().toArray(new String[0]);
			String[] targetTerms = idx.lexicon_2.keySet().toArray(new String[0]);
			ArrayList<String[]> workLoads = task_spliter.get_workLoads_terms(general_config.cpuNum, targetTerms);
			
			ArrayList<idf_cal_thread> threadList = new ArrayList<idf_cal_thread>();
			
			for(String[] workload : workLoads ) {
				idf_cal_thread it = new idf_cal_thread(workload);
				it.run();
				threadList.add(it);
			}
			for(idf_cal_thread it : threadList) {
				it.join();
			}
			
			calDoneFlag = 1;
			
		}catch(Exception e) {
			e.printStackTrace();
			
			calDoneFlag = 0;
		}
		return calDoneFlag;
	}
	
	
	// modified to use the termIns provide the idf
	public static Double get_info(String targetName) {
		return idx.lexicon_2.get(targetName).termProp.get("idf");
	}
	
	public static int clear_info() {
		for (String term: idx.lexicon_2.keySet()) {
			term termIns = idx.lexicon_2.get(term);
			termIns.termProp.remove("idf");
		}
		return 1;
	}
	
	
	public static int del_info(String targetName) {
		return information_common_methods.del_info(targetName, infoMap);
	}
		
	public static int load_info() {
		return information_common_methods.load_info(persistingPath, infoMap);
	}
	
	public static int persist_info() {
		return information_common_methods.persist_info(persistingPath, infoMap);
	}

}
