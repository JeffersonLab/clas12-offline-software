package org.jlab.rec.rtpc.banks;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.RecoHitVector;
import java.util.List;
import java.util.HashMap;
import org.jlab.rec.rtpc.hit.FinalTrackInfo;

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
        int listsize = finaltrackinfomap.size();
        if(listsize == 0) return null;
        if(finaltrackinfomap.size() == 0) return null;
        int row = 0;

        
        DataBank bank = event.createBank("RTPC::tracks", listsize);
        
        
        if (bank == null) {
            System.err.println("COULD NOT CREATE A BANK!!!!!!");
            return null;
        }
		
        for(int TID : finaltrackinfomap.keySet()) {

            bank.setInt("trkID", row, TID);
            bank.setFloat("px", row, (float) finaltrackinfomap.get(TID).get_px()/1000);
            bank.setFloat("py", row, (float) finaltrackinfomap.get(TID).get_py()/1000);
            bank.setFloat("pz", row, (float) finaltrackinfomap.get(TID).get_pz()/1000);
            bank.setFloat("vz", row, (float) finaltrackinfomap.get(TID).get_vz()/10);
            bank.setFloat("theta", row, (float) finaltrackinfomap.get(TID).get_theta());
            bank.setFloat("phi", row, (float) finaltrackinfomap.get(TID).get_phi());
            bank.setInt("nhits", row, finaltrackinfomap.get(TID).get_numhits());
            bank.setFloat("path", row, (float) finaltrackinfomap.get(TID).get_tl());
            bank.setFloat("dedx", row, (float) finaltrackinfomap.get(TID).get_dEdx());

            row++;
            //bank.show();
        }

        return bank;
    }	
}