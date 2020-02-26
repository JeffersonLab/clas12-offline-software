package basic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;

public class BasicPanel3D extends GLJPanel implements GLEventListener {
	
	//the preferred size
	private Dimension _preferredSize;
	
	//the 3D manager
	private BasicManager3D _manager3D;
	
	public BasicPanel3D(Dimension preferredSize) {
		_manager3D = BasicManager3D.getInstance();
		_preferredSize = (preferredSize != null) ? preferredSize : new Dimension(600, 400);
		addGLEventListener(this);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return _preferredSize;
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		System.out.println("Got a GLEventListener init event.");
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		System.out.println("Got a GLEventListener dispose event.");
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		float x1[] = {-0.5f, 0f, 0f};
		float y1[] = {-0.5f, 0.5f, 0.5f};
		float z1[] = {0f, 0f, 0f};
		float x2[] = {0.5f, -0.5f, 0.5f};
		float y2[] = {-0.5f, -0.5f, -0.5f};
		float z2[] = {0f, 0f, 0f};
		BasicLineDrawing.drawLines(drawable, x1, y1, z1, x2, y2, z2, Color.yellow);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		System.out.println("Got a GLEventListener reshape event.");
	}

	/**
	 * main program for testing
	 * @param arg command line arguments (ignored)
	 */
	public static void main(String arg[]) {
		final JFrame testFrame = new JFrame("Test Frame");
		testFrame.setLayout(new BorderLayout(8, 8));
		
		BasicPanel3D panel3D = new BasicPanel3D(null);
		testFrame.add(panel3D, BorderLayout.CENTER);
		
		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.err.println("Done");
				System.exit(1);
			}
		};
		
		
		testFrame.addWindowListener(windowAdapter);
		testFrame.pack();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
			}
		});

	}
}
