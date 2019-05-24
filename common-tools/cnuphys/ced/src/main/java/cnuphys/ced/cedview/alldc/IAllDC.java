package cnuphys.ced.cedview.alldc;

import cnuphys.ced.cedview.CedView;

public interface IAllDC {

	/**
	 * Get the underlying view
	 * 
	 * @return the underlying view
	 */
	public CedView getView();

	/**
	 * Is this the standard alldc view?
	 * 
	 * @return <code>true/code> if this is the standard alldc view.
	 */
	public boolean isStandardAllDCView();
}
