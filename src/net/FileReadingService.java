package net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileReadingService {
	
	private FileInputStream input;
	
	public FileReadingService(File fileToReadFrom) throws FileNotFoundException {
		input = new FileInputStream(fileToReadFrom);
	}
	
	public int getNextPart(byte[] dataToWriteTo) {
		int bytesRead = 0;
		try {
			bytesRead = input.read(dataToWriteTo);
			if(bytesRead == -1) {
				input.close();
			}
			return bytesRead;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytesRead;
	}
	
}
