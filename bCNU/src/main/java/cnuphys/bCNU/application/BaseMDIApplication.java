package cnuphys.bCNU.application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.attributes.Attributes;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.menu.FileMenu;
import cnuphys.bCNU.menu.MenuManager;
import cnuphys.bCNU.menu.OptionMenu;
import cnuphys.bCNU.menu.WindowMenu;
import cnuphys.bCNU.view.ViewManager;

/**
 * This is the basic "desktop frame" for a MDI application. The constructor
 * takes a variable length list of arguments which should be name-value pairs of
 * attributes. The attributes can be standard attributes, or user defined
 * attributes. All attributes are optional, and attributes can be provided in
 * any order.
 * 
 * @see cnuphys.bCNU.attributes.Attributes
 * 
 * @author heddle
 * 
 */

@SuppressWarnings("serial")
public class BaseMDIApplication extends JFrame {

	/**
	 * Attributes created from the variable length arguments.
	 */
	protected Attributes _attributes;

	/**
	 * Constructor
	 * 
	 * @param keyVals
	 *            an optional variable length list of attributes in type-value
	 *            pairs. For example, AttributeType.NAME, "my application",
	 *            AttributeType.CENTER, true, etc.
	 */
	public BaseMDIApplication(Object... keyVals) {

		_attributes = new Attributes(keyVals);

		// create a menu manager
		setJMenuBar(new JMenuBar());
		MenuManager menuManager = MenuManager.createMenuManager(getJMenuBar());

		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.out.println("Exiting.");
				System.exit(0);
			}
		};
		addWindowListener(wa);

		// Get the attributes that we recognize;

		WindowMenu.setUseWindowMenu(_attributes
				.booleanValue(AttributeType.WINDOWMENU));

		Color background = _attributes.colorValue(AttributeType.BACKGROUND);
		String backgroundImage = _attributes
				.stringValue(AttributeType.BACKGROUNDIMAGE);
		String title = _attributes.stringValue(AttributeType.TITLE);
		boolean maximize = _attributes.booleanValue(AttributeType.MAXIMIZE);
		double screenFraction = _attributes.doubleValue(AttributeType.FRACTION);
		int width = _attributes.intValue(AttributeType.WIDTH);
		int height = _attributes.intValue(AttributeType.HEIGHT);

		// set the title
		if (title == null) {
			title = "MDI Application";
		}
		setTitle(title);

		// create the desktop
		Desktop desktop = Desktop.createDesktop(background, backgroundImage);
		add(desktop, BorderLayout.CENTER);
		// setContentPane(_desktop);

		// to size the frame we use this precedence order.
		// 1) If MAXIMIZE specified then maximize,
		// 2) Else if fraction provided, use as a screen fraction, e.g., 0.85
		// 3) Else use width and height, if provided, with default minimum
		// values.

		// maximize?
		if (maximize) {
			setExtendedState(getExtendedState() | Frame.MAXIMIZED_BOTH);
		} else if (!Double.isNaN(screenFraction)) { // fractional size
			screenFraction = Math.max(0.25, Math.min(1.0, screenFraction));
			setSize(GraphicsUtilities.screenFraction(screenFraction));
		} else {
			width = Math.max(400, Math.min(4000, width));
			height = Math.max(400, Math.min(4000, height));
			setSize(width, height);
		}
		GraphicsUtilities.centerComponent(this);

		// add the basic file menu. Actual apps can add to it.
		menuManager.addMenu(new FileMenu());

		// add the basic option menu. Actual apps can add to it.
		// add option menu defaults?
		boolean adOptionMenuDefaults = _attributes
				.booleanValue(AttributeType.OPTIONMENUDEFAULTS);
		menuManager.addMenu(new OptionMenu(adOptionMenuDefaults));

		// add the view menu
		menuManager.addMenu(ViewManager.getInstance().getViewMenu());

		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * Constructor
	 * 
	 * @param noAdapter
	 *            tells the BaseMDIApplication to ignore it's own window adapter
	 *            so a child can use it's own. True = Child uses own else use
	 *            base MDI
	 * 
	 * @param keyVals
	 *            an optional variable length list of attributes in type-value
	 *            pairs. For example, AttributeType.NAME, "my application",
	 *            AttributeType.CENTER, true, etc.
	 * 
	 */
	public BaseMDIApplication(boolean noAdapter, Object... keyVals) {

		_attributes = new Attributes(keyVals);

		// create a menu manager
		setJMenuBar(new JMenuBar());
		MenuManager menuManager = MenuManager.createMenuManager(getJMenuBar());

		if (!noAdapter) {
			WindowAdapter wa = new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent we) {
					System.exit(0);
				}
			};
			addWindowListener(wa);
		}

		// Get the attributes that we recognize;
		Color background = _attributes.colorValue(AttributeType.BACKGROUND);
		String backgroundImage = _attributes
				.stringValue(AttributeType.BACKGROUNDIMAGE);
		String title = _attributes.stringValue(AttributeType.TITLE);
		boolean maximize = _attributes.booleanValue(AttributeType.MAXIMIZE);
		double screenFraction = _attributes.doubleValue(AttributeType.FRACTION);
		int width = _attributes.intValue(AttributeType.WIDTH);
		int height = _attributes.intValue(AttributeType.HEIGHT);

		// set the title
		if (title == null) {
			title = "MDI Application";
		}
		setTitle(title);

		// create the desktop
		Desktop desktop = Desktop.createDesktop(background, backgroundImage);
		add(desktop, BorderLayout.CENTER);
		// setContentPane(_desktop);

		// to size the frame we use this precedence order.
		// 1) If MAXIMIZE specified then maximize,
		// 2) Else if fraction provided, use as a screen fraction, e.g., 0.85
		// 3) Else use width and height, if provided, with default minimum
		// values.

		// maximize?
		if (maximize) {
			setExtendedState(getExtendedState() | Frame.MAXIMIZED_BOTH);
		} else if (!Double.isNaN(screenFraction)) { // fractional size
			screenFraction = Math.max(0.25, Math.min(1.0, screenFraction));
			setSize(GraphicsUtilities.screenFraction(screenFraction));
		} else {
			width = Math.max(400, Math.min(4000, width));
			height = Math.max(400, Math.min(4000, height));
			setSize(width, height);
		}
		GraphicsUtilities.centerComponent(this);

		// add the basic file menu. Actual apps can add to it.
		menuManager.addMenu(new FileMenu());

		// add the basic option menu. Actual apps can add to it.
		// add option menu defaults?
		boolean adOptionMenuDefaults = _attributes
				.booleanValue(AttributeType.OPTIONMENUDEFAULTS);
		menuManager.addMenu(new OptionMenu(adOptionMenuDefaults));

		// add the view menu
		menuManager.addMenu(ViewManager.getInstance().getViewMenu());

		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * A request has come from a server, for example a Grails application
	 * 
	 * @param request
	 *            the request in the form of a Properties object.
	 * @return an object providing the response to the request. Often it will be
	 *         an image.
	 */
	public Object serverRequest(Properties request) {
		return null;
	}
}
