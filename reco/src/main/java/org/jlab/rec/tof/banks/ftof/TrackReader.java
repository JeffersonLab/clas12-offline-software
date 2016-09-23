package org.jlab.rec.tof.banks.ftof;
import java.util.ArrayList;
import java.util.List;

import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;

import eu.mihosoft.vrl.v3d.Vector3d;

/**
 * 
 * @author ziegler
 *
 */
public class TrackReader {


	public TrackReader() {
		// TODO Auto-generated constructor stub
	}
	
	private List<Line3d>  _TrkLines ;
	private double[] _Paths;
	
	public List<Line3d> get_TrkLines() {
		return _TrkLines;
	}

	public void set_TrkLines(List<Line3d> trkLines) {
		this._TrkLines = trkLines;
	}

	public double[] get_Paths() {
		return _Paths;
	}

	public void set_Paths(double[] paths) {
		this._Paths = paths;
	}

	
	
	public void fetch_Trks(DataEvent event) {
	 
		if(event.hasBank("TimeBasedTrkg::TBTracks")==false ) {
			System.err.println("there is no DC bank ");
			
			_TrkLines = new ArrayList<Line3d>();
			
			return;
		}

				
		EvioDataBank bankDC = (EvioDataBank) event.getBank("TimeBasedTrkg::TBTracks");
		
		double[] fitChisq = bankDC.getDouble("fitChisq"); // use this to select good tracks
       
		double[] x		= bankDC.getDouble("c3_x");			// Region 3 cross x-position in the lab
		double[] y		= bankDC.getDouble("c3_y");  		// Region 3 cross y-position in the lab
		double[] z		= bankDC.getDouble("c3_z");  		// Region 3 cross z-position in the lab
		double[] ux		= bankDC.getDouble("c3_ux"); 		// Region 3 cross x-unit-dir in the lab
		double[] uy		= bankDC.getDouble("c3_uy"); 		// Region 3 cross y-unit-dir in the lab
		double[] uz		= bankDC.getDouble("c3_uz"); 		// Region 3 cross z-unit-dir in the lab
		double[] p		= bankDC.getDouble("pathlength"); 	// pathlength of the track from origin to DC R3
		  
		if(event.hasBank("TimeBasedTrkg::TBTracks")==true) {
			// instanciates the list 
			// each arraylist corresponds to the tracks for a given sector
			List<Line3d> trkLines = new ArrayList<Line3d>();
			// each array of paths likewise corresponds to the tracks for a given sector
			double[] paths = new double[x.length];
			
			
			for(int i = 0; i<x.length; i++){
				if(fitChisq[i]>1)
					continue; // check this
				
				Line3d trk_path = new Line3d(new Vector3d(x[i],y[i],z[i]), new Vector3d(ux[i],uy[i],uz[i]));
			    
	    	    // add this hit
			    trkLines.add(trk_path);
			    paths[i] = p[i];
			}
		
			// fill the list of TOF hits
			this.set_TrkLines(trkLines);
			this.set_Paths(paths);
		}
	}

	

}
