package item3D;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.awt.TextRenderer;

public class Axis3D extends Line3D {

    // the type of axis
    private AxisType _type;

    // length of major ticks
    private static final float MAJTICKLENFRAC = 0.01f;
    private float _majTickLen;

    /** possible types of axes */
    public enum AxisType {
	X_AXIS, Y_AXIS, Z_AXIS
    };

    /** text renderer */
    private TextRenderer _renderer;

    /**
     * Create a coordinate axis
     * @param panel3D the owner 3D panel
     * 
     * @param type
     *            the type
     * @param vmin
     *            the minimum value
     * @param vmax
     *            the maximum value
     * @param color
     *            the color
     * @param lineWidth
     *            the line width
     * @param numMajorTicks
     *            the number of major ticks
     */
    public Axis3D(Panel3D panel3D, AxisType type, float vmin, float vmax, Color color,
	    float lineWidth, int numMajorTicks, Color textColor, Font font) {
	super(panel3D, getEndpoints(type, vmin, vmax), color, lineWidth);
	_type = type;
	_majTickLen = MAJTICKLENFRAC*(vmax-vmin);
	addMajorTicks(vmin, vmax, color, lineWidth, numMajorTicks);
	setTextColor(textColor);
	setFont(font);
    }

    // add major tick marks as child items
    private void addMajorTicks(float vmin, float vmax, Color color,
	    float lineWidth, int numMajorTicks) {
	if (numMajorTicks < 1) {
	    return;
	}

	float del = (vmax - vmin) / (numMajorTicks - 1);

	for (int i = 0; i <= numMajorTicks; i++) {
	    float val = vmin;
	    if (i == numMajorTicks) {
		val = vmax;
	    } else {
		val += (i * del);
	    }

	    if (Math.abs(val) > 1.0e-4) {

		Panel3D p3d = getPanel3D();
		Line3D line = null;
		switch (_type) {
		case X_AXIS:
		    line = new Line3D(p3d, val, _majTickLen, 0, val, -_majTickLen, 0,
			    color, lineWidth);
		    addChild(line);
		    line = new Line3D(p3d, val, 0, _majTickLen, val, 0, -_majTickLen,
			    color, lineWidth);
		    addChild(line);
		    break;

		case Y_AXIS:
		    line = new Line3D(p3d, 0, val, _majTickLen, 0, val, -_majTickLen,
			    color, lineWidth);
		    addChild(line);
		    line = new Line3D(p3d, _majTickLen, val, 0, -_majTickLen, val, 0,
			    color, lineWidth);
		    addChild(line);
		    break;

		case Z_AXIS:
		    line = new Line3D(p3d, _majTickLen, 0, val, -_majTickLen, 0, val,
			    color, lineWidth);
		    addChild(line);
		    line = new Line3D(p3d, 0, _majTickLen, val, 0, -_majTickLen, val,
			    color, lineWidth);
		    addChild(line);
		    break;
		}
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

	String s = "?";
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
	
	FontMetrics fm = _panel3D.getFontMetrics(getFont());
	float winPos[] = new float[3];
	
	
	_panel3D.project(gl, getX1(), getY1(), getZ1(), winPos);
	int x = (int)winPos[0] + 4;
	int y = (int)winPos[1] - (fm.getHeight() + 4);
	
	_renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
	_renderer.setColor(getTextColor());
	_renderer.draw(s, x, y);
	_renderer.endRendering();
	
	
	_panel3D.project(gl, getX0(), getY0(), getZ0(), winPos);
	x = (int)winPos[0] + 4;
	y = (int)winPos[1] - (fm.getHeight() + 4);
	
	_renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
	_renderer.setColor(getTextColor());
	_renderer.draw("-"+s, x, y);
	_renderer.endRendering();
	
	
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
