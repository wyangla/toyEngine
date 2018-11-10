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
	// need the lexicon generated
	public void test_3() {
		System.out.println(idx.lexicon.get("wanted"));
		System.out.println(infoManager.get_info(term_max_tf.class, "wanted")); // 4.701457146816058
		System.out.println(idxAdOp.get_term_upper_bounds(new String[] {"wanted", "a"})); // 2.0481418383862344
	}
	
	// test get_doc_upper_bounds
	public void test_4() {
		System.out.println(idxAdOp.get_doc_upper_bounds(new String[] {"wanted"}));
	}
	
	// test search_MaxScore
	public void test_5() {
		System.out.println("-->");
		long t1 = System.currentTimeMillis();
		System.out.println(idxAdOp.search(new String[] {"wanted", "a", "want"}));
		long t2 = System.currentTimeMillis();
		System.out.println(t2 - t1);
		
		System.out.println(idxAdOp.search_MaxScore(new String[] {"wanted", "a", "want"}, 2));
		long t3 = System.currentTimeMillis();
		System.out.println(t3 - t2);
	}
	
	
	public static void main(String[] args) {
		index idx = index.get_instance();
		index_probe idxProbe = new index_probe();
		index_advanced_operations_test idxAdOpTest = new index_advanced_operations_test();
		
		// prepare the inverted-index
		index_io_operations.get_instance().load_lexicon();
		index_io_operations.get_instance().load_posting(new String[] {"wanted", "tasty"});
		index_io_operations.get_instance().load_info();
		index_io_operations.get_instance().load_docMap();
		idxProbe.show();
		
//		idxAdOpTest.test_1();
//		idxAdOpTest.test_2();
		
//		idxProbe.display_content("Sure");
//		System.out.println(idx.docMap.get("/test_1/EKAN4jw3LsE3631feSaA_g").flatten());
//		System.out.println(doc.deflatten(idx.docMap.get("/test_1/EKAN4jw3LsE3631feSaA_g").flatten()).docId);
//		System.out.println(doc.deflatten(idx.docMap.get("/test_1/EKAN4jw3LsE3631feSaA_g").flatten()).docLength);
//		System.out.println(doc.deflatten(idx.docMap.get("/test_1/EKAN4jw3LsE3631feSaA_g").flatten()).docProp);
		
//		idxAdOpTest.test_3();
//		idxAdOpTest.test_4();
		idxAdOpTest.test_5();
	}
	
}
