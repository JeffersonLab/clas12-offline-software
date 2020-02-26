package item3D;

import java.awt.Color;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;

import com.jogamp.opengl.GLAutoDrawable;

public class PointSet3D extends Item3D {

	// the points as [x1, y1, z1, ..., xn, yn, zn]
	private float _coords[];

	// the point color
	private Color _color;

	// the point size
	private float _pointSize;
	
	//draw circular points?
	private boolean _circular;

	/**
	 * Create a set of same color and size points for use on a Panel3D.
	 * 
	 * @param panel3D the owner 3D panel
	 * @param coords the points as [x1, y1, z1, ..., xn, yn, zn]
	 * @param color the color of the points
	 * @param pointSize the drawing size of the points
	 */
	public PointSet3D(Panel3D panel3D, float[] coords, Color color,
			float pointSize, boolean circular) {
		super(panel3D);
		_coords = coords;
		_color = color;
		_pointSize = pointSize;
		_circular = circular;
	}

	@Override
	public void draw(GLAutoDrawable drawable) {
		Support3D.drawPoints(drawable, _coords, _color, _pointSize, _circular);
	}
	
	public void setCoords(float[] coords) {
		_coords = coords;
	}

}
