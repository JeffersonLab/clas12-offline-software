package cnuphys.bCNU.eliza;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import javax.swing.JFrame;

/**
 * Eliza Application.
 * 
 * Adapted from implementation by Charles Hayden see:
 * http://chayden.net/eliza/Eliza.html
 */
public class ElizaApp extends JFrame {

	private ElizaPanel _elizaPanel;

	public ElizaApp() {
		super("Eliza");

		addContent();
		System.err.println("done adding content");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// setSize(600, 600);
		pack();
	}

	private void addContent() {
		setLayout(new BorderLayout(4, 4));
		_elizaPanel = new ElizaPanel();

		add("Center", _elizaPanel);
	}

	/**
	 * Center a component.
	 * 
	 * @param component
	 *            The Component to center.
	 */
	public static void centerComponent(Component component) {

		if (component == null)
			return;

		try {
			
			Dimension screenSize = null;
			try {
				GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
				int width = gd.getDisplayMode().getWidth();
				int height = gd.getDisplayMode().getHeight();
				if ((width > 100) && (height > 100)) {
					screenSize = new Dimension(width, height);
				}
			}
			catch (Exception e) {	
			}

			if (screenSize == null) {
				screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			}
			Dimension componentSize = component.getSize();
			if (componentSize.height > screenSize.height) {
				componentSize.height = screenSize.height;
			}
			if (componentSize.width > screenSize.width) {
				componentSize.width = screenSize.width;
			}

			int x = ((screenSize.width - componentSize.width) / 2);
			int y = ((screenSize.height - componentSize.height) / 2);

			component.setLocation(x, y);

		} catch (Exception e) {
			component.setLocation(200, 200);
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		final ElizaApp app = new ElizaApp();
		centerComponent(app);
		System.err.println("Size: " + app.getSize());

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				app.setVisible(true);
			}
		});

	}

}