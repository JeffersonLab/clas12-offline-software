package cnuphys.ced.ced3d;

import item3D.Axes3D;

import java.awt.Color;
import java.awt.Font;

import cnuphys.ced.geometry.FTOFGeometry;
import cnuphys.lund.X11Colors;

public class ForwardPanel3D extends CedPanel3D {

    private final float xymax = 600f;
    private final float zmax = 600f;
    private final float zmin = -100f;
    
    //labels for the check box
    public static final String SHOW_VOLUMES = "Volumes";
    public static final String SHOW_TRUTH = "GEMC Truth";
    public static final String SHOW_DC = "DC";
    public static final String SHOW_EC = "EC";
    public static final String SHOW_PCAL = "PCAL";
    public static final String SHOW_FTOF = "FTOF";
    public static final String SHOW_DOCA = "DC DOCA";
    private static final String _cbaLabels[] = {SHOW_VOLUMES, SHOW_TRUTH, SHOW_DC, SHOW_EC, SHOW_PCAL, SHOW_FTOF, SHOW_DOCA};


    public ForwardPanel3D(float angleX, float angleY, float angleZ,
	    float xDist, float yDist, float zDist) {
	super(angleX, angleY, angleZ, xDist, yDist, zDist, _cbaLabels);
    }
    
    @Override
    public void createInitialItems() {
	//coordinate axes
	Axes3D axes = new Axes3D(this, -xymax, xymax, -xymax, xymax, zmin, zmax, Color.darkGray, 1f, 7, 7, 8, Color.black,
		X11Colors.getX11Color("Dark Green"), new Font("SansSerif", Font.PLAIN, 12), 0);
	addItem(axes);
	
	//trajectory drawer
	TrajectoryDrawer3D trajDrawer = new TrajectoryDrawer3D(this);
	addItem(trajDrawer);
	
 	
	//dc super layers
	for (int sector = 1; sector <= 6; sector++) {
	    for (int superlayer = 1; superlayer <= 6; superlayer++) {
		DCSuperLayer3D dcsl = new DCSuperLayer3D(this, sector, superlayer);
		addItem(dcsl);
	    }
	}
	
	// tof paddles
	for (int sector = 1; sector <= 6; sector++) {
	    for (int superlayer = 0; superlayer < 3; superlayer++) {
		for (int paddleId = 1; paddleId <= FTOFGeometry.numPaddles[superlayer]; paddleId++) {
		    FTOFPaddle3D ftofpad = new FTOFPaddle3D(this, sector,
			    superlayer, paddleId);
		    addItem(ftofpad);
		}
	    }
	}
	
	//mc hit drawer
	MCHitDrawer3D mchd = new MCHitDrawer3D(this);
	addItem(mchd);
	
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
