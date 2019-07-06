package org.jlab.rec.rich;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import org.jlab.geom.prim.Point3D;
import org.jlab.geometry.prim.Line3d;
import eu.mihosoft.vrl.v3d.Vector3d;

import org.jlab.clas.detector.DetectorResponse;

public class RICHEvent {

    /**
     * A RICH Event is the collected information of the event
     */

    private int runID;
    private int eventID;
    private float eventTime;
    private long exeStart;
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

    private double match_chi2 = 0.0;
    private int match_nchi2 = 0;

    
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
    public void set_exeStart(long exetime) { exeStart = exetime; }
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
    public long get_exeStart() {return exeStart;}
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
    public void get_pid(RICHParticle hadron, int recotype) {
    // ----------------

        int debugMode = 0;
        int npho = 0;
        double lh_el = 0.0;
        double lh_pi = 0.0;
        double lh_k  = 0.0;
        double lh_pr = 0.0;
        double lh_bg = 0.0;

        double ch_el = 0.0;
        double ch_pi = 0.0;
        double ch_k  = 0.0;
        double ch_pr = 0.0;
        double ch_bg = 0.0;
        double ch_had = 0.0;

        int n_el = 0;
        int n_pi = 0;
        int n_k  = 0;
        int n_pr = 0;
        int n_bg = 0;

        if(debugMode>=1)System.out.format("Likelihood calc for reco %d \n",recotype);

        double prob = 0.0;
        for( RICHParticle pho: photons) {
            if(pho.get_type()==0 && pho.get_ParentIndex() == hadron.get_id()){

                if(debugMode>=1)System.out.format("calc prob for photon %d \n",npho);

                RICHSolution reco = new RICHSolution(); 
                if(recotype==0) reco = pho.analytic;
                if(recotype==1) reco = pho.traced;
                double etac = reco.get_EtaC();
     
                // prob for backgound
                prob = pho.pid_probability(hadron, 0, recotype);
                if(prob-1>=RICHConstants.RICH_BKG_PROBABILITY){
                    lh_bg += Math.log(prob);
                    ch_bg += Math.log(prob)*etac;
                    n_bg++;
                    reco.set_bgprob(Math.log(prob));
                    if(debugMode>=2)System.out.format("prob  %d etac %8.4f for background %g %g \n",npho, etac*MRAD, prob, Math.log(prob));
                }else{
                    //System.out.format("ATT: wrong prob  for background %g \n",prob-1);
                }

                // prob for electron
                prob = pho.pid_probability(hadron, 11, recotype);
                if(prob-1>=RICHConstants.RICH_BKG_PROBABILITY){
                    lh_el += Math.log(prob);
                    ch_el += Math.log(prob)*etac;
                    n_el++;
                    reco.set_elprob(Math.log(prob));
                    if(debugMode>=2)System.out.format("prob  %d etac %8.4f for electron %g %g \n",npho, etac*MRAD, prob, Math.log(prob));
                }else{
                    //System.out.format("ATT: wrong prob  for electron %g \n",prob-1);
                }

                // prob for pion
                prob=pho.pid_probability(hadron, 211, recotype);
                if(prob-1>=RICHConstants.RICH_BKG_PROBABILITY){
                    lh_pi += Math.log(prob);
                    ch_pi += Math.log(prob)*etac;
                    n_pi++;
                    reco.set_piprob(Math.log(prob));
                    if(debugMode>=1)System.out.format("prob  %d etac %8.4f for pion %g %g \n",npho, etac*MRAD, prob, Math.log(prob));
                }else{
                    //System.out.format("ATT: wrong prob  for pion %g \n",prob-1);
                }

                // prob for kaon
                prob=pho.pid_probability(hadron, 321, recotype);
                if(prob-1>=RICHConstants.RICH_BKG_PROBABILITY){
                    lh_k  += Math.log(prob);
                    ch_k  += Math.log(prob)*etac;
                    n_k++;
                    reco.set_kprob(Math.log(prob));
                    if(debugMode>=1)System.out.format("prob  %d etac %8.4f for kaon %g %g \n",npho, etac*MRAD, prob, Math.log(prob));
                }else{
                    //System.out.format("ATT: wrong prob  for kaon %g \n",prob-1);
                }

                // prob for proton
                prob=pho.pid_probability(hadron, 2212, recotype);
                if(prob-1>=RICHConstants.RICH_BKG_PROBABILITY){
                    lh_pr += Math.log(prob);
                    ch_pr += Math.log(prob)*etac;
                    reco.set_prprob(Math.log(prob));
                    n_pr++;
                    if(debugMode>=1)System.out.format("prob  %d etac %8.4f for proton %g %g \n",npho, etac*MRAD, prob, Math.log(prob));
                }else{
                    //System.out.format("ATT: wrong prob  for proton %g \n",prob-1);
                }

                if(debugMode>=2) pho.shortshow();
            }
            npho++;
        }
        
        if(lh_el>0)ch_el=ch_el/lh_el;
        if(lh_pi>0)ch_pi=ch_pi/lh_pi;
        if(lh_k>0)ch_k=ch_k/lh_k;
        if(lh_pr>0)ch_pr=ch_pr/lh_pr;
        if(lh_pi>0 || lh_k>0 || lh_pr>0)ch_had=(ch_pi*lh_pi + ch_k*lh_k + ch_pr*lh_pr)/(lh_pi + lh_k + lh_pr);
        lh_el -= lh_bg;
        lh_pi -= lh_bg;
        lh_k -= lh_bg;
        lh_pr -= lh_bg;

        
        if(debugMode>=1)System.out.format("raw likelihoods %3d %g %3d %g %3d %g %3d %g %3d %g | %g %g %g %g | %g \n",n_el,lh_el,n_pi,lh_pi,n_k,lh_k,n_pr,lh_pr,n_bg,lh_bg, 
                      ch_el, ch_pi, ch_k, ch_pr, ch_had);

        double newRQ = 0.0;
        if(recotype==0) {
            newRQ = hadron.analytic.assign_PID(lh_el, lh_pi, lh_k, lh_pr, lh_bg);
            hadron.set_RICHpid(hadron.analytic.get_bestH());
            if(debugMode>=1)System.out.format("NEW ALY eve %8d  mom %6.2f xy %7.2f %7.2f %7.2f %7.2f %8.2f %8.4f  Npho %5d %8.4f %3d %g %3d %g --> %8.5f %7.2f %7.2f %3d %3d | %5d %8.4f \n",eventID, hadron.get_momentum(), 
                hadron.lab_origin.x(), hadron.lab_origin.y(), hadron.meas_hit.x, hadron.meas_hit.y, hadron.get_changle(0)*MRAD, hadron.refi_emission,
                n_el, lh_el, hadron.analytic.get_bestH(), hadron.analytic.get_bestprob(), hadron.analytic.get_secH(), hadron.analytic.get_secprob(), 
                newRQ, ch_had*MRAD, ch_el*MRAD, hadron.get_CLASpid(), hadron.get_RICHpid(), match_nchi2, match_chi2);
        }
        if(recotype==1) {
            newRQ = hadron.traced.assign_PID(lh_el, lh_pi, lh_k, lh_pr, lh_bg);
            hadron.set_RICHpid(hadron.traced.get_bestH());
            if(debugMode>=1)System.out.format("NEW TRA eve %8d  mom %6.2f xy %7.2f %7.2f %7.2f %7.2f %8.2f %8.4f  Npho %5d %8.4f %3d %g %3d %g --> %8.5f %7.2f %7.2f %3d %3d | %5d %8.4f \n",eventID, hadron.get_momentum(), 
                hadron.lab_origin.x(), hadron.lab_origin.y(), hadron.meas_hit.x, hadron.meas_hit.y, hadron.get_changle(0)*MRAD, hadron.refi_emission,
                n_el, lh_el, hadron.traced.get_bestH(), hadron.traced.get_bestprob(), hadron.traced.get_secH(), hadron.traced.get_secprob(),  
                newRQ, ch_had*MRAD, ch_el*MRAD, hadron.get_CLASpid(), hadron.get_RICHpid(), match_nchi2, match_chi2);
        }

    }


    // ----------------
    public void showEvent(){
    // ----------------

        System.out.format(" ------------------------------------ \n");
        System.out.format(" RICH Event with nhad %4d  and npho  %4d \n",hadrons.size(),photons.size());
        System.out.format(" ------------------------------------ \n");
        for(int hid=0; hid<hadrons.size(); hid++) {
            RICHParticle had = hadrons.get(hid);
            System.out.format(" had %3d %3d %3d %3d [ %8.2f %8.2f %8.2f -->  %8.2f %8.2f %8.2f ] [ %8.2f %8.2f %8.2f -->  %8.2f %8.2f %8.2f ]  [ %8.2f %8.2f -->  %8.2f %8.2f ]\n", 
                              hid,had.get_id(),had.get_ParentIndex(),had.get_hit_index(), 
                              had.lab_emission.x, had.lab_emission.y, had.lab_emission.z ,had.ref_emission.x, had.ref_emission.y, had.ref_emission.z,
                              had.meas_hit.x, had.meas_hit.y, had.meas_hit.z, had.ref_impact.x, had.ref_impact.y, had.ref_impact.z,
                              had.lab_phi*RAD, had.lab_theta*RAD, had.ref_phi*RAD, had.ref_theta*RAD);
            for(int ipho=0; ipho<photons.size(); ipho++) {
                RICHParticle pho = photons.get(ipho);
                if(pho.get_ParentIndex()==hid){
                    System.out.format(" pho %3d id %3d  hit %3d  had %3d  type %3d  meas[ %7.1f %7.1f %7.1f %8.2f ]  analyt[ %8.2f %8.2f ] traced[%8.2f %7.1f %7.1f %7.1f%8.2f ] \n",
                                      ipho, pho.get_id(), pho.get_ParentIndex(), pho.get_hit_index(), pho.get_type(),
                                      pho.meas_hit.x, pho.meas_hit.y, pho.meas_hit.z, pho.get_meas_time(),
                                      pho.analytic.get_EtaC()*MRAD, pho.get_start_time()+pho.analytic.get_time(),
                                      pho.traced.get_EtaC()*MRAD, pho.traced.get_hit().x, pho.traced.get_hit().y, pho.traced.get_hit().z, pho.get_start_time()+pho.traced.get_time());
                }
            }    
        }    
    }


    // ----------------
    public void analyze_Photons(){
    // ----------------

        int debugMode = 0;
        int jj=0;
        for( RICHParticle photon: photons){
            if(photon.get_type()==0){

                RICHParticle richhadron = get_Hadron( photon.get_ParentIndex() );
                if(debugMode>=1){
                    System.out.format(" --------------------------------- \n");
                    System.out.format(" Analyze Photon %4d  from  Hadron %3d  meas hit %7.2f %7.2f %7.2f \n",jj,richhadron.get_id(), 
                                       photon.meas_hit.x,photon.meas_hit.y,photon.meas_hit.z);
                    System.out.format(" --------------------------------- \n");
                }
                if (richhadron.get_Status()==1){
                    photon.find_EtaC_analytic_migrad(richhadron);
                }else{
                    if(debugMode>=1)System.out.format(" Hadron pointing to mirror, skip analytic analysis \n");
                }
            }
            jj++;
        }
    }

    // ----------------
    public void trace_Photons(RICHTool tool){
    // ----------------

        int debugMode = 0;
        int jj=0;
        for( RICHParticle photon: photons){
            if(photon.get_type()==0){

                RICHParticle richhadron = get_Hadron( photon.get_ParentIndex() );
                if(debugMode>=1){
                    System.out.format(" --------------------------------- \n");
                    System.out.format(" Trace Photon %4d  from  Hadron %3d  meas hit %7.2f %7.2f %7.2f \n",jj,richhadron.get_id(), 
                                       photon.meas_hit.x,photon.meas_hit.y,photon.meas_hit.z);
                    System.out.format(" --------------------------------- \n");
                }
                photon.find_EtaC_raytrace_steps(richhadron, tool);
            }
            jj++;
        }
    }


    // ----------------
    public void associate_Throws(RICHTool tool){
    // ----------------

        int debugMode = 0;
        match_nchi2 = 0 ;
        match_chi2 = 0.0 ;

        RICHConstants recopar = tool.get_Constants();

        if(debugMode>=1)System.out.format("Associate photon with trials \n");
        int jj=0;
        for( RICHParticle photon: photons){
            if(photon.get_type()==0){
                if(debugMode>=1)System.out.format(" Look for pho id %4d  xy %8.2f %8.2f  time %7.2f \n",photon.get_id(),photon.meas_hit.x,photon.meas_hit.y, photon.get_meas_time());
                int ii=0;
                double distmin = 99999;
                for( RICHParticle trial: photons){
                    if(trial.get_type()>0){
                        double dist = trial.meas_hit.distance(photon.meas_hit);
                        //if(debugMode>=0)System.out.format("  -->  %4d %8.2f %8.2f  dist %8.2f  prev %8.2f \n",ii, trial.meas_hit.x, trial.meas_hit.y, dist, distmin);
                        if(dist < distmin){
                            distmin = dist;
                            if(distmin<recopar.THROW_ASSOCIATION_CUT){
                                photon.trial_pho = trial;
                            }
                        }
                    }
                    ii++; 
                }
                if(photon.trial_pho!=null && distmin<4){
                    double tprob=photon.trial_pho.time_probability(photon.get_meas_time(), 1);
                    if(tprob-1>1.e-3){
                        match_chi2+=Math.pow((distmin/recopar.RICH_HITMATCH_RMS),2);
                        match_nchi2++;
                        if(debugMode>=1)System.out.format("  -->  store throw id %d  xy %8.2f %8.2f  time %7.2f  --> dist %7.2f  ch2 %7.2f  tprob %8.6f %10.2f \n",
                            photon.trial_pho.get_id(),photon.trial_pho.meas_hit.x,photon.trial_pho.meas_hit.y, photon.trial_pho.get_meas_time(), 
                            distmin, (distmin/recopar.RICH_HITMATCH_RMS), tprob-1, match_chi2);
                    }
                }
            }
            jj++;
        }

        if(match_nchi2>0)match_chi2=(match_chi2)/match_nchi2;
        if(debugMode>=1)System.out.format("  CHI2 %d --> %7.2f \n",match_nchi2,match_chi2);
    }

    // ----------------
    public void throw_Photons(RICHParticle hadron, int Npho, double theta, int type, RICHTool tool){
    // ----------------

        int debugMode = 0;
        Vector3d vhad = hadron.direct_ray.diff().normalized();

        // Define Cherenkov rotation
        Vector3d vax = (vhad.cross(Vector3d.X_ONE)).normalized();
        Quaternion qch = new Quaternion(theta, vax);
        Vector3d vch = (qch.rotate(vhad)).normalized();

        if(debugMode>=3){
            System.out.format("   -->  vhad %8.3f %8.3f %8.3f | %8.3f %8.3f \n", vhad.x, vhad.y, vhad.z, hadron.lab_theta*RAD, hadron.lab_phi*RAD);
            System.out.format("   -->  vax  %8.3f %8.3f %8.3f | %8.3f %8.3f \n", vax.x, vax.y, vax.z, vax.angle(Vector3d.Z_ONE)*RAD, Math.atan2(vax.y, vax.x)*RAD);
            System.out.format("   -->  vch  %8.3f %8.3f %8.3f | %8.3f %8.3f \n", vch.x, vch.y, vch.z, vch.angle(Vector3d.Z_ONE)*RAD, Math.atan2(vch.y, vch.x)*RAD);
            System.out.format("   -->  resulting angle %8.3f %8.3f \n", vch.angle(vhad)*MRAD, vch.angle(vhad)*RAD); 
        }

        //for(int k=0 ; k<Npho; k++) {

        int nk=0;
        double fac = 1;
        double dphi = 2*Math.PI/Npho;
        double cophi = -Math.PI-dphi;
        while (cophi<Math.PI){
  
            //cophi = -Math.PI + k*dphi;
            cophi = cophi + dphi/fac;
            if(debugMode>=1){  
                System.out.println(" ------------------------------------ ");
                System.out.format(" Throw photon %4d from %4d %6d  at the %8.3f (%8.3f, %8.3f) step %4.1f \n", 
                                  photons.size(), hadron.ilay_emission, hadron.ico_emission, theta*MRAD, theta*RAD, cophi*RAD, fac);
                System.out.println(" ------------------------------------ ");
            }
      
            // Define Cone rotation
            Quaternion qco = new Quaternion(cophi, vhad);
            Vector3d vpho = qco.rotate(vch).normalized();
            
            double vpho_phi = Math.atan2(vpho.y, vpho.x);
            double vpho_th = vpho.angle(Vector3d.Z_ONE);
            double che_th = vch.angle(vhad);
            if(debugMode>=3) System.out.format(" %d %8.2f --> vpho %8.3f %8.3f %8.3f | %8.3f %8.3f --> %8.3f %8.3f\n", 
                             photons.size(), cophi*RAD, vpho.x, vpho.y, vpho.z, vpho_th*RAD, vpho_phi*RAD, che_th*MRAD, che_th*RAD);

            RICHParticle photon = new RICHParticle(photons.size(), hadron.get_id(), 0, 1.e-6, 22, tool);
            photon.set_points(hadron, hadron.lab_emission.plus(vpho.times(400)));
            photon.set_start_time(hadron.get_start_time());
            photon.set_type(type);
            photon.traced.set_EtaC((float) theta);
            photon.traced.set_theta((float) vpho_th);
            photon.traced.set_phi((float) vpho_phi);

            ArrayList<RICHRay> rays = tool.RayTrace(photon.lab_emission, photon.ilay_emission, photon.ico_emission, vpho);
            if(rays!=null) {
                if(debugMode>=2) System.out.format(" Photon traced till PMT detection \n");
                
                photon.traced.set_raytracks(rays);

                photon.set_meas_hit( photon.traced.get_hit() );
                photon.set_meas_time( photon.get_start_time() + photon.traced.get_time() );
                String head = String.format(" THROW %7de %3d ",eventID,photon.get_id());
                if(debugMode>=1)photon.traced.dump_raytrack(head);

                // store in the event
                if(rays.get(rays.size()-1).is_detected()){
                    photons.add(photon);
                    fac=1;
                    if (photon.traced.get_rayrefle()>2)fac=2.;
                    //if (photon.traced.get_rayrefle()>2)fac=4.;
                    nk++;
                }
            }

        }
    }

}
