package net.packet;

public class PacketFileUploadProgress {
	
	private String fileId;
	private int bytesWritten;
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public int getBytesWritten() {
		return bytesWritten;
	}
	public void setBytesWritten(int bytesWritten) {
		this.bytesWritten = bytesWritten;
	}
	
}
