package experiment_ground;
import java.io.IOException;
import java.nio.file.*;
import java.util.stream.*;
import java.util.*;

// ref: https://stackoverrun.com/cn/q/12079625
public class stream {
	public static void main(String[] args) throws IOException {
		List<String> files =  Files.walk(Paths.get(configs.index_config.postingsPersistancePath), 2)
				.map(p -> p.toString())
				.filter(path -> path.endsWith("posting"))
				.collect(Collectors.toList());
		System.out.println(files);
	}
	
}
