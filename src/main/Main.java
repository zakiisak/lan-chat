package main;

import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import gui.ChatWindow;
import gui.LoadingWindow;
import net.Network;
import net.Network.ClientConnectCallback;
import net.Network.ServerStartCallback;

public class Main {
	
	private static LoadingWindow loadingWindow;
	
	private static void startChatWindow() {
		try {
			loadingWindow.dispatchEvent(new WindowEvent(loadingWindow, WindowEvent.WINDOW_CLOSING));
			new ChatWindow();
		} catch (IOException e) {
			e.printStackTrace();
			Network.stopClient();
			Network.stopServer();
			System.exit(1);
		}
	}
	
	private static void proceed(String ip) 
	{
		if(ip == null) {
			Network.beginServer(new ServerStartCallback() {
				
				@Override
				public void serverStartedStatus(boolean started) {
					if(started == false)
					{
						JOptionPane.showMessageDialog(null, "Failed to start server!", "Server failed to start", JOptionPane.ERROR_MESSAGE);
						System.exit(1);
					}
					else {
						startChatWindow();
					}
				}
			});
		}
		else {
			Network.beginClient(ip, new ClientConnectCallback() {
				
				@Override
				public void status(boolean connected) {
					if(connected) {
						startChatWindow();
					}
					else {
						JOptionPane.showMessageDialog(null, "Failed to connect to server: " + ip, "Connection Failed!", JOptionPane.ERROR_MESSAGE);
						System.exit(1);
					}
				}
			});
		}
	}
	
	public static void main(String[] args) {
		Settings.load();
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
				
				loadingWindow = new LoadingWindow();
				String ip = args.length > 0 ? args[1] : null;
				if(ip == null)
				{
					new Thread(new Runnable() {
						public void run() {
							InetAddress firstServer = Network.scanForFirstServer(Network.NetworkPort, 5000); //TODO FIX TO 5000
							proceed(firstServer != null ? firstServer.getHostAddress() : ip);
						}
					}).start();
				}
				
			}
		});
	}
	
}
