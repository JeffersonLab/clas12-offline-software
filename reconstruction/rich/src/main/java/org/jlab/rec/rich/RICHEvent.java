package org.jlab.rec.rich;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.detector.geom.RICH.RICHRay;
import org.jlab.detector.geom.RICH.RICHGeoConstants;

public class RICHEvent {

    /**
     * A RICH Event is the collected information of the event
     */

    private int runID;
    private int eventID;
    private float eventTime;
    private long CPUTime;
    private int  phase;

    private static double MRAD = 1000.;
    private static double RAD = 180./Math.PI;

    private ArrayList<RICHCluster>       clusters  = new ArrayList<RICHCluster>();
    private ArrayList<RICHHit>               hits  = new ArrayList<RICHHit>();

    private ArrayList<DetectorResponse>   resclus  = new ArrayList<DetectorResponse>();
    private ArrayList<DetectorResponse>   reshits  = new ArrayList<DetectorResponse>();
    private ArrayList<DetectorResponse>   matches  = new ArrayList<DetectorResponse>();

    private ArrayList<RICHParticle> hadrons = new ArrayList<RICHParticle>();
    private ArrayList<RICHParticle> photons = new ArrayList<RICHParticle>();

    
    // constructor
    // ----------------
    public RICHEvent() {
    // ----------------
        
    }

    // ----------------
    public void clear(){
    // ----------------

        this.clusters.clear();
        this.hits.clear();

        this.hadrons.clear();
        this.photons.clear();

        this.resclus.clear();
        this.reshits.clear();
        this.matches.clear();

    }


    // ----------------
    public void set_RunID(int ID) { runID = ID; }
    // ----------------

    // ----------------
    public void set_EventID(int ID) { eventID = ID; }
    // ----------------

    // ----------------
    public void set_EventTime(float time) { eventTime = time; }
    // ----------------

    // ----------------
    public void set_CPUTime(long CPUTime) { this.CPUTime = CPUTime; }
    // ----------------

    //------------------------------
    public int getFTOFphase() {return phase;}
    //------------------------------

    // ----------------
    public void add_Hit(RICHHit hit){ hits.add(hit); }
    // ----------------

    // ----------------
    public void add_Cluster(RICHCluster cluster){ clusters.add(cluster); }
    // ----------------

    // ----------------
    public void add_Photon(RICHParticle photon){ photons.add(photon); }
    // ----------------

    // ----------------
    public void add_Hadron(RICHParticle hadron){ hadrons.add(hadron); }
    // ----------------

    // ----------------
    public void add_ResClu(DetectorResponse resclu){ resclus.add(resclu); }
    // ----------------

    // ----------------
    public void add_ResHit(DetectorResponse reshit){ reshits.add(reshit); }
    // ----------------

    // ----------------
    public void add_Match(DetectorResponse match){ matches.add(match); }
    // ----------------

    // ----------------
    public void add_Clusters(ArrayList<RICHCluster> res){if(res!=null) for (RICHCluster clu: res) clusters.add(clu); }
    // ----------------

    // ----------------
    public void add_Hits(ArrayList<RICHHit> res){if(res!=null) for (RICHHit hit: res) hits.add(hit); }
    // ----------------

    // ----------------
    public void add_Photons(ArrayList<RICHParticle> phos){if(phos!=null) for (RICHParticle par: phos) photons.add(par); }
    // ----------------

    // ----------------
    public void add_Hadron(ArrayList<RICHParticle> hads){if(hads!=null) for (RICHParticle par: hads) hadrons.add(par); }
    // ----------------

    // ----------------
    public void add_ResClus(ArrayList<DetectorResponse> resps){if(resps!=null) for (DetectorResponse res: resps) resclus.add(res); }
    // ----------------

    // ----------------
    public void add_ResHits(ArrayList<DetectorResponse> resps){if(resps!=null) for (DetectorResponse res: resps) reshits.add(res); }
    // ----------------

    // ----------------
    public void add_Matches(ArrayList<DetectorResponse> resps){if(resps!=null) for (DetectorResponse res: resps) matches.add(res); }
    // ----------------

    // ----------------
    public int get_RunID() {return runID;}
    // ----------------

    // ----------------
    public int get_EventID() {return eventID;}
    // ----------------

    // ----------------
    public float get_EventTime() {return eventTime;}
    // ----------------

    // ----------------
    public long get_CPUTime() {return CPUTime;}
    // ----------------

    //------------------------------
    public void setFTOFphase(int phase) { this.phase = phase; }
    //------------------------------

    // ----------------
    public RICHCluster get_Cluster(int i){ return  clusters.get(i); }
    // ----------------

    // ----------------
    public RICHHit get_Hit(int i){ return  hits.get(i); }
    // ----------------

    // ----------------
    public RICHParticle get_Photon(int i){ return  photons.get(i); }
    // ----------------

    // ----------------
    public RICHParticle get_Hadron(int i){ return  hadrons.get(i); }
    // ----------------

    // ----------------
    public DetectorResponse get_ResClu(int i){ return  resclus.get(i); }
    // ----------------

    // ----------------
    public DetectorResponse get_ResHit(int i){ return  reshits.get(i); }
    // ----------------

    // ----------------
    public DetectorResponse get_Match(int i){ return  matches.get(i); }
    // ----------------

    // ----------------
    public ArrayList<RICHCluster> get_Clusters(){ return  clusters; }
    // ----------------

    // ----------------
    public ArrayList<RICHHit> get_Hits(){ return  hits; }
    // ----------------

    // ----------------
    public ArrayList<RICHParticle> get_Photons(){ return  photons; }
    // ----------------

    // ----------------
    public ArrayList<RICHParticle> get_Hadrons(){ return  hadrons; }
    // ----------------

    // ----------------
    public ArrayList<DetectorResponse> get_ResClus(){ return  resclus; }
    // ----------------

    // ----------------
    public ArrayList<DetectorResponse> get_ResHits(){ return  reshits; }
    // ----------------

    // ----------------
    public ArrayList<DetectorResponse> get_Matches(){ return  matches; }
    // ----------------

    // ----------------
    public int get_nClu() { return  clusters.size(); }
    // ----------------

    // ----------------
    public int get_nHit() { return  hits.size(); }
    // ----------------

    // ----------------
    public int get_nHad() { return  hadrons.size(); }
    // ----------------

    // ----------------
    public int get_nPho() { return  photons.size(); }
    // ----------------

    // ----------------
    public int get_nResClu() { return  resclus.size(); }
    // ----------------

    // ----------------
    public int get_nResHit() { return  reshits.size(); }
    // ----------------

    // ----------------
    public int get_nMatch() { return  matches.size(); }
    // ----------------


    // ----------------
    public void select_Signals() { 
    // ----------------

        int debugMode = 0;

        int NHIT = hits.size();
        int NCLU = clusters.size();
        if(debugMode>=1)System.out.format("Selecting Signals (%4d HITs and %4d CLUs) \n", NHIT,NCLU);

        if(NHIT>0 || NCLU>0) {

            int nsig = 0;
            int one  = 1;

            for(int i = 0; i < NHIT; i++){
                RICHHit hit = get_Hit(i);
                if(hit.get_cluster()!=0)continue;
                if(hit.get_xtalk()!=0) continue; 
                if(hit.get_status()!=0 && hit.get_status()!=5)continue;
                hit.set_signal(one);
                if(debugMode>=1)System.out.format("  --> hit %3d %7.2f signal %3d \n",i,hit.get_Time(),nsig);
                nsig++;
            }
            for(int i = 0; i < NCLU; i++){
                RICHCluster clu = get_Cluster(i);
                clu.set_signal(one);
                if(debugMode>=1)System.out.format("  --> clu %3d %7.2f signal %3d \n",i,clu.get_time(),nsig);
                nsig++;
            }
        }
    }


    // ----------------
    public int count_Signals() {
    // ----------------

        int debugMode = 0;

        int nsig = 0;
        for( RICHHit hit: hits) if(hit.get_signal()>0)nsig++;
        for( RICHCluster clu: clusters) if(clu.get_signal()>0) nsig++;

        return nsig;
    }


    // ----------------
    public void get_ChMean(RICHParticle hadron, int hypo, int recotype) {
    // ----------------

        int debugMode = 0;
        int SELE = 11;

        if(hypo<0 || hypo>=RICHConstants.N_HYPO ) return; 
        if(debugMode>=1)System.out.format("Hypo %s: Ch Mean calc for reco %d \n",RICHConstants.HYPO_LUND[hypo], recotype);

        int update = 1;
        while (update==1) { 

            update     = 0;
            int    nea = 0;
            double mea = 0.0;
            double sea = 0.0;
            for( RICHParticle pho: photons) {
                if(pho.get_type()==hypo && pho.get_ParentIndex() == hadron.get_id()){


                    if(debugMode>=1)System.out.format("calc mean for photon %d ",pho.get_id());
                    RICHSolution reco = new RICHSolution(); 
                    if(recotype==0) reco = pho.analytic;
                    if(recotype==1) reco = pho.traced;
                    double etac = reco.get_EtaC();

                    if(reco.get_OK()==SELE){
                        if(debugMode>=1)System.out.format(" etac %7.2f \n",etac*MRAD);
                        nea++;
                        mea += etac;
                        sea += etac*etac;
                    }else{
                        if(debugMode>=1)System.out.format(" -> no\n");
                    }
                }

            }

            if(nea>0){
                mea = mea /nea;
                sea = Math.sqrt( sea/nea - mea*mea );
            }
            if(debugMode>=1)System.out.format(" mean etac %7.2f %7.2f \n",mea*MRAD,sea*MRAD);

            for( RICHParticle pho: photons) {
                if(pho.get_type()==hypo && pho.get_ParentIndex() == hadron.get_id()){

                    RICHSolution reco = new RICHSolution(); 
                    if(recotype==0) reco = pho.analytic;
                    if(recotype==1) reco = pho.traced;
                    double etac = reco.get_EtaC();
                    double chi = mea - 3 *sea;
                    double cha = mea + 3 *sea;

                    if(reco.get_OK()==SELE && ( etac < chi || etac > cha )) {
                        //reco.set_OK(1);
                        update = 1;
                        if(debugMode>=1)System.out.format(" reject photon %d  with etac %7.2f vs [%7.2f : %7.2f] \n",pho.get_id(),etac*MRAD,chi*MRAD,cha*MRAD);
                    }
                }
            }

        }
        if(debugMode>=1)System.out.format(" Done with average \n");

        int ndir = 0;
        int nlat = 0;
        int nspe = 0;
        int ntot = 0;
        double chdir = 0.0;
        double chlat = 0.0;
        double chspe = 0.0;
        double chtot = 0.0;
        double sdir = 0.0;
        double slat = 0.0;
        double sspe = 0.0;
        double stot = 0.0;
        for( RICHParticle pho: photons) {
            if(pho.get_type()==hypo && pho.get_ParentIndex() == hadron.get_id()){

                RICHSolution reco = new RICHSolution();
                if(recotype==0) reco = pho.analytic;
                if(recotype==1) reco = pho.traced;
                if(reco.get_OK()!=SELE) continue;

                double etac = reco.get_EtaC();

                int irefle = reco.get_RefleType();
                if(irefle<0 || irefle>2) continue;

                chtot+=etac;
                stot+=etac*etac;
                ntot++;

                if(irefle==0){
                    chdir+=etac;
                    sdir+=etac*etac;
                    ndir++;
                }else{
                    if(irefle==2){
                        chspe+=etac;
                        sspe+=etac*etac;
                        nspe++;
                    }else{
                        chlat+=etac;
                        slat+=etac*etac;
                        nlat++;
                    }
                }
            }
        }

        if(ntot>0){
            chtot = chtot /ntot;
            stot = Math.sqrt( stot/ntot - chtot*chtot );
        }
        if(ndir>0){
            chdir = chdir /ndir;
            sdir = Math.sqrt( sdir/ndir - chdir*chdir );
        }
        if(nlat>0){
            chlat = chlat /nlat;
            slat = Math.sqrt( slat/nlat - chlat*chlat );
        }
        if(nspe>0){
            chspe = chspe /nspe;
            sspe = Math.sqrt( sspe/nspe - chspe*chspe );
        }

        RICHSolution hreco = null;
        String sreco = null;
        if(recotype==0) {hreco = hadron.analytic; sreco = "ALI";}
        if(recotype==1) {hreco = hadron.traced; sreco = "TRA";}

        ntot = ndir+nlat+nspe;
        chtot = 0.0;
        stot = 0.0;
        double sstot = 0.0;
        if(sdir>0) {stot+=1/Math.pow(sdir,2); sstot+=1/Math.pow(sdir,2)*(ndir-1); chtot+=chdir/Math.pow(sdir,2);}
        if(slat>0) {stot+=1/Math.pow(slat,2); sstot+=1/Math.pow(slat,2)*(nlat-1); chtot+=chlat/Math.pow(slat,2);}
        if(sspe>0) {stot+=1/Math.pow(sspe,2); sstot+=1/Math.pow(sspe,2)*(nspe-1); chtot+=chspe/Math.pow(sspe,2);}
        if(stot>0){
            chtot = chtot/stot;
            stot = 1/Math.sqrt(stot);
            sstot = 1/Math.sqrt(sstot);
        }

        int hypo_pid = RICHConstants.HYPO_LUND[hypo];

        double madir = 0.0;
        double c2dir = 0.0;
        double ssdir = 0.0;
        if(chdir>0){
            double refdir  = hadron.get_chindex(hypo_pid,0);
            double det = Math.pow((refdir*Math.cos(chdir)),2) -1;
            madir = hadron.get_momentum() * hadron.get_momentum() * det;
            if(debugMode>=1)System.out.format(" Refdir %7.4f \n",refdir);

            ssdir = sdir/Math.sqrt(ndir-1);
            c2dir = Math.abs(chdir - hadron.changle(hypo_pid,0)) / ssdir;
        }

        hreco.set_Ndir(ndir);
        hreco.set_Chdir(chdir);
        hreco.set_RMSdir(sdir);
        hreco.set_Madir(madir);

        double malat = 0.0;
        double c2lat = 0.0;
        double sslat = 0.0;
        if(chlat>0){
            double reflat  = hadron.get_chindex(hypo_pid,1);
            double det = Math.pow((reflat*Math.cos(chlat)),2) -1;
            malat = hadron.get_momentum() * hadron.get_momentum() * det;
            if(debugMode>=1)System.out.format(" Reflat %7.4f \n",reflat);

            sslat = slat/Math.sqrt(nlat-1);
            c2lat = Math.abs(chlat - hadron.changle(hypo_pid,1)) / sslat;
        }

        hreco.set_Nlat(nlat);
        hreco.set_Chlat(chlat);
        hreco.set_RMSlat(slat);
        hreco.set_Malat(malat);

        double maspe = 0.0;
        double c2spe = 0.0;
        double ssspe = 0.0;
        if(chspe>0){
            double refspe  = hadron.get_chindex(hypo_pid,2);
            double det = Math.pow((refspe*Math.cos(chspe)),2) -1;
            maspe = hadron.get_momentum() * hadron.get_momentum() * det;
            if(debugMode>=1)System.out.format(" Refspe %7.4f \n",refspe);

            ssspe = sspe/Math.sqrt(nspe-1);
            c2spe = Math.abs(chspe - hadron.changle(hypo_pid,2)) / ssspe;
        }

        hreco.set_Nspe(nspe);
        hreco.set_Chspe(chspe);
        hreco.set_RMSspe(sspe);
        hreco.set_Maspe(maspe);

        double matot = 0.0;
        double c2tot = 0.0;
        if(chtot>0){
            double reftot  = hadron.get_chindex(hypo_pid,0);
            double det = Math.pow((reftot*Math.cos(chtot)),2) -1;
            matot = hadron.get_momentum() * hadron.get_momentum() * det;
            if(debugMode>=2)System.out.format(" Reftot %7.4f \n",reftot);

            c2tot = Math.abs(chtot - hadron.changle(hypo_pid,0)) / sstot;
        }

        hreco.set_Ntot(ntot);
        hreco.set_Chtot(chtot);
        hreco.set_RMStot(stot);
        hreco.set_Matot(matot);

        if(debugMode>=1)
            System.out.format("%d  %s:  %s %7.2f %7.2f %5d  %7.4f \n", eventID,
                  sreco,RICHConstants.HYPO_LUND[hypo],hadron.get_momentum(),hadron.lab_theta*RAD,hadron.get_CLASpid(),hadron.refi_emission);
            System.out.format("%d    --> dir %d %7.2f %7.2f %7.2f %7.2f %8.5f %8.5f \n",eventID,
                  ndir,hadron.changle(hypo_pid,0)*MRAD,chdir*MRAD,sdir*MRAD,ssdir*MRAD,c2dir,madir);
            System.out.format("%d    --> lat %d %7.2f %7.2f %7.2f %7.2f %8.5f %8.5f \n",eventID,
                  nlat,hadron.changle(hypo_pid,1)*MRAD,chlat*MRAD,slat*MRAD,sslat*MRAD,c2lat,malat);
            System.out.format("%d    --> spe %d %7.2f %7.2f %7.2f %7.2f %8.5f %8.5f \n",eventID,
                  nspe,hadron.changle(hypo_pid,2)*MRAD,chspe*MRAD,sspe*MRAD,ssspe*MRAD,c2spe,maspe);
            System.out.format("%d    --> tot %d %7.2f %7.2f %7.2f %7.2f %8.5f %8.5f \n",eventID,
                  ntot,hadron.changle(hypo_pid,0)*MRAD,chtot*MRAD,stot*MRAD,sstot*MRAD,c2tot,matot);

    }
     

    // ----------------
    public void get_HypoPID(RICHParticle hadron, int recotype, RICHParameters richpar) {
    // ----------------

        int debugMode = 0;

        int n_tot[]      = {0, 0, 0, 0};     // photons in likelihood
        int n_sig[]      = {0, 0, 0, 0};     // signal photons 
        int n_bck[]      = {0, 0, 0, 0};     // background photons
        int n_dir[]      = {0, 0, 0, 0};
        int n_lat[]      = {0, 0, 0, 0};
        int n_spe[]      = {0, 0, 0, 0};

        double n_exp[]   = {0.0, 0.0, 0.0, 0.0};
        double c2_sig[]  = {0.0, 0.0, 0.0, 0.0};
        double lh_sig[]  = {0.0, 0.0, 0.0, 0.0};
        double lh_ref[]  = {0.0, 0.0, 0.0, 0.0};
        double lh_dnn[]  = {0.0, 0.0, 0.0, 0.0};
        double lh_all[]  = {0.0, 0.0, 0.0, 0.0};

        double ch_sig[]  = {0.0, 0.0, 0.0, 0.0};
        double ch_dir[]  = {0.0, 0.0, 0.0, 0.0};
        double ch_lat[]  = {0.0, 0.0, 0.0, 0.0};
        double ch_spe[]  = {0.0, 0.0, 0.0, 0.0};
        double ma_sig[]  = {0.0, 0.0, 0.0, 0.0};
        double ma_dir[]  = {0.0, 0.0, 0.0, 0.0};
        double ma_lat[]  = {0.0, 0.0, 0.0, 0.0};
        double ma_spe[]  = {0.0, 0.0, 0.0, 0.0};

        double ch_had   = 0.0;

        for (int hypo=0; hypo<RICHConstants.N_HYPO; hypo++){

            int hypo_pid = RICHConstants.HYPO_LUND[hypo];
            if(debugMode>=2){
                System.out.format("------------------------ \n");
                System.out.format("Likelihood (PASS2) for reco %d hypo %s \n",recotype, RICHConstants.HYPO_STRING[hypo]);
                System.out.format("------------------------ \n");
            }

            for( RICHParticle pho: photons) {
                if(pho.get_type()==hypo && pho.get_ParentIndex() == hadron.get_id()){

                    RICHSolution reco = pho.traced;
                    if(recotype==0) reco = pho.analytic;

                    if(!reco.is_used()) {
                        if(debugMode>=2)System.out.format("pho %4d %3d %3d rejected %3d %3d \n",pho.get_id(),pho.get_ParentIndex(), pho.get_HitIndex(), reco.get_OK(), reco.status()); 
                        continue;
                    }

                    double etac    = reco.get_EtaC();
                    int    irefle  = reco.get_RefleType();
                    double Npho    = hadron.nchangle(hypo_pid, irefle);
                    double NphoEle = hadron.nchangle(11, irefle);

                    double htime   = pho.get_HitTime();
                    double t_time  = pho.get_StartTime() + pho.traced.get_time();

                    if(debugMode>=2)System.out.format("pho %4d %3d %3d (%3d) %8.4f [%8.4f]  %7.2f [%7.2f] %4d %7.2f %7.2f ",pho.get_id(), 
                        pho.get_ParentIndex(), pho.get_HitIndex(), reco.get_OK(), etac*MRAD, 
                        hadron.changle(hypo_pid,irefle)*MRAD, htime, t_time, irefle, NphoEle, Npho);

                    // prob for signal
                    double prob     = pho.calc_HypoYield(hadron, hypo_pid, recotype, Npho, 0);
                    double prob_ref = pho.calc_HypoYield(hadron, hypo_pid, recotype, Npho, 1);
                    double c2       = pho.calc_HypoC2(hadron, hypo_pid, recotype);

                    // ATT: why here and not when the photon is created ?
                    //reco.set_hypo(hypo_pid);
                    // This is for the single photon, assig_PID does for the particle
                    if(hypo==0) reco.set_ElProb(Math.log(1./prob));
                    if(hypo==1) reco.set_PiProb(Math.log(1./prob));
                    if(hypo==2) reco.set_KProb(Math.log(1./prob));
                    if(hypo==3) reco.set_PrProb(Math.log(1./prob));

                    double refind = hadron.get_chindex(hypo_pid,irefle);
                    double det    = Math.pow((refind*Math.cos(etac)),2) -1;
                    lh_sig[hypo] += 2*Math.log(1./prob);
                    lh_ref[hypo] += 2*Math.log(prob_ref);
                    c2_sig[hypo] += c2;
                    if(reco.get_OK()==11){
                        ch_sig[hypo] += 1.*etac;
                        n_sig[hypo]  += 1.;
                        if(irefle==0){
                            ch_dir[hypo] += 1.*etac;
                            ma_dir[hypo] += Math.pow(hadron.get_momentum(),2) * det;
                            n_dir[hypo]  += 1.;
                        }
                        if(irefle==1){
                            ch_lat[hypo] += 1.*etac;
                            ma_lat[hypo] += Math.pow(hadron.get_momentum(),2) * det;
                            n_lat[hypo]  += 1.;
                        }
                        if(irefle==2){
                            ch_spe[hypo] += 1.*etac;
                            ma_spe[hypo] += Math.pow(hadron.get_momentum(),2) * det;
                            n_spe[hypo]  += 1.;
                        }
                    }
                    n_tot[hypo]++;
                    if(reco.get_OK()>=100)n_bck[hypo]++;
                    if(debugMode>=2)System.out.format(" --> %10.4g %10.4g %10.4g %10.4g %10.4g %7.2f\n", 
                        prob, prob_ref, Math.log(1./prob), Math.log(prob_ref), Math.log(prob_ref/prob),c2);

                    if(debugMode>=3) pho.shortshow();
                }
            }

            if(richpar.USE_ELECTRON_ANGLES==1){
                for(int ir=0; ir<RICHConstants.N_PATH; ir++) n_exp[hypo] += hadron.nchangle(hypo_pid, ir);
            }else{
                n_exp[hypo] = hadron.nchangle(hypo_pid, 0);
            }
            if(richpar.USE_LIKE_DELTAN==1){
                lh_dnn[hypo] = 2* (n_exp[hypo] - (n_sig[hypo]-n_bck[hypo]));
                //lh_dnn[hypo] = 2* (n_exp[hypo] - n_sig[hypo]);
            }
            lh_all[hypo] = lh_sig[hypo]+lh_dnn[hypo];
            if(n_sig[hypo]>0) ch_sig[hypo] = ch_sig[hypo]/n_sig[hypo];
            if(n_dir[hypo]>0) ch_dir[hypo] = ch_dir[hypo]/n_dir[hypo];
            if(n_lat[hypo]>0) ch_lat[hypo] = ch_lat[hypo]/n_lat[hypo];
            if(n_spe[hypo]>0) ch_spe[hypo] = ch_spe[hypo]/n_spe[hypo];
            if(n_dir[hypo]>0 || n_lat[hypo]>0 || n_spe[hypo]>0) 
                ma_sig[hypo] = (ma_dir[hypo]+ma_lat[hypo]+ma_spe[hypo])/(n_dir[hypo]+n_lat[hypo]+n_spe[hypo]);

            if(debugMode>=2)System.out.format("Eve %d %7.2f raw like hypo %5d : %7.2f %4d %4d  %10.4g %7.2f  %7.2f %7.2f\n",
                eventID,hadron.get_momentum(),hypo_pid,n_exp[hypo],n_sig[hypo],n_bck[hypo],lh_sig[hypo],lh_dnn[hypo],ch_sig[hypo]*MRAD,c2_sig[hypo]);

        }

        RICHSolution hreco = hadron.traced;
        if(recotype==0)hreco = hadron.analytic;
        double newRQ = hreco.assign_HypoPID(lh_all);

        int best_hypo = -1;
        int zero = 0; 
        double bestRL = 0.0;
        double bestC2 = 0.0;
        if(hreco.get_BestH()>0){
            for (int hypo=0; hypo<RICHConstants.N_HYPO; hypo++){
                int hypo_pid = RICHConstants.HYPO_LUND[hypo];
                if(hypo_pid==hreco.get_BestH()){
                    // check the hypothesis has at least a signal candidate 
                    if(n_sig[hypo]==0) {
                        if(debugMode==1)System.out.format("Reset PID \n");
                        hreco.set_BestH(zero);
                        continue;
                    }
                    best_hypo = hypo;
                    if(n_tot[hypo]>0){
                        if(debugMode==1)System.out.format("Define reduced RL and C2\n");
                        bestRL = (lh_all[hypo]+lh_ref[hypo])/(2*n_tot[hypo]);
                        bestC2 = c2_sig[hypo]/(2*n_tot[hypo]);
                    }
                }
            }
        }

        hadron.set_RICHpid(hreco.get_BestH());
        if(best_hypo>=0){
            hreco.set_BestRL(bestRL);
            hreco.set_BestC2(bestC2);
            hreco.set_BestCH(ch_sig[best_hypo]);
            hreco.set_BestNpho(n_tot[best_hypo]);
            hreco.set_BestMass(ma_sig[best_hypo]);

            hreco.set_Ndir(n_dir[best_hypo]);
            hreco.set_Nlat(n_lat[best_hypo]);
            hreco.set_Nspe(n_spe[best_hypo]);
            hreco.set_Chdir(ch_dir[best_hypo]);
            hreco.set_Chlat(ch_lat[best_hypo]);
            hreco.set_Chspe(ch_spe[best_hypo]);
        }

        if(debugMode>=1){
            for (int hypo=0; hypo<RICHConstants.N_HYPO; hypo++){
                int hypo_pid = RICHConstants.HYPO_LUND[hypo];
                System.out.format(" [%5d] %5d [%7.2f] %4d %4d %4d %4d [%10.4g %10.4g %10.4g] --> %10.4f %10.4g (%10.4g %7.2f)\n",
                        hypo_pid, hreco.get_BestH(), n_exp[hypo], n_tot[hypo], n_sig[hypo], n_bck[hypo], n_spe[hypo],
                        lh_sig[hypo], lh_ref[hypo], lh_dnn[hypo], lh_all[hypo], (lh_all[hypo]+lh_ref[hypo]), (lh_all[hypo]+lh_ref[hypo])/(2*n_tot[hypo]), c2_sig[hypo]/(2*n_tot[hypo]));
            }

            String hstri = "Traced PID";
            if(recotype==0) hstri="Analytic PID";
            System.out.format("%s %5d %8d P %6.2f %7.2f PID [%5d] ",
                hstri,runID,eventID, hadron.get_momentum(), hadron.lab_theta*RAD, hadron.get_CLASpid());
            if(best_hypo>=0){
                int hypo_pid = RICHConstants.HYPO_LUND[best_hypo];
                //double c2r = 12.;
                //if(n_sig[best_hypo]>1) c2r = c2_sig[best_hypo]/(2*n_sig[best_hypo]);
                System.out.format("%5d %5d Npho %6d %6d %6d [%6.2f] Eta %7.2f [%7.2f]  C2 %7.2f [%7.2f]  RQ %7.3f %7.3f %4d %4d \n",
                    hadron.get_RICHpid(), hreco.get_secH(), 
                    n_sig[best_hypo], n_bck[best_hypo], n_spe[best_hypo], n_exp[best_hypo], ch_sig[best_hypo]*MRAD, hadron.changle(11,0)*MRAD,
                    bestRL, bestC2, newRQ, hreco.get_ReQP(),hadron.ilay_emission, hadron.ico_emission);
            }else{
                System.out.format("\n");
            }
        }

    }


    // ----------------
    public void get_LHCbpid(RICHParticle hadron, int recotype, RICHParameters richpar) {
    // ----------------

        int debugMode = 0;

        int n_sig[]      = {0, 0, 0, 0};
        double lh_sig[]  = {0.0, 0.0, 0.0, 0.0};
        double ch_sig[]  = {0.0, 0.0, 0.0, 0.0};

        double ch_had = 0.0;
        double FAC  = 1.0;

        for (int hypo=0; hypo<RICHConstants.N_HYPO; hypo++){

            int hypo_pid = RICHConstants.HYPO_LUND[hypo];
            if(debugMode>=1)System.out.format("Likelihood (LHCB) for reco %d hypo %s \n",recotype, RICHConstants.HYPO_STRING[hypo]);

            for( RICHParticle pho: photons) {
                if(pho.get_type()==hypo && pho.get_ParentIndex() == hadron.get_id()){

                    RICHSolution reco = pho.traced;
                    if(recotype==0) reco = pho.analytic;
                    if(!reco.is_used()) continue;

                    double etac = reco.get_EtaC();

                    if(debugMode>=1)System.out.format("calc prob for photon %d %7.2f \n",pho.get_id(), etac*MRAD);

                    double prob      = pho.pid_LHCb(hadron, hypo_pid, recotype, 0);
                    double prob_norm = pho.pid_LHCb(hadron, hypo_pid, recotype, 1);

                    double ratiolog = Math.log(prob)/Math.log(prob_norm);

                    if(hypo==0) reco.set_ElProb(ratiolog*FAC);
                    if(hypo==1) reco.set_PiProb(ratiolog*FAC);
                    if(hypo==2) reco.set_KProb(ratiolog*FAC);
                    if(hypo==3) reco.set_PrProb(ratiolog*FAC);
                    
                    lh_sig[hypo] += ratiolog;
                    ch_sig[hypo] += ratiolog*etac;
                    n_sig[hypo]++;
                    if(debugMode>=1)System.out.format(" --> %8.4f %8.4f %g \n", prob, prob_norm, ratiolog);

                    if(debugMode>=2) pho.shortshow();
                }
            }

            if(lh_sig[hypo]>0) ch_sig[hypo] = ch_sig[hypo]/lh_sig[hypo];
            if(debugMode>=1)System.out.format("raw like hypo %2d :  %4d %10.4g %7.2f \n",
                hypo,n_sig[hypo],lh_sig[hypo],ch_sig[hypo]*MRAD);
        }

        if(lh_sig[1]>0 || lh_sig[2]>0 || lh_sig[3]>0)
            ch_had=(ch_sig[1]*lh_sig[1] + ch_sig[2]*lh_sig[2] + ch_sig[3]*lh_sig[3])/(lh_sig[1] + lh_sig[2] + lh_sig[3]);


        for (int hypo=0; hypo<RICHConstants.N_HYPO; hypo++){
            //ATT: trick just for comparison
            lh_sig[hypo]*=FAC;
        }
        
        double newRQ = 0.0;
        RICHSolution hreco = new RICHSolution();
        String hstri = null;
        if(recotype==0) {hreco = hadron.analytic; hstri="ALI";}
        if(recotype==1) {hreco = hadron.traced; hstri="TRA";}

        newRQ = hreco.assign_LHCbPID(lh_sig);
        hadron.set_RICHpid(hreco.get_BestH());

        int best_hypo = -1;
        int sec_hypo = -1;
        double bestRL = 0.0;
        if(hreco.get_BestH()>0){
            for (int hypo=0; hypo<RICHConstants.N_HYPO; hypo++){
                int hypo_pid = RICHConstants.HYPO_LUND[hypo];
                if(hypo_pid==hreco.get_BestH())best_hypo = hypo;
                if(hypo_pid==hreco.get_secH())sec_hypo = hypo;
            }
            bestRL = (lh_sig[best_hypo]-lh_sig[sec_hypo])/(2*n_sig[best_hypo]-1);
            //bestRL = lh_sig[best_hypo]/(n_sig[best_hypo]-1);
        }
        if(debugMode>=1)System.out.format("assign %5d %3d %3d %7.3f %7.2f \n",hreco.get_BestH(),best_hypo,sec_hypo,newRQ,bestRL);

        if(best_hypo>-1){
            hreco.set_BestCH(ch_sig[best_hypo]);
            hreco.set_BestNpho(n_sig[best_hypo]);
            hreco.set_BestRL(bestRL);
        }
        if(best_hypo>-1 && debugMode>=1)
            System.out.format("%s eve %8d  mom %6.2f xy %7.2f %7.2f %7.2f %7.2f %8.2f %8.4f  Npho %5d %10.4g %3d %10.4g %3d %10.4g --> %8.5f %7.2f %7.2f %3d %3d\n", 
            hstri,eventID, hadron.get_momentum(), 
            hadron.direct_ray.origin().x(), hadron.direct_ray.origin().y(), hadron.get_HitPos().x(), hadron.get_HitPos().y(), 
            hadron.changle(best_hypo,0)*MRAD, hadron.refi_emission, n_sig[best_hypo], lh_sig[best_hypo], 
            hreco.get_BestH()*hadron.charge(), hreco.get_Bestprob(), hreco.get_secH(), hreco.get_secprob(), 
            newRQ, ch_sig[best_hypo]*MRAD, ch_had*MRAD, hadron.get_CLASpid(), hadron.get_RICHpid());

    }


    // ----------------
    public void get_pid(RICHParticle hadron, int recotype, RICHParameters richpar) {
    // ----------------

        int debugMode = 0;

        int n_sig[]      = {0, 0, 0, 0};
        double lh_sig[]  = {0.0, 0.0, 0.0, 0.0};
        double ch_sig[]  = {0.0, 0.0, 0.0, 0.0};

        int n_bg[]       = {0, 0, 0, 0};
        double lh_bg[]   = {0.0, 0.0, 0.0, 0.0};
        double ch_bg[]   = {0.0, 0.0, 0.0, 0.0};

        double ch_had = 0.0;
        double FAC  = 1.0;

        for (int hypo=0; hypo<RICHConstants.N_HYPO; hypo++){

            int hypo_pid = RICHConstants.HYPO_LUND[hypo];
            if(debugMode>=1)System.out.format("Likelihood (PASS1) for reco %d hypo %s \n",recotype, RICHConstants.HYPO_STRING[hypo]);

            double prob = 0.0;
            for( RICHParticle pho: photons) {
                if(pho.get_type()==hypo && pho.get_ParentIndex() == hadron.get_id()){

                    RICHSolution reco = pho.traced;
                    if(recotype==0) reco = pho.analytic;
                    if(!reco.is_used()) continue;

                    double etac = reco.get_EtaC();

                    if(debugMode>=1)System.out.format("calc prob for photon %d %7.2f ",pho.get_id(), etac*MRAD);

                    // prob for backgound
                    prob = pho.pid_probability(hadron, 0, recotype);
                    if(prob-1>=richpar.PIXEL_NOMINAL_DARKRATE){
                        lh_bg[hypo] += Math.log(prob);
                        ch_bg[hypo] += Math.log(prob)*etac;
                        n_bg[hypo]++;
                        if(debugMode>=2)System.out.format(" --> etac %8.4f for background %10.4g %10.4g \n", etac*MRAD, prob, Math.log(prob));
                    }else{
                        //System.out.format(" Wrong prob  for background %g \n",prob-1);
                    }

                    // prob for signal
                    prob = pho.pid_probability(hadron, hypo_pid, recotype);

                    if(hypo==0) reco.set_ElProb(Math.log(prob)*FAC);
                    if(hypo==1) reco.set_PiProb(Math.log(prob)*FAC);
                    if(hypo==2) reco.set_KProb(Math.log(prob)*FAC);
                    if(hypo==3) reco.set_PrProb(Math.log(prob)*FAC);

                    if(prob-1>richpar.PIXEL_NOMINAL_DARKRATE){
                        lh_sig[hypo] += Math.log(prob);
                        ch_sig[hypo] += Math.log(prob)*etac;
                        n_sig[hypo]++;
                        if(debugMode>=1)System.out.format(" --> etac %8.4f %g %g \n", etac*MRAD, prob, Math.log(prob));
                    }else{
                        if(debugMode>=1)System.out.format(" \n");
                        //System.out.format(" Wrong prob  for electron %g \n",prob-1);
                    }

                    if(debugMode>=2) pho.shortshow();
                }
            }

            if(lh_sig[hypo]>0) ch_sig[hypo] = ch_sig[hypo]/lh_sig[hypo];
            if(debugMode>=1)System.out.format("raw like hypo %2d :  %4d %10.4g %10.4g %7.2f \n",
                hypo,n_sig[hypo],lh_sig[hypo],lh_bg[hypo],ch_sig[hypo]*MRAD);
        }

        if(lh_sig[1]>0 || lh_sig[2]>0 || lh_sig[3]>0)
            ch_had=(ch_sig[1]*lh_sig[1] + ch_sig[2]*lh_sig[2] + ch_sig[3]*lh_sig[3])/(lh_sig[1] + lh_sig[2] + lh_sig[3]);


        for (int hypo=0; hypo<RICHConstants.N_HYPO; hypo++){
            //ATT: trick just for comparison
            lh_sig[hypo]*=FAC;
            lh_bg[hypo]*=FAC;
            lh_sig[hypo] -= lh_bg[hypo];
        }
        
        double newRQ = 0.0;
        RICHSolution hreco = new RICHSolution();
        String hstri = null;
        if(recotype==0) {hreco = hadron.analytic; hstri="ALI";}
        if(recotype==1) {hreco = hadron.traced; hstri="TRA";}

        newRQ = hreco.assign_PID(lh_sig);
        hadron.set_RICHpid(hreco.get_BestH());

        int best_hypo = -1;
        int sec_hypo = -1;
        double bestRL = 0.0;
        if(hreco.get_BestH()>0){
            for (int hypo=0; hypo<RICHConstants.N_HYPO; hypo++){
                int hypo_pid = RICHConstants.HYPO_LUND[hypo];
                if(hypo_pid==hreco.get_BestH())best_hypo = hypo;
                if(hypo_pid==hreco.get_secH())sec_hypo = hypo;
            }
            bestRL = (lh_sig[best_hypo]-lh_sig[sec_hypo])/(2*n_sig[best_hypo]-1);
        }
        if(debugMode>=1)System.out.format("assign %5d %3d %3d %7.3f %7.2f \n",hreco.get_BestH(),best_hypo,sec_hypo,newRQ,bestRL);

        if(best_hypo>-1){
            hreco.set_BestCH(ch_sig[best_hypo]);
            hreco.set_BestNpho(n_sig[best_hypo]);
            hreco.set_BestRL(bestRL);
        }
        if(best_hypo>-1 && debugMode>=1)
            System.out.format("%s eve %8d  mom %6.2f xy %7.2f %7.2f %7.2f %7.2f %8.2f %8.4f  Npho %5d %10.4g %3d %10.4g %3d %10.4g --> %8.5f %7.2f %7.2f %3d %3d\n", 
            hstri,eventID, hadron.get_momentum(), 
            hadron.direct_ray.origin().x(), hadron.direct_ray.origin().y(), hadron.get_HitPos().x(), hadron.get_HitPos().y(), 
            hadron.changle(best_hypo,0)*MRAD, hadron.refi_emission, n_sig[best_hypo], lh_sig[best_hypo], 
            hreco.get_BestH()*hadron.charge(), hreco.get_Bestprob(), hreco.get_secH(), hreco.get_secprob(), 
            newRQ, ch_sig[best_hypo]*MRAD, ch_had*MRAD, hadron.get_CLASpid(), hadron.get_RICHpid());

    }


    // ----------------
    public void showEvent(){
    // ----------------

        System.out.format(" ------------------------------------ \n");
        System.out.format(" RICH Event with nhad %4d  and npho  %4d \n",hadrons.size(),photons.size());
        System.out.format(" ------------------------------------ \n");
        for(int hid=0; hid<hadrons.size(); hid++) {
            RICHParticle had = hadrons.get(hid);
            System.out.format(" had %3d %3d %3d %3d [ %s -->  %s ] [ %s -->  %s ]  [ %8.2f %8.2f -->  %8.2f %8.2f ]\n", 
                              hid,had.get_id(),had.get_ParentIndex(),had.get_HitIndex(), 
                              had.lab_emission.toStringBrief(2),
                              had.ref_emission.toStringBrief(2),
                              had.get_HitPos().toStringBrief(2), had.ref_impact.toStringBrief(2),
                              had.lab_phi*RAD, had.lab_theta*RAD, had.ref_phi*RAD, had.ref_theta*RAD);
            for(int ipho=0; ipho<photons.size(); ipho++) {
                RICHParticle pho = photons.get(ipho);
                if(pho.get_ParentIndex()==hid){
                    System.out.format(" pho %3d id %3d  hit %3d  had %3d  type %3d  meas[ %s %8.2f ]  analyt[ %8.2f %8.2f ] traced[%8.2f %s %8.2f ] \n",
                                      ipho, pho.get_id(), pho.get_ParentIndex(), pho.get_HitIndex(), pho.get_type(),
                                      pho.get_HitPos().toStringBrief(2), pho.get_HitTime(),
                                      pho.analytic.get_EtaC()*MRAD, pho.get_StartTime()+pho.analytic.get_time(),
                                      pho.traced.get_EtaC()*MRAD, pho.traced.get_hit().toStringBrief(2), pho.get_StartTime()+pho.traced.get_time());
                }
            }    
        }    
    }


    // ----------------
    public void select_Photons(RICHParticle hadron, int recotype, RICHParameters richpar){
    // ----------------

        int debugMode = 0;
        //if(hadron.ilay_emission==0 && hadron.ico_emission==13)debugMode=1;

        String stype = "traced";
        if(recotype==0)stype="analytic";

        for (int hypo=0; hypo<RICHConstants.N_HYPO; hypo++){
            int hypo_pid = RICHConstants.HYPO_LUND[hypo];

            if(debugMode>=2)System.out.format("Select %s photons for hadron %3d hypo %5d\n",stype,hadron.get_id(),hypo_pid);
            for( RICHParticle pho: photons){

                if(!pho.is_real())continue;
                if(pho.get_type()!=hypo)continue;
                if(pho.get_ParentIndex() != hadron.get_id()) continue;

                RICHParticle had = get_Hadron( pho.get_ParentIndex() );

                RICHSolution reco = pho.traced;
                if(recotype==0) {
                    if (had.get_Status()!=1)continue;
                    reco = pho.analytic; 
                }

                double htime = pho.get_HitTime();
                double dtime = pho.get_StartTime() + reco.get_time() - htime;
                if(debugMode>=2)System.out.format(" %4d  -->  [%7.2f + %7.2f] %7.2f  --> %7.2f ",pho.get_HitIndex(),pho.get_StartTime(),reco.get_time(),htime,dtime);

                int irefle = reco.get_RefleType();
                if(irefle<0 || irefle>2) continue;

                // open acceptance intervals around the given hypothesis
                double CHMI = had.changle(hypo_pid,irefle) - richpar.NSIGMA_CHERENKOV * had.schangle(irefle);
                double CHMA = had.changle(hypo_pid,irefle) + richpar.NSIGMA_CHERENKOV * had.schangle(irefle);
                double DTMI = pho.chtime() - richpar.NSIGMA_TIME * pho.schtime();
                double DTMA = pho.chtime() + richpar.NSIGMA_TIME * pho.schtime();

                double etaC = reco.get_EtaC();
                if(debugMode>=2)System.out.format(" %4d  -->  %7.2f [%7.2f-%7.2f]  %7.2f [%6.2f:%6.2f] ",
                    pho.get_HitIndex(),etaC*MRAD, CHMI*MRAD, CHMA*MRAD, dtime, DTMI, DTMA);

                // select acceptable Cherenkov solution
                int SELE = 0;
                if(etaC>0 && etaC>CHMI && etaC<CHMA) SELE=10;
                if(dtime > DTMI && dtime < DTMA) SELE=1;
                if((etaC>0 && etaC>CHMI && etaC<CHMA) && (dtime > DTMI && dtime < DTMA)) SELE=11;

                reco.set_OK(SELE);

                if(debugMode>=2){
                    if(SELE==11){
                        System.out.format(" --> %4d  (%5d %8d  %8d  %8d)\n",
                            reco.get_OK(), reco.get_Nrefle(),reco.get_FirstRefle(), reco.get_RefleLayers(), reco.get_RefleCompos());
                    }else{
                        System.out.format(" --> rejected\n");
                    }
               } 
            }
        }

        for (int hypo=0; hypo<RICHConstants.N_HYPO; hypo++){

            for( RICHParticle pho: photons) {

                if(!pho.is_real())continue;
                if(pho.get_type()==hypo)continue;
                if(pho.get_ParentIndex() != hadron.get_id()) continue;

                RICHSolution reco = pho.traced;
                if(recotype==0) reco = pho.analytic;

                if(reco.get_OK()<11){
                    for( RICHParticle oth: photons) {
                        if(oth.get_id()!=pho.get_id() && oth.get_ParentIndex()==hadron.get_id() && oth.get_HitIndex()==pho.get_HitIndex()){
                            RICHSolution roth = oth.traced;
                            if(recotype==0) roth = oth.analytic;
                            if(roth.get_OK()==11 && roth.status()==0){
                                reco.set_OK( reco.get_OK()+100 );
                                // take elemets for likelihood calculation
                                // ATT: take first working hypo as most probable (to be refined)
                                reco.set_dthe_res( roth.get_dthe_res());
                                reco.set_dphi_res( roth.get_dphi_res());
                                reco.set_dthe_bin( roth.get_dthe_bin());
                                reco.set_dphi_bin( roth.get_dphi_bin());
                                break;
                            }
                        }
                    }
                }
            }
        }

        if(debugMode>=1){
            for( RICHParticle pho: photons) {
                
                RICHSolution reco = pho.traced;
                if(recotype==0)reco = pho.analytic;

                if(reco.get_OK()<11)continue;

                System.out.format("sele pixel %4d for %s pho %4d %3d %7.2f OK  %3d  [%6d : %6d %6d ] %3d\n",
                    pho.get_HitIndex(), stype, pho.get_id(), pho.get_type(), reco.get_EtaC()*MRAD, reco.get_OK(),reco.get_FirstRefle(), reco.get_RefleLayers(), reco.get_RefleCompos(), reco.status());
            }

        }

    }

    // ----------------
    public void analyze_Photons(int hypo, RICHRayTrace richtrace){
    // ----------------

        int debugMode = 0;
        int jj=0;
        for( RICHParticle photon: photons){
            if(photon.get_type()==hypo){

                System.out.format(" Analyze Photon %4d  from  Hadron %3d  \n",jj,photon.get_ParentIndex()); 
                RICHParticle richhadron = get_Hadron( photon.get_ParentIndex() );
                if(debugMode>=1){
                    System.out.format(" --------------------------------- \n");
                    System.out.format(" Analyze Photon %4d  from  Hadron %3d  meas hit %s \n",jj,richhadron.get_id(), 
                                       photon.get_HitPos().toStringBrief(2));
                    System.out.format(" --------------------------------- \n");
                }
                if (richhadron.get_Status()==1){
                    richtrace.find_EtaC_analytic_migrad(richhadron, photon);
                }else{
                    if(debugMode>=1)System.out.format(" Hadron pointing to mirror, skip analytic analysis \n");
                }
            }
            jj++;
        }
    }

    // ----------------
    public void trace_Photons(RICHParticle hadron, int hypo, RICHRayTrace richtrace, RICHCalibration richcal){
    // ----------------

        int debugMode = 0;

        int jj=0;
        if(hypo<0 || hypo>=RICHConstants.N_HYPO ) return;
        for( RICHParticle photon: photons){
            if(photon.get_ParentIndex()==hadron.get_id() && photon.get_type()==hypo){

                int pmt = hits.get(photon.get_HitIndex()).get_pmt();
                if(debugMode>=1){
                    System.out.format(" --------------------------------- \n");
                    System.out.format(" Hypo %s: Trace Photon %4d %4d  from  Hadron %3d  meas hit %4d %s  pmt %4d \n",
                        RICHConstants.HYPO_LUND[hypo],jj,photon.get_id(),hadron.get_id(),photon.get_HitIndex(),photon.get_HitPos().toStringBrief(2), pmt);
                }
                //RICHParticle richhadron = get_Hadron( photon.get_ParentIndex() );
                richtrace.find_EtaC_raytrace_steps(hadron, photon, hypo);
                if(photon.traced.exist()){
                    photon.traced.set_status(photon.get_sector(), richcal);
                    if(debugMode>=1){
                        System.out.format(" FOUND with status = %3d \n",photon.traced.status());
                        photon.traced.dump_raytrack(" RAY ");
                        System.out.format(" --------------------------------- \n");
                    }
                }
            }
            jj++;
        }
    }


    // ----------------
    public void associate_Throws(RICHParticle hadron, int hypo, RICHParameters richpar) {
    // ----------------

        int debugMode = 0;
        int match_nchi2 = 0 ;
        double match_chi2 = 0.0 ;

        if(hypo<0 || hypo>=RICHConstants.N_HYPO ) return;

        if(debugMode>=1)System.out.format("Associate photon with trials \n");
        int jj=0;
        for( RICHParticle photon: photons){
            if(photon.get_ParentIndex()==hadron.get_id() && photon.get_type()==hypo){

                if(debugMode>=1)System.out.format(" Hypo %s: Look for pho id %4d  xy %8.2f %8.2f  time %7.2f \n",
                           RICHConstants.HYPO_LUND[hypo],photon.get_id(),photon.get_HitPos().x(),photon.get_HitPos().y(), photon.get_HitTime());
                int ii=0;
                double distmin = 99999;
                for( RICHParticle trial: photons){
                    if(trial.get_type()==hypo+10 && trial.traced.exist()){
                        double dist = trial.get_HitPos().distance(photon.get_HitPos());
                        if(debugMode>=1)System.out.format("     --> trial %4d xy %8.2f %8.2f  time %7.2f \n", trial.get_id(),
                                                trial.get_HitPos().x(),trial.get_HitPos().y(), trial.get_HitTime());
                        if(dist < distmin){
                            distmin = dist;
                            if(distmin<richpar.THROW_ASSOCIATION_CUT){
                                photon.trial_pho = trial;
                            }
                        }
                    }
                    ii++; 
                }
                if(photon.trial_pho!=null && distmin<4){
                    double tprob=photon.trial_pho.time_probability(photon.get_HitTime(), 1);
                    if(tprob>richpar.PIXEL_NOMINAL_DARKRATE){
                        match_chi2+=Math.pow((distmin/richpar.RICH_HITMATCH_RMS),2);
                        match_nchi2++;
                        if(debugMode>=1)System.out.format("  -->  store throw id %d  xy %8.2f %8.2f  time %7.2f  --> dist %7.2f  ch2 %7.2f  tprob %8.6f %10.2f \n",
                            photon.trial_pho.get_id(),photon.trial_pho.get_HitPos().x(),photon.trial_pho.get_HitPos().y(), photon.trial_pho.get_HitTime(), 
                            distmin, (distmin/richpar.RICH_HITMATCH_RMS), tprob-1, match_chi2);
                    }
                }
            }
            jj++;
        }

        if(match_nchi2>0)match_chi2=(match_chi2)/match_nchi2;
        if(debugMode>=1)System.out.format("  CHI2 %d --> %7.2f \n",match_nchi2,match_chi2);
    }

    // ----------------
    public void throw_Photons(RICHParticle hadron, int Nthrows, int hypo, RICHRayTrace richtrace, RICHParameters richpar, RICHCalibration richcal){
    // ----------------

        int debugMode = 0;
        Vector3D vhad = hadron.direct_ray.toVector().asUnit();

        if(hypo<0 || hypo>=RICHConstants.N_HYPO ) return;
        int hypo_pid = RICHConstants.HYPO_LUND[hypo];

        int NTHE = 1;
        if(richpar.TRACE_NITROGEN==1 && hypo==0)NTHE=2;

        for (int ithe=0; ithe<NTHE; ithe++){
            double theta = hadron.changle(hypo_pid,0);
            double fac = 1;
            int IOFF = 10;
            if(ithe==1){
                theta = Math.acos(1./RICHGeoConstants.RICH_AIR_INDEX);
                fac = 0.2;
                IOFF = 20;
            }

            if(debugMode>=1)System.out.format("TTT %3d %3d %3d %7.2f \n",hypo,NTHE,ithe,theta*MRAD);

            // Define Cherenkov rotation
            Vector3D X_ONE = new Vector3D(1., 0., 0.);
            Vector3D vax = (vhad.cross(X_ONE)).asUnit();
            Quaternion qch = new Quaternion(theta, vax);
            Vector3D vch = (qch.rotate(vhad)).asUnit();

            if(debugMode>=3){
                System.out.format("   -->  vhad %s | %8.3f %8.3f \n", vhad.toStringBrief(2), hadron.lab_theta*RAD, hadron.lab_phi*RAD);
                System.out.format("   -->  vax  %s | %8.3f %8.3f \n", vax.toStringBrief(2), vax.theta()*RAD, vax.phi()*RAD);
                System.out.format("   -->  vch  %s | %8.3f %8.3f \n", vch.toStringBrief(2), vch.theta()*RAD, vch.phi()*RAD);
                System.out.format("   -->  resulting angle %8.3f %8.3f \n", vch.angle(vhad)*MRAD, vch.angle(vhad)*RAD); 
            }

            int nk=0;
            double dphi = 2*Math.PI/Nthrows;
            double cophi = -Math.PI;
            while (cophi<Math.PI){
      
                //cophi = -Math.PI + k*dphi;
                cophi = cophi + dphi/fac;
                if(debugMode>=1){  
                    System.out.println(" ------------------------------------ ");
                    System.out.format(" Throw photon %4d %s aero %4d %6d  at the %8.3f (%8.3f, %8.3f) step %4.1f \n", 
                                      photons.size(), RICHConstants.HYPO_STRING[hypo],
                                      hadron.ilay_emission, hadron.ico_emission, theta*MRAD, theta*RAD, cophi*RAD, fac);
                    System.out.println(" ------------------------------------ ");
                }
          
                // Define Cone rotation
                Quaternion qco = new Quaternion(cophi, vhad);
                Vector3D vpho = (qco.rotate(vch)).asUnit();
                
                double che_th = vch.angle(vhad);
                if(debugMode>=3) System.out.format(" %d %8.2f --> vpho %s | %8.3f %8.3f --> %8.3f %8.3f\n", 
                                 photons.size(), cophi*RAD, vpho.toStringBrief(2), vpho.theta()*RAD, vpho.phi()*RAD, che_th*MRAD, che_th*RAD);

                Point3D emission = hadron.lab_emission;
                if(ithe==1) {
                    double Lemi  = 3+(cophi+Math.PI)/2./Math.PI*50.;  //emission point along the trajectory in gas 
                    emission = new Point3D(hadron.lab_emission, vhad.multiply(Lemi));
                    if(debugMode>=1)System.out.format("TTT %7.2f %s \n",Lemi,emission.toStringBrief(2));
                }
                Point3D extrap = new Point3D(emission, vpho.multiply(400));
                RICHParticle photon = new RICHParticle(photons.size(), hadron, null, extrap, richpar);
                photon.set_rotated_points(hadron);
                photon.set_type(hypo+IOFF);
                photon.traced.set_EtaC(theta);
                photon.traced.set_theta(vpho.theta());
                photon.traced.set_phi(vpho.phi());
                photon.traced.set_scale(fac);
                photon.traced.set_hypo(hypo_pid);

                ArrayList<RICHRay> rays = richtrace.RayTrace(photon, vpho);
                if(rays!=null) {
                    if(debugMode>=2) System.out.format(" Photon traced till PMT detection \n");
                    
                    photon.traced.set_raytracks(rays);

                    photon.set_HitPos( photon.traced.get_hit() );
                    photon.set_HitTime( photon.get_StartTime() + photon.traced.get_time() );
                    String head = String.format(" THROW %7de %3d ",eventID,photon.get_id());
                    if(debugMode>=1)photon.traced.dump_raytrack(head);

                    // store in the event
                    if(rays.get(rays.size()-1).is_detected()){

                        // shift (in cm) on PMT surface corresponding to one angular sigma 
                        double dthe_res = richtrace.find_dthe_steps(photon);
                        double dphi_res = richtrace.find_dphi_steps(photon);
                        // gate (number of sigma) used in reconstruction
                        double dthe_bin = 2 * richpar.NSIGMA_CHERENKOV;
                        double dphi_bin = dphi/fac/photon.nominal_sChAngle();
       
                        photon.traced.set_dthe_res(dthe_res);
                        photon.traced.set_dphi_res(dphi_res);
                        photon.traced.set_dthe_bin(dthe_bin);
                        photon.traced.set_dphi_bin(dphi_bin);

                        if(debugMode>=1)System.out.format(" --> detected time %7.2f ttime %7.2f dthe (%7.2f, %7.2f) dphi (%7.2f,%7.2f)\n",
                            photon.get_HitTime(),photon.traced.get_time(), dthe_res,dthe_bin,dphi_res,dphi_bin);

                        photons.add(photon);
                        fac=1;
                        if(ithe==1)fac=0.2;
                        if (photon.traced.get_Nrefle()>2)fac*=2.;
                        nk++;
                    }
                }

            }
        }
    }

}
