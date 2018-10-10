package utils;

public class name_generator {
	public static long thread_name_gen() {
		return (long)(System.currentTimeMillis() + Math.random()*1000);
	}
	
	public static void main(String[] args) {
		name_generator ng = new name_generator();
		long name = ng.thread_name_gen();
		System.out.println("" + name);
	}
}
