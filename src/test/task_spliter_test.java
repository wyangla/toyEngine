package test;
import utils.*;
import java.util.*;


public class task_spliter_test {
	
	public static void main(String[] args) {
		String[] targetTerms = new String[] {"a", "b", "c", "d", "e", "f", "g"};
		
		ArrayList<String[]> workLoads = task_spliter.get_workLoads_terms(3L, targetTerms);
		for(String[] workLoad : workLoads) {
			System.out.print(Arrays.asList(workLoad));
		}
	}
}
