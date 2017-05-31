package cnuphys.bCNU.component.checkboxarray;

import java.awt.Color;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.log.Log;

/**
 * Used to create a panel of options.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class CheckBoxArray extends JPanel {

	/**
	 * Used to add buttons optionally to a button group to make mutually
	 * exclusive groups.
	 */
	protected Hashtable<String, ButtonGroup> buttonGroups;

	/**
	 * A map of the buttons that are added.
	 */
	protected Hashtable<String, AbstractButton> buttons = new Hashtable<String, AbstractButton>(
			59);

	// keep track of what column
	private int _nextIndex = 0;

	// a panel for each column
	private JPanel _subPanels[];

	/**
	 * Create a check box array.
	 * 
	 * @param numColumns
	 *            the number of columns.
	 * @param hgap
	 *            the horizontal gap.
	 * @param vgap
	 *            the vertical gap.
	 */
	public CheckBoxArray(int numColumns, int hgap, int vgap) {
		Box box = Box.createHorizontalBox();
		box.setOpaque(true);

		_subPanels = new JPanel[numColumns];
		for (int i = 0; i < numColumns; i++) {
			_subPanels[i] = new JPanel();

			_subPanels[i].setLayout(new VerticalFlowLayout(true, vgap));
			box.add(_subPanels[i]);
			if (i < (numColumns - 1)) {
				box.add(Box.createHorizontalStrut(hgap));
			}
		}

		add(box);
	}

	/**
	 * Creates a CheckBoxArray of checkboxes (no radio buttons). All the buttons
	 * are enabled and unselected.
	 * 
	 * @param numColumns
	 *            the number of columns.
	 * @param hgap
	 *            the horizontal gap.
	 * @param vgap
	 *            the vertical gap.
	 * @param labels
	 *            variable length list of labels
	 */
	public CheckBoxArray(int numColumns, int hgap, int vgap, String... labels) {
		this(numColumns, hgap, vgap);

		for (String s : labels) {
			add(s, false, true, null, Color.black);
		}
	}

	/**
	 * Add a check box that is not part of a button group.
	 * 
	 * @param label
	 *            the checkbox label.
	 * @param initialState
	 *            the initial state of the selection.
	 * @param enabled
	 *            whether it is selectable or not.
	 * @param itemListener
	 *            listens for changes on the button.
	 * @param textColor
	 *            the next color. Will use black if this is null.
	 * @return the CheckBoxData object.
	 */
	public CheckBoxData add(String label, boolean initialState,
			boolean enabled, ItemListener itemListener, Color textColor) {
		return add(label, initialState, enabled, null, itemListener, textColor);
	}

	/**
	 * Add a check box that is not part of a button group. This has a
	 * convenience piece that checks that a mask bit is set.
	 * 
	 * @param label
	 *            the checkbox label.
	 * @param initialState
	 *            the initial state of the selection.
	 * @param enabled
	 *            whether it is selectable or not.
	 * @param bits
	 *            a bitwise control
	 * @param mask
	 *            the mask to check.
	 * @param itemListener
	 *            listens for changes on the button.
	 * @param textColor
	 *            the next color. Will use black if this is null.
	 * @return the CheckBoxData object.
	 */
	public CheckBoxData add(String label, boolean initialState,
			boolean enabled, long bits, long mask, ItemListener itemListener,
			Color textColor) {
		if ((bits & mask) == mask) {
			return add(label, initialState, enabled, null, itemListener,
					textColor);
		}
		return null;
	}

	/**
	 * Add a check box that might be part of a button group.
	 * 
	 * @param label
	 *            the checkbox label.
	 * @param initialState
	 *            the initial state of the selection.
	 * @param enabled
	 *            whether it is sectable or not.
	 * @param buttonGroupName
	 *            if not <code>null</code>, placed in a mutually exclusive
	 *            button group of that name.
	 * @param itemListener
	 *            listens for changes on the button.
	 * @param textColor
	 *            the next color. Will use black if this is null.
	 * @return the CheckBoxData.
	 */
	public CheckBoxData add(String label, boolean initialState,
			boolean enabled, String buttonGroupName, ItemListener itemListener,
			Color textColor) {
		CheckBoxData ccd = new CheckBoxData(label, initialState, enabled,
				buttonGroupName != null, itemListener, textColor);

		buttons.put(label, ccd.getCheckBox());

		_subPanels[_nextIndex].add(ccd.getCheckBox());
		_nextIndex = ((_nextIndex + 1) % _subPanels.length);

		// get a button group from the hash (or create)
		if (buttonGroupName != null) {
			ButtonGroup bg = getOrCreate(buttonGroupName);
			bg.add(ccd.getCheckBox());
			if (initialState) {
				setSelected(bg, ccd.getCheckBox());
			}
		}
		return ccd;
	}

	/**
	 * Get which button is active on a given button group
	 * 
	 * @param buttonGroupName
	 *            the name of the buttonGroup;
	 * @return the active toolbar toggle button, or null.
	 */

	public AbstractButton getActiveButton(String buttonGroupName) {

		if ((buttonGroupName == null) || (buttonGroups == null)) {
			nullWarning(buttonGroupName);
			return null;
		}

		ButtonGroup buttonGroup = buttonGroups.get(buttonGroupName);
		if (buttonGroup == null) {
			nullWarning(buttonGroupName);
			return null;
		}

		try {
			for (Enumeration<AbstractButton> e = buttonGroup.getElements(); e
					.hasMoreElements();) {
				AbstractButton ab = e.nextElement();
				if (ab.isSelected()) {
					return ab;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		nullWarning(buttonGroupName);
		return null;
	}

	/**
	 * Set a member of a button group to be selected.
	 * 
	 * @param buttonGroup
	 *            the button group.
	 * @param abstractButton
	 *            the button to set selected.
	 */
	private void setSelected(ButtonGroup buttonGroup,
			AbstractButton abstractButton) {
		try {
			for (Enumeration<AbstractButton> e = buttonGroup.getElements(); e
					.hasMoreElements();) {
				AbstractButton ab = e.nextElement();
				ab.setSelected(ab == abstractButton);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get a button group, creating if necessary.
	 * 
	 * @param buttonGroupName
	 *            the name of the button group.
	 * @return the button group with the given name.
	 */
	private ButtonGroup getOrCreate(String buttonGroupName) {
		if (buttonGroups == null) {
			buttonGroups = new Hashtable<String, ButtonGroup>(47);
		}
		ButtonGroup bg = buttonGroups.get(buttonGroupName);

		if (bg == null) {
			bg = new ButtonGroup();
			buttonGroups.put(buttonGroupName, bg);
		}
		return bg;
	}

	/**
	 * Set a button's (checkbox) selected state based on its label.
	 * 
	 * @param label
	 *            the button's label
	 * @param selected
	 *            the button's selected state
	 */
	public void setSelected(String label, boolean selected) {
		AbstractButton ab = buttons.get(label);
		if (ab == null) {
			nullWarning(label);
			return;
		}
		ab.setSelected(selected);
	}

	/**
	 * See if a button (checkbox) is selected based on its label.
	 * 
	 * @param label
	 *            the button's label
	 * @return <code>true</code> if the button is selected.
	 */
	public boolean isSelected(String label) {
		AbstractButton ab = buttons.get(label);
		if (ab == null) {
			nullWarning(label);
			return false;
		}
		return ab.isSelected();
	}

	/**
	 * See if a button (checkbox) is enabled based on its label.
	 * 
	 * @param label
	 *            the button's label
	 * @return <code>true</code> if the button is enabled.
	 */
	public boolean isEnabled(String label) {
		AbstractButton ab = buttons.get(label);
		if (ab == null) {
			nullWarning(label);
			return false;
		}
		return ab.isSelected();
	}

	/**
	 * Set a button's enabled stated based on its label.
	 * 
	 * @param label
	 *            the button's label
	 * @param enabled
	 *            the enabled flag
	 */
	public void setEnabled(String label, boolean enabled) {
		AbstractButton ab = buttons.get(label);
		if (ab == null) {
			nullWarning(label);
		} else {
			ab.setEnabled(enabled);
		}
	}

	/**
	 * Get the corresponding abstract button
	 * 
	 * @param label
	 *            the label to match
	 * @return the correseponding button
	 */
	public AbstractButton getButton(String label) {
		return buttons.get(label);
	}

	/**
	 * Issue a warning that we tried to acess a non-existent button based on the
	 * provided label.
	 * 
	 * @param label
	 *            the label in question.
	 */
	private void nullWarning(String label) {
		Log.getInstance().warning(
				"Tried to access non-existent button or group  on CheckBoxArray with label: "
						+ label);
	}

}
