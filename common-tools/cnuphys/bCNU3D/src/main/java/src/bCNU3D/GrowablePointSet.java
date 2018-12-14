package bCNU3D;

import java.awt.Color;
import java.util.ArrayList;

public class GrowablePointSet extends ArrayList<Vector3f> {

	private float[] _array;
	private boolean _dirty = true;

	// the point color
	private Color _color;

	// the point size
	private float _pointSize;

	// draw circular points?
	private boolean _circular;

	// name
	private String _name;

	/**
	 * 
	 * @param name
	 * @param color
	 *            the color of the points
	 * @param pointSize
	 *            the drawing size of the points
	 */
	public GrowablePointSet(String name, Color color, float pointSize, boolean circular) {
		_name = name;
		System.err.println("Created point set with name [" + _name + "]");
		_color = color;
		_pointSize = pointSize;
		_circular = circular;
	}
	
	public String getName() {
		return _name;
	}
	
	public boolean isCircular() {
		return _circular;
	}
	
	public float getPointSize() {
		return _pointSize;
	}
	
	public Color getColor() {
		return _color;
	}

	/**
	 * Add a point to this set
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return <code>true</code> if the 3D point was added
	 */
	public boolean add(float x, float y, float z) {
		return add(new Vector3f(x, y, z));
	}

	@Override
	public boolean add(Vector3f v) {
		_dirty = true;
		return super.add(v);
	}
	
	@Override
	public void clear() {
		_dirty = true;
		super.clear();
	}

	// get the coord array
	public float[] getArray() {
		if (_dirty) {
			int n = 3 * size();
			if (n == 0) {
				return null;
			}

			_array = new float[n];
			int index = 0;
			for (Vector3f v : this) {
				_array[index++] = v.x;
				_array[index++] = v.y;
				_array[index++] = v.z;
			}

		}

		return _array;
	}

	public void add(double x, double y, double z) {
		add((float)x, (float)y, (float)z);
	}
}