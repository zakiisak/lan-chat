package gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class HtmlReader {
	
	//TODO make part of the classpath
	public static String readHtmlFile(String filepath) throws IOException
	{
		List<String> lines = Files.readAllLines(new File(filepath).toPath());
		StringBuilder builder = new StringBuilder();
		for(String line : lines)
		{
			builder.append(line + System.lineSeparator());
		}
		return builder.toString();
	}
	
}
