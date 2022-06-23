package gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

public class InputController implements KeyListener {

	private final Runnable action;
	private final JTextField field;
	private List<String> previousMessages = new ArrayList<String>();
	private int currentIndex = 0;
	
	public InputController(JTextField field, Runnable action)
	{
		this.field = field;
		this.action = action;
	}
	
	@Override
	public void keyPressed(KeyEvent var1) {
		if(var1.getKeyCode() == KeyEvent.VK_UP) 
		{
			if(previousMessages.size() > 0)
			{
				currentIndex--;
				if(currentIndex < 0)
					currentIndex = 0;
				field.setText(previousMessages.get(currentIndex));
			}
		}
		else if(var1.getKeyCode() == KeyEvent.VK_DOWN) 
		{
			if(previousMessages.size() > 0) 
			{
				currentIndex++;
				if(currentIndex > previousMessages.size())
					currentIndex = previousMessages.size();
				if(currentIndex >= previousMessages.size())
					field.setText("");
				else field.setText(previousMessages.get(currentIndex));				
			}
		}
		else if(var1.getKeyCode() == KeyEvent.VK_ENTER)
		{
			if(field.getText().trim().length() > 0)
			{
				if(previousMessages.size() == 0 || previousMessages.get(previousMessages.size() - 1).equals(field.getText()) == false)
				{
					previousMessages.add(field.getText());
					currentIndex = previousMessages.size();			
				}
				currentIndex = previousMessages.size();
				this.action.run();
			}
		}
		
	}

	@Override
	public void keyReleased(KeyEvent var1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent var1) {
		// TODO Auto-generated method stub
		
	}

}
