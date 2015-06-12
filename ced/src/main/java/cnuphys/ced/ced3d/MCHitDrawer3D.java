package cnuphys.ced.ced3d;

import java.awt.Color;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.ADataContainer;
import cnuphys.ced.event.data.DCDataContainer;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.ECDataContainer;
import cnuphys.ced.event.data.FTOFDataContainer;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

import com.jogamp.opengl.GLAutoDrawable;

import item3D.Item3D;

public class MCHitDrawer3D extends Item3D {
    
    // the event manager
    ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

    private static Color color = X11Colors.getX11Color("Dark Blue");
    private static final float POINTSIZE = 3f;

    public MCHitDrawer3D(Panel3D panel3d) {
	super(panel3d);
    }

    @Override
    public void draw(GLAutoDrawable drawable) {
		
	if (!showMCTruth()) {
	    return;
	}
	
	DCDataContainer dcData = _eventManager.getDCData();
	FTOFDataContainer ftofData = _eventManager.getFTOFData();
	ECDataContainer ecData = _eventManager.getECData();
	
	showGemcXYZHits(drawable, dcData, dcData.dc_true_avgX,
		dcData.dc_true_avgY, dcData.dc_true_avgZ, dcData.dc_true_pid, 0);
	showGemcXYZHits(drawable, ftofData, ftofData.ftof1a_true_avgX,
		ftofData.ftof1a_true_avgY, ftofData.ftof1a_true_avgZ, ftofData.ftof1a_true_pid, 0);
	showGemcXYZHits(drawable, ftofData, ftofData.ftof1b_true_avgX,
		ftofData.ftof1b_true_avgY, ftofData.ftof1b_true_avgZ, ftofData.ftof1b_true_pid, 0);
	showGemcXYZHits(drawable, ftofData, ftofData.ftof2b_true_avgX,
		ftofData.ftof2b_true_avgY, ftofData.ftof2b_true_avgZ, ftofData.ftof2b_true_pid, 0);
	
   }

    //show MC Truth?
    protected boolean showMCTruth() {
	return ((ForwardPanel3D)_panel3D).show(ForwardPanel3D.SHOW_TRUTH);
    }
    
    
    private void showGemcXYZHits(GLAutoDrawable drawable,
	    ADataContainer data, double x[], double y[], double z[], int pid[],
	    int option) {

	if ((x == null) || (y == null) || (z == null) || (x.length < 1)) {
	    return;
	}

	// should not be necessary but be safe
	int len = x.length;
	len = Math.min(len, y.length);
	len = Math.min(len, z.length);
	
	if (len < 1) {
	    return;
	}
	
	float coords[] = new float[3*len];
	
	for (int hitIndex = 0; hitIndex < len; hitIndex++) {
	    int j = 3*hitIndex;
	    coords[j] = (float) (x[hitIndex] / 10); // mm to cm
	    coords[j+1] = (float) (y[hitIndex] / 10); // mm to cm
	    coords[j+2] = (float) (z[hitIndex] / 10); // mm to cm
	}
	Support3D.drawPoints(drawable, coords, color, POINTSIZE);

    }


}
