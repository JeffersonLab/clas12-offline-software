package item3D;

import java.awt.Color;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

public class Line3D extends Item3D {

    //p0 and p1 are the end points
    protected float[] _p0;
    protected float[] _p1;
     /**
     * Create a simple 3D Line item for use on a Panel3D.
     * @param panel3D the owner 3D panel
     * @param p0 one end point as [x, y, z]
     * @param p1 other end point as [x, y, z]
     * @param color the color
     * @param lineWidth the line width
     */
    public Line3D(Panel3D panel3D, float[] p0, float[] p1, Color color, float lineWidth) {
	super(panel3D);
	_p0 = p0;
	_p1 = p1;
	setColor(color);
	setLineWidth(lineWidth);
    }
    
    /**
     * Create a simple 3D Line item for use on a Panel3D.
     * @param panel3D the owner 3D panel
     * @param x1 x coordinate of one end
     * @param y1 y coordinate of one end
     * @param z1 z coordinate of one end
     * @param x2 x coordinate of other end
     * @param y2 y coordinate of other end
     * @param z2 z coordinate of other end
     * @param color the color
     * @param lineWidth the line width
     */
    public Line3D(Panel3D panel3D, float x1, float y1, float z1, float x2,
	    float y2, float z2, Color color, float lineWidth) {
	this(panel3D, Support3D.toArray(x1, y1, z1), Support3D.toArray(x2, y2, z2), color, lineWidth);
    }
    
    /**
     * Create a simple 3D Line item for use on a Panel3D.
     * @param panel3D the owner 3D panel
     * @param coords the endpoints  as [x1, y1, z1, x2, y2, z2]
     * @param color the color
     * @param lineWidth the line width
     */
    public Line3D(Panel3D panel3D, float[] coords, Color color, float lineWidth) {
	this(panel3D, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], 
		color, lineWidth);
    }
    
    @Override
    public void draw(GLAutoDrawable drawable) {
	Support3D.drawLine(drawable, _p0, _p1, getColor(), getLineWidth());
    }
    
    
    /**
     * Get the x coordinate of the start
     * @return x coordinate of the start
     */
    public float getX0() {
	return _p0[0];
    }

    /**
     * Get the x coordinate of the end
     * @return x coordinate of the end
     */
    public float getX1() {
	return _p1[0];
    }

    /**
     * Get the y coordinate of the start
     * @return y coordinate of the start
     */
    public float getY0() {
	return _p0[1];
    }

    /**
     * Get the y coordinate of the end
     * @return y coordinate of the end
     */
    public float getY1() {
	return _p1[1];
    }

    /**
     * Get the z coordinate of the start
     * @return z coordinate of the start
     */
    public float getZ0() {
	return _p0[2];
    }

    /**
     * Get the z coordinate of the end
     * @return z coordinate of the end
     */
    public float getZ1() {
	return _p1[2];
    }   
    
    /**
     * Add many lines to see how it handles them
     * @param p3D the panel
     * @param n the number to add
     */
    public static void lineItemTest(Panel3D p3D, int n) {
	for (int i = 0; i < n; i++) {
	    float x1 = 1f -2*(float) Math.random();
	    float y1 = 1f -2*(float) Math.random();
	    float z1 = 1f -2*(float) Math.random();
	    float x2 = 1f -2*(float) Math.random();
	    float y2 = 1f -2*(float) Math.random();
	    float z2 = 1f -2*(float) Math.random();
	    
	    int r = (int) (253*Math.random());
	    int g = (int) (253*Math.random());
	    int b = (int) (253*Math.random());
	    
	    Color c = new Color(r, g, b);
	    
	    Line3D item = new Line3D(p3D, x1, y1, z1, x2, y2, z2, c, 1f);
	    p3D.addItem(item);
	}
    }

}
