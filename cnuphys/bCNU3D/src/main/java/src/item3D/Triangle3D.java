package item3D;

import java.awt.Color;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;

import com.jogamp.opengl.GLAutoDrawable;

public class Triangle3D extends Item3D {
    
    //the coordinates as [x1, y1, z1, ..., x3, y3, z3]
    protected float[] _coords;
    
    //frame?
    protected boolean _frame;
 

    /**
     * Create a triangle from an array of coordinates
     * @param panel3d the owner panel
     * @param coords the coordinates [x1, y1, ..., y3, z3]
     * @param color the triangle color
     * @param lineWidth the line width
     * @param frame frame the triangle
     */
    public Triangle3D(Panel3D panel3d, float coords[], Color color, float lineWidth, boolean frame) {
	super(panel3d);
	_coords = coords;
	_frame = frame;
	
	setColor(color);
	setLineWidth(lineWidth);
    }
    
    /**
     * Create a triangle from nine explicit coordinates
     * @param panel3d the owner panel
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @param x3
     * @param y3
     * @param z3
     * @param color the triangle color
     * @param lineWidth the line width
     * @param frame frame the triangle
     */
    public Triangle3D(Panel3D panel3d, 
	    float x1, float y1, float z1,
	    float x2, float y2, float z2,
	    float x3, float y3, float z3,
	    Color color, float lineWidth,
	    boolean frame) {
	super(panel3d);
	_coords = new float[9];
	_coords[0] = x1;
	_coords[1] = y1;
	_coords[2] = z1;
	_coords[3] = x2;
	_coords[4] = y2;
	_coords[5] = z2;
	_coords[6] = x3;
	_coords[7] = y3;
	_coords[8] = z3;
	_frame = frame;
	
	//test triangulation
//	_coords = Support3D.triangulateTriangle(_coords, 4);
//	System.err.println("Coords len: " + _coords.length);

	
	setColor(color);
	setLineWidth(lineWidth);
    }


   
    @Override
    public void draw(GLAutoDrawable drawable) {
	Support3D.drawTriangles(drawable, _coords, getColor(), getLineWidth(), _frame);
    }

}
