package experiment_ground;
import java.util.*;
import java.util.Collections;


public class try_HashMap {
	public static HashMap<String, Double> a = new HashMap<String, Double>();
	public static HashMap<String, Double> b = new HashMap<String, Double>();
	
	public static void main(String[] args) {
		a.put("a", 1.);
		a.put("b", 2.);
		b.put("a", 2.);
		
		System.out.println(a);
		
		// copy a
		HashMap<String, Double> updatedCounter = new HashMap<String, Double>();
		updatedCounter.putAll(a);
		
		// copy the a.keySet()
		ArrayList<String> aKeys = new ArrayList<String>();
		aKeys.addAll(a.keySet());
		HashSet<String> aKeySet = new HashSet<String>();
		aKeySet.addAll(aKeys);
		
		// intersection
		aKeySet.retainAll(b.keySet()); 
		System.out.println(aKeySet);
		System.out.println(a);
		
		// add the original value into the new values
		updatedCounter.putAll(b);
		for(String sharedKey : aKeySet) {
			updatedCounter.put(sharedKey, updatedCounter.get(sharedKey) + a.get(sharedKey));
		}
		
		System.out.println(updatedCounter);
		
		a.putAll(b);
		System.out.println(a);
	}
	
}
