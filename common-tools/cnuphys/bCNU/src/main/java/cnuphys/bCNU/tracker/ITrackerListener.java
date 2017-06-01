/**
 * 
 */
package cnuphys.bCNU.tracker;

import java.util.EventListener;

/**
 * @author heddle
 * 
 */
public interface ITrackerListener extends EventListener {

	public void trackerEvent(TrackerEvent te);

}
