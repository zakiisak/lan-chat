package net;

import java.awt.Color;

public class User {
	
	private String id;
	private int clientId;
	private String name;
	private int r, g, b, a;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getClientId() {
		return clientId;
	}
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Color getColor() {
		return new Color(r, g, b, a);
	}
	
	public void setColor(int r, int g, int b, int a) {
		this.r =  r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public void setColor(Color color) {
		this.r = color.getRed();
		this.g = color.getGreen();
		this.b = color.getBlue();
		this.a = color.getAlpha();
	}
	
}
