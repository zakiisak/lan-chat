package gui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.basic.BasicScrollBarUI;

import main.Settings;
import net.Network;
import net.Network.MessageReceiveCallback;
import net.User;
import net.packet.PacketGreeting;
import net.packet.PacketMessage;

public class ChatWindow extends JFrame implements ComponentListener, MessageReceiveCallback, WindowFocusListener {
	private static final long serialVersionUID = 1L;

	private JScrollPane scrollPane;
	public JEditorPane pane;
	private PlaceholderTextField field;
	private TrayIcon trayIcon; 
	
	private boolean focused = true;
	
	private long lastGotMessage = 0;
	
	public ChatWindow() throws IOException {
		setSize(640, 480);
		setResizable(true);
		setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("logo_dark_medium.png")));
		setTitle("LAN CHAT");
		//Read image from jar file through the classpath system
		//setIconImage(ImageIO.read);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setLocationRelativeTo(null);
		
		addWindowFocusListener(this);
		this.setLayout(null);
		
		Color bgc = new Color(32, 32, 48, 255);
		
		addComponentListener(this);
		setBackground(bgc);
		
		getContentPane().setBackground(bgc);
		pane = new JEditorPane();
		pane.setBackground(bgc);
		pane.setContentType("text/html");
		pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		pane.setFont(new Font("Consolas", 0, 14));
		pane.repaint();
		
		String serverText = "";
		if(Network.isServerStarted())
		{
			serverText = "<h1>Welcome! You are the server ;)</h1>";
		}
		
		pane.setText(HtmlTemplates.styles + serverText);
		pane.addHyperlinkListener(new HyperlinkListener() {
			
			@Override
			public void hyperlinkUpdate(HyperlinkEvent event) {
				try {
					
					InputEvent e = event.getInputEvent();
					if(e instanceof MouseEvent)
					{
						MouseEvent me = (MouseEvent) e;
						if(me.getButton() == MouseEvent.BUTTON1)
						{
							Desktop.getDesktop().browse(event.getURL().toURI());
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		scrollPane = new JScrollPane(pane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
	        public void adjustmentValueChanged(AdjustmentEvent e) {
	        	if(System.currentTimeMillis() - lastGotMessage < 500)
	        		e.getAdjustable().setValue(e.getAdjustable().getMaximum());  
	        	else
	        		e.getAdjustable().setValue(e.getValue());  
	        }
	    });
		
		scrollPane.setBackground(bgc);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setBackground(bgc);
		scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
		    @Override
		    protected void configureScrollBarColors() {
				Color c = new Color(bgc.getRed(), bgc.getGreen(), bgc.getBlue(), bgc.getAlpha()).brighter().brighter();
		        this.thumbColor = c;
		    }
		});
		
		pane.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent var1) {
				field.requestFocus();
			}
			
			@Override
			public void keyReleased(KeyEvent var1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent var1) {
				field.requestFocus();
			}
		});
		
		pane.setBorder(null);
		pane.setEditable(false);
		add(scrollPane);
		field = new PlaceholderTextField();
		field.setForeground(new Color(220, 220, 220, 255));
		field.setCaretColor(new Color(220, 200, 200, 255));
		field.setPlaceholder("Text");
		field.setFont(new Font("Consolas", 0, 14));
		field.addKeyListener(new InputController(field, new Runnable() {
			
			@Override
			public void run() {
				String message = field.getText();
				message = Commands.getTextFromInput(ChatWindow.this, message);
				if(message != null && message.length() > 0)
				{
					PacketMessage packet = new PacketMessage();
					packet.setMessage(message);
					packet.setAuthorId(Settings.id);
					packet.setTime(System.currentTimeMillis());
					Network.sendMessage(packet);
					field.setText("");					
				}
				
			}
		}));
		field.setBorder(new Border() {
			
			@Override
			public void paintBorder(Component var1, Graphics g, int x, int y, int width, int height) {
				Color borderColor = new Color(bgc.getRed(), bgc.getGreen(), bgc.getBlue(), bgc.getAlpha()).brighter().brighter();
				g.setColor(borderColor);
				g.fillRect(x, y + height - 4, width, 4);
			}
			
			@Override
			public boolean isBorderOpaque() {
				return false;
			}
			
			@Override
			public Insets getBorderInsets(Component var1) {
				return new Insets(0, 0, 0, 0);
			}
		});
		field.setBackground(bgc);
		field.requestFocus();
		add(field);
		resizeElements();
		
		initSystemTray();
		doNetStuff();
		setVisible(true);
		field.requestFocus();
		
	}
	
	public void clearChat() {
		pane.setText(HtmlTemplates.styles);
	}
	
	public void appendText(String text)
	{
		String currentText = pane.getText();
		int bodyEnd = currentText.indexOf("</body>");
		pane.setText(currentText.substring(0, bodyEnd) + text + currentText.substring(bodyEnd));
	}
	
	private void initSystemTray() {
		final PopupMenu popup = new PopupMenu();
		final SystemTray tray = SystemTray.getSystemTray();
		Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("logo_dark_small.png"));
		//ImageIcon imageIcon = new ImageIcon(getClass().getResource("logo_dark.png"));
		trayIcon = new TrayIcon(image);
		trayIcon.setImageAutoSize(true);
		MenuItem showItem = new MenuItem("Open");
		MenuItem aboutItem = new MenuItem("About");
		MenuItem quitItem = new MenuItem("Quit");
		showItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				ChatWindow.this.setVisible(true);
			}
		});
		aboutItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				JOptionPane.showMessageDialog(null, "LAN CHAT was developed by Isak Østergaard on 23-06-2022 in relation with an upcoming LAN party the day after.\nTo make it more easy to share information etc, and for fun, he developed this application.", "LAN Chat - About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		quitItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				Network.stopClient();
				Network.stopServer();
				System.exit(0);
			}
		});
		popup.add(showItem);
		popup.add(aboutItem);
		popup.add(quitItem);
		trayIcon.setToolTip("LAN CHAT");
		trayIcon.setPopupMenu(popup);
		trayIcon.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				System.out.println("das");
			}
		});
		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			e.printStackTrace();
			//If it fails to add the tray icon, we might as well exit the application by default if pressing the x instead of hiding it.
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		
	}
	
	public void doNetStuff() {
		Network.setMessageReceiveCallback(this);
		PacketGreeting greeting = new PacketGreeting();
		Color c = Settings.color;
		greeting.setR(c.getRed());
		greeting.setG(c.getGreen());
		greeting.setB(c.getBlue());
		greeting.setA(c.getAlpha());
		greeting.setName(Settings.name);
		greeting.setUserId(Settings.id);
		Network.sendGreeting(greeting);
	}
	
	private void resizeElements() {
		pane.setBounds(0, 0, getWidth() - 20, getHeight() - 80);
		scrollPane.setBounds(0, 0, getWidth() - 20, getHeight() - 80);
		field.setBounds(8, getHeight() - 30 - 40, getWidth() - 28, 30);

	}

	@Override
	public void componentHidden(ComponentEvent var1) {}

	@Override
	public void componentMoved(ComponentEvent var1) {}

	@Override
	public void componentResized(ComponentEvent var1) {
		resizeElements();
	}

	@Override
	public void componentShown(ComponentEvent var1) {}

	private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); 
	
	@Override
	public void receive(PacketMessage message) {
		
		String output = HtmlTemplates.normal_message;
		output = output.replace("{time}", formatter.format(message.getTime()));
		
		User author = Network.getUserById(message.getAuthorId());
		String authorName = "";
		if(author != null)
		{
			Color c = author.getColor();
			authorName = "<span style=\"color: rgb(" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ")\">" + author.getName() + "</span>";
		}
		output = output.replace("{author}", authorName);
		output = output.replace("{text}", message.getMessage());
		
		lastGotMessage = System.currentTimeMillis();
		
		appendText(output);
		
		if(focused == false) 
		{
			String escapedMessage = message.getMessage().replaceAll("\\<[^>]*>","");
			String title = "";
			String name = (author != null ? author.getName() : authorName);
			if(name.length() == 0)
			{
				title = "Info";
			}
			else 
			{
				title = name + " sent a message";
			}
			trayIcon.displayMessage(title, escapedMessage, MessageType.INFO);
		}
		
	}


	@Override
	public void windowGainedFocus(WindowEvent var1) {
		this.focused = true;
	}

	@Override
	public void windowLostFocus(WindowEvent var1) {
		this.focused = false;
		System.out.println("lost focus");
	}

	
}
