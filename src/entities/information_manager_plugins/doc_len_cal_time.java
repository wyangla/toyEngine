package entities.information_manager_plugins;

import data_structures.*;
import entities.*;
import inverted_index.*;
import utils.*;

import java.util.*;

import configs.general_config;
import configs.information_manager_config;



// recording the document length calculation time, for comparing with the term IDF calculation time, 
// in order to determine whether to re-calculate the doc length 
// TODO: save it to local as a separate file or not?
public class doc_len_cal_time{
	// directly set the calculation time information into the docIns.prop
	// the doc length information should be the sameee
	
	public static HashMap<String, Double> infoMap = new HashMap<String, Double>();    // not being used here
	public static String persistingPath = information_manager_config.persistingDir + "/not_really_been_stored";
	
	private static index idx = index.get_instance();
	
	
	public static int set_info(long docId) {
		int calDoneFlag = -1;
		doc docIns = idx.docIdMap.get(docId);
		docIns.docProp.put("doc_len_cal_time", (double)System.currentTimeMillis());
		return calDoneFlag;
	}
	
	
	
	// the following are fixed
	public static Double get_info(String targetName) {
		return information_common_methods.get_info(targetName, infoMap);
	}
	
	public static int del_info(String targetName) {
		return information_common_methods.del_info(targetName, infoMap);
	}
	
	public static int clear_info() {
		return information_common_methods.clear_info(infoMap);
	}
	
	public static int load_info() {
		return information_common_methods.load_info(persistingPath, infoMap);
	}
	
	public static int persist_info() {
		return information_common_methods.persist_info(persistingPath, infoMap);
	}

}
