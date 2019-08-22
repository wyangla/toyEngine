package entities.information_manager_plugins;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import configs.information_manager_config;
import data_structures.posting_unit;
import data_structures.term;
import inverted_index.index;



public class posting_loaded_status {
	public static ConcurrentHashMap<String, Double> infoMap = null;    // new ConcurrentHashMap<String, Double>();
	public static String persistingPath = "";    // information_manager_config.persistingDir + "/posting_loaded_status";
	public static index idx = index.get_instance();

	// depricate the infoMap
	// status == -1, means not loaded
	public static int set_info(posting_unit pUnit) {
		int addedFlag = -1;
		try {
			idx.lexicon_2.get(pUnit.term).status = (double)System.currentTimeMillis();
			addedFlag = 1;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return addedFlag;
	}
	
	public static Double get_info(String targetName) {
		return idx.lexicon_2.get(targetName).status;
	}
	
	
	public static int del_info(String targetName) {
		idx.lexicon_2.get(targetName).status = -1;
		return 1;
	}
	
	public static int clear_info() {
		for (String term: idx.lexicon_2.keySet()) {
			term termIns = idx.lexicon_2.get(term);
			termIns.status = -1;
		}
		return 1;
	}
	
	
	
	public static int load_info() {
		return information_common_methods.load_info(persistingPath, infoMap);
	}
	
	public static int persist_info() {
		return information_common_methods.persist_info(persistingPath, infoMap);
	}
}
