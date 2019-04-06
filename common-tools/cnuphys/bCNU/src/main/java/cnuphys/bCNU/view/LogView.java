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
		this(600, 600, false);
	}
	
	public LogView(int width, int height, boolean visible) {
		super(PropertySupport.TITLE, "Log", PropertySupport.ICONIFIABLE, true, PropertySupport.MAXIMIZABLE, true,
				PropertySupport.CLOSABLE, true, PropertySupport.RESIZABLE, true, 
				PropertySupport.WIDTH, width,
				PropertySupport.HEIGHT, height, PropertySupport.VISIBLE, visible);
		add(new SimpleLogPane());
	}
		
}
