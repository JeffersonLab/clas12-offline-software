package cnuphys.ced.event.data;

public class Hit1 extends BaseHit {

	/** Energy in MeV*/
	public float energy;  //energy in MeV
	
	/** CLAS system x coordinate in cm */
	public float x;
	
	/** CLAS system y coordinate in cm */	
	public float y;
	
	/** CLAS system z coordinate in cm */
	public float z;
	

	
	/**
	 * @param sector the 1-based sector
	 * @param layer the 1-based layer
	 * @param component the 1-based component
	 * @param energy energy in MeV
	 * @param x x coordinate (CLAS system)  in cm
	 * @param y y coordinate (CLAS system)  in cm
	 * @param z z coordinate (CLAS system)  in cm
	 */
	public Hit1(byte sector, byte layer, short component, float energy, float x, float y, float z) {
		super(sector, layer, component);
		this.energy = energy;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	/**
	 * Get the azimuthal value of this hit
	 * @return the azimuthal value (phi) of this hit in degrees
	 */
	public double phi() {
		return Math.toDegrees(Math.atan2(y, x));
	}
	
	
	public static void main(String arg[]) {
		new Hit1((byte)0, (byte)0, (short)0, 0.f, 0.f, 0.f, 0.f);
	}
	
}
