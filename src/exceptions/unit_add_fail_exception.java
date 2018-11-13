package exceptions;

// used in index
public class unit_add_fail_exception extends Exception {
	public unit_add_fail_exception(String message) {
		super(message);
	}
	
	public static void main(String[] args){
		unit_add_fail_exception uaf = new unit_add_fail_exception("unit failed");
		System.out.println(uaf);
	}
}
