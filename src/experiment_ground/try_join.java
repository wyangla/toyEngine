package experiment_ground;
import java.util.*;


public class try_join {
	public static ArrayList<String> al = new ArrayList<String>();
	
//	public static class subThread extends Thread {
//		public void run() {
//			al.add("sub thread");
//			System.out.println("sub thread");
//		}
//		
//		public void start() {
//			Thread t = new Thread(this, "" + System.currentTimeMillis());
//			t.start();
//		}
//	}
//	
//	public static void main(String[] args) throws InterruptedException {
//		ArrayList<subThread> subThreads = new ArrayList<subThread>();
//		
//		for(int i = 0; i < 50; i ++) {
//			subThread t = new subThread();
//			subThreads.add(t);
//			t.start();
//		}
//		
//		for(subThread t : subThreads) {
//			t.join(); // this is not the really running thread "t", which is started by the sub.start not touched
//		}
//		
//		System.out.println("--> main thread");
//	}
	
	public static class subThread extends Thread {
		
		public subThread(String threadName) {
			this.setName(threadName);
		}
		
		public void run() {
			System.out.println("sub thread: " + this.getName());
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		ArrayList<subThread> subThreads = new ArrayList<subThread>();
		
		for(int i = 0; i < 50; i ++) {
			subThread t = new subThread("thread: "+i);
			subThreads.add(t);
			t.start();
		}
		
		for(subThread t : subThreads) {
			t.join();
		}
		
		System.out.println("--> main thread");
	}
	
}
