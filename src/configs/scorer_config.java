package configs;

public class scorer_config {
	public static String scorerPluginPath = "entities.scorer_plugins.";
	public static String[] modelsInUse = new String[] {"tfidf", "oh"};
	public static double[] modelWeight = new double[] {0.8, 0.2};
}
