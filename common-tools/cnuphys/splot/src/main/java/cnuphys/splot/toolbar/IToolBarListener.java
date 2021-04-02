package cnuphys.splot.toolbar;

import java.util.EventListener;

public interface IToolBarListener extends EventListener {

	public void buttonPressed(CommonToolBar toolbar, ToolBarButton button);

	public void toggleButtonActivated(CommonToolBar toolbar, ToolBarToggleButton button);
}
