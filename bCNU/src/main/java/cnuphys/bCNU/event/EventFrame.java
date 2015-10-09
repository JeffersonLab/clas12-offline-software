package cnuphys.bCNU.event;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.menu.MenuManager;
import cnuphys.bCNU.view.EventPanel;

@SuppressWarnings("serial")
public class EventFrame extends JFrame {

	// the overall tabbed frame
	protected JTabbedPane tabbedPane;

	// holds the tree view of the event
	protected EventPanel eventPanel;

	// the application frame
	public EventFrame(String title) {

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(1);
			}
		};
		addWindowListener(windowAdapter);
		setLayout(new BorderLayout());

		createMenus();
		eventPanel = new EventPanel();

		tabbedPane = new JTabbedPane();
		tabbedPane.add(eventPanel, "Events");

		add(tabbedPane, BorderLayout.CENTER);

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.9);
		setSize(d);
	}

	// create the menus
	private void createMenus() {
		JMenuBar mbar = new JMenuBar();
		setJMenuBar(mbar);

		MenuManager.createMenuManager(mbar);
	}

	public static void main(String arg[]) {

		final EventFrame testFrame = new EventFrame("EVIO Events");

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
