package item3D;

import java.awt.Color;
import bCNU3D.Panel3D;
import bCNU3D.Support3D;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;

public class Cube extends Item3D {

    // x coordinate of center of cube
    private float _xc;

    // y coordinate of center of cube
    private float _yc;

    // z coordinate of center of cube
    private float _zc;
    
    //half length
    private float _halfLength;

    public Cube(Panel3D panel3D, float xc, float yc, float zc, float length,
	    Color color) {
	super(panel3D);
	_xc = xc;
	_yc = yc;
	_zc = zc;
	_halfLength = length/2;
	setColor(color);
    }

    @Override
    public void draw(GLAutoDrawable drawable) {
	GL2 gl = drawable.getGL().getGL2();
	
	float xm = _xc - _halfLength;
	float xp = _xc + _halfLength;
	float ym = _yc - _halfLength;
	float yp = _yc + _halfLength;
	float zm = _zc - _halfLength;
	float zp = _zc + _halfLength;
	
	Support3D.setColor(gl, getColor());
	gl.glBegin(GL2GL3.GL_QUADS);
	gl.glVertex3f(xm, ym, zp);
	gl.glVertex3f(xm, yp, zp);
	gl.glVertex3f(xp, yp, zp);
	gl.glVertex3f(xp, ym, zp);
	gl.glEnd();
	
	gl.glBegin(GL2GL3.GL_QUADS);
	gl.glVertex3f(xm, ym, zm);
	gl.glVertex3f(xm, yp, zm);
	gl.glVertex3f(xp, yp, zm);
	gl.glVertex3f(xp, ym, zm);
	gl.glEnd();
	
	gl.glBegin(GL2GL3.GL_QUADS);
	gl.glVertex3f(xm, yp, zm);
	gl.glVertex3f(xm, yp, zp);
	gl.glVertex3f(xp, yp, zp);
	gl.glVertex3f(xp, yp, zm);
	gl.glEnd();
	
	gl.glBegin(GL2GL3.GL_QUADS);
	gl.glVertex3f(xm, ym, zm);
	gl.glVertex3f(xm, ym, zp);
	gl.glVertex3f(xp, ym, zp);
	gl.glVertex3f(xp, ym, zm);
	gl.glEnd();
	
	gl.glBegin(GL2GL3.GL_QUADS);
	gl.glVertex3f(xp, yp, zm);
	gl.glVertex3f(xp, yp, zp);
	gl.glVertex3f(xp, ym, zp);
	gl.glVertex3f(xp, ym, zm);
	gl.glEnd();

	gl.glBegin(GL2GL3.GL_QUADS);
	gl.glVertex3f(xm, yp, zm);
	gl.glVertex3f(xm, yp, zp);
	gl.glVertex3f(xm, ym, zp);
	gl.glVertex3f(xm, ym, zm);
	gl.glEnd();
	
    }

    
    public static void cubeTest(Panel3D p3D, int n) {
	for (int i = 0; i < n; i++) {
	    float xc = 1f -2*(float) Math.random();
	    float yc = 1f -2*(float) Math.random();
	    float zc = 1f -2*(float) Math.random();
	    
	    int r = (int) (253*Math.random());
	    int g = (int) (253*Math.random());
	    int b = (int) (253*Math.random());
	    
	    Color c = new Color(r, g, b, 32);
	    
	    Cube item = new Cube(p3D, xc, yc, zc, 0.1f, c);
	    p3D.addItem(item);
	}
    }

}
