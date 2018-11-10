package experiment_ground;
import java.util.*;


public class try_linkedHashMap {
	static LinkedHashMap lhm = new LinkedHashMap();
	
	public static void main(String[] args) {
		lhm.put("-", 2);
		lhm.put("a", 1);
		lhm.put("-", 3);
		System.out.print(lhm);
	}
	
}
