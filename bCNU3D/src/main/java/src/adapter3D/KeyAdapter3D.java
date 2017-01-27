package adapter3D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import bCNU3D.Panel3D;

public class KeyAdapter3D implements KeyListener {

	// steps in rotation angle
	private static final float DTHETA = 2f; // degrees

	private Panel3D _panel3D;

	public KeyAdapter3D(Panel3D panel3D) {
		_panel3D = panel3D;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		System.err.println("3D KEY TYPE");
	}

	@Override
	public void keyPressed(KeyEvent e) {
		System.err.println("3D KEY PRESS " + e.getSource().getClass().getName());
		
		int keyCode = e.getKeyCode();
		boolean shifted = e.isShiftDown();

		if (keyCode == KeyEvent.VK_UP) {
			int factor = (shifted ? 3 : 1);
			float dz = _panel3D.getZStep();
			_panel3D.deltaZ(factor * dz);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_DOWN) {
			int factor = (shifted ? 3 : 1);
			float dz = -_panel3D.getZStep();
			_panel3D.deltaZ(factor * dz);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_L) {
			int factor = (shifted ? 3 : 1);
			float dz = -_panel3D.getZStep();
			_panel3D.deltaX(factor * dz);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_R) {
			int factor = (shifted ? 3 : 1);
			float dz = _panel3D.getZStep();
			_panel3D.deltaX(factor * dz);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_U) {
			int factor = (shifted ? 3 : 1);
			float dz = _panel3D.getZStep();
			_panel3D.deltaY(factor * dz);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_D) {
			int factor = (shifted ? 3 : 1);
			float dz = -_panel3D.getZStep();
			_panel3D.deltaY(factor * dz);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_LEFT) {
			int factor = (shifted ? 3 : 1);
			_panel3D.setRotationZ(_panel3D.getRotationZ() - factor * DTHETA);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_RIGHT) {
			int factor = (shifted ? 3 : 1);
			_panel3D.setRotationZ(_panel3D.getRotationZ() + factor * DTHETA);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_X) {
			int factor = (shifted ? -1 : 1);
			_panel3D.setRotationX(_panel3D.getRotationX() + factor * DTHETA);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_Y) {
			int factor = (shifted ? -1 : 1);
			_panel3D.setRotationY(_panel3D.getRotationY() + factor * DTHETA);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_Z) {
			int factor = (shifted ? -1 : 1);
			_panel3D.setRotationZ(_panel3D.getRotationZ() + factor * DTHETA);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_1) { // x out
			_panel3D.setRotationX(180f);
			_panel3D.setRotationY(90f);
			_panel3D.setRotationZ(0f);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_2) { // y out3
			_panel3D.setRotationX(90f);
			_panel3D.setRotationY(90f);
			_panel3D.setRotationZ(0f);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_3) { // z out
			_panel3D.setRotationX(0f);
			_panel3D.setRotationY(0f);
			_panel3D.setRotationZ(0f);
			_panel3D.refresh();
		}
		else if (keyCode == KeyEvent.VK_4) { // z in
			_panel3D.setRotationX(0f);
			_panel3D.setRotationY(180f);
			_panel3D.setRotationZ(0f);
			_panel3D.refresh();
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if (keyCode == KeyEvent.VK_F5) {
			System.err.println("refreshing");
			_panel3D.refresh();
		}

		else if (keyCode == KeyEvent.VK_F6) {
			_panel3D.print();
		}

		else if (keyCode == KeyEvent.VK_F7) {
			_panel3D.snapshot();
		}
	}

}
