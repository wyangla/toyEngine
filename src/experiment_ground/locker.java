package experiment_ground;

import java.lang.reflect.*;
import java.util.*;


public class locker {
	private Object synchronizedTarget;
	
	private locker(Object target) {
		synchronizedTarget = target;
	}
	
	public static locker setTarget(Object target) { // locker.setTarget(ArrayList...)
		return new locker(target); // in this project, it demands different locks for multiple object of the same type, so using instance instead of class is a reasonable choice
	}
	
	
	// feed to the conduct
	public static void print(Object a) {
		System.out.println(a);
	}
	
	
	public void conduct(Method operationOnTarget) {
		synchronized(synchronizedTarget) {
			try {
				operationOnTarget.invoke(null); // better to be the static operation, with no input
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		ArrayList<String> target = new ArrayList<String>();
		target.addAll(Arrays.asList(new String[] {"a", "b", "c"}));
		
		locker l = locker.setTarget(target);
		l.conduct(locker.class.getMethod("print", Object.class));
	}
}
