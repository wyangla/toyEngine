package experiment_ground;

public class string_operations {

	public static void main(String[] args) {
		String[] l = " sdfs /tasds".split(" ");
		for(String t : l) {
			System.out.println(t);
		}
		System.out.println("  aa  ".replaceAll("aa" + " ", "1"));
	}
}
