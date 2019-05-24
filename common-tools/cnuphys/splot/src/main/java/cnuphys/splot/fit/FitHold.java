package cnuphys.splot.fit;

/**
 * This class is used to place holds on fit parameters
 * 
 * @author heddle
 *
 */
public class FitHold {

	// the index of the fit parameter to be held
	protected int index;

	// the value it is held at
	protected double value;

	/**
	 * Create a hold on a fitting parameter. When the fit is performed, any
	 * parameters that are "held" will be set to a fixed value and no changed in the
	 * optimization
	 * 
	 * @param index the parameter index. All fits have a parameter array (usually
	 *              called <code>a[]</code>. This is an index into that array.
	 * @param value the value that should be held.
	 */
	protected FitHold(int index, double value) {
		this.index = index;
		this.value = value;
	}

}
