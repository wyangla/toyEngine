package entities.information_manager_plugins;

import data_structures.posting_unit;
import java.util.*;
import configs.information_manager_config;



public class term_max_tf{
	public static HashMap<String, Double> infoMap = new HashMap<String, Double>();
	public static String persistingPath = information_manager_config.persistingDir + "/term_max_tf";
	
	
	
	public static int set_info(posting_unit pUnit) {
		int addedFlag = -1;
		try {
			Double tf = infoMap.get(pUnit.term);
			if(tf != null) {
				if(pUnit.uProp.get("tf") > tf) {	// only update when the new tf is larger than the original one
					infoMap.put(pUnit.term, pUnit.uProp.get("tf"));
				}
			}else {
				infoMap.put(pUnit.term, pUnit.uProp.get("tf"));
			}

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