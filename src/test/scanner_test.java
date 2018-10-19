package test;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;

import configs.scanner_config;
import inverted_index.*;
import inverted_index.scanner_plugins.*;
import probes.index_probe;



public class scanner_test {
	index idx = index.get_instance();
	scanner snr = new scanner();
	index_probe idxPobe = new index_probe();
	
	// test scan_posting_list
	public void test_1() {
		idx.load_lexicon();
		idx.load_posting(new String[] {"wanted"});
		ArrayList<Long> affectedUnits = new ArrayList<Long> (); // collect Ids of units which are affected
		
		delete_doc.set_parameters("/test_1/EKAN4jw3LsE3631feSaA_g");
		snr.scan_posting_list("wanted",delete_doc.class, affectedUnits);
		System.out.println(affectedUnits);
		idx.clear_index();
	}
	
	
	// test scan
	// tasty 2447 2919 2121 {"tf":1,"oh":1} /test_1/EKAN4jw3LsE3631feSaA_g 1
	// wanted 2393 4144 681 {"tf":1,"oh":1} /test_1/EKAN4jw3LsE3631feSaA_g 1
	public void test_2() throws Exception{
		
//		Class operationClass;
//		operationClass = Class.forName(scanner_config.pluginPath + "delete_doc"); // get the plugin class
//		Method setParameters = operationClass.getMethod("set_parameters", String.class); // get the set parameter method
//		setParameters.invoke(operationClass, "/test_1/EKAN4jw3LsE3631feSaA_g"); // set the parameter
		
		idx.load_lexicon();
		delete_doc.set_parameters("/test_1/EKAN4jw3LsE3631feSaA_g");
		System.out.println(snr.scan(new String[] {"wanted", "tasty"}, delete_doc.class)); // input the class which contains the parameters
	}

	
	
	public static void main(String[] args) {
		// prepare the index
		scanner_test scannerTest = new scanner_test();
		scannerTest.test_1();
		// scannerTest.idxPobe.display_content("Sure");
		
		
		try {
			scannerTest.test_2();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
