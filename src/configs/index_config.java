package configs;

public class index_config {
	public static int retryTimes = 20; // the retry times of requiring the lock and add the units 
	public static String posting_persistance_path = "persistance/posting"; // this is the relevance path from the classpath
	public static String lexicon_persistance_path = "persistance/lexicon";
	public static String last_post_unit_id_path = "persistance/lastId";
}
