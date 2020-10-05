package org.jlab.rec.rtpc.banks;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.RecoHitVector;
import java.util.List;
import java.util.HashMap;
import org.jlab.rec.rtpc.hit.FinalTrackInfo;
import org.jlab.rec.rtpc.hit.PadVector;

public class RecoBankWriter {

    /**
     * 
     * @param hitlist the list of  hits that are of the type Hit.
     * @return hits bank
     *
     */
    public  DataBank fillRTPCHitsBank(DataEvent event, HitParameters params) {

        int listsize = 0;
        int row = 0;
        HashMap<Integer, List<RecoHitVector>> recotrackmap = params.get_recotrackmap();

        for(int TID : recotrackmap.keySet()) {
            for(int i = 0; i < recotrackmap.get(TID).size(); i++) {
                    listsize++;
            }
        }
        if(listsize == 0) return null;
        DataBank bank = event.createBank("RTPC::hits", listsize);
        
        
        if (bank == null) {
            System.err.println("COULD NOT CREATE A BANK!!!!!!");
            return null;
        }
		
        for(int TID : recotrackmap.keySet()) {
            for(int i = 0; i < recotrackmap.get(TID).size(); i++) {
                int cellID = recotrackmap.get(TID).get(i).pad();
                double x_rec = recotrackmap.get(TID).get(i).x();
                double y_rec = recotrackmap.get(TID).get(i).y();
                double z_rec = recotrackmap.get(TID).get(i).z();
                double time  = recotrackmap.get(TID).get(i).time();
                double tdiff = recotrackmap.get(TID).get(i).dt();

                bank.setInt("trkID", row, TID);
                bank.setInt("id", row, cellID);
                bank.setFloat("time", row, (float) time);
                bank.setFloat("x", row, (float) x_rec);
                bank.setFloat("y", row, (float) y_rec);
                bank.setFloat("z", row, (float) z_rec);				
                bank.setFloat("tdiff", row, (float) tdiff);

                row++;
            }
        }
        //bank.show();
        return bank;
    }	
    
    public  DataBank fillRTPCTrackBank(DataEvent event, HitParameters params) {

        HashMap<Integer, FinalTrackInfo> finaltrackinfomap = params.get_finaltrackinfomap();
        HashMap<Integer, List<RecoHitVector>> recotrackmap = params.get_recotrackmap();
        int listsize = finaltrackinfomap.size();
        if(listsize == 0) return null;
        int row = 0;

        
        DataBank bank = event.createBank("RTPC::tracks", listsize);
        
        
        if (bank == null) {
            System.err.println("COULD NOT CREATE A BANK!!!!!!");
            return null;
        }
		
        for(int TID : finaltrackinfomap.keySet()) {
            FinalTrackInfo track = finaltrackinfomap.get(TID);
            List<RecoHitVector> listhits = recotrackmap.get(TID);
            RecoHitVector hitvec = recotrackmap.get(TID).get(0);
            PadVector smallpad = params.get_padvector(hitvec.smallhit().pad());
            PadVector largepad = params.get_padvector(hitvec.largehit().pad());
            
            bank.setInt("trkID", row, TID);
            bank.setFloat("px", row, (float) track.get_px()/1000);
            bank.setFloat("py", row, (float) track.get_py()/1000);
            bank.setFloat("pz", row, (float) track.get_pz()/1000);
            bank.setFloat("vz", row, (float) track.get_vz()/10);
            bank.setFloat("theta", row, (float) track.get_theta());
            bank.setFloat("phi", row, (float) track.get_phi());
            bank.setInt("nhits", row, track.get_numhits());
            bank.setFloat("path", row, (float) track.get_tl());
            bank.setFloat("adcsum", row, (float) track.get_ADCsum());
            bank.setFloat("dedx", row, (float) track.get_dEdx());
            bank.setFloat("r_helix", row, (float) track.get_R());
            bank.setFloat("x_helix", row, (float) track.get_A());
            bank.setFloat("y_helix", row, (float) track.get_B());
            bank.setFloat("chi2_helix", row, (float) track.get_chi2());
            bank.setFloat("min_row", row, (float) smallpad.row());
            bank.setFloat("min_col", row, (float) smallpad.col());
            bank.setFloat("max_row", row, (float) largepad.row());
            bank.setFloat("max_col", row, (float) largepad.col());
            bank.setFloat("min_time", row, (float) hitvec.smallhit().time());
            bank.setFloat("max_time", row, (float) hitvec.largehit().time());
            bank.setFloat("min_radius", row, (float) listhits.get(listhits.size()-1).r());
            bank.setFloat("max_radius", row, (float) listhits.get(0).r());
            bank.setFloat("min_phi", row, (float) listhits.get(listhits.size()-1).phi());
            bank.setFloat("max_phi", row, (float) listhits.get(0).phi());

            row++;
        }
        //bank.show();
        return bank;
    }	
}