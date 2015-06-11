package bCNU3D;

import item3D.Axes3D;
import item3D.Cube;
import item3D.Item3D;
import item3D.Line3D;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import adapter3D.KeyAdapter3D;
import adapter3D.MouseAdapter3D;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;

@SuppressWarnings("serial")
public class Panel3D extends JPanel implements GLEventListener {

    protected GLProfile glprofile;
    protected GLCapabilities glcapabilities;
    protected final GLJPanel gljpanel;
    protected GLU glu; // glu utilities

    // view rotation angles (degrees)
    private float _view_rotx;
    private float _view_roty;
    private float _view_rotz;

    // distance in front of the screen
    private float _zdist;

    // x and y translation
    private float _xdist;
    private float _ydist;

    // the list of 3D items to be drawn
    protected Vector<Item3D> _itemList = new Vector<Item3D>();

    // listen for mouse events
    protected MouseAdapter3D _mouseAdapter;

    // listen for key events
    protected KeyAdapter3D _keyAdapter;

    // the openGL version and renderer strings
    protected String _versionStr;
    protected String _rendererStr;

    // redrawing needed?
    private boolean _dirty = true;

    /*
     * The panel that holds the 3D objects
     * 
     * @param angleX the initial x rotation angle in degrees
     * 
     * @param angleY the initial y rotation angle in degrees
     * 
     * @param angleZ the initial z rotation angle in degrees
     * 
     * @param xdist move viewpoint left/right
     * 
     * @param ydist move viewpoint up/down
     * 
     * @param zdist the initial viewer z distance should be negative
     */
    public Panel3D(float angleX, float angleY, float angleZ, float xDist,
	    float yDist, float zDist) {
	_view_rotx = angleX;
	_view_roty = angleY;
	_view_rotz = angleZ;
	_xdist = xDist;
	_ydist = yDist;
	_zdist = zDist;

	setLayout(new BorderLayout(0, 0));
	glprofile = GLProfile.getDefault();
	glcapabilities = new GLCapabilities(glprofile);
	glcapabilities.setRedBits(8);
	glcapabilities.setBlueBits(8);
	glcapabilities.setGreenBits(8);
	glcapabilities.setAlphaBits(8);

	gljpanel = new GLJPanel(glcapabilities);
	gljpanel.addGLEventListener(this);

	// a one frame per sec animator just for maintenance
	final FPSAnimator animator = new FPSAnimator(gljpanel, 24);
	animator.start();

	safeAdd(addNorth(), BorderLayout.NORTH);
	safeAdd(addSouth(), BorderLayout.SOUTH);
	safeAdd(addEast(), BorderLayout.EAST);
	safeAdd(addWest(), BorderLayout.WEST);

	// GLJPanel in the center
	add(gljpanel, BorderLayout.CENTER);

	_mouseAdapter = new MouseAdapter3D(this);
	gljpanel.addMouseListener(_mouseAdapter);
	gljpanel.addMouseMotionListener(_mouseAdapter);
	gljpanel.addMouseWheelListener(_mouseAdapter);

	_keyAdapter = new KeyAdapter3D(this);
	gljpanel.addKeyListener(_keyAdapter);

	createInitialItems();
    }

    /**
     * Create the initial items
     */
    public void createInitialItems() {
    }

    // add a component in the specified place if not null
    private void safeAdd(JComponent c, String placement) {
	if (c != null) {
	    add(c, placement);
	}
    }

    // add the component in the north
    private JComponent addNorth() {
	return null;
    }

    // add the component in the south
    private JComponent addSouth() {
	return null;
    }

    // add the component in the north
    private JComponent addEast() {
	return null;
    }

    // add the component in the north
    private JComponent addWest() {
	return null;
    }

    @Override
    public void display(GLAutoDrawable drawable) {
	
	if (!_dirty) {
//	    System.err.println("skipping display, should be clean");
	    return;
	}
	_dirty = false;
	// System.err.println("display");
	// System.err.println("called display _view_rotx = " + _view_rotx +
	// "  _view_roty = " + _view_roty);
	GL2 gl = drawable.getGL().getGL2();
	gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

	gl.glLoadIdentity(); // reset the model-view matrix

	gl.glTranslatef(_xdist, _ydist, _zdist); // translate into the screen
	// gl.glPolygonStipple(sd, 0);

	gl.glPushMatrix();
	gl.glRotatef(_view_rotx, 1.0f, 0.0f, 0.0f);
	gl.glRotatef(_view_roty, 0.0f, 1.0f, 0.0f);
	gl.glRotatef(_view_rotz, 0.0f, 0.0f, 1.0f);

	// draw the items
	for (Item3D item3D : _itemList) {
	    // if item is not visible, neither it nor its children are drawn
	    if (item3D.isVisible()) {
		item3D.drawItem(drawable);
	    }
	}

	//
	// gl.glBegin(GL.GL_TRIANGLES); // draw using triangles
	//
	// // Right-face triangle
	// gl.glColor4f(1.0f, 1.0f, 0.0f, 0.4f); // Red
	// gl.glVertex3f(0.0f, 1.0f, 0.0f);
	// gl.glVertex3f(1.0f, -1.0f, 1.0f);
	// gl.glVertex3f(1.0f, -1.0f, -1.0f);
	//
	// // Back-face triangle
	// gl.glColor4f(1.0f, 0.0f, 1.0f, 0.4f); // Red
	// gl.glVertex3f(0.0f, 1.0f, 0.0f);
	// gl.glVertex3f(1.0f, -1.0f, -1.0f);
	// gl.glVertex3f(-1.0f, -1.0f, -1.0f);
	//
	// // Left-face triangle
	// gl.glColor4f(0.0f, 1.0f, 0.0f, 0.4f); // Red
	// gl.glVertex3f(0.0f, 1.0f, 0.0f);
	// gl.glVertex3f(-1.0f, -1.0f, -1.0f);
	// gl.glVertex3f(-1.0f, -1.0f, 1.0f);
	//
	// // Font-face triangle
	// gl.glColor4f(1.0f, 0.0f, 0.0f, 0.4f); // Red
	// gl.glVertex3f(0.0f, 1.0f, 0.0f);
	// gl.glVertex3f(-1.0f, -1.0f, 1.0f);
	// gl.glVertex3f(1.0f, -1.0f, 1.0f);

	// gl.glEnd();
	gl.glPopMatrix();

    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
	System.err.println("called dispose");
    }

    @Override
    public void init(GLAutoDrawable drawable) {
	_dirty = true;
	glu = GLU.createGLU();
	GL2 gl = drawable.getGL().getGL2();

	// version?
	_versionStr = gl.glGetString(GL.GL_VERSION);
	System.err.println("OpenGL version: " + _versionStr);

	_rendererStr = gl.glGetString(GL.GL_RENDERER);
	System.err.println("OpenGL renderer: " + _rendererStr);

	float values[] = new float[2];
	gl.glGetFloatv(GL2.GL_LINE_WIDTH_GRANULARITY, values, 0);
	System.err
		.println("GL.GL_LINE_WIDTH_GRANULARITY value is " + values[0]);

	gl.glGetFloatv(GL2.GL_LINE_WIDTH_RANGE, values, 0);
	System.err.println("GL.GL_LINE_WIDTH_RANGE values are " + values[0]
		+ ", " + values[1]);

	// Global settings.
	gl.glEnable(GL2.GL_POLYGON_STIPPLE);
	gl.glClearColor(0.9f, 0.9f, 0.9f, 1.0f); // set background (clear) color
	gl.glClearDepth(1.0f); // set clear depth value to farthest
	//gl.glEnable(GL.GL_DEPTH_TEST); // enables depth testing
	 gl.glDepthFunc(GL.GL_LEQUAL); // the type of depth test to do
	//gl.glDepthFunc(GL.GL_ALWAYS); // the type of depth test to do
	// best perspective correction
	gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
	// blends colors, smoothes lighting
	// gl.glShadeModel(GL2ES1.GL_SMOOTH);
	gl.glShadeModel(GL2.GL_FLAT);

	gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	// gl.glDepthMask(false);
	gl.glEnable(GL.GL_BLEND);
	gl.glEnable(GL2.GL_COLOR);
	gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL.GL_DONT_CARE);
	gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);

	// float pos[] = { 0.0f, 0.0f, 10.0f, 0.0f };

	// gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos, 0);
	// // gl.glEnable(GL2.GL_CULL_FACE);
	// gl.glEnable(GL2.GL_LIGHTING);
	// gl.glEnable(GL2.GL_LIGHT0);

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
	    int height) {
	System.err.println("called reshape");
	GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context

	if (height == 0) {
	    height = 1; // prevent divide by zero
	}

	float aspect = (float) width / height;

	// Set the view port (display area) to cover the entire window
	gl.glViewport(0, 0, width, height);

	// Setup perspective projection, with aspect ratio matches viewport
	gl.glMatrixMode(GL2ES1.GL_PROJECTION); // choose projection matrix
	gl.glLoadIdentity(); // reset projection matrix

	// arguments are fovy, aspect, znear, zFar
	glu.gluPerspective(45.0, aspect, 0.1, 10000.0);

	// Enable the model-view transform
	gl.glMatrixMode(GL2ES1.GL_MODELVIEW);
	gl.glLoadIdentity(); // reset
	_dirty = true;
    }

    /**
     * Set rotation angle about x
     * 
     * @param angle
     *            about x (degrees)
     */
    public void setRotationX(float angle) {
	_view_rotx = angle;
	_dirty = true;
    }

    /**
     * Set rotation angle about y
     * 
     * @param angle
     *            about y (degrees)
     */
    public void setRotationY(float angle) {
	_view_roty = angle;
	_dirty = true;
    }

    /**
     * Set rotation angle about z
     * 
     * @param angle
     *            about z (degrees)
     */
    public void setRotationZ(float angle) {
	_view_rotz = angle;
	_dirty = true;
    }

    /**
     * Get the rotation about x
     * 
     * @return the rotation about x (degrees)
     */
    public float getRotationX() {
	return _view_rotx;
    }

    /**
     * Get the rotation about y
     * 
     * @return the rotation about y (degrees)
     */
    public float getRotationY() {
	return _view_roty;
    }

    /**
     * Get the rotation about z
     * 
     * @return the rotation about z (degrees)
     */
    public float getRotationZ() {
	return _view_rotz;
    }

    /**
     * Change the x distance to move in or out
     * 
     * @param dx
     *            the change in x
     */
    public void deltaX(float dx) {
	_xdist += dx;
	_dirty = true;
    }

    /**
     * Change the y distance to move in or out
     * 
     * @param dy
     *            the change in y
     */
    public void deltaY(float dy) {
	_ydist += dy;
	_dirty = true;
    }

    /**
     * Change the z distance to move in or out
     * 
     * @param dz
     *            the change in z
     */
    public void deltaZ(float dz) {
	_zdist += dz;
	_dirty = true;
    }

    /**
     * Refresh the drawing
     */
    public void refresh() {
	setDirty(true);
	// if (gljpanel != null) {
	// gljpanel.display();
	// }
    }

    /**
     * Add an item to the list. Note that this does not initiate a redraw.
     * 
     * @param item
     *            the item to add.
     */
    public void addItem(Item3D item) {
	if (item != null) {
	    _itemList.remove(item);
	    _itemList.add(item);
	    _dirty = true;
	}
    }

    /**
     * Remove an item from the list. Note that this does not initiate a redraw.
     * 
     * @param item
     *            the item to remove.
     */
    public void removeItem(Item3D item) {
	if (item != null) {
	    _itemList.remove(item);
	    _dirty = true;
	}
    }

    /**
     * Conver GL coordinates to screen coordinates
     * 
     * @param gl
     *            graphics context
     * @param objX
     *            GL x coordinate
     * @param objY
     *            GL y coordinate
     * @param objZ
     *            GL z coordinate
     * @param winPos
     *            should be float[3]. Will hold screen coords as floats as [x,
     *            y, z]. Not sure what z is--ignore.
     */
    public void project(GL2 gl, float objX, float objY, float objZ,
	    float winPos[]) {

	int[] view = new int[4];
	gl.glGetIntegerv(GL.GL_VIEWPORT, view, 0);

	float[] model = new float[16];
	gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, model, 0);

	float[] proj = new float[16];
	gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, proj, 0);

	glu.gluProject(objX, objY, objZ, model, 0, proj, 0, view, 0, winPos, 0);

	// System.err.println("pos: (" + objX + ", " + objY + ", " + objZ +
	// ") screen: (" + winPos[0] + ", " + winPos[1] + ", " + winPos[2] +
	// ")");
    }

    /**
     * This gets the z step used by the mouse and key adapters, to see how fast
     * we move in or in in response to mouse wheel or up/down arrows. It should
     * be overridden to give something sensible. like the scale/100;
     * 
     * @return the z step (changes to zDist) for moving in and out
     */
    public float getZStep() {
	return 0.1f;
    }

    /**
     * Main program for testing. Put the panel on JFrame,
     * 
     * @param arg
     */
    public static void main(String arg[]) {
	final JFrame testFrame = new JFrame("bCNU 3D Panel Test");

	int n = 10000;
	if (arg.length > 0) {
	    n = Integer.parseInt(arg[0]);
	}

	final int num = n;

	testFrame.setLayout(new BorderLayout(4, 4));

	final float xymax = 600f;
	final float zmax = 600f;
	final float zmin = -100f;
	final float xdist = -100f;
	final float ydist = 0f;
	final float zdist = -1600f;

	final float thetax = 0f;
	final float thetay = 90f;
	final float thetaz = 90f;

	Panel3D p3d = new Panel3D(thetax, thetay, thetaz, xdist, ydist, zdist) {
	    @Override
	    public void createInitialItems() {
		// coordinate axes

		Axes3D axes = new Axes3D(this, -xymax, xymax, -xymax, xymax,
			zmin, zmax, Color.darkGray, 1f, 7, 7, 8, Color.blue,
			new Font("SansSerif", Font.PLAIN, 11), 0);
		addItem(axes);

		// Cube cube = new Cube(this, 0.25f, 0.25f, 0.25f, 0.5f,
		// Color.yellow);
		// addItem(cube);

		// System.err.println("test with " + num + " lines.");
		// Line3D.lineItemTest(this, num);

		// Cube.cubeTest(this, 40000);

	    }

	    /**
	     * This gets the z step used by the mouse and key adapters, to see
	     * how fast we move in or in in response to mouse wheel or up/down
	     * arrows. It should be overridden to give something sensible. like
	     * the scale/100;
	     * 
	     * @return the z step (changes to zDist) for moving in and out
	     */
	    @Override
	    public float getZStep() {
		return (zmax - zmin) / 50f;
	    }

	};

	testFrame.add(p3d, BorderLayout.CENTER);

	// set up what to do if the window is closed
	WindowAdapter windowAdapter = new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent event) {
		System.err.println("Done");
		System.exit(1);
	    }
	};

	testFrame.addWindowListener(windowAdapter);
	testFrame.setBounds(200, 100, 900, 700);

	javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		testFrame.setVisible(true);
	    }
	});

    }
    

    /**
     * Controls whether redraw is needed
     * 
     * @param dirty
     */
    public void setDirty(boolean dirty) {
	_dirty = dirty;
    }

}
