package experiment_ground;
import java.util.*;


public class compare {
	public static void main(String[] args) {
		Integer a = 1;
		Integer b = 2;
		System.out.println(a.compareTo(b));
		
		ArrayList<Integer> l = new ArrayList();
		l.add(a);
		l.add(b);
		
		l.sort((o1,o2) -> o1.compareTo(o2));
		System.out.println(l);
		
		l.sort((o1,o2) -> o2.compareTo(o1));
		System.out.println(l);
	}
}
