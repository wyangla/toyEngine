package data_structures;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.*;
import org.json.*;


// the first unit of each posting list is empty, just for holding the place and acting as the starter of the link list
public class posting_unit {
	
	public long currentId = -1L; // unique identification of current link unit, set directly from outside
	
	public long nextId = -1L; // unique identification of next link unit
	public long previousId = -1L; // unique identification of previous link unit
	
	public posting_unit nextUnit = null; // link to the next unit, if == null means is the last one
	public posting_unit prevUnit = null; // link to the prvious unit, if == null means is the earliest one
	
	public ConcurrentHashMap<String, Double> uProp = new ConcurrentHashMap<String, Double>(); // for storing properties like tfidf, etc.
	
	public int status = 1; // 1 linked, 0 disconnected, TODO: -1 suspended
	public long docId = -1L; // the unique id of doc in the file system, not using path here
	public String term = "--";
	
	
	// TODO: need to be flattened
	public long nextTermId = -1L;
	public long previousTermId = -1L;
	
	public posting_unit nextTermUnit = null;    // link to the next term in doc
	public posting_unit prevTermUnit = null;    // link to the prvious term in doc
	
	public ArrayList<Integer> positions = null;     // this information needs to be generated from the operator, with this information, the units does not need to be linked in order
	
	
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
	
	
	// link to the previous posting list unit
	public long link_to_previous_term(posting_unit previous_term_ins) {
		prevTermUnit = previous_term_ins;
		if (previous_term_ins != null) {
			previousTermId = prevTermUnit.currentId;
		}else { // when links to nothing previous, means the current is the starter
			previousTermId = -1;
		}
		return previousTermId;
	}
	
	// link to the following posting list unit
	public long link_to_next_term(posting_unit next_term_ins) {
		nextTermUnit = next_term_ins;
		if(next_term_ins != null) {
			nextTermId = nextTermUnit.currentId;
		}else {  // when links to nothing following, means the current is the ender
			nextTermId = -1;
		}
		return nextTermId;
	}

	
	
	// serialisation of the unit
	// does not care about the linking, which is handled within the index
	// format: [term] currentId nextId previousId {uProp} docId status
	// [term] is added in index
	public String flatten() {
		JSONObject uPropJson = new JSONObject(uProp);
		return String.format("%s %s %s %s %s %s %s %s %s", term, currentId, nextId, previousId, nextTermId, previousTermId, uPropJson, docId, status);
	}
	
	
	// load the serialisation into unit object
	// the linking is handled by index
	// no term in persistedUnitNoTerm, this is because the posting unit it self does not care about the term
	public static posting_unit deflatten(String persistedUnitNoTerm) {
		posting_unit pUnit = new posting_unit();
		String[] pUnitFields = persistedUnitNoTerm.split(" ");
		
		pUnit.term = pUnitFields[0];
		pUnit.currentId = Long.parseLong(pUnitFields[1]);
		pUnit.nextId = Long.parseLong(pUnitFields[2]);
		pUnit.previousId = Long.parseLong(pUnitFields[3]);
		pUnit.nextTermId = Long.parseLong(pUnitFields[4]);
		pUnit.previousTermId = Long.parseLong(pUnitFields[5]);
		pUnit.docId = Long.parseLong(pUnitFields[7]);
		pUnit.status = Integer.parseInt(pUnitFields[8]);
		
		JSONObject uPropJson = new JSONObject(pUnitFields[6]);
		Map<String, Object> uProp = uPropJson.toMap(); 
		for(String p : uProp.keySet()) {
			pUnit.uProp.put(p, Double.parseDouble("" + uProp.get(p)));
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
