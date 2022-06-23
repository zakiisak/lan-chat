package net.packet;

public class PacketFileUpload {
	
	//The path to the file - also used as id
	private String fileLink;
	private byte[] data;
	
	public String getFileLink() {
		return fileLink;
	}
	public void setFileLink(String fileLink) {
		this.fileLink = fileLink;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	
	
	
}
