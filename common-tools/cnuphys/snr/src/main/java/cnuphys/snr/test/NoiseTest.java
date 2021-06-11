package cnuphys.snr.test;

import java.awt.BorderLayout;
import java.awt.Color;
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

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.SNRAnalysisLevel;

@SuppressWarnings("serial")
public class NoiseTest extends JFrame {

	private DetectorTest _detectorTest;


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

		setSize(1200, 1100);
		//pack();
		setLocation(200, 80);
	};

	/**
	 * Add the GUI components
	 */
	public void addComponents() {
		double bw = 10.0;
		double xmin = 0.15;
		double width = bw - 2.0 * xmin;
		
		NoiseReductionParameters parameters = NoiseReductionParameters.getDefaultParameters();


		double csize = width / parameters.getNumWire();

		double height = parameters.getNumLayer() * csize;

		double dy = height / 6;

		// space fopr two extra superlayer (left/right composite superlayers)
		double bh = 8.5 * (dy + height) + dy;

		_detectorTest = new DetectorTest(0.0, 0.0, bw, bh);

		for (int i = 0; i < 6; i++) {
			double y = -0.6 + (i + 1.5) * (3.2*dy + height);
			_detectorTest.addChamber(new Rectangle2D.Double(xmin, y, width, height));
		}

		add(_detectorTest, BorderLayout.CENTER);

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
		addClusterHotSpot(menubar);
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
	
	private void addClusterHotSpot(JMenuBar menubar) {
		menubar.add(Box.createHorizontalStrut(40));
		final JLabel left = new JLabel(" Show Clusters ");
		left.setOpaque(true);
		left.setBackground(Color.darkGray);
		left.setForeground(Color.cyan);
		left.setBorder(BorderFactory.createEtchedBorder());
		menubar.add(left);

		MouseAdapter ml = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent me) {
				left.setBackground(Color.darkGray);
				left.setForeground(Color.yellow);
				TestParameters.showClusters = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent me) {
				left.setBackground(Color.darkGray);
				left.setForeground(Color.cyan);
				TestParameters.showClusters = false;
				repaint();
			}

		};

		left.addMouseListener(ml);
	}
	

	/**
	 * Create the option menu.
	 * 
	 * @return the option menu.
	 */
	private JMenu createOptionMenu() {
		JMenu menu = new JMenu("Options");
		menu.add(_detectorTest.getDisplayOptionMenu());
		return menu;
	}

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
				_detectorTest.nextEvent(true);
			}
		});

		menu.add(nextItem);

		JMenuItem sbitem = new JMenuItem("screwball event");
		sbitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				_detectorTest.screwballEvent();
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

		//use 2-stage analysis
		NoiseReductionParameters.setSNRAnalysisLevel(SNRAnalysisLevel.TWOSTAGE);
		
		final NoiseTest noiseTest = new NoiseTest();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				noiseTest.setVisible(true);
			}
		});
		
		
	}
}
