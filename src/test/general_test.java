package test;

public class general_test {
	public int a = 1;
	public int b = 2;
	
	public static void main(String[] args) {
		
		// 
		Class c = String.class;
		System.out.println(c); // full name
		
		String cString = "" + c;
		System.out.println(cString); // get the full name
		
		try {
			Class cRec = Class.forName(cString.replaceAll("class ", "")); // eliminate the "class " at the front of the full name
			System.out.println("" + cRec);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
