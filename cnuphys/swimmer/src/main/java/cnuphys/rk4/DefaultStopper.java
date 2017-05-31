package cnuphys.rk4;

/**
 * Default stopper in fact never stops!
 * 
 * @author heddle
 *
 */

public class DefaultStopper implements IStopper {

	// final value of the independent variable
	private double _tf;

	@Override
	public boolean stopIntegration(double t, double[] y) {
		return false;
	}

	@Override
	public double getFinalT() {
		return _tf;
	}

	@Override
	public void setFinalT(double finalT) {
		_tf = finalT;
	}

}
