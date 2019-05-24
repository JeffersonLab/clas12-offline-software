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
//		System.err.println("3D KEY PRESS " + e.getSource().getClass().getName());

		int keyCode = e.getKeyCode();
		boolean shifted = e.isShiftDown();

		handleVK(_panel3D, keyCode, shifted);
	}

	/**
	 * Respond to a key stroke or mimic a key stroke
	 * 
	 * @param panel3D the owner panel
	 * @param keyCode the key code
	 * @param shifted whether it was shifted (e.g., capitalized)
	 */
	public static void handleVK(Panel3D panel3D, int keyCode, boolean shifted) {

		if (keyCode == KeyEvent.VK_UP) {
			int factor = (shifted ? 3 : 1);
			float dz = panel3D.getZStep();
			panel3D.deltaZ(factor * dz);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_DOWN) {
			int factor = (shifted ? 3 : 1);
			float dz = -panel3D.getZStep();
			panel3D.deltaZ(factor * dz);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_L) {
			int factor = (shifted ? 3 : 1);
			float dz = -panel3D.getZStep();
			panel3D.deltaX(factor * dz);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_R) {
			int factor = (shifted ? 3 : 1);
			float dz = panel3D.getZStep();
			panel3D.deltaX(factor * dz);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_U) {
			int factor = (shifted ? 3 : 1);
			float dz = panel3D.getZStep();
			panel3D.deltaY(factor * dz);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_D) {
			int factor = (shifted ? 3 : 1);
			float dz = -panel3D.getZStep();
			panel3D.deltaY(factor * dz);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_LEFT) {
			int factor = (shifted ? 3 : 1);
			panel3D.setRotationZ(panel3D.getRotationZ() - factor * DTHETA);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_RIGHT) {
			int factor = (shifted ? 3 : 1);
			panel3D.setRotationZ(panel3D.getRotationZ() + factor * DTHETA);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_X) {
			int factor = (shifted ? -1 : 1);
			panel3D.setRotationX(panel3D.getRotationX() + factor * DTHETA);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_Y) {
			int factor = (shifted ? -1 : 1);
			panel3D.setRotationY(panel3D.getRotationY() + factor * DTHETA);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_Z) {
			int factor = (shifted ? -1 : 1);
			panel3D.setRotationZ(panel3D.getRotationZ() + factor * DTHETA);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_1) { // x out
			panel3D.setRotationX(180f);
			panel3D.setRotationY(90f);
			panel3D.setRotationZ(0f);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_2) { // y out3
			panel3D.setRotationX(90f);
			panel3D.setRotationY(90f);
			panel3D.setRotationZ(0f);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_3) { // z out
			panel3D.setRotationX(0f);
			panel3D.setRotationY(0f);
			panel3D.setRotationZ(0f);
			panel3D.refresh();
		} else if (keyCode == KeyEvent.VK_4) { // z in
			panel3D.setRotationX(0f);
			panel3D.setRotationY(180f);
			panel3D.setRotationZ(0f);
			panel3D.refresh();
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
