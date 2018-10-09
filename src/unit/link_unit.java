package unit;

public class link_unit {
	long current_num = 0;  // index number of current link unit
	long next_num = 0;  // index number of next link unit
	unit_properties uProp = new unit_properties(); // for storing properties like tfidf, etc.
	
	public static void main(String[] args) {
		link_unit _self = new link_unit();
		_self.uProp.set_params("testKey", 0.01);
		System.out.println("" + _self.uProp.propertiesDict.entrySet());
		
	}
}
