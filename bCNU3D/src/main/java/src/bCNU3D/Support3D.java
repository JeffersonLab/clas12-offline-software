package bCNU3D;

import java.awt.Color;

import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

public class Support3D {
    
    private static final byte  byteA = (byte)170;
    private static final byte  byteB = (byte)170;
    
    /* a stipple pattern */
    public byte sd[] = { byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA};

    /** for half-tone stipples */
    public byte halftone[] = { (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
	    (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55,
	    (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55,
	    (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA,
	    (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55,
	    (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
	    (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xAA,
	    (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
	    (byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
	    (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55,
	    (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55,
	    (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA,
	    (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55,
	    (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
	    (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xAA,
	    (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
	    (byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
	    (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55,
	    (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55,
	    (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA,
	    (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55,
	    (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
	    (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0xAA,
	    (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
	    (byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
	    (byte) 0xAA, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55 };


    /**
     * Draw a 3D line
     * @param gl the gl context
     * @param x1 x coordinate of one end
     * @param y1 y coordinate of one end
     * @param z1 z coordinate of one end
     * @param x2 x coordinate of other end
     * @param y2 y coordinate of other end
     * @param z2 z coordinate of other end
     * @param color the color
     * @param lineWidth lineWidth;
     */
    public static void drawLine(GL2 gl, float x1, float y1, float z1, float x2,
	    float y2, float z2, Color color, float lineWidth) {

	gl.glBegin(GL.GL_LINES);
	setColor(gl, color);
	gl.glVertex3f(x1, y1, z1);
	gl.glVertex3f(x2, y2, z2);
	gl.glLineWidth(lineWidth);
	gl.glEnd();
    }
    
    /**
     * Draw a 3D line
     * @param drawable the OpenGL drawable
     * @param p0 one end point as [x, y, z]
     * @param p1 other end point as [x, y, z]
     * @param color the color
     * @param lineWidth lineWidth;
     */
    public static void drawLine(GLAutoDrawable drawable, float[] p0, float[] p1, Color color, float lineWidth) {

	GL2 gl = drawable.getGL().getGL2();

	gl.glBegin(GL.GL_LINES);
	setColor(gl, color);
	gl.glVertex3f(p0[0], p0[1], p0[2]);
	gl.glVertex3f(p1[0], p1[1], p1[2]);
	gl.glLineWidth(lineWidth);
	gl.glEnd();
    }
 
    
    /**
     * Set a color based on an awt color
     * @param gl the graphics context
     * @param color the awt color
     */
    public static void setColor(GL2 gl, Color color) {
	float r = color.getRed()/255f;
	float g = color.getGreen()/255f;
	float b = color.getBlue()/255f;
	float a = color.getAlpha()/255f;
	gl.glColor4f(r, g, b, a);
   }
    
    /**
     * Get a simple vertex
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return a vertex
     */
    public static SVertex vertex(float x, float y, float z) {
	return new SVertex(x, y, z, false);
    }
    
    
    /**
     * Convenience method to convert a variable list of floats into
     * a float array.
     * @param v the variable length list of floats
     * @return the corresponding array
     */
    public static float[] toArray(float... v) {
	return v;
    }
    
 
    
}
