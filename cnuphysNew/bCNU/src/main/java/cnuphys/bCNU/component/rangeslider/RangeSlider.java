package cnuphys.bCNU.component.rangeslider;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.TextUtilities;

@SuppressWarnings("serial")
public class RangeSlider extends JComponent implements MouseMotionListener,
		MouseListener {

	public static final IValueLabeler defaultLabeler = new IValueLabeler() {

		@Override
		public String valueString(long value) {
			return "" + value;
		}

	};

	private static Rectangle nullRectangle = new Rectangle(-999999, -999999, 0,
			0);

	// Listener list for slider updates.
	protected EventListenerList _listenerList;

	// default font used for the labels
	protected Font _font = Fonts.commonFont(Font.PLAIN, 9);

	// creates labels from values
	IValueLabeler _valueLabeler;

	// the full range of the slider
	protected Range _fullRange;

	// the present range of the slider
	protected Range _currentRange;

	// vgap is between the current text and the north/south border
	protected static final int VGAP = 8;

	// hgap is between the edge of the mn or max text and the west/east border
	protected static final int HGAP = 4;

	// offset to the start of trench
	protected int delMin;

	// offset from end of trench
	protected int delMax;

	// trench width
	protected int trenchWidth;

	// thread used to run animation
	Thread runThread;

	// used for animation
	protected boolean running = false;
	protected long runTime = -Long.MAX_VALUE;
	protected Color runColor = Color.red;
	protected int trimLen = 0;

	// flag icon for min current value
	protected static ImageIcon minImage = ImageManager.getInstance()
			.loadImageIcon("images/rightflag.png");

	// flag icon for max current value
	protected static ImageIcon maxImage = ImageManager.getInstance()
			.loadImageIcon("images/leftflag.png");

	// protected static ImageIcon minImage = ImageManager.getInstance()
	// .loadImageIcon("images/upflag.png");
	//
	// // flag icon for max current value
	// protected static ImageIcon maxImage = ImageManager.getInstance()
	// .loadImageIcon("images/downflag.png");
	//
	// the length of the pin part of a thumb
	protected static final int PINHEIGHT = (minImage != null) ? minImage
			.getIconHeight() : 0;

	// extent above/below trench
	protected static final int PINEXTRA = 2;

	// color for current values
	protected static final Color currentColor = Color.red;

	private static int _errorCount = 0;

	// rectangle to detect click in min thumb;
	protected Rectangle minThumbRectangle = new Rectangle();

	// rectangle to detect click in max thumb;
	protected Rectangle maxThumbRectangle = new Rectangle();

	// dragging bounds for minThumb
	protected Rectangle minThumbDragBounds = new Rectangle();

	// dragging bounds for maxThumb
	protected Rectangle maxThumbDragBounds = new Rectangle();

	// for dragging the run time
	protected Rectangle runTimeRectangle = new Rectangle();

	// runtime limit rect
	protected Rectangle runTimeDragBounds = new Rectangle();

	// drawing tun time text opaque
	protected Rectangle runTimeBackgroundRectangle = new Rectangle();

	// range between min and max
	protected Rectangle rangeBounds = new Rectangle();

	// pressed mouse in min thumb?
	protected boolean inMinThumb;

	// pressed mouse in max thumb?
	protected boolean inMaxThumb;

	// pressed in run time (red line)
	protected boolean inRunTime;

	// mouse x at start of drag
	protected int startX = -Integer.MAX_VALUE;

	// reference x at start of drag;
	protected int refStartX = 0;

	protected static final Color minThumbFill = new Color(0, 0, 255, 16);
	protected static final Color maxThumbFill = new Color(255, 0, 0, 16);
	protected static final Color rangeColor = new Color(255, 0, 0, 64);
	protected static final Color runTimeFill = new Color(255, 255, 255, 64);

	/**
	 * Create a Range Slider with two thumbs
	 * 
	 * @param fullRange
	 *            the full range of the slider.
	 * @param valueLabeler
	 *            knows the appropriate way to draw labels for the values. For
	 *            example, if the values represent unix times, the labeler might
	 *            convert unix times to readable strings.
	 */
	public RangeSlider(Range fullRange, IValueLabeler valueLabeler) {
		_fullRange = fullRange;
		_currentRange = new Range(_fullRange);
		setValueLabeler(valueLabeler);
		setBackground(Environment.getInstance()
				.getDefaultPanelBackgroundColor());
		setOpaque(true);
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.width = Math.max(200, d.width);
		d.height = getPreferredHeight();
		return d;
	}

	/**
	 * Paint the component
	 * 
	 * @param g
	 *            the graphics context
	 */
	@Override
	public void paintComponent(Graphics g) {
		// super.paintComponent(g);

		Rectangle b = getBounds();
		if ((b == null) || (b.width < 1) || (b.height < 1)) {
			return;
		}

		if (minImage == null) {
			if (_errorCount < 10) {
				System.err.println("Missing images for range slider");
			}
			_errorCount++;
			return;
		}

		FontMetrics fm = getFontMetrics(_font);

		g.setFont(_font);
		g.setColor(getBackground());
		g.fillRect(0, 0, b.width, b.height);

		// get the y mid point
		int ymid = b.height / 2;

		// get the full range strings and their widths
		String fullRangeMinString = _valueLabeler.valueString(_fullRange
				.getMinValue());
		String fullRangeMaxString = _valueLabeler.valueString(_fullRange
				.getMaxValue());
		int fullRangeMinStrW = fm.stringWidth(fullRangeMinString);
		int fullRangeMaxStrW = fm.stringWidth(fullRangeMaxString);

		// get the current range strings and their widths
		String currentRangeMinString = _valueLabeler.valueString(_currentRange
				.getMinValue());
		String currentRangeMaxString = _valueLabeler.valueString(_currentRange
				.getMaxValue());
		int currentRangeMinStrW = fm.stringWidth(currentRangeMinString);
		int currentRangeMaxStrW = fm.stringWidth(currentRangeMaxString);

		// get the dels from the west and east sides
		delMin = HGAP + fullRangeMinStrW / 2;
		delMax = HGAP + fullRangeMaxStrW / 2;

		// get the trench width
		trenchWidth = b.width - delMin - delMax;
		if (trenchWidth < 10) {
			if (_errorCount < 10) {
				System.err.println("Range slider too narrow to draw");
			}
			_errorCount++;
			return;
		}

		// draw the pins
		int ih = minImage.getIconHeight();
		int iw = minImage.getIconWidth();

		// curMin and curMax are pixel values
		int curMin = getX(_currentRange.getMinValue());
		int curMax = getX(_currentRange.getMaxValue());

		minThumbRectangle.setBounds(curMin, ymid - ih + PINEXTRA + 2, iw, ih);
		minThumbDragBounds.setBounds(delMin, minThumbRectangle.y, curMax
				- delMin + 1, ih);

		// maxThumbRectangle.setBounds(curMax - iw + 1, ymid - PINEXTRA, iw,
		// ih);
		maxThumbRectangle.setBounds(curMax - iw + 1, ymid - ih + PINEXTRA + 2,
				iw, ih);
		maxThumbDragBounds.setBounds(curMin + 1, maxThumbRectangle.y, delMin
				+ trenchWidth - curMin - 1, ih);

		drawImage(g, minImage, minThumbRectangle);
		drawImage(g, maxImage, maxThumbRectangle);

		rangeBounds.setBounds(curMin, ymid - 4, curMax - curMin, 9);
		fillRect(g, rangeColor, rangeBounds);
		GraphicsUtilities.drawSimple3DRect(g, rangeBounds.x, rangeBounds.y,
				rangeBounds.width, rangeBounds.height, false);

		// draw a line indicating runTime (animation) if it is in range
		if (inCurrentRange(runTime)) {
			int rtx = getX(runTime);

			String rtString = _valueLabeler.valueString(runTime);

			if (trimLen > 0) {
				rtString = rtString.substring(trimLen);
			}

			int runTimeStrW = fm.stringWidth(rtString);

			// long left = runTime - _currentRange.getMinValue();
			// long right = _currentRange.getMaxValue() - runTime;

			g.setFont(_font);
			int xx = rtx - runTimeStrW / 2;

			// if (left < right) {
			// int yy = ymid - 5;
			// runTimeBackgroundRectangle.setBounds(xx, yy-fm.getAscent(),
			// runTimeStrW, fm.getAscent());
			// fillRect(g,
			// Environment.getInstance().getDefaultPanelBackgroundColor(),
			// runTimeBackgroundRectangle);
			// g.setColor(Color.blue);
			// g.drawString(rtString, xx, yy);
			// }
			// else {
			// int yy = ymid + 3 + fm.getHeight();
			// runTimeBackgroundRectangle.setBounds(xx, yy-fm.getAscent(),
			// runTimeStrW, fm.getAscent());
			// fillRect(g,
			// Environment.getInstance().getDefaultPanelBackgroundColor(),
			// runTimeBackgroundRectangle);
			// g.setColor(Color.blue);
			// g.drawString(rtString, xx, yy);
			// }
			int yy = ymid - 5;
			runTimeBackgroundRectangle.setBounds(xx, yy - fm.getAscent(),
					runTimeStrW, fm.getAscent());
			fillRect(g, Environment.getInstance()
					.getDefaultPanelBackgroundColor(),
					runTimeBackgroundRectangle);
			g.setColor(Color.blue);
			g.drawString(rtString, xx, yy);

			// we can drag the red line if we are not running
			if (running) {
				runTimeRectangle.setBounds(nullRectangle);
				runTimeDragBounds.setBounds(nullRectangle);
			} else {
				runTimeRectangle.setBounds(rtx - 4, ymid - 8, 8, 16);
				runTimeDragBounds.setBounds(curMin, ymid - 8, curMax - curMin,
						16);

				fillRect(g, runTimeFill, runTimeRectangle);
			}

			// draw the line last
			g.setColor(runColor);
			g.drawLine(rtx, ymid - 6, rtx, ymid + 7);

		} else { // red line out of bounds
			runTimeRectangle.setBounds(nullRectangle);
			runTimeDragBounds.setBounds(nullRectangle);
		}

		// draw the current range strings
		g.setColor(currentColor);
		g.drawString(currentRangeMinString, curMin - currentRangeMinStrW / 2,
				ymid - ih + 2);
		// g.drawString(currentRangeMaxString, curMax - currentRangeMaxStrW / 2,
		// ymid + ih + fm.getHeight());
		g.drawString(currentRangeMaxString, curMax - currentRangeMaxStrW / 2,
				ymid - ih + 2);

		// draw the trench;
		g.setColor(Color.white);
		g.drawLine(delMin, ymid + 1, delMin + trenchWidth, ymid + 1);
		g.setColor(Color.black);
		g.drawLine(delMin, ymid, delMin + trenchWidth, ymid);

		// draw full range strings
		TextUtilities.drawGhostText(g, fullRangeMinString, delMin
				- fullRangeMinStrW / 2, ymid + 1 + PINEXTRA + fm.getHeight());
		// TextUtilities.drawGhostText(g, fullRangeMaxString, delMin +
		// trenchWidth
		// - fullRangeMaxStrW / 2 -1, ymid - 3 - PINEXTRA);
		TextUtilities.drawGhostText(g, fullRangeMaxString, delMin + trenchWidth
				- fullRangeMaxStrW / 2 - 1,
				ymid + 1 + PINEXTRA + fm.getHeight());
	}

	// convenience method for filling a rectangle
	private void fillRect(Graphics g, Color c, Rectangle r) {
		g.setColor(c);
		g.fillRect(r.x, r.y, r.width, r.height);
	}

	// convenience method for drawing an image
	private void drawImage(Graphics g, ImageIcon icon, Rectangle r) {
		g.drawImage(icon.getImage(), r.x, r.y, this);
	}

	// get the x pixel location give a value
	private int getX(long val) {
		double dw = trenchWidth;
		double rangeDel = _fullRange.getMaxValue() - _fullRange.getMinValue();
		double fract = (val - _fullRange.getMinValue()) / rangeDel;
		int pval = delMin + (int) (dw * fract);
		pval = Math.max(pval, delMin);
		pval = Math.min(pval, delMin + trenchWidth);
		return pval;
	}

	// get the value location given a pixel value
	protected long getValue(int x) {
		double dx = x - delMin;
		double dw = trenchWidth;
		double rangeDel = _fullRange.getMaxValue() - _fullRange.getMinValue();
		long val = _fullRange.getMinValue() + (long) (dx * rangeDel / dw);
		val = Math.max(val, _fullRange.getMinValue());
		val = Math.min(val, _fullRange.getMaxValue());

		return val;
	}

	/**
	 * Get the height needed for all the stuff;
	 * 
	 * @return
	 */
	protected int getPreferredHeight() {
		FontMetrics fm = getFontMetrics(_font);
		return -PINHEIGHT - 3 + 2
				* (1 + (PINHEIGHT - PINEXTRA) + VGAP + 1 + fm.getHeight());
	}

	@Override
	public Font getFont() {
		return _font;
	}

	@Override
	public void setFont(Font font) {
		_font = font;
	}

	public Range getFullRange() {
		return _fullRange;
	}

	public Range getCurrentRange() {
		return _currentRange;
	}

	/**
	 * Set the current range of the slider
	 * 
	 * @param currentRange
	 */
	public void setCurrentRange(Range currentRange) {
		_currentRange = currentRange;
	}

	/**
	 * Remove a range slider listener.
	 * 
	 * @param listener
	 *            the range slider listener to remove.
	 */
	public void removeRangeSliderListener(IRangeSliderListener listener) {

		if ((listener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(IRangeSliderListener.class, listener);
	}

	/**
	 * Add a range slider listener.
	 * 
	 * @param listener
	 *            the range slider listener to add.
	 */
	public void addRangeSliderListener(IRangeSliderListener listener) {

		if (listener == null) {
			return;
		}

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		_listenerList.add(IRangeSliderListener.class, listener);
	}

	/**
	 * Notify listeners that a range slider (namely "this") was updated.
	 */
	protected void notifyListeners() {
		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IRangeSliderListener.class) {
				((IRangeSliderListener) listeners[i + 1])
						.rangeSliderChanged(this);
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	// reset the drag parameters
	private void resetDrag() {
		inMinThumb = false;
		inMaxThumb = false;
		inRunTime = false;
		startX = -Integer.MAX_VALUE;
		refStartX = 0;
	}

	@Override
	public void mousePressed(MouseEvent me) {
		// check the one with the biggest limit rectangle first
		resetDrag();

		Point pp = me.getPoint();

		// check the redline first
		if (!running) {
			inRunTime = runTimeRectangle.contains(pp);
			if (inRunTime) {
				startX = pp.x;
				refStartX = getX(runTime);
				return;
			}
		}

		if (minThumbDragBounds.width > maxThumbDragBounds.width) {
			inMinThumb = minThumbRectangle.contains(pp);
			if (!inMinThumb) {
				inMaxThumb = maxThumbRectangle.contains(pp);
			}
		} else {
			inMaxThumb = maxThumbRectangle.contains(pp);
			if (!inMaxThumb) {
				inMinThumb = minThumbRectangle.contains(pp);
			}
		}

		if (inMinThumb) {
			startX = pp.x;
			refStartX = minThumbRectangle.x;
		} else if (inMaxThumb) {
			startX = pp.x;
			refStartX = maxThumbRectangle.x + maxThumbRectangle.width;
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (inRunTime & !running) {
			runtimeAdjusted(false);
		}

		resetDrag();
	}

	@Override
	public void mouseDragged(MouseEvent me) {

		Point pp = me.getPoint();

		if (inMinThumb) {
			int newX = (pp.x - startX) + refStartX;
			newX = inBounds(newX, minThumbDragBounds);
			long newVal = getValue(newX);
			if (newVal != _currentRange.getMinValue()) {
				_currentRange.setMinValue(newVal);
				repaint();
				notifyListeners();
			}
		} else if (inMaxThumb) {
			int newX = refStartX - (startX - pp.x);
			newX = inBounds(newX, maxThumbDragBounds);
			long newVal = getValue(newX);
			if (newVal != _currentRange.getMaxValue()) {
				_currentRange.setMaxValue(newVal);
				repaint();
				notifyListeners();
			}
		} else if (inRunTime && !running) {
			int newX = (pp.x - startX) + refStartX;
			newX = inBounds(newX, runTimeDragBounds);
			runTime = getValue(newX);
			runTime = Math.max(_currentRange.getMinValue(),
					Math.min(_currentRange.getMaxValue(), runTime));
			repaint();
			runtimeAdjusted(true);
		}
	}

	// default implementation does nothing
	protected void runtimeAdjusted(boolean stillDragging) {

	}

	private int inBounds(int x, Rectangle r) {
		return Math.max(r.x, Math.min(r.x + r.width, x));
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

	/**
	 * @return the valueLabeler
	 */
	public IValueLabeler getValueLabeler() {
		return _valueLabeler;
	}

	/**
	 * @param valueLabeler
	 *            the valueLabeler to set
	 */
	public void setValueLabeler(IValueLabeler valueLabeler) {
		_valueLabeler = valueLabeler;
		if (_valueLabeler == null) {
			_valueLabeler = defaultLabeler;
		}
	}

	/**
	 * Reset the current range to the full extent.
	 */
	public void reset() {
		pause();
		_currentRange.setMinValue(_fullRange.getMinValue());
		_currentRange.setMaxValue(_fullRange.getMaxValue());
		runTime = -Long.MAX_VALUE;
		repaint();
	}

	/**
	 * Check whether the given value is in the current range. It is an inclusive
	 * check.
	 * 
	 * @param val
	 *            the value to test.
	 * @return <code>true</code> if the value is equal to a limit or between the
	 *         limits.
	 */
	public boolean inCurrentRange(long val) {
		return _currentRange.inRange(val);
	}

	/**
	 * Check whether the given value is in the full range. It is an inclusive
	 * check.
	 * 
	 * @param val
	 *            the value to test.
	 * @return <code>true</code> if the value is equal to a limit or between the
	 *         limits.
	 */
	public boolean inFullRange(long val) {
		return _fullRange.inRange(val);
	}

	/**
	 * Set a new full range. Will set the current range to be the full extent.
	 * 
	 * @param fullRange
	 *            the new full range
	 */
	public void setFullRange(Range fullRange) {
		_fullRange = new Range(fullRange);

		reset();
	}

	/**
	 * Called when the animation run button is selected
	 * 
	 * @param del
	 *            the increment of the runTime value. It will be incremented
	 *            from the current range min time to the max time in steps of
	 *            del.
	 * @param nonAWTRunnable
	 *            will be called first in each step and executed in a non-AWT
	 *            thread.
	 * @param awtRunnable
	 *            will be placed on the AWT thread when the non AWT runnable
	 *            finishes
	 */
	public void run(final long del, final Runnable nonAWTRunnable,
			final Runnable awtRunnable) {
		if (running) {
			System.err.println("Already running. That's rarely a good sign.");
		}

		if ((runTime < _currentRange.getMinValue() || (runTime >= _currentRange
				.getMaxValue()))) {
			runTime = _currentRange.getMinValue();
		}

		running = true;

		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				boolean oneLastTime = false;

				while (running) {

					// do the non awt runnable
					if (nonAWTRunnable != null) {
						nonAWTRunnable.run();
					}

					if (!oneLastTime) {
						try {
							Thread.sleep(30);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					// are we still running? If so schedule the awtRunnable
					// which should update
					if (running) {
						if (awtRunnable != null) {
							try {
								SwingUtilities.invokeAndWait(awtRunnable);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
						}
						if (!oneLastTime) {
							runTime += del;
							if (runTime >= _currentRange.getMaxValue()) {
								runTime = _currentRange.getMaxValue();
								oneLastTime = true;
							}
						} else {
							running = false;
						}
					}
					repaint();
				} // while running
				repaint();

				System.err.println("Animation thread ended successfully.");
			} // end of run
		};

		runThread = new Thread(runnable);
		runThread.start();
	}

	public void pause() {
		running = false;
		try {
			if (runThread != null) {
				runThread.join(2000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		runThread = null;
	}

	public void rewind() {
		pause();
		runTime = -Long.MAX_VALUE;
		repaint();
	}

	/**
	 * Main program for testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final JFrame testFrame = new JFrame();

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				System.exit(1);
			}
		};

		testFrame.addWindowListener(windowAdapter);

		testFrame.setLayout(new BorderLayout());

		Range fullRange = new Range(0, 2000);
		IValueLabeler ivl = new IValueLabeler() {

			@Override
			public String valueString(long value) {
				return "" + value;
			}

		};

		RangeSlider rs = new RangeSlider(fullRange, ivl);
		testFrame.add(rs, BorderLayout.NORTH);

		testFrame.setSize(600, 600);
		testFrame.pack();

		testFrame.setVisible(true);
	}

	/**
	 * Get the run time for an animation.
	 * 
	 * @return the run time for the animation.
	 */
	public long getRunTime() {
		return runTime;
	}

	/**
	 * Check whether we are running
	 * 
	 * @return the running flag for the animation
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Check whether we are at full range
	 * 
	 * @return <code>true</code> if we are at full range.
	 */
	public boolean atFullRange() {
		return _fullRange.equals(_currentRange);
	}

}
