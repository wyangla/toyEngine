package inverted_index;
import java.util.*;
import java.lang.reflect.*;
import org.json.*;


// the first unit of each posting list is empty, just for holding the place and acting as the starter of the link list
public class posting_unit {
	
	public long currentId = -1L; // unique identification of current link unit, set directly from outside
	
	public long nextId = -1L; // unique identification of next link unit
	public long previousId = -1L; // unique identification of previous link unit
	
	public posting_unit nextUnit = null; // link to the next unit, if == null means is the last one
	public posting_unit prevUnit = null; // link to the prvious unit, if == null means is the earliest one
	
	public HashMap<String, Double> uProp = new HashMap<String, Double>(); // for storing properties like tfidf, etc.
	
	public int status = 1; // 1 linked, 0 disconnected
	
	
	
	// link to the previous posting list unit
	public long link_to_previous(posting_unit previous_unit_ins) {
		prevUnit = previous_unit_ins;
		if (previous_unit_ins != null) {
			previousId = prevUnit.currentId;
		}else { // when links to nothing previous, means the current is the starter
			previousId = -1;
		}
		return previousId;
	}
	
	
	// link to the following posting list unit
	public long link_to_next(posting_unit next_unit_ins) {
		nextUnit = next_unit_ins;
		if(next_unit_ins != null) {
			nextId = nextUnit.currentId;
		}else {  // when links to nothing following, means the current is the ender
			nextId = -1;
		}
		return nextId;
	}
	
	
	
	// serialisation of the unit
	// does not care about the linking, which is handled within the index
	// format: [term] currentId nextId previousId {uProp}
	// [term] is added in index
	public String flatten() {
		JSONObject uPropJson = new JSONObject(uProp);
		return String.format("%s %s %s %s %s", currentId, nextId, previousId, uPropJson, status);
	}
	
	
	// load the serialisation into unit object
	// the linking is handled by index
	public static posting_unit deflatten(String persistedUnit) {
		posting_unit pUnit = new posting_unit();
		String[] pUnitFields = persistedUnit.split(" ");
		
		pUnit.currentId = Long.parseLong(pUnitFields[0]);
		pUnit.nextId = Long.parseLong(pUnitFields[1]);
		pUnit.previousId = Long.parseLong(pUnitFields[2]);
		pUnit.status = Integer.parseInt(pUnitFields[4]);
		
		JSONObject uPropJson = new JSONObject(pUnitFields[3]);
		Map<String, Object> uProp = uPropJson.toMap(); 
		for(String p : uProp.keySet()) {
			pUnit.uProp.put(p, (Double)uProp.get(p));
		}
		
		return pUnit;
	}
	
	
	
	// testing
	public static void main(String[] args) {
		posting_unit _self = new posting_unit();
		_self.uProp.put("testKey", 0.01);
		System.out.println("" + _self.uProp.entrySet());
		System.out.println("" + _self.link_to_next(null));
		
		// test flatten
		System.out.println(_self.flatten());
		
		// test deflatten 
		_self.deflatten(_self.flatten());
		System.out.print(_self.uProp.get("testKey").getClass());
	}
}
