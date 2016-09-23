package org.jlab.rec.ctof;

import java.util.ArrayList;

import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Point3D;

public class CTOFGeometry {

	public CTOFGeometry() {
	}
		public ScintillatorPaddle getScintillatorPaddle(int paddle) {
			double len = 31.765*2.54;
			ArrayList<Point3D> cornerPoints = new ArrayList<Point3D>(8);
			cornerPoints.add(new Point3D(-28.023, 1.803, -len/2));
			cornerPoints.add(new Point3D(-25.0, 1.605, -len/2));
			cornerPoints.add(new Point3D(-25.0, -1.605, -len/2));
			cornerPoints.add(new Point3D(-28.023, -1.803, -len/2));
			cornerPoints.add(new Point3D(-28.023, 1.803, len/2));
			cornerPoints.add(new Point3D(-25.0, 1.605, len/2));
			cornerPoints.add(new Point3D(-25.0, -1.605, len/2));
			cornerPoints.add(new Point3D(-28.023, -1.803, len/2));
			
			for(int i =0; i<8; i++)
				cornerPoints.get(i).rotateZ((paddle-1)*Math.toRadians(7.5) + Math.toRadians(180.));
			
			ScintillatorPaddle geomPaddle = 
					new ScintillatorPaddle(paddle-1, cornerPoints.get(0), cornerPoints.get(1), cornerPoints.get(2), cornerPoints.get(3), cornerPoints.get(4), cornerPoints.get(5), cornerPoints.get(6), cornerPoints.get(7));
			
			return geomPaddle;
		}
	

}
