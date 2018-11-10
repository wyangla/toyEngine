package test;
import data_structures.doc;
import entities.information_manager;
import inverted_index.*;
import probes.*;
import entities.information_manager_plugins.*;



public class index_advanced_operations_test {
	
	index idx = index.get_instance();
	index_advanced_operations idxAdOp = new index_advanced_operations();
	index_io_operations idIOOp = index_io_operations.get_instance();
	information_manager infoManager = information_manager.get_instance();
	
	// test delete_doc
	public void test_1() {
		try {
			System.out.println(idxAdOp.delete_doc(new String[] {"wanted", "tasty"}, "/test_1/EKAN4jw3LsE3631feSaA_g"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// test add_doc, moved to the index
	public void test_2() {
		idx.add_doc(new String[] {"your 2499 5131 2203 {\"tf\":1,\"oh\":1} /test_1/EKAN4jw3LsE3631feSaA_g 1"}, "/test_1/EKAN4jw3LsE3631feSaA_g");
	}
	
	// test get_term_upper_bounds
	public void test_3() {
		idIOOp.load_posting(new String[] {"wanted"});
		System.out.println(idxAdOp.get_term_upper_bounds(new String[] {"wanted"}));
	}
	
	
	public static void main(String[] args) {
		index idx = index.get_instance();
		index_probe idxProbe = new index_probe();
		index_advanced_operations_test idxAdOpTest = new index_advanced_operations_test();
		
		// prepare the inverted-index
		index_io_operations.get_instance().load_lexicon();
		index_io_operations.get_instance().load_posting(new String[] {"wanted", "tasty"});
		idxProbe.show();
		
//		idxAdOpTest.test_1();
//		idxAdOpTest.test_2();
		
//		idxProbe.display_content("Sure");
//		System.out.println(idx.docMap.get("/test_1/EKAN4jw3LsE3631feSaA_g").flatten());
//		System.out.println(doc.deflatten(idx.docMap.get("/test_1/EKAN4jw3LsE3631feSaA_g").flatten()).docId);
//		System.out.println(doc.deflatten(idx.docMap.get("/test_1/EKAN4jw3LsE3631feSaA_g").flatten()).docLength);
//		System.out.println(doc.deflatten(idx.docMap.get("/test_1/EKAN4jw3LsE3631feSaA_g").flatten()).docProp);
		
		idxAdOpTest.test_3();
	}
	
}
