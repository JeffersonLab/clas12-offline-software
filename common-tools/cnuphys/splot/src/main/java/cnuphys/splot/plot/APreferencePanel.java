package cnuphys.splot.plot;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;

/**
 * A base class for panels that will be used as tabs on the Preferences dialog
 * 
 * @author heddle
 *
 */
public abstract class APreferencePanel extends JPanel implements KeyListener {

	// the plot canvas
	protected PlotCanvas _canvas;

	/**
	 * Create a panel for use in editing plot preferences
	 * 
	 * @param canvas  the plot canvas
	 * @param tabname the name that will appear on the editor tab
	 */
	public APreferencePanel(PlotCanvas canvas, String tabname) {
		_canvas = canvas;
		setName(tabname);
	}

	// get a titled panel
	protected JPanel titledPanel(String title) {
		JPanel pan = new JPanel();
		pan.setBorder(new CommonBorder(title));
		Environment.getInstance().commonize(pan, null);
		return pan;
	}

	/**
	 * Default implementation is to do nothing
	 * 
	 * @param kev the Key Event
	 */
	@Override
	public void keyTyped(KeyEvent kev) {
	}

	/**
	 * Default implementation is to do nothing
	 * 
	 * @param kev the Key Event
	 */
	@Override
	public void keyPressed(KeyEvent kev) {
	}

	/**
	 * Default implementation is call apply() if the hey was "Enter"
	 * 
	 * @param kev the Key Event
	 */
	@Override
	public void keyReleased(KeyEvent kev) {
		if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
			apply();
		}
	}

	/**
	 * Apply all the changes
	 */
	public abstract void apply();
}
