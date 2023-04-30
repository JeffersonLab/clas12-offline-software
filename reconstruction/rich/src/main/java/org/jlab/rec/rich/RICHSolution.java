package org.jlab.rec.rich;

import org.jlab.clas.pdg.PhysicsConstants;
import java.util.ArrayList;
import java.util.Arrays;

import org.jlab.detector.geom.RICH.RICHRay;
import org.jlab.detector.geom.RICH.RICHGeoConstants;

import org.jlab.geom.prim.Point3D;

// ----------------
public class RICHSolution {
// ----------------

    private int debugMode = 0;
    private static double MRAD = RICHGeoConstants.MRAD;


    // ----------------
     public RICHSolution(){
    // ----------------
    }

    // ----------------
    public RICHSolution(int type) {
    // ----------------

        this.type        = type;
        this.raytracks.clear();

      }

    private int     type;                                  //      Solution type
    private int     OK         = -1;                       //      Solution exists and has been selected as good for the given hypothesis
    private int     hypo       = 0;                        //      Particle ID hypothesis (LUND)
    private int     status     = 0;                        //      Flag solutions with a bad-status mirror

    private double  EtaC       = 0.0;                      //      Cherenkov angle
    private double  aeron      = 0.0;                      //      Aerogel refrative index
    private double  theta      = 0.0;                      //      Laboratory theta 
    private double  phi        = 0.0;                      //      Laboratory phi 
    private double  dthe_res   = 0.0;                      //      Shift over MAPMT surface corresponding to 1 nominal sigma in theta
    private double  dphi_res   = 0.0;                      //      Shift over MAPMT surface corresponding to 1 nominal sigma in phi
    private double  dthe_bin   = 0.0;                      //      Rescaled factor to the likelihood theta bin
    private double  dphi_bin   = 0.0;                      //      Rescaled factor to the likelihood phi bin
    private double  scale      = 0.0;                      //      Laboratory scale
    private double  path       = 0.0;                      //      Path within the RICH
    private int     nrefle     = 0;                        //      Number of photon reflections 
    private int     nrefra     = 0;                        //      Number of photon refractions
    private double  time       = 0.0;                      //      Transit time within the RICH (solution dependent)
    private double  machi2     = 0.0;                      //      chi2 of the hit vs trajectory extrapolation (distance/resolution)
    private Point3D hit        = new Point3D(0,0,0);       //      Impact point of photon on the PMT
 
    private ArrayList<RICHRay> raytracks = new ArrayList<RICHRay>(); // Detailed path of the photon

    private double  elprob   = 0.0;                      //      Cherenkov probability for electron
    private double  piprob   = 0.0;                      //      Cherenkov probability for pion
    private double  kprob    = 0.0;                      //      Cherenkov probability for kaon
    private double  prprob   = 0.0;                      //      Cherenkov probability for proton
    private double  bgprob   = 0.0;                      //      Cherenkov probability for background

    private int     ndir     = 0;                        //      Number of direct photons
    private double  chdir    = 0.0;                      //      Mean Cherenkov angle for direct photons
    private double  sdir     = 0.0;                      //      RMS Cherenkov angle for direct photons
    private double  madir    = 0.0;                      //      Mean equivalent mass for direct photons
    private int     nlat     = 0;                        //      Number of photons reflected by lateral mirrors
    private double  chlat    = 0.0;                      //      Mean Cherenkov angle for photons reflected by lateral mirrors
    private double  slat     = 0.0;                      //      RMS Cherenkov angle for photons reflected by lateral mirrors
    private double  malat    = 0.0;                      //      Mean equivalent mass for photons reflected by lateral mirrors
    private int     nspe     = 0;                        //      Number of photons reflected by ssperical mirrors
    private double  chspe    = 0.0;                      //      Mean Cherenkov angle for photons reflected by ssperical mirrors
    private double  sspe     = 0.0;                      //      RMS Cherenkov angle for photons reflected by ssperical mirrors
    private double  maspe    = 0.0;                      //      Mean equivalent mass for photons reflected by ssperical mirrors
    private int     ntot     = 0;                        //      Number of all photons 
    private double  chtot    = 0.0;                      //      Mean Cherenkov angle for all photons 
    private double  stot     = 0.0;                      //      RMS Cherenkov angle for all photons 
    private double  matot    = 0.0;                      //      Mean equivalent mass for all photons 

    private double  bestprob = 0.0;                      //      best Cherenkov probability for hadron ID
    private double  secprob  = 0.0;                      //      second best Cherenkov probability for hadron ID
    private int     bestH    = 0;                        //      best Cherenkov probability for hadron ID
    private int     secH     = 0;                        //      second best Cherenkov probability for hadron ID
    private double  R_QP     = 0.0;                      //      Quality parameter of PID assignment
    private double  Re_QP    = 0.0;                      //      Quality parameter of ELE assignment
    private double  bestch   = 0.0;                      //      Mean Cehrekov angle for best PID assignment
    private double  bestRL   = 0.0;                      //      Likelihood ratio for best PID assignment
    private double  bestc2   = 0.0;                      //      Chi2 for best PID assignment
    private double  bestNp   = 0.0;                      //      Number of photons used for best PID assignment
    private double  bestMass = 0.0;                      //      Measured mass for best PID assignment  


    // ----------------
    public int get_type() { return type; }
    // ----------------

    // ----------------
    public int get_OK() { return OK; }
    // ----------------

    // ----------------
    // good for the current hypo (111) or other (+1000)
    public boolean is_used() { if(OK>110)return true; return false;} 
    // ----------------

    // ----------------
    public boolean is_OK() { if(OK==111)return true; return false;} 
    // ----------------

    // ----------------
    public int get_hypo(int charge) { 
    // ----------------
        int hypo_ch = hypo;
        if(hypo_ch==11){
            if(charge==1)hypo_ch*=-1;
        }else{
            hypo_ch = hypo*charge;
        }
        return hypo_ch;
    }

    // ----------------
    public int get_hypo() { return hypo; }
    // ----------------

    // ----------------
    public double get_EtaC() { return EtaC; }
    // ----------------

    // ----------------
    public double get_aeron() { return aeron; }
    // ----------------

    // ----------------
    public double get_theta() { return theta; }
    // ----------------

    // ----------------
    public double get_phi() { return phi; }
    // ----------------

    // ----------------
    public double get_dthe_res() { return dthe_res; }
    // ----------------

    // ----------------
    public double get_dphi_res() { return dphi_res; }
    // ----------------

    // ----------------
    public double get_dthe_bin() { return dthe_bin; }
    // ----------------

    // ----------------
    public double get_dphi_bin() { return dphi_bin; }
    // ----------------

    // ----------------
    public double get_scale() { return scale; }
    // ----------------

    // ----------------
    public double get_path() { return path; }
    // ----------------

    // ----------------
    public double get_time() { return time; }
    // ----------------

    // ----------------
    public double get_machi2() { return machi2; }
    // ----------------

    // ----------------
    public double get_raypath() {
    // ----------------

        double rpath = 0.0;
        for (RICHRay ray : raytracks) {
            rpath = rpath + ray.direction().mag();
            if(debugMode>=2)System.out.format(" photon ray path %s --> %8.2f %8.2f \n",ray.direction().toStringBrief(3), ray.direction().mag(),rpath);
        }
        return (double) rpath;
    }

    // ----------------
    public double get_raytime() {
    // ----------------

        double time = 0;
        int ii=0;
        for (RICHRay ray : raytracks) {
            double dtime = ray.direction().mag()/PhysicsConstants.speedOfLight()*ray.get_refind();
            time = time + (double) dtime;
            if(debugMode>=3)System.out.format(" photon ray path %8.2f  n %8.2f  --> %8.2f %8.2f \n", ray.direction().mag(),ray.get_refind(),dtime,time);
        }
        return time;
    }


    // ----------------
    public int get_RefleLayers() {
    // ----------------
    // return a coded flag with the layer sequence alongt the photon path

        int debugMode = 0;

        int relay = 0 ;
        if(raytracks.size()<=2) return relay;
        for(int i=2; i<raytracks.size(); i++){
            double off = Math.pow(10,i-2);
            int ilay = (int) ( raytracks.get(i).get_type() - 10000)/100;
            if(debugMode==1)System.out.format(" layers %8d --> %3d %7.2f %5d \n",raytracks.get(i).get_type(),i,off,ilay);
            if (ilay==11){
                relay += off*2;
            }else{
                relay += off*1;
            }
        }
        return relay;

    }


    // ----------------
    public int get_RefleCompos() {
    // ----------------
    // return a coded flag with the component sequence alongt the photon path

        int recompo = 0 ;
        if(raytracks.size()<=2) return recompo;
        for(int i=2; i<raytracks.size(); i++){
            double off = Math.pow(10,i-2);
            int itype = (int) ( raytracks.get(i).get_type()/10000);
            int ilay = (int) ( raytracks.get(i).get_type() - 10000)/100;
            int icompo = 0;
            if(itype==2){
                //refraction onto aerogel
                if (ilay==11){
                    icompo = (int) ( raytracks.get(i).get_type() - 10000 - ilay*100 - 1);
                }else{
                    icompo = (int) ilay;
                }
            }
            if(itype==1){
                //reflection onto mirror
                if (ilay==11){
                    icompo = (int) ( raytracks.get(i).get_type() - 10000 - ilay*100 - 1);
                }else{
                    icompo = (int) ilay-4;
                }
            }
            recompo += off*icompo;
        }
        return recompo;

    }


    // ----------------
    public boolean is_Hit(int lay, int compo) {
    // ----------------
    // return true if the given component is hit by the photon along the path

        if(raytracks.size()<=2) return false;
        for(int i=2; i<raytracks.size(); i++){
            double off = Math.pow(10,i-2);
            int ilay = (int) ( raytracks.get(i).get_type() - 10000)/100;
            int icompo = 0;
            if (ilay==11){
                icompo = (int) ( raytracks.get(i).get_type() - 10000 - ilay*100 - 1);
            }else{
                icompo = 0;
            }
            if(ilay==lay && icompo==compo)return true;
        }
        return false;
    }


    // ----------------
    public boolean exist(){
    // ----------------
    // return true if a raytrace solution was found
        if(raytracks.size()<=1) {
            if(debugMode>0)System.out.format("No raytrace solution \n");
            return false;
        }
        if(!get_lastray().is_detected()){
            if(debugMode>0)System.out.format("No raytrace detected \n");
            return false;
        }
        return true;
    }


    // ----------------
    public int status(){ return status; }
    // ----------------
    
    // ----------------
    public void set_status(int isec, int lai, int ico, int iqua, RICHCalibration richcal) {
    // ----------------
    // record if there is a bad-status mirror on the photon path

        int debugMode = 0;

        if(!exist()) return;
        if(richcal.get_AeroStatus(isec, lai, ico, iqua)>0){status=1; return;} 
        if(raytracks.size()>2){
            for(int i=2; i<raytracks.size(); i++){
                double off = Math.pow(10,i-2);
                int ilay = (int) ( raytracks.get(i).get_type() - 10000)/100;
                int icompo = 0;
                if (ilay==11){
                    icompo = (int) ( raytracks.get(i).get_type() - 10000 - ilay*100 - 1);
                }else{
                    icompo = 0;
                }
                int check = richcal.get_MirrorStatus(isec, ilay, icompo+1);
                if(debugMode>0)System.out.format("check ray %3d %6d %3d %3d --> %3d \n",i,raytracks.get(i).get_type(),ilay,icompo, check);
                if(check>0){status=1; return;}
            }
        }
        return;
    }


    // ----------------
    public int get_FirstRefle() {
    // ----------------

        if(raytracks.size()>2) return raytracks.get(2).get_type();
        return 0;

    }

    // ----------------
    public int get_RefleType() {
    // ----------------

        int ifirst = get_FirstRefle();
        int ilay = (int) (ifirst-10000)/100;
        if(ifirst<10000){
            return 0;
        }else{
            if(ilay==11){
                return 2;
            }else{
                return 1;
            }
        }

    }

    // ----------------
    public int get_Nrefle() {
    // ----------------

        int debugMode = 0;

        if(debugMode==1)System.out.format("RICHSolution::get_Nrefle \n");
        int nrfl=0;
        int ira=0;
        for (RICHRay ray : raytracks) {
            int refe = (int) ray.get_type()/10000;
            if(refe == 1) nrfl++;
            if(debugMode==1)System.out.format(" ray %3d  type %6d  refe %3d  nrfl %4d \n",ira, ray.get_type(), refe, nrfl);
            ira++;
        }
        return nrfl;
    }

    // ----------------
    public int get_Nrefra() {
    // ----------------

        int nrfr=0;
        for (RICHRay ray : raytracks) {
            int refa = (int) ray.get_type()/10000;
            if(refa == 2) nrfr++;
        }
        return nrfr;
    }

    // ----------------
    public int get_nrefle() { return nrefle; }
    // ----------------

    // ----------------
    public int get_nrefra() { return nrefra; }
    // ----------------

    // -------------
    public int get_nrays() { return raytracks.size(); }
    // -------------

    // ----------------
    public RICHRay get_ray(int i){ return  this.raytracks.get(i); }
    // ----------------

    // -------------
    public RICHRay get_lastray() { return raytracks.get(raytracks.size()-1); }
    // -------------

    // ----------------
    public Point3D get_hit() { return hit; }
    // ----------------

    // ----------------
    public double get_ElProb() { return elprob; }
    // ----------------

    // ----------------
    public double get_PiProb() { return piprob; }
    // ----------------

    // ----------------
    public double get_KProb() { return kprob; }
    // ----------------

    // ----------------
    public double get_PrProb() { return prprob; }
    // ----------------

    // ----------------
    public double get_BgProb() { return bgprob; }
    // ----------------

    // ----------------
    public int get_Ndir() { return ndir; }
    // ----------------

    // ----------------
    public double get_Chdir() { return chdir; }
    // ----------------

    // ----------------
    public double get_RMSdir() { return sdir; }
    // ----------------

    // ----------------
    public double get_Madir() { return madir; }
    // ----------------

    // ----------------
    public int get_Nlat() { return nlat; }
    // ----------------

    // ----------------
    public double get_Chlat() { return chlat; }
    // ----------------

    // ----------------
    public double get_RMSlat() { return slat; }
    // ----------------

    // ----------------
    public double get_Malat() { return malat; }
    // ----------------

    // ----------------
    public int get_Nspe() { return nspe; }
    // ----------------

    // ----------------
    public double get_Chspe() { return chspe; }
    // ----------------

    // ----------------
    public double get_RMSspe() { return sspe; }
    // ----------------

    // ----------------
    public double get_Maspe() { return maspe; }
    // ----------------

    // ----------------
    public int get_Ntot() { return ntot; }
    // ----------------

    // ----------------
    public double get_Chtot() { return chtot; }
    // ----------------

    // ----------------
    public double get_RMStot() { return stot; }
    // ----------------

    // ----------------
    public double get_Matot() { return matot; }
    // ----------------

    // ----------------
    public int get_BestH(int charge) { 
    // ----------------
        int hypo_pid_ch = bestH;
        if(hypo_pid_ch==11){
            if(charge==1)hypo_pid_ch*=-1;
        }else{
            hypo_pid_ch = bestH*charge;
        }
        return hypo_pid_ch;
    }

    // ----------------
    public int get_BestH() { return bestH; }
    // ----------------

    // ----------------
    public void set_BestH(int bestH) { this.bestH = bestH;}
    // ----------------

    // ----------------
    public double get_BestCH() { return bestch; }
    // ----------------

    // ----------------
    public double get_BestRL() { return bestRL; }
    // ----------------

    // ----------------
    public double get_BestC2() { return bestc2; }
    // ----------------

    // ----------------
    public double get_BestNpho() { return bestNp; }
    // ----------------

    // ----------------
    public double get_BestMass() { return bestMass; }
    // ----------------

    // ----------------
    public int get_secH() { return secH; }
    // ----------------

    // ----------------
    public double get_Bestprob() { return bestprob; }
    // ----------------

    // ----------------
    public double get_secprob() { return secprob; }
    // ----------------

    // ----------------
    public double get_RQP() { return R_QP; }
    // ----------------

    // ----------------
    public double get_ReQP() { return Re_QP; }
    // ----------------

    // ----------------
    public double assign_LHCbPID(double lh[]) {
    // ----------------

        int debugMode = 0;

        set_ElProb(lh[0]);
        set_PiProb(lh[1]);
        set_KProb(lh[2]);
        set_PrProb(lh[3]);
        if(debugMode==1)System.out.format(" assign (LHCB) %10.4g [%10.4g %10.4g %10.4g] --> ",lh[0],lh[1],lh[2],lh[3]);

        int ibest=-1;
        double lhtest=0.0;
        for(int i=1; i<4; i++){
            if(lh[i]>lhtest){
                lhtest=lh[i];
                ibest=i;
            }
        }

        int isec=-1;
        if(ibest>=0){
            bestprob = lh[ibest];
            bestH = RICHConstants.HYPO_LUND[ibest];

            double test = 0.0;
            for(int i=1; i<4; i++){
                if(i!=ibest && lh[i]>test){
                    test=lh[i];
                    isec=i;
                }
            }
            if(isec>=0){
                secprob = lh[isec];
                R_QP  = 1-secprob/bestprob;
                secH  = RICHConstants.HYPO_LUND[isec];
            }else{
                R_QP  = 1.0;
            }
        }

        if(elprob>0){
            if(piprob>0){
                //ATT: pass2 works with only hadrons PID to not geenrate confusion.
                //if(elprob>piprob){
                //    Re_QP = 1-piprob/elprob;
                //    if(bestH==0 || bestH==RICHConstants.HYPO_LUND[1]) bestH=RICHConstants.HYPO_LUND[0];
                //}else{
                Re_QP = 1-elprob/piprob;
                //}
            }else{
                Re_QP = 1.0;
                bestprob = elprob;
                bestH=RICHConstants.HYPO_LUND[0];
            }
        }

        if(debugMode==1)System.out.format(" --> %5d (%10.4g) %5d (%10.4g) %8.4f %8.4f \n",bestH,bestprob,secH,secprob,R_QP,Re_QP);

        return R_QP;
    }


    // ----------------
    public double assign_HypoPID(double lh[]) {
    // ----------------

        int debugMode = 0;

        set_ElProb(lh[0]);
        set_PiProb(lh[1]);
        set_KProb(lh[2]);
        set_PrProb(lh[3]);
        if(debugMode==1)System.out.format(" assign (PASS2) %10.4g [%10.4g %10.4g %10.4g] --> ",lh[0],lh[1],lh[2],lh[3]);

        int ibest=-1;
        double lhtest=999.0;
        for(int i=1; i<4; i++){
            if(lh[i]>0 && lh[i]<lhtest){
                lhtest=lh[i];
                ibest=i;
            }
        }

        int isec=-1;
        if(ibest>=0){
            bestprob = lh[ibest];
            bestH = RICHConstants.HYPO_LUND[ibest];

            double test = 999.0;
            for(int i=1; i<4; i++){
                if(i!=ibest && lh[i]>0 && lh[i]<test){
                    test=lh[i];
                    isec=i;
                }
            }
            if(isec>=0){
                secprob = lh[isec];
                R_QP  = 1-bestprob/secprob;
                secH  = RICHConstants.HYPO_LUND[isec];
            }else{
                R_QP  = 1.0;
            }
        }

        if(elprob>0){
            if(piprob>0){
                //ATT: pass2 works with only hadrons PID to not geenrate confusion.
                //if(elprob<piprob){
                // Re_QP = 1-elprob/piprob;
                //    if(bestH==0 || bestH==RICHConstants.HYPO_LUND[1]) bestH=RICHConstants.HYPO_LUND[0];
                //}else{
                Re_QP = 1-piprob/elprob;
                //}
            }else{
                Re_QP = 1.0;
                bestprob = elprob;
                bestH=RICHConstants.HYPO_LUND[0];
            }
        }

        if(debugMode==1)System.out.format(" --> %5d (%10.4g) %5d (%10.4g) %8.4f %8.4f \n",bestH,bestprob,secH,secprob,R_QP,Re_QP);

        return R_QP;
    }


    // ----------------
    public double assign_PID(double lh[]) {
    // ----------------

        int debugMode = 0;

        set_ElProb(lh[0]);
        set_PiProb(lh[1]);
        set_KProb(lh[2]);
        set_PrProb(lh[3]);
        if(debugMode==1)System.out.format(" assign (PASS1) %10.4g [%10.4g %10.4g %10.4g] --> ",lh[0],lh[1],lh[2],lh[3]);

        double likeh[] = {lh[1], lh[2], lh[3]};
        Arrays.sort(likeh);
        bestprob = likeh[2];
        secprob  = likeh[1];

        if(bestprob>0){
            double likehr[] = {lh[1], lh[2], lh[3]};
            for (int i=0; i<3; i++){
                int hypo_pid = RICHConstants.HYPO_LUND[i+1];
                if(Math.abs(bestprob-likehr[i])<1e-6) bestH=hypo_pid;
                if(Math.abs(secprob-likehr[i])<1e-6) secH=hypo_pid; 
            }
            R_QP = 1-secprob/bestprob;
        }

        if(elprob>0){
            if(piprob>0){
                //ATT: pass2 works with only hadrons PID to not geenrate confusion.
                //if(elprob>piprob){
                //Re_QP = 1-piprob/elprob;
                //if(bestH==0 || bestH==RICHConstants.HYPO_LUND[1]) bestH=RICHConstants.HYPO_LUND[0];
                //}else{
                Re_QP = 1-elprob/piprob;
                //}
            }
        }
        if(debugMode==1)System.out.format(" --> %5d (%10.4g) %5d (%10.4g) %8.4f %8.4f \n",bestH,bestprob,secH,secprob,R_QP,Re_QP);

        return R_QP;
    }


    // ----------------
    public void set_type(int type) { this.type = type; }
    // ----------------

    // ----------------
    public void set_OK(int ok) { this.OK = ok; }
    // ----------------

    // ----------------
    public void set_hypo(int hypo) { this.hypo = hypo; }
    // ----------------

    // ----------------
    public void set_EtaC(double EtaC) { this.EtaC = EtaC; }
    // ----------------

    // ----------------
    public void set_aeron(double aeron) { this.aeron = aeron; }
    // ----------------

    // ----------------
    public void set_theta(double theta) { this.theta = theta; }
    // ----------------

    // ----------------
    public void set_phi(double phi) { this.phi = phi; }
    // ----------------

    // ----------------
    public void set_dthe_res(double dthe_res) { this.dthe_res = dthe_res; }
    // ----------------

    // ----------------
    public void set_dthe_bin(double dthe_bin) { this.dthe_bin = dthe_bin; }
    // ----------------

    // ----------------
    public void set_dphi_res(double dphi_res) { this.dphi_res = dphi_res; }
    // ----------------

    // ----------------
    public void set_dphi_bin(double dphi_bin) { this.dphi_bin = dphi_bin; }
    // ----------------

    // ----------------
    public void set_scale(double scale) { this.scale = scale; }
    // ----------------

    // ----------------
    public void set_path(double path) { this.path = path; }
    // ----------------

    // ----------------
    public void set_time(double time) { this.time = time; }
    // ----------------

    // ----------------
    public void set_machi2(double machi2) { this.machi2 = machi2; }
    // ----------------

    // ----------------
    public void set_nrefle(int nrefle) { this.nrefle = nrefle; }
    // ----------------
          
    // ----------------
    public void set_nrefra(int nrefra) { this.nrefra = nrefra; }
    // ----------------

    // ----------------
    public void add_ray(RICHRay ray) {this.raytracks.add(ray);}
    // ----------------

    // ----------------
    public ArrayList<RICHRay> get_raytracks() {return this.raytracks;}
    // ----------------

    // ----------------
    public void set_raytracks(ArrayList<RICHRay> rays){
    // ----------------

        int debugMode = 0;

        //ATT: Vedere se puo' succedere
        if(rays==null){if(debugMode>=1)System.out.format("No RAYTRACE solution\n"); return;}
        for (RICHRay ray: rays){
            this.raytracks.add(ray);
        }
        this.hit  = this.get_lastray().end();
        this.time = this.get_raytime();
        this.path = this.get_raypath();
        this.nrefle = this.get_Nrefle();
        this.nrefra = this.get_Nrefra();
    }

    
    // ----------------
    public void set_hit(Point3D hit) { this.hit = hit; }
    // ----------------

    // ----------------
    public void set_ElProb(double elprob) { this.elprob = elprob; }
    // ----------------

    // ----------------
    public void set_PiProb(double piprob) { this.piprob = piprob; }
    // ----------------

    // ----------------
    public void set_KProb(double kprob) { this.kprob = kprob; }
    // ----------------

    // ----------------
    public void set_PrProb(double prprob) { this.prprob = prprob; }
    // ----------------

    // ----------------
    public void set_BgProb(double bgprob) { this.bgprob = bgprob; }
    // ----------------

    // ----------------
    public void set_Ndir(int ndir) { this.ndir = ndir; }
    // ----------------

    // ----------------
    public void set_Chdir(double chdir) { this.chdir = chdir; }
    // ----------------

    // ----------------
    public void set_RMSdir(double sdir) { this.sdir = sdir; }
    // ----------------

    // ----------------
    public void set_Madir(double madir) { this.madir = madir; }
    // ----------------

    // ----------------
    public void set_Nlat(int nlat) { this.nlat= nlat; }
    // ----------------

    // ----------------
    public void set_Chlat(double chlat) { this.chlat= chlat; }
    // ----------------

    // ----------------
    public void set_RMSlat(double slat) { this.slat= slat; }
    // ----------------

    // ----------------
    public void set_Malat(double malat) { this.malat = malat; }
    // ----------------

    // ----------------
    public void set_Nspe(int nspe) { this.nspe= nspe; }
    // ----------------

    // ----------------
    public void set_Chspe(double chspe) { this.chspe= chspe; }
    // ----------------

    // ----------------
    public void set_RMSspe(double sspe) { this.sspe= sspe; }
    // ----------------

    // ----------------
    public void set_Maspe(double maspe) { this.maspe = maspe; }
    // ----------------

    // ----------------
    public void set_Ntot(int ntot) { this.ntot= ntot; }
    // ----------------

    // ----------------
    public void set_Chtot(double chtot) { this.chtot= chtot; }
    // ----------------

    // ----------------
    public void set_RMStot(double stot) { this.stot= stot; }
    // ----------------

    // ----------------
    public void set_Matot(double matot) { this.matot = matot; }
    // ----------------

    // ----------------
    public void set_BestCH(double bestch) { this.bestch = bestch; }
    // ----------------

    // ----------------
    public void set_BestRL(double bestRL) { this.bestRL = bestRL; }
    // ----------------

    // ----------------
    public void set_BestC2(double bestc2) { this.bestc2 = bestc2; }
    // ----------------

    // ----------------
    public void set_BestNpho(double bestNp) { this.bestNp = bestNp; }
    // ----------------

    // ----------------
    public void set_BestMass(double bestMass) { this.bestMass = bestMass; }
    // ----------------

    // ----------------
    public double get_dthe_pixel(){ if(dthe_res>0) return RICHConstants.PIXEL_NOMINAL_SIZE/dthe_res; return 1.0;}
    // ----------------

    // ----------------
    public double get_dphi_pixel(){ if(dphi_res>0) return RICHConstants.PIXEL_NOMINAL_SIZE/dphi_res; return 1.0;}
    // ----------------

    // ----------------
    public double get_EqPixelNumber(){
    // ----------------
 
        int debugMode = 0;
        double dthe_delta = dthe_res*dthe_bin;
        double dphi_delta = dphi_res*dphi_bin;
        double solid = dthe_delta/RICHConstants.PIXEL_NOMINAL_SIZE*dphi_delta/RICHConstants.PIXEL_NOMINAL_SIZE;

        if(debugMode>=1)System.out.format(" Eq Pixel %7.2f %7.2f (%7.2f) --> %8.4f \n",dthe_delta,dphi_delta,RICHConstants.PIXEL_NOMINAL_SIZE,solid);
        return solid;
    }

    // ----------------
    public void show_raytrack() {
    // ----------------

        int ii=0;
        for(RICHRay ray: raytracks){
            System.out.format(" %d",ii);
            ray.showRay();
            ii++;
        }
    }


    // ----------------
    public void dump_raytrack(String head) {
    // ----------------

        int ii=0;
        for(RICHRay ray: raytracks){
            if(head!=null){
                System.out.format("%s",head);
            }
            System.out.format(" %8d %8d %8d ",ii,get_RefleLayers(),get_RefleCompos());
            ray.dumpRay();
            ii++;
        }
    }
    

    // ----------------
    public void showSolution() {
    // ----------------
        System.out.format("SOL type %3d  EtaC %8.3f  n %6.4f  the %7.3f  phi %7.3f  hit %s  path %6.1f  time %6.2f  nrfl %2d  nfr %2d  pel %7.5f  pi %7.5g  k %7.5g  pr %7.5g  bg %7.5g \n",
             get_type(), get_EtaC(), get_aeron(), get_theta(), get_phi(), get_hit().toStringBrief(2), get_path(), get_time(), get_nrefle(), get_nrefra(), 
             get_ElProb(), get_PiProb(), get_KProb(), get_PrProb(), get_BgProb());
    }
            
}
