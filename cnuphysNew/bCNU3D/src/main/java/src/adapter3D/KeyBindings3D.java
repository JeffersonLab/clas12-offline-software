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

import com.jogamp.newt.event.InputEvent;
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
		
		

		//other actions
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0),"j");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0),"k");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0),"u");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0),"d");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0),"l");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0),"r");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0),"x");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0),"y");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0),"z");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.SHIFT_MASK),"X");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.SHIFT_MASK),"Y");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.SHIFT_MASK),"Z");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0),"1");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0),"2");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0),"3");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0),"4");
		
		actionMap.put("u", new KeyAction("u"));
		actionMap.put("d", new KeyAction("d"));
		actionMap.put("l", new KeyAction("l"));
		actionMap.put("r", new KeyAction("r"));


		actionMap.put("j", new KeyAction("j"));
		actionMap.put("k", new KeyAction("k"));
		actionMap.put("x", new KeyAction("x"));
		actionMap.put("y", new KeyAction("y"));
		actionMap.put("z", new KeyAction("z"));
		actionMap.put("X", new KeyAction("X"));
		actionMap.put("Y", new KeyAction("Y"));
		actionMap.put("Z", new KeyAction("Z"));
		actionMap.put("1", new KeyAction("1"));
		actionMap.put("2", new KeyAction("2"));
		actionMap.put("3", new KeyAction("3"));
		actionMap.put("4", new KeyAction("4"));
	}

	
	public class KeyAction extends AbstractAction {

	    public KeyAction(String name) {
	    	super(name);
//	        putValue(Action.NAME, name);
	        putValue(ACTION_COMMAND_KEY, name);
	    }

	    @Override
	    public void actionPerformed(ActionEvent e) {
	//    	System.err.println("KEY BINDINGS " + e.getActionCommand());
	    	
	    	String command = e.getActionCommand();
	    	if ("u".equals(command)) {
				float dz = _panel3D.getZStep();
				_panel3D.deltaY(_factor * dz);
				_panel3D.refresh();
	    	}
	    	else if ("d".equals(command)) {
				float dz = -_panel3D.getZStep();
				_panel3D.deltaY(_factor * dz);
				_panel3D.refresh();
	    	}
	    	else if ("l".equals(command)) {
				float dz = -_panel3D.getZStep();
				_panel3D.deltaX(_factor * dz);
				_panel3D.refresh();
	    	}
	    	else if ("r".equals(command)) {
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
	    	else if ("X".equals(command)) {
				_panel3D.setRotationX(_panel3D.getRotationX() - _factor * DTHETA);
				_panel3D.refresh();
	    	}
	    	else if ("Y".equals(command)) {
				_panel3D.setRotationY(_panel3D.getRotationY() - _factor * DTHETA);
				_panel3D.refresh();
	    	}
	    	else if ("Z".equals(command)) {
				_panel3D.setRotationZ(_panel3D.getRotationZ() - _factor * DTHETA);
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
	    	else if ("j".equals(command)) {
				float dz = _panel3D.getZStep();
				_panel3D.deltaZ(_factor * dz);
				_panel3D.refresh();
	    	}
	    	else if ("k".equals(command)) {
				float dz = -_panel3D.getZStep();
				_panel3D.deltaZ(_factor * dz);
				_panel3D.refresh();
	    	}

	    	
	    }
	}
}
