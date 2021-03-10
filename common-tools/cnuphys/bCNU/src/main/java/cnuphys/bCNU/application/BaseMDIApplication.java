package cnuphys.bCNU.application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import cnuphys.bCNU.feedback.HeadsUpDisplay;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.menu.FileMenu;
import cnuphys.bCNU.menu.MenuManager;
import cnuphys.bCNU.menu.OptionMenu;
import cnuphys.bCNU.util.PropertySupport;
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
public class BaseMDIApplication extends JFrame implements KeyEventDispatcher {

	/**
	 * Attributes created from the variable length arguments.
	 */
	protected Properties _properties;

	// used (optionally) for feedback
	private static HeadsUpDisplay _headsUpDisplay;

	// singleton. Applications cannot create other applications.
	private static BaseMDIApplication instance;

	/**
	 * Constructor
	 * 
	 * @param keyVals an optional variable length list of attributes in type-value
	 *                pairs. For example, AttributeType.NAME, "my application",
	 *                AttributeType.CENTER, true, etc.
	 */
	protected BaseMDIApplication(Object... keyVals) {

		if (instance != null) {
			System.err.println("Singleton violation in BaseMDI Application");
			System.exit(1);
		}

		_properties = PropertySupport.fromKeyValues(keyVals);

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

		Color background = PropertySupport.getBackground(_properties);
		String backgroundImage = PropertySupport.getBackgroundImage(_properties);
		String title = PropertySupport.getTitle(_properties);
		boolean maximize = PropertySupport.getMaximize(_properties);
		double screenFraction = PropertySupport.getFraction(_properties);
		int width = PropertySupport.getWidth(_properties);
		int height = PropertySupport.getHeight(_properties);

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
		menuManager.addMenu(new OptionMenu());

		// add the view and plugin menus
		menuManager.addMenu(ViewManager.getInstance().getViewMenu());

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// add a global key listener
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

		instance = this;
	}

	public static BaseMDIApplication getApplication() {
		return instance;
	}

	/**
	 * Get the heads up display (might be null)
	 * 
	 * @return the heads up display
	 */
	public static HeadsUpDisplay getHeadsUpDisplay() {
		return _headsUpDisplay;
	}

	/**
	 * Add a heads up display (as a glass pane) for this application
	 */
	protected static void addHeadsUp() {
		_headsUpDisplay = new HeadsUpDisplay(instance);
		instance.setGlassPane(_headsUpDisplay);
		instance.getGlassPane().setVisible(true);

	}

	/**
	 * The global key dispatcher. Default implementation passes the event to type
	 * handlers.
	 * 
	 * @param the key event
	 * @return <code>false</code> if the event should continue down the chain
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {

		int type = e.getID();

		if (type == KeyEvent.KEY_PRESSED) {
			return handleKeyPressed(e);
		} else if (type == KeyEvent.KEY_RELEASED) {
			return handleKeyTyped(e);
		}
		if (type == KeyEvent.KEY_TYPED) {
			return handleKeyReleased(e);
		}
		return false;
	}

	/**
	 * The dispatcher handed this a key press event. The default implementation does
	 * nothing.
	 * 
	 * @param e the key event
	 * @return <code>false</code> if the event should continue down the chain
	 */
	protected boolean handleKeyPressed(KeyEvent e) {
		return false;
	}

	/**
	 * The dispatcher handed this a key type event. The default implementation does
	 * nothing.
	 * 
	 * @param e the key event
	 * @return <code>false</code> if the event should continue down the chain
	 */
	protected boolean handleKeyTyped(KeyEvent e) {
		return false;
	}

	/**
	 * The dispatcher handed this a key release event. The default implementation
	 * does nothing.
	 * 
	 * @param e the key event
	 * @return <code>false</code> if the event should continue down the chain
	 */
	protected boolean handleKeyReleased(KeyEvent e) {
		return false;
	}

}
