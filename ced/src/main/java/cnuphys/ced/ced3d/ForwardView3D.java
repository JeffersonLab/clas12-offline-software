package cnuphys.ced.ced3d;

public class ForwardView3D extends CedView3D {

	public static final float xdist = -200f;
	public static final float ydist = 0f;
	public static final float zdist = -1600f;

	private static final float thetax = 0f;
	private static final float thetay = 90f;
	private static final float thetaz = 90f;

	public ForwardView3D() {
		super("Forward Detectors 3D View", thetax, thetay, thetaz, xdist,
				ydist, zdist);
	}

	@Override
	protected CedPanel3D make3DPanel(float angleX, float angleY, float angleZ,
			float xDist, float yDist, float zDist) {
		return new ForwardPanel3D(this, angleX, angleY, angleZ, xDist, yDist, zDist);
	}
}
