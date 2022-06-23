package main;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Settings {
	
	public static String id;
	public static Color color;
	public static String name;
	
	public static void load() {
		File configFile = new File("lanchat.config");
		boolean configFileLoadedId = false;
		if(configFile.exists())
		{
			try {
				List<String> lines = Files.readAllLines(configFile.toPath());
				for(String line : lines)
				{
					if(line.toLowerCase().startsWith("id")) {
						String id = line.split("=")[1];
						Settings.id = id;
						configFileLoadedId = true;
					}
					else if(line.toLowerCase().startsWith("name"))
					{
						name = line.split("=")[1];
					}
					else if(line.toLowerCase().startsWith("c")) {
						String[] colorParts = line.split("=")[1].split(",");
						Color c = new Color(Integer.parseInt(colorParts[0]), Integer.parseInt(colorParts[1]), Integer.parseInt(colorParts[2]));
						color = c;
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		if(color == null) {
			color = Color.getHSBColor((float) Math.random(), 0.4f, 0.5f + (float) Math.random() * 0.5f);
		}
		if(name == null)
		{
			name = System.getProperty("user.name");
		}
		
		if(configFileLoadedId == false) {
			id = UUID.randomUUID().toString();
			save();
		}
	}
	
	
	public static void save() {
		File configFile = new File("lanchat.config");
		List<String> lines = new ArrayList<String>();
		lines.add("id=" + id);
		lines.add("name=" + name);
		lines.add("c=" + color.getRed() + "," + color.getGreen() + "," + color.getBlue());
		try {
			Files.write(configFile.toPath(), lines, StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
