package experiment_ground;
import experiment_ground.rlocker;
import java.util.*;
import java.util.concurrent.locks.*;;


public class try_rlock {

	public static int counter = 1;
	public static ArrayList<String>  targetAL = new ArrayList<String>();
	public static ReentrantLock rl = new ReentrantLock();
	
	
	public static void addToAL() { // static; TODO: what if no static?
		targetAL.add( "" + counter++ );
		System.out.println(targetAL);
	}
	
	
	public static void requireLock(String threadName) throws Exception {
//		System.out.println(threadName + "acquired the lock");
		rlocker.setLock(targetAL, rl).conduct(try_rlock.class.getMethod("addToAL"));
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
		for(String element : try_rlock.targetAL) {
			System.out.println(element); 
		}
	}

	// should be able to successfully print out all elements in targetAL
	public static void LockedPrintAL() throws Exception{
		rlocker.setLock(try_rlock.targetAL, rl).conduct(try_rlock.class.getMethod("printAL"));
	}


	
	public static void main(String[] args) throws Exception {
		
		ArrayList<try_rlock.testThread> threads = new ArrayList<try_rlock.testThread>();
		for(int i = 0; i < 50; i ++ ) {
			try_rlock.testThread at = new try_rlock.testThread("thread-" + i);
			at.start();
			threads.add(at);
		}
		for(try_rlock.testThread at : threads) {
			at.join();
		}
		
		LockedPrintAL();
		
		
	}
}
