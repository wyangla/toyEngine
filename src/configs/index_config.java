package configs;

public class index_config {
	public static int retryTimes = 20; // the retry times of requiring the lock and add the units 
//	public static String postingPersistancePath = "persistance/posting"; // this is the relevance path from the classpath
	public static String lexiconPersistancePath = "persistance/lexicon";
	public static String lexicon2PersistancePath = "persistance/lexicon2";
	public static String lastPostUnitIdPath = "persistance/lastId";
	public static String lastDocIdPath = "persistance/lastDocId";
	public static String docsPath = "persistance/docs";
	public static String lastTermIdPath = "persistance/lastTermId";
	
	public static String postingsPersistancePath = "persistance/postings";
	public static int addingDocRetryTimes = 100;	// retry times of retrying adding units of a doc
	public static int reloadWorkNum = 4;
}
