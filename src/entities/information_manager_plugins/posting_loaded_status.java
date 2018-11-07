package entities.information_manager_plugins;

import java.util.HashMap;

import configs.information_manager_config;
import data_structures.posting_unit;

public class posting_loaded_status {
	public static HashMap<String, Double> infoMap = new HashMap<String, Double>();
	public static String persistingPath = information_manager_config.persistingDir + "/posting_loaded_status";
	
	
	
	public static int set_info(posting_unit pUnit) {
		int addedFlag = -1;
		try {
			infoMap.put(pUnit.term, 1.0);	// 1 loaded, 0 not loaded
			addedFlag = 1;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return addedFlag;
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
