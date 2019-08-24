package utils;

import java.io.*;


// ref: https://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
public class delete_directory{
	
	public static void delete(File root) {
		try {
			if(root.isDirectory()) {
				for(File f: root.listFiles()) {
					delete(f);
				}
			}
			
			root.delete();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}