package cnuphys.bCNU.graphics.splashscreen;

import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

/**
 * A splashscreen window
 * 
 * @author heddle
 *
 */
@SuppressWarnings("serial")
public class SplashScreen extends Window {

	private List<TransientText> _transientText;

	// when the window was first opened
	private long _startTime = -1;

	// minimum time when a setVisible(false) is effective
	private long _minCloseTime = Long.MAX_VALUE;

	// minimum time in milliseconds that the splash will stay visible.
	private long _minDuration;

	/**
	 * Create a translucent window to use as a splash screen
	 * 
	 * @param imagePath
	 *            the path to the splash screen image
	 * @param opacity
	 *            the smaller the number, the more transparent
	 * @param minDuration
	 *            the minimum duration in milliseconds that you want the splash
	 *            screen to be visible.
	 * @throws HeadlessException
	 *             , FileNotFoundException
	 */
	public SplashScreen(String imagePath, float opacity, long minDuration,
			List<TransientText> transientText) throws HeadlessException,
			FileNotFoundException {
		super(null);
		try {
			Class<?> awtUtilitiesClass = Class
					.forName("com.sun.awt.AWTUtilities");
			Method mSetWindowOpacity = awtUtilitiesClass.getMethod(
					"setWindowOpacity", Window.class, float.class);
			mSetWindowOpacity.invoke(null, this, opacity);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		_minDuration = Math.max(10, Math.min(10000, minDuration));

		_transientText = transientText;
	}

	/**
	 * See if we can close (that the minimum up has passed)
	 * 
	 * @return <code>true</code> if we can close.
	 */
	public boolean canClose() {
		return (System.currentTimeMillis() > _minCloseTime);
	}

	/**
	 * Add transient text to the collection of transient texts.
	 * 
	 * @param transientText
	 *            the transient text to add.
	 */
	public void addTransientText(TransientText transientText) {
		if (_transientText == null) {
			_transientText = new Vector<TransientText>();
		}
		_transientText.add(transientText);
	}

	@Override
	public void paint(Graphics g) {

		// draw image

		// draw transient text
		if (_transientText != null) {
			for (TransientText tt : _transientText) {
				tt.draw(g, _startTime);
			}
		}
	}

	@Override
	public void setVisible(boolean visible) {

		if (visible && (_startTime < 0)) {
			_startTime = System.currentTimeMillis();
			_minCloseTime = _startTime + _minDuration;
		}

		super.setVisible(visible);
	}
}
