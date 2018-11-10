package test;
import utils.counter;


public class counter_test {
	public static counter c1 = new counter();
	public static counter c2 = new counter();
	
	public static void main(String[] args) {
		c1.put("a", 1.);
		c1.put("b", 1.);
		c1.put("c", -1.0);
		c2.put("b", 1.);
		c2.put("c", 1.);
		
		counter c3 = c1.update(c2);
		System.out.println(c1);
		System.out.println(c2);
		System.out.println(c3);
		
		c3.increase("b", 3.);
		System.out.println(c3);
		
		System.out.println(c3.get_min_key());
		System.out.println(c3.get_topKth_key(1));
		System.out.println(c3.get_max_key());
		System.out.println(c3.get_topK_keys(2));
		
		c3.remove_after_topK(1);
		System.out.println(c3);
		
		counter c4 = new counter();
		for(int i = 0; i < 100000; i ++) {
			c4.increase(""+i, Math.random());
		}
		System.out.println("-- " + 1);
		System.out.println(c4.get_min_key());
		System.out.println("-- " + 2);
		System.out.println(c4.get_topKth_key(1));
		System.out.println("-- " + 3);
		System.out.println(c4.get_max_key());
		System.out.println("-- " + 4);
		System.out.println(c4.get_topK_keys(10));
		
		System.out.println("-- " + 5);
		c4.remove_after_topK(1000);
		System.out.println(c4.size());
		
	}
}
