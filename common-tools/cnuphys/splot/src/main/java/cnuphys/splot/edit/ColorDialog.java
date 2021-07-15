package cnuphys.splot.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

@SuppressWarnings("serial")
public class ColorDialog extends JDialog {
	protected ColorPanel colorPanel = null;
	protected int answer = -1;

	/**
	 * Constructor (transparency disabled).
	 * 
	 * @param title        probably a description of what is having its color
	 *                     changed
	 * @param initColor    the initial color
	 * @param allowNoColor if <code>true</code>, user can select "no color"
	 */

	public ColorDialog(String title, Color initColor, boolean allowNoColor) {
		this(title, initColor, allowNoColor, false);
	}

	/**
	 * Constructor (transparency disabled).
	 * 
	 * @param title             probably a description of what is having its color
	 *                          changed
	 * @param initColor         the initial color
	 * @param allowNoColor      if <code>true</code>, user can select "no color"
	 * @param allowTransparency if <code>true</code>, alpha color slider is enabled.
	 */

	public ColorDialog(String title, Color initColor, boolean allowNoColor, boolean allowTransparency) {
		setTitle((title != null) ? title : "Color Selection");

		setModal(true);
		setup();
		addColorPanel(initColor, allowNoColor, allowTransparency);
		pack();
		DialogUtilities.centerDialog(this);
	}

	/**
	 * Create the color panel
	 * 
	 * @param initColor         The initial color
	 * @param allowNoColor      If true, user can select "no color"
	 * @param allowTransparency It true, alpha color slider is enabled.
	 */

	private void addColorPanel(Color initColor, boolean allowNoColor, boolean allowTransparency) {
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

		ButtonPanel bp = ButtonPanel.closeOutPanel(ButtonPanel.USE_OKCANCEL, alist, 6);

		cp.add("South", bp);

	}

	/**
	 * Get the selected color
	 * 
	 * @return the selected color
	 */
	public Color getColor() {
		if (colorPanel.isNoColorSelected()) {
			return null;
		}
		return colorPanel.getColor();
	}

	/**
	 * Close the dialog
	 * 
	 * @param reason the reason it closed
	 */
	protected void doClose(int reason) {
		answer = reason;
		setVisible(false);

		if (answer == DialogUtilities.OK_RESPONSE) {
		}
	}

	/**
	 * Get the reason the dialog closed
	 * 
	 * @return the reason it closed
	 */
	public int getAnswer() {
		return answer;
	}

}