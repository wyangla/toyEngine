package data_structures;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;


public class term{
	public long termId = -1L;
	public String termName = " ";
	public ConcurrentHashMap<String, Double> termProp = new ConcurrentHashMap<String, Double>();    // termIdf, termDf, termMaxtf, termUpperBound, postingLoadedStatus; - termIdfCalTime, as all the same
	
	public long firstPostUnitId = -1;    // point to the posting unit of first term in the document
	public long lastPostUnitId = -1;    // point to the last term unit
	
	
	public String flatten() {
		JSONObject termPropJson = new JSONObject(termProp);
		return String.format("%s %s %s %s %s", termId, termName, firstPostUnitId, lastPostUnitId, termPropJson);
	}
	
	
	public static term deflatten(String persistedTerm) {
		term termIns = new term();
		String[] termFields = persistedTerm.split(" ");
		
		termIns.termId = Long.parseLong(termFields[0]);
		termIns.termName = termFields[1];
		termIns.firstPostUnitId = Integer.parseInt(termFields[2]);
		termIns.lastPostUnitId = Integer.parseInt(termFields[3]);
		
		JSONObject termPropJson = new JSONObject(termFields[4]);
		Map<String, Object> termProp = termPropJson.toMap(); 
		for(String p : termProp.keySet()) {
			termIns.termProp.put(p, Double.parseDouble("" + termProp.get(p)));
		}
		
		return termIns;
	}
	
	
}