package net.packet;

public class PacketFileUploadPoint {
	
	private String fileId;
	private String extension;
	private String readableName;
	
	//Tells whether this is the beginning of a file upload or the end of the file upload
	private boolean begin;
	
	
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public boolean isBegin() {
		return begin;
	}
	public void setBegin(boolean begin) {
		this.begin = begin;
	}
	public String getReadableName() {
		return readableName;
	}
	public void setReadableName(String readableName) {
		this.readableName = readableName;
	}
	
	
	
	
	
	
	
}
