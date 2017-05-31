package cnuphys.bCNU.component.led;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import cnuphys.bCNU.dialog.VerticalFlowLayout;

/**
 * This is a panel that holds LEDs. LEDs are are stop-lights of sorts and are
 * used to represent the state of a view or application.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class LedPanel extends JPanel {

	public enum Orientation {
		HORIZONTAL, VERTICAL
	};

	/**
	 * The LEds that have been added
	 */
	private Hashtable<String, Led> leds = new Hashtable<String, Led>(47);

	// number of leds added
	protected int ledCount = 0;

	/**
	 * Create a LED panel.
	 * 
	 * @param gap
	 *            the spacing
	 * @param orientation
	 *            the orientation.
	 */
	public LedPanel(int gap, Orientation orientation) {

		switch (orientation) {
		case HORIZONTAL:
			setLayout(new FlowLayout(FlowLayout.LEFT, gap, 0));
			break;

		case VERTICAL:
			setLayout(new VerticalFlowLayout(true, gap));
			break;
		}
	}

	/**
	 * Create a LED panel using a grid layout
	 * 
	 * @param numRow
	 *            the number of rows
	 * @param numCol
	 *            the number of columns
	 * @param hgap
	 *            the horizontal pixel gap
	 * @param vgap
	 *            the vertical pixel gap
	 */
	public LedPanel(int numRow, int numCol, int hgap, int vgap) {
		setLayout(new GridLayout(numRow, numCol, hgap, vgap));
		setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 0));
	}

	/**
	 * @param state
	 *            the initial state.
	 * @param label
	 *            the initial label.
	 * @param ledListener
	 *            the lister for clicks.
	 * @return the added Led
	 */
	public Led addLed(LedState state, String label, LedListener ledListener) {
		Led led = new Led(state, label);
		led.addLedListener(ledListener);
		add(led);
		leds.put(label, led);
		validate();
		ledCount++;
		return led;
	}

	/**
	 * Get the LED state.
	 * 
	 * @param label
	 *            the unique label for the led.
	 * @return the state of the led.
	 */
	public LedState getLedState(String label) {
		if (leds != null) {
			Led led = leds.get(label);
			if (led != null) {
				return led.getState();
			}
		}
		return null;
	}

	/**
	 * Set the state of the led.
	 * 
	 * @param label
	 *            the unique label for the led.
	 * @param state
	 *            the new state of the led.
	 */
	public void setLedState(String label, LedState state) {
		if (leds != null) {
			Led led = leds.get(label);
			if (led != null) {
				led.setState(state);
				led.repaint();
			}
		}
	}

	// @Override
	// public Dimension getPreferredSize() {
	// return size;
	// }

	/**
	 * Set the state of the led. This allows for only two statesm RED and GREEN
	 * corresponding to on and off.
	 * 
	 * @param label
	 *            the unique label for the led.
	 * @param on
	 *            the new state of the led.
	 */
	public void setLedState(String label, boolean on) {
		setLedState(label, on ? LedState.GREEN : LedState.RED);
	}

	/**
	 * Convenience method to set all Leds to a given state.
	 * 
	 * @param state
	 *            the desired state for all leds.
	 */
	public void setAll(LedState state) {
		if (leds != null) {
			Set<String> keys = leds.keySet();
			if (!(keys.isEmpty())) {
				for (String s : keys) {
					setLedState(s, state);
				}
			}
		}
	}

	/**
	 * Convenience method to see if all the LEDs are GREEN.
	 * 
	 * @return <code>true</code> if all the Leds are green.
	 */
	public boolean areAllGreen() {
		if (leds != null) {
			Set<String> keys = leds.keySet();
			if (!(keys.isEmpty())) {
				for (String s : keys) {
					if (getLedState(s) != LedState.GREEN) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @return the ledCount
	 */
	public int getLedCount() {
		return ledCount;
	}

}
