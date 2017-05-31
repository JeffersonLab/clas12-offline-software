package bCNU3D;

public class Vector3f {
	
    /**
     * The coordinates
     */
    public float x, y, z;

    /**
     * null constructor; all coordinates are 0
     */
	public Vector3f() {
	    this(0, 0, 0);
	}

	/**
	 * Constructor
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
	public Vector3f(float x, float y, float z) {
	    this.x = x;
	    this.y = y;
	    this.z = z;
	}
	
	/**
	 * Constructor using a set of coordinates
	 * @param coords [x, y, z, x, y, z, etc]
	 * @param index start at 3*index for the x coordinate
	 */
	public Vector3f(float coords[], int index) {
	    int j = 3*index;
	    x = coords[j];
	    y = coords[j+1];
	    z = coords[j+2];
	}
	
	/**
	 * Obtain a vector that is the mispoint of two other vectors
	 * @param v1 one vector
	 * @param v2 the other vector
	 * @return the midpoint 
	 */
	public static Vector3f midpoint (Vector3f v1, Vector3f v2) {
	    float x= 0.5f*(v1.x + v2.x);
	    float y= 0.5f*(v1.y + v2.y);
	    float z= 0.5f*(v1.z + v2.z);
	    return new Vector3f(x, y, z);
	}

}
