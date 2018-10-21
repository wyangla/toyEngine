package experiment_ground;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock; 


public class rlocker {
//	private Object synchronizedTarget;
	
//	public static ArrayList<String> targetAL = new ArrayList<String> ();
	private ReentrantLock rlock;
	private static Object targetObj;
	
	// feed to the conduct
	public static void print() {
		System.out.println(targetObj);
	}
	
	
	private rlocker(ReentrantLock lock) {
		rlock = lock;
	}
	
	
	public static rlocker setLock(Object targetObject, ReentrantLock lock) { // for reentranLock, it is using the lock object itself to form the synchronization instead of monitoring object
		targetObj = targetObject;
		return new rlocker(lock);
	}
	
	
	public void conduct(Method operationOnTarget) {
		rlock.lock();
		try {
			operationOnTarget.invoke(null); // better to be the static operation, with no input
		}catch(Exception e) {
			e.printStackTrace();
		} finally {
			rlock.unlock();
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		ReentrantLock rlock = new ReentrantLock();
		ArrayList<String> targetAL = new ArrayList<String>();
		targetAL.addAll(Arrays.asList(new String[] {"a", "b", "c"}));
		
		rlocker rl = rlocker.setLock(targetAL, rlock);
		rl.conduct(rlocker.class.getMethod("print"));
	}
}




