package experiment_ground;
import java.util.*;


public class get_complicate_class {
	public static HashMap<String, ArrayList<Long>> ch = new HashMap<String, ArrayList<Long>>();
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		Class<? extends HashMap> chClass = ch.getClass();
		chClass.newInstance().put("a", new ArrayList<Long>());
		System.out.println(ch.getClass());
	}
}
