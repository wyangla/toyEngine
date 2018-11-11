package experiment_ground;
import java.util.*;


public class try_iterator {
	public static void main(String[] args){
		ArrayList<Integer> aa = new ArrayList<Integer>();
		aa.add(1);
		aa.add(2);
		aa.add(3);
		aa.add(4);
		aa.add(5);
		aa.add(6);
		Iterator ait = aa.iterator();
		while(true) {
			System.out.println(ait.next());
		}
		
	}
}
