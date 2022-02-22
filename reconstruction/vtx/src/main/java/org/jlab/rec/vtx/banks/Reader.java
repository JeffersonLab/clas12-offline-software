package org.jlab.rec.vtx.banks;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.swimtools.Swim;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.vtx.TrackParsHelix;

public class Reader {

	
	public Reader() {
		// TODO Auto-generated constructor stub
	}

        float[] b = new float[3];
	public List<TrackParsHelix> get_Trks(DataEvent event, Swim swimmer) {
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
                        (double)event.getBank("REC::Particle").getFloat("vx",i),
                        (double)event.getBank("REC::Particle").getFloat("vy",i),
                        (double)event.getBank("REC::Particle").getFloat("vz",i), b);
                    
                    TrackParsHelix dcpars = new TrackParsHelix((int)i);
                    dcpars.setHelixParams(
                            (double)event.getBank("REC::Particle").getFloat("vx",i),
                            (double)event.getBank("REC::Particle").getFloat("vy",i),
                            (double)event.getBank("REC::Particle").getFloat("vz",i),
                            (double)event.getBank("REC::Particle").getFloat("px",i),
                            (double)event.getBank("REC::Particle").getFloat("py",i),
                            (double)event.getBank("REC::Particle").getFloat("pz",i),
                            (double) bank.getByte("charge", i), b[2]); 
                    helices.add(dcpars);
                }
            }
		
            return helices;
	}
        public List<TrackParsHelix> get_MCTrks(DataEvent event, Swim swimmer) {
    	
		
		if(event.hasBank("MC::Particle")==false ) {
			System.err.println(" NO MC bank! ");						
			return new ArrayList<TrackParsHelix>();
		}
 
		List<TrackParsHelix> helices = new ArrayList<TrackParsHelix>();		
		DataBank bankMC = event.getBank("MC::Particle");
		
                for(int i = 0; i<bankMC.rows(); i++){
			TrackParsHelix dcpars = new TrackParsHelix(i);
                        swimmer.BfieldLab(bankMC.getFloat("vx",i), bankMC.getFloat("vy",i), bankMC.getFloat("vz",i), b);
			dcpars.setHelixParams(bankMC.getFloat("vx",i), bankMC.getFloat("vy",i), bankMC.getFloat("vz",i),
                                bankMC.getFloat("px",i), bankMC.getFloat("py",i), bankMC.getFloat("pz",i), 
					             this.getQ(bankMC.getInt("pid",i)), b[2]);
			helices.add(dcpars);
		}
		
		return helices;
	}
	
    private double getQ(int pid) {
        double q = 1;
        if(Math.abs(pid/100)>0) {
            q = (double)-Math.abs(pid); // leptons 11,12,13
        } else {
            q = (double)Math.abs(pid);
        }
        return q;
    }
	
	
} // end class
