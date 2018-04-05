package basic;

import java.awt.Color;

import com.jogamp.opengl.GL2;

public class BasicColorSupport3D {

	/**
	 * Set the color
	 * @param gl2 the context
	 * @param color the color
	 */
	public static void setColor(GL2 gl2, Color color) {
		float rf = color.getRed()/255f;
		float gf = color.getGreen()/255f;
		float bf = color.getBlue()/255f;
		float af = color.getAlpha()/255f;
		gl2.glColor4f(rf, gf, bf, af);
	}
}
