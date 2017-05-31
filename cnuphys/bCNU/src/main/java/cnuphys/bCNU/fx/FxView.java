package cnuphys.bCNU.fx;


import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

/**
 * This is a view whose component is a JFXPanel
 * @author heddle
 *
 */
public abstract class FxView extends BaseView {
	protected final JFXPanel jfxPanel;

	public FxView(String title, int x, int y, int w, int h) {
		super(PropertySupport.TITLE, title, PropertySupport.ICONIFIABLE, true,
				PropertySupport.MAXIMIZABLE, true, PropertySupport.CLOSABLE, true,
				PropertySupport.RESIZABLE, true, PropertySupport.LEFT, x,
				PropertySupport.TOP, y, PropertySupport.WIDTH, w,
				PropertySupport.HEIGHT, h, PropertySupport.VISIBLE, false);
		jfxPanel = new JFXPanel();
		add(jfxPanel);
		
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	start(jfxPanel);
            }
        });

	}
	
	public abstract void start(JFXPanel panel);
}
