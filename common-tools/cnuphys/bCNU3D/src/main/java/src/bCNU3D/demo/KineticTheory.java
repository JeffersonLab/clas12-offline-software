package bCNU3D.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import bCNU3D.Panel3D;
import item3D.Axes3D;
import item3D.Cylinder;
import item3D.PointSet3D;
import item3D.Triangle3D;

public class KineticTheory extends JFrame {
	
	private Panel3D _panel3D;

	public KineticTheory() {
		setLayout(new BorderLayout(2, 2));
		
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		};
		addWindowListener(wa);

		
		addCenter();
		
		setSize(1200, 1100);
		setLocation(200, 80);

	}
	
	//add the center component
	private void addCenter() {
		_panel3D = createPanel3D();
		
		add(_panel3D, BorderLayout.CENTER);
	}
	

	private static Panel3D createPanel3D() {

		final float xymax = 600f;
		final float zmax = 600f;
		final float zmin = -100f;
		final float xdist = -100f;
		final float ydist = 0f;
		final float zdist = -1600f;

		final float thetax = 45f;
		final float thetay = 45f;
		final float thetaz = 45f;

		Panel3D p3d = new Panel3D(thetax, thetay, thetaz, xdist, ydist, zdist) {
			@Override
			public void createInitialItems() {
				// coordinate axes

				Axes3D axes = new Axes3D(this, -xymax, xymax, -xymax, xymax, zmin, zmax, null, Color.darkGray, 1f, 7, 7,
						8, Color.black, Color.blue, new Font("SansSerif", Font.PLAIN, 11), 0);
				addItem(axes);


			}

			/**
			 * This gets the z step used by the mouse and key adapters, to see how fast we
			 * move in or in in response to mouse wheel or up/down arrows. It should be
			 * overridden to give something sensible. like the scale/100;
			 * 
			 * @return the z step (changes to zDist) for moving in and out
			 */
			@Override
			public float getZStep() {
				return (zmax - zmin) / 50f;
			}

		};

		return p3d;
	}
	
	/**
	 * Main program 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final KineticTheory app = new KineticTheory();
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				app.setVisible(true);
			}
		});

	}
}
