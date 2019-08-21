package entities.information_manager_plugins;

import data_structures.*;
import inverted_index.index;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import configs.information_manager_config;



public class term_max_tf{
	public static ConcurrentHashMap<String, Double> infoMap = null;    //new ConcurrentHashMap<String, Double>();
	public static String persistingPath = "";    // information_manager_config.persistingDir + "/term_max_tf";
	public static index idx = index.get_instance();
	
	
	public static int set_info(posting_unit pUnit) {
		int addedFlag = -1;
		try {
			term termIns = idx.lexicon_2.get(pUnit.term);
			Double origTf = termIns.termProp.get("mtf");
			Double curTf = pUnit.uProp.get("mtf");
			
			if(origTf != null) {
				if(curTf != null && curTf > origTf) {	// only update when the new tf is larger than the original one
					termIns.termProp.put("mtf", curTf);
				}
			}else {
				termIns.termProp.put("mtf", curTf);
			}

			addedFlag = 1;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return addedFlag;
	}
	
	
	// modified
	public static Double get_info(String targetName) {
		return idx.lexicon_2.get(targetName).termProp.get("mtf");
	}
	
	public static int clear_info() {
		for (String term: idx.lexicon_2.keySet()) {
			term termIns = idx.lexicon_2.get(term);
			termIns.termProp.remove("mtf");
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
