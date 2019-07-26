package entities.information_manager_plugins;

import data_structures.posting_unit;
import entities.*;
import inverted_index.*;
import utils.*;

import java.util.*;

import configs.general_config;
import configs.information_manager_config;



// each time the input documents are scanned, this info map is set once
public class term_idf_cal_time{
	public static HashMap<String, Double> infoMap = new HashMap<String, Double>();
	public static String persistingPath = information_manager_config.persistingDir + "/term_idf_cal_time";
	
	
	
	public static int set_info() {
		int calDoneFlag = -1;
		infoMap.put("term_idf_cal_time", (double)System.currentTimeMillis());    // for consistence
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
