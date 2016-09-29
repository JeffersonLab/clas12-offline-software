package org.jlab.rec.dc;


import java.io.FileNotFoundException;

import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.detector.dc.DCDetector;
import org.jlab.geom.detector.dc.DCFactoryUpdated;
import org.jlab.geom.prim.Point3D;

/**
 * A class to load the geometry constants used in the DC reconstruction.  The coordinate system used in the Tilted Sector coordinate system.
 * @author ziegler
 *
 */
public class GeometryLoader {

	public static boolean isGeometryLoaded = false;
	public static DCDetector dcDetector;

	public static synchronized void Load(int runNb, String var) {
		// the geometry is different is hardware and geometry... until GEMC gets updated we need to run with this flag
		ConstantProvider  provider = GeometryFactory.getConstants(DetectorType.DC, runNb, var);
        dcDetector = (new DCFactoryUpdated()).createDetectorTilted(provider);
       
		System.out.println(" -- DC Geometry constants are Loaded for RUN   "+runNb+" with VARIATION "+var);
	}
	
	public static void main (String arg[]) throws FileNotFoundException {

		GeometryLoader.Load(10, "default");
		
		Point3D ep1 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(0).getComponent(111).getLine().origin();
		Point3D ep2 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(0).getComponent(111).getLine().end();
		System.out.println(ep1.toString()+", "+ep2.toString());
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
		
		/*int layer =1;
		for(int superlayer =1; superlayer<=6; superlayer++) {
			double x11 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(0+1).getMidpoint().x();
			double x0 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(0).getMidpoint().x();
			
			double z1 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(0).getMidpoint().z();
			double z0 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1+1).getComponent(0).getMidpoint().z();
			
			
			double deltax = Math.abs(x11-x0);
			double deltaz = Math.abs(z1-z0);
			//System.out.println(Math.toDegrees(Math.cos(2*deltaz*Math.tan(Math.PI/6)/deltax))+" "+deltax+" "+2*deltaz*Math.tan(Math.PI/6)+" "+deltax*Math.cos(Math.PI/6.)+" "+GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(0).getMidpoint().x());
			System.out.println((deltax)*Math.tan(Math.PI/6.)); 
		}*/
		
		
	}
}
