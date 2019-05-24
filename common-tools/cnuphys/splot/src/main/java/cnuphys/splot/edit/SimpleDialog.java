package cnuphys.splot.edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * A simple dialog template. The dialog is given a Border layout, and component
 * creators for the different directions are called. They should be overridden
 * to provide actual content.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class SimpleDialog extends JDialog implements ActionListener {

	// the components of the dialog
	protected Component northComponent;
	protected Component southComponent;
	protected Component eastComponent;
	protected Component westComponent;
	protected Component centerComponent;

	// the labels for the closeout buttons
	protected String _closeout[];

	// the reason the dialog closed.
	protected String reason;

	// convenient access to south button panel (if exists)
	protected JPanel buttonPanel;

	protected Object _userObject;

	/**
	 * Create a SimpleDialog
	 * 
	 * @param title    the title of the dialog
	 * @param modal    if <code>true</code> the dialog is modal
	 * @param closeout a set of closeout labels
	 */
	public SimpleDialog(String title, Object userObject, boolean modal, String... closeout) {
		super();
		setTitle(title);
		setModal(modal);
		_closeout = closeout;
		_userObject = userObject;

		// close is like a close
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				setVisible(false);
			}
		};
		addWindowListener(wa);

		setLayout(new BorderLayout(4, 4));

		// can do preparation--for example a component might be
		// added on "createCenterComponent" but a reference needed
		// in "addNorthComponent"
		prepare();

		// create the components. The create methods, with the exception
		// of south, return null by default
		northComponent = createNorthComponent();
		southComponent = createSouthComponent();
		eastComponent = createEastComponent();
		westComponent = createWestComponent();
		centerComponent = createCenterComponent();

		conditionalAdd(northComponent, BorderLayout.NORTH);
		conditionalAdd(southComponent, BorderLayout.SOUTH);
		conditionalAdd(eastComponent, BorderLayout.EAST);
		conditionalAdd(westComponent, BorderLayout.WEST);
		conditionalAdd(centerComponent, BorderLayout.CENTER);

		// add menus
		addMenus();

		sizeDialog();
		DialogUtilities.centerDialog(this);
	}

	/**
	 * Add menus to the dialog. The default implementation does nothing.
	 */
	protected void addMenus() {
	}

	/**
	 * Check the enabled state of all the buttons. Default implementation does
	 * nothing.
	 */
	protected void checkButtons() {

	}

	/**
	 * This can be overridden to provide the size of the dialog. The default
	 * implementation is to call pack.
	 */
	protected void sizeDialog() {
		pack();
	}

	/**
	 * Add a component if the given borderlayout direction as long as it is not
	 * <code>null</code>.
	 * 
	 * @param component the component to add (oftent a panel with other components)
	 * @param direction the BorderLayout direction.
	 */
	private void conditionalAdd(Component component, String direction) {
		if (component != null) {
			add(component, direction);
		}
	}

	/**
	 * Override to create the component that goes in the north.
	 * 
	 * @return the component that is placed in the north
	 */
	protected Component createNorthComponent() {
		return null;
	}

	/**
	 * Override to create the component that goes in the south.
	 * 
	 * @return the component that is placed in the south. The default implementation
	 *         creates a row of closeout buttons.
	 */
	protected Component createSouthComponent() {
		if (_closeout != null) {
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
			buttonPanel.add(Box.createHorizontalGlue());

			int lenm1 = _closeout.length - 1;
			for (int index = 0; index <= lenm1; index++) {
				JButton button = new JButton(_closeout[index]);
				button.addActionListener(this);
				buttonPanel.add(button);
				if (index != lenm1) {
					buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
				}
			}
			return buttonPanel;
		}
		return null;
	}

	/**
	 * Get the first button on this dialog with the given label. This will search
	 * all buttons, not just the closout buttons, so it is safe only if no more than
	 * one button has the given label.
	 * 
	 * @param label the label to search for.
	 * @return the first button among its components that has the given label, or
	 *         <code>null</code>.
	 */
	public AbstractButton getButton(String label) {
		return getButton(this, label);
	}

	// search all children
	private AbstractButton getButton(Container container, String label) {

		if (label == null) {
			return null;
		}

		Component components[] = container.getComponents();

		for (Component c : components) {
			if (c instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) c;
				String blabel = button.getText();
				if (label.equals(blabel)) {
					return button;
				}
			}
			else if (c instanceof Container) {
				AbstractButton button = getButton((Container) c, label);
				if (button != null) {
					return button;
				}
			}
		}

		return null;
	}

	/**
	 * Enable or disable a button with a given label. Assumes no more than one
	 * button has the given label.
	 * 
	 * @param label   the label to match
	 * @param enabled the enable flag
	 */
	public void setButtonEnabled(String label, boolean enabled) {
		AbstractButton button = getButton(this, label);
		if (button != null) {
			button.setEnabled(enabled);
		}
	}

	/**
	 * can do preparation--for example a component might be added on
	 * "createCenterComponent" but a reference needed in "addNorthComponent"
	 */
	protected void prepare() {
	}

	/**
	 * Override to create the component that goes in the east.
	 * 
	 * @return the component that is placed in the east
	 */
	protected Component createEastComponent() {
		return null;
	}

	/**
	 * Override to create the component that goes in the west.
	 * 
	 * @return the component that is placed in the west.
	 */
	protected Component createWestComponent() {
		return null;
	}

	/**
	 * Override to create the component that goes in the center. Usually this is the
	 * "main" component.
	 * 
	 * @return the component that is placed in the center
	 */
	protected Component createCenterComponent() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		reason = e.getActionCommand();
		handleCommand(reason);
		checkButtons();
	}

	/**
	 * Get the reason that the dialog was closed, which is just the label of the
	 * last button hit.
	 * 
	 * @return the label of the last button hit.
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * A closeout button was hit. The default behavior is to shutdown the dialog.
	 * 
	 * @param command the label on the button that was hit.
	 */
	protected void handleCommand(String command) {
		setVisible(false);
	}

	/**
	 * Set the wait cursor
	 */
	public void waitCursor() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	/**
	 * Restore the default cursor
	 */
	public void defaultCursor() {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public static void main(String arg[]) {
		String closeout[] = { "OK", "Apply", "Cancel" };
		SimpleDialog sd = new SimpleDialog("Sample Dialog", null, true, closeout) {

			@Override
			public void handleCommand(String command) {
				if (!command.equals("Apply")) {
					setVisible(false);
				}
			}
		};

		sd.setVisible(true);

		System.exit(0);
	}
}