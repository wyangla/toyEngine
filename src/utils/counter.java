package utils;
import java.util.*;


// refer to the collections.Counter in Python 
// for merging the searching results
public class counter extends LinkedHashMap<String, Double> {
	
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
	
	public ArrayList<Map.Entry<String, Double>> sort(){
		ArrayList<Map.Entry<String, Double>> entryList = new ArrayList<Map.Entry<String, Double>>();
		entryList.addAll(this.entrySet());
		entryList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue())); // big to small
		
		// reload the counter
		this.clear();
		entryList.forEach(e -> this.put(e.getKey(), e.getValue()));
		
		return entryList;
	}
	
	
	// ref: http://www.cnblogs.com/unclecc/p/9400939.html
	public String get_min_key() {
		String minKey = null;				
		minKey = this.sort().get(this.size() - 1).getKey();
		return minKey;
	}
	
	
	// topK : index = K - 1
	public String get_topKth_key(int topKth) {
		String topKthKey = null;		
		topKthKey = this.sort().get(topKth - 1).getKey();
		return topKthKey;
	}
	
	
	public String get_max_key() {
		return get_topKth_key(1);
	}
	
	
	// topK : index = K - 1
	public ArrayList<String> get_topK_keys(int topK) {
		ArrayList<String> topKKeys = new ArrayList<String>();	
		ArrayList<Map.Entry<String, Double>> entryList = this.sort();
		
		for(int i = 0; i < topK; i ++) {
			topKKeys.add(entryList.get(i).getKey());
		}
		
		return topKKeys;
	}
	
	
	public void remove_after_topK(int K) {
		ArrayList<Map.Entry<String, Double>> entryList = this.sort();
		if(K < entryList.size()) {
			entryList.subList(K, entryList.size()).forEach(e -> this.remove(e.getKey(), e.getValue()));
		}
	}
}
