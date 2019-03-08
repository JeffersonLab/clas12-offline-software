package cnuphys.lund;

public class TrajectoryRowData {

	protected int trackId; // track id

	// the lund id
	protected LundId lundId;

	// vertex x in cm
	protected double xo;

	// vertex y in cm
	protected double yo;

	// vertex z in cm
	protected double zo;

	// momentum (MeV/c)
	protected double p;

	// polar angle (deg)
	protected double theta;

	// azimuthal angle (deg)
	protected double phi;

	// status
	protected int status;

	// source
	protected String source;

	/**
	 * Create a data row for display in the table.
	 * 
	 * @param lundId the full lundId object
	 * @param xo     the vertex x (cm)
	 * @param yo     the vertex y (cm)
	 * @param zo     the vertex z (cm)
	 * @param p      the initial momentum (MeV/c)
	 * @param theta  the initial polar angle (degrees)
	 * @param phi    the initial azimuthal angle (degrees)
	 */
	public TrajectoryRowData(int trackId, LundId lundId, double xo, double yo, double zo, double p, double theta,
			double phi, int status, String source) {
		super();
		this.trackId = trackId;
		this.lundId = lundId;
		this.xo = xo;
		this.yo = yo;
		this.zo = zo;
		this.p = p;
		this.theta = theta;
		this.phi = phi;
		this.status = status;
		this.source = source;

		if (lundId == null) {
			lundId = LundSupport.getInstance().get(0);
		}
	}

	/**
	 * Get the track id
	 * 
	 * @return the track id
	 */
	public int getTrackId() {
		return trackId;
	}

	/**
	 * Get the charge in units of e.
	 * 
	 * @return the charge. -99 on failure.
	 */
	public int getCharge() {
		return (lundId == null) ? -99 : lundId.getCharge();
	}

	/**
	 * Get the source, probably a bank name
	 * 
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Get the status
	 * 
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * The mass in MeV
	 * 
	 * @return the mass in MeV
	 */
	public double getMass() {
		return 1000.0 * lundId.getMass();
	}

	/**
	 * Get the name of the particle
	 * 
	 * @return the name of the particle
	 */
	public String getName() {
		return lundId.getName();
	}

	/**
	 * The total energy in MeV
	 * 
	 * @return the total energy in MeV
	 */
	public double getTotalEnergy() {
		double m = getMass();
		return Math.sqrt(p * p + m * m);
	}

	/**
	 * Get the polar angle
	 * 
	 * @return the polar angle in degrees.
	 */
	public double getTheta() {
		return theta;
	}

	/**
	 * Get the azimuthal angle
	 * 
	 * @return the azimuthal angle in degrees.
	 */
	public double getPhi() {
		return phi;
	}

	/**
	 * Get the vertex x coordinate
	 * 
	 * @return the vertex x coordinate in cm
	 */
	public double getXo() {
		return xo;
	}

	/**
	 * Get the vertex y coordinate
	 * 
	 * @return the vertex y coordinate in cm
	 */
	public double getYo() {
		return yo;
	}

	/**
	 * Get the vertex z coordinate
	 * 
	 * @return the vertex z coordinate in cm
	 */
	public double getZo() {
		return zo;
	}

	/**
	 * Get the lund (pdg) is for the particle
	 * 
	 * @return the lund (pdg) is for the particle
	 */
	public int getId() {
		return lundId.getId();
	}

	/**
	 * Get the lund id
	 * 
	 * @return the lund id
	 */
	public LundId getLundId() {
		return lundId;
	}

	/**
	 * Get the momentum in MeV
	 * 
	 * @return the momentum in MeV
	 */
	public double getMomentum() {
		return p;
	}

	/**
	 * Get the kinetic energy in MeV
	 * 
	 * @return the kinetic energy in MeV
	 */
	public double getKineticEnergy() {
		return getTotalEnergy() - getMass();
	}
}
