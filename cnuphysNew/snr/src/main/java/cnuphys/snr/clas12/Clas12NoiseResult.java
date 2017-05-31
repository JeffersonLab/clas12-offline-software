package cnuphys.snr.clas12;

public class Clas12NoiseResult {

	/**
	 * noise array, parallel to the dc tdc sector array. Should be thread
	 * safe
	 */
	public boolean noise[];

	public Clas12NoiseResult() {

	}

	/** clear all the results */
	public void clear() {
		noise = null;
	}

}
