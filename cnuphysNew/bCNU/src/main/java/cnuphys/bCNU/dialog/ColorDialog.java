package cnuphys.bCNU.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

/**
 * @author heddle
 */
@SuppressWarnings("serial")
public class ColorDialog extends JDialog {
	protected ColorPanel colorPanel = null;
	protected int answer = -1;

	/**
	 * Constructor (transparency disabled).
	 * 
	 * @param initColor
	 *            The initial color
	 * @param allowNoColor
	 *            If true, user can select "no color"
	 */

	public ColorDialog(Color initColor, boolean allowNoColor) {
		this(initColor, allowNoColor, false);
	}

	/**
	 * Constructor (transparency disabled).
	 * 
	 * @param initColor
	 *            The initial color
	 * @param allowNoColor
	 *            If true, user can select "no color"
	 * @param allowTransparency
	 *            It true, alpha color slider is enabled.
	 */

	public ColorDialog(Color initColor, boolean allowNoColor,
			boolean allowTransparency) {
		setTitle("Color Selection");

		setModal(true);
		setup();
		addColorPanel(initColor, allowNoColor, allowTransparency);
		pack();
		DialogUtilities.centerDialog(this);
	}

	/**
	 * Create the color panel
	 * 
	 * @param initColor
	 *            The initial color
	 * @param allowNoColor
	 *            If true, user can select "no color"
	 * @param allowTransparency
	 *            It true, alpha color slider is enabled.
	 */

	private void addColorPanel(Color initColor, boolean allowNoColor,
			boolean allowTransparency) {
		Container cp = getContentPane();
		colorPanel = new ColorPanel();
		cp.add("Center", colorPanel);
		colorPanel.setColor(initColor);
		colorPanel.enableNoColor(allowNoColor);
		colorPanel.setNoColor(initColor == null);
		colorPanel.enableTransparency(allowTransparency);
	}

	/**
	 * Add the components to the dialog
	 */

	protected void setup() {

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout(2, 2));

		// closeout buttons-- use OK and CANCEL

		// buttons

		ActionListener alist = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();

				if (ButtonPanel.OK_LABEL.equals(command)) {
					doClose(DialogUtilities.OK_RESPONSE);
				}

				if (ButtonPanel.CANCEL_LABEL.equals(command)) {
					doClose(DialogUtilities.CANCEL_RESPONSE);
				}

			}

		};

		ButtonPanel bp = ButtonPanel.closeOutPanel(ButtonPanel.USE_OKCANCEL,
				alist, 6);

		cp.add("South", bp);

	}

	public Color getColor() {
		if (colorPanel.isNoColorSelected()) {
			return null;
		}
		return colorPanel.getColor();
	}

	protected void doClose(int reason) {
		answer = reason;
		setVisible(false);

		if (answer == DialogUtilities.OK_RESPONSE) {
		}
	}

	public int getAnswer() {
		return answer;
	}

}
