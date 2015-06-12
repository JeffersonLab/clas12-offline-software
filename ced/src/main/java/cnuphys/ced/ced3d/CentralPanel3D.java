package cnuphys.ced.ced3d;

import item3D.Axes3D;

import java.awt.Color;
import java.awt.Font;

import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.lund.X11Colors;

public class CentralPanel3D extends CedPanel3D {

    private final float xymax = 25f;
    private final float zmax = 30f;
    private final float zmin = -30f;
    
    //labels for the check box
    public static final String SHOW_VOLUMES = "Volumes";
    public static final String SHOW_TRUTH = "GEMC Truth";
    public static final String SHOW_SVT = "SVT";
    private static final String _cbaLabels[] = {SHOW_VOLUMES, SHOW_TRUTH, SHOW_SVT};

    public CentralPanel3D(float angleX, float angleY, float angleZ,
	    float xDist, float yDist, float zDist) {
	super(angleX, angleY, angleZ, xDist, yDist, zDist, _cbaLabels);
    }
    
    @Override
    public void createInitialItems() {
	//coordinate axes
	Axes3D axes = new Axes3D(this, -xymax, xymax, -xymax, xymax, zmin, zmax, Color.darkGray, 1f, 6, 6, 7, 
		X11Colors.getX11Color("Dark Green"), new Font("SansSerif", Font.PLAIN, 12), 0);
	addItem(axes);
	
	//trajectory drawer
	TrajectoryDrawer3D trajDrawer = new TrajectoryDrawer3D(this);
	addItem(trajDrawer);
	
	//svt panels
	for (int layer = 1; layer <= 8; layer++) {
	    // geom service uses 0-based superlayer [0,1,2,3] and layer [0,1]
	    int supl = ((layer - 1) / 2); // 0, 1, 2, 3
	    for (int sector = 1; sector <= BSTGeometry.sectorsPerSuperlayer[supl]; sector++) {
		SVTPanel3D svt = new SVTPanel3D(this, sector, layer);
		addItem(svt);
	    }
	}
		
  }
    
    
    /**
     * This gets the z step used by the mouse and key adapters, to see how
     * fast we move in or in in response to mouse wheel or up/down arrows.
     * It should be overridden to give something sensible. like the scale/100;
     * @return the z step (changes to zDist) for moving in and out
     */
    @Override
    public float getZStep() {
	return (zmax-zmin)/50f;
    }

    
}
