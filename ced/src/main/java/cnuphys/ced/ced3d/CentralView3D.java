package cnuphys.ced.ced3d;

public class CentralView3D extends CedView3D {

    public static final float xdist = 0f;
    public static final float ydist = 0f;
    public static final float zdist = -140f;
    
    private static final float thetax = 0f;
    private static final float thetay = 90f;
    private static final float thetaz = 90f;

    public CentralView3D() {
	super("Central Detectors 3D View", thetax, thetay, thetaz, 
		xdist, ydist, zdist);
    }

    @Override
    protected CedPanel3D make3DPanel(float angleX, float angleY, float angleZ,
	    float xDist, float yDist, float zDist) {
	return new CentralPanel3D(angleX, angleY, angleZ,
		xDist, yDist, zDist);
    }

}
