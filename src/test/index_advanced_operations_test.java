package test;
import data_structures.doc;
import inverted_index.*;
import probes.*;



public class index_advanced_operations_test {
	
	index_advanced_operations idxAdOp = new index_advanced_operations();
	
	
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
//	public void test_2() {
//		idxAdOp.add_doc(new String[] {"your 2499 5131 2203 {\"tf\":1,\"oh\":1} /test_1/EKAN4jw3LsE3631feSaA_g 1"}, "/test_1/EKAN4jw3LsE3631feSaA_g");
//	}
	
	
	public static void main(String[] args) {
		index idx = index.get_instance();
		index_probe idxProbe = new index_probe();
		index_advanced_operations_test idxAdOpTest = new index_advanced_operations_test();
		
		// prepare the inverted-index
		idx.load_lexicon();
		idx.load_posting(new String[] {"wanted", "tasty"});
		idxProbe.show();
		
		idxAdOpTest.test_1();
//		idxAdOpTest.test_2();
		
		idxProbe.display_content("Sure");
		System.out.println(idx.docMap.get("/test_1/EKAN4jw3LsE3631feSaA_g").flatten());
		System.out.println(doc.deflatten(idx.docMap.get("/test_1/EKAN4jw3LsE3631feSaA_g").flatten()).docId);
		System.out.println(doc.deflatten(idx.docMap.get("/test_1/EKAN4jw3LsE3631feSaA_g").flatten()).docLength);
		System.out.println(doc.deflatten(idx.docMap.get("/test_1/EKAN4jw3LsE3631feSaA_g").flatten()).docProp);
	}
	
}
