package cnuphys.cnf.frame;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.cnf.event.EventManager;


public class DefCommon {

	/** Environment variable name */
	public static final String VNAME = "CLAS12DIR";

	//return true on success
	private static boolean clas12DirFromEnvVar() {
		String c12dir = System.getenv(VNAME);
		
		if (c12dir == null) {
			System.err.println("No environment variable named [" + VNAME + "] was found.");
			return false;
		}
		
		System.err.println("Found environment variable named [" + VNAME + "] value [" + c12dir + "]");
		
		//see if it is a directory
		File file = new File(c12dir);
		if (!file.exists()) {
			System.err.println("The directory [" + c12dir + "] does not exist. Please check your " + VNAME + " environment variable.");
			System.exit(1);
		}
		if (!file.isDirectory()) {
			System.err.println("[" + c12dir + "] is not a directory. Please check your " + VNAME + " environment variable.");
			System.exit(1);
		}
		
		return true;
	}
	
	// this is so we can find json files
	protected static void initClas12Dir(boolean checkEnv) throws IOException {
		
		// first try, environment variable
		if (checkEnv) {
			if (clas12DirFromEnvVar()) {
				return;
			}
		}

		// for running from runnable jar (for coatjava)
		String clas12dir = System.getProperty("CLAS12DIR");

		if (clas12dir == null) {
			clas12dir = "coatjava";
		}

		File clasDir = new File(clas12dir);

		if (clasDir.exists() && clasDir.isDirectory()) {
			System.err.println("**** Found CLAS12DIR [" + clasDir.getCanonicalPath() + "]");
			System.setProperty("CLAS12DIR", clas12dir);
			Log.getInstance().config("CLAS12DIR: " + clas12dir);
			return;
		} else {
			System.err.println("**** Did not find CLAS12DIR [" + clasDir.getCanonicalPath() + "]");
		}

		String cwd = Environment.getInstance().getCurrentWorkingDirectory();
		clas12dir = cwd + "/../../../coatjava";
		clasDir = new File(clas12dir);

		if (clasDir.exists() && clasDir.isDirectory()) {
			System.err.println("**** Found CLAS12DIR [" + clasDir.getCanonicalPath() + "]");
			System.setProperty("CLAS12DIR", clas12dir);
			Log.getInstance().config("CLAS12DIR: " + clas12dir);
			return;
		} else {
			System.err.println("**** Did not find CLAS12DIR [" + clasDir.getCanonicalPath() + "]");
		}

		throw (new IOException("Could not locate the coatjava directory."));
	}
	
	
	/**
	 * Fix the event count label
	 */
	protected static  void fixEventMenuLabels(JMenuItem eventCountLabel, JMenuItem eventRemainingLabel) {
		int count = EventManager.getInstance().getEventCount();
		if (count < Integer.MAX_VALUE) {
			eventCountLabel.setText("Event Count: " + count);
		} else {
			eventCountLabel.setText("Event Count: N/A");
		}
		
		int numRemain = EventManager.getInstance().getNumRemainingEvents();
		eventRemainingLabel.setText("Events Remaining: " + numRemain);
	}

	
	protected static void fixTitle(JFrame frame) {
		String title = frame.getTitle();

		// adjust title as needed
		frame.setTitle(title);
	}

	protected static void setEventNumberLabel(JLabel eventNumberLabel, int num) {

		if (num < 0) {
			eventNumberLabel.setText("  Event Num:      ");
		} else {
			eventNumberLabel.setText("  Event Num: " + num);
		}
	}

	// add to the event menu
	protected static JMenuItem addEventCountToEventMenu(JMenu eventMenu) {

		JMenuItem eventCountLabel = new JMenuItem("Event Count: N/A");
		eventCountLabel.setOpaque(true);
		eventCountLabel.setBackground(Color.white);
		eventCountLabel.setForeground(X11Colors.getX11Color("Dark Blue"));
		eventMenu.add(eventCountLabel);
		return eventCountLabel;	
	}


	// add to the event menu
	protected static JMenuItem addEventRemainingToEventMenu(JMenu eventMenu) {
		
		JMenuItem  eventRemainingLabel = new JMenuItem("Events Remaining: N/A");
		eventRemainingLabel.setOpaque(true);
		eventRemainingLabel.setBackground(Color.white);
		eventRemainingLabel.setForeground(X11Colors.getX11Color("Dark Blue"));
		eventMenu.add(eventRemainingLabel);

		return eventRemainingLabel;
	}
	
	// create the event number label
	protected static JLabel createEventNumberLabel(JFrame frame) {
		JLabel _eventNumberLabel = new JLabel("  Event Num: ");
		_eventNumberLabel.setOpaque(true);
		_eventNumberLabel.setBackground(Color.black);
		_eventNumberLabel.setForeground(Color.yellow);
		_eventNumberLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		_eventNumberLabel.setBorder(BorderFactory.createLineBorder(Color.cyan, 1));
		setEventNumberLabel(_eventNumberLabel, -1);

		frame.getJMenuBar().add(Box.createHorizontalGlue());
		frame.getJMenuBar().add(_eventNumberLabel);
		frame.getJMenuBar().add(Box.createHorizontalStrut(100));
		return _eventNumberLabel;
	}

}
