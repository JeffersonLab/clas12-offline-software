package cnuphys.bCNU.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.log.Log;

public class DialogUtilities {

	/**
	 * Dialog "Reason" constant
	 */

	public static final int OK_RESPONSE = 0;

	/**
	 * Dialog "Reason" constant
	 */

	public static final int CANCEL_RESPONSE = 1;

	/**
	 * Current answer string
	 */

	protected String outputdata = null;

	/**
	 * Dialog "Reason" constant
	 */

	public static final int APPLY_RESPONSE = 2;

	/**
	 * Dialog "Reason" constant
	 */

	public static final int DONE_RESPONSE = 0;

	/**
	 * Dialog "Reason" constant
	 */

	public static final int YES_RESPONSE = 0;

	/**
	 * Dialog "Reason" constant
	 */

	public static final int NO_RESPONSE = 1;

	/**
	 * Center a dialog
	 * 
	 * @param dialog the dialog to center
	 */

	public static void centerDialog(JDialog dialog) {
		GraphicsUtilities.centerComponent(dialog);
	}

	/**
	 * Place a component in the upper right
	 * 
	 * @param component The Component to center.
	 */
	public static void upperRightComponent(Component component, int dh, int dv) {

		if (component == null) {
			return;
		}

		try {

			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] allScreens = env.getScreenDevices();
			GraphicsConfiguration gc = allScreens[0].getDefaultConfiguration();

			Rectangle bounds = gc.getBounds();
			Dimension componentSize = component.getSize();
			if (componentSize.height > bounds.height) {
				componentSize.height = bounds.height;
			}
			if (componentSize.width > bounds.width) {
				componentSize.width = bounds.width;
			}

			int x = bounds.x + bounds.width - componentSize.width - dh;
			int y = bounds.y + dv;

			component.setLocation(x, y);

		} catch (Exception e) {
			Log.getInstance().exception(e);
			component.setLocation(200, 200);
			e.printStackTrace();
		}
	}

	/**
	 * Convenience routine for padding a string using the default font.
	 * 
	 * @param inp  The string to be padded.
	 * @param tstr The test string-- try to return a string the same length
	 */

	public String padString(Component c, String inp, String tstr) {

		String str;
		int oldgap;
		int newgap;

		if (inp == null) {
			str = new String("");
		} else {
			str = new String(inp);
		}

		FontMetrics fm = c.getFontMetrics(c.getFont());

		int sw = fm.stringWidth(tstr);
		oldgap = Math.abs(sw - fm.stringWidth(str));

		while (true) {
			String str2 = str += " ";
			newgap = Math.abs(sw - fm.stringWidth(str2));
			if (newgap < oldgap) {
				str = str2;
				oldgap = newgap;
			} else {
				break;
			}
		}

		return str;
	}

	/**
	 * Create a nice padded panel.
	 * 
	 * @param hpad      the pixel pad on the left and right
	 * @param vpad      the pixel pad on the top and bottom
	 * @param component the main component placed in the center.
	 * @return the padded panel
	 */
	public static JPanel paddedPanel(int hpad, int vpad, Component component) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		if (hpad > 0) {
			panel.add(Box.createHorizontalStrut(hpad), BorderLayout.WEST);
			panel.add(Box.createHorizontalStrut(hpad), BorderLayout.EAST);
		}
		if (hpad > 0) {
			panel.add(Box.createVerticalStrut(vpad), BorderLayout.NORTH);
			panel.add(Box.createVerticalStrut(vpad), BorderLayout.SOUTH);
		}

		panel.add(component, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Create a dialog with a prompt and a set of options
	 * 
	 * @param prompt
	 * @param options
	 * @return a result indicating yes or no.
	 */
	public static int yesNoDialog(String prompt, String... options) {

		JOptionPane pane = new JOptionPane(prompt);

		pane.setOptions(options);
		JDialog dialog = pane.createDialog(null, "Dialog");
		dialog.setVisible(true);
		Object obj = pane.getValue();
		for (int k = 0; k < options.length; k++) {
			if (options[k].equals(obj)) {
				return k;
			}
		}
		return -1;
	}

}
