package cnuphys.splot.toolbar;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.EventListenerList;

import cnuphys.splot.plot.Environment;

public class CommonToolBar extends JToolBar implements ActionListener, ItemListener {

	// make all the toggle buttons mutually exclusive
	private ButtonGroup _buttonGroup = new ButtonGroup();

	// the default button
	private ToolBarToggleButton _pointerButton;

	// box zoom
	private ToolBarToggleButton _boxZoomButton;

	// recenter button
	private ToolBarToggleButton _recenterButton;

	// List of toolBar listeners
	private EventListenerList _listenerList;

	// command strings
	public static final String ZOOMIN = "ZOOM IN";
	public static final String ZOOMOUT = "ZOOM OUT";
	public static final String POINTER = "POINTER";
	public static final String PRINT = "PRINT";
	public static final String BOXZOOM = "BOXZOOM";
	public static final String CENTER = "CENTER";
	public static final String WORLD = "WORLD";
	public static final String PNG = "PNG";

	/**
	 * Creates a new tool bar with a specified name and orientation. All other
	 * constructors call this constructor.
	 * 
	 * @param orientation the initial orientation -- it must be either
	 *                    <code>HORIZONTAL</code> or <code>VERTICAL</code>
	 */
	public CommonToolBar(int orientation) {
		super("toolBar", orientation);
		setFloatable(false);

		Environment.getInstance().commonize(this, null);
		setBorder(BorderFactory.createEtchedBorder());

		_pointerButton = new ToolBarToggleButton("images/pointer.gif", "Make selections", POINTER, 3, 1,
				"images/pointercursor.gif");
		_pointerButton.setSelected(true);
		add(_pointerButton);

		_boxZoomButton = new ToolBarToggleButton("images/box_zoom.gif", "Zoom to area", BOXZOOM, 3, 1,
				"images/box_zoomcursor.gif");
		add(_boxZoomButton);

		_recenterButton = new ToolBarToggleButton("images/center.gif", "Recenter the plot", CENTER, -1, -1,
				"images/centercursor.gif");

		add(_recenterButton);

		addHGap(8);
		add(new ToolBarButton("images/zoom_in.gif", "Zoom in", ZOOMIN));
		add(new ToolBarButton("images/zoom_out.gif", "Zoom out", ZOOMOUT));
		add(new ToolBarButton("images/world.gif", "Include all data", WORLD));
		add(new ToolBarButton("images/printer.gif", "Print the plot", PRINT));
		add(new ToolBarButton("images/camera.gif", "Save as PNG", PNG));
	}

	/**
	 * Set which toggle buttonis selected
	 */
	public void setSelectedToggle(String s) {
		if (s == null) {
			_pointerButton.setSelected(true);
		}
		else if (s.equals(BOXZOOM)) {
			_boxZoomButton.setSelected(true);
		}
		else if (s.equals(CENTER)) {
			_recenterButton.setSelected(true);
		}
		else {
			_pointerButton.setSelected(true);
		}
	}

	/**
	 * Get the primary button group so that we can add other buttons to the group
	 * 
	 * @return the primary button group so that we can add other buttons to the
	 *         group
	 */
	public ButtonGroup getMainButtonGroup() {
		return _buttonGroup;
	}

	// add a gap between buttons
	private void addHGap(int gap) {
		add(Box.createHorizontalStrut(gap));
	}

	/**
	 * Add a regular button to the toolbar
	 * 
	 * @param button the ToolBarButton to add.
	 */
	public void add(ToolBarButton button) {
		super.add(button);
		button.addActionListener(this);
	}

	/**
	 * Add a toggle button to the toolbar.
	 * 
	 * @param toggleButton the button to add.
	 */
	public void add(JToggleButton toggleButton) {
		add(toggleButton, true);
	}

	/**
	 * Add a toggle button to the toolbar.
	 * 
	 * @param toggleButton the button to add.
	 * @param toGroup      if <code>true</code> and to the primary button group
	 */
	public void add(JToggleButton toggleButton, boolean toGroup) {
		super.add(toggleButton);

		if (toGroup) {
			if (_buttonGroup == null) {
				_buttonGroup = new ButtonGroup();
			}
			_buttonGroup.add(toggleButton);
		}
		toggleButton.addItemListener(this);
	}

	/**
	 * remove a toggle button from the toolbar.
	 * 
	 * @param toggleButton the button to remove.
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
	 * Get the default toggle button. This will become active if you click an active
	 * toggle button to turn it off.
	 * 
	 * @return the default toggle buton.
	 */
	public JToggleButton getDefaultToggleButton() {
		return _pointerButton;
	}

	/**
	 * Set the default toggle button. This will become active if you click an active
	 * toggle button to turn it off.
	 * 
	 * @param defaultToggleButton the default toggle button.
	 */
	public void setDefaultToggleButton(ToolBarToggleButton defaultToggleButton) {
		_pointerButton = defaultToggleButton;
	}

	/**
	 * Reset the default toggle button selection
	 */
	public void resetDefaultSelection() {

		if (_pointerButton != null) {
			_pointerButton.doClick();
		}
	}

	/**
	 * Get the command of the active toggle button
	 * 
	 * @return the command of the active toggle button
	 */
	public String getActiveCommand() {
		ToolBarToggleButton tbtb = getActiveButton();
		return (tbtb != null) ? tbtb.getActionCommand() : "??";
	}

	/**
	 * Get which tool bar toggle button from the primary button group is active
	 * 
	 * @return the active toolbar toggle button (from the primary button group), or
	 *         null.
	 */
	public ToolBarToggleButton getActiveButton() {

		if (_buttonGroup == null) {
			return null;
		}

		try {
			for (Enumeration<AbstractButton> e = _buttonGroup.getElements(); e.hasMoreElements();) {
				AbstractButton ab = e.nextElement();
				if (ab.isSelected()) {
					return (ToolBarToggleButton) ab;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Check whether the default button is active
	 * 
	 * @return <code>true</code> if the default button is active.
	 */
	public boolean isDefaultActivated() {
		ToolBarToggleButton tbtb = getActiveButton();
		return ((tbtb != null) && (tbtb == _pointerButton));
	}

	/**
	 * Check whether a given button is active
	 * 
	 * @param tbtb the button to test
	 * @return <code>true</code> if the default button is active.
	 */
	public boolean isButtonActivated(AbstractButton tbtb) {
		return ((tbtb != null) && (tbtb == getActiveButton()));
	}

	// notify listeners of a change in the magnetic field
	private void notifyListeners(AbstractButton button) {

		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// This weird loop is the bullet proof way of notifying all listeners.
		// for (int i = listeners.length - 2; i >= 0; i -= 2) {
		// order is flipped so it goes in order as added
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == IToolBarListener.class) {
				IToolBarListener listener = (IToolBarListener) listeners[i + 1];
				if (button instanceof ToolBarButton) {
					listener.buttonPressed(this, (ToolBarButton) button);
				}
				if (button instanceof ToolBarToggleButton) {
					listener.toggleButtonActivated(this, (ToolBarToggleButton) button);
				}
			}

		}
	}

	/**
	 * Add a toolBar listener
	 * 
	 * @param ToolBarListener the listener to add
	 */
	public void addToolBarListener(IToolBarListener listener) {

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		// avoid adding duplicates
		_listenerList.remove(IToolBarListener.class, listener);
		_listenerList.add(IToolBarListener.class, listener);
	}

	/**
	 * Remove a ToolBarListener.
	 * 
	 * @param ToolBarListener the ToolBarListener to remove.
	 */

	public void removeToolBarListener(IToolBarListener listener) {

		if ((listener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(IToolBarListener.class, listener);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source instanceof ToolBarButton) {
			notifyListeners((ToolBarButton) source);
		}

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (source instanceof ToolBarToggleButton) {
				notifyListeners((ToolBarToggleButton) source);
			}
		}
	}

	/**
	 * Find a button from the action command
	 * 
	 * @param actionCommand the action command to match
	 * @return the button or <code>null</code> if not found
	 */
	public AbstractButton getButton(String actionCommand) {
		if (actionCommand == null) {
			return null;
		}
		Component array[] = getComponents();

		for (Component c : array) {
			if (c instanceof AbstractButton) {
				AbstractButton b = (AbstractButton) c;
				if (actionCommand.equalsIgnoreCase(b.getActionCommand())) {
					return b;
				}
			}
		}

		return null;
	}

	/**
	 * Set a button enabled by the action command
	 * 
	 * @param actionCommand the action command
	 * @param enabled       the flag
	 */
	public void setButtonEnabled(String actionCommand, boolean enabled) {
		AbstractButton b = getButton(actionCommand);
		if (b != null) {
			b.setEnabled(enabled);
		}
	}

}
