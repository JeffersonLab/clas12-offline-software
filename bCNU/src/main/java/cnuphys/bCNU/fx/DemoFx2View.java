package cnuphys.bCNU.fx;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

public class DemoFx2View extends FxView {

	public DemoFx2View(String title, int x, int y, int w, int h) {
		super(title, x, y, w, h);
	}
	
	@Override
	public void start(JFXPanel panel) {
		Group root = new Group();
        Scene scene = new Scene(root, 2000, 2000);
        scene.setFill(Color.BLACK);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1000.0);
        scene.setCamera(camera);
        panel.setScene(scene);
	}

}
