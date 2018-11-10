package configs;

public class scorer_config {
	public static String scorerPluginPath = "entities.scorer_plugins.";
	
	// configs used the search_term, which is a plain terms scoring method
	public static String[] modelsInUse = new String[] {"tfidf", "oh"};
	public static double[] modelWeight = new double[] {1.0, 0.0};
}
