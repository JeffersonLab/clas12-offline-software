package cnuphys.bCNU.component.rangeslider;

import java.util.EventListener;

public interface IRangeSliderListener extends EventListener {

	/**
	 * The range has changed.
	 * 
	 * @param slider
	 *            the RangeSlider being updated.
	 */
	public void rangeSliderChanged(RangeSlider slider);
}
