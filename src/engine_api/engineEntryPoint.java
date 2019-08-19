package engine_api;

import py4j.GatewayServer;
import inverted_index.*;
import entities.keeper_plugins.*;
import entities.information_manager_plugins.*;
import probes.index_probe;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import data_structures.*;
import entities.*;
import utils.*;



// using the py4j to start a GatewayServer for the python to operate the java objects
public class engineEntryPoint {
	
	private index idx = index.get_instance();
	private keeper kpr = keeper.get_instance();
	private cleaner clr = cleaner.getInstance();
	index_probe idxProb = new index_probe();
	index_advanced_operations advOps = new index_advanced_operations();
	index_io_operations ioOps = index_io_operations.get_instance(); // the order is not important, as it firstly initiated idx, whose definiton does not contain the ioOps 
	information_manager infoManager = information_manager.get_instance();
	deactivator dac = deactivator.get_instance();
	
	
	// start serving
	private void load_index() {
		ioOps.load_index();
	}
	
	// main objects 
	public index _get_index() {
		return idx;
	}
	
	public keeper _get_keeper() {
		return kpr;
	}
	
	public cleaner _get_cleaner() {
		return clr;
	}
	
	public ConcurrentHashMap<String, ArrayList<Long>> _get_lexicon() {
		return idx.lexicon;
	}	
	
	public ConcurrentHashMap<Long, posting_unit> _get_postUnitMap() {
		return idx.postUnitMap;
	}
	
	public HashMap<String, HashMap<String, String>> _get_docMap() {
		HashMap<String, HashMap<String, String>> docInfoMap = new HashMap<String, HashMap<String, String>> ();
		
		for (String docName : idx.docMap.keySet()) {
			HashMap<String, String> infoMap = new HashMap<String, String> ();
			doc docIns = idx.docMap.get(docName);
			
			String docId = "" + docIns.docId;
			String docLengthStr = "" + docIns.docLength;
			String docPropStr = "" + docIns.docProp.toString();
			// String pUnitIdListStr = "" + docIns.pUnitIdList.toString();
			
			infoMap.put("docId", docId);
			infoMap.put("docLength", docLengthStr);
			infoMap.put("docProp", docPropStr);
			// infoMap.put("pUnitIdList", pUnitIdListStr);
			
			docInfoMap.put(docName, infoMap);
		}
		return docInfoMap;
	}
	
	public HashMap<String, HashMap<String, String>> _get_docIdMap() {
		HashMap<String, HashMap<String, String>> docInfoMap = new HashMap<String, HashMap<String, String>> ();
		
		for (long docId : idx.docIdMap.keySet()) {
			HashMap<String, String> infoMap = new HashMap<String, String> ();
			doc docIns = idx.docIdMap.get(docId);
			
			String docName = "" + docIns.docName;
			String docLengthStr = "" + docIns.docLength;
			String docPropStr = "" + docIns.docProp.toString();
			// String pUnitIdListStr = "" + docIns.pUnitIdList.toString();
			
			infoMap.put("docName", docName);
			infoMap.put("docLength", docLengthStr);
			infoMap.put("docProp", docPropStr);
			// infoMap.put("pUnitIdList", pUnitIdListStr);
			
			docInfoMap.put("" + docId, infoMap);
		}
		return docInfoMap;
	}
	
	public ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> _get_lexiconLockInfoMap() {
		return kpr.get_lockInfoMap(lexicon_locker.class);
	}
	
	public ConcurrentHashMap<String, ReentrantLock> _get_lexiconLockMap() {
		return kpr.get_lockMap(lexicon_locker.class);
	}
	
	// the postUnitMap will be dump to local after not being accessed for a while
	public Set<Long> _get_pUnitList() {
		return idx.postUnitMap.keySet();
	}
	
	public int _check_pUnitStatus(Long pUnitId) {
		return idx.postUnitMap.get(pUnitId).status;
	}
	
	// TODO: just for test
	public ConcurrentHashMap<String, Double> _get_termDf() {
		return term_df.infoMap;
	}
	
	// TODO: just for test
	public ConcurrentHashMap<String, Double> _get_termIdf() {
		return term_idf.infoMap;
	}
	
	
	
	

	// basic operations defined in the index
	public long add_term(String term) {
		long firstUnitId = idx.add_term(term);
		return firstUnitId;
	}
	
	public void del_term(String term) {
		idx.del_term(term);
	}
	
	public int cal_termIdf() {
		return idx.cal_termIdf();
	}
	
	
	// persistedUnit: [term<String>] currentId<Long> nextId<Long> previousId<Long> {uProp}<String, Long> docId<String> status<Integer>
	public long add_posting_unit(String persistedUnit) {
		long addedUnitId = idx.add_posting_unit(persistedUnit).currentId;
		return addedUnitId;
	}
	
	public long del_posting_unit(long postingUnitId) {
		long delUnitId = idx.del_posting_unit(postingUnitId);
		return delUnitId;
	}
	
	
	public void persist_index() {
		ioOps.persist_index();
	}
	
	public long load_posting_unit(posting_unit postUnit) {
		long addedUnitId = ioOps.load_posting_unit(postUnit);
		return addedUnitId;
	}
	
	public long[] load_posting(ArrayList<String> targetTermsAL) {
		String[] targetTerms = targetTermsAL.toArray(new String[0]); // (String[]) 
		long[] loaded_units = ioOps.load_posting(targetTerms);
		return loaded_units;
	}
	
	public void _load_doc_related_postings(long docId) {
		ioOps.load_doc_related_postings(docId);
	}
	
	// load all the postings into memory
	public void load_all_posting() {
		ioOps.load_all_posting();
	}
	
	public void clear_index() {
		idx.clear_index();
	}
	
	public void reload_index() {
		idx.reload_index();
	}
	
	public void clean_index() {
		clr.clean();
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
	
	// display the current post unit id counter value
	public long get_pc() {
		return idx.get_pc();
	}
	
	
	public ArrayList<String> add_doc(ArrayList<String> persistedUnits, String targetDocName) {
		ArrayList<String> failedPersistedUnits = idx.add_doc(persistedUnits.toArray(new String[0]), targetDocName);
		return failedPersistedUnits;
	}
	
	
	/*use posting list*/
//	public ArrayList<Long> delete_doc(ArrayList<String> containedTerms, String targetDocName) {
//		ArrayList<Long> affectedUnits = new ArrayList<Long>();
//		try {
//			affectedUnits = advOps.delete_doc(containedTerms.toArray(new String[0]), targetDocName);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		return affectedUnits;
//	}
	
	/*use term chain*/
	public ArrayList<Long> delete_doc(String targetDocName) {
		ArrayList<Long> affectedUnits = new ArrayList<Long>();
		try {
			affectedUnits = advOps.delete_doc(targetDocName);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return affectedUnits;
	}
	
	
	// naive search
	public counter search(ArrayList<String> queryTerms) {
		counter relatedDocumentScores = new counter();
		try {
			relatedDocumentScores = advOps.search(queryTerms.toArray(new String[0]));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return relatedDocumentScores;
	}
	
	public counter search_normalized(ArrayList<String> queryTerms) {
		counter relatedDocumentScoresNormalized = new counter();
		try {
			relatedDocumentScoresNormalized = advOps.search_normalized(queryTerms.toArray(new String[0]));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return relatedDocumentScoresNormalized;
	}
	
	
	// maxScore
	public counter search_maxScore(ArrayList<String> queryTerms, int topK) {
		counter relatedDocumentScores = new counter();
		try {
			relatedDocumentScores = advOps.search_maxScore(queryTerms.toArray(new String[0]), topK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return relatedDocumentScores;
	}
	
	public counter search_maxScore_normalized(ArrayList<String> queryTerms, int topK) {
		counter relatedDocumentScores = new counter();
		try {
			relatedDocumentScores = advOps.search_maxScore_normalized(queryTerms.toArray(new String[0]), topK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return relatedDocumentScores;
	}
	
	
	
	// WAND
	public counter search_WAND(ArrayList<String> queryTerms, int topK) {
		counter relatedDocumentScores = new counter();
		try {
			relatedDocumentScores = advOps.search_WAND(queryTerms.toArray(new String[0]), topK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return relatedDocumentScores;
	}
	
	
	// high level information
	public ConcurrentHashMap<String, Double> get_posting_loaded_status() {
		return posting_loaded_status.infoMap;
	}
	
	
	public ConcurrentHashMap<String, Double> get_term_max_tf() {
		return term_max_tf.infoMap;
	}
	
	
	// deactivator
	public void start_monitoring() {
		try {
			dac.start_monitoring();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void start_serving() {
		load_index();
		start_monitoring();
	}
	
	public void pause_deactivator() {
		dac.pause_deactivator();
	}
	
	public void resume_deactivator() {
		dac.resume_deactivator();
	}
	
	
	
	public static void main(String[] args) {
		engineEntryPoint ep = new engineEntryPoint();
		ep.start_serving();
		
		GatewayServer gServer = new GatewayServer(ep); 
		gServer.start();
	}
	
}
