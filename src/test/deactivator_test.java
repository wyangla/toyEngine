package test;

import inverted_index.index;
import data_structures.*;
import probes.index_probe;
import entities.*;


public class deactivator_test {
	public static index idx = index.get_instance();
	public static index_probe idxProb = new index_probe();
	public static deactivator dac = deactivator.get_instance();


	public void prepare_index() {
		idx.add_term("-test-");
		posting_unit pUnit_1 = new posting_unit();
		pUnit_1.term = "-test-";
		idx.add_posting_unit(pUnit_1.flatten());
		idxProb.display_content("Sure");
	}
	
	
	// test expire
	public void test_1() {		
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(dac.check_expired("-test-"));
		System.out.println();
	}
	
	
	// test deactivate
	public void test_2(){
		try {
			dac.deactivate();
			idxProb.display_content("Sure");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// test start_monitoring
	public void test_3() {
		try {
			dac.start_monitoring();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		deactivator_test dacTest = new deactivator_test();
		dacTest.prepare_index();
		dacTest.test_1();
//		dacTest.test_2();
		dacTest.test_3();
		
	}
	
}





