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
			
			// when current unit is not the starter
			if (prevUnit != null) { 
				prevUnit.link_to_next(nextUnit);	
			}else {
				// if the first unit of posting unit is removed, reset the firstPostUnitId of term
				if(nextUnit != null) {
					term termIns = idx.lexicon_2.get(pUnit.term);
					termIns.firstPostUnitId = nextUnit.currentId;	
				}
			}
			// when current unit is not the ender
			if (nextUnit != null) { 
				nextUnit.link_to_previous(prevUnit);	
			}else {
				// if the last unit of posting unit is removed, reset the lastPostUnitId of term
				if(prevUnit != null) {
					term termIns = idx.lexicon_2.get(pUnit.term);
					termIns.lastPostUnitId = prevUnit.currentId;	
				}
			}
			
			
			/*
			 * relink the term posting chain
			 * */
			posting_unit prevTermUnit = pUnit.prevTermUnit;
			posting_unit nextTermUnit = pUnit.nextTermUnit;

			// when current unit is not the first term unit
			if (prevTermUnit != null) { 
				prevTermUnit.link_to_next_term(nextTermUnit);	
			}else {
				// if first term unit is removed, and there are other term units left
				if(nextTermUnit != null) {
					doc docIns = idx.docIdMap.get(pUnit.docId);
					if(docIns != null) {    // if the doc is not deleted yet, currently the cleaner is always used to clean up the deleted docs, so that docIns is always null
						docIns.firstTermUnitId = nextTermUnit.currentId;	
					}	
				}
			}
			
			// when current unit is not the last term unit
			if (nextTermUnit != null) { 
				nextTermUnit.link_to_previous_term(prevTermUnit);	
			}else {
				// if the last term unit is removed, and there are still preceding term units
				if(prevTermUnit != null) {
					doc docIns = idx.docIdMap.get(pUnit.docId);
					if(docIns != null) {
						docIns.lastTermUnitId = prevTermUnit.currentId;	
					}
				}
			}
			
			elPUnitIdList.put(pUnit.currentId, 1.0);
		}
		
		return affectedUnitId;
	}
	
}