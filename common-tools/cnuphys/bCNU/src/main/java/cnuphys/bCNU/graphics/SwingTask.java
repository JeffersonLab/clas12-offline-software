package cnuphys.bCNU.graphics;

import javax.swing.SwingWorker;

public abstract class SwingTask extends SwingWorker<Void, Void> {

	public void setTaskProgress(int val) {
		val = Math.max(0, Math.min(val, 100));
		super.setProgress(val);
	}
}
