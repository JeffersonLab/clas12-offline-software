package cnuphys.ced.component;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;

import cnuphys.bCNU.component.checkboxarray.CheckBoxArray;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Bits;
import cnuphys.ced.cedview.CedView;

/**
 * Create the display flags based on bits. This allows for a common appearance
 * across all views.
 * 
 * @author DHeddle
 * 
 */

@SuppressWarnings("serial")
public class MagFieldDisplayArray extends CheckBoxArray implements ItemListener {

	/**
	 * /** Flag for displaying no magnetic field information
	 */
	public static final int NOMAGDISPLAY = 0;

	/** Flag for displaying the magnitude of the magnetic field */
	public static final int BMAGDISPLAY = 1;

	/** Flag for displaying the x component of the magnetic field */
	public static final int BXDISPLAY = 2;

	/** Flag for displaying the y component of the magnetic field */
	public static final int BYDISPLAY = 3;

	/** Flag for displaying the z component of the magnetic field */
	public static final int BZDISPLAY = 4;

	/** Flag for displaying the perp component of the magnetic field */
	public static final int BPERPDISPLAY = 5;

	/** Flag for displaying the perp component of the magnetic field */
	public static final int BGRADDISPLAY = 6;
	// current option
	private int _magFieldDisplayOption = NOMAGDISPLAY;

	/**
	 * Magnetic field button group
	 */
	public static final String MAGFIELD_BUTTONGROUP = "MagneticField";

	/**
	 * Label and access to the No Mag field display radio button
	 */
	public static final String NOFIELD_LABEL = "No B";

	/**
	 * Label and access to the Bmag checkbox
	 */
	public static final String FIELD_LABEL = "<html>B<SUB>mag</SUB>";

	/**
	 * Label and access to the Bx checkbox
	 */
	public static final String BX_LABEL = "<html>B<SUB>x</SUB>";

	/**
	 * Label and access to the By checkbox
	 */
	public static final String BY_LABEL = "<html>B<SUB>y</SUB>";

	/**
	 * Label and access to the Bz checkbox
	 */
	public static final String BZ_LABEL = "<html>B<SUB>z</SUB>";

	/**
	 * Label and access to the Bz checkbox
	 */
	public static final String BPERP_LABEL = "<html>B&perp;";

	/** string for grad magnitude */
	public static final String BGRAD_LABEL = "<html>|&nabla;B|";

	// controls whether any mag field is displayed
	private AbstractButton _noMagButton;

	// controls whether total mag field is displayed
	private AbstractButton _showBmagButton;

	// controls whether x component of the mag field is displayed
	private AbstractButton _showBxButton;

	// controls whether y component of the mag field is displayed
	private AbstractButton _showByButton;

	// controls whether z component of the mag field is displayed
	private AbstractButton _showBzButton;

	// controls whether perpendicular component of the mag field is displayed
	private AbstractButton _showBperpButton;

	// controls whether z component of the mag field is displayed
	private AbstractButton _showBgradButton;

	// the parent view
	private CedView _view;

	/**
	 * Create a display flag array. This constructor produces a two column
	 * array.
	 * 
	 * @param view
	 *            the parent view
	 * @param bits
	 *            controls what flags are added
	 */
	public MagFieldDisplayArray(CedView view, int bits) {
		super(4, 15, 0);
		_view = view;

		// display magnetic field?
		if (Bits.checkBit(bits, DisplayBits.MAGFIELD)) {
			_noMagButton = add(NOFIELD_LABEL, _magFieldDisplayOption == NOMAGDISPLAY, true, MAGFIELD_BUTTONGROUP, this,
					Color.black).getCheckBox();

			_showBmagButton = add(FIELD_LABEL, _magFieldDisplayOption == BMAGDISPLAY, true, MAGFIELD_BUTTONGROUP, this,
					Color.black).getCheckBox();

			_showBxButton = add(BX_LABEL, _magFieldDisplayOption == BXDISPLAY, true, MAGFIELD_BUTTONGROUP, this,
					Color.black).getCheckBox();

			_showByButton = add(BY_LABEL, _magFieldDisplayOption == BYDISPLAY, true, MAGFIELD_BUTTONGROUP, this,
					Color.black).getCheckBox();

			_showBzButton = add(BZ_LABEL, _magFieldDisplayOption == BZDISPLAY, true, MAGFIELD_BUTTONGROUP, this,
					Color.black).getCheckBox();

			_showBperpButton = add(BPERP_LABEL, _magFieldDisplayOption == BPERPDISPLAY, true, MAGFIELD_BUTTONGROUP,
					this, Color.black).getCheckBox();

			_showBgradButton = add(BGRAD_LABEL, _magFieldDisplayOption == BPERPDISPLAY, true, MAGFIELD_BUTTONGROUP,
					this, Color.black).getCheckBox();

		}

		setBorder(new CommonBorder("Field Display Options"));
	}

	/**
	 * A button has been clicked
	 * 
	 * @param e
	 *            the causal event
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		AbstractButton button = (AbstractButton) e.getSource();
		if (button == _noMagButton) {
			_view.getMagneticFieldLayer().setVisible(!button.isSelected());
			if (button.isSelected()) {
				_magFieldDisplayOption = NOMAGDISPLAY;
			}
		}
		else if (button == _showBmagButton) {
			_view.getMagneticFieldLayer().setVisible(button.isSelected());
			if (button.isSelected()) {
				_magFieldDisplayOption = BMAGDISPLAY;
			}
		}
		else if (button == _showBxButton) {
			_view.getMagneticFieldLayer().setVisible(button.isSelected());
			if (button.isSelected()) {
				_magFieldDisplayOption = BXDISPLAY;
			}
		}
		else if (button == _showByButton) {
			_view.getMagneticFieldLayer().setVisible(button.isSelected());
			if (button.isSelected()) {
				_magFieldDisplayOption = BYDISPLAY;
			}
		}
		else if (button == _showBzButton) {
			_view.getMagneticFieldLayer().setVisible(button.isSelected());
			if (button.isSelected()) {
				_magFieldDisplayOption = BZDISPLAY;
			}
		}
		else if (button == _showBperpButton) {
			_view.getMagneticFieldLayer().setVisible(button.isSelected());
			if (button.isSelected()) {
				_magFieldDisplayOption = BPERPDISPLAY;
			}
		}
		else if (button == _showBgradButton) {
			_view.getMagneticFieldLayer().setVisible(button.isSelected());
			if (button.isSelected()) {
				_magFieldDisplayOption = BGRADDISPLAY;
			}
		}
		// repaint the container
		if (_view != null) {
			_view.getContainer().refresh();
		}
	}

	/**
	 * Returns the value that specifies what information about the field is to
	 * be displayed.
	 * 
	 * @return value that specifies what information about the field is to be
	 *         displayed.
	 */
	public int getMagFieldDisplayOption() {
		return _magFieldDisplayOption;
	}

}