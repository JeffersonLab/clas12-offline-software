package cnuphys.bCNU.view;

import java.util.EventListener;

public interface IViewListener extends EventListener {

	public void viewAdded(BaseView view);

	public void viewRemoved(BaseView view);
}
