package cnuphys.fastMCed.snr;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.Fonts;

/**
 * A set of static methods to help ensure a uniform loon and feel
 * 
 * @author heddle
 * 
 */
public class ComponentSupport {

	/**
	 * Create a simple label
	 * 
	 * @param text
	 *            the text of the label
	 * @return the label
	 */
	public static JLabel makeLabel(String text) {
		JLabel jlabel = new JLabel(text);
		jlabel.setFont(Fonts.smallFont);
		return jlabel;
	}

	/**
	 * Create a button
	 * 
	 * @param label
	 *            the label on the button
	 * @param enabled
	 *            the selectability of the button
	 * @param al
	 *            an optional action listener for the button
	 * @return the button
	 */
	public static JButton makeButton(String label, boolean enabled,
			ActionListener al) {
		JButton button = new JButton(label);
		button.setEnabled(false);

		if (al != null) {
			button.addActionListener(al);
		}
		GraphicsUtilities.setTexturedButton(button);
		button.setFont(Fonts.smallFont);
		return button;
	}

	/**
	 * Create a radio button
	 * 
	 * @param label
	 *            the label on the button
	 * @param bg
	 *            the button group that will group this radio button
	 * @param enabled
	 *            the selectability of the button
	 * @param selected
	 *            the initial selection state
	 * @param al
	 *            an optional action listener for the radio button
	 * @return the button
	 */
	public static JRadioButton makeRadioButton(String label, ButtonGroup bg,
			boolean enabled, boolean selected, ActionListener al) {
		JRadioButton rb = new JRadioButton(label, selected);
		rb.setEnabled(enabled);
		GraphicsUtilities.setSizeSmall(rb);
		rb.setFont(Fonts.smallFont);
		if (al != null) {
			rb.addActionListener(al);
		}
		bg.add(rb);
		return rb;
	}

	/**
	 * Create a check box
	 * 
	 * @param label
	 *            the label on the check box
	 * @param enabled
	 *            the selectability of the check box
	 * @param selected
	 *            the initial selection state
	 * @param il
	 *            an optional iutem listener for the button
	 * @return the check box
	 */
	public static JCheckBox makeCheckBox(String label, boolean enabled,
			boolean selected, ItemListener il) {
		JCheckBox jcb = new JCheckBox(label, selected);
		jcb.setEnabled(enabled);
		jcb.setFont(Fonts.smallFont);
		GraphicsUtilities.setSizeSmall(jcb);

		if (il != null) {
			jcb.addItemListener(il);
		}

		return jcb;
	}

}