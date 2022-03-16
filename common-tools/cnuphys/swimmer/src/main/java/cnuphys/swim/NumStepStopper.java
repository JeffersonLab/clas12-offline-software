package cnuphys.swim;

import cnuphys.rk4.IStopper;

public class NumStepStopper implements IStopper {
	
	private int _maxStep;
	private int count;
	
	protected double _finalPathLength = Double.NaN;

	
	public NumStepStopper(int numStep) {
		_maxStep = numStep;
	}

	@Override
	public boolean stopIntegration(double t, double[] y) {
		count++;
		return count >= _maxStep;
	}

	@Override
	public double getFinalT() {
		return _finalPathLength;
	}

	@Override
	public void setFinalT(double finalPathLength) {
		_finalPathLength = finalPathLength;
	}

}
