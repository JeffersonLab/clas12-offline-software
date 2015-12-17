package cnuphys.bCNU.view;

import cnuphys.bCNU.log.SimpleLogPane;
import cnuphys.bCNU.util.PropertySupport;

/**
 * This is a predefined view used to display all the log messages.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class LogView extends BaseView {

	public LogView() {
		super(PropertySupport.TITLE, "Log", PropertySupport.ICONIFIABLE, true,
				PropertySupport.MAXIMIZABLE, true, PropertySupport.CLOSABLE, true,
				PropertySupport.RESIZABLE, true, PropertySupport.WIDTH, 600,
				PropertySupport.HEIGHT, 600, PropertySupport.VISIBLE, false);
		add(new SimpleLogPane());
	}
}
