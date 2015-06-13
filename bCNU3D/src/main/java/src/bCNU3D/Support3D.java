package bCNU3D;

import java.awt.Color;

import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.gl2.GLUT;

public class Support3D {
    
    public static GLUT glut = new GLUT();

    private static final byte byteA = (byte) 170;
    private static final byte byteB = (byte) 170;
    
    private static GLUquadric _quad;

    /* a stipple pattern */
    public byte sd[] = { byteB, byteA, byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB,
	    byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA,
	    byteB, byteA, byteB, byteA };

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
     * Draw a set of points
     * @param drawable
     *            the OpenGL drawable
     * @param coords
     *            the vertices as [x, y, z, x, y, z, ...]
     * @param color
     *            the color
     * @param size
     *            the points size
     */
    public static void drawPoints(GLAutoDrawable drawable, float coords[], Color color, float size) {
	GL2 gl = drawable.getGL().getGL2();
	gl.glPointSize(size);
	
	int np = coords.length / 3;

	gl.glBegin(GL.GL_POINTS);
	setColor(gl, color);

	for (int i = 0; i < np; i++) {
	    int j = i * 3;
	    gl.glVertex3f(coords[j], coords[j + 1], coords[j + 2]);
	}
	gl.glEnd();
    }
       
    /**
     * Draw a wire sphere
     * @param drawable
     *            the OpenGL drawable
     * @param x x center
     * @param y y center
     * @param z z enter
     * @param radius radius in physical units
     * @param slices number of slices
     * @param stacks number of strips
     * @param color color of wires
     */
    public static void wireSphere(GLAutoDrawable drawable, float x, float y,
	    float z, float radius, int slices, int stacks, Color color) {
	GL2 gl = drawable.getGL().getGL2();
	 setColor(gl, color);
	gl.glPushMatrix();
	gl.glTranslatef(x, y,z);
//	glut.glutSolidSphere(radius, 20, 10);
	glut.glutWireSphere(radius, slices, stacks);
	gl.glPopMatrix();

    }
    
    
    /**
     * @param drawable the openGL drawable
     * @param coords the coordinate array
     * @param color the color
     * @param lineWidth the line width
     * @param frame if <code>true</code> frame in slightly darker color
     */
    public static void drawQuads(GLAutoDrawable drawable, float coords[],
	    Color color,
	    float lineWidth, boolean frame) {

	GL2 gl = drawable.getGL().getGL2();
	gl.glLineWidth(lineWidth);

	gl.glBegin(GL2GL3.GL_QUADS);
	setColor(gl, color);

	int numPoints = coords.length / 3;

	for (int i = 0; i < numPoints; i++) {
	    int j = 3*i;
	    gl.glVertex3f(coords[j], coords[j+1], coords[j+2]);
	}

	gl.glEnd();

	if (frame) {
	    int numQuad = coords.length / 12;
	    for (int i = 0; i < numQuad; i++) {
		gl.glBegin(GL.GL_LINE_STRIP);
		setColor(gl, color.darker());
		
		int j = i*12;
		
		gl.glVertex3f(coords[j++], coords[j++], coords[j++]);
		gl.glVertex3f(coords[j++], coords[j++], coords[j++]);
		gl.glVertex3f(coords[j++], coords[j++], coords[j++]);
		gl.glVertex3f(coords[j++], coords[j++], coords[j++]);

		gl.glEnd();
	    }
	}
	

    }
   
    
    /**
     * @param drawable the openGL drawable
     * @param coords the coordinate array
     * @param index1 index into first vertex
     * @param index2 index into second vertex
     * @param index3 index into third vertex
     * @param index4 index into fourth vertex
     * @param color the color
     * @param lineWidth the line width
     * @param frame if <code>true</code> frame in slightly darker color
     */
    public static void drawQuad(GLAutoDrawable drawable, float coords[],
	    int index1, int index2, int index3, int index4, Color color,
	    float lineWidth, boolean frame) {

	int i1 = 3 * index1;
	int i2 = 3 * index2;
	int i3 = 3 * index3;
	int i4 = 3 * index4;

	GL2 gl = drawable.getGL().getGL2();
	gl.glLineWidth(lineWidth);

	gl.glBegin(GL2GL3.GL_QUADS);
	setColor(gl, color);
	gl.glVertex3f(coords[i1], coords[i1 + 1], coords[i1 + 2]);
	gl.glVertex3f(coords[i2], coords[i2 + 1], coords[i2 + 2]);
	gl.glVertex3f(coords[i3], coords[i3 + 1], coords[i3 + 2]);
	gl.glVertex3f(coords[i4], coords[i4 + 1], coords[i4 + 2]);
	
	gl.glEnd();
	
	if (frame) {
	    gl.glBegin(GL.GL_LINE_STRIP);
	    setColor(gl, color.darker());

	    gl.glVertex3f(coords[i1], coords[i1 + 1], coords[i1 + 2]);
	    gl.glVertex3f(coords[i2], coords[i2 + 1], coords[i2 + 2]);
	    gl.glVertex3f(coords[i3], coords[i3 + 1], coords[i3 + 2]);
	    gl.glVertex3f(coords[i4], coords[i4 + 1], coords[i4 + 2]);
	    gl.glEnd();
	}
	

    }

    /**
     * 
     * @param drawable the OpenGL drawable
     * @param coordsthe trangle as [x1, y1, ..., y3, z3]
     * @param color the color
     * @param lineWidth the line width
     * @param frame if <code>true</code> frame in slightly darker color
    * @param lineWidth
     */
    public static void drawTriangle(GLAutoDrawable drawable, float coords[],
	    Color color,
	    float lineWidth, boolean frame) {
	drawTriangle(drawable, coords, 0, 1, 2, color, lineWidth, frame);
    }
    
    /**
     * 
     * @param drawable the OpenGL drawable
     * @param coords a set of points
     * @param index1
     * @param index2
     * @param index3
     * @param color the color
     * @param lineWidth the line width
     * @param frame if <code>true</code> frame in slightly darker color
    * @param lineWidth
     */
    public static void drawTriangle(GLAutoDrawable drawable, float coords[],
	    int index1, int index2, int index3, Color color,
	    float lineWidth, boolean frame) {

	int i1 = 3 * index1;
	int i2 = 3 * index2;
	int i3 = 3 * index3;

	GL2 gl = drawable.getGL().getGL2();
	gl.glLineWidth(lineWidth);

	gl.glBegin(GL.GL_TRIANGLES);
	setColor(gl, color);
	gl.glVertex3f(coords[i1], coords[i1 + 1], coords[i1 + 2]);
	gl.glVertex3f(coords[i2], coords[i2 + 1], coords[i2 + 2]);
	gl.glVertex3f(coords[i3], coords[i3 + 1], coords[i3 + 2]);

	gl.glEnd();

	if (frame) {
	    gl.glBegin(GL.GL_LINE_STRIP);
	    setColor(gl, color.darker());

	    gl.glVertex3f(coords[i1], coords[i1 + 1], coords[i1 + 2]);
	    gl.glVertex3f(coords[i2], coords[i2 + 1], coords[i2 + 2]);
	    gl.glVertex3f(coords[i3], coords[i3 + 1], coords[i3 + 2]);
	    gl.glEnd();
	}

    }
    
    /**
     * Draw a 3D tube
     * 
     * @param drawable
     *            the OpenGL drawable
     * @param gl
     *            the gl context
     * @param x1
     *            x coordinate of one end
     * @param y1
     *            y coordinate of one end
     * @param z1
     *            z coordinate of one end
     * @param x2
     *            x coordinate of other end
     * @param y2
     *            y coordinate of other end
     * @param z2
     *            z coordinate of other end
     @param radius the radius of the tube
     * @param color
     *            the color
     */
    public static void drawTube(GLAutoDrawable drawable, float x1, float y1,
	    float z1, float x2, float y2, float z2, float radius, Color color) {

	if (_quad == null) {
	    _quad = Panel3D.glu.gluNewQuadric();
	}
	
	float vx = x2-x1;
	float vy = y2-y1;
	float vz = z2-z1;
	if (Math.abs(vz) < 1.0e-5) {
	    vz = 0.0001f;
	}

	float v = (float) Math.sqrt( vx*vx + vy*vy + vz*vz );
	float ax = (float) (57.2957795*Math.acos( vz/v ));
	if ( vz < 0.0 )
	    ax = -ax;
	float rx = -vy*vz;
	float ry = vx*vz;

	GL2 gl = drawable.getGL().getGL2();
	setColor(gl, color);

	gl.glPushMatrix();
	//draw the cylinder body
	gl.glTranslatef( x1, y1, z1 );
	gl.glRotatef(ax, rx, ry, 0f);
//	gluQuadricOrientation(quadric,GLU_OUTSIDE);
	Panel3D.glu.gluCylinder(_quad, radius, radius, v, 10, 1);
	
	
	gl.glPopMatrix();
    }
    
    /**
     * Draw a 3D line
     * 
     * @param drawable
     *            the OpenGL drawable
     * @param gl
     *            the gl context
     * @param x1
     *            x coordinate of one end
     * @param y1
     *            y coordinate of one end
     * @param z1
     *            z coordinate of one end
     * @param x2
     *            x coordinate of other end
     * @param y2
     *            y coordinate of other end
     * @param z2
     *            z coordinate of other end
     * @param color
     *            the color
     * @param lineWidth
     *            the line width
     */
    public static void drawLine(GLAutoDrawable drawable, float x1, float y1,
	    float z1, float x2, float y2, float z2, Color color, float lineWidth) {

	GL2 gl = drawable.getGL().getGL2();
	gl.glLineWidth(lineWidth);

	gl.glBegin(GL.GL_LINES);
	setColor(gl, color);
	gl.glVertex3f(x1, y1, z1);
	gl.glVertex3f(x2, y2, z2);
	gl.glEnd();
    }

    /**
     * Draw a 3D line
     * 
     * @param drawable
     *            the OpenGL drawable
     * @param p0
     *            one end point as [x, y, z]
     * @param p1
     *            other end point as [x, y, z]
     * @param color
     *            the color
     * @param lineWidth
     *            the line width
     */
    public static void drawLine(GLAutoDrawable drawable, float[] p0,
	    float[] p1, Color color, float lineWidth) {

	GL2 gl = drawable.getGL().getGL2();
	gl.glLineWidth(lineWidth);

	gl.glBegin(GL.GL_LINES);
	setColor(gl, color);
	gl.glVertex3f(p0[0], p0[1], p0[2]);
	gl.glVertex3f(p1[0], p1[1], p1[2]);
	gl.glEnd();
    }

    /**
     * Draw a 3D line
     * 
     * @param drawable
     *            the OpenGL drawable
     * @param coords
     *            the line as [x1, y1, z1, x2, y2, z2]
     * @param color
     *            the color
     * @param lineWidth
     *            the line width
     */
    public static void drawLine(GLAutoDrawable drawable, float[] coords,
	    Color color, float lineWidth) {

	GL2 gl = drawable.getGL().getGL2();
	gl.glLineWidth(lineWidth);

	gl.glBegin(GL.GL_LINES);
	setColor(gl, color);
	gl.glVertex3f(coords[0], coords[1], coords[2]);
	gl.glVertex3f(coords[3], coords[4], coords[5]);
	gl.glEnd();
    }

    /**
     * Draw a polyline
     * 
     * @param drawable
     *            the OpenGL drawable
     * @param coords
     *            the vertices as [x, y, z, x, y, z, ...]
     * @param color
     *            the color
     * @param lineWidth
     *            the line width
     */
    public static void drawPolyLine(GLAutoDrawable drawable, float[] coords,
	    Color color, float lineWidth) {
	GL2 gl = drawable.getGL().getGL2();
	gl.glLineWidth(lineWidth);

	int np = coords.length / 3;

	gl.glBegin(GL.GL_LINE_STRIP);
	setColor(gl, color);

	for (int i = 0; i < np; i++) {
	    int j = i * 3;
	    gl.glVertex3f(coords[j], coords[j + 1], coords[j + 2]);
	}
	gl.glEnd();

    }

    /**
     * Draw a two color 3D line
     * 
     * @param drawable
     *            the OpenGL drawable
     * @param gl
     *            the gl context
     * @param x1
     *            x coordinate of one end
     * @param y1
     *            y coordinate of one end
     * @param z1
     *            z coordinate of one end
     * @param x2
     *            x coordinate of other end
     * @param y2
     *            y coordinate of other end
     * @param z2
     *            z coordinate of other end
     * @param color1
     *            one color
     * @param color2
     *            other color
     * @param lineWidth
     *            the line width
     */
    public static void drawLine(GLAutoDrawable drawable, float x1, float y1,
	    float z1, float x2, float y2, float z2, Color color1, Color color2,
	    float lineWidth) {

	GL2 gl = drawable.getGL().getGL2();
	gl.glEnable(GL2.GL_LINE_STIPPLE);
	gl.glLineWidth(lineWidth);

	if (color1 != null) {
	    gl.glLineStipple(1, (short) 0x00FF); /* dashed */
	    gl.glBegin(GL.GL_LINES);
	    setColor(gl, color1);
	    gl.glVertex3f(x1, y1, z1);
	    gl.glVertex3f(x2, y2, z2);
	    gl.glEnd();
	}
	if (color2 != null) {
	    gl.glLineStipple(1, (short) 0xFF00); /* dashed */
	    gl.glBegin(GL.GL_LINES);
	    setColor(gl, color2);
	    gl.glVertex3f(x1, y1, z1);
	    gl.glVertex3f(x2, y2, z2);
	    gl.glEnd();
	}

	gl.glDisable(GL2.GL_LINE_STIPPLE);

    }

    /**
     * Draw a two color polyline
     * 
     * @param drawable
     *            the OpenGL drawable
     * @param coords
     *            the vertices as [x, y, z, x, y, z, ...]
     * @param color1
     *            one color
     * @param color2
     *            other color
     * @param lineWidth
     *            the line width
     */
    public static void drawPolyLine(GLAutoDrawable drawable, float[] coords,
	    Color color1, Color color2, float lineWidth) {
	GL2 gl = drawable.getGL().getGL2();
	gl.glLineWidth(lineWidth);
	gl.glEnable(GL2.GL_LINE_STIPPLE);

	int np = coords.length / 3;

	if (color1 != null) {
	    gl.glLineStipple(1, (short) 0x00FF); /* dashed */
	    gl.glBegin(GL.GL_LINE_STRIP);
	    setColor(gl, color1);

	    for (int i = 0; i < np; i++) {
		int j = i * 3;
		gl.glVertex3f(coords[j], coords[j + 1], coords[j + 2]);
	    }
	    gl.glEnd();
	}
	if (color2 != null) {
	    gl.glLineStipple(1, (short) 0xFF00); /* dashed */
	    gl.glBegin(GL.GL_LINE_STRIP);
	    setColor(gl, color2);

	    for (int i = 0; i < np; i++) {
		int j = i * 3;
		gl.glVertex3f(coords[j], coords[j + 1], coords[j + 2]);
	    }
	    gl.glEnd();
	}

	gl.glDisable(GL2.GL_LINE_STIPPLE);

    }

    /**
     * Set a color based on an awt color
     * 
     * @param gl
     *            the graphics context
     * @param color
     *            the awt color
     */
    public static void setColor(GL2 gl, Color color) {
	float r = color.getRed() / 255f;
	float g = color.getGreen() / 255f;
	float b = color.getBlue() / 255f;
	float a = color.getAlpha() / 255f;
	gl.glColor4f(r, g, b, a);
    }

    /**
     * Get a simple vertex
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @param z
     *            the z coordinate
     * @return a vertex
     */
    public static SVertex vertex(float x, float y, float z) {
	return new SVertex(x, y, z, false);
    }

    /**
     * Convenience method to convert a variable list of floats into a float
     * array.
     * 
     * @param v
     *            the variable length list of floats
     * @return the corresponding array
     */
    public static float[] toArray(float... v) {
	return v;
    }

}
