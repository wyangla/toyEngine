package entities;
import java.io.BufferedReader;
import java.io.FileReader;

import data_structures.*;
import java.lang.reflect.*;



// where to access the information layer beyond the inverted_index
// the unified information API within the project
// all the new information come from the posting_unit
// separate the advanced information from the basic data structure

public class information_manager {
	
	// singleton, lazy instantiating 
	private static information_manager infoManager = null;
	private information_manager() {};
	public static information_manager get_instance() {
		if(infoManager == null) {
			infoManager = new information_manager();
		}
		return infoManager;
	}
	
	
	// targetName could be the term when trying to get the max score on a term
	public Double get_info(Class targetClass, String targetName) {
		Double info = null;
		try {
			Method getInfoMethod = targetClass.getMethod("get_info", String.class);
			info = (Double)getInfoMethod.invoke(null, targetName);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return info;	
	}
	
	
	// all the new information come from the posting_unit
	public int set_info(Class targetClass, posting_unit pUnit) {
		int addedFlag = -1;
		try {
			Method setInfoMethod = targetClass.getMethod("set_info", posting_unit.class);
			addedFlag = (Integer)setInfoMethod.invoke(null, pUnit);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return addedFlag;
	}
	
	
	public int del_info(Class targetClass, String targetName) {
		int deletedFlag = -1;
		try {
			Method delInfoMethod = targetClass.getMethod("del_info",String.class);
			deletedFlag = (Integer)delInfoMethod.invoke(null, targetName);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return deletedFlag;
	}  
	
	public int clear_info(Class targetClass) {
		int clearedFlag = -1;
		try {
			Method delInfoMethod = targetClass.getMethod("clear_info");
			clearedFlag = (Integer)delInfoMethod.invoke(null);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return clearedFlag;
	}  
	
	public int load_info(Class targetClass) {
		int loadedFlag = -1;
		try {
			Method loadInfoFlag = targetClass.getMethod("load_info");
			loadedFlag = (Integer)loadInfoFlag.invoke(null);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return loadedFlag;
	}
	
	public int persist_info(Class targetClass) {
		int persistedFlag = -1;
		try {
			Method persistInfoFlag = targetClass.getMethod("persist_info");
			persistedFlag = (Integer)persistInfoFlag.invoke(null);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return persistedFlag;
	}
	
}
