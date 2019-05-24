package cnuphys.splot.edit;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;

public class ButtonPanel extends JPanel {

	/**
	 * Bits indicating use OK, Cancel, ...
	 */

	public static final int USE_OK = 01;

	public static final int USE_CANCEL = 02;

	public static final int USE_APPLY = 04;

	public static final int USE_DELETE = 010;

	/**
	 * Bit indicating using combinations OK and CANCEL
	 */
	public static final int USE_OKCANCEL = USE_OK | USE_CANCEL;

	public static final int USE_OKCANCELAPPLY = USE_OK | USE_CANCEL | USE_APPLY;

	public static final int USE_OKCANCELDELETE = USE_OK | USE_CANCEL | USE_DELETE;

	/**
	 * Constant indicating an "OK" response
	 */

	public static final int OK_RESPONSE = 0;

	/**
	 * Constant indicating a "cancel" response
	 */

	public static final int CANCEL_RESPONSE = 1;

	/**
	 * Constant indicating an "apply" response
	 */

	public static final int APPLY_RESPONSE = 2;

	/*
	 * Constant indicating a "delete" response
	 */

	public static final int DELETE_RESPONSE = 3;

	/**
	 * Constant string for "ok" label
	 */

	public static final String OK_LABEL = " OK ";

	/**
	 * Constant string for "cancel" label
	 */

	public static final String CANCEL_LABEL = " Cancel ";

	/**
	 * Constant string for "apply" label
	 */

	public static final String APPLY_LABEL = " Apply ";

	/**
	 * Constant string for "delete" label
	 */

	public static final String DELETE_LABEL = " Delete ";

	/**
	 * The buttons
	 */

	protected JButton[] buttons = null;

	/**
	 * The labels
	 */

	protected String[] labels = null;

	/**
	 * The constructor.
	 * 
	 * @param labels The button labels.
	 */

	public ButtonPanel(String labels[]) {
		this(labels, null, -1);
	}

	/**
	 * The constructor.
	 * 
	 * @param labels The button labels.
	 * @param alist  Optional action listener.
	 */

	public ButtonPanel(String labels[], ActionListener alist) {
		this(labels, alist, 4, -1);
	}

	/**
	 * The constructor.
	 * 
	 * @param labels  The button labels.
	 * @param alist   Optional action listener.
	 * @param spacing The spacing between labels.
	 */

	public ButtonPanel(String labels[], ActionListener alist, int spacing) {

		this(labels, alist, spacing, FlowLayout.CENTER);
	}

	/**
	 * The constructor.
	 * 
	 * @param labels    the button labels.
	 * @param alist     optional action listener.
	 * @param spacing   the spacing between labels.
	 * @param alignment the layout alignment
	 */

	public ButtonPanel(String labels[], ActionListener alist, int spacing, int alignment) {

		super(new FlowLayout(alignment, spacing, 0));
		this.labels = labels;

		userSetup();

		if (this.labels == null) {
			return;
		}

		addButtons(alist);
	}

	/**
	 * Place for subclasses to add something else
	 */

	protected void userSetup() {
	}

	/**
	 * Add the buttons to the panel.
	 * 
	 * @param alist An action listener.
	 */

	protected void addButtons(ActionListener alist) {

		buttons = new JButton[labels.length];

		for (int i = 0; i < labels.length; i++) {
			if (labels[i] != null) {
				buttons[i] = new JButton(labels[i]);
				add(buttons[i]);
				if (alist != null) {
					buttons[i].addActionListener(alist);
				}
			}
			else {
				buttons[i] = null;
			}
		}
	}

	/**
	 * Enable or disable a button.
	 * 
	 * @param index the index of the button. If -1, applies to all.
	 * @param state if true, enable, else disable.
	 */

	public void setEnabled(int index, boolean state) {

		if (index < 0) {
			for (int i = 0; i < buttons.length; i++) {
				if (buttons[i] != null) {
					buttons[i].setEnabled(state);
				}
			}
		}
		else if (index < buttons.length) {
			if (buttons[index] != null) {
				buttons[index].setEnabled(state);
			}
		}
	}

	/**
	 * Set tooltip
	 * 
	 * @param index The index of the button.
	 * @param tip   tooltip string
	 */

	public void setToolTip(int index, String tip) {
		if (buttons[index] != null) {
			buttons[index].setToolTipText(tip);
		}
	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
	}

	/**
	 * Makes a button panel with combinations of OK, Save, Save As, Cancel, Apply,
	 * and Delete
	 * 
	 * @param opt            Bitwise combination of USE_OK, USE_CANCEL, USE_APPLY,
	 *                       USE_DELETE
	 * @param actionListener Action listener for button clicks
	 * @param spacing        The spacing between labels.
	 */

	public static ButtonPanel closeOutPanel(int opt, ActionListener actionListener, int spacing) {

		Vector<String> vl = new Vector<String>(7);

		if (checkBit(opt, USE_OK)) {
			vl.addElement(OK_LABEL);
		}

		if (checkBit(opt, USE_CANCEL)) {
			vl.addElement(CANCEL_LABEL);
		}

		if (checkBit(opt, USE_APPLY)) {
			vl.addElement(APPLY_LABEL);
		}

		if (checkBit(opt, USE_DELETE)) {
			vl.addElement(DELETE_LABEL);
		}

		int num = vl.size();

		if (num < 1) {
			return null;
		}

		String[] labels = new String[num];

		for (int i = 0; i < num; i++) {
			labels[i] = (vl.elementAt(i));
		}

		return new ButtonPanel(labels, actionListener, spacing);
	}

	/**
	 * Method to see if a bit is set.
	 */

	private static boolean checkBit(int opt, int bit) {
		return ((opt & bit) == bit);
	}

}