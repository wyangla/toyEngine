package engine_api;

import py4j.GatewayServer;
import inverted_index.*;
import inverted_index.keepe_plugins.*;
import probes.index_probe;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;



// using the py4j to start a GatewayServer for the python to operate the java objects
public class engineEntryPoint {
	
	private index idx = index.get_instance();
	private keeper kpr = keeper.get_instance();
	private cleaner clr = cleaner.getInstance();
	index_probe idxProb = new index_probe();
	index_advanced_operations advOps = new index_advanced_operations();
	
	
	// start serving
	private void load_lexicon() {
		idx.load_lexicon();
		System.out.println("Lexicon loaded");
	}
	
	// main objects 
	public index get_index() {
		return idx;
	}
	
	public keeper get_keeper() {
		return kpr;
	}
	
	public cleaner get_cleaner() {
		return clr;
	}
	
	public HashMap<String, ArrayList<Long>> get_lexicon() {
		return idx.lexicon;
	}	
	
	public HashMap<Long, posting_unit> get_postUnitMap() {
		return idx.postUnitMap;
	}
	
	public HashMap<String, HashMap<String, Long>> get_lexiconLockInfoMap() {
		return kpr.get_lockInfoMap(lexicon_locker.class);
	}
	
	public HashMap<String, ReentrantLock> get_lexiconLockMap() {
		return kpr.get_lockMap(lexicon_locker.class);
	}
	
	
	
	// basic operations defined in the index
	public long add_term(String term) {
		long firstUnitId = idx.add_term(term);
		return firstUnitId;
	}
	
	public void del_term(String term) {
		idx.del_term(term);
	}
	
	// persistedUnit: [term<String>] currentId<Long> nextId<Long> previousId<Long> {uProp}<String, Long> docId<String> status<Integer>
	public long add_posting_unit(String persistedUnit) {
		long addedUnitId = idx.add_posting_unit(persistedUnit);
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
	
	public long[] load_posting(ArrayList<String> targetTermsAL) {
		String[] targetTerms = targetTermsAL.toArray(new String[0]); // (String[]) 
		long[] loaded_units = idx.load_posting(targetTerms);
		return loaded_units;
	}
	
	// load all the postings into memory
	public void load_all_posting() {
		idx.load_all_posting();
	}
	
	public void clear_index() {
		idx.clear_index();
	}
	
	public void reload_index() {
		idx.reload_index();
	}
	
	// print the whole inverted-index
	public HashMap<String, String> display_content(String areYouSureAboutPrintTheWholeIndex) {
		HashMap<String, String> infoMap = idxProb.display_content(areYouSureAboutPrintTheWholeIndex);
		return infoMap;
	}
	
	// only print the statistic information of the inverted-index
	public HashMap<String, String> show() {
		HashMap<String, String> infoMap = idxProb.show();
		return infoMap;
	}
	
	// cleaner index
	public void clean_index() {
		clr.clean();
	}
	
	
	
	// advanced operations
	public ArrayList<Long> delete_doc(ArrayList<String> containedTerms, String targetDocName) {
		ArrayList<Long> affectedUnits = new ArrayList<Long>();
		try {
			affectedUnits = advOps.delete_doc(containedTerms.toArray(new String[0]), targetDocName);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return affectedUnits;
	}
	
	
	
	public static void main(String[] args) {
		engineEntryPoint ep = new engineEntryPoint();
		ep.load_lexicon(); // load the lexicon into memory firstly
		
		GatewayServer gServer = new GatewayServer(ep); 
		gServer.start();
	}
	
}
