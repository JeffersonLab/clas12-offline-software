package cnuphys.bCNU.log;

import java.awt.BorderLayout;

import javax.swing.JDialog;

import cnuphys.bCNU.graphics.GraphicsUtilities;

@SuppressWarnings("serial")
public class SimpleLogDialog extends JDialog {

	/**
	 * Creat a simple dialog for displaying log messages.
	 */
	public SimpleLogDialog() {
		setTitle("Log Messages");
		setModal(false);
		add(new SimpleLogPane(), BorderLayout.CENTER);
		pack();
		GraphicsUtilities.centerComponent(this);
	}
}
