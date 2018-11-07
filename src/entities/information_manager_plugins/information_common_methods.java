package entities.information_manager_plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import utils.file_creater;

public class information_common_methods {
	
	public static Double get_info(String targetName, HashMap<String, Double> infoMap) {
		Double info = infoMap.get(targetName);
		return info;	
	}
	
	
	
	public static int del_info(String targetName, HashMap<String, Double> infoMap) {
		int deletedFlag = -1;
		try {
			infoMap.remove(targetName);
			deletedFlag = 1;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return deletedFlag;
	}
	
	
	
	public static int clear_info(HashMap<String, Double> infoMap) {
		int clearedFlag = -1;
		try {
			String[] targetNameList = infoMap.keySet().toArray(new String[0]);
			for(String targetName : targetNameList) {
				del_info(targetName, infoMap);
			}
			clearedFlag = 1;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return clearedFlag;
	}
	
	
	
	public static int load_info(String persistingPath, HashMap<String, Double> infoMap) {
		int loadedFlag = -1;
		Double information;
		try {
			
			FileReader infof = new FileReader(persistingPath);
			BufferedReader infob = new BufferedReader(infof);
			try {
				String infoString;
				do {
					infoString = infob.readLine();
					if (infoString != null) {
						String[] infoList = infoString.trim().split(" ");
						String targetName = infoList[0];
						if(!infoList[1].matches("null")) {
							information = Double.parseDouble(infoList[1]);
						}else {
							information = null;
						}
						infoMap.put(targetName, information);
					}
				} while(infoString != null);
				loadedFlag = 1;
				
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				infof.close();
				infob.close();
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			if(e.getClass().equals(java.io.FileNotFoundException.class)) {
				file_creater.create_file(persistingPath);
			};
		}
		return loadedFlag;
	}
	
	
	
	public static int persist_info(String persistingPath, HashMap<String, Double> infoMap) {
		int persistedFlag = -1;
		
		File infoFile = new File(persistingPath);
		if(!infoFile.exists()) {
			file_creater.create_file(persistingPath);
		}
		
		try {
			FileWriter infof = new FileWriter(persistingPath);
			try {
				for(String targetName : infoMap.keySet()) {
					Double info = infoMap.get(targetName);
					infof.write(targetName + " " + info + "\n");
				}
				persistedFlag = 1;
				
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				infof.flush();
				infof.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}

		return persistedFlag;
	}
}
