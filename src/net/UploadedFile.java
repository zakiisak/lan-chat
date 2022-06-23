package net;

import java.io.File;

public class UploadedFile {
	
	private File file;
	private String readableName;
	private long fileSize;
	private String extension;
	
	public UploadedFile() {}
	
	public UploadedFile(File file, String readableName, String extension) {
		this.file = file;
		this.readableName = readableName;
		this.extension = extension;
	}
	
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getReadableName() {
		return readableName;
	}
	public void setReadableName(String readableName) {
		this.readableName = readableName;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	
	
	
	
}
