package cnuphys.bCNU.simanneal;

import java.util.EventListener;

public interface IUpdateListener extends EventListener {

	/**
	 * Notifies listeners of a new solution
	 * @param simulationthe simulation
	 * @param newSolution the new solution
	 * @param oldSolution the old solution
	 */
	public void updateSolution(Simulation simulation, Solution newSolution, Solution oldSolution);
}
