package probes;

import java.util.Arrays;
import java.util.HashMap;
import inverted_index.*;


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
			System.out.println("lexiconLockMap: " + kpr.lexiconLockMap.entrySet());
			System.out.println("");
			
			infoMap.put("postUnitMap: ", idx.postUnitMap.entrySet().toString());
			infoMap.put("lexicon: ", idx.lexicon.entrySet().toString());
			infoMap.put("lexiconLockMap: ", kpr.lexiconLockMap.entrySet().toString());
			
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
		return infoMap;	
	}

}
