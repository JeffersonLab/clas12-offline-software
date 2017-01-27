package adapter3D;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.jogamp.newt.event.KeyEvent;

import bCNU3D.Panel3D;

public class KeyBindings3D {

	
	public KeyBindings3D(JComponent panel) {
		InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = panel.getActionMap();
		

		inputMap.put(KeyStroke.getKeyStroke("W"),"forward");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0),"backward");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LeftArrow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UpArrow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DownArrow");

		actionMap.put("forward", new KeyAction("forward"));
		actionMap.put("backward", new KeyAction("backward"));
		actionMap.put("Enter", new KeyAction("Enter"));
		actionMap.put("right", new KeyAction("right"));
		actionMap.put("LeftArrow", new KeyAction("LeftArrow"));
		actionMap.put("UpArrow", new KeyAction("UpArrow"));
		actionMap.put("DownArrow", new KeyAction("DownArrow"));	
	}

	
	public class KeyAction extends AbstractAction {

	    private String cmd;

	    public KeyAction(String cmd) {
	        this.cmd = cmd;
	        putValue(Action.NAME, cmd);
	        putValue(ACTION_COMMAND_KEY, "Command: " + cmd);
	    }

	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	System.err.println("KEY BINDINGS " + e.getActionCommand());
	        if (cmd.equalsIgnoreCase("LeftArrow")) {
	            System.out.println("The left arrow was pressed!");
	        } else if (cmd.equalsIgnoreCase("RightArrow")) {
	            System.out.println("The right arrow was pressed!");
	        } else if (cmd.equalsIgnoreCase("UpArrow")) {
	            System.out.println("The up arrow was pressed!");
	        } else if (cmd.equalsIgnoreCase("DownArrow")) {
	            System.out.println("The down arrow was pressed!");
	        }
	    }
	}
}
