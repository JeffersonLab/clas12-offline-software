package cnuphys.snr.clas12;

public class Clas12NoiseResult {

	/**
	 * noise array, parallel to the dc tdc sector array. Should be thread safe
	 */
	public boolean noise[];

	public Clas12NoiseResult() {

	}

	/** clear all the results */
	public void clear() {
		noise = null;
	}

	/**
	 * Get the number of noise hits
	 * 
	 * @return the number of noise hits
	 */
	public int noiseCount() {
		int count = 0;

		if (noise != null) {
			for (boolean b : noise) {
				if (b)
					count++;
			}
		}

		return count;
	}

}
