package cnuphys.ced.ced3d;

import java.awt.Color;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.ced.event.data.ECDataContainer;
import cnuphys.ced.geometry.ECGeometry;

import com.jogamp.opengl.GLAutoDrawable;

public class ECViewPlane3D extends DetectorItem3D {

    protected static final Color outlineColorInner = new Color(0, 128, 64, 16);
    protected static final Color outlineColorOuter = new Color(0, 64, 128, 16);
    protected static Color outlineColors[] = {null, outlineColorInner, outlineColorOuter}; 
    protected static final Color hitColor = new Color(255, 0, 0, 128);

    //sector is 1..6
    private final int _sector;
    
    // [1,2] for inner/outer like geometry "superlayer"
    private final int _stack;
    
    //[1, 2, 3] for [u, v, w] like geometry "layer+1"
    private final int _view;
    
    //the triangle coordinates
    private float _coords[];
    
    public ECViewPlane3D(Panel3D panel3d, int sector, int stack, int view) {
	super(panel3d);
	_sector = sector;
	_stack = stack;
	_view = view;
	_coords = new float[9];
	ECGeometry.getViewTriangle(sector, stack, view, _coords);
    }

    @Override
    public void drawShape(GLAutoDrawable drawable) {
	
	if (!show()) {
	    return;
	}
	
	Support3D.drawTriangle(drawable, _coords, outlineColors[_stack], 1f, true);
 
//	float coords[] = new float[24];
//	for (int strip = 1; strip <= 36; strip++) {
//	    ECGeometry.getStrip(_sector, _stack, _view, strip, coords);
//	    drawStrip(drawable, outlineColor, coords);
//	}
    }
    
    //draw a single strip
    private void drawStrip(GLAutoDrawable drawable, Color color, float coords[]) {
	
	boolean frame = true;
 	Support3D.drawQuad(drawable, coords, 0, 1, 2, 3, color, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 3, 7, 6, 2, color, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 0, 4, 7, 3, color, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 0, 4, 5, 1, color, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 1, 5, 6, 2, color, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 4, 5, 6, 7, color, 1f, frame);	
    }


    @Override
    public void drawData(GLAutoDrawable drawable) {
	if (!show()) {
	    return;
	}
	
	//has EC and PCAL data
	ECDataContainer ecData = _eventManager.getECData();
	
	int sector[] = ecData.ec_dgtz_sector;
	int stack[] = ecData.ec_dgtz_stack;
	int view[] = ecData.ec_dgtz_view;
	int strip[] = ecData.ec_dgtz_strip;
	
	if (sector != null) {
	    float coords[] = new float[24];
	    for (int i = 0; i < sector.length; i++) {
		if ((_sector == sector[i]) && (_stack == stack[i]) && (_view == view[i])) {
		    ECGeometry.getStrip(_sector, _stack, _view, strip[i], coords);
		    drawStrip(drawable, hitColor, coords);
		}
	    }
	}

    }

    // show ECs?
    private boolean show() {
	if (_stack == 1) {
	    return ((ForwardPanel3D) _panel3D).show(ForwardPanel3D.SHOW_EC_IN);

	} else {
	    return ((ForwardPanel3D) _panel3D).show(ForwardPanel3D.SHOW_EC_OUT);
	}
    }

    //show MC Truth?
    protected boolean showMCTruth() {
	return ((ForwardPanel3D)_panel3D).show(ForwardPanel3D.SHOW_TRUTH);
    }

    //show Volumes?
    protected boolean showVolumes() {
	return ((ForwardPanel3D)_panel3D).show(ForwardPanel3D.SHOW_VOLUMES);
    }

}
