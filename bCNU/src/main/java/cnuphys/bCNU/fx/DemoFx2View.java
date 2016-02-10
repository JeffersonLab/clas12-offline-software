package cnuphys.bCNU.fx;

import java.util.Vector;

import org.fxyz.cameras.CameraTransformer;
import org.fxyz.geometry.Point3D;
import org.fxyz.shapes.composites.PolyLine3D;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class DemoFx2View extends FxView {

	//Camera related
	   private  CameraTransformer cameraTransform = new CameraTransformer();
	   private  Camera camera;

	public DemoFx2View(String title, int x, int y, int w, int h) {
		super(title, x, y, w, h);
	}
	
	@Override
	public void start(JFXPanel panel) {
		Group root = new Group();
        Scene scene = new Scene(root, 2000, 2000);
        scene.setFill(Color.BLACK);
        
//        PolyLine3D p3D = createPolyLine(10, 100, Color.AQUA);
//        root.getChildren().add(p3D);
        
        root.getChildren().add(buildAxes());

        addCamera(scene, -50, -50, -500);
         panel.setScene(scene);
	}
	
	private void addCamera(Scene scene, double xo, double yo, double zo) {
		camera = new PerspectiveCamera(true);

		// setup camera transform for rotational support
		cameraTransform.setTranslate(0, 0, 0);
		cameraTransform.getChildren().add(camera);
		camera.setNearClip(0.001);
		camera.setFarClip(10000.0);
		camera.setTranslateX(xo);
		camera.setTranslateY(yo);
		camera.setTranslateZ(zo);
		// cameraTransform.ry.setAngle(-45.0);
		// cameraTransform.rx.setAngle(-10.0);
		// add a Point Light for better viewing of the grid coordinate system
		// PointLight light = new PointLight(Color.WHITE);
		// cameraTransform.getChildren().add(light);
		// light.setTranslateX(camera.getTranslateX());
		// light.setTranslateY(camera.getTranslateY());
		// light.setTranslateZ(camera.getTranslateZ());
		scene.setCamera(camera);
	}
	
	private PolyLine3D createPolyLine(int n, int width, Color color) {
		Point3D v;
		Vector<Point3D> points;
		
		float scale = 800;
		float s2 = scale/2;
		
		points = new Vector<Point3D>(n);
		for (int i = 0; i < n; i++) {
			
			float x = s2-(float) (scale*Math.random());
			float y = s2-(float) (scale*Math.random());
			float z = s2-(float) (scale*Math.random());
			Point3D p3d = new Point3D(x, y, z);
			points.add(p3d);
		}
		
		return new PolyLine3D(points, width, color);
	}
	
	private Group buildAxes() {
		final Group axisGroup = new Group();
        System.out.println("buildAxes()");
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
 
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
 
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.WHITE);
        blueMaterial.setSpecularColor(Color.WHITE);
 
        final Box xAxis = new Box(1000.0, 5, 5);
        final Box yAxis = new Box(5, 1000.0, 5);
        final Box zAxis = new Box(5, 5, 1000.0);
        
        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
 
        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        return axisGroup;
     }

}
