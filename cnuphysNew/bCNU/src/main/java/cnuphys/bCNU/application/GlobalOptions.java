package cnuphys.bCNU.application;

/**
 * One stop shopping for the some common bCNU level options, such as whether we
 * should display heads up text.
 * 
 * @author heddle
 * 
 */
public class GlobalOptions {

	// show the heads up display?
	private static boolean showHeadsUp = true;

	// display parent child arrows
	private static boolean showParentChildArrows = true;

	/**
	 * Should the headsup mechanism be used?
	 * 
	 * @return <code>true</code> if the heads up should be displayed.
	 */
	public static boolean isHeadsUpVisible() {
		return showHeadsUp;
	}

	/**
	 * Set whether the headsup mechanism should be used.
	 * 
	 * @param showHeadsUp
	 *            the new value of the flag.
	 */
	public static void headsUpSetVisible(boolean showHeadsUp) {
		GlobalOptions.showHeadsUp = showHeadsUp;
	}

	/**
	 * Should the parent child arrows be visible?
	 * 
	 * @return <code>true</code> if the parent child arrows be visible.
	 */
	public static boolean isParentChildArrowsVisible() {
		return showParentChildArrows;
	}

	/**
	 * Set whether the parent child arrows are visible.
	 * 
	 * @param showParentChildArrows
	 *            the new value of the flag.
	 */
	public static void parentChildArrowsSetVisible(boolean showParentChildArrows) {
		GlobalOptions.showParentChildArrows = showParentChildArrows;
	}

}
