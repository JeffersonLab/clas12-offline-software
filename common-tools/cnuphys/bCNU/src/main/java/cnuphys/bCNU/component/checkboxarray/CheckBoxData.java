package cnuphys.bCNU.component.checkboxarray;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.Fonts;

public class CheckBoxData {

	// The underlying check box
	private AbstractButton _checkBox;

	/**
	 * Create an object that will possibly then be placed in a
	 * <code>CommonCheckBoxArray</code>.
	 * 
	 * @param label
	 *            the check box label.
	 * @param intialState
	 *            the initial state of the selection.
	 * @param enabled
	 *            whether the check box is selectable.
	 * @param radioStyle
	 *            controls whether it is a radio style button.
	 * @param itemListener
	 *            listens for changes on the button.
	 * @param textColor
	 *            the next color. Will use black if this is null.
	 */
	public CheckBoxData(String label, boolean intialState, boolean enabled,
			boolean radioStyle, ItemListener itemListener, Color textColor) {
		this(label, intialState, enabled, radioStyle, null, itemListener,
				textColor);
	}

	/**
	 * Create an object that will possibly then be placed in a
	 * <code>CheckBoxArray</code>.
	 * 
	 * @param label
	 *            the check box label.
	 * @param initialState
	 *            the initial state of the selection.
	 * @param enabled
	 *            whether the check box is selectable.
	 * @param font
	 *            the font to use.
	 * @param itemListener
	 *            listens for changes on the button.
	 * @param textColor
	 *            the next color. Will use black if this is null.
	 */
	public CheckBoxData(String label, boolean initialState, boolean enabled,
			boolean radioStyle, Font font, ItemListener itemListener,
			Color textColor) {

		if (radioStyle) {
			_checkBox = new JRadioButton(label);
		} else {
			_checkBox = new JCheckBox(label);
		}

		GraphicsUtilities.setSizeSmall(_checkBox);

		// _checkBox.setOpaque(true);

		_checkBox.setFont((font != null) ? font : Fonts.smallFont);

		if (textColor != null) {
			_checkBox.setForeground(textColor);
		}

		_checkBox.setSelected(initialState);
		_checkBox.setEnabled(enabled);

		if (itemListener != null) {
			_checkBox.addItemListener(itemListener);
		}
	}

	/**
	 * Convenience method to get the label.
	 * 
	 * @return the underlying checkbox's label.
	 */
	public String getText() {
		return _checkBox.getText();
	}

	/**
	 * Convenience method to get the selection state.
	 * 
	 * @return the underlying checkbox's selection state.
	 */
	public boolean isSelected() {
		return _checkBox.isSelected();
	}

	/**
	 * Convenience method to set the underlying checkbox's selection state.
	 * 
	 * @param selected
	 *            the value for the state.
	 */
	public void setSelected(boolean selected) {
		_checkBox.setSelected(selected);
	}

	/**
	 * Convenience method to set the underlying checkbox's enabled state.
	 * 
	 * @param enabled
	 *            the value for the state.
	 */
	public void setEnabled(boolean enabled) {
		_checkBox.setEnabled(enabled);
	}

	/**
	 * Gets the underlying check box.
	 * 
	 * @return the underlying checkbox.
	 */
	public AbstractButton getCheckBox() {
		return _checkBox;
	}

}
