package entities.information_manager_plugins;

import data_structures.posting_unit;
import inverted_index.index;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import configs.information_manager_config;



public class term_df{
	public static ConcurrentHashMap<String, Double> infoMap = new ConcurrentHashMap<String, Double>();
	public static String persistingPath = information_manager_config.persistingDir + "/term_df";
	public static index idx = index.get_instance();
	
	
	public static int set_info(posting_unit pUnit) {
		int addedFlag = -1;
		try {
			Double origDf = infoMap.get(pUnit.term);
			Double curDf = 1.0;
			
			// TODO: extract the origDf curDf from lexicon instead
			
			if(origDf != null) {
				infoMap.put(pUnit.term, origDf + curDf);
				idx.lexicon_2.get(pUnit.term).termProp.put("df", origDf + curDf);
			}else {
				infoMap.put(pUnit.term, curDf);
				idx.lexicon_2.get(pUnit.term).termProp.put("df", curDf);
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
