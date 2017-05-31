package adapter3D;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import bCNU3D.Panel3D;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;

public class MouseAdapter3D implements MouseListener, MouseMotionListener,
		MouseWheelListener {

	protected int prevMouseX;
	protected int prevMouseY;

	protected Panel3D _panel3D;

	public MouseAdapter3D(Panel3D panel3D) {
		_panel3D = panel3D;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		prevMouseX = e.getX();
		prevMouseY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
		final int x = e.getX();
		final int y = e.getY();
		int width = 0, height = 0;
		Object source = e.getSource();

		if (source instanceof GLJPanel) {
			GLJPanel window = (GLJPanel) source;
			width = window.getSurfaceWidth();
			height = window.getSurfaceHeight();
		} else if (source instanceof GLAutoDrawable) {
			GLAutoDrawable glad = (GLAutoDrawable) source;
			width = glad.getSurfaceWidth();
			height = glad.getSurfaceHeight();
		} else if (GLProfile.isAWTAvailable()
				&& source instanceof java.awt.Component) {
			java.awt.Component comp = (java.awt.Component) source;
			width = comp.getWidth();
			height = comp.getHeight();
		} else {
			throw new RuntimeException(
					"Event source neither Window nor Component: " + source);
		}
		float theta1 = 360.0f * ((float) (x - prevMouseX) / (float) width);
		float theta2 = 360.0f * ((float) (prevMouseY - y) / (float) height);

		prevMouseX = x;
		prevMouseY = y;

		if (e.isControlDown()) {
			_panel3D.setRotationY(_panel3D.getRotationY() + theta1);
			_panel3D.setRotationZ(_panel3D.getRotationZ() + theta2);
		} else if (e.isShiftDown()) {
			_panel3D.setRotationZ(_panel3D.getRotationZ() + theta1);
			_panel3D.setRotationX(_panel3D.getRotationX() + theta2);
		} else {
			_panel3D.setRotationX(_panel3D.getRotationX() + theta1);
			_panel3D.setRotationY(_panel3D.getRotationY() + theta2);
		}
		_panel3D.refresh();

	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int clicks = e.getWheelRotation();

		float dz = _panel3D.getZStep() * clicks;
		_panel3D.deltaZ(dz);
		_panel3D.refresh();
	}

}
