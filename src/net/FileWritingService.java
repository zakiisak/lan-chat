package net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class FileWritingService implements Runnable {
	
	private File fileToWriteTo;
	private Thread thread;
	private boolean running = false;

	private Queue<byte[]> bytesToWrite = new ConcurrentLinkedDeque<byte[]>();
	
	public FileWritingService(File fileToWriteTo) {
		this.fileToWriteTo = fileToWriteTo;
		running = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public void addBytesToWrite(byte[] bytes) {
		if(running)
			bytesToWrite.add(bytes);
	}
	
	@Override
	public void run() {
		
		try {
			FileOutputStream output = new FileOutputStream(fileToWriteTo);
			while(running || bytesToWrite.size() > 0)
			{
				while(bytesToWrite.size() > 0) {
					byte[] data = bytesToWrite.poll();
					output.write(data);
				}
				
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			output.flush();
			output.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			running = false;
			e1.printStackTrace();
		}
	}
	
	public void stop() {
		this.running = false;
	}
	
}
