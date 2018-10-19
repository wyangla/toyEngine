package test;
import inverted_index.*;
import probes.*;



public class index_advanced_operations_test {
	
	index_advanced_operations idxAdOp = new index_advanced_operations();
	
	
	// test delete_doc
	public void test_1() {
		try {
			idxAdOp.delete_doc(new String[] {"wanted", "tasty"}, "/test_1/EKAN4jw3LsE3631feSaA_g");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		index idx = index.get_instance();
		index_probe idxProbe = new index_probe();
		index_advanced_operations_test idxAdOpTest = new index_advanced_operations_test();
		
		// prepare the inverted-index
		idx.load_index(new String[] {"wanted", "tasty"});
		idxProbe.show();
		
		idxAdOpTest.test_1();
	}
	
}
