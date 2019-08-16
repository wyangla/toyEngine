package data_structures;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;


// no specific references to the post units, as the post unit ids are not fixed, reload operation could change them all
public class doc {
	public long docId = -1L;
	public String docName = " ";
	public int docLength = 0;
	public ConcurrentHashMap<String, Double> docProp = new ConcurrentHashMap<String, Double>();
	
	// public ArrayList<Long> pUnitIdList = new ArrayList<Long> ();   
		// will be dynamically refilled when reload the postings
		// depreciated, it will consume too much memory
	
	public long firstTermUnitId = -1;    // point to the posting unit of first term in the document
	public long lastTermUnitId = -1;    // point to the last term unit
	
	
	public String flatten() {
		JSONObject docPropJson = new JSONObject(docProp);
		return String.format("%s %s %s %s %s %s", docId, docName, "" + docLength, firstTermUnitId, lastTermUnitId, docPropJson);
	}
	
	
	public static doc deflatten(String persistedDoc) {
		doc docIns = new doc();
		String[] docFields = persistedDoc.split(" ");
		
		docIns.docId = Long.parseLong(docFields[0]);
		docIns.docName = docFields[1];
		docIns.docLength = Integer.parseInt(docFields[2]);
		docIns.firstTermUnitId = Integer.parseInt(docFields[3]);
		docIns.lastTermUnitId = Integer.parseInt(docFields[4]);
		
		JSONObject docPropJson = new JSONObject(docFields[5]);
		Map<String, Object> docProp = docPropJson.toMap(); 
		for(String p : docProp.keySet()) {
			docIns.docProp.put(p, Double.parseDouble("" + docProp.get(p)));
		}
		
		return docIns;
	}
}
