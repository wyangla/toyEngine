package entities.scanner_plugins;

import data_structures.*;
import inverted_index.index;
import java.util.*;
import java.util.concurrent.*;

import utils.*;



public class clean_posting implements scanner_plugin_interface{
	private index idx = index.get_instance();
	private ConcurrentHashMap<Long, Double> elPUnitIdList = null;
	
	public void set_parameters(Object eliminatePostUnitIdList) {	
		elPUnitIdList = (ConcurrentHashMap<Long, Double>) eliminatePostUnitIdList;
	}

	public long conduct(posting_unit pUnit) {
		long affectedUnitId = -1L; 
		
		if(pUnit.status == 0) {
			
			/*
			 * relink the posting chain
			 * */
			posting_unit prevUnit = pUnit.prevUnit;
			posting_unit nextUnit = pUnit.nextUnit;
			term termIns = idx.lexicon_2.get(pUnit.term);
			
			
			if(prevUnit != null && nextUnit != null) {
				prevUnit.link_to_next(nextUnit);
				nextUnit.link_to_previous(prevUnit);
			}
			if(prevUnit != null && nextUnit == null) {
				prevUnit.link_to_next(nextUnit);
				termIns.lastPostUnitId = prevUnit.currentId;
			}
			if(prevUnit == null && nextUnit == null) {
				termIns.firstPostUnitId = -1;
				termIns.lastPostUnitId = -1;
			}
			if(prevUnit == null && nextUnit != null) {
				nextUnit.link_to_previous(prevUnit);
				termIns.firstPostUnitId = nextUnit.currentId;
			}
			
			
			/*
			 * relink the term posting chain
			 * not used here, as the way to use cleaner now, is use to eliminate the posting units of delete document, thus the whole term chain will be removed
			 * */
//			doc docIns = idx.docIdMap.get(pUnit.docId);
//			synchronized(docIns) {    // need synchronization here, as the one posting for sure in one thread, but one term chain is not
//				posting_unit prevTermUnit = pUnit.prevTermUnit;
//				posting_unit nextTermUnit = pUnit.nextTermUnit;
//
//				if(docIns != null) {
//					if(prevTermUnit != null && nextTermUnit != null) {
//						prevTermUnit.link_to_next(nextTermUnit);
//						nextTermUnit.link_to_previous(prevTermUnit);
//					}
//					if(prevTermUnit != null && nextTermUnit == null) {
//						prevTermUnit.link_to_next(nextTermUnit);
//						docIns.lastTermUnitId = prevTermUnit.currentId;
//					}
//					if(prevTermUnit == null && nextTermUnit == null) {
//						docIns.firstTermUnitId = -1;
//						docIns.lastTermUnitId = -1;
//					}
//					if(prevTermUnit == null && nextTermUnit != null) {
//						nextTermUnit.link_to_previous(prevTermUnit);
//						docIns.firstTermUnitId = nextTermUnit.currentId;
//					}	
//				}	
//			}			
			
			elPUnitIdList.put(pUnit.currentId, 1.0);
		}
		
		return affectedUnitId;
	}
	
}