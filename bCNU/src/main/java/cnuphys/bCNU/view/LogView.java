package cnuphys.bCNU.view;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.log.SimpleLogPane;

/**
 * This is a predefined view used to display all the log messages.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class LogView extends BaseView {

	// reserved view type for log view
	public static final int LOGVIEWTYPE = -77001;

	public LogView() {
		super(AttributeType.TITLE, "Log", AttributeType.ICONIFIABLE, true,
				AttributeType.MAXIMIZABLE, true, AttributeType.CLOSABLE, true,
				AttributeType.RESIZABLE, true, AttributeType.WIDTH, 600,
				AttributeType.HEIGHT, 600, AttributeType.VISIBLE, false,
				AttributeType.VIEWTYPE, LOGVIEWTYPE);
		add(new SimpleLogPane());
	}
}
