package gui;

import java.awt.Color;
import java.util.Collection;

import net.Network;
import net.Network.ClientConnectCallback;
import net.User;

public class Commands {
	
	public static String getTextFromInput(ChatWindow window, String input) {
		String lowered = input.trim().toLowerCase();
		if(lowered.startsWith("/list")) {
			Collection<User> users = null;
			if(Network.isServerStarted())
				users = Network.server.users.values();
			else if(Network.isClientConnected())
				users = Network.client.users.values();
			if(users != null)
			{
				String result = "There are " + users.size() + " users connected:<br /><table style=\"margin-top: 10px;\"><tr><th>Name</th><th>ID</th></tr>";
				for(User user : users) 
				{
					Color c = user.getColor();
					result += "<tr>"
								+ "<td style=\"color=rgb(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")\">" + user.getName() + "</td>"
								+ "<td>" + user.getId() + "</td>"
							+ "</tr>";
				}
				result += "</table>";
				return result;
			}
			
			return "You are not connected";
		}
		else if(lowered.startsWith("/flip")) {
			boolean isTrue = Math.random() >= 0.5;
			if(isTrue)
				return "<span style=\"color: #aaffaa\">heads</span>";
			else 
				return "<span style=\"color: #ffaaaa\">tails</span>";
		}
		else if(lowered.startsWith("/connect"))
		{
			try {
				String ip = lowered.split(" ")[1];
				if(Network.isServerStarted())
					Network.stopServer();
				if(Network.isClientConnected())
					Network.stopClient();
				window.clearChat();
				window.appendText("<span style=\"color: #ff66aa\">Connecting...</span>");
				Network.beginClient(ip, new ClientConnectCallback() {
					
					@Override
					public void status(boolean connected) {
						if(connected)
						{
							window.appendText("<span style=\"color: #aaffaa\">Connected!</span>");
							window.doNetStuff();
						}
						else 
						{
							window.appendText("<span style=\"color: #ffaaaa\">Failed to connect.</span>");							
						}
					}
				});
			}
			catch(Exception e) 
			{
				e.printStackTrace();
			}
		}
		return replaceUrlsWithLinks(input);
	}
	
	
	
	private static String replaceUrlsWithLinks(String message) {
		int httpIndex = -1;
		int offset = 0;
		int httpsIndex = -1;
		String offsetString = message;
		do {
			httpIndex = offsetString.indexOf("http://");
			httpsIndex = offsetString.indexOf("https://");
			
			int indexToGoWith = -1; 
			if(httpIndex >= 0 && (httpIndex < httpsIndex || httpsIndex < 0))
			{
				indexToGoWith = httpIndex;
			}
			else if(httpsIndex >= 0 && (httpsIndex <= httpIndex || httpIndex < 0)) 
			{
				indexToGoWith = httpsIndex;
			}
			
			if(indexToGoWith >= 0)
			{
				indexToGoWith += offset;
				String url = message.substring(indexToGoWith);
				
				int nextSpaceIndex = url.indexOf(" ");
				
				if(nextSpaceIndex >= 0)
					url = url.substring(0, url.indexOf(" ")); //Stop the url at the first space
				
				String replacement = "<a href=\"" + url + "\">" + url + "</a>";
				
				
				String before = message.substring(0, indexToGoWith);
				String after = message.substring(indexToGoWith + url.length());
				
				message = before + replacement + after;
				offsetString = after;
				offset = before.length() + replacement.length();
			}
		} while(httpIndex >= 0 || httpsIndex >= 0);
		return message;
	}
	
}
