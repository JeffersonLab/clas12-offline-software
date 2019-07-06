package org.jlab.rec.rich;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.clas.detector.DetectorResponse;

public class RICHio {

    /*
     *   RICH i/o 
     */

    // constructor
    // ----------------
    public RICHio() {
    // ----------------
        
    }

    // ----------------
    public void clear_Banks(DataEvent event) {
    // ----------------

        int debugMode = 0;

        // remove previous version of banks from the event
        if(event.hasBank("RICH::hits")){
            event.removeBank("RICH::hits");
            if(debugMode==1)System.out.format("Remove RICH::hits from event \n");
        }
        if(event.hasBank("RICH::clusters")){
            event.removeBank("RICH::clusters");
            if(debugMode==1)System.out.format("Remove RICH::clusters from event \n");
        }
        if(event.hasBank("RICH::response")){
            event.removeBank("RICH::response");
            if(debugMode==1)System.out.format("Remove RICH::response from event \n");
        }
        if(event.hasBank("RICH::ringCher")){
            event.removeBank("RICH::ringCher");
            if(debugMode==1)System.out.format("Remove RICH::ringCher from event \n");
        }
        if(event.hasBank("RICH::hadrons")){
            event.removeBank("RICH::hadrons");
            if(debugMode==1)System.out.format("Remove RICH::hadrons from event \n");
        }
        if(event.hasBank("RICH::photons")){
            event.removeBank("RICH::photons");
            if(debugMode==1)System.out.format("Remove RICH::photons from event \n");
        }

    }


    // ----------------
    public void write_PMTBanks(DataEvent event, RICHEvent richevent) {
    // ----------------

        if(richevent.get_nHit()>0)write_HitBank(event, richevent);

        if(richevent.get_nClu()>0)write_ClusterBank(event, richevent);

    }


    // ----------------
    public void write_RECBank(DataEvent event, RICHEvent richevent, RICHConstants recopar) {
    // ----------------

        int debugMode = 0;

        int NMAT = richevent.get_nMatch();
        if(debugMode>=1)System.out.format("Creating Bank for %5d Matches \n", NMAT);

        if(NMAT>0){

            String richBank = "RICH::response";
            DataBank bankRich = getRichResponseBank(richevent.get_Matches(), event, richBank, recopar);
            if(bankRich!=null)event.appendBanks(bankRich);

        }

    }


    // ----------------
    public void write_CherenkovBanks(DataEvent event, RICHEvent richevent, RICHConstants recopar) {
    // ----------------

        if(richevent.get_nHad()>0)write_HadronBank(event, richevent);

        if(richevent.get_nPho()>0)write_PhotonBank(event, richevent);

        if(richevent.get_nPho()>0)write_RingCherBank(event, richevent, recopar);

    }


    // ----------------
    private void write_HitBank(DataEvent event, RICHEvent richevent) {
    // ----------------

        int debugMode = 0;

        int NHIT = richevent.get_nHit();
        if(debugMode>=1)System.out.format("Creating Bank for %5d Hits \n", NHIT);

        if(NHIT>0) {
            if(debugMode>=2)System.out.println(" --> Creating the Hits Bank ");
            DataBank bankHits = event.createBank("RICH::hits", NHIT);
            if(bankHits==null){
                System.out.println("ERROR CREATING BANK : RICH::hits");
                return;
            }

            for(int i = 0; i < NHIT; i++){

                RICHHit hit = richevent.get_Hit(i);

                bankHits.setShort("id",      i, (short) hit.get_id());
                bankHits.setShort("sector",  i, (short) hit.get_sector());
                bankHits.setShort("tile",    i, (short) hit.get_tile());
                bankHits.setShort("pmt",     i, (short) hit.get_pmt());
                bankHits.setShort("anode",   i, (short) hit.get_anode());
                bankHits.setFloat("x",       i, (float) (hit.get_x()));
                bankHits.setFloat("y",       i, (float) (hit.get_y()));
                bankHits.setFloat("z",       i, (float) hit.get_z());
                bankHits.setFloat("time",    i, (float) hit.get_time());
                bankHits.setFloat("rawtime", i, (float) hit.get_rawtime());
                bankHits.setShort("cluster", i, (short) hit.get_cluster());
                bankHits.setShort("xtalk",   i, (short) hit.get_xtalk());
                bankHits.setShort("duration",i, (short) hit.get_duration());
            }
            event.appendBanks(bankHits);
        }

    }

    // ----------------
    private void write_ClusterBank(DataEvent event, RICHEvent richevent) {
    // ----------------

        int debugMode = 0;

        int NCLU = richevent.get_nClu();
        if(debugMode>=1)System.out.format("Creating Bank for %5d Clusters \n", NCLU);

        if(NCLU>0) {
            if(debugMode>=2)System.out.println(" --> Creating the Clusters Bank ");
            DataBank bankCluster = event.createBank("RICH::clusters", NCLU);
            if(bankCluster==null){
                System.out.println("ERROR CREATING BANK : RICH::clusters");
                return;
            }

            for(int i = 0; i < NCLU; i++){

                RICHCluster clu = richevent.get_Cluster(i);

                bankCluster.setShort("id",      i, (short) clu.get_id());
                bankCluster.setShort("size",    i, (short) clu.get_size());
                bankCluster.setShort("sector",  i, (short) clu.get(0).get_sector());
                bankCluster.setShort("tile",    i, (short) clu.get(0).get_tile());
                bankCluster.setShort("pmt",     i, (short) clu.get(0).get_pmt());
                bankCluster.setFloat("charge",  i, (float) clu.get_charge());
                bankCluster.setFloat("time",    i, (float) clu.get_time());
                bankCluster.setFloat("rawtime", i, (float) clu.get_rawtime());
                bankCluster.setFloat("x",       i, (float) (clu.get_x()));
                bankCluster.setFloat("y",       i, (float) (clu.get_y()));
                bankCluster.setFloat("z",       i, (float) clu.get_z());
                bankCluster.setFloat("wtime",   i, (float) clu.get_wtime());
                bankCluster.setFloat("wx",      i, (float) clu.get_wx());
                bankCluster.setFloat("wy",      i, (float) clu.get_wy());
                bankCluster.setFloat("wz",      i, (float) clu.get_wz());
            }
            event.appendBanks(bankCluster);
        }
    }


    // ----------------
    public void write_HadronBank(DataEvent event, RICHEvent richevent) {
    // ----------------

        int debugMode = 0;

        int NHAD = richevent.get_nHad();
        if(debugMode>=1)System.out.format("Creating Bank for %5d Hadrons \n", NHAD);
        
        if(NHAD>0) {
            if(debugMode>=1)System.out.println(" --> Creating the Hadrons Bank ");
            DataBank bankHads = event.createBank("RICH::hadrons", NHAD);
            if(bankHads==null){
                System.out.println("ERROR CREATING BANK : RICH::hadrons");
                return;
            }

            for(int i = 0; i < NHAD; i++){

                RICHParticle had = richevent.get_Hadron(i);

                bankHads.setShort("id",i,(short) had.get_id());
                bankHads.setShort("hit_index",     i, (short) had.get_hit_index());
                bankHads.setShort("particle_index",i, (short) had.get_ParentIndex());

                bankHads.setFloat("traced_the",    i, (float) had.lab_theta);
                bankHads.setFloat("traced_phi",    i, (float) had.lab_phi);
                bankHads.setFloat("traced_hitx",   i, (float) had.meas_hit.x);
                bankHads.setFloat("traced_hity",   i, (float) had.meas_hit.y);
                bankHads.setFloat("traced_hitz",   i, (float) had.meas_hit.z);
                bankHads.setFloat("traced_time",   i, (float) had.traced.get_time());
                bankHads.setFloat("traced_path",   i, (float) had.traced.get_path());

                bankHads.setShort("traced_ilay",   i, (short) had.ilay_emission);
                bankHads.setShort("traced_ico",    i, (short) had.ico_emission);
                bankHads.setFloat("traced_emix",   i, (float) had.lab_emission.x);
                bankHads.setFloat("traced_emiy",   i, (float) had.lab_emission.y);
                bankHads.setFloat("traced_emiz",   i, (float) had.lab_emission.z);

                bankHads.setFloat("EtaC_ele",      i, (float) had.get_changle(0));
                bankHads.setFloat("EtaC_pi",       i, (float) had.get_changle(1));
                bankHads.setFloat("EtaC_k",        i, (float) had.get_changle(2));
                bankHads.setFloat("EtaC_pr",       i, (float) had.get_changle(3));
                bankHads.setFloat("EtaC_min",      i, (float) had.minChAngle());
                bankHads.setFloat("EtaC_max",      i, (float) had.maxChAngle());

            }
            event.appendBanks(bankHads);
        }

    }

    // ----------------
    public void write_RingCherBank(DataEvent event, RICHEvent richevent, RICHConstants recopar) {
    // ----------------

        int debugMode = 0;

        int NPHO = richevent.get_nPho();
        if(debugMode>=1)System.out.format("Creating Bank for %5d Ring Cherenkovs \n", NPHO);
        
        if(NPHO!=0) {
            if(debugMode>=1)System.out.println(" --> Creating the RICH::ringCher Bank ");
            DataBank bankRing = event.createBank("RICH::ringCher", NPHO);
            if(bankRing==null){
                System.out.println("ERROR CREATING BANK : RICH::ringCher");
                return;
            }

            for(int i = 0; i < NPHO; i++){

                RICHParticle pho = richevent.get_Photon(i);

                // only reconstructed photons
                if(pho.get_type()!=0) continue;
 
                RICHHit hit = richevent.get_Hit( pho.get_hit_index() );
                RICHParticle had = richevent.get_Hadron( pho.get_ParentIndex() );

                double htime = hit.get_time();
                double a_time = pho.get_start_time() + pho.analytic.get_time();
                double t_time = pho.get_start_time() + pho.traced.get_time();
                double chmi = had.minChAngle();
                double chma = had.maxChAngle();
                double dtma = recopar.RICH_TIME_RMS*3;

                double a_etaC = pho.analytic.get_EtaC();
                double t_etaC = pho.traced.get_EtaC();

                // skip no real Cherenkov solution
                if((a_etaC<chmi || a_etaC>chma) && (t_etaC<chmi || t_etaC>chma)) continue;
                if(Math.abs(a_time-htime)>dtma && Math.abs(t_time-htime)>dtma) continue;

                bankRing.setShort("id",     i, (short) pho.get_id());
                bankRing.setShort("hindex", i, (short) pho.get_hit_index());
                bankRing.setShort("pindex", i, (short) had.get_ParentIndex());

                bankRing.setFloat("apath",  i, (float) pho.analytic.get_path());
                bankRing.setFloat("atime",  i, (float) a_time );
                bankRing.setFloat("aEtaC",  i, (float) a_etaC );

                bankRing.setFloat("tpath",  i, (float) pho.traced.get_path());
                bankRing.setFloat("ttime",  i, (float) t_time );
                bankRing.setFloat("tEtaC",  i, (float) t_etaC );

            }
            event.appendBanks(bankRing);
        }
    }


    // ----------------
    public void write_PhotonBank(DataEvent event, RICHEvent richevent) {
    // ----------------

        int debugMode = 0;

        int NPHO = richevent.get_nPho();
        if(debugMode>=1)System.out.format("Creating Bank for %5d Photons \n", NPHO);
        
        if(NPHO!=0) {
            if(debugMode>=1)System.out.println(" --> Creating the Photons Bank ");
            DataBank bankPhos = event.createBank("RICH::photons", NPHO);
            if(bankPhos==null){
                System.out.println("ERROR CREATING BANK : RICH::photons");
                return;
            }

            for(int i = 0; i < NPHO; i++){

                RICHParticle pho = richevent.get_Photon(i);

                bankPhos.setShort("id",i, (short) pho.get_id());
                bankPhos.setShort("type",i, (short) pho.get_type());
                bankPhos.setShort("hit_index",i,(short) pho.get_hit_index());
                bankPhos.setShort("hadron_index",i,(short) pho.get_ParentIndex());
                bankPhos.setFloat("start_time",i,(float) pho.get_start_time());

                bankPhos.setFloat("analytic_the",i,(float) pho.analytic.get_theta());
                bankPhos.setFloat("analytic_phi",i,(float) pho.analytic.get_phi());
                bankPhos.setFloat("analytic_path",i,(float) pho.analytic.get_path());
                bankPhos.setFloat("analytic_time",i,(float) pho.analytic.get_time());
                //bankPhos.setShort("analytic_nrfl",i,(short) pho.analytic.get_nrefle());
                //bankPhos.setShort("analytic_nrfr",i,(short) pho.analytic.get_nrefra());

                bankPhos.setFloat("analytic_EtaC",i,(float) pho.analytic.get_EtaC());
                bankPhos.setFloat("analytic_aeron",i,(float) pho.analytic.get_aeron());
                bankPhos.setFloat("analytic_elpr",i,(float) pho.analytic.get_elprob());
                bankPhos.setFloat("analytic_pipr",i,(float) pho.analytic.get_piprob());
                bankPhos.setFloat("analytic_kpr",i,(float) pho.analytic.get_kprob());
                bankPhos.setFloat("analytic_prpr",i,(float) pho.analytic.get_prprob());
                bankPhos.setFloat("analytic_bgpr",i,(float) pho.analytic.get_bgprob());

                bankPhos.setFloat("traced_the",i,(float) pho.traced.get_theta());
                bankPhos.setFloat("traced_phi",i,(float) pho.traced.get_phi());
                bankPhos.setFloat("traced_hitx",i,(float) pho.traced.get_hit().x);
                bankPhos.setFloat("traced_hity",i,(float) pho.traced.get_hit().y);
                bankPhos.setFloat("traced_hitz",i,(float) pho.traced.get_hit().z);
                bankPhos.setFloat("traced_path",i,(float) pho.traced.get_path());
                bankPhos.setFloat("traced_time",i,(float) pho.traced.get_time());
                bankPhos.setShort("traced_nrfl",i,(short) pho.traced.get_nrefle());
                bankPhos.setShort("traced_nrfr",i,(short) pho.traced.get_nrefra());
                bankPhos.setShort("traced_1rfl",i,(short) pho.traced.get_firstrefle());

                bankPhos.setFloat("traced_EtaC",i,(float) pho.traced.get_EtaC());
                bankPhos.setFloat("traced_aeron",i,(float) pho.traced.get_aeron());
                bankPhos.setFloat("traced_elpr",i,(float) pho.traced.get_elprob());
                bankPhos.setFloat("traced_pipr",i,(float) pho.traced.get_piprob());
                bankPhos.setFloat("traced_kpr",i,(float) pho.traced.get_kprob());
                bankPhos.setFloat("traced_prpr",i,(float) pho.traced.get_prprob());
                bankPhos.setFloat("traced_bgpr",i,(float) pho.traced.get_bgprob());

            }
            event.appendBanks(bankPhos);
        }

    }

    // ----------------
    public DataBank getRichResponseBank(List<DetectorResponse> responses, DataEvent event, String bank_name, RICHConstants recopar){
    // ----------------

        int debugMode = 0;
        if(debugMode>=1)System.out.format("Saving match in RICH::response bank  Nmatches  %5d \n",responses.size());
        DataBank bank = event.createBank(bank_name, responses.size());
        for(int row = 0; row < responses.size(); row++){
            DetectorResponse r = (DetectorResponse) responses.get(row);

            bank.setShort("index", row, (short) r.getHitIndex());
            bank.setShort("pindex", row, (short) r.getAssociation());
            bank.setByte("detector", row, (byte) r.getDescriptor().getType().getDetectorId());
            bank.setByte("sector", row, (byte) r.getDescriptor().getSector());
            bank.setByte("layer", row, (byte) r.getDescriptor().getLayer());
            bank.setFloat("x", row, (float) r.getPosition().x());
            bank.setFloat("y", row, (float) r.getPosition().y());
            bank.setFloat("z", row, (float) r.getPosition().z());
            bank.setFloat("hx", row, (float) r.getMatchedPosition().x());
            bank.setFloat("hy", row, (float) r.getMatchedPosition().y());
            bank.setFloat("hz", row, (float) r.getMatchedPosition().z());
            bank.setFloat("path", row, (float) r.getPath());
            bank.setFloat("time", row, (float) r.getTime());
            bank.setFloat("energy", row, (float) r.getEnergy());
            float chi2 = (float) (2*Math.sqrt(Math.pow(r.getMatchedPosition().x()-r.getPosition().x(),2)+
                                  Math.pow(r.getMatchedPosition().y()-r.getPosition().y(),2)+
                                  Math.pow(r.getMatchedPosition().z()-r.getPosition().z(),2))/recopar.RICH_HITMATCH_RMS);
            bank.setFloat("chi2", row, (float) chi2);

            if(debugMode>=1)System.out.format("RICH::response id %3d   hit %6.1f %6.1f %6.1f    tk %6.1f %6.1f %6.1f chi2 %8.3f \n",
                                  r.getHitIndex(),
                                  r.getPosition().x(),r.getPosition().y(),r.getPosition().z(),
                                  r.getPosition().x()+(r.getMatchedPosition().x()-r.getPosition().x())*2,
                                  r.getPosition().y()+(r.getMatchedPosition().y()-r.getPosition().y())*2,
                                  r.getPosition().z()+(r.getMatchedPosition().z()-r.getPosition().z())*2,chi2);
        }
        return bank;
    }


}
