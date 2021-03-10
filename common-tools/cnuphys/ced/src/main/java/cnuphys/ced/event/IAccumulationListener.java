package cnuphys.ced.event;

import java.util.EventListener;

public interface IAccumulationListener extends EventListener {

	/**
	 * An accumulation event has occurred
	 * 
	 * @param reason the reason
	 */
	public void accumulationEvent(int reason);

}
