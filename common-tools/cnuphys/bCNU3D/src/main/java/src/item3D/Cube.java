package item3D;

import java.awt.Color;
import bCNU3D.Panel3D;
import bCNU3D.Support3D;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GLAutoDrawable;

public class Cube extends Item3D {

	// x coordinate of center of cube
	private float _xc;

	// y coordinate of center of cube
	private float _yc;

	// z coordinate of center of cube
	private float _zc;

	// half length
	private float _halfLength;
	
	// frame?
	protected boolean _frame;
	
	public Cube(Panel3D panel3D, float xc, float yc, float zc, float length, Color color) {
		this(panel3D, xc, yc, zc, length, color, false);
	}

	public Cube(Panel3D panel3D, float xc, float yc, float zc, float length, Color color, boolean frame) {
		super(panel3D);
		_xc = xc;
		_yc = yc;
		_zc = zc;
		_frame = frame;
		_halfLength = length / 2;
		setFillColor(color);
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

		Support3D.setColor(gl, getFillColor());
		gl.glBegin(GL2ES3.GL_QUADS);
		gl.glVertex3f(xm, ym, zp);
		gl.glVertex3f(xm, yp, zp);
		gl.glVertex3f(xp, yp, zp);
		gl.glVertex3f(xp, ym, zp);
		gl.glEnd();

		gl.glBegin(GL2ES3.GL_QUADS);
		gl.glVertex3f(xm, ym, zm);
		gl.glVertex3f(xm, yp, zm);
		gl.glVertex3f(xp, yp, zm);
		gl.glVertex3f(xp, ym, zm);
		gl.glEnd();

		gl.glBegin(GL2ES3.GL_QUADS);
		gl.glVertex3f(xm, yp, zm);
		gl.glVertex3f(xm, yp, zp);
		gl.glVertex3f(xp, yp, zp);
		gl.glVertex3f(xp, yp, zm);
		gl.glEnd();

		gl.glBegin(GL2ES3.GL_QUADS);
		gl.glVertex3f(xm, ym, zm);
		gl.glVertex3f(xm, ym, zp);
		gl.glVertex3f(xp, ym, zp);
		gl.glVertex3f(xp, ym, zm);
		gl.glEnd();

		gl.glBegin(GL2ES3.GL_QUADS);
		gl.glVertex3f(xp, yp, zm);
		gl.glVertex3f(xp, yp, zp);
		gl.glVertex3f(xp, ym, zp);
		gl.glVertex3f(xp, ym, zm);
		gl.glEnd();

		gl.glBegin(GL2ES3.GL_QUADS);
		gl.glVertex3f(xm, yp, zm);
		gl.glVertex3f(xm, yp, zp);
		gl.glVertex3f(xm, ym, zp);
		gl.glVertex3f(xm, ym, zm);
		gl.glEnd();
		
		if (_frame) {
			Support3D.setColor(gl, Color.gray);

			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glVertex3f(xm, yp, zm);
			gl.glVertex3f(xm, yp, zp);
			gl.glVertex3f(xm, ym, zp);
			gl.glVertex3f(xm, ym, zm);
			gl.glVertex3f(xm, yp, zm);
			gl.glEnd();

			
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glVertex3f(xm, ym, zm);
			gl.glVertex3f(xm, yp, zm);
			gl.glVertex3f(xp, yp, zm);
			gl.glVertex3f(xp, ym, zm);
			gl.glVertex3f(xm, ym, zm);
			gl.glEnd();
		
			
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glVertex3f(xm, yp, zm);
			gl.glVertex3f(xm, yp, zp);
			gl.glVertex3f(xp, yp, zp);
			gl.glVertex3f(xp, yp, zm);
			gl.glVertex3f(xm, yp, zm);
			gl.glEnd();
		
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glVertex3f(xm, ym, zm);
			gl.glVertex3f(xm, ym, zp);
			gl.glVertex3f(xp, ym, zp);
			gl.glVertex3f(xp, ym, zm);
			gl.glVertex3f(xm, ym, zm);
			gl.glEnd();

			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glVertex3f(xp, yp, zm);
			gl.glVertex3f(xp, yp, zp);
			gl.glVertex3f(xp, ym, zp);
			gl.glVertex3f(xp, ym, zm);
			gl.glVertex3f(xp, yp, zm);
			gl.glEnd();

			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glVertex3f(xm, yp, zm);
			gl.glVertex3f(xm, yp, zp);
			gl.glVertex3f(xm, ym, zp);
			gl.glVertex3f(xm, ym, zm);
			gl.glVertex3f(xm, yp, zm);
			gl.glEnd();
		}

	}


}
