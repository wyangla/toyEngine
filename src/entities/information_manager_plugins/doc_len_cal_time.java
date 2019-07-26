package entities.information_manager_plugins;

import data_structures.posting_unit;
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
	public static HashMap<String, Double> infoMap = new HashMap<String, Double>();
	public static String persistingPath = information_manager_config.persistingDir + "/doc_len_cal_time";
	
	
	
	public static int set_info() {
		int calDoneFlag = -1;
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
