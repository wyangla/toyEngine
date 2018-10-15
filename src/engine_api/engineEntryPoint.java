package engine_api;

import py4j.GatewayServer;
import inverted_index.*;

// using the py4j to start a GatewayServer for the python to operate the java objects
public class engineEntryPoint {
	
	private index idx = index.get_instance(); // singleton
	private keeper kpr = keeper.get_instance(); // singleton
	private cleaner clr = new cleaner(); // could be multiple instances
	
	public index getIndex() {
		return idx;
	}
		
	public keeper getKeeper() {
		return kpr;
	}
	
	public cleaner getCleaner() {
		return clr;
	}

	
	
	public static void main(String[] args) {
		engineEntryPoint ep = new engineEntryPoint();
		GatewayServer gServer = new GatewayServer(ep); 
		gServer.start();
	}
	
}
