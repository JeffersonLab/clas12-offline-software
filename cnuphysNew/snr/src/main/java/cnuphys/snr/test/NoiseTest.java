package cnuphys.snr.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import cnuphys.snr.NoiseReductionParameters;

@SuppressWarnings("serial")
public class NoiseTest extends JFrame {

	private DetectorTest detectorTest;

	private NoiseReductionParameters parameters = NoiseReductionParameters.getDefaultParameters();

	/**
	 * Constructor
	 */
	public NoiseTest() {
		super("Noise and Segment Finding Test");
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		};
		addWindowListener(wa);

		addComponents();

		setSize(1200, 900);
		setLocation(200, 100);
	};

	/**
	 * Add the GUI components
	 */
	public void addComponents() {
		double bw = 10.0;
		double xmin = 0.15;
		double width = bw - 2.0 * xmin;

		double csize = width / parameters.getNumWire();

		double height = parameters.getNumLayer() * csize;

		double dy = height / 6;

		// space fopr two extra superlayer (left/right composite superlayers)
		double bh = (6 + 2.5) * (dy + height) + dy;

		detectorTest = new DetectorTest(parameters, 0.0, 0.0, bw, bh);

		for (int i = 1; i <= 6; i++) {
			double y = (i + 1.5) * (dy + height);
			detectorTest.addChamber(new Rectangle2D.Double(xmin, y, width, height));
		}

		// composite chamber
		double y = 2 * dy;
		detectorTest.addCompositeChamber(NoiseReductionParameters.LEFT_LEAN,
				new Rectangle2D.Double(xmin, y, width, height));

		y += (dy + height);
		detectorTest.addCompositeChamber(NoiseReductionParameters.RIGHT_LEAN,
				new Rectangle2D.Double(xmin, y, width, height));

		add(detectorTest, BorderLayout.CENTER);

		addMenus();
	}

	/**
	 * Add all the menus.
	 */
	private void addMenus() {
		JMenuBar menubar = new JMenuBar();
		menubar.add(createFileMenu());
		menubar.add(createEventMenu());
		menubar.add(createOptionMenu());
		// menubar.add(createTestMenu());

		addCleanHotSpot(menubar);
		setJMenuBar(menubar);
	}

	private void addCleanHotSpot(JMenuBar menubar) {
		menubar.add(Box.createHorizontalStrut(40));
		final JLabel clean = new JLabel(" Show Cleaned Data ");
		clean.setOpaque(true);
		clean.setBackground(Color.darkGray);
		clean.setForeground(Color.cyan);
		clean.setBorder(BorderFactory.createEtchedBorder());
		menubar.add(clean);

		MouseAdapter ml = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent me) {
				clean.setBackground(Color.darkGray);
				clean.setForeground(Color.yellow);
				TestParameters.noiseOff = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent me) {
				clean.setBackground(Color.darkGray);
				clean.setForeground(Color.cyan);
				TestParameters.noiseOff = false;
				repaint();
			}

		};

		clean.addMouseListener(ml);
	}

	/**
	 * Create the option menu.
	 * 
	 * @return the option menu.
	 */
	private JMenu createOptionMenu() {
		JMenu menu = new JMenu("Options");
		menu.add(detectorTest.getDisplayOptionMenu());
		return menu;
	}

	private Frame getParentFrame(Component comp) {
		return comp != null ? (Frame) SwingUtilities.getAncestorOfClass(Frame.class, comp) : null;
	}

	// /**
	// * Create the test menu.
	// *
	// * @return the test menu.
	// */
	// private JMenu createTestMenu() {
	// JMenu menu = new JMenu("Tests");
	//
	// final JMenuItem pnrTestItem = new
	// JMenuItem("Percent Noise Removed vs. Noise Rate");
	// final JMenuItem wireCountTestItem = new
	// JMenuItem("Number of Wires vs. Time");
	//
	// final NoiseTest ntest = this;
	//
	// ActionListener al = new ActionListener() {
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	//
	//
	// detectorTest.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	//
	// Object source = e.getSource();
	// int numEvent = 200000;
	// int numRun = 6;
	// TestParameters.setNoiseRate(0.03);
	// //mstr suitable for Mathematica
	// String mStr = "{";
	// long counts[] = new long[4];
	//
	// if (source == pnrTestItem) {
	// for (int i = 1; i < 11; i++) {
	// TestParameters.setNoiseRate(0.01*i);
	// for (int j = 0; j < counts.length; j++) {
	// counts[j] = 0;
	// }
	//
	// detectorTest.doNoiseTest(numEvent, counts);
	// //order track hits, noise, removed noise, saved noise
	// long noiseHits = counts[1];
	// long noiseRemovedHits = counts[2];
	// double percentRemoved = 100;
	// if (noiseHits > 0) {
	// percentRemoved = (100.0 * noiseRemovedHits)/(double)noiseHits;
	// }
	//
	// String s = String.format("{%d, %-5.2f}, ", i, percentRemoved);
	// mStr += s;
	// System.err.println("percent removed: " + percentRemoved);
	//
	// } //end loop i
	//
	// } //end pnrTest
	//
	// else if (source == wireCountTestItem) {
	// for (int j = 30; j < 151; j+=4) {
	//
	// TestParameters.setNumWire(j);
	// double sumtime = 0;
	// for (int i = 0; i < numRun; i++) {
	// double ttime = detectorTest.doTimingTest2(numEvent);
	// if (i > 0) {
	// sumtime += ttime;
	// }
	// }
	// sumtime /= (numRun - 1);
	// System.err.println("NumWire: "
	// + TestParameters.getNumWire() + " AVG TIME: "
	// + sumtime);
	// String s = String.format("{%d, %-5.2f}, ", TestParameters.getNumWire(),
	// sumtime);
	//
	// mStr += s;
	// }
	// } //wire count test item
	//
	// mStr += "}";
	// System.err.println(mStr);
	// detectorTest.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	//
	// } //end action performed
	// }; //end new ActionListener
	//
	//
	// pnrTestItem.addActionListener(al);
	// wireCountTestItem.addActionListener(al);
	// menu.add(pnrTestItem);
	// menu.add(wireCountTestItem);
	// return menu;
	// }
	//

	/**
	 * Create the event menu.
	 * 
	 * @return the event menu.
	 */
	private JMenu createEventMenu() {

		JMenu menu = new JMenu("Event");
		JMenuItem nextItem = new JMenuItem("Next Pretend Event");

		nextItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		nextItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				detectorTest.nextEvent(true);
			}
		});

		menu.add(nextItem);

		JMenuItem sbitem = new JMenuItem("screwball event");
		sbitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				detectorTest.screwballEvent();
				;
			}
		});
		menu.add(sbitem);

		return menu;
	}

	// create the file menu
	private JMenu createFileMenu() {

		JMenu menu = new JMenu("File");
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});

		menu.add(quitItem);
		return menu;
	}

	/**
	 * Main program for testing
	 * 
	 * @param args
	 */
	public static void main(String args[]) {


		final NoiseTest noiseTest = new NoiseTest();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				noiseTest.setVisible(true);
			}
		});
	}
}
