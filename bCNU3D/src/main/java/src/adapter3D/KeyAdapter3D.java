package adapter3D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import bCNU3D.Panel3D;

public class KeyAdapter3D implements KeyListener {
    
    // steps in rotation angle
    private static final float DTHETA = 2f; //degrees

    private Panel3D _panel3D;

    public KeyAdapter3D(Panel3D panel3D) {
	_panel3D = panel3D;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {

	int keyCode = e.getKeyCode();
	boolean shifted = e.isShiftDown();

	if (keyCode == KeyEvent.VK_UP) {
	    float dz = _panel3D.getZStep();
	    _panel3D.deltaZ(dz);
	    _panel3D.refresh();
	}
	else if (keyCode == KeyEvent.VK_DOWN) {
	    float dz = -_panel3D.getZStep();
	    _panel3D.deltaZ(dz);
	    _panel3D.refresh();
	}
	if (keyCode == KeyEvent.VK_L) {
	    float dz = _panel3D.getZStep();
	    _panel3D.deltaX(dz);
	    _panel3D.refresh();
	}
	else if (keyCode == KeyEvent.VK_R) {
	    float dz = -_panel3D.getZStep();
	    _panel3D.deltaX(dz);
	    _panel3D.refresh();
	}
	if (keyCode == KeyEvent.VK_U) {
	    float dz = _panel3D.getZStep();
	    _panel3D.deltaY(dz);
	    _panel3D.refresh();
	}
	else if (keyCode == KeyEvent.VK_D) {
	    float dz = -_panel3D.getZStep();
	    _panel3D.deltaY(dz);
	    _panel3D.refresh();
	}
	else if (keyCode == KeyEvent.VK_LEFT) {
	    _panel3D.setRotationZ(_panel3D.getRotationZ() - DTHETA);
	    _panel3D.refresh();
	}
	else if (keyCode == KeyEvent.VK_RIGHT) {
	    _panel3D.setRotationZ(_panel3D.getRotationZ() + DTHETA);
	    _panel3D.refresh();
	}
	else if (keyCode == KeyEvent.VK_X) {
	    System.err.println("rotate x");
	    int factor = (shifted ? -1 : 1);
	    _panel3D.setRotationX(_panel3D.getRotationX() + factor*DTHETA);
	    _panel3D.refresh();
	}
	else if (keyCode == KeyEvent.VK_Y) {
	    System.err.println("rotate y");
	    int factor = (shifted ? -1 : 1);
	    _panel3D.setRotationY(_panel3D.getRotationY() + factor*DTHETA);
	    _panel3D.refresh();
	}
	else if (keyCode == KeyEvent.VK_Z) {
	    System.err.println("rotate z");
	    int factor = (shifted ? -1 : 1);
	    _panel3D.setRotationZ(_panel3D.getRotationZ() + factor*DTHETA);
	    _panel3D.refresh();
	}

  }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}
