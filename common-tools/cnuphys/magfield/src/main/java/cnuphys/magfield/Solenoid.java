package cnuphys.magfield;

import java.io.PrintStream;

public abstract class Solenoid extends MagneticField {

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void printConfiguration(PrintStream ps) {
	}

	@Override
	public boolean isActive() {
		return false;
	}
	
	/**
	 * Checks whether the field has been set to always return zero.
	 * 
	 * @return <code>true</code> if the field is set to return zero.
	 */
	@Override
	public final boolean isZeroField() {
		if (isActive()) {
			return super.isZeroField();
		} else {
			return true;
		}
	}


}
