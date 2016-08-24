package org.jlab.rec.dc.trajectory;

import java.util.Random;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.rec.dc.track.Track;

public class Vertex {

	Random rn = new Random();
	public static double SMEARING_FAC = 0;
	DCSwimmer swim2 = new DCSwimmer();
	
	public Vertex() {

		swim2.isRotatedCoordinateSystem = false;
		
	}
	public double vertexEstimator(EvioDataEvent event) {

		EvioDataBank bank = (EvioDataBank) event.getBank("GenPart::true");
        
        double[] vx = bank.getDouble("vx");
        double[] vy = bank.getDouble("vy");
        
		double val = Math.sqrt(vx[0]*vx[0]+vy[0]*vy[0])/10.; // analysis done in cm, gemc vtx units = mm
		
		double smearedVal = val + SMEARING_FAC * rn.nextGaussian();
		
		return smearedVal; 
	}
	
	public void resetTrackAtRasterRadius(EvioDataEvent event, Track thecand) {
		
		double r = vertexEstimator(event) ;
		
		double x0 = thecand.get_Vtx0().x();
		double y0 = thecand.get_Vtx0().y();
		double z0 = thecand.get_Vtx0().z();
		
		double p0x = thecand.get_pAtOrig().x();
		double p0y = thecand.get_pAtOrig().y();
		double p0z = thecand.get_pAtOrig().z();
		int q = thecand.get_Q();

    	swim2.SetSwimParameters(x0, y0, z0, p0x, p0y, p0z, q);
    	double[] result = swim2.SwimToCylinder(r);
    	
    	double rx = result[0];
    	double ry = result[1];
    	double rz = result[2];
    	double rpx = result[3];
    	double rpy = result[4];
    	double rpz = result[5];
    	double rpath = result[6];
    	
    	
    	double path = thecand.get_TotPathLen()-rpath;
    	
    	thecand.set_TotPathLen(path);
    	thecand.set_Vtx0(new Point3D(rx,ry,rz));
    	thecand.set_pAtOrig(new Vector3D(rpx,rpy,rpz));
    	
	}

	
}