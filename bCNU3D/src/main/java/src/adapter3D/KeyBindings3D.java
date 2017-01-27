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

	private Panel3D _panel3D;
	private int _factor = 1;
	private static final float DTHETA = 2f; // degrees

	
	public KeyBindings3D(Panel3D panel) {
		_panel3D = panel;
		InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = panel.getActionMap();
		
		
		//the arrow keys
		inputMap.put(KeyStroke.getKeyStroke("UP"), "up");
		inputMap.put(KeyStroke.getKeyStroke("DOWN"), "down");
		inputMap.put(KeyStroke.getKeyStroke("LEFT"), "left");
		inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "right");


		//other actions
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0),"up");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0),"down");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0),"left");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0),"right");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0),"x");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0),"y");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0),"z");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0),"1");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0),"2");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0),"3");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0),"4");
		

		actionMap.put("up", new KeyAction("up"));
		actionMap.put("down", new KeyAction("down"));
		actionMap.put("left", new KeyAction("left"));
		actionMap.put("right", new KeyAction("right"));


		actionMap.put("x", new KeyAction("x"));
		actionMap.put("y", new KeyAction("y"));
		actionMap.put("z", new KeyAction("z"));
		actionMap.put("1", new KeyAction("1"));
		actionMap.put("2", new KeyAction("2"));
		actionMap.put("3", new KeyAction("3"));
		actionMap.put("4", new KeyAction("4"));
	}

	
	public class KeyAction extends AbstractAction {

//	    private String cmd;

	    public KeyAction(String cmd) {
//	        this.cmd = cmd;
	        putValue(Action.NAME, cmd);
	        putValue(ACTION_COMMAND_KEY, cmd);
	    }

	    @Override
	    public void actionPerformed(ActionEvent e) {
	//    	System.err.println("KEY BINDINGS " + e.getActionCommand());
	    	
	    	String command = e.getActionCommand();
	    	if ("up".equals(command)) {
				float dz = _panel3D.getZStep();
				_panel3D.deltaY(_factor * dz);
				_panel3D.refresh();
	    	}
	    	else if ("down".equals(command)) {
				float dz = -_panel3D.getZStep();
				_panel3D.deltaZ(_factor * dz);
				_panel3D.refresh();
	    	}
	    	else if ("left".equals(command)) {
				float dz = -_panel3D.getZStep();
				_panel3D.deltaX(_factor * dz);
				_panel3D.refresh();
	    	}
	    	else if ("right".equals(command)) {
				float dz = _panel3D.getZStep();
				_panel3D.deltaX(_factor * dz);
				_panel3D.refresh();
	    	}
	    	else if ("x".equals(command)) {
				_panel3D.setRotationX(_panel3D.getRotationX() + _factor * DTHETA);
				_panel3D.refresh();
	    	}
	    	else if ("y".equals(command)) {
				_panel3D.setRotationY(_panel3D.getRotationY() + _factor * DTHETA);
				_panel3D.refresh();
	    	}
	    	else if ("z".equals(command)) {
				_panel3D.setRotationZ(_panel3D.getRotationZ() + _factor * DTHETA);
				_panel3D.refresh();
	    	}
	    	else if ("1".equals(command)) {
				_panel3D.setRotationX(180f);
				_panel3D.setRotationY(90f);
				_panel3D.setRotationZ(0f);
				_panel3D.refresh();
	    	}
	    	else if ("2".equals(command)) {
				_panel3D.setRotationX(90f);
				_panel3D.setRotationY(90f);
				_panel3D.setRotationZ(0f);
				_panel3D.refresh();
	    	}
	    	else if ("3".equals(command)) {
				_panel3D.setRotationX(0f);
				_panel3D.setRotationY(0f);
				_panel3D.setRotationZ(0f);
				_panel3D.refresh();
	    	}
	    	else if ("4".equals(command)) {
				_panel3D.setRotationX(0f);
				_panel3D.setRotationY(180f);
				_panel3D.setRotationZ(0f);
				_panel3D.refresh();
	    	}

	    	
	    }
	}
}
