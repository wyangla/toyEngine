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
}
