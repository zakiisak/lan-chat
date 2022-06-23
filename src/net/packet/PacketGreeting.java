package net.packet;

import net.User;

public class PacketGreeting {
	
	private String userId;
	private String name;
	//Used for coloring the name
	private int r, g, b, a;
	
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getR() {
		return r;
	}
	public void setR(int r) {
		this.r = r;
	}
	public int getG() {
		return g;
	}
	public void setG(int g) {
		this.g = g;
	}
	public int getB() {
		return b;
	}
	public void setB(int b) {
		this.b = b;
	}
	public int getA() {
		return a;
	}
	public void setA(int a) {
		this.a = a;
	}
	
	public User convertToUser() {
		User user = new User();
		user.setColor(r, g, b, a);
		user.setId(userId);
		user.setName(name);
		return user;
	}
	
	
	
	
	
	
}
