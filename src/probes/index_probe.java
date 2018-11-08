package probes;

import java.util.HashMap;

import entities.keeper;
import inverted_index.*;
import entities.keeper_plugins.*;
import entities.information_manager_plugins.*;



public class index_probe {
	index idx = index.get_instance();
	keeper kpr = keeper.get_instance();
	
	// display whole lexicon, postUnitMap, lexiconLockMap
	public HashMap<String, String> display_content(String areYouSureAboutPrintTheWholeIndex) {
		HashMap<String, String> infoMap = new HashMap<String, String>();;
		// TODO: for test
		System.out.println(areYouSureAboutPrintTheWholeIndex);
		if(areYouSureAboutPrintTheWholeIndex.matches("Sure")) {
			System.out.println("postUnitMap: " + idx.postUnitMap.entrySet()); // only display 10 items for illustration
			System.out.println("lexicon: " + idx.lexicon.entrySet());
			System.out.println("lexiconLockInfoMap: " + kpr.get_lockInfoMap(lexicon_locker.class).entrySet());
			System.out.println("lexiconLockMap: " + kpr.get_lockMap(lexicon_locker.class));
			System.out.println("docMap: " + idx.docMap);
			System.out.println("loadedStatus: " + posting_loaded_status.infoMap.keySet());
			System.out.println("maxTf: " + term_max_tf.infoMap.keySet());
			
			System.out.println("");
			
			infoMap.put("postUnitMap: ", idx.postUnitMap.entrySet().toString());
			infoMap.put("lexicon: ", idx.lexicon.entrySet().toString());
			infoMap.put("lexiconLockInfoMap: ", kpr.get_lockInfoMap(lexicon_locker.class).entrySet().toString());
			infoMap.put("lexiconLockMap: ", kpr.get_lockMap(lexicon_locker.class).toString());
			infoMap.put("docMap: ", idx.docMap.entrySet().toString());
			infoMap.put("loadedStatus: ", posting_loaded_status.infoMap.keySet().toString());
			infoMap.put("maxTf: ", "" + term_max_tf.infoMap.keySet().toString());
			
		} else {
			System.out.println("Dont print out the whole inverted index only if you are sure");
			infoMap.put(" ", "Dont print out the whole inverted index only if you are sure");
		}
		return infoMap;	
	}
	
	// show the statistics of the inverted-index
	public HashMap<String, String> show() {
		System.out.println("postUnitMap size: " + idx.postUnitMap.size()); // only display 10 items for illustration
		System.out.println("lexicon size: " + idx.lexicon.size());
		System.out.println("");
		
		HashMap<String, String> infoMap = new HashMap<String, String>();
		infoMap.put("postUnitMap size: ", "" + idx.postUnitMap.size());
		infoMap.put("lexicon size: ", "" + idx.lexicon.size());
		infoMap.put("docMap size: ", "" + idx.docMap.size());
		infoMap.put("loadedStatus size: ", "" + posting_loaded_status.infoMap.size());
		infoMap.put("maxTf size: ", "" + term_max_tf.infoMap.size());
		return infoMap;	
	}

}
