package inverted_index;
import inverted_index.scanner_plugins.*;
import java.util.*;



// operatiosn demands scanning the posting list
public class index_advanced_operations {
	
	scanner snr = new scanner();
	
	
	public ArrayList<Long> delete_doc(String[] containedTerms, String targetDocName) throws Exception { // the containedTerms are generated and provided by the engine operator
		ArrayList<Long> affectedUnitIds = new ArrayList<Long>();
		
		// here not using the path to find the plugins, as the design for scanner is not for command input 
		delete_doc.set_parameters(targetDocName);
		affectedUnitIds = snr.scan(containedTerms, delete_doc.class); // input the class which contains the parameters
		
		return affectedUnitIds;
	}
	
}
