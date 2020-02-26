package cnuphys.bCNU.application;

import java.awt.EventQueue;

import cnuphys.bCNU.fx.DemoFx2View;
import cnuphys.bCNU.fx.DemoFxView;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.DrawingView;
import cnuphys.bCNU.view.LogView;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.bCNU.view.XMLView;

/**
 * Demonstrates and tests the generic views
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class DemoApp extends BaseMDIApplication {

	// the singleton
	private static DemoApp instance;

	/**
	 * Constructor (private--used to create singleton)
	 * 
	 * @param keyVals
	 *            an optional variable length list of attributes in type-value
	 *            pairs. For example, AttributeType.NAME, "my application",
	 *            AttributeType.CENTER, true, etc.
	 */
	private DemoApp(Object... keyVals) {
		super(keyVals);
	}

	/**
	 * Public access to the singleton.
	 * 
	 * @return the singleton (the main application frame.)(
	 */
	public static DemoApp getInstance() {
		if (instance == null) {
			instance = new DemoApp(PropertySupport.TITLE,
					"Demo Application of Generic bCNU Views",
					PropertySupport.BACKGROUNDIMAGE, "images/cnu.png",
					PropertySupport.FRACTION, 0.8);

			instance.addInitialViews();
		}
		return instance;
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

		// xml view
		XMLView xmlView = new XMLView();
		xmlView.setVisible(false);
		
		//fx view
		new DemoFxView("JavaFX View", 400, 70, 400, 400);
		new DemoFx2View("JavaFX2 View", 500, 270, 400, 400);

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

		final DemoApp frame = getInstance();

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
