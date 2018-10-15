package engine_api;

import py4j.GatewayServer;
import inverted_index.*;
import java.util.*;

// using the py4j to start a GatewayServer for the python to operate the java objects
public class engineEntryPoint {
	
	private index idx = index.get_instance();
	private keeper kpr = keeper.get_instance();
	private cleaner clr = cleaner.getInstance();
	
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
	
	
	public static void main(String[] args) {
		engineEntryPoint ep = new engineEntryPoint();
		GatewayServer gServer = new GatewayServer(ep); 
		gServer.start();
	}
	
}
