package item3D;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Panel3D;
import cnuphys.bCNU.geometry.Point;
import cnuphys.bCNU.geometry.Slab;

public class SlabItem extends Item3D {

	// the underlying Slab object
	private Slab _slab;

	public SlabItem(Panel3D panel3D, Point s1, Point e1, Point s2, Point e2, Point s3, Point e3, Point s4, Point e4) {
		super(panel3D);
		_slab = new Slab(s1, e1, s2, e2, s3, e3, s4, e4);
	}

	@Override
	public void draw(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

}
