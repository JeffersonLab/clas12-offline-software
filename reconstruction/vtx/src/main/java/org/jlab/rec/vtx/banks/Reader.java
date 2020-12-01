package org.jlab.rec.vtx.banks;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.physics.RecEvent;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.vtx.TrackParsHelix;

public class Reader {

	
	public Reader() {
		// TODO Auto-generated constructor stub
	}


	public List<TrackParsHelix> get_DCTrks(DataEvent event) {
    	
		
		if(event.hasBank("TimeBasedTrkg::TBTracks")==false ) {
			System.err.println(" NO TBTracks bank! ");						
			return new ArrayList<TrackParsHelix>();
		}
 
		List<TrackParsHelix> helices = new ArrayList<TrackParsHelix>();		
		DataBank bankDC = event.getBank("TimeBasedTrkg::TBTracks");
		
		float[] x		= bankDC.getFloat("Vtx0_x");	// doca to beam line x-position in the lab (in cm = default unit)
		float[] y		= bankDC.getFloat("Vtx0_y");  	// doca to beam line y-position in the lab
		float[] z		= bankDC.getFloat("Vtx0_z");  	// doca to beam line z-position in the lab
		float[] px		= bankDC.getFloat("p0_x"); 		// px at doca to beam line in the lab (in GeV/c)
		float[] py		= bankDC.getFloat("p0_y"); 		// py at doca to beam line in the lab
		float[] pz		= bankDC.getFloat("p0_z"); 		// pz at doca to beam line in the lab
		byte[] q        = bankDC.getByte("q");
		// cut on fitc2	
		
		for(int i = 0; i<x.length; i++){
			TrackParsHelix dcpars = new TrackParsHelix();
			dcpars.setHelixParams(x[i], y[i], z[i], px[i], py[i], pz[i], 
					             (double)q[i], dcpars.Bfield);
			helices.add(dcpars);
		}
		
		return helices;
	}
	
	
	public List<TrackParsHelix> get_CVTTrks(DataEvent event) {
		
		if(event.hasBank("CVTRec::Tracks")==false ) {
			System.err.println("there is no CVTRec bank ");						
			return new ArrayList<TrackParsHelix>();
		}

		List<TrackParsHelix> helices = new ArrayList<TrackParsHelix>();		
		
		EvioDataBank bankCV = (EvioDataBank) event.getBank("CVTRec::Tracks");
		
		double[] q		= bankCV.getDouble("q"); 			// charge
		double[] pt		= bankCV.getDouble("pt");			// pt in lab (GeV/c)
		double[] phi0	= bankCV.getDouble("phi0");  		// phi at doca to beam line in the lab
		double[] d0		= bankCV.getDouble("d0");  			// doca to beam line  in the lab (mm)
		double[] z0		= bankCV.getDouble("z0");	 		// z at doca to beam line in the lab (mm)
		double[] tandip	= bankCV.getDouble("tandip"); 		// tan of dip angle of helix
		double[] fitc2  = bankCV.getDouble("circlefit_chi2_per_ndf");
		// cut on fit c2
		
		if(event.hasBank("CVTRec::Tracks")==true) {
			for(int i = 0; i<pt.length; i++){
				TrackParsHelix cvtpars = new TrackParsHelix();
				cvtpars.setHelixParams(pt[i], phi0[i], d0[i], z0[i], tandip[i], (double)q[i], cvtpars.Bfield);
				helices.add(cvtpars);
			}
		
		}
		return helices;
	}
	
	
} // end class
