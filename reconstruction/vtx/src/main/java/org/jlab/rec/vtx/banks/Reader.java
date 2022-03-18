package org.jlab.rec.vtx.banks;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.vtx.TrackParsHelix;

public class Reader {

	
	public Reader() {
		// TODO Auto-generated constructor stub
	}

        static float[] b = new float[3];
	public static List<TrackParsHelix> get_Trks(DataEvent event, Swim swimmer, double xb, double yb) {
            if(event.hasBank("REC::Particle")==false ) {
                //System.err.println(" NO Tracks bank! ");						
                return new ArrayList<TrackParsHelix>();
            }
            List<TrackParsHelix> helices = new ArrayList<TrackParsHelix>();	
            DataBank bank = event.getBank("REC::Particle");
            int rows = bank.rows();
            for (int i = 0; i < rows; i++) {
                if (bank.getFloat("chi2pid", i) !=(float)9999 && bank.getByte("charge", i)!=0) {
                    swimmer.BfieldLab(
                        (double)bank.getFloat("vx",i),
                        (double)bank.getFloat("vy",i),
                        (double)bank.getFloat("vz",i), b);
                    double Bf = Math.sqrt(b[0]*b[0]+b[1]*b[1]+b[2]*b[2]);
            
                    TrackParsHelix trkPars = new TrackParsHelix((int)i, (double)bank.getFloat("vx",i),
                            (double)bank.getFloat("vy",i),
                            (double)bank.getFloat("vz",i),
                            (double)bank.getFloat("px",i),
                            (double)bank.getFloat("py",i),
                            (double)bank.getFloat("pz",i),
                            (int) bank.getByte("charge", i), Bf,
                            xb,yb);
                    
                    helices.add(trkPars);
                }
            }
		
            return helices;
	}
        public static List<TrackParsHelix> get_MCTrks(DataEvent event, Swim swimmer, double xb, double yb) {
    	
		
		if(event.hasBank("MC::Particle")==false ) {
			System.err.println(" NO MC bank! ");						
			return new ArrayList<TrackParsHelix>();
		}
 
		List<TrackParsHelix> helices = new ArrayList<TrackParsHelix>();		
		DataBank bank = event.getBank("MC::Particle");
		double Bf = Math.sqrt(b[0]*b[0]+b[1]*b[1]+b[2]*b[2]);
                    
                for(int i = 0; i<bank.rows(); i++){
                    TrackParsHelix trkPars = new TrackParsHelix((int)i, (double)bank.getFloat("vx",i),
                            (double)bank.getFloat("vy",i),
                            (double)bank.getFloat("vz",i),
                            (double)bank.getFloat("px",i),
                            (double)bank.getFloat("py",i),
                            (double)bank.getFloat("pz",i),
                            getQ(bank.getInt("pid",i)), Bf,
                            xb,yb);
                    
			helices.add(trkPars);
		}
		
		return helices;
	}
	
    private static int getQ(int pid) {
        int q = 1;
        if(Math.abs(pid/100)>0) {
            q = -Math.abs(pid); // leptons 11,12,13
        } else {
            q = Math.abs(pid);
        }
        return q;
    }
	
	
} // end class
