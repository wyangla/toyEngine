package entities.scanner_plugins;

import data_structures.*;
import java.io.*;
import java.util.concurrent.*;



// shared by multiple terms
public class persist_postings implements scanner_plugin_interface{
	
	private ConcurrentHashMap<String, File> dirMap = new ConcurrentHashMap<String, File>();
	private ConcurrentHashMap<String, FileWriter> fileMap = new ConcurrentHashMap<String, FileWriter>();

	public void set_parameters(Object placeholder) {		
	}

	public long conduct(posting_unit pUnit) {
		long relatedUnitId = -1L;
		
		// check the directory of one term is existing or not
		File postingDir = dirMap.get(pUnit.term);
		if(postingDir == null) {
			postingDir = new File(configs.index_config.postingsPersistancePath + '/' + pUnit.term);
			dirMap.put(pUnit.term, postingDir);
		}
		if(!postingDir.exists()) {
			postingDir.mkdirs();
		}
		
		
		FileWriter pf = fileMap.get(pUnit.term);
		try {
			if(pf == null) {
				pf = new FileWriter(postingDir.getPath() + "/posting");
				fileMap.put(pUnit.term, pf);
			}
			
			String pUnitString = pUnit.flatten();
			pf.write(pUnitString + "\r\n");
			relatedUnitId = pUnit.currentId;
			
			// if reach the end of the posting list, close the file
			if(pUnit.nextId == -1) {
				pf.flush();
				pf.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("--> error pUnitId: " + pUnit.currentId);
			
			// if the writing process is broken, close the file
			try {
				pf.flush();
				pf.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		return relatedUnitId;
	}
	
	
}