package cnuphys.bCNU.view;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

/**
 * A very simple utility class to cache data for later feedback
 * @author davidheddle
 *
 */
public class FBData {
	private String[] _text;
	private Rectangle _rect;
	
	public FBData(Point pp, String... text) {
		_text = text;
		_rect = new Rectangle(pp.x - 4, pp.y - 4, 8, 8);
	}

	public boolean addFeedback(Point screenPoint, List<String> feedbackStrings) {
		if (_rect.contains(screenPoint)) {
			for (String s : _text) {
				feedbackStrings.add(s);
			}
		}
		return false;
	}
}
