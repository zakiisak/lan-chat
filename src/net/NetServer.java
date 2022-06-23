package net;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import net.packet.PacketDisconnect;
import net.packet.PacketFileDownload;
import net.packet.PacketFileDownloadFailed;
import net.packet.PacketFileDownloadPoint;
import net.packet.PacketFileDownloadRequest;
import net.packet.PacketFileUpload;
import net.packet.PacketFileUploadPoint;
import net.packet.PacketFileUploadProgress;
import net.packet.PacketGreeting;
import net.packet.PacketMessage;
import net.packet.PacketMessages;
import net.packet.PacketUser;
import net.packet.PacketUsers;

public class NetServer extends Listener {
	
	public NetServer()
	{
	}

	public Server server;
	
	public boolean start(int tcpPort, int udpPort) {
		this.server = new Server(Network.PACKET_BUFFER_SIZE, Network.PACKET_BUFFER_SIZE);
		Network.register(this.server.getKryo());
		try {
			this.server.bind(tcpPort, udpPort);
		} catch (Exception e) {
			System.out.println(
					"Unable to start server on ports: [tcpPort=" + tcpPort
							+ ";udpPort=" + udpPort + "] - \n" + e.getMessage());
			return false;
		}
		this.server.start();
		this.server.addListener(this);
		return true;
	}
	
	public Map<Integer, User> users = new ConcurrentHashMap<Integer, User>();
	
	private List<PacketMessage> messages = Collections.synchronizedList(new ArrayList<PacketMessage>());
	private Map<String, UploadedFile> uploadedFiles = new ConcurrentHashMap<String, UploadedFile>();
	private Map<String, FileWritingService> fileUploadServices = new ConcurrentHashMap<String, FileWritingService>();
	
	private Map<Integer, FileReadingService> fileReadingServices = new ConcurrentHashMap<Integer, FileReadingService>();
	
	public boolean isClientConnected(int clientId) {
		return users.containsKey(clientId);
	}

	
	public void connected(Connection c) {
	}
	
	private void sendAllMessagesToUser(Connection c) {
		PacketMessage[] messages = new PacketMessage[this.messages.size()];
		for(int i = 0; i < messages.length; i++)
			messages[i] = this.messages.get(i);
		PacketMessages packet = new PacketMessages();
		packet.setMessages(messages);
		c.sendTCP(packet);
	}
	
	public void sendMessage(PacketMessage message) 
	{
		messages.add(message);
		if(Network.messageReceiveCallback != null)
			Network.messageReceiveCallback.receive(message);
		server.sendToAllTCP(message);
	}
	
	private void sendAllUsersToUser(Connection c) {
		try {
			Collection<User> collection = this.users.values();
			User[] users = new User[collection.size()];
			int index = 0;
			for(User user : collection) {
				users[index] = user;
				index++;
			}
			PacketUsers packet = new PacketUsers();
			packet.setUsers(users);
			c.sendTCP(packet);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addServerUser(User user) {
		users.put(-1, user);
	}

	public void received(Connection c, Object o) {
		if(o instanceof PacketGreeting)
		{
			PacketGreeting greeting = (PacketGreeting) o;
			User user = greeting.convertToUser();
			user.setClientId(c.getID());
			users.put(c.getID(), user);
			
			//
			sendAllUsersToUser(c);
			sendAllMessagesToUser(c);
			PacketUser packetNewUser = new PacketUser();
			packetNewUser.setUser(user);
			server.sendToAllTCP(packetNewUser);
			
			PacketMessage messageWelcome = new PacketMessage();
			Color color = user.getColor();
			messageWelcome.setMessage("Welcome <span style=\"color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");\">" + user.getName() + "</span>!");
			messageWelcome.setTime(System.currentTimeMillis());
			messageWelcome.setAuthorId("");
			c.sendTCP(messageWelcome);
			
			//Send to everyone else that the given person has just joined!
			
			PacketMessage broadcastMessage = new PacketMessage();
			broadcastMessage.setMessage("<span style=\"color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");\">" + user.getName() + "</span> has connected!");
			broadcastMessage.setTime(System.currentTimeMillis());
			broadcastMessage.setAuthorId("");
			server.sendToAllExceptTCP(c.getID(), broadcastMessage);
			if(Network.messageReceiveCallback != null)
				Network.messageReceiveCallback.receive(broadcastMessage);
		}
		else if(o instanceof PacketMessage) {
			PacketMessage message = (PacketMessage) o;
			sendMessage(message);
		}
		else if(o instanceof PacketFileUploadPoint) {
			PacketFileUploadPoint uploadPoint = (PacketFileUploadPoint) o;
			if(uploadPoint.isBegin())
			{
				String uniqueId = UUID.randomUUID().toString();
				uploadPoint.setFileId(uniqueId);
				File file = new File(uniqueId + uploadPoint.getExtension());
				uploadedFiles.put(uniqueId, new UploadedFile(file, uploadPoint.getReadableName(), uploadPoint.getExtension()));
				fileUploadServices.put(uniqueId, new FileWritingService(file));
				
				
				c.sendTCP(uploadPoint);
			}
			//The file is finished
			else {
				//Stop the file writing and flush the data to disk
				fileUploadServices.get(uploadPoint.getFileId()).stop();
				
				//Also send a file message now to everyone
				
				PacketMessage fileMessage = new PacketMessage();
				User user = users.get(c.getID());
				fileMessage.setAuthorId(user.getId());
				fileMessage.setFile(true);
				fileMessage.setFileLink(uploadPoint.getFileId());
				fileMessage.setTime(System.currentTimeMillis());
				fileMessage.setMessage("" + uploadPoint.getReadableName());
				server.sendToAllTCP(fileMessage);
			}
		}
		else if(o instanceof PacketFileUpload) {
			PacketFileUpload upload = (PacketFileUpload) o;
			PacketFileUploadProgress progressPacket = new PacketFileUploadProgress();
			
			UploadedFile file = uploadedFiles.get(upload.getFileLink()); 
			file.setFileSize(file.getFileSize() + upload.getData().length);
			progressPacket.setFileId(upload.getFileLink());
			progressPacket.setBytesWritten(upload.getData().length);
			
			//Add data for writing
			fileUploadServices.get(upload.getFileLink()).addBytesToWrite(upload.getData());
			
			//Send progress back
			c.sendTCP(progressPacket);
		}
		else if(o instanceof PacketFileDownloadRequest) 
		{
			PacketFileDownloadRequest request = (PacketFileDownloadRequest) o;
			if(uploadedFiles.containsKey(request.getFileId())) {
				
				final Connection downloader = c;
				final String fileId = request.getFileId();
				final UploadedFile uploadedFileInfo = uploadedFiles.get(request.getFileId());
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						try {
							FileReadingService service = new FileReadingService(uploadedFileInfo.getFile());
							
							PacketFileDownloadPoint beginPacket = new PacketFileDownloadPoint();
							beginPacket.setBegin(true);
							beginPacket.setExtension(uploadedFileInfo.getExtension());
							beginPacket.setFileId(fileId);
							beginPacket.setFileSize(uploadedFileInfo.getFileSize());
							beginPacket.setReadableName(uploadedFileInfo.getReadableName());
							downloader.sendTCP(beginPacket);
							
							byte[] buffer = new byte[8192];
							int bytesRead = 0;
							while((bytesRead = service.getNextPart(buffer)) != -1) {
								byte[] copy = new byte[bytesRead];
								for(int i = 0; i < bytesRead; i++)
									copy[i] = buffer[i];
								
								PacketFileDownload downloadPacket = new PacketFileDownload();
								downloadPacket.setData(copy);
								downloadPacket.setFileId(fileId);
								downloader.sendTCP(downloadPacket);
							}
							
							
							PacketFileDownloadPoint endPacket = new PacketFileDownloadPoint();
							endPacket.setBegin(false);
							endPacket.setExtension(uploadedFileInfo.getExtension());
							endPacket.setFileId(fileId);
							endPacket.setFileSize(uploadedFileInfo.getFileSize());
							endPacket.setReadableName(uploadedFileInfo.getReadableName());
							downloader.sendTCP(endPacket);
							
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							
							PacketFileDownloadFailed failPacket = new PacketFileDownloadFailed();
							failPacket.setFileId(fileId);
							downloader.sendTCP(failPacket);
						}
						
					}
				}).start();
			}
		}
	}

	public void disconnected(Connection c) {
		
		User user = users.get(c.getID());
		if(user != null)
		{
			PacketMessage broadcastMessage = new PacketMessage();
			Color color = user.getColor();
			broadcastMessage.setMessage("<span style=\"color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");\">" + user.getName() + "</span> has left.");
			broadcastMessage.setTime(System.currentTimeMillis());
			broadcastMessage.setAuthorId("");
			server.sendToAllExceptTCP(c.getID(), broadcastMessage);
			if(Network.messageReceiveCallback != null)
				Network.messageReceiveCallback.receive(broadcastMessage);
			users.remove(c.getID());
			
			PacketDisconnect disconnectPacket = new PacketDisconnect();
			disconnectPacket.setUserId(user.getId());
			server.sendToAllExceptTCP(c.getID(), disconnectPacket);
		}
		
	}
	
	public int getUserCount() {
		return users.size();
	}
	
	public Server getCommunication() {
		return server;
	}

	public void stop() {
		if(server != null) 
		{
			server.stop();
			server = null;
		}
	}
	
}
