package cnuphys.bCNU.application;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.DrawingView;
import cnuphys.bCNU.view.LogView;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.bCNU.view.XMLView;

public class DemoApp2 extends BaseMDIApplication {

	// the singleton
	private static DemoApp2 instance;

	/**
	 * Constructor (private--used to create singleton)
	 * 
	 * @param keyVals
	 *            an optional variable length list of attributes in type-value
	 *            pairs. For example, AttributeType.NAME, "my application",
	 *            AttributeType.CENTER, true, etc.
	 */
	private DemoApp2(Object... keyVals) {
		super(keyVals);
	}

	/**
	 * Public access to the singleton.
	 * 
	 * @return the singleton (the main application frame.)(
	 */
	public static DemoApp2 getInstance() {
		if (instance == null) {
			instance = new DemoApp2(PropertySupport.TITLE,
					"Demo Application of Generic bCNU Views",
					PropertySupport.BACKGROUNDIMAGE, "images/cnu.png",
					PropertySupport.FRACTION, 0.65);

			instance.addInitialViews();
		}

		return instance;
	}

	private JPanel makeUserPanel() {
		JPanel panel = new JPanel();

		panel.setBorder(new CommonBorder("Gagik's Curiosities"));

		panel.setLayout(new BorderLayout(4, 4));
		// north
		JPanel nPanel = new JPanel();
		nPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		nPanel.add(new JButton("Go"));
		nPanel.add(new JButton("To"));
		nPanel.add(new JButton("Hell"));

		// center
		try {
			final BufferedImage img = ImageIO
					.read(new URL(
							"http://i5.photobucket.com/albums/y188/MsPurrl/Fictional%20Characters/Zippy.jpg"));

			JComponent comp = new JComponent() {
				@Override
				public void paintComponent(Graphics g) {
					g.drawImage(img, 0, 0, this);
				}
			};
			panel.add(comp, BorderLayout.CENTER);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// south
		JPanel sPanel = new JPanel();
		sPanel.setLayout(new GridLayout(2, 2));
		sPanel.add(new JButton("Are we"));
		sPanel.add(new JButton("Having"));
		sPanel.add(new JButton("Fun"));
		sPanel.add(new JButton("Yet?"));

		panel.add(nPanel, BorderLayout.NORTH);
		panel.add(sPanel, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * Add the initial views to the desktop.
	 */
	private void addInitialViews() {

		// add logview
		LogView logView = new LogView();
		logView.setVisible(false);
		ViewManager.getInstance().getViewMenu().addSeparator();

		// log some environment info
		Log.getInstance().config(Environment.getInstance().toString());

		// drawing view
		DrawingView drawingView = DrawingView.createDrawingView();
		drawingView.setVisible(true);

		// add a user panel on the east
		drawingView.add(makeUserPanel(), BorderLayout.EAST);

		// xml view
		XMLView xmlView = new XMLView();
		xmlView.setVisible(false);

	}

	/**
	 * Main program used for testing only.
	 * <p>
	 * Command line arguments:</br> -p [dir] dir is the optional default
	 * directory for the file manager
	 * 
	 * @param arg
	 *            the command line arguments.
	 */
	public static void main(String[] arg) {

		if ((arg != null) && (arg.length > 0)) {
			int len = arg.length;
			int lm1 = len - 1;
			boolean done = false;
			int i = 0;
			while (!done) {
				if (arg[i].equalsIgnoreCase("-p")) {
					if (i < lm1) {
						i++;
						FileUtilities.setDefaultDir(arg[i]);
					}
				}
				i++;
				done = (i >= lm1);
			}
		}

		final DemoApp2 frame = getInstance();

		// now make the frame visible, in the AWT thread
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				frame.setVisible(true);
			}

		});
		Log.getInstance().error("DemoApp is ready.");
	}
}
