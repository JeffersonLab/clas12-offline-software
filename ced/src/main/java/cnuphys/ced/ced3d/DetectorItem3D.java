package cnuphys.ced.ced3d;

import java.awt.Color;

import item3D.Item3D;
import bCNU3D.Panel3D;
import cnuphys.ced.clasio.ClasIoEventManager;

import com.jogamp.opengl.GLAutoDrawable;

public abstract class DetectorItem3D extends Item3D {
    
    protected static final Color dgtzColor = new Color(255, 0, 0, 128);

    // the event manager
    ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

    public DetectorItem3D(Panel3D panel3d) {
	super(panel3d);
    }

    @Override
    public void draw(GLAutoDrawable drawable) {
	drawData(drawable);
	drawShape(drawable);
    }
    
    /**
     * Draw the boundary
     * @param drawable the GL drawable
     */
    public abstract void drawShape(GLAutoDrawable drawable);
    
    /**
     * Draw the data
     * @param drawable the GL drawable
     */
    public abstract void drawData(GLAutoDrawable drawable);

    //show MC Truth?
    protected boolean showMCTruth() {
	return ((CedPanel3D)_panel3D).show(CedPanel3D.SHOW_TRUTH);
    }


}
