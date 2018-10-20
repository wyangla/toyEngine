package experiment_ground;
import experiment_ground.locker;
import java.util.*;



public class try_lock {

	public static int counter = 1;
	public static ArrayList<String>  targetAL = new ArrayList<String>();
	
	public static void addToAL() {
		targetAL.add( "" + counter++ );
		System.out.println(targetAL);
	}
	
	public static void requireLock(String threadName) throws Exception {
//		System.out.println(threadName + "acquired the lock");
		locker.setTarget(targetAL).conduct(try_lock.class.getMethod("addToAL") );
		// at the same time only one thread can use the addToAL method to operate the targetAL
		
	}
	
	
	public static class testThread extends Thread {
		
		// set the name of thread
		public testThread(String threadName) {
			this.setName(threadName);
		}
		
		public void run() {
//			System.out.println(tName + " is running");
			try {
				requireLock(this.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	// should have the concurrent exception here
	public static void printAL() throws Exception{
		for(String element : try_lock.targetAL) {
			System.out.println(element); 
		}
	}

	
	// should be able to successfully print out all elements in targetAL
	public static void LockedPrintAL() throws Exception{
		locker.setTarget(try_lock.targetAL).conduct(try_lock.class.getMethod("printAL"));
	}

	

	
	public static void main(String[] args) throws Exception {
		
		ArrayList<try_lock.testThread> threads = new ArrayList<try_lock.testThread>();
		for(int i = 0; i < 50; i ++ ) {
			try_lock.testThread at = new try_lock.testThread("thread-" + i);
			at.start();
			threads.add(at);
		}
		for(try_lock.testThread at : threads) {
			at.join();
		}
		
		LockedPrintAL();
		
		
	}
}
