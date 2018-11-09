package utils;
import java.util.*;


// refer to the collections.Counter in Python 
// for merging the searching results
public class counter extends HashMap<String, Double> {
	
	public counter update(counter anotherCounter) {
		// copy current Counter
		counter updatedCounter = new counter();
		updatedCounter.putAll(this);
		
		// copy the this.keySet()
		ArrayList<String> aKeys = new ArrayList<String>();
		aKeys.addAll(this.keySet());
		HashSet<String> aKeySet = new HashSet<String>();
		aKeySet.addAll(aKeys);
		
		// intersection
		aKeySet.retainAll(anotherCounter.keySet()); 
		
		// add the original value into the new values
		updatedCounter.putAll(anotherCounter);
		for(String sharedKey : aKeySet) {
			updatedCounter.put(sharedKey, updatedCounter.get(sharedKey) + this.get(sharedKey));
		}
		
		return updatedCounter;
	}
	
	public void increase(String key, Double value) {
		if(this.get(key) == null) {
			this.put(key, value);
		} else {
			this.put(key, this.get(key) + value);
		}
	}
	
	// ref: http://www.cnblogs.com/unclecc/p/9400939.html
	public String get_min_key() {
		String minKey = null;
		ArrayList<Map.Entry<String, Double>> entryList = new ArrayList<Map.Entry<String, Double>>();
		entryList.addAll(this.entrySet());
		entryList.sort((e1, e2) -> e1.getValue().compareTo(e2.getValue()));
		minKey = entryList.get(0).getKey();
		return minKey;
	}
}
