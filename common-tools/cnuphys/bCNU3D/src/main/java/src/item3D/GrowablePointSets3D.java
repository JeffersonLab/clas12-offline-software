package item3D;

import java.awt.Color;
import java.util.ArrayList;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.GrowablePointSet;
import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import bCNU3D.Vector3f;

public class GrowablePointSets3D extends Item3D  {
	
	private ArrayList<GrowablePointSet> _pointSets;

	public GrowablePointSets3D(Panel3D panel3D)  {
		super(panel3D);
		_pointSets = new ArrayList<>();
		
	}
	
	/**
	 * Get the collection of point sets
	 * @return the collection of point sets
	 */
	public ArrayList<GrowablePointSet> getPointSets() {
		return _pointSets;
	}
	
	/**
	 * Add a new point set
	 * @param name the name of the point set
	 * @param color the color of the points
	 * @param pointSize the size of the points
	 * @param circular whether the points are circular
	 */
	public void addPointSet(String name, Color color, float pointSize, boolean circular) {
		
		GrowablePointSet gps = findByName(name);
		if (gps != null) {
			System.err.println("Duplicate point set name in addPointSet");
		}
		
		gps = new GrowablePointSet(name, color, pointSize, circular);
		_pointSets.add(gps);
	}
	
	@Override
	public void draw(GLAutoDrawable drawable) {
		for (GrowablePointSet ps : _pointSets) {
			float[] coords = ps.getArray();

			if (coords != null) {
				Support3D.drawPoints(drawable, coords, ps.getColor(), ps.getPointSize(), ps.isCircular());
			}
		}
	}
	
	/**
	 * Find a point set by name
	 * @param name the name of the set (case sensitive)
	 * @return the set with a matching name, or null.
	 */
	public GrowablePointSet findByName(String name) {
		for (GrowablePointSet gps : _pointSets) {
			if (name.equals(gps.getName())) {
				return gps;
			}
		}
		
		return null;
	}


}
