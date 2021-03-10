package cnuphys.ced.event.data;

/**
 * These are reconstructed (HB or TB) clusters
 * @author heddle
 *
 */
public class DCCluster {
	
	/** The 1-based sector */
	public byte sector;

	/** The 1-based superlayer 1..6 */
	public byte superlayer;
	
	/** the cluster id */
	public short id;

	/** the number of hits in the cluster */
	public byte size;
	
	/** the status */
	public short status;
		
	/** the average wire number of the cluster */
	public float avgWire;
	
	/** fit chi-squared */
	public float fitChisqProb;

	/** the intercept */
	public float fitInterc;

	/** the error in the intercept */
	public float fitIntercErr;

	/** the slope */
	public float fitSlope;

	/** the error in the slope */
	public float fitSlopeErr;
	
	/** The hit ids (max of 12) */
	public short hitID[];

	
	public DCCluster(byte sector, byte superlayer, short id, byte size, short status, 
			float avgWire, float fitChisqProb,
			float fitInterc, float fitIntercErr, float fitSlope, float fitSlopeErr,
			short... hitID) {
		this.sector = sector;
		this.superlayer = superlayer;
		this.id = id;
		this.size = size;
		this.status = status;
		
		this.avgWire = avgWire;
		this.fitChisqProb = fitChisqProb;
		
		
		this.fitInterc = fitInterc;
		this.fitIntercErr = fitIntercErr;
		this.fitSlope = fitSlope;
		this.fitSlopeErr = fitSlopeErr;
		
		this.hitID = hitID;
	}

}
