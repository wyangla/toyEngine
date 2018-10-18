package inverted_index;

import inverted_index.scanner_plugins.*;

import java.lang.reflect.Method;
import java.util.*;

import configs.scanner_config;



public class index_advanced_operations {
	scanner snr = new scanner();
	
	// TODO: directly write fixed APIs?
	// TODO: or use commands and configs?
	public ArrayList<Long> delete_doc(String targetDocName) throws Exception{
		ArrayList<Long> affectedUnitIds = new ArrayList<Long>();
		
		Class operationClass = Class.forName(scanner_config.pluginPath + "delete_doc"); // get the plugin class
		Method setParameters = operationClass.getMethod("set_parameters", String.class); // get the set parameter method
		setParameters.invoke(operationClass, "/test_1/EKAN4jw3LsE3631feSaA_g"); // set the parameter
		System.out.println(snr.scan(new String[] {"wanted", "tasty"}, operationClass)); // input the class which contains the parameters
		
		return affectedUnitIds;
	}
	
	
}
