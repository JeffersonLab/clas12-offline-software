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
    private static double RAD = 180./Math.PI;
    private static double MRAD = 1000;

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
        if(event.hasBank("RICH::hadrons")){
            event.removeBank("RICH::hadrons");
            if(debugMode==1)System.out.format("Remove RICH::hadrons from event \n");
        }
        if(event.hasBank("RICH::photons")){
            event.removeBank("RICH::photons");
            if(debugMode==1)System.out.format("Remove RICH::photons from event \n");
        }
        if(event.hasBank("RICH::ringCher")){
            event.removeBank("RICH::ringCher");
            if(debugMode==1)System.out.format("Remove RICH::ringCher from event \n");
        }
        if(event.hasBank("RICH::hadCher")){
            event.removeBank("RICH::hadCher");
            if(debugMode==1)System.out.format("Remove RICH::hadCher from event \n");
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

        if(richevent.get_nPho()>0)write_PhotonBank(event, richevent, recopar);

        if(richevent.get_nPho()>0)write_RingCherBank(event, richevent, recopar);

        if(richevent.get_nHad()>0)write_HadCherBank(event, richevent, recopar);

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
                //bankHads.setFloat("traced_mchi2",  i, (float) had.traced.get_machi2());

                bankHads.setShort("traced_ilay",   i, (short) had.ilay_emission);
                bankHads.setShort("traced_ico",    i, (short) had.ico_emission);
                bankHads.setFloat("traced_emix",   i, (float) had.lab_emission.x);
                bankHads.setFloat("traced_emiy",   i, (float) had.lab_emission.y);
                bankHads.setFloat("traced_emiz",   i, (float) had.lab_emission.z);

                bankHads.setFloat("EtaC_ele",      i, (float) had.get_changle(0,0));
                bankHads.setFloat("EtaC_pi",       i, (float) had.get_changle(1,0));
                bankHads.setFloat("EtaC_k",        i, (float) had.get_changle(2,0));
                bankHads.setFloat("EtaC_pr",       i, (float) had.get_changle(3,0));
                bankHads.setFloat("EtaC_min",      i, (float) had.minChAngle(0));
                bankHads.setFloat("EtaC_max",      i, (float) had.maxChAngle(0));

            }
            event.appendBanks(bankHads);
        }

    }

    // ----------------
    public void write_HadCherBank(DataEvent event, RICHEvent richevent, RICHConstants recopar) {
    // ----------------

        int debugMode = 0;

        int NHAD = richevent.get_nHad();
        if(debugMode>=1)System.out.format("Creating Bank for %5d Hadrons \n", NHAD);

        if(NHAD>0) {
            if(debugMode>=1)System.out.println(" --> Creating the RICH::hadCher Bank ");
            DataBank bankHads = event.createBank("RICH::hadCher", NHAD);
            if(bankHads==null){
                System.out.println("ERROR CREATING BANK : RICH::hadCher");
                return;
            }

            for(int i = 0; i < NHAD; i++){

                RICHParticle had = richevent.get_Hadron(i);
                double dT_max = recopar.RICH_TIME_RMS*3;

                if(debugMode>0)System.out.format(" RICHio %7.2f %7.2f %5d %7.2f %9.4f %9.4f \n",had.minChAngle(0)*MRAD,had.maxChAngle(0)*MRAD,
                          had.traced.get_BestH(),had.traced.get_RQP(),had.traced.get_ElProb(),had.traced.get_PiProb());

                if(had.get_id()>255)continue;
                if(had.get_hit_index()>255)continue;
                if(had.get_ParentIndex()>255)continue;
                if(had.ilay_emission>255)continue;
                if(had.ico_emission>255)continue;
                if(had.ico_entrance>255)continue;
                if(had.iqua_emission>255)continue;
                if(had.traced.get_BestH()>255)continue;

                bankHads.setByte("id",             i ,(byte) had.get_id());
                bankHads.setByte("hindex",         i, (byte) had.get_hit_index());
                bankHads.setByte("pindex",         i, (byte) had.get_ParentIndex());

                bankHads.setByte("emilay",         i, (byte) had.ilay_emission);
                bankHads.setByte("emico",          i, (byte) had.ico_emission);
                bankHads.setByte("enico",          i, (byte) had.ico_entrance);
                bankHads.setShort("emqua",         i, (short) had.iqua_emission);
                bankHads.setFloat("mchi2",         i, (float) had.traced.get_machi2());

                bankHads.setFloat("ch_min",        i, (float) had.minChAngle(0));
                bankHads.setFloat("ch_max",        i, (float) had.maxChAngle(0));
                bankHads.setFloat("dt_max",        i, (float) dT_max);
                bankHads.setFloat("ch_dir",        i, (float) had.traced.get_Chdir());
                bankHads.setFloat("ch_lat",        i, (float) had.traced.get_Chlat());
                bankHads.setFloat("ch_spe",        i, (float) had.traced.get_Chspe());

                bankHads.setByte("best_PID",       i, (byte) had.traced.get_BestH());
                bankHads.setFloat("RQ_prob",       i, (float) had.traced.get_RQP());
                bankHads.setFloat("el_prob",       i, (float) had.traced.get_ElProb());
                bankHads.setFloat("pi_prob",       i, (float) had.traced.get_PiProb());
                bankHads.setFloat("k_prob",        i, (float) had.traced.get_KProb());
                bankHads.setFloat("pr_prob",       i, (float) had.traced.get_PrProb());
            }
            event.appendBanks(bankHads);
        }

    }

    // ----------------
    public void write_RingCherBank(DataEvent event, RICHEvent richevent, RICHConstants recopar) {
    // ----------------

        int debugMode = 0;

        int SELE = 2;

        int NPHO = richevent.get_nPho();
        if(debugMode>=1)System.out.format("Creating Bank for %5d Ring Cherenkovs \n", NPHO);
        
        if(NPHO!=0) {

            int Nring = 0;

            for(int i = 0; i < NPHO; i++){
                RICHParticle pho = richevent.get_Photon(i);
                if(pho.get_type()!=0) continue;
                if(pho.analytic.get_OK()==SELE || pho.traced.get_OK()==SELE) Nring++;
            }

            if(debugMode>=1)System.out.format(" --> Creating the RICH::ringCher Bank for Npho %5d Nring %5d \n",NPHO,Nring);
            DataBank bankRing = event.createBank("RICH::ringCher", Nring);
            if(bankRing==null){
                System.out.println("ERROR CREATING BANK : RICH::ringCher");
                return;
            }


            int ientry = 0;
            for(int i = 0; i < NPHO; i++){

                RICHParticle pho = richevent.get_Photon(i);

                // consistency
                if(ientry>Nring) continue;

                // only reconstructed photons
                if(pho.get_type()!=0) continue;
 
                RICHHit hit = richevent.get_Hit( pho.get_hit_index() );
                RICHParticle had = richevent.get_Hadron( pho.get_ParentIndex() );

                double htime = hit.get_time();
                double a_time = pho.get_start_time() + pho.analytic.get_time();
                double t_time = pho.get_start_time() + pho.traced.get_time();

                double a_etaC = pho.analytic.get_EtaC();
                double t_etaC = pho.traced.get_EtaC();

                if(debugMode>=1)System.out.format(" pho %3d %3d flag %3d %3d ",i,ientry,pho.analytic.get_OK(),pho.traced.get_OK());
                // skip no real Cherenkov solution
                if(pho.analytic.get_OK()==SELE || pho.traced.get_OK()==SELE){

                    if(had.get_ParentIndex()>255)continue;
                    if(hit.get_anode()>255)continue;
                    if(pho.traced.get_nrefle()>255)continue;

                    if(debugMode>=1)System.out.format("   --> store \n");

                    bankRing.setShort("id",     ientry, (short) pho.get_id());
                    bankRing.setShort("hindex", ientry, (short) pho.get_hit_index());
                    bankRing.setByte("pindex",  ientry, (byte) had.get_ParentIndex());

                    bankRing.setShort("pmt",    ientry, (short) hit.get_pmt());
                    bankRing.setByte("anode",   ientry, (byte) hit.get_anode());
                    bankRing.setFloat("time",   ientry, (float) htime);

                    bankRing.setFloat("apath",  ientry, (float) pho.analytic.get_path());
                    bankRing.setFloat("atime",  ientry, (float) a_time );
                    bankRing.setFloat("aEtaC",  ientry, (float) a_etaC );

                    bankRing.setFloat("tpath",  ientry, (float) pho.traced.get_path());
                    bankRing.setFloat("ttime",  ientry, (float) t_time );
                    bankRing.setFloat("tEtaC",  ientry, (float) t_etaC );

                    bankRing.setByte("nrfl",    ientry, (byte) pho.traced.get_nrefle());
                    bankRing.setShort("1rfl",   ientry, (short) pho.traced.get_FirstRefle());

                    ientry++;

                }else{
                    if(debugMode>=1)System.out.format(" \n");
                }

            }
            event.appendBanks(bankRing);
        }
    }


    // ----------------
    public void write_PhotonBank(DataEvent event, RICHEvent richevent, RICHConstants recopar) {
    // ----------------

        int debugMode = 0;

        int NPHO = richevent.get_nPho();
        if(debugMode>=1)System.out.format("Creating Bank for %5d Photons \n", NPHO);
        
        if(NPHO!=0) {

            int Nrow = 0;

            for(int i = 0; i < NPHO; i++){
                RICHParticle pho = richevent.get_Photon(i);
                if(pho.get_type()==0 || recopar.SAVE_THROWS==1)Nrow++;
            }

            if(debugMode>=1)System.out.format(" --> Creating the Photons Bank for Npho %4d Nrow %4d \n",NPHO,Nrow);
            DataBank bankPhos = event.createBank("RICH::photons", Nrow);
            if(bankPhos==null){
                System.out.println("ERROR CREATING BANK : RICH::photons");
                return;
            }

            int ientry = 0;

            for(int i = 0; i < NPHO; i++){

                RICHParticle pho = richevent.get_Photon(i);
                if(debugMode>=1)System.out.format(" pho %3d %3d type %3d %3d ",i,ientry,pho.get_type(),pho.get_ParentIndex());

                // consistency
                if(ientry>Nrow) continue;

                if(pho.get_type()==0 || recopar.SAVE_THROWS==1){
    
                    if(debugMode>=1)System.out.format("   --> store \n");

                    bankPhos.setShort("id",             ientry, (short) pho.get_id());
                    bankPhos.setShort("type",           ientry, (short) pho.get_type());
                    bankPhos.setShort("hit_index",      ientry,(short) pho.get_hit_index());
                    bankPhos.setShort("hadron_index",   ientry,(short) pho.get_ParentIndex());
                    bankPhos.setFloat("start_time",     ientry,(float) pho.get_start_time());

                    bankPhos.setFloat("analytic_the",   ientry,(float) pho.analytic.get_theta());
                    bankPhos.setFloat("analytic_phi",   ientry,(float) pho.analytic.get_phi());
                    bankPhos.setFloat("analytic_path",  ientry,(float) pho.analytic.get_path());
                    bankPhos.setFloat("analytic_time",  ientry,(float) pho.analytic.get_time());
                    //bankPhos.setShort("analytic_nrfl",ientry,(short) pho.analytic.get_nrefle());
                    //bankPhos.setShort("analytic_nrfr",ientry,(short) pho.analytic.get_nrefra());

                    bankPhos.setFloat("analytic_EtaC",  ientry,(float) pho.analytic.get_EtaC());
                    bankPhos.setFloat("analytic_aeron", ientry,(float) pho.analytic.get_aeron());
                    bankPhos.setFloat("analytic_elpr",  ientry,(float) pho.analytic.get_ElProb());
                    bankPhos.setFloat("analytic_pipr",  ientry,(float) pho.analytic.get_PiProb());
                    bankPhos.setFloat("analytic_kpr",   ientry,(float) pho.analytic.get_KProb());
                    bankPhos.setFloat("analytic_prpr",  ientry,(float) pho.analytic.get_PrProb());
                    bankPhos.setFloat("analytic_bgpr",  ientry,(float) pho.analytic.get_BgProb());

                    bankPhos.setFloat("traced_the",     ientry,(float) pho.traced.get_theta());
                    bankPhos.setFloat("traced_phi",     ientry,(float) pho.traced.get_phi());
                    bankPhos.setFloat("traced_hitx",    ientry,(float) pho.traced.get_hit().x);
                    bankPhos.setFloat("traced_hity",    ientry,(float) pho.traced.get_hit().y);
                    bankPhos.setFloat("traced_hitz",    ientry,(float) pho.traced.get_hit().z);
                    bankPhos.setFloat("traced_path",    ientry,(float) pho.traced.get_path());
                    bankPhos.setFloat("traced_time",    ientry,(float) pho.traced.get_time());
                    bankPhos.setShort("traced_nrfl",    ientry,(short) pho.traced.get_nrefle());
                    bankPhos.setShort("traced_nrfr",    ientry,(short) pho.traced.get_nrefra());
                    bankPhos.setShort("traced_1rfl",    ientry,(short) pho.traced.get_FirstRefle());

                    bankPhos.setFloat("traced_EtaC",    ientry,(float) pho.traced.get_EtaC());
                    bankPhos.setFloat("traced_aeron",   ientry,(float) pho.traced.get_aeron());
                    bankPhos.setFloat("traced_elpr",    ientry,(float) pho.traced.get_ElProb());
                    bankPhos.setFloat("traced_pipr",    ientry,(float) pho.traced.get_PiProb());
                    bankPhos.setFloat("traced_kpr",     ientry,(float) pho.traced.get_KProb());
                    bankPhos.setFloat("traced_prpr",    ientry,(float) pho.traced.get_PrProb());
                    bankPhos.setFloat("traced_bgpr",    ientry,(float) pho.traced.get_BgProb());

                    ientry++;

                  }else{
                    if(debugMode>=1)System.out.format(" \n");
                  }

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
