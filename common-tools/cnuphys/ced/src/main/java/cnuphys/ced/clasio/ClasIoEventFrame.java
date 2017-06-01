package cnuphys.ced.clasio;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import cnuphys.bCNU.util.Environment;
import cnuphys.ced.clasio.table.NodePanel;

@SuppressWarnings("serial")
public class ClasIoEventFrame extends JFrame {

	// holds the panel that has the table
	protected NodePanel _nodePanel;

	// the application frame
	public ClasIoEventFrame(String title) {

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(1);
			}
		};
		addWindowListener(windowAdapter);
		setLayout(new BorderLayout());

		_nodePanel = new NodePanel();

		add(_nodePanel, BorderLayout.CENTER);

		makeMenus();

		// set to a fraction of screen
		// Dimension d = GraphicsUtilities.screenFraction(0.9);
		// setSize(d);

		pack();
	}

	// make the menus
	private void makeMenus() {
		JMenuBar menubar = new JMenuBar();
		setJMenuBar(menubar);
		menubar.add(new ClasIoEventMenu(false, true));
	}

	public static void main(String arg[]) {

		System.out.println("App name: "
				+ Environment.getInstance().getApplicationName());

		final ClasIoEventFrame testFrame = new ClasIoEventFrame("EVIO Events");

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// testFrame.pack();
				testFrame.setVisible(true);
				testFrame.setLocationRelativeTo(null);
			}
		});
	}

}