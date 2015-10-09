package cnuphys.bCNU.component.led;

import java.util.EventListener;

public interface LedListener extends EventListener {

	/**
	 * An LED has been single clicked.
	 * 
	 * @param led
	 *            the led in question.
	 */
	public void ledClicked(Led led);

	/**
	 * An LED has been double clicked.
	 * 
	 * @param led
	 *            the led in question.
	 */
	public void ledDoubleClicked(Led led);

	/**
	 * An LED has been single clicked with the right button.
	 * 
	 * @param led
	 *            the led in question.
	 */
	public void ledRightClicked(Led led);

}
