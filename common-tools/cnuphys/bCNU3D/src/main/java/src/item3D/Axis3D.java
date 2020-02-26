package item3D;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import bCNU3D.DoubleFormat;
import bCNU3D.Panel3D;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.awt.TextRenderer;

public class Axis3D extends Line3D {

	// the type of axis
	private AxisType _type;

	// length of major ticks
	private static final float MAJTICKLENFRAC = 0.01f;
	private float _tickLen;
	private int _numTick;
	private Line3D _lines1[];
	private Line3D _lines2[];

	// number of decimals for tick mark labels
	// skip if negative
	private int _numDec;

	// limits and step
	private float _valMin;
	private float _valMax;
	private float _del;
	private float _vals[];

	// offset to shift axis (hack)
	private float _offset;

	/** possible types of axes */
	public enum AxisType {
		X_AXIS, Y_AXIS, Z_AXIS
	};

	/** text renderer */
	private TextRenderer _renderer;
	
	//if null will use default
	private String _label;

	/**
	 * Create a coordinate axis
	 * 
	 * @param panel3D
	 *            the owner 3D panel
	 * 
	 * @param type
	 *            the type
	 * @param vmin
	 *            the minimum value
	 * @param vmax
	 *            the maximum value
	 * @param color
	 *            the color of the axes
	 * @param lineWidth
	 *            the line width
	 * @param numTicks
	 *            the number of ticks
	 * @param tickColor
	 *            the color of the ticks
	 * @param textColor
	 *            the text color
	 * @param font
	 *            the text font
	 * @param numdec
	 *            the number of decimals to display
	 */
	public Axis3D(Panel3D panel3D, AxisType type, String label, float vmin, float vmax, Color color, float lineWidth, int numTicks,
			Color tickColor, Color textColor, Font font, int numDec) {
		this(panel3D, type, label, vmin, vmax, 0f, color, lineWidth, numTicks, tickColor, textColor, font, numDec);
	}

	/**
	 * Create a coordinate axis
	 * 
	 * @param panel3D
	 *            the owner 3D panel
	 * 
	 * @param type
	 *            the type
	 * @param vmin
	 *            the minimum value
	 * @param vmax
	 *            the maximum value
	 * @param color
	 *            the color of the axes
	 * @param lineWidth
	 *            the line width
	 * @param numTicks
	 *            the number of ticks
	 * @param tickColor
	 *            the color of the ticks
	 * @param textColor
	 *            the text color
	 * @param font
	 *            the text font
	 * @param numdec
	 *            the number of decimals to display
	 */
	public Axis3D(Panel3D panel3D, AxisType type, String label, float vmin, float vmax, float offset, Color color, float lineWidth,
			int numTicks, Color tickColor, Color textColor, Font font, int numDec) {
		super(panel3D, getEndpoints(type, vmin, vmax), color, lineWidth);
		_type = type;
		_label = label;
		_numDec = numDec;
		_tickLen = MAJTICKLENFRAC * (vmax - vmin);
		_numTick = numTicks;

		_valMin = vmin;
		_valMax = vmax;

		_offset = offset;
		if (numTicks > 1) {
			_del = (vmax - vmin) / (numTicks - 1);
			addMajorTicks(color, tickColor, lineWidth);
		}

		setTextColor(textColor);
		setFont(font);
	}

	// add major tick marks as child items
	private void addMajorTicks(Color color, Color tickColor, float lineWidth) {

		_vals = new float[_numTick];
		_lines1 = new Line3D[_numTick];
		_lines2 = new Line3D[_numTick];

		_vals[0] = _valMin;
		_vals[_numTick - 1] = _valMax;
		for (int i = 1; i < (_numTick - 1); i++) {
			_vals[i] = _valMin + _del * i;
		}

		Panel3D p3d = getPanel3D();

		// crosses at ticks
		for (int i = 0; i < _numTick; i++) {

			_lines1[i] = null;
			_lines2[i] = null;

			if (Math.abs(_vals[i]) > 1.0e-4) {

				switch (_type) {
				case X_AXIS:
					_lines1[i] = new Line3D(p3d, _vals[i], _tickLen, 0, _vals[i], -_tickLen, 0, tickColor, lineWidth);
					_lines2[i] = new Line3D(p3d, _vals[i], 0, _tickLen, _vals[i], 0, -_tickLen, tickColor, lineWidth);
					break;

				case Y_AXIS:
					_lines1[i] = new Line3D(p3d, 0, _vals[i], _tickLen, 0, _vals[i], -_tickLen, tickColor, lineWidth);
					_lines2[i] = new Line3D(p3d, _tickLen, _vals[i], 0, -_tickLen, _vals[i], 0, tickColor, lineWidth);
					break;

				case Z_AXIS:
					_lines1[i] = new Line3D(p3d, _tickLen, 0, _vals[i], -_tickLen, 0, _vals[i], tickColor, lineWidth);
					_lines2[i] = new Line3D(p3d, 0, _tickLen, _vals[i], 0, -_tickLen, _vals[i], tickColor, lineWidth);
					break;
				}
			} // > 1.0e-4
		}

		for (int i = 0; i < _numTick; i++) {
			if (_lines1[i] != null) {
				addChild(_lines1[i]);
			}
			if (_lines2[i] != null) {
				addChild(_lines2[i]);
			}
		}
	}

	@Override
	public void draw(GLAutoDrawable drawable) {
		super.draw(drawable);
		GL2 gl = drawable.getGL().getGL2();

		// now the text

		if (_renderer == null) {
			_renderer = new TextRenderer(getFont());
		}

		String s = _label;

		if (s == null) {
			switch (_type) {
			case X_AXIS:
				s = "x";
				break;
			case Y_AXIS:
				s = "y";
				break;
			case Z_AXIS:
				s = "z";
				break;
			}
		}

		FontMetrics fm = _panel3D.getFontMetrics(getFont());
		float winPos[] = new float[3];

		float extend[] = new float[3];

		double extLen = Math.abs(Math.max(getX1(), Math.max(getY1(), getZ1()))) / 10;
		extendedPoint(1, (float) extLen, extend);

		_panel3D.project(gl, extend[0], extend[1], extend[2], winPos);
		// _panel3D.project(gl, getX1(), getY1(), getZ1(), winPos);
		int x = (int) winPos[0] + 4;
		int y = (int) winPos[1] - (fm.getHeight() + 4);

		_renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
		_renderer.setColor(getTextColor());
		_renderer.draw(s, x, y);
		_renderer.endRendering();

		// axis values
		if (_numDec >= 0) {
			for (int i = 0; i < _numTick; i++) {

				if (_lines1[i] != null) {
					s = DoubleFormat.doubleFormat(_vals[i] + _offset, _numDec);
					_panel3D.project(gl, _lines1[i].getX1(), _lines1[i].getY1(), _lines1[i].getZ1(), winPos);
					x = (int) winPos[0] + 4;
					y = (int) winPos[1] - (fm.getHeight() + 4);

					_renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
					_renderer.setColor(getTextColor());
					_renderer.draw(s, x, y);
					_renderer.endRendering();
				}
			}

		}

	}

	private static float[] getEndpoints(AxisType type, float vmin, float vmax) {
		float coords[] = new float[6];
		for (int i = 0; i < 6; i++) {
			coords[i] = 0f;
		}

		switch (type) {

		case X_AXIS:
			coords[0] = vmin;
			coords[3] = vmax;
			break;

		case Y_AXIS:
			coords[1] = vmin;
			coords[4] = vmax;
			break;

		case Z_AXIS:
			coords[2] = vmin;
			coords[5] = vmax;
			break;
		}

		return coords;
	}

	/**
	 * Get the type of axis
	 * 
	 * @return the type of axis
	 */
	public AxisType getType() {
		return _type;
	}

}
