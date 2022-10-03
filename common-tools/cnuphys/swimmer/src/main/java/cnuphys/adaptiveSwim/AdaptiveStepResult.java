package cnuphys.adaptiveSwim;

public class AdaptiveStepResult {

	//new stepsize
	private double _hNew;

	//new value of independent variable,
	//might be pathlength or z
	private double _sNew;

	/**
	 * Get the new stepsize
	 * @return the new stepsize
	 */
	public double getHNew() {
		return _hNew;
	}

	/**
	 * Set the new stepsize
	 * @param hNew the new stepsize
	 */
	public void setHNew(double hNew) {
		_hNew = hNew;
	}

	/**
	 * Get the new pathlength
	 * @return the new pathlength
	 */
	public double getSNew() {
		return _sNew;
	}

	/**
	 * et the new pathlength
	 * @param sNew the new pathlength
	 */
	public void setSNew(double sNew) {
		_sNew = sNew;
	}


}
