package cnuphys.fastMCed.eventgen.sweep;

public class Odometer {
	
	//limits
	private int _numX;
	private int _numY;
	private int _numZ;
	private int _numP;
	private int _numTheta;
	private int _numPhi;
	
	private long totalCount;
	private long index;
	
	
	public int xStep;
	public int yStep;
	public int zStep;
	public int pStep;
	public int thetaStep;
	public int phiStep;
	
	public Odometer(int numX, int numY, int numZ, int numP, int numTheta, int numPhi) {
		_numX = numX;
		_numY = numY;
		_numZ = numZ;
		_numP = numP;
		_numTheta = numTheta;
		_numPhi = numPhi;
		totalCount = numX*numY*numZ*numP*numTheta*numPhi;
		System.err.println("Odomoter total count = " + totalCount);
		index = 0;
	}
	
	public boolean rolledOver() {
		return index >= totalCount;
	}
	
	/**
	 * Increment the odomemter
	 */
	public void increment() {
		phiStep++;
		if (phiStep == _numPhi) {
			phiStep = 0;
			thetaStep++;
			if (thetaStep == _numTheta) {
				thetaStep = 0;
				pStep++;
				if (pStep == _numP) {
					pStep = 0;
					zStep++;
					if (zStep == _numZ) {
						zStep = 0;
						yStep++;
						if (yStep == _numY) {
							yStep = 0;
							xStep++;
							if (xStep == _numX) {
								xStep = 0;
							}
						}
					}
				}
			}
		}
		index++;
	} //increment

}
