package cnuphys.bCNU.fx;

import javafx.geometry.Point3D;
import javafx.scene.shape.Box;

public class Line3D extends Box {

	public Line3D(Point3D p1, Point3D p2, double lineWidth) {
		super(p1.distance(p2), lineWidth, lineWidth);
		
		
		Point3D mid = p1.midpoint(p2);
		double dX = p2.getX() - mid.getX();
		double dY = p2.getY() - mid.getY();
		double dZ = p2.getZ() - mid.getZ();
		
		double phi = Math.atan2(dY, dX);
		double r = Math.sqrt(dX*dX + dY*dY + dZ*dZ);
		double theta = Math.acos(dZ/r);
		
		
	}
	
	public static void main(String arg[]) {
		
	}
}
