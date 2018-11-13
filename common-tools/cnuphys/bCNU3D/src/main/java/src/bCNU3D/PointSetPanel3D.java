package bCNU3D;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

import item3D.Axes3D;
import item3D.GrowablePointSets3D;

public class PointSetPanel3D extends Panel3D {
	
	//the point sets
	private GrowablePointSets3D _pointSetItem;

	/*
	 * The panel that holds the 3D objects
	 * 
	 * @param angleX the initial x rotation angle in degrees
	 * 
	 * @param angleY the initial y rotation angle in degrees
	 * 
	 * @param angleZ the initial z rotation angle in degrees
	 * 
	 * @param xdist move viewpoint left/right
	 * 
	 * @param ydist move viewpoint up/down
	 * 
	 * @param zdist the initial viewer z distance should be negative
	 */
	public PointSetPanel3D(float angleX, float angleY, float angleZ, float xDist, float yDist, float zDist) {
		super(angleX, angleY, angleZ, xDist, yDist, zDist);
		
		_pointSetItem = new GrowablePointSets3D(this);
		addItem(0, _pointSetItem);
	}
	
	/**
	 * Get the point set item
	 * @return the point set item
	 */
	public GrowablePointSets3D getPointSetItem() {
		return _pointSetItem;
	}
	
	/**
	 * Clear the data
	 */
	public void clear() {
		ArrayList<GrowablePointSet> psets = getPointSets();
		if (psets != null) {
			for (GrowablePointSet gps : psets) {
				gps.clear();
			}
		}
	}
	
	/**
	 * Get the collection of point sets
	 * @return the collection of point sets
	 */
	public ArrayList<GrowablePointSet> getPointSets() {
		return _pointSetItem.getPointSets();
	}
	
	public static void main(String arg[]) {
		final JFrame testFrame = new JFrame("bCNU PointSetPanel3D Test");
		testFrame.setLayout(new BorderLayout(4, 4));

		Random rand = new Random();

		final float xymax = 600f;
		final float zmax = 600f;
		final float zmin = -100f;
		final float xdist = -100f;
		final float ydist = 0f;
		final float zdist = -1600f;

		final float thetax = 45f;
		final float thetay = 45f;
		final float thetaz = 45f;
		
		PointSetPanel3D p3d = new PointSetPanel3D(thetax, thetay, thetaz, xdist, ydist, zdist) {
			@Override
			public void createInitialItems() {
				
				String labels[] = {"P (Gev/c)", "theta", "phi"};
				Axes3D axes = new Axes3D(this, -xymax, xymax, -xymax, xymax,
						zmin, zmax, labels, Color.darkGray, 2f, 7, 7, 8, Color.black,
						Color.blue, new Font("SansSerif", Font.PLAIN, 32), 0);
				addItem(axes);

			}
			
			/**
			 * This gets the z step used by the mouse and key adapters, to see
			 * how fast we move in or in in response to mouse wheel or up/down
			 * arrows. It should be overridden to give something sensible. like
			 * the scale/100;
			 * 
			 * @return the z step (changes to zDist) for moving in and out
			 */
			@Override
			public float getZStep() {
				return (zmax - zmin) / 50f;
			}

		};
		
		testFrame.add(p3d, BorderLayout.CENTER);
		System.err.println("3D test (B)");

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.err.println("Done");
				System.exit(1);
			}
		};

		testFrame.addWindowListener(windowAdapter);
		testFrame.setBounds(200, 100, 900, 700);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
			}
		});


		String name1 = "Point Set 1";
		String name2 = "Point Set 2";
		GrowablePointSets3D setItem = p3d.getPointSetItem();
		
		setItem.addPointSet(name1, Color.red, 10, true);
		setItem.addPointSet(name2, Color.blue, 10, false);
		
		GrowablePointSet gps1 = setItem.findByName(name1);
		GrowablePointSet gps2 = setItem.findByName(name2);
		
		for (int i = 0; i < 100; i++) {
			float x = (float) (-xymax + 2 * xymax * Math.random());
			float y = (float) (-xymax + 2 * xymax * Math.random());
			float z = (float) (zmin + (zmax - zmin) * Math.random());

			gps1.add(x, y, z);
		}
		
//		p3d.refresh();
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
