package cnuphys.ced.ced3d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;

import org.jlab.geom.component.DriftChamberWire;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.ADataContainer;
import cnuphys.ced.event.data.DCDataContainer;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.geometry.DCGeometry;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

import com.jogamp.opengl.GLAutoDrawable;

public class DCSuperLayer3D extends DetectorItem3D {
    
    protected static final Color outlineColor = new Color(0, 200, 200, 2);

    private static final boolean frame = true;


    
    //one based sector [1..6]
    private final int _sector;
    
    // one based superlayer [1..6]
    private final int _superLayer;
    
    //the vertices
    private float[] coords = new float[18];
    

    /**
     * The owner panel
     * @param panel3d
     * @param sector one based sector [1..6]
     * @param superLayer one based superlayer [1..6]
     */
    public DCSuperLayer3D(Panel3D panel3d, int sector, int superLayer) {
	super(panel3d);
	_sector = sector;
	_superLayer = superLayer;
	DCGeometry.superLayerVertices(_sector, _superLayer, coords);
    }

    @Override
    public void drawShape(GLAutoDrawable drawable) {
	if (!show()) {
	    return;
	}
	
	Support3D.drawTriangle(drawable, coords, 0, 1, 2, outlineColor, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 1, 4, 3, 0, outlineColor, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 0, 3, 5, 2, outlineColor, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 1, 4, 5, 2, outlineColor, 1f, frame);
 	Support3D.drawTriangle(drawable, coords, 3, 4, 5, outlineColor, 1f, frame);
 	
// 	if (_sector == 1) {
// 	    Support3D.wireSphere(drawable, 100f, 0, 50f, 50f, 20, 10, Color.yellow);
// 	}
   }

    @Override
    public void drawData(GLAutoDrawable drawable) {
	if (!show()) {
	    return;
	}
	
	if (_eventManager.isAccumulating()) {
	    return;
	}

	DCDataContainer dcData = _eventManager.getDCData();
	float coords[] = new float[6];
	for (int i = 0; i < dcData.getHitCount(0); i++) {
	    try {
		int sect1 = dcData.dc_dgtz_sector[i]; // 1 based
		if (sect1 == _sector) {
		    int supl1 = dcData.dc_dgtz_superlayer[i]; // 1 based
		    if (supl1 == _superLayer) {
			int lay1 = dcData.dc_dgtz_layer[i];
			int wire1 = dcData.dc_dgtz_wire[i];
			getWire(lay1, wire1, coords);
			Support3D.drawLine(drawable, coords, dgtzColor, 1f);
		    }
		}
	    }
	    catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
    
    
    /**
     * Get the 1-based sector [1..6]
     * @return the 1-based sector [1..6]
     */
    public int getSector() {
	return _sector;
    }

    /**
     * Get the 1-based super layer [1..6]
     * @return the 1-based super layer [1..6]
     */
    public int getSuperLayer() {
	return _superLayer;
    }
    
    private void getWire(int layer, int wire, float coords[]) {
	org.jlab.geom.prim.Line3D dcwire = DCGeometry.getWire(_sector, _superLayer, layer, wire);
	org.jlab.geom.prim.Point3D p0 = dcwire.origin();
	org.jlab.geom.prim.Point3D p1 = dcwire.end();
	coords[0] = (float) p0.x();
	coords[1] = (float) p0.y();
	coords[2] = (float) p0.z();
	coords[3] = (float) p1.x();
	coords[4] = (float) p1.y();
	coords[5] = (float) p1.z();
    }
    
    //show DCs?
    private boolean show() {
	return ((CedPanel3D)_panel3D).show(CedPanel3D.SHOW_DC);
    }

}
