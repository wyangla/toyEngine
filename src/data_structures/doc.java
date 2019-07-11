package data_structures;
import java.util.*;

import org.json.JSONObject;


// no specific references to the post units, as the post unit ids are not fixed, reload operation could change them all
public class doc {
	public String docId = " ";
	public int docLength = 0;
	public HashMap<String, Double> docProp = new HashMap<String, Double>();
	
	// public ArrayList<Long> pUnitIdList = new ArrayList<Long> ();   
		// will be dynamically refilled when reload the postings
		// depreciated, it will consume too much memory
	
	
	public String flatten() {
		JSONObject docPropJson = new JSONObject(docProp);
		return String.format("%s %s %s", docId, "" + docLength, docPropJson);
	}
	
	
	public static doc deflatten(String persistedDoc) {
		doc docIns = new doc();
		String[] docFields = persistedDoc.split(" ");
		
		docIns.docId = docFields[0];
		docIns.docLength = Integer.parseInt(docFields[1]);
		
		JSONObject docPropJson = new JSONObject(docFields[2]);
		Map<String, Object> docProp = docPropJson.toMap(); 
		for(String p : docProp.keySet()) {
			docIns.docProp.put(p, Double.parseDouble("" + docProp.get(p)));
		}
		
		return docIns;
	}
}
