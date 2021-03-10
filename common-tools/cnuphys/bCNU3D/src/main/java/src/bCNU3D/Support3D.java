package bCNU3D;

import java.awt.Color;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;

public class Support3D {

	public static GLUT glut = new GLUT();

	private static final byte byteA = (byte) 170;
	private static final byte byteB = (byte) 170;

	private static GLUquadric _quad;

	/* a stipple pattern */
	public static byte sd[] = { byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA,
			byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB,
			byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA,
			byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB,
			byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA,
			byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB,
			byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA,
			byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB,
			byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA, byteB, byteA };

	/** for half-tone stipples */
	public static byte halftone[] = { (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x55, (byte) 0x55,
			(byte) 0x55, (byte) 0x55 };

	/**
	 * Draw a set of points
	 * 
	 * @param drawable the OpenGL drawable
	 * @param coords   the vertices as [x, y, z, x, y, z, ...]
	 * @param color    the color
	 * @param size     the points size
	 */
	public static void drawPoints(GLAutoDrawable drawable, float coords[], Color color, float size, boolean circular) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glPointSize(size);

		// how many points?
		int np = coords.length / 3;

		if (circular) {
			gl.glEnable(GL2ES1.GL_POINT_SMOOTH);
		} else {
			gl.glDisable(GL2ES1.GL_POINT_SMOOTH);
		}
		gl.glBegin(GL.GL_POINTS);
		setColor(gl, color);

		for (int i = 0; i < np; i++) {
			int j = i * 3;
			gl.glVertex3f(coords[j], coords[j + 1], coords[j + 2]);
		}
		gl.glEnd();
	}

	/**
	 * Draw a set of points
	 * 
	 * @param drawable the OpenGL drawable
	 * @param coords   the vertices as [x, y, z, x, y, z, ...]
	 * @param fill     the fill color
	 * @param frame    the frame color
	 * @param size     the points size
	 */
	public static void drawPoints(GLAutoDrawable drawable, float coords[], Color fill, Color frame, float size,
			boolean circular) {
		if (frame == null) {
			drawPoints(drawable, coords, fill, size, circular);
		} else {
			drawPoints(drawable, coords, frame, size, circular);
			drawPoints(drawable, coords, fill, size - 2, circular);
		}
	}

	/**
	 * Draw a single point using double coordinates
	 * 
	 * @param drawable the OpenGL drawable
	 * @param x        the x coordinate
	 * @param y        the y coordinate
	 * @param z        the z coordinate
	 * 
	 * @param color    the color
	 * @param size     the point's pixel size
	 */
	public static void drawPoint(GLAutoDrawable drawable, double x, double y, double z, Color color, float size,
			boolean circular) {
		drawPoint(drawable, (float) x, (float) y, (float) z, color, size, circular);
	}

	/**
	 * Draw a point using float coordinates
	 * 
	 * @param drawable the OpenGL drawable
	 * @param x        the x coordinate
	 * @param y        the y coordinate
	 * @param z        the z coordinate
	 * 
	 * @param color    the color
	 * @param size     the points size
	 * @param circular
	 */
	public static void drawPoint(GLAutoDrawable drawable, float x, float y, float z, Color color, float size,
			boolean circular) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glPointSize(size);

		setColor(gl, color);
		if (circular) {
			gl.glEnable(GL2ES1.GL_POINT_SMOOTH);
		} else {
			gl.glDisable(GL2ES1.GL_POINT_SMOOTH);
		}

		gl.glBegin(GL.GL_POINTS);
		gl.glVertex3f(x, y, z);
		gl.glEnd();
	}

	public static final String vshader1 = "void main {\n" + "gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n"
			+ "}";

	public static final String fshader1 = "void main {\n" + "}";

	public static void loadShader(GL2 gl, String vertexShaderString, String fragmentShaderString) {
		int v = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
		int f = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);

		// Compile the vertexShader String into a program.
		String[] vlines = new String[] { vertexShaderString };
		int[] vlengths = new int[] { vlines[0].length() };
		gl.glShaderSource(v, vlines.length, vlines, vlengths, 0);
		gl.glCompileShader(v);

		String[] flines = new String[] { fragmentShaderString };
		int[] flengths = new int[] { flines[0].length() };
		gl.glShaderSource(f, flines.length, flines, flengths, 0);
		gl.glCompileShader(f);

		int shaderprogram = gl.glCreateProgram();
		gl.glAttachShader(shaderprogram, v);
		gl.glAttachShader(shaderprogram, f);
		gl.glLinkProgram(shaderprogram);
		gl.glValidateProgram(shaderprogram);

		// gl.glDeleteShader(v);
		// gl.glDeleteShader(f);
		gl.glUseProgram(shaderprogram);
	}

	public static void drawSprite(GLAutoDrawable drawable, Texture texture, float x, float y, float z, float size) {

		GL2 gl = drawable.getGL().getGL2();
		gl.glPointSize(size);

		// adjust(tendToColor);
		texture.bind(gl);
		// gl.glColor4f(rgba.r,rgba.g,rgba.b,rgba.a);
		setColor(gl, Color.black);

		gl.glBegin(GL.GL_POINTS);
		texture.bind(gl);
		gl.glVertex3f(x, y, z);
		gl.glEnd();
	}

	/**
	 * Draw a wire sphere
	 * 
	 * @param drawable the OpenGL drawable
	 * @param x        x center
	 * @param y        y center
	 * @param z        z enter
	 * @param radius   radius in physical units
	 * @param slices   number of slices
	 * @param stacks   number of strips
	 * @param color    color of wires
	 */
	public static void wireSphere(GLAutoDrawable drawable, float x, float y, float z, float radius, int slices,
			int stacks, Color color) {
		GL2 gl = drawable.getGL().getGL2();
		setColor(gl, color);
		gl.glPushMatrix();
		gl.glTranslatef(x, y, z);
		glut.glutWireSphere(radius, slices, stacks);
		gl.glPopMatrix();

	}
	
	/**
	 * Draw a solid sphere
	 * 
	 * @param drawable the OpenGL drawable
	 * @param x        x center
	 * @param y        y center
	 * @param z        z enter
	 * @param radius   radius in physical units
	 * @param slices   number of slices
	 * @param stacks   number of strips
	 * @param color    color of wires
	 */
	public static void solidSphere(GLAutoDrawable drawable, float x, float y, float z, float radius, int slices,
			int stacks, Color color) {
		GL2 gl = drawable.getGL().getGL2();
		setColor(gl, color);
		gl.glPushMatrix();
		gl.glTranslatef(x, y, z);
		glut.glutSolidSphere(radius, slices, stacks);
		gl.glPopMatrix();

	}


	/**
	 * @param drawable  the openGL drawable
	 * @param coords    the coordinate array
	 * @param color     the color
	 * @param lineWidth the line width
	 * @param frame     if <code>true</code> frame in slightly darker color
	 */
	public static void drawQuads(GLAutoDrawable drawable, float coords[], Color color, float lineWidth, boolean frame) {

		drawQuads(drawable, coords, color, (frame ? color.darker() : null), lineWidth);
	}
	
	/**
	 * @param drawable  the openGL drawable
	 * @param coords    the coordinate array
	 * @param color     the color
	 * @param lineWidth the line width
	 * @param frame     if <code>true</code> frame in slightly darker color
	 */
	public static void drawQuads(GLAutoDrawable drawable, float coords[], Color color, Color lineColor, float lineWidth) {

		GL2 gl = drawable.getGL().getGL2();
		gl.glLineWidth(lineWidth);

		gl.glBegin(GL2ES3.GL_QUADS);
		setColor(gl, color);

		int numPoints = coords.length / 3;

		for (int i = 0; i < numPoints; i++) {
			int j = 3 * i;
			gl.glVertex3f(coords[j], coords[j + 1], coords[j + 2]);
		}

		gl.glEnd();

		if (lineColor != null) {

			// a quad has four vertices therefor 12 points
			int numQuad = coords.length / 12;
			for (int i = 0; i < numQuad; i++) {
				gl.glBegin(GL.GL_LINE_STRIP);
				setColor(gl, lineColor);

				int j = i * 12;

				gl.glVertex3f(coords[j++], coords[j++], coords[j++]);
				gl.glVertex3f(coords[j++], coords[j++], coords[j++]);
				gl.glVertex3f(coords[j++], coords[j++], coords[j++]);
				gl.glVertex3f(coords[j++], coords[j++], coords[j++]);

				j = i * 12;
				gl.glVertex3f(coords[j++], coords[j++], coords[j++]);

				gl.glEnd();
			}
		}
	}

	/**
	 * @param drawable  the openGL drawable
	 * @param coords    the coordinate array
	 * @param color     the color
	 * @param lineWidth the line width
	 * @param frame     if <code>true</code> frame in slightly darker color
	 */
	public static void drawQuadsHalfTone(GLAutoDrawable drawable, float coords[], Color color, float lineWidth,
			boolean frame) {

		GL2 gl = drawable.getGL().getGL2();
		gl.glLineWidth(lineWidth);
		gl.glEnable(GL2.GL_POLYGON_STIPPLE);
		gl.glPolygonStipple(halftone, 0);

		gl.glBegin(GL2ES3.GL_QUADS);
		setColor(gl, color);

		int numPoints = coords.length / 3;

		for (int i = 0; i < numPoints; i++) {
			int j = 3 * i;
			gl.glVertex3f(coords[j], coords[j + 1], coords[j + 2]);
		}

		gl.glEnd();
		gl.glDisable(GL2.GL_POLYGON_STIPPLE);

		if (frame) {
			int numQuad = coords.length / 12;
			for (int i = 0; i < numQuad; i++) {
				gl.glBegin(GL.GL_LINE_STRIP);
				setColor(gl, color.darker());

				int j = i * 12;

				gl.glVertex3f(coords[j++], coords[j++], coords[j++]);
				gl.glVertex3f(coords[j++], coords[j++], coords[j++]);
				gl.glVertex3f(coords[j++], coords[j++], coords[j++]);
				gl.glVertex3f(coords[j++], coords[j++], coords[j++]);

				j = i * 12;
				gl.glVertex3f(coords[j++], coords[j++], coords[j++]);

				gl.glEnd();
			}
		}

	}

	/**
	 * @param drawable  the openGL drawable
	 * @param coords    the coordinate array
	 * @param index1    index into first vertex
	 * @param index2    index into second vertex
	 * @param index3    index into third vertex
	 * @param index4    index into fourth vertex
	 * @param color     the color
	 * @param lineWidth the line width
	 * @param frame     if <code>true</code> frame in slightly darker color
	 */
	public static void drawQuad(GLAutoDrawable drawable, float coords[], int index1, int index2, int index3, int index4,
			Color color, float lineWidth, boolean frame) {

		int i1 = 3 * index1;
		int i2 = 3 * index2;
		int i3 = 3 * index3;
		int i4 = 3 * index4;

		GL2 gl = drawable.getGL().getGL2();
		gl.glLineWidth(lineWidth);

		gl.glBegin(GL2ES3.GL_QUADS);
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
			gl.glVertex3f(coords[i1], coords[i1 + 1], coords[i1 + 2]);
			gl.glEnd();
		}

	}

	/**
	 * 
	 * @param drawable  the OpenGL drawable
	 * @param coords    the triangle as [x1, y1, ..., y3, z3]
	 * @param color     the color
	 * @param lineWidth the line width
	 * @param frame     if <code>true</code> frame in slightly darker color
	 * @param lineWidth
	 */
	public static void drawTriangles(GLAutoDrawable drawable, float coords[], Color color, float lineWidth,
			boolean frame) {
		int numTriangle = coords.length / 9;
		for (int i = 0; i < numTriangle; i++) {
			int j = 3 * i;
			drawTriangle(drawable, coords, j, j + 1, j + 2, color, lineWidth, frame);
		}
	}

	/**
	 * Break one triangle into smaller triangles
	 * 
	 * @param coords the triangle as [x1, y1, ..., y3, z3]
	 * @param level  [1..] number of times called recursively. If level is n, get
	 *               4^n triangles
	 * @return all the triangles in a coordinate array
	 */
	public static float[] triangulateTriangle(float coords[], int level) {
		if (level < 1) {
			return coords;
		}
		float tricoords[] = oneToFourTriangle(coords);

		for (int lev = 2; lev <= level; lev++) {
			int numtri = tricoords.length / 9;
			int numNewTri = 4 * numtri;
			float[] tri[] = new float[numtri][];
			float allTris[] = new float[9 * numNewTri];
			for (int i = 0; i < numtri; i++) {
				tri[i] = oneToFourTriangle(tricoords, i);
				System.arraycopy(tri[i], 0, allTris, 36 * i, 36);
			}
			tricoords = allTris;

		}
		return tricoords;
	}

	/**
	 * Break one triangle into four by connecting the midpoints
	 * 
	 * @param coords the triangle as [x1, y1, ..., y3, z3] starting at index
	 * @param index  to first vertex, where coords is assume to contain a list of
	 *               triangles each one requiring 9 numbers
	 * @return all four triangles in a coordinate array
	 */
	public static float[] oneToFourTriangle(float coords[], int index) {
		Vector3f p[] = new Vector3f[6];

		int j = 3 * index;

		p[0] = new Vector3f(coords, j + 0);
		p[1] = new Vector3f(coords, j + 1);
		p[2] = new Vector3f(coords, j + 2);

		p[3] = Vector3f.midpoint(p[0], p[1]);
		p[4] = Vector3f.midpoint(p[1], p[2]);
		p[5] = Vector3f.midpoint(p[2], p[0]);

		float coords4[] = new float[36];

		fillCoords(coords4, 0, p[0], p[3], p[5]);
		fillCoords(coords4, 1, p[1], p[3], p[4]);
		fillCoords(coords4, 2, p[3], p[4], p[5]);
		fillCoords(coords4, 3, p[2], p[4], p[5]);
		// System.err.println("Found Triangles");
		return coords4;
	}

	/**
	 * Break one triangle into four by connecting the midpoints
	 * 
	 * @param coords the triangle as [x1, y1, ..., y3, z3]
	 * @return all four triangles in a coordinate array
	 */
	public static float[] oneToFourTriangle(float coords[]) {
		return oneToFourTriangle(coords, 0);
	}

	// create a coords array by appending 3D points
	private static void fillCoords(float coords[], int index, Vector3f... p) {

		int size = 3 * p.length;
		int i = size * index;

		for (Vector3f v3f : p) {
			coords[i++] = v3f.x;
			coords[i++] = v3f.y;
			coords[i++] = v3f.z;
		}
	}

	/**
	 * Draw a triangle from a coordinate array
	 * 
	 * @param drawable  the OpenGL drawable
	 * @param coords    a set of points
	 * @param index1    "three index" of start of first corner, which will be the
	 *                  next three entries in the coords array
	 * @param index2    "three index" of start of second corner, which will be the
	 *                  next three entries in the coords array
	 * @param index3    "three index" of start of third corner, which will be the
	 *                  next three entries in the coords array
	 * @param color     the color the fill color
	 * @param lineWidth the line width in pixels (if framed)
	 * @param frame     if <code>true</code> frame in slightly darker color
	 * @param lineWidth
	 */
	public static void drawTriangle(GLAutoDrawable drawable, float coords[], int index1, int index2, int index3,
			Color color, float lineWidth, boolean frame) {

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
			gl.glVertex3f(coords[i1], coords[i1 + 1], coords[i1 + 2]);
			gl.glEnd();
		}

	}

	/**
	 * Draw a cone
	 * 
	 * @param drawable the OpenGL drawable
	 * @param x1       x coordinate of center of base
	 * @param y1       y coordinate of center of base
	 * @param z1       z coordinate of center of base
	 * @param x2       x coordinate of tip
	 * @param y2       y coordinate of tip
	 * @param z2       z coordinate of tip
	 * @param radius   radius of base
	 * @param color    color of cone
	 */
	public static void drawCone(GLAutoDrawable drawable, float x1, float y1, float z1, float x2, float y2, float z2,
			float radius, Color color) {

		float vx = x2 - x1;
		float vy = y2 - y1;
		float vz = z2 - z1;
		if (Math.abs(vz) < 1.0e-5) {
			vz = 0.0001f;
		}

		float v = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
		float ax = (float) (57.2957795 * Math.acos(vz / v));
		if (vz < 0.0)
			ax = -ax;
		float rx = -vy * vz;
		float ry = vx * vz;

		GL2 gl = drawable.getGL().getGL2();
		setColor(gl, color);

		gl.glPushMatrix();
		// draw the cylinder body
		gl.glTranslatef(x1, y1, z1);
		gl.glRotatef(ax, rx, ry, 0f);

		glut.glutSolidCone(radius, v, 20, 20);

		gl.glPopMatrix();
	}

	/**
	 * Draw a 3D tube
	 * 
	 * @param drawable the OpenGL drawable
	 * @param x1       x coordinate of one end
	 * @param y1       y coordinate of one end
	 * @param z1       z coordinate of one end
	 * @param x2       x coordinate of other end
	 * @param y2       y coordinate of other end
	 * @param z2       z coordinate of other end
	 * @param radius   the radius of the tube
	 * @param color    the color of the tube
	 */
	public static void drawTube(GLAutoDrawable drawable, float x1, float y1, float z1, float x2, float y2, float z2,
			float radius, Color color) {

		if (_quad == null) {
			_quad = Panel3D.glu.gluNewQuadric();
		}

		float vx = x2 - x1;
		float vy = y2 - y1;
		float vz = z2 - z1;
		if (Math.abs(vz) < 1.0e-5) {
			vz = 0.0001f;
		}

		float v = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
		float ax = (float) (57.2957795 * Math.acos(vz / v));
		if (vz < 0.0)
			ax = -ax;
		float rx = -vy * vz;
		float ry = vx * vz;

		GL2 gl = drawable.getGL().getGL2();
		setColor(gl, color);

		gl.glPushMatrix();
		// draw the cylinder body
		gl.glTranslatef(x1, y1, z1);
		gl.glRotatef(ax, rx, ry, 0f);
		// gluQuadricOrientation(quadric,GLU_OUTSIDE);
		Panel3D.glu.gluCylinder(_quad, radius, radius, v, 50, 1);

		gl.glPopMatrix();
	}

	/**
	 * @param drawable  the OpenGL drawable
	 * @param x1        x coordinate of start
	 * @param y1        y coordinate of start
	 * @param z1        z coordinate of start
	 * @param ux        x component of unit vector direction
	 * @param uy        y component of unit vector direction
	 * @param uz        z component of unit vector direction
	 * @param length    the length of the line
	 * @param color     the color
	 * @param lineWidth the line width
	 */
	public static void drawLine(GLAutoDrawable drawable, float x1, float y1, float z1, float ux, float uy, float uz,
			float length, Color color, float lineWidth) {

		float x2 = x1 + length * ux;
		float y2 = y1 + length * uy;
		float z2 = z1 + length * uz;

		drawLine(drawable, x1, y1, z1, x2, y2, z2, color, lineWidth);
	}

	/**
	 * Draw a 3D line
	 * 
	 * @param drawable  the OpenGL drawable
	 * @param x1        x coordinate of one end
	 * @param y1        y coordinate of one end
	 * @param z1        z coordinate of one end
	 * @param x2        x coordinate of other end
	 * @param y2        y coordinate of other end
	 * @param z2        z coordinate of other end
	 * @param color     the color
	 * @param lineWidth the line width in pixels
	 */
	public static void drawLine(GLAutoDrawable drawable, float x1, float y1, float z1, float x2, float y2, float z2,
			Color color, float lineWidth) {

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
	 * @param drawable  the OpenGL drawable
	 * @param p0        one end point as [x, y, z]
	 * @param p1        other end point as [x, y, z]
	 * @param color     the color
	 * @param lineWidth the line width in pixels
	 */
	public static void drawLine(GLAutoDrawable drawable, float[] p0, float[] p1, Color color, float lineWidth) {

		drawLine(drawable, p0[0], p0[1], p0[2], p1[0], p1[1], p1[2], color, lineWidth);
	}

	/**
	 * Draw a 3D line
	 * 
	 * @param drawable  the OpenGL drawable
	 * @param coords    the line as [x1, y1, z1, x2, y2, z2]
	 * @param color     the color
	 * @param lineWidth the line width in pixels
	 */
	public static void drawLine(GLAutoDrawable drawable, float[] coords, Color color, float lineWidth) {

		drawLine(drawable, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], color, lineWidth);
	}

	/**
	 * Draw a polyline
	 * 
	 * @param drawable  the OpenGL drawable
	 * @param coords    the vertices as [x, y, z, x, y, z, ...]
	 * @param color     the color
	 * @param lineWidth the line width
	 */
	public static void drawPolyLine(GLAutoDrawable drawable, float[] coords, Color color, float lineWidth) {
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
	 * @param drawable  the OpenGL drawable
	 * @param gl        the gl context
	 * @param x1        x coordinate of one end
	 * @param y1        y coordinate of one end
	 * @param z1        z coordinate of one end
	 * @param x2        x coordinate of other end
	 * @param y2        y coordinate of other end
	 * @param z2        z coordinate of other end
	 * @param color1    one color
	 * @param color2    other color
	 * @param lineWidth the line width in pixels
	 */
	public static void drawLine(GLAutoDrawable drawable, float x1, float y1, float z1, float x2, float y2, float z2,
			Color color1, Color color2, float lineWidth) {

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
	 * @param drawable  the OpenGL drawable
	 * @param coords    the vertices as [x, y, z, x, y, z, ...]
	 * @param color1    one color
	 * @param color2    other color
	 * @param lineWidth the line width in pixels
	 */
	public static void drawPolyLine(GLAutoDrawable drawable, float[] coords, Color color1, Color color2,
			float lineWidth) {
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
	 * @param gl    the graphics context
	 * @param color the awt color
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
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return a vertex
	 */
	public static SVertex vertex(float x, float y, float z) {
		return new SVertex(x, y, z, false);
	}

	/**
	 * Convenience method to convert a variable list of floats into a float array.
	 * 
	 * @param v the variable length list of floats
	 * @return the corresponding array
	 */
	public static float[] toArray(float... v) {
		return v;
	}
	

}
