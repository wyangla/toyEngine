package test;
import entities.scanner_plugins.*;
import inverted_index.*;
import java.lang.reflect.*;

import data_structures.posting_unit;
import entities.scanner_plugins.delete_doc;


public class scanner_plugin_test {

	// test delete_doc plugin
	public void test_1() throws Exception {
		delete_doc delDoc = new delete_doc();
		delDoc.set_parameters("test");
		
		posting_unit pUnit = new posting_unit();
		pUnit.docId = 1L;
		System.out.println(pUnit.status);
		
		delDoc.conduct(pUnit);
		System.out.println(pUnit.status);
		
	}
	
	public static void main(String[] args) {
		scanner_plugin_test sTest = new scanner_plugin_test();
		try {
			sTest.test_1();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
