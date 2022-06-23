package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class LoadingWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private Image bigLogo;
	
	public LoadingWindow() {
		setSize(256, 256);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setLocationRelativeTo(null);
		try {
			bigLogo = Toolkit.getDefaultToolkit().createImage(getClass().getResource("logo_dark.png"));
			setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("logo_dark_medium.png")));
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		setUndecorated(true);
		
		setAlwaysOnTop(true);
		setBackground(new Color(0, 0, 0, 0));
		setTitle("LAN CHAT - Loading");
		setVisible(true);
		
	}
	
	final int baseY = 195;
	
	int[] xPoints = new int[] {16, 48, 64};
	int[] yPoints = new int[] {baseY - 16, baseY - 48, baseY - 32};
	
	int[] xPointsLarge = new int[] {16 - 12, 48, 64 + 4};
	int[] yPointsLarge = new int[] {baseY - 16 + 8, baseY - 48 - 4, baseY - 32 + 4};
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		
		/*
		g.setColor(Color.BLACK);
		g.fillOval(28, (int) (getHeight() / 4) - 4, getWidth() - 56, getHeight() - getHeight() / 2 + 8);
		
		g.fillPolygon(xPointsLarge, yPointsLarge, yPointsLarge.length);
		
		final int tileSize = 32;
		int s = this.getWidth() / tileSize;
		g.setColor(new Color(255, 255, 255, 255));
		g.fillPolygon(xPoints, yPoints, xPoints.length);
		g.fillOval(32, (int) (getHeight() / 4), getWidth() - 64, getHeight() - getHeight() / 2);
		*/
		
		g.setColor(Color.WHITE);
		g.drawImage(bigLogo, 0, 0, getWidth(), getWidth(), null);
		
		
		
	}
	
}
