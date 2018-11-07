package test;
import entities.information_manager_plugins.*;
import data_structures.posting_unit;
import java.util.*;



public class term_max_tf_test {
	public static void main(String[] args) {
		// prepare the infoMap
		term_max_tf.infoMap.put("a", 1.0);
		term_max_tf.infoMap.put("b", 2.0);
		term_max_tf.infoMap.put("c", 3.0);
		
		// test get_info
		double info = term_max_tf.get_info("a");
		System.out.println(info);
		
		// test set_info
		posting_unit pUnit = new posting_unit();
		pUnit.uProp.put("tf", 1.0);
		term_max_tf.set_info(pUnit);
		System.out.println(term_max_tf.infoMap);
		
		// test del_info
		term_max_tf.del_info("b");
		System.out.println(term_max_tf.infoMap);
		
		// test persist
		term_max_tf.persist_info();
		
		// test load
		ArrayList<String> targetNameList = new ArrayList<String>();
		for(String targetName : term_max_tf.infoMap.keySet()) {
			targetNameList.add(targetName);
		}
		for (String targetName : targetNameList) {
			term_max_tf.del_info(targetName);
		}
		System.out.println(term_max_tf.infoMap);
		term_max_tf.load_info();
		System.out.println(term_max_tf.infoMap);
	}
}
