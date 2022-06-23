package net;

import java.net.InetAddress;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.minlog.Log;

import net.packet.PacketDisconnect;
import net.packet.PacketFileDownload;
import net.packet.PacketFileDownloadFailed;
import net.packet.PacketFileDownloadPoint;
import net.packet.PacketFileDownloadRequest;
import net.packet.PacketFileUpload;
import net.packet.PacketFileUploadFailed;
import net.packet.PacketFileUploadPoint;
import net.packet.PacketFileUploadProgress;
import net.packet.PacketGreeting;
import net.packet.PacketMessage;
import net.packet.PacketMessages;
import net.packet.PacketUser;
import net.packet.PacketUsers;

public class Network {
	public static final short NetworkPort = 7707;
	public static final int PACKET_BUFFER_SIZE = 8192 * 128;

	public static interface MessageReceiveCallback {
		public void receive(PacketMessage message);
	}
	
	public static NetClient client;
	public static NetServer server;
	private static boolean stopClient;
	
	static MessageReceiveCallback messageReceiveCallback;

	/***
	 *
	 * @return whether any activity is going at all over the network.
	 */
	public static boolean isNetworkGoing() {
		if (client != null)
			return client.isConnected();
		return server != null;
	}
	
	public static User getUserById(String id) 
	{
		if(isServerStarted())
		{
			for(User user : server.users.values()) {
				if(user.getId().equals(id))
					return user;
			}
		}
		else if(isClientConnected()) {
			return client.users.get(id);
		}
		
		return null;
	}
	
	public static void sendMessage(PacketMessage packet) {
		if(isServerStarted()) {
			server.sendMessage(packet);
		}
		else if(isClientConnected()) {
			client.getCommunication().sendTCP(packet);
		}
	}
	
	public static void sendGreeting(PacketGreeting greeting)
	{
		if(isServerStarted())
		{
			server.addServerUser(greeting.convertToUser());
		}
		else if(isClientConnected()) {
			client.getCommunication().sendTCP(greeting);
		}
	}

	public static void register(Kryo kryo) {
		kryo.register(PacketMessage.class);
		kryo.register(PacketMessage[].class);
		kryo.register(PacketMessages.class);
		kryo.register(User.class);
		kryo.register(User[].class);
		kryo.register(PacketUser.class);
		kryo.register(PacketUsers.class);
		kryo.register(PacketGreeting.class);
		kryo.register(PacketFileUpload.class);
		kryo.register(PacketFileUploadPoint.class);
		kryo.register(PacketFileUploadProgress.class);
		kryo.register(PacketFileUploadFailed.class);
		
		kryo.register(PacketFileDownload.class);
		kryo.register(PacketFileDownloadPoint.class);
		kryo.register(PacketFileDownloadRequest.class);
		kryo.register(PacketFileDownloadFailed.class);
		
		kryo.register(PacketDisconnect.class);
		
		kryo.register(byte[].class);
	}

	public static boolean isServerStarted() {
		return server != null;
	}

	public static boolean isClientConnected() {
		return client != null && stopClient == false && client.isConnected();
	}

	public static interface ClientConnectCallback {
		public void status(boolean connected);
	}
	
	public static void beginClient(final String ip, final ClientConnectCallback callback) {
		Log.set(Log.LEVEL_DEBUG);
		stopClient = false;
		if (client == null)
			client = new NetClient();
		else
			client.stop();
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean connected = client.connect(ip, NetworkPort, NetworkPort);
				callback.status(connected);
				if (connected == false)
					stopClient = true;
			}
		}).start();
	}

	public static interface ServerStartCallback {
		public void serverStartedStatus(boolean started);
	}

	public static void beginServer(final ServerStartCallback callback) {
		Log.set(Log.LEVEL_DEBUG);
		if (server == null)
			server = new NetServer();
		else
			server.stop();

		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean started = server.start(NetworkPort, NetworkPort);
				if (callback != null)
					callback.serverStartedStatus(started);
			}
		}).start();
	}

	public static void stopServer() {
		if (server != null) {
			server.stop();
			server = null;
		}
	}

	public static void stopClient() {
		if (client != null) {
			client.stop();
			client = null;
		}
	}

	
	public static interface ServerListCallback {
		public void onReceivedServerList(List<InetAddress> servers);
		public void onFailed();
	}
	
	public static void scanForServers(short port, int timeoutMillis, final ServerListCallback callback)
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					List<InetAddress> servers = new Client().discoverHosts(port, timeoutMillis);
					if(servers != null)
						callback.onReceivedServerList(servers);
					else callback.onFailed();
				}
				catch(Exception e) {
					e.printStackTrace();
					callback.onFailed();
				}
			}
		}).start();
	}

	public static InetAddress scanForFirstServer(short port, int timeoutMillis) {
		return new Client().discoverHost(port, timeoutMillis);
	}
	
	public static void setMessageReceiveCallback(MessageReceiveCallback callback)
	{
		messageReceiveCallback = callback;
	}

	public static void dispose() {
		if (client != null) {
			client.stop();
			client = null;
		}
		if (server != null) {
			server.stop();
			server = null;
		}
	}
}
