package org.jlab.rec.vtx;



import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

public class VertexFinder {
	
	public VertexFinder() {
		// TODO Auto-generated constructor stub
	}
	
        private List<ArrayList<TrackParsHelix>> _HelixPairs = new ArrayList<ArrayList<TrackParsHelix>>();	


	public List<ArrayList<TrackParsHelix>> get_HelixPairs() {
		return _HelixPairs;
	}
	public void set_HelixPairs(List<ArrayList<TrackParsHelix>> _HelixPairs) {
		this._HelixPairs = _HelixPairs;
	}
	
	static void combinations2(int[] arr, List<int[]> list){
		
        for(int i = 0; i<arr.length; i++)
            for(int j = i+1; j<arr.length; j++)
                list.add(new int[]{arr[i],arr[j]});
    }	
	
	
	public void FindHelixPairs(List<TrackParsHelix> allHelices) {
		
		List<ArrayList<TrackParsHelix>> allHelixPairs = new ArrayList<ArrayList<TrackParsHelix>>();	
		List<int[]> list = new ArrayList<int[]>();
		int[] arr = new int[allHelices.size()];
		for(int i =0; i< allHelices.size(); i++) {
			arr[i] = i;
		}
		combinations2(arr,list);	

		for(int i=0; i< list.size(); i++) {
			int h1Idx = list.get(i)[0];
			int h2Idx = list.get(i)[1];
			ArrayList<TrackParsHelix> helixPair = new ArrayList<TrackParsHelix>(2);
			helixPair.add(allHelices.get(h1Idx));
			helixPair.add(allHelices.get(h2Idx));
			allHelixPairs.add(helixPair);
		}
		this.set_HelixPairs(allHelixPairs);
	}
	
	
	public Vertex FindVertex(ArrayList<TrackParsHelix> helixPair) {
		
	    TrackParsHelix h1 = helixPair.get(0);
	    TrackParsHelix h2 = helixPair.get(1);
           
	    int nsteps=50;
	    double step = 0.005;    		
	    double minD = 9999.;
	    double phiMin1 = 9999.;
	    double phiMin2 = 9999.;	    
	    Point3D pMin1 = null;
            Point3D pMin2 = null;
            Vector3D uMin1 = null;
            Vector3D uMin2 = null;
	    // loop through the points of both helix curves 
	    // and find two points with the minimal distance between them.
		for(double phi1=-(nsteps*step) ; phi1<(nsteps*step); phi1+=step) {
                    for(double phi2=-(nsteps*step) ; phi2<(nsteps*step); phi2+=step) {
                    double d = h1.calcPoint(phi1).distance(h2.calcPoint(phi2)); 

                    if(d<minD) { minD = d; phiMin1=phi1; phiMin2=phi2; }
                    }
		}
		
		// repeat previous loops around phiMin1 and phiMin2 with smaller step
		nsteps=15;
		step = step/10.;
		for(double phi1=phiMin1-(nsteps*step) ; phi1<phiMin1+(nsteps*step); phi1+=step) {
                    for(double phi2=phiMin2-(nsteps*step) ; phi2<phiMin2+(nsteps*step); phi2+=step) {
                        double d = h1.calcPoint(phi1).distance(h2.calcPoint(phi2));
                        if(d<minD) { minD = d; phiMin1=phi1; phiMin2=phi2; 
                        uMin1 =  h1.calcDir(phi1); uMin2 =  h2.calcDir(phi2);}
                    }
		}

		// one more iteration
		nsteps=15;
		step = step/10.;
		for(double phi1=phiMin1-(nsteps*step) ; phi1<phiMin1+(nsteps*step); phi1+=step) {
                    for(double phi2=phiMin2-(nsteps*step) ; phi2<phiMin2+(nsteps*step); phi2+=step) {
                        double d = h1.calcPoint(phi1).distance(h2.calcPoint(phi2));
                        if(d<minD) { minD = d; phiMin1=phi1; phiMin2=phi2; 
                        pMin1 =  h1.calcPoint(phi1);pMin2 =  h2.calcPoint(phi2);
                        uMin1 =  h1.calcDir(phi1); uMin2 =  h2.calcDir(phi2);}
                    }
		}
				
		// the line connecting the points of two helixes with minimal distance between them
		Line3D intrxLine = new Line3D(h1.calcPoint(phiMin1), h2.calcPoint(phiMin2));
		Point3D intrxPoint = intrxLine.midpoint();		
		
                Vertex v = null;
                if(pMin1!=null && pMin2!=null && uMin1!=null && uMin2!=null) {
                    v = new Vertex();
                    v.set_HelixPair(helixPair);
                    v.set_Vertex(intrxPoint);
                    v.setDoca(minD);
                    v.setTrack1POCA(pMin1);
                    v.setTrack2POCA(pMin2);
                    v.setTrack1POCADir(uMin1);
                    v.setTrack2POCADir(uMin2);
                }
                
	    return v;
	
	} // end FindVertex()
} // end class
