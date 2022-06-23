package net;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import net.packet.PacketDisconnect;
import net.packet.PacketFileDownload;
import net.packet.PacketFileDownloadPoint;
import net.packet.PacketFileUploadProgress;
import net.packet.PacketMessage;
import net.packet.PacketMessages;
import net.packet.PacketUser;
import net.packet.PacketUsers;

public class NetClient extends Listener {
	private boolean loggedIn = false;
	private Client client;

	private boolean stoppingIntentionally;
	public Map<String, User> users = new ConcurrentHashMap<String, User>();
	

	public NetClient()
	{
	}

	public void stop()
	{
		if(client != null) 
		{
			stoppingIntentionally = true;
			client.stop();
			client = null;
		}
	}
	
	public void connected(Connection c) {
		loggedIn = true;
	}
	
	private Map<String, FileWritingService> writingServices = new ConcurrentHashMap<String, FileWritingService>();
	private Random random = new Random();

	public void received(Connection c, Object o) {
		if(o instanceof PacketUser) {
			PacketUser packet = (PacketUser) o;
			User newUser = packet.getUser();
			users.put(newUser.getId(), newUser);
		}
		else if(o instanceof PacketUsers) 
		{
			PacketUsers packetUsers = (PacketUsers) o;
			for(User u : packetUsers.getUsers())
			{
				users.put(u.getId(), u);
			}
		}
		else if(o instanceof PacketMessage) {
			PacketMessage message = (PacketMessage) o;
			if(Network.messageReceiveCallback != null)
				Network.messageReceiveCallback.receive(message);
		}
		else if(o instanceof PacketMessages) 
		{
			PacketMessages messages = (PacketMessages) o;
			if(Network.messageReceiveCallback != null)
			{
				for(PacketMessage packet : messages.getMessages())
					Network.messageReceiveCallback.receive(packet);
			}
		}
		else if(o instanceof PacketDisconnect) 
		{
			PacketDisconnect packet = (PacketDisconnect) o;
			if(users.containsKey(packet.getUserId()))
			{
				users.remove(packet.getUserId());
			}
		}
		else if(o instanceof PacketFileUploadProgress) {
			PacketFileUploadProgress progress = (PacketFileUploadProgress) o;
			
		}
		else if(o instanceof PacketFileDownloadPoint) {
			PacketFileDownloadPoint downloadPoint = (PacketFileDownloadPoint) o;
			if(downloadPoint.isBegin()) {
				File file = new File(downloadPoint.getReadableName() + downloadPoint.getExtension());
				while(file.exists()) 
				{
					file = new File(downloadPoint.getReadableName() + "_" + random.nextInt(1000000) + downloadPoint.getExtension());
				}
				writingServices.put(downloadPoint.getFileId(), new FileWritingService(file));
			}
			else {
				FileWritingService service = writingServices.get(downloadPoint.getFileId());
				if(service != null) {
					service.stop();
				}
				else {
					//File download error
					System.err.println("[client] An error occurred during end point download packet");
				}
			}
		}
		else if(o instanceof PacketFileDownload) 
		{
			PacketFileDownload downloadPacket = (PacketFileDownload) o;
			FileWritingService service = writingServices.get(downloadPacket.getFileId());
			if(service != null) {
				service.addBytesToWrite(downloadPacket.getData());
			}
			else {
				//File download error
				System.err.println("[client] An error occurred during download packet");
			}
		}
	}
	
	public void disconnected(Connection c) {}
	
	public int getUserCount() {
		return users.size();
	}
	
	public boolean connect(String ip, int tcpPort, int udpPort) {
		int maxSize = Network.PACKET_BUFFER_SIZE;
		this.client = new Client(maxSize, maxSize);
		client.setTimeout(20000);
		Network.register(this.client.getKryo());
		this.client.addListener(this);
		this.client.start();
		
		try {
			this.client.connect(5000, ip, tcpPort, udpPort);
		}
		catch(Exception e) {
			this.client = null;
			return false;
		}
		return true;
	}
	
	public InetAddress findFirstServer(short udpPort, int waitingResponse)
	{
		if(this.client == null) return null;
		return client.discoverHost(udpPort, waitingResponse);
	}
	
	public List<InetAddress> scanForServers(short udpPort, int waitingResponse)
	{
		if(this.client == null) return null;
		return client.discoverHosts(udpPort, waitingResponse);
	}
	
	public Client getCommunication()
	{
		return client;
	}
	
	public boolean isConnected()
	{
		if(this.client == null) loggedIn = false;
		return loggedIn;
	}

	public int getNetId()
	{
		return client.getID();
	}
	
}
