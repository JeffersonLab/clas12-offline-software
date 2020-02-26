package cnuphys.ced.geometry;

import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Point3D;

import eu.mihosoft.vrl.v3d.Vector3d;

public class GeometryManager2 {


    boolean isGeometryLoaded = false;
    private static DCGeant4Factory dcDetector;

    
    public static synchronized void load(int runNb, String var) {
        // the geometry is different is hardware and geometry... until GEMC gets updated we need to run with this flag
        ConstantProvider providerDC = GeometryFactory.getConstants(DetectorType.DC, runNb, var);
        //dcDetector = new DCGeant4Factory(providerDC);
        dcDetector = new DCGeant4Factory(providerDC, DCGeant4Factory.MINISTAGGERON);
    }
    

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @param sector 1-based sector
     * @return
     */
    private static Point3D rotateFromTSCtoLabC(double x, double y, double z, int sector) {
        double rzs = -x * Math.sin(Math.toRadians(25.)) + z * Math.cos(Math.toRadians(25.));
        double rxs = x * Math.cos(Math.toRadians(25.)) + z * Math.sin(Math.toRadians(25.));
        
        double rx = rxs * Math.cos((sector - 1) * Math.toRadians(60.)) - y * Math.sin((sector - 1) * Math.toRadians(60.));
        double ry = rxs * Math.sin((sector - 1) * Math.toRadians(60.)) + y * Math.cos((sector - 1) * Math.toRadians(60.));

        return new Point3D(rx,ry,rzs);
    }

	
	public static void main(String arg[]) {
		load(1, "default");
		Vector3d v3d1 = dcDetector.getWireRightend(0, 0, 0);
		Vector3d v3d2 = dcDetector.getWireLeftend(0, 0, 0);
		Vector3d vmid = dcDetector.getWireMidpoint(0, 0, 0);
		
		Point3D p0 = rotateFromTSCtoLabC(v3d1.x, v3d1.y, v3d1.z, 1);
		Point3D p1 = rotateFromTSCtoLabC(v3d2.x, v3d2.y, v3d2.z, 1);
		Point3D pmid = rotateFromTSCtoLabC(vmid.x, vmid.y, vmid.z, 1);
		
		System.out.println(p0 + "   " + p1 + "  MID: " + pmid);
	}
}
