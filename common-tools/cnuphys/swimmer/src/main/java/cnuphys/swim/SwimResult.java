package cnuphys.swim;

public class SwimResult {
	
	//the final state vector
	private double[] _uf;
	
	//the number of integration steps
	private int _nStep;
	
	//the final path length
	private double _finalS;
	
	/**
	 * Create a container for the swim results
	 * @param dim the dimension of the system (probably 6)
	 */
	public SwimResult(int dim) {
		_uf = new double[dim];
	}
	
	public double[] getUf() {
		return _uf;
	}


	public void setUf(double[] uf) {
		_uf = uf;
	}


	public int getNStep() {
		return _nStep;
	}


	public void setNStep(int nStep) {
		_nStep = nStep;
	}


	public double getFinalS() {
		return _finalS;
	}


	public void setFinalS(double finalS) {
		_finalS = finalS;
	}


}
