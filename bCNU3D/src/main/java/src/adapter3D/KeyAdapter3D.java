package adapter3D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import bCNU3D.Panel3D;

public class KeyAdapter3D implements KeyListener {

    private Panel3D _panel3D;

    public KeyAdapter3D(Panel3D panel3D) {
	_panel3D = panel3D;
    }

    @Override
    public void keyTyped(KeyEvent e) {
	System.err.println("key typed");
    }

    @Override
    public void keyPressed(KeyEvent e) {

	int keyCode = e.getKeyCode();

	if (keyCode == KeyEvent.VK_UP) {
		System.err.println("key pressed up");
	    float dz = 0.1f;
	    _panel3D.deltaZ(dz);
	    _panel3D.refresh();
	}
	if (keyCode == KeyEvent.VK_DOWN) {
		System.err.println("key pressed down");
	    float dz = -0.1f;
	    _panel3D.deltaZ(dz);
	    _panel3D.refresh();
	}
    }

    @Override
    public void keyReleased(KeyEvent e) {
	System.err.println("key released");
    }

}
