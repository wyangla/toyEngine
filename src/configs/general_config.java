package configs;

public class general_config {
	public static int cpuNum = 1; // when the posting list are very short, the threads establishing time is larger than the scanning time, so that not suitable with using multi-threads
	public static String machine = "thinkpad_wyan";
	public static String processedDocPath = "C:\\desktop\\workspace\\toyProject\\toyEngine_operator\\corpus";    // TODO: split out!
}
