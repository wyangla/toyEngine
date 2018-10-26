package configs;

public class index_config {
	public static int retryTimes = 20; // the retry times of requiring the lock and add the units 
//	public static String postingPersistancePath = "persistance/posting"; // this is the relevance path from the classpath
	public static String lexiconPersistancePath = "persistance/lexicon";
	public static String lastPostUnitIdPath = "persistance/lastId";
	public static String docsPath = "persistance/docs";
	
	public static String postingsPersistancePath = "persistance/postings";
}
