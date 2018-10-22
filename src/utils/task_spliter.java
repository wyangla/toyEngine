package utils;
import java.util.*;


public class task_spliter {
	public static ArrayList<String[]> get_workLoads_terms(long chunkNum, String[] targetTermList) { // using ArrayList here is to make it applicable to various objects
		
		ArrayList<String[]> workLoads = new ArrayList<String[]>();
		long workLoad = (long)Math.ceil((double)targetTermList.length / (double)chunkNum);
		
		Iterator<String> targetTermIter = Arrays.asList(targetTermList).iterator();
		
		for(long i = 0L; i < chunkNum; i ++) {
			ArrayList<String> chunk = new ArrayList<String>();
			while(true) {
				try {
					chunk.add(targetTermIter.next());
					if(chunk.size() >= workLoad) {
						break;
					}
				} catch(NoSuchElementException e) {
					break;
				}
			}
			workLoads.add(chunk.toArray(new String[0]));
		}

		return workLoads;
	}
}
