package cnuphys.ced.fastmc;

public class NoiseData {

	/** Number of hits */
	public int count;
	
	/** 1-based sector array */
	public byte sector[];

	/** 1-based superlayer array */
	public byte superlayer[];

	/** 1-based layer array */
	public byte layer[];

	/** 1-based wire array */
	public short wire[];

	
	public NoiseData() {
		
	}
}
