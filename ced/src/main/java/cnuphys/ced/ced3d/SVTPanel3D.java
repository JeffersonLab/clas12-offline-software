package cnuphys.ced.ced3d;

import java.awt.Color;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.ced.event.data.BSTDataContainer;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.lund.X11Colors;

import com.jogamp.opengl.GLAutoDrawable;

public class SVTPanel3D extends DetectorItem3D {
    
    protected static final Color outlineColor = X11Colors.getX11Color("wheat", 48);
    protected static final Color hitColor = new Color(255, 64, 0, 32);


    //the 1=based sect
    private int _sector;
    
    //the 1-based "biglayer" [1..8] used by the data
    private int _layer;
    
    public SVTPanel3D(Panel3D panel3d, int sector, int layer) {
	super(panel3d);
	_sector = sector;
	_layer = layer;
    }

    @Override
    public void drawShape(GLAutoDrawable drawable) {
	if (!show()) {
	    return;
	}
	float coords[] = new float[36];
	
	BSTGeometry.getLayerQuads(_sector, _layer, coords);
	Support3D.drawQuads(drawable, coords, outlineColor, 1f, true);
    }

    @Override
    public void drawData(GLAutoDrawable drawable) {
	if (!show()) {
	    return;
	    
	}

	BSTDataContainer bstData = _eventManager.getBSTData();

	int hitCount = bstData.getHitCount(0);
	float coords6[] = new float[6];
	float coords36[] = new float[36];
	
	
	boolean drawOutline = false;
	//now strips
	for (int i = 0; i < hitCount; i++) {
	    // 1 based sector
	    int sector = bstData.bst_dgtz_sector[i];
	    // "big layer" ..8
	    int layer = bstData.bst_dgtz_layer[i];

	    if ((_sector == sector) && (_layer == layer)) {
		// strip 1..256
		int strip = bstData.bst_dgtz_strip[i];

//		System.err.println("SVT hit: sector = " + sector + "  layer = "
//			+ layer + "  strip = " + strip);
		
		BSTGeometry.getStrip(sector, layer, strip, coords6);
		Support3D.drawLine(drawable, coords6, dgtzColor, 1f);
		drawOutline = true;
	    }
	}
	
	if (drawOutline) {
	    BSTGeometry.getLayerQuads(_sector, _layer, coords36);
	    Support3D.drawQuads(drawable, coords36, hitColor, 1f, true);
	}

    }

    // show SVT?
    private boolean show() {
	return ((CentralPanel3D) _panel3D).show(CentralPanel3D.SHOW_SVT);
    }
    
    //show MC Truth?
    protected boolean showMCTruth() {
	return ((CentralPanel3D)_panel3D).show(CentralPanel3D.SHOW_TRUTH);
    }

    //show Volumes?
    protected boolean showVolumes() {
	return ((CentralPanel3D)_panel3D).show(CentralPanel3D.SHOW_VOLUMES);
    }


}
