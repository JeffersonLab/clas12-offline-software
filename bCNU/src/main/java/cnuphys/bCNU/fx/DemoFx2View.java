package cnuphys.bCNU.fx;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public class DemoFx2View extends FxView {

	public DemoFx2View(String title, int x, int y, int w, int h) {
		super(title, x, y, w, h);
	}

	@Override
	public void start(JFXPanel panel) {
		Group root = new Group();
		Scene scene = new Scene(root, 2000, 2000);
		scene.setFill(Color.WHITE);

		TriangleMesh pyramidMesh = new TriangleMesh();
		pyramidMesh.getTexCoords().addAll(0, 0);
		float h = 200; // Height
		float s = 400; // Side
		pyramidMesh.getPoints().addAll(0, 0, 0, // Point 0 - Top
				0, h, -s / 2, // Point 1 - Front
				-s / 2, h, 0, // Point 2 - Left
				s / 2, h, 0, // Point 3 - Back
				0, h, s / 2 // Point 4 - Right
		);
		pyramidMesh.getFaces().addAll(0, 0, 2, 0, 1, 0, // Front left face
				0, 0, 1, 0, 3, 0, // Front right face
				0, 0, 3, 0, 4, 0, // Back right face
				0, 0, 4, 0, 2, 0, // Back left face
				4, 0, 1, 0, 2, 0, // Bottom rear face
				4, 0, 3, 0, 1, 0 // Bottom front face
		);
		MeshView pyramid = new MeshView(pyramidMesh);
		pyramid.setDrawMode(DrawMode.FILL);
		
		PhongMaterial blueStuff = new PhongMaterial();
		
		blueStuff.setDiffuseColor(Color.RED);
		
//		Color blue = new Color(0, 0, 1, 0.5);
//		blueStuff.setSpecularColor(Color.BLUE);
		
		pyramid.setMaterial(blueStuff);
//		pyramid.setTranslateX(200);
		pyramid.setTranslateY(-200);
//		pyramid.setTranslateZ(200);
		root.getChildren().add(pyramid);

		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setNearClip(0.1);
		camera.setFarClip(10000.0);
		camera.setTranslateZ(-1000.0);
		scene.setCamera(camera);
		panel.setScene(scene);
	}

}
