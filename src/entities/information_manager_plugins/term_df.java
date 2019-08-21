package entities.information_manager_plugins;

import data_structures.*;
import inverted_index.index;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import configs.information_manager_config;



public class term_df{
	public static ConcurrentHashMap<String, Double> infoMap = null;    // new ConcurrentHashMap<String, Double>();
	public static String persistingPath = "";    // information_manager_config.persistingDir + "/term_df";
	public static index idx = index.get_instance();
	
	
	public static int set_info(posting_unit pUnit) {
		int addedFlag = -1;
		try {
			term termIns = idx.lexicon_2.get(pUnit.term);
			Double origDf = termIns.termProp.get("df");
			Double curDf = 1.0;
			
			if(origDf != null) {
				termIns.termProp.put("df", origDf + curDf);
			}else {
				termIns.termProp.put("df", curDf);
			}
			
			addedFlag = 1;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return addedFlag;
	}
	
	
	// modified
	public static Double get_info(String targetName) {
		return idx.lexicon_2.get(targetName).termProp.get("df");
	}
	
	public static int clear_info() {
		for (String term: idx.lexicon_2.keySet()) {
			term termIns = idx.lexicon_2.get(term);
			termIns.termProp.remove("df");
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
