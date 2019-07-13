package experiment_ground;

import java.lang.reflect.*; // reflection
import java.util.HashMap;
import org.json.*;

import configs.scanner_config;
import data_structures.posting_unit;
import inverted_index.*;


public class class_loader {
	public int a = 1;
	public int b = 2;
	
	public static void main(String[] args) {
		
		// get the full name
		Class c = int.class; // TODO: String is good, but int not, as it is the primitive type, class.forName cannot handle it.
		System.out.println(c);
		
		// get the full name
		String cString = "" + c;
		System.out.println(cString); 
		
		// reconstruct from full name
		try {
			Class cRec = Class.forName(cString.replaceAll("class ", "")); // eliminate the "class " at the front of the full name
			System.out.println("" + cRec);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// access the value of fields of obj
		class_loader cl = new class_loader();
		Class clc = cl.getClass();
		Field[] clcF = clc.getDeclaredFields();
		
		for(Field f : clcF) {
			Class fClass = f.getType();
			System.out.println(fClass);
			
			String fn = f.getName();
			try {
				System.out.println(fn + " : " + f.get(cl)); // read the value from the fields
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// serialise the HashMap
		HashMap<String, Double> s = new HashMap<String, Double>();
		s.put("a", 0.1);
		System.out.println(s);
		
		JSONObject sJson = new JSONObject(s); // Map -> String
		System.out.println(sJson.toString());
		
		JSONObject sRec = new JSONObject(sJson.toString()); // String -> Map
		System.out.println(sRec.toMap().get("a").getClass()); // json can automatically retrieve data type
		
		
		// test getMethod
		// TODO: the method here is already changed
		try {
			Class operationClass = Class.forName("inverted_index.scanner_plugins.delete_doc");
			try {
				
				Method conduct = operationClass.getMethod("conduct", posting_unit.class, String.class);
				posting_unit pUnit = new posting_unit();
				pUnit.docId = 1L;
				conduct.invoke(operationClass, pUnit, "test");
				System.out.println(pUnit.status); // using DELUNIT operation here, so status should be 0
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
