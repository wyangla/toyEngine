package utils;
import java.io.*;

public class file_creater {
	
	public static boolean create_file(String filePath) {
		boolean createdFlag = false;
		File f = new File(filePath);
		if(!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		try {
			createdFlag = f.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return createdFlag;
	}
	
	public static void main(String[] args) {
		file_creater fc = new file_creater();
		fc.create_file("./asd/asd/ds.txt");
	}
}
