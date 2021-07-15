package cnuphys.adaptiveSwim;

public class AdaptiveStepResult {
		
	//new stepsize
	private double _hNew;
	
	//new value of independent variable
	private double _sNew;

	public double getHNew() {
		return _hNew;
	}

	public void setHNew(double hNew) {
		_hNew = hNew;
	}

	public double getSNew() {
		return _sNew;
	}

	public void setSNew(double sNew) {
		_sNew = sNew;
	}
	

}
