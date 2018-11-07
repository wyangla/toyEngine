package test;
import entities.*;
import entities.information_manager_plugins.*;
import data_structures.*;


public class information_manager_test {
	public static information_manager infoManager = information_manager.get_instance();
	public static posting_unit pUnit = new posting_unit();
	
	public static void main(String[] args) {
		pUnit.term = "test_infoManager";
		pUnit.uProp.put("tf", 1.5);
		
		int addedFlag = infoManager.set_info(term_max_tf.class, pUnit);
		System.out.println(term_max_tf.infoMap);
		System.out.println(addedFlag);
		
		double info = infoManager.get_info(term_max_tf.class, pUnit.term);
		System.out.println(term_max_tf.infoMap);
		System.out.println(info);
		
		int persistFlag = infoManager.persist_info(term_max_tf.class);
		System.out.println(term_max_tf.infoMap);
		System.out.println(persistFlag);
		
		int delFlag = infoManager.del_info(term_max_tf.class, pUnit.term);
		System.out.println(term_max_tf.infoMap);
		System.out.println(delFlag);
		
		int loadFlag = infoManager.load_info(term_max_tf.class);
		System.out.println(term_max_tf.infoMap);
		System.out.println(loadFlag);
		
		// will not update the info, as tf is not > original
		posting_unit pUnit_2 = new posting_unit();
		pUnit_2.term = "test_infoManager";
		pUnit_2.uProp.put("tf", 0.0);
		int addedFlag_2 = infoManager.set_info(term_max_tf.class, pUnit_2);
		System.out.println(term_max_tf.infoMap);
		System.out.println(addedFlag_2);
		
		// will update the info, as tf is > original
		posting_unit pUnit_3 = new posting_unit();
		pUnit_3.term = "test_infoManager";
		pUnit_3.uProp.put("tf", 2.0);
		int addedFlag_3 = infoManager.set_info(term_max_tf.class, pUnit_3);
		System.out.println(term_max_tf.infoMap);
		System.out.println(addedFlag_3);
		
		Double info_2 = infoManager.get_info(term_max_tf.class, "--");
		System.out.println(term_max_tf.infoMap);
		System.out.println(info_2);
		
		int clearFlag = infoManager.clear_info(term_max_tf.class);
		System.out.println(term_max_tf.infoMap);
		System.out.println(clearFlag);
	}
}
