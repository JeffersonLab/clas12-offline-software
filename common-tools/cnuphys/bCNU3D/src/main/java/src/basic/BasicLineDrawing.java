package basic;

import java.awt.Color;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Vector3f;

public class BasicLineDrawing {
	
	
	/**
	 * Draw a line
	 * @param drawable the GL drawable
	 * @param x1 x coordinate of start
	 * @param y1 y coordinate of start
	 * @param z1 z coordinate of start
	 * @param x2 x coordinate of end
	 * @param y2 y coordinate of end
	 * @param z2 z coordinate of end
	 */
	public static void drawLine(GLAutoDrawable drawable, float x1, float y1, float z1, float x2, float y2, float z2) {
		GL2 gl2 = drawable.getGL().getGL2();
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex3f(x1, y1, z1);
		gl2.glVertex3f(x2, y2, z2);
		gl2.glEnd();
	}
	
	/**
	 * Draw a line
	 * @param drawable  the GL drawable
	 * @param p1 one end of the line
	 * @param p2 the other end of the line
	 */
	public static void drawLine(GLAutoDrawable drawable, Vector3f p1, Vector3f p2) {
		drawLine(drawable, p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
	}
	
	/**
	 * Draw lines (not connected)
	 * @param drawable the GL drawable
	 * @param x1 x coordinates of start of line
	 * @param y1 y coordinates of start of line
	 * @param z1 z coordinates of start of line
	 * @param x2 x coordinates of end of line
	 * @param y2 y coordinates of end of line
	 * @param z2 z coordinates of end of line
	 */
	public static void drawLines(GLAutoDrawable drawable, float x1[], float y1[], float z1[], float x2[], float y2[],
			float z2[], Color color) {

		int len = (x1 != null) ? x1.length : 0;

		if (len != 0) {
			GL2 gl2 = drawable.getGL().getGL2();
			
			BasicColorSupport3D.setColor(gl2, color);
			
			gl2.glBegin(GL2.GL_LINES);

			for (int i = 0; i < len; i++) {
				gl2.glBegin(GL2.GL_LINES);
				gl2.glVertex3f(x1[i], y1[i], z1[i]);
				gl2.glVertex3f(x2[i], y2[i], z2[i]);
				gl2.glEnd();

			}
			gl2.glFlush();
		}		
		
		
	}

}
