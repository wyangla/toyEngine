// try if the heritage will overwrite the static probability
package experiment_ground;
import java.util.ArrayList;


public class heritate_static_3 extends heritate_static {
	// public static ArrayList<Integer> staticArrayList = new ArrayList<Integer> ();
	public ArrayList<Integer> staticArrayList4Ins = new ArrayList<Integer> ();    // ins property
	
	public static void main(String[] args) {
		heritate_static_2 h2Ins = new heritate_static_2();
		
		/*static property*/
		h2Ins.inc(1);    // in h2Ins, the staticArrayList is override, however the .inc still working on the heritate_static.staticArrayList, as it is defined in terms of that object
		System.out.println("h2Ins" + h2Ins.staticArrayList);    // []
		
		System.out.println("heritate_static_3" + heritate_static_3.staticArrayList);    // [1], in heritate_static_3, the staticArrayList is not override
		
		inc(2);
		System.out.println("heritate_static_3" + heritate_static_3.staticArrayList);    // [1, 2], here is operating the original staticArrayList
		
		System.out.println();
		
		/*ins property*/
		h2Ins.insIns(1);
		System.out.println("h2Ins" + h2Ins.staticArrayList4Ins);    // [], the h2Ins.staticArrayList4Ins is different object to the heritate.h2Ins.staticArrayList4Ins, 
		
		heritate_static_3 h3Ins = new heritate_static_3();
		System.out.println("heritate_static_3" + h3Ins.staticArrayList4Ins);    // []
		
		h3Ins.insIns(2);
		System.out.println("heritate_static_3" + h3Ins.staticArrayList4Ins);    // [], the link between heritate.insIns and new heritate_static_3, is then broken
		
	}
}