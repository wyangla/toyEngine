package test;
import utils.counter;


public class counter_test {
	public static counter c1 = new counter();
	public static counter c2 = new counter();
	
	public static void main(String[] args) {
		c1.put("a", 1.);
		c1.put("b", 2.);
		c2.put("b", 3.);
		c2.put("c", 1.);
		
		counter c3 = c1.update(c2);
		System.out.println(c1);
		System.out.println(c2);
		System.out.println(c3);
		
	}
}
