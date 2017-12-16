package cnuphys.bCNU.simanneal;

import java.util.EventListener;

public interface IUpdateListener extends EventListener {

	public void updateSolution(Solution newSolution, Solution oldSolution, double temperature);
}
