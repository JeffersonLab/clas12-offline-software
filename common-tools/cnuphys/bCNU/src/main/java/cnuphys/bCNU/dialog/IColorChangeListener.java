package cnuphys.bCNU.dialog;

import java.awt.Color;
import java.awt.Component;

/**
 * @author heddle An interface for objects that want to listen for a color
 *         change.
 */
public interface IColorChangeListener {

	public void colorChanged(Component component, Color color);
}
