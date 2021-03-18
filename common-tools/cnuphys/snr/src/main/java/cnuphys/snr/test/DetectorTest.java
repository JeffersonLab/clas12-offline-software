package cnuphys.snr.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import cnuphys.snr.NoiseReductionParameters;

@SuppressWarnings("serial")
public class DetectorTest extends JPanel implements MouseListener, MouseMotionListener {

	public enum DisplayOption {
		DATA, REALITY, ANALYZED
	}

	private DisplayOption displayOption = DisplayOption.ANALYZED;

	/**
	 * Display the tracks
	 */
	private boolean displayTrack = true;
	
	/** the event number */
	private int _eventNumber = 0;

	/**
	 * A list of simulated tracks.
	 */
	private Vector<TrackTest> tracks = new Vector<TrackTest>(10);

	/**
	 * The current world.
	 */
	private Rectangle2D.Double _world;

	/**
	 * The default world
	 */
	private Rectangle2D.Double _defaultWorld;

	/** The chambers */
	private Vector<ChamberTest> chambers = new Vector<ChamberTest>(10);

	/**
	 * The local system; always the bounds of the last draw.
	 */
	private Rectangle _local;

	/**
	 * The display of the detectors
	 */
	private JComponent _display;

	/**
	 * For a status line feedback string.
	 */
	private JTextArea _status;


	// common font
	private static Font _font = new Font("SanSerif", Font.BOLD, 10);

	/**
	 * Used for creating the feedback label.
	 */
	private StringBuffer stringBuffer = new StringBuffer(512);

	private static long _seed = 364785861L;
	private static Random _rand;


	/**
	 * Create a test detector.
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public DetectorTest(double x, double y, double w, double h) {
		
		_rand = new Random(_seed);

		_world = new Rectangle2D.Double(x, y, w, h);
		_defaultWorld = new Rectangle2D.Double(x, y, w, h);

		_display = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				paintDetector(g);
			}
		};
		_display.addMouseMotionListener(this);
		_display.addMouseListener(this);
		
	//	_display.setPreferredSize(new Dimension(1000, 1000));

		_status = new JTextArea(5, 200);
		_status.setFont(new Font("SanSerif", Font.BOLD, 9));
		_status.setEditable(false);

		setLayout(new BorderLayout(4, 4));
		add(_display, BorderLayout.CENTER);
		add(_status, BorderLayout.SOUTH);
	}

	/**
	 * Get the shared random number generator
	 * @return the shared random number generator
	 */
	public static Random getRandom() {
		return _rand;
	}
	
	// a screwball event
	public void screwballEvent() {
		clearTracks();
		for (ChamberTest ct : chambers) {
			ct.clearHits();

			if (ct.index == 0) {
//				ct.forceHit(1, 11);
//				ct.forceHit(1, 14);
//				ct.forceHit(2, 12);
//				ct.forceHit(2, 14);
//				ct.forceHit(2, 15);
//				ct.forceHit(3, 11);
//				ct.forceHit(3, 14);
//				ct.forceHit(4, 11);
//				ct.forceHit(4, 14);
//				ct.forceHit(5, 10);
//				ct.forceHit(5, 13);
//				ct.forceHit(6, 11);
//				ct.forceHit(6, 13);
			} else if (ct.index == 1) {
//				ct.forceHit(2, 28);
//				ct.forceHit(3, 27);
//				ct.forceHit(4, 27);
//				ct.forceHit(5, 27);
//				ct.forceHit(6, 27);
			} else if (ct.index == 2) {
				ct.forceHit(2, 63);
				ct.forceHit(3, 64);
				ct.forceHit(4, 65);
				ct.forceHit(5, 65);
				ct.forceHit(6, 66);
				
//				ct.forceHit(2, 23);
//				ct.forceHit(3, 22);
//				ct.forceHit(4, 21);
//				ct.forceHit(5, 21);
//				ct.forceHit(6, 20);

			} else if (ct.index == 3) {
//				ct.forceHit(1, 42);
//				ct.forceHit(1, 41);
//				ct.forceHit(2, 41);
//				ct.forceHit(3, 41);
//				ct.forceHit(4, 41);
//				ct.forceHit(4, 40);
//				ct.forceHit(5, 41);
//				ct.forceHit(6, 40);
			} else if (ct.index == 4) {
//				ct.forceHit(1, 21);
//				ct.forceHit(3, 21);
//				ct.forceHit(4, 21);
//				ct.forceHit(5, 21);
//				ct.forceHit(5, 22);
//				ct.forceHit(5, 23);
//				ct.forceHit(6, 21);
//				ct.forceHit(6, 22);
			} else if (ct.index == 5) {
//				ct.forceHit(1, 35);
//				ct.forceHit(2, 34);
//				ct.forceHit(3, 35);
//				ct.forceHit(3, 34);
//				ct.forceHit(4, 34);
//				ct.forceHit(5, 34);
//				ct.forceHit(6, 33);
			}
			ct.loadBitData();
			ct.removeNoise();
			
		}
		
		for (ChamberTest ct : chambers) {
			// graphical mark, not part of algorithm pe se
			ct.markHits();
		}

		repaint();
	}

	/**
	 * Generate the next event.
	 * 
	 * @param repaint if <code>true</code>, repaint after getting next event.
	 */

	public void nextEvent(boolean repaint) {
		_eventNumber++;
		clearTracks();
		generateTracks();

		for (ChamberTest chamberTest : chambers) {
			chamberTest.clearHits();

			chamberTest.generateNoise();
			for (TrackTest tt : tracks) {
				chamberTest.hitsFromTrack(tt);
			}

			chamberTest.loadBitData();
			chamberTest.removeNoise();
		}

		for (ChamberTest ct : chambers) {
			// graphical mark, not part of algorithm pe se
			ct.markHits();
		}

		repaint();
	}

	/**
	 * Clear all the tracks.
	 */
	public void clearTracks() {
		tracks.removeAllElements();
	}

	/**
	 * Generate some random straightline tracks
	 */
	public void generateTracks() {
		// get num tracks

		double rand = _rand.nextDouble();

		int numTrack = 0;
		double probTracks[] = TestParameters.getProbTracks();

		for (int i = probTracks.length - 1; i >= 0; i--) {
			if (rand < probTracks[i]) {
				numTrack = i + 1;
				break;
			}
		}

		for (int i = 0; i < numTrack; i++) {
			Point2D.Double wp0 = new Point2D.Double();
			Point2D.Double wp1 = new Point2D.Double();
			// wp0.y = world.y;
			wp0.y = chambers.get(0).boundary.getMinY();
			wp1.y = _world.y + _world.height;

			wp0.x = _world.x + _world.width / 2.0;
			double dx = 0.5 * _world.width * (1.0 - 2.0 * _rand.nextDouble());
			wp0.x = wp0.x + dx;

			double thetaMax = Math.toRadians(TestParameters.getThetaMax());
			double ang = thetaMax * (1.0 - 2.0 * _rand.nextDouble());
			wp1.x = wp0.x + _world.height * Math.tan(ang);

			tracks.add(new TrackTest(wp0, wp1));
		}
	}

	/**
	 * Paint the detector
	 */
	public void paintDetector(Graphics g) {
		_local = getBounds();

		g.setColor(Color.white);
		g.fillRect(_local.x, _local.y, _local.width, _local.height);

		int numTrackHits = 0;
		int numNoiseHits = 0;
		int numRemovedNoiseHits = 0;
		int numSavedNoiseHits = 0;

		// draw the chambers
		for (ChamberTest ct : chambers) {
			numTrackHits += ct.getNumTrackHits();
			numNoiseHits += ct.getNumNoiseHits();
			numRemovedNoiseHits += ct.getNumRemovedNoiseHits();
			numSavedNoiseHits += ct.getNumSavedNoiseHits();
			ct.draw(g, _world, _local);
		}

		// draw the tracks
		if (displayTrack) {
			for (TrackTest tt : tracks) {
				tt.draw(g, _world, _local);
			}
		}
		
		// draw more on the chambers
		for (ChamberTest ct : chambers) {
			ct.drawAfter(g, _world, _local);
		}

		// draw some text
		g.setColor(Color.black);
		g.setFont(_font);
		FontMetrics fm = this.getFontMetrics(_font);
		String message = TestParameters.paramString();

		String rStr = "  #hits: " + numTrackHits + " #noise: " + numNoiseHits + " #remNoise: " + numRemovedNoiseHits
				+ " #savedNoise: " + numSavedNoiseHits;

		g.drawString("Event: " + _eventNumber + "   " + message + rStr, 6, fm.getHeight() - 2);

	}

	public void restoreDefaultWorld() {
		_world = (Rectangle2D.Double) _defaultWorld.clone();
		_display.repaint();
	}

	/**
	 * @param name     the name of the chamber.
	 * @param boundary the rectangular boundary in world coordinates.
	 */
	public void addChamber(Rectangle2D.Double boundary) {
		int index = chambers.size();
		chambers.add(new ChamberTest(this, index, NoiseReductionParameters.getDefaultParameters(), boundary));
	}


	/**
	 * Update the status based on a pixel point
	 * 
	 * @param pp
	 */
	private void updateStatus(Point pp) {
		Point.Double wp  =  new Point.Double();
		
		TestSupport.toWorld(_world, _local, pp, wp);
		stringBuffer.delete(0, stringBuffer.capacity());

		stringBuffer.append(String.format("local [%d, %d]  world [%5.2f, %5.2f] ", pp.x, pp.y, wp.x, wp.y));
		
		// mouse over chamber
		for (ChamberTest ct : chambers) {
			String s = ct.feedback(pp, _world, _local);
			if (s != null) {
				stringBuffer.append(" " + s);
				break;
			}
		}

		updateStatus(stringBuffer.toString());
	}

	/**
	 * Update the status label with a new string.
	 * 
	 * @param s the new status string.
	 */
	private void updateStatus(String s) {
		_status.setText(s);
	}

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		updateStatus(mouseEvent.getPoint());
	}

	@Override
	public void mouseClicked(MouseEvent mouseEvent) {
		if (mouseEvent.getClickCount() == 2) {
			for (ChamberTest ct : chambers) {
				if (ct.contains(mouseEvent.getPoint(), _world, _local)) {
					System.out.println("Double Click on " + ct.getName());
				}
			}
		}
	}

	/**
	 * The mouse has entered the display area.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseEntered(MouseEvent mouseEvent) {
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	/**
	 * The mouse has exited the display area.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseExited(MouseEvent mouseEvent) {
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mousePressed(MouseEvent mouseEvent) {
	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
		_display.repaint();
	}

	public DisplayOption getDisplayOption() {
		return displayOption;
	}

	public void setDisplayOption(DisplayOption displayOption) {
		this.displayOption = displayOption;
	}

	/**
	 * Get the menu that controls boolean display flags.
	 * 
	 * @return the menu that controls boolean display flags.
	 */
	public JMenu getDisplayOptionMenu() {
		JMenu menu = new JMenu("Display");

		final JCheckBoxMenuItem dataItem = new JCheckBoxMenuItem("Display Data", displayOption == DisplayOption.DATA);
		final JCheckBoxMenuItem realityItem = new JCheckBoxMenuItem("Display Reality",
				displayOption == DisplayOption.REALITY);
		final JCheckBoxMenuItem analyzedItem = new JCheckBoxMenuItem("Display Analysis",
				displayOption == DisplayOption.ANALYZED);

		ButtonGroup bg = new ButtonGroup();
		bg.add(dataItem);
		bg.add(realityItem);
		bg.add(analyzedItem);

		// track display separate
		final JCheckBoxMenuItem trackItem = new JCheckBoxMenuItem("Display Tracks", displayTrack);

		final DisplayOption oldDisplayOption = displayOption;

		ItemListener il = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				Object o = arg0.getSource();
				if (o == dataItem) {
					displayOption = DisplayOption.DATA;
					if (displayOption != oldDisplayOption) {
						_display.repaint();
					}
				} else if (o == realityItem) {
					displayOption = DisplayOption.REALITY;
					if (displayOption != oldDisplayOption) {
						_display.repaint();
					}
				} else if (o == analyzedItem) {
					displayOption = DisplayOption.ANALYZED;
					if (displayOption != oldDisplayOption) {
						_display.repaint();
					}
				}

				else if (o == trackItem) {
					displayTrack = trackItem.getState();
					_display.repaint();
				}

			}

		};

		dataItem.addItemListener(il);
		realityItem.addItemListener(il);
		analyzedItem.addItemListener(il);
		trackItem.addItemListener(il);

		menu.add(dataItem);
		menu.add(realityItem);
		menu.add(analyzedItem);

		menu.addSeparator();
		menu.add(trackItem);

		return menu;
	}

	public boolean isDisplayTrack() {
		return displayTrack;
	}
}
