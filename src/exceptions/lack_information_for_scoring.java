package exceptions;

// not used yet
public class lack_information_for_scoring extends Exception {
	public lack_information_for_scoring(String msg) {
		super(msg);
	}
	
	public static void main(String[] args) {
		new lack_information_for_scoring("lack info").printStackTrace();
	}
}
