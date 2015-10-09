/**
 * 
 */
package cnuphys.bCNU.tracker;

/**
 * @author heddle This is sent to listeners on a tracker
 */

public class TrackerEvent {

	public static enum TrackerEventType {
		RUNCOMPLETED, ALLRUNSCOMPLETED
	}

	private Runnable runnable;
	private TrackerEventType type;

	public TrackerEvent(TrackerEventType type, Runnable runnable) {
		this.type = type;
		this.runnable = runnable;
	}

	public Runnable getRunnable() {
		return runnable;
	}

	public TrackerEventType getType() {
		return type;
	}
}
