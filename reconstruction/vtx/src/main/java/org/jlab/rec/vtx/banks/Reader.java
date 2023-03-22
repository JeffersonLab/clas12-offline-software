package org.jlab.rec.vtx.banks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.vtx.Particle;

public class Reader {

    public Reader() {
            // TODO Auto-generated constructor stub
    }

    /**
     * @return the _particles
     */
    public List<Particle> getParticles() {
        return _particles;
    }

    /**
     * @param _particles the _particles to set
     */
    public void setParticles(List<Particle> _particles) {
        this._particles = _particles;
    }
    private boolean updateWithUTrack = true;
    
    private List<Particle> _particles;
   
    public void readDataBanks(DataEvent event) {
        if(_particles!=null) {
            _particles.clear();
        } else {
            _particles = new ArrayList<>();
        }
        DataBank recBankEB = null;
        DataBank trkBankEB = null;
        DataBank utrkBankEB = null;
        
        if(event.hasBank("REC::Particle")) recBankEB = event.getBank("REC::Particle");
        if(event.hasBank("REC::Track")) trkBankEB = event.getBank("REC::Track");
        if(event.hasBank("REC::UTrack")) utrkBankEB = event.getBank("REC::UTrack");
        
        Map<Integer, double[]> uTrkMap = new HashMap<>();
        Map<Integer, double[]> pTrkMap = new HashMap<>();
        if(utrkBankEB!=null) {
            int nrows2 = utrkBankEB.rows();
            for(int loop = 0; loop < nrows2; loop++){
                int uindex = utrkBankEB.getInt("index", loop);
                double px = utrkBankEB.getFloat("px", loop);
                double py = utrkBankEB.getFloat("py", loop);
                double pz = utrkBankEB.getFloat("pz", loop);
                double vx = utrkBankEB.getFloat("vx", loop);
                double vy = utrkBankEB.getFloat("vy", loop);
                double vz = utrkBankEB.getFloat("vz", loop);
                double[] t = new double[]{px,py,pz,vx,vy,vz};
                uTrkMap.put(uindex, t);
            }
        }
        if(recBankEB!=null) {
            int nrows = recBankEB.rows();
            for(int loop = 0; loop < nrows; loop++){
                double px = recBankEB.getFloat("px", loop);
                double py = recBankEB.getFloat("py", loop);
                double pz = recBankEB.getFloat("pz", loop);
                double vx = recBankEB.getFloat("vx", loop);
                double vy = recBankEB.getFloat("vy", loop);
                double vz = recBankEB.getFloat("vz", loop);
                double[] t = new double[]{px,py,pz,vx,vy,vz};
                pTrkMap.put(loop, t);
            }
        }
        if(trkBankEB!=null) {
            int nrows2 = trkBankEB.rows();
            for(int loop = 0; loop < nrows2; loop++){
                int detector = trkBankEB.getInt("detector", loop);
                if(detector!=5) 
                    continue;
                int index = trkBankEB.getInt("index", loop);
                int pindex = trkBankEB.getInt("pindex", loop);
                if(uTrkMap.containsKey(index) && pTrkMap.containsKey(pindex)) {
                    pTrkMap.put(pindex, uTrkMap.get(index));
                }
            }
        }
        if(recBankEB!=null) {
            int nrows = recBankEB.rows();
            for(int loop = 0; loop < nrows; loop++){
                int pidCode = recBankEB.getInt("pid", loop);
                if(pidCode==0 || pidCode==22 || pidCode==2112) continue;
                int q = 0;
                if((int) (pidCode/1000)==0) {
                    q = (int) -Math.signum(pidCode);
                } else {
                    q = (int) Math.signum(pidCode);
                }
                
                double px = recBankEB.getFloat("px", loop);
                double py = recBankEB.getFloat("py", loop);
                double pz = recBankEB.getFloat("pz", loop);
                double vx = recBankEB.getFloat("vx", loop);
                double vy = recBankEB.getFloat("vy", loop);
                double vz = recBankEB.getFloat("vz", loop);
                if(this.updateWithUTrack && pTrkMap.containsKey(loop)) {
                    double[] t = pTrkMap.get(loop);
                    px = t[0];
                    py = t[1];
                    pz = t[2];
                    vx = t[3];
                    vy = t[4];
                    vz = t[5];
                } 
                _particles.add(new Particle(loop,pidCode, vx, vy, vz, px, py, pz, q));
            }
        }
    }
	
} // end class
