// try if the heritage will overwrite the static probability
package experiment_ground;
import java.util.ArrayList;


public class heritate_static {
	public static ArrayList<Integer> staticArrayList = new ArrayList<Integer> ();
	public ArrayList<Integer> staticArrayList4Ins = new ArrayList<Integer> ();    // ins property
	
	
	public static void inc(int x) {
		staticArrayList.add(x);
	}
	
	public void insIns(int x) {  // ins method
		staticArrayList4Ins.add(x);
	}
}