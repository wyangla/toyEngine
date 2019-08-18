package experiment_ground;

class clean_name{
	public static void main(String[] args) {
		String origName = "asdfadf__12341";
		String cleanedName = origName.substring(0, origName.lastIndexOf("__"));
		System.out.println(cleanedName);
	}
}