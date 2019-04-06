package item3D;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.bCNU.geometry.Point;
import cnuphys.bCNU.geometry.Slab;

public class SlabItem extends Item3D {

	// the underlying Slab object
	private Slab _slab;

	public SlabItem(Panel3D panel3D, Point s1, Point e1, Point s2, Point e2, Point s3, Point e3, Point s4, Point e4, Color fillColor, Color lineColor, float lineWidth) {
		super(panel3D);
		_slab = new Slab(s1, e1, s2, e2, s3, e3, s4, e4);
		setFillColor(fillColor);
		setLineColor(lineColor);
		setLineWidth(lineWidth);
	}

	@Override
	public void draw(GLAutoDrawable drawable) {
		float quads[] = new float[72];
		_slab.getQuads(quads);
		
		Color fillColor =  getFillColor();
		Color lineColor =  getLineColor();
        Support3D.drawQuads(drawable, quads, fillColor, lineColor, 1f);
	}
	
	public static SlabItem testSlab(Panel3D p3d, double dxy, double dz) {
		Point s1 = new Point(0, 0, 0);
		Point e1 = new Point(0, 0, dz);
		Point s2 = new Point(dxy, 0, 0);
		Point e2 = new Point(dxy, 0, dz);
		Point s3 = new Point(dxy, dxy, 0);
		Point e3 = new Point(dxy, dxy, dz);
		Point s4 = new Point(0, dxy, 0);
		Point e4 = new Point(0, dxy, dz);
		return new SlabItem(p3d, s1, e1, s2, e2, s3, e3, s4, e4, Color.green, Color.gray, 2.f);
	}

}
