package engine_api;

import py4j.GatewayServer;
import inverted_index.*;
import java.util.*;



// using the py4j to start a GatewayServer for the python to operate the java objects
public class engineEntryPoint {
	
	private index idx = index.get_instance();
	private keeper kpr = keeper.get_instance();
	private cleaner clr = cleaner.getInstance();
	
	
	
	// main objects 
	public index getIndex() {
		return idx;
	}
	
	public keeper getKeeper() {
		return kpr;
	}
	
	public cleaner getCleaner() {
		return clr;
	}
	
	public HashMap<String, ArrayList<Long>> getLexicon() {
		return idx.lexicon;
	}	
	
	public HashMap<Long, posting_unit> getPostUnitMap() {
		return idx.postUnitMap;
	}
	
	public HashMap<String, HashMap<String, Long>> getLexiconLockMap() {
		return kpr.lexiconLockMap;
	}
	
	
	
	// index APIs
	public long add_term(String term) {
		long firstUnitId = idx.add_term(term);
		return firstUnitId;
	}
	
	public void del_term(String term) {
		idx.del_term(term);
	}
	
	// persistedUnit: [term<String>] currentId<Long> nextId<Long> previousId<Long> {uProp}<String, Long> docId<String> status<Integer>
	public long add_posting_unit(String term, String persistedUnit) {
		long addedUnitId = idx.add_posting_unit(term, persistedUnit);
		return addedUnitId;
	}
	
	public long del_posting_unit(long postingUnitId) {
		long delUnitId = idx.del_posting_unit(postingUnitId);
		return delUnitId;
	}
	
	public void persist_index() {
		idx.persist_index();
	}
	
	public long load_posting_unit(String term, posting_unit postUnit) {
		long addedUnitId = idx.load_posting_unit(term, postUnit);
		return addedUnitId;
	}
	
	public long[] load_index(String[] targetTerms) {
		long[] loaded_units = idx.load_index(targetTerms);
		return loaded_units;
	}
	
	public void clear_index() {
		idx.clear_index();
	}
	
	public void reload_index() {
		idx.reload_index();
	}
	
	public HashMap<String, String> display_content() {
		HashMap<String, String> infoMap = idx.display_content();
		return infoMap;
	}
	
	
	
	// cleaner index
	public void clean_index() {
		clr.clean();
	}
	
	
	
	public static void main(String[] args) {
		engineEntryPoint ep = new engineEntryPoint();
		GatewayServer gServer = new GatewayServer(ep); 
		gServer.start();
	}
	
}
