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

	
	public double[] VertexParams(double x, double y, double z, double px, double py, double pz, double Q, double Bfield, double xb, double yb ) {
        
		double[] value = new double[7];
		
		double LIGHTVEL     = 0.000299792458 ; 
        double pt = Math.sqrt(px*px + py*py);
        
        double R = Q* pt / (LIGHTVEL * Bfield);
        double tanL = pz / pt;

        double phi = Math.atan2(py, px);
       
        double xc   = x + R * Math.sin(phi);
        double yc   = y - R * Math.cos(phi);
        
        double Rc = Math.sqrt(xc*xc + yc*yc);
        
        double dca = R + Rc;
        if(Q>0) {
            dca = R - Rc;
        }
        
        //xc = 0 + (R-dca)sinphi0; yc = 0-(R-dca)cosphi0;
        double phi_dca = Math.atan2(-(xc-xb),-( -yc+yb));
        if(Q>0) {
        	phi_dca = Math.atan2((xc-xb),( -yc+yb));
        }
        
        double x0 = -dca*Math.sin(phi_dca);
        double y0 = dca*Math.cos(phi_dca);
        
    
        double arclength  = (((x-x0)*Math.cos(phi_dca))+((y-y0)*Math.sin(phi_dca)));
        double z0 = z - arclength * tanL;
 		
        double p0x = pt*Math.cos(phi_dca);
        double p0y = pt*Math.sin(phi_dca);
        double p0z = pz;
        
        value[0] = x0;
        value[1] = y0;
        value[2] = z0;
        value[3] = p0x;
        value[4] = p0y;
        value[5] = p0z;
        value[6] = arclength;
        
        return value;
        
	}
	
}