package cnuphys.ced.event;

import java.util.EventListener;

public interface IAccumulationListener extends EventListener {

	public void accumulationEvent(int reason);

}
