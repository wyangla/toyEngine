package utils;
import java.util.*;

public class slicer {
	// String[] slicer
	public static String[] string_array_slicer(String[] inputArray, int startIdx, int endIdx) {
		String[] subArray = Arrays.asList(inputArray).subList(startIdx, endIdx).toArray(new String[0]);
		return subArray;
	}
	
	public static void main(String[] args) {
		slicer slr = new slicer();
		System.out.println(Arrays.asList(
				slr.string_array_slicer(new String[] {"a",  "b",  "c", "d"}, 0, 3)
				));
	}
}
