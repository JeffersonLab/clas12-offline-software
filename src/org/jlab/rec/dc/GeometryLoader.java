package org.jlab.rec.dc;


import java.io.FileNotFoundException;

import org.jlab.clas.detector.DetectorType;
import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.detector.dc.DCDetector;
import org.jlab.geom.detector.dc.DCFactory;
import org.jlab.geom.detector.dc.DCFactoryUpdated;

/**
 * A class to load the geometry constants used in the DC reconstruction.  The coordinate system used in the Tilted Sector coordinate system.
 * @author ziegler
 *
 */
public class GeometryLoader {

	public static boolean isGeometryLoaded = false;
	public static DCDetector dcDetector;

	public static void Load() {
		// the geometry is different is hardware and geometry... until GEMC gets updated we need to run with this flag
		ConstantProvider dcDataProvider = DataBaseLoader.getDetectorConstants(DetectorType.DC);
		if(Constants.newGeometry == false)
			dcDetector = (new DCFactory()).createDetectorTilted(dcDataProvider);
		if(Constants.newGeometry == true)
			dcDetector = (new DCFactoryUpdated()).createDetectorTilted(dcDataProvider);
		if (isGeometryLoaded) return;

		// mark the geometry as loaded
		isGeometryLoaded = true;
		System.out.println("DC Geometry constants are Loaded -- new geometry = "+Constants.newGeometry);
	}
	
	public static void main (String arg[]) throws FileNotFoundException {
		//PrintWriter pw = new PrintWriter(new File("/Users/ziegler/workspace/coatjava-2.4/cpFiles.csh"));
		Constants.newGeometry = true;
		GeometryLoader.Load();
		double x1 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(1).getComponent(16).getMidpoint().x();
		System.out.println(x1);
		//pw.close();
	/*	System.out.println("dx; = "+(GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(0).getComponent(0).getMidpoint().x()-
				GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(0).getComponent(1).getMidpoint().x())*Math.cos(Math.toRadians(6.)));
		System.out.println("dz; = "+(GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(0).getComponent(0).getMidpoint().z()-
				GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(1).getComponent(0).getMidpoint().z()));
		System.out.println("layer 1 x; = "+GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(0).getComponent(1).getMidpoint().x());
		System.out.println("layer 2 x; = "+GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(1).getComponent(1).getMidpoint().x());
			pw.printf("%f\n",GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(1).getComponent(1).getMidpoint().x()
					-0*GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(2).getComponent(1).getMidpoint().x()
			);
		
		pw.close();*/
		/*
		int superlayer =1;
		int layer =1;
		double trkX = -43.41;
		
		double x1 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(1).getMidpoint().x();
		double x0 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(0).getMidpoint().x();
		
		double deltax = Math.abs(x1-x0);
		
		double xFirstCell = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(0).getMidpoint().x();
		
		System.out.println(Math.ceil((trkX-xFirstCell+deltax/2.)/deltax) );
		
		*/
		/*
		int superlayer =2;
		int layer =1;
		for(int i =0; i<10; i++) {
			double x1 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(i+1).getMidpoint().x();
			double x0 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(i).getMidpoint().x();
			
			double z1 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(i).getMidpoint().z();
			double z0 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1+1).getComponent(i).getMidpoint().z();
			
			
			double deltax = Math.abs(x1-x0);
			double deltaz = Math.abs(z1-z0);
			//System.out.println(Math.toDegrees(Math.cos(2*deltaz*Math.tan(Math.PI/6)/deltax))+" "+deltax+" "+2*deltaz*Math.tan(Math.PI/6)+" "+deltax*Math.cos(Math.PI/6.)+" "+GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(0).getMidpoint().x());
			System.out.println(deltax+" "+2*deltaz*Math.tan(Math.PI/6));
		}
		*/
		
	}
}
