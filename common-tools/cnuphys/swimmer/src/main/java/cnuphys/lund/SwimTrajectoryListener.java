package cnuphys.lund;

import java.util.EventListener;

public interface SwimTrajectoryListener extends EventListener {

	/**
	 * Swam a particle
	 */
	public void trajectoriesChanged();
}
