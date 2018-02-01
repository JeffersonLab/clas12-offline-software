package cnuphys.bCNU.simanneal;

import java.util.EventListener;

public interface IUpdateListener extends EventListener {

	public void updateSolution(Simulation simulation, Solution newSolution, Solution oldSolution);
}
