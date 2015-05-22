package cnuphys.ced.event;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import cnuphys.ced.event.data.ADataContainer;

@SuppressWarnings("serial")
public class FeedbackRect extends Rectangle {

    /** the Data container associated with this feedback rect */
    public ADataContainer dataContainer;

    /** the hit index associated with this feedback rect */
    public int hitIndex;

    /** a user option */
    public int option;

    /** optional cached feedback (mouse over) string */
    public String[] feedbackString;

    /**
     * @param x
     *            left
     * @param y
     *            top
     * @param w
     *            width
     * @param h
     *            height
     * @param index
     *            the hit index associated with this feedback rect
     * @param data
     *            the Data container associated with this feedback rect
     * @param opt
     *            a user option
     * @param fbString
     *            optional cached feedback strings
     */
    public FeedbackRect(int x, int y, int w, int h, int index,
	    ADataContainer data, int opt, String... fbString) {
	super(x, y, w, h);
	hitIndex = index;
	dataContainer = data;
	option = opt;
	feedbackString = fbString;
    }

    /**
     * @param x
     *            left
     * @param y
     *            top
     * @param w
     *            width
     * @param h
     *            height
     * @param index
     *            the hit index associated with this feedback rect
     * @param data
     *            the Data container associated with this feedback rect
     * @param opt
     *            a user option
     * @param fbString
     *            optional cached feedback strings
     */
    public FeedbackRect(int x, int y, int w, int h, int index,
	    ADataContainer data, int opt, List<String> fbString) {
	super(x, y, w, h);
	hitIndex = index;
	dataContainer = data;
	option = opt;

	if (fbString != null) {
	    int size = fbString.size();
	    if (size > 0) {
		feedbackString = new String[size];
		fbString.toArray(feedbackString);
	    }
	}
    }

    /**
     * Convenience function that both checks for inside and also, if it is
     * inside, adds the cached feedback String (if there is one.)
     * 
     * @param pp
     *            the screen location
     * @param feedbackStrings
     *            the collection of feedback strings
     * @return <code>true</code> if the point is inside
     */
    public boolean contains(Point pp, List<String> feedbackStrings) {
	boolean inside = super.contains(pp);
	if (inside && (feedbackString != null)) {
	    if (feedbackString != null) {
		for (String s : feedbackString) {
		    feedbackStrings.add(s);
		}
	    }
	}
	return inside;
    }

}
