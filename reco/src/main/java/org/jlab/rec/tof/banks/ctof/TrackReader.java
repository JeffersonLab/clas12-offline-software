package org.jlab.rec.tof.banks.ctof;
import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;

/**
 * 
 * @author ziegler
 *
 */
public class TrackReader {
 


		public TrackReader() {
			// TODO Auto-generated constructor stub
	}
	
	private List<ArrayList<Path3D>>  _TrkLines ;
	private List<double[]> _Paths;
	
	public List<ArrayList<Path3D>> get_TrkLines() {
		return _TrkLines;
	}

	public void set_TrkLines(List<ArrayList<Path3D>> _TrkLines) {
		this._TrkLines = _TrkLines;
	}

	public List<double[]> get_Paths() {
		return _Paths;
	}

	public void set_Paths(List<double[]> _Paths) {
		this._Paths = _Paths;
	}

	
	
	public void fetch_Trks(DataEvent event) {
	 
		if(event.hasBank("CVTRec::Tracks")==false ) {
			System.err.println("there is no CVT bank ");
			
			_TrkLines = new ArrayList<ArrayList<Path3D>>();
			
			return;
		}

				
		EvioDataBank bank = (EvioDataBank) event.getBank("CVTRec::Tracks");
		
		double[] fitChisq = bank.getDouble("circlefit_chi2_per_ndf"); // use this to select good tracks
	      
		double[] x		= bank.getDouble("c_x");			// cross x-position in the lab at the CTOF face
		double[] y		= bank.getDouble("c_y");  			// cross y-position in the lab at the CTOF face
		double[] z		= bank.getDouble("c_z");  			// cross z-position in the lab at the CTOF face
		double[] ux		= bank.getDouble("c_ux"); 			// cross x-unit-dir in the lab at the CTOF face
		double[] uy		= bank.getDouble("c_uy"); 			// cross y-unit-dir in the lab at the CTOF face
		double[] uz		= bank.getDouble("c_uz"); 			// cross z-unit-dir in the lab at the CTOF face
		double[] p		= bank.getDouble("pathlength"); 	// pathlength
		  
		if(event.hasBank("CVTRec::Tracks")==true) {
			// instanciates the list 
			// each arraylist corresponds to the tracks for a given sector
			List<ArrayList<Path3D>> trkLines = new ArrayList<ArrayList<Path3D>>(1);
			// each array of paths likewise corresponds to the tracks for a given sector
			List<double[]> paths = new ArrayList<double[]>(1);
			
			for(int s = 0; s < 1; s++) {
				trkLines.add(new ArrayList<Path3D>());
			}
			
			for(int i = 0; i<1; i++){
				if(fitChisq[i]>1)
					continue; // check this
				
			    Path3D trk_path = new Path3D();
			   
			    trk_path.generate(new Point3D(x[i],y[i],z[i]), new Vector3D(ux[i],uy[i],uz[i]),  1500.0, 2);
			    
	    	    // add this hit
			    trkLines.get(0).add(trk_path);
			    paths.get(0)[i] = p[i];
			}
		
			// fill the list of TOF hits
			this.set_TrkLines(trkLines);
			this.set_Paths(paths);
		}
	}

}


