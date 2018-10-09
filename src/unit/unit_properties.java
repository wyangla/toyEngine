package unit;
import java.util.*;




public class unit_properties {
	// common properties related to a single doc
	// term frequency
	// term position
	// doc length

	
	
	HashMap propertiesDict = new HashMap();  // TODO: for dynamically put in the properties, directly used by link_unit?
	

	
	public int set_params(String pName, double pVal) {
		// TODO: invoking the scroing models
		try{
			propertiesDict.put(pName, pVal);
			return 1;
		}catch(Exception e){
			return 0;
		}
	}
	

	
	
	// for testing
	public static void main(String[] args) {
		unit_properties _self = new unit_properties();
		int ret = _self.set_params("testKey", 0.01);
		System.out.println("" + ret);
		System.out.println("" + _self.propertiesDict.entrySet());
	}
	
	
	
}
