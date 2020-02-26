package cnuphys.fastMCed.view;


import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;

import cnuphys.bCNU.component.checkboxarray.CheckBoxArray;
import cnuphys.bCNU.util.Bits;
import cnuphys.bCNU.util.X11Colors;

/**
 * Create the display flags based on bits. This allows for a common appearance
 * across all views
 * 
 * @author DHeddle
 * 
 */

@SuppressWarnings("serial")
public class DisplayArray extends CheckBoxArray implements ItemListener {

	
	// the parent view
	private AView _view;

	/**
	 * Create a display flag array. This constructor produces a two column
	 * array.
	 * 
	 * @param view
	 *            the parent view
	 * @param bits
	 *            controls what flags are added
	 */
	public DisplayArray(AView view, int bits, int nc, int hgap) {
		super(nc, hgap, 0);
		_view = view;


	}

	/**
	 * A button has been clicked
	 * 
	 * @param e
	 *            the causal event
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		// repaint the view
		if (_view != null) {
			_view.getContainer().refresh();
		}
	}


}