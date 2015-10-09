package cnuphys.bCNU.graphics.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import cnuphys.bCNU.log.Log;

/**
 * This is a convenience class extending JToolBar for the case where all
 * JToggleButtons added form a mutually exclusive set. Note you can still add
 * other buttons.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class CommonToolBar extends JToolBar {

	// make all the toggle buttons mutually exclusive
	private ButtonGroup _buttonGroup = new ButtonGroup();

	// the default button
	private JToggleButton _defaultToggleButton;

	// action listener for toggle buttons
	private ActionListener _toggleActionListener;

	/**
	 * Creates a new tool bar; orientation defaults to <code>HORIZONTAL</code>.
	 */
	public CommonToolBar() {
		this("ToolBar", SwingConstants.HORIZONTAL);
	}

	/**
	 * Creates a new tool bar with the specified name. The name is used as the
	 * title of the undocked tool bar. The default orientation is
	 * <code>HORIZONTAL</code>.
	 * 
	 * @param name
	 *            the name of the tool bar
	 */
	public CommonToolBar(String name) {
		this(name, SwingConstants.HORIZONTAL);
	}

	/**
	 * Creates a new tool bar with the specified orientation. The orientation
	 * must be either <code>HORIZONTAL</code> or <code>VERTICAL</code>.
	 * 
	 * @param orientation
	 */
	public CommonToolBar(int orientation) {
		this("ToolBar", orientation);
	}

	/**
	 * Creates a new tool bar with a specified name and orientation. All other
	 * constructors call this constructor.
	 * 
	 * @param name
	 *            the name of the tool bar
	 * @param orientation
	 *            the initial orientation -- it must be either
	 *            <code>HORIZONTAL</code> or <code>VERTICAL</code>
	 */
	public CommonToolBar(String name, int orientation) {
		super(name, orientation);
	}

	/**
	 * Add a toggle button to the toolbar.
	 * 
	 * @param toggleButton
	 *            the button to add.
	 */
	public void add(JToggleButton toggleButton) {
		if (toggleButton != null) {
			add(toggleButton, true);
		}
	}

	/**
	 * The active toggle button has changed
	 */
	protected void activeToggleButtonChanged() {
	}

	/**
	 * Add a toggle button to the toolbar.
	 * 
	 * @param toggleButton
	 *            the button to add.
	 * @param toGroup
	 *            if <code>true</code> and to the primary button group
	 */
	public void add(JToggleButton toggleButton, boolean toGroup) {

		if (toggleButton == null) {
			return;
		}

		super.add(toggleButton);

		if (toGroup) {
			if (_buttonGroup == null) {
				_buttonGroup = new ButtonGroup();
			}

			if (_toggleActionListener == null) {
				_toggleActionListener = new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						activeToggleButtonChanged();
					}

				};
			}
			_buttonGroup.add(toggleButton);
			toggleButton.addActionListener(_toggleActionListener);

		}
	}

	/**
	 * remove a toggle button from the toolbar.
	 * 
	 * @param toggleButton
	 *            the button to remove.
	 */
	public void remove(JToggleButton toggleButton) {
		if (toggleButton != null) {
			super.remove(toggleButton);

			if (_buttonGroup != null) {
				_buttonGroup.remove(toggleButton);
			}
		}
	}

	/**
	 * Get the default toggle button. This will become active if you click an
	 * active toggle button to turn it off.
	 * 
	 * @return the default toggle buton.
	 */
	public JToggleButton getDefaultToggleButton() {
		return _defaultToggleButton;
	}

	/**
	 * Set the default toggle button. This will become active if you click an
	 * active toggle button to turn it off.
	 * 
	 * @param defaultToggleButton
	 *            the default toggle button.
	 */
	public void setDefaultToggleButton(JToggleButton defaultToggleButton) {
		_defaultToggleButton = defaultToggleButton;
	}

	/**
	 * Reset the default toggle button selection
	 */
	public void resetDefaultSelection() {

		if (_defaultToggleButton != null) {
			_defaultToggleButton.doClick();
		}
	}

	/**
	 * Get which tool bar toggle button from the primary button group is active
	 * 
	 * @return the active toolbar toggle button (from the primary button group),
	 *         or null.
	 */
	public JToggleButton getActiveButton() {

		if (_buttonGroup == null) {
			return null;
		}

		try {
			for (Enumeration<AbstractButton> e = _buttonGroup.getElements(); e
					.hasMoreElements();) {
				AbstractButton ab = e.nextElement();
				if (ab.isSelected()) {
					return (JToggleButton) ab;
				}
			}
		} catch (Exception e) {
			Log.getInstance().exception(e);
			e.printStackTrace();
		}

		return null;
	}

}
