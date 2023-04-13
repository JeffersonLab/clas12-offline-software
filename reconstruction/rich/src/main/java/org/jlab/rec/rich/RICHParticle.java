package org.jlab.rec.rich;

import org.jlab.detector.geom.RICH.RICHIntersection;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;

import java.util.ArrayList;

import org.jlab.io.base.DataEvent;

import org.jlab.detector.base.DetectorType;

import org.freehep.math.minuit.FCNBase;
import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnScan;
import org.freehep.math.minuit.MnUserParameters;

import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;

import org.jlab.detector.geom.RICH.RICHRay;
import org.jlab.detector.geom.RICH.RICHIntersection;
import org.jlab.detector.geom.RICH.RICHGeoConstants;


public class RICHParticle {

    /*
    * A particle in the RICH consists of an array of ray lines plus particle information 
    */

    private int      id                = -999;
    private int      parent_index      = -999;
    private int      type              = 0;
    private int      recofound         = -1;
    private int      sector            = -1;
    private int      status            = -1;
    private int      CLASpid           = -999;
    private int      RICHpid           = -999;
    private double   momentum          = 0; 
    private int      charge            = 0; 
    //private double[][] ChAngle         = new double[4][3];
    //private double[] sChAngle          = new double[3];

    private RICHHit  hit               = new RICHHit();

    public Point3D   lab_emission      = null;
    public int       ilay_emission     = -1;
    public int       ico_emission      = -1;
    public int       ico_entrance      = -1;
    public int       iqua_emission     = -1;
    public double    refi_emission     = 0;
    public double    chele_emission[]  = {0.0, 0.0, 0.0};
    public double    schele_emission[] = {0.0, 0.0, 0.0};
    public double    nchele_emission[] = {0.0, 0.0, 0.0};
    public double    lab_phi           = 0.0;
    public double    lab_theta         = 0.0;

    public double     pixel_gain        = 0.0;
    public double     pixel_eff         = 0.0;
    public double     pixel_mtime       = 0.0;
    public double     pixel_stime       = 0.0;
    public double     pixel_backr       = 0.0;

    // rotated frame into RICH local coordinates (used for the analytic solution)
    public double    RotAngle          = 0.0;
    public Vector3D  RotAxis           = null;
    public Point3D   reference         = null;
    public Point3D   ref_emission      = null;
    public Point3D   ref_impact        = null;    // P_P in Evaristo's note
    public Point3D   ref_proj          = null;    // P_PCP in Evaristo's note
    public double    ref_phi           = 0.0;
    public double    ref_theta         = 0.0;
    public double    EtaC_ref          = 0.0;

    private double   start_time        = 0.0;

    public RICHSolution analytic       = new RICHSolution(0);
    public RICHSolution traced         = new RICHSolution(1);

    public RICHParticle trial_pho      = null;

    public Point3D      aero_entrance  = new Point3D(0.,0.,9999.);
    public Point3D      aero_exit      = new Point3D(0.,0.,0.);
    public Point3D      aero_middle    = null;
    public Vector3D     aero_normal    = null;

    public Line3D       direct_ray     = new Line3D(0.0,0.0,0.0,0.0,0.0,0.0);
    public double       path           = 0.0;

    private final static  RICHGeoConstants  geocost = new RICHGeoConstants();
    private               RICHParameters richpar;

    private static double MRAD = geocost.MRAD;
    private static double RAD  = geocost.RAD;

    // -------------
    public RICHParticle(int id, DetectorParticle p, DetectorResponse exr, RICHParameters richpar){
    // -------------
    /*
    * RICHParticle from a CLAS charged particle for RICH reconstruction
    */
        int debugMode = 0;

        this.id = id;
        // ATT: vedere se si puo' evitare tenendolo fuori
        this.parent_index  = p.getTrackStatus();
        this.momentum      = p.vector().mag();
        set_CLASpid(p.getPid());
        this.sector        = p.getTrackSector();
        this.charge        = p.getCharge();

        // exr already account for track extrapolation at the RICH surface and possible cluster match
        this.hit.set_id( exr.getHitIndex() );
        this.hit.set_Position( exr.getPosition().toPoint3D() );
        this.hit.set_Time( exr.getTime() );
        this.hit.set_sector( exr.getDescriptor().getSector() );
        this.hit.set_pmt( exr.getDescriptor().getLayer() );
        this.hit.set_anode( exr.getDescriptor().getComponent() );

        this.richpar       = richpar;

        this.direct_ray    = p.getFirstCross(); 
        int detid          = DetectorType.RICH.getDetectorId();;
        this.path          = p.getTrackTrajectory().get(detid,0).getPathLength();
        if(richpar.FORCE_DC_MATCH==1){
            if(debugMode>=1)System.out.println(" FORCE DC-RICH match \n");
            this.direct_ray = new Line3D(p.getFirstCross().origin(), exr.getPosition().toPoint3D());
        }
        this.lab_phi       = direct_ray.toVector().phi();
        this.lab_theta     = direct_ray.toVector().theta();
        this.status        = exr.getStatus();

    }


    // -------------
    public RICHParticle(int id, RICHParticle hadron, DetectorResponse hit, Point3D point, RICHParameters richpar){
    // -------------
    /*
    *  RICHParticle as new particle for RICH reconstruction
    */

        this.id = id;
        this.parent_index = hadron.get_id();
        this.momentum  = 1.e-6;
        this.CLASpid   = 22;
        this.sector    = hadron.get_sector();

        this.start_time = hadron.get_StartTime();
        if (hit!=null){
            // photon from detected hit
            this.hit.set_id( hit.getHitIndex() );
            this.hit.set_Position( hit.getPosition().toPoint3D() );
            this.hit.set_Time( hit.getTime() );
            this.hit.set_sector( hit.getDescriptor().getSector() );
            this.hit.set_pmt( hit.getDescriptor().getLayer() );
            this.hit.set_anode( hit.getDescriptor().getComponent() );
        }else{
            // photon from throw particle
            this.hit.set_Position( point );
        }

        this.lab_emission  = hadron.lab_emission;
        this.ilay_emission = hadron.ilay_emission;
        this.ico_emission  = hadron.ico_emission;

        this.direct_ray = new Line3D(this.lab_emission, this.hit.get_Position());
        this.lab_phi = direct_ray.toVector().phi();
        this.lab_theta = direct_ray.toVector().theta();

        this.richpar  = richpar;

    }


    // -------------
    public void set_CLASpid(int pid)
    // -------------
    /*
    * Set the particle pid that generates the track, take piion in case of doubt 
    * @param int pid, the identifier of the particle  (only gamma, e, pi, proton and k are allowed)
    */
    {
        if (Math.abs(pid)==22 || Math.abs(pid)==11 || Math.abs(pid)==211 | Math.abs(pid)==321 || Math.abs(pid)==2212) {
            this.CLASpid = pid;
        }else{
            this.CLASpid=211;
            if(pid<0)this.CLASpid*=-1;
        }
    }


    // -------------
    private double get_mass(int pid) {
    // -------------

        if (Math.abs(pid)==22 || Math.abs(pid)==11 || Math.abs(pid)==211 | Math.abs(pid)==321 || Math.abs(pid)==2212) {
            return PDGDatabase.getParticleById(pid).mass();
        }else{
            return 0.0;
        }

    }


    /*
    // -------------
    private void set_changle() {
    // -------------
    // define only one Cherenkov angle depending on the pid hypothesis
    
        int debugMode = 0;
        for(int j=0; j<3; j++) {
            for(int k=0 ; k<4; k++) ChAngle[k][j] = 0.0;
            sChAngle[j] = 0.0;
        }

        for (int iref=0; iref<3; iref++){
            if(richpar.USE_ELECTRON_ANGLES==1){
                ChAngle[0][iref] = calibrated_ChAngle(11, iref);      // expected angle for electron
                ChAngle[1][iref] = calibrated_ChAngle(211, iref);     // pion
                ChAngle[2][iref] = calibrated_ChAngle(321, iref);     // kaon
                ChAngle[3][iref] = calibrated_ChAngle(2212, iref);    // proton

                sChAngle[iref]   = calibrated_sChAngle(iref);      // expected angle for electron

            }else{

                ChAngle[0][iref] = nominal_ChAngle(11);      // expected angle for electron
                ChAngle[1][iref] = nominal_ChAngle(211);     // pion
                ChAngle[2][iref] = nominal_ChAngle(321);     // kaon
                ChAngle[3][iref] = nominal_ChAngle(2212);    // proton

                sChAngle[iref]   = nominal_sChAngle();      // expected angle for electron
            }
        }

        if(debugMode>=1)  {
            System.out.format(" Create RICH particle mom %8.2f \n",momentum);
            for(int ir=0; ir<3; ir++) System.out.format("      --> Refle %3d  pr %7.2f k  %7.2f  pi %7.2f  e %7.2f -->  limi  %7.2f %7.2f  res %7.2f\n",ir,
                         ChAngle[3][ir]*MRAD,ChAngle[2][ir]*MRAD,ChAngle[1][ir]*MRAD,ChAngle[0][ir]*MRAD,min_changle(ir)*MRAD,max_changle(ir)*MRAD,sChAngle[ir]*MRAD);
        }


        if(debugMode>=1)  {
            System.out.format(" ============================= \n");
            dump_ChAngle();
            System.out.format(" ============================= \n");
        }
    }*/


    // -------------
    public double changle(int pid, int iref) {
    // -------------
    
        if(richpar.USE_ELECTRON_ANGLES==1) return calibrated_ChAngle(pid, iref);
        return nominal_ChAngle(pid);

    }


    // -------------
    public double schangle(int iref) {
    // -------------
    
        if(richpar.USE_ELECTRON_ANGLES==1) return calibrated_sChAngle(iref);
        return nominal_sChAngle();

    }


    // -------------
    public double nchangle(int pid, int iref) {
    // -------------
    
        if(richpar.USE_ELECTRON_ANGLES==1) return calibrated_nChAngle(pid, iref);
        return nominal_nChAngle(pid);

    }


    // -------------
    public double chgain() {
    // -------------

        if(richpar.USE_CALIBRATED_PIXELS==1) return pixel_gain;
        return RICHConstants.PIXEL_NOMINAL_GAIN;

    }


    // -------------
    public double chbackgr() {
    // -------------

        int debugMode = 0;

        if(debugMode>=1)System.out.format("Pixel back %7.2f %7.2f (1e-9)\n",pixel_backr*1e9,richpar.PIXEL_NOMINAL_DARKRATE*1e9);
        if(richpar.USE_CALIBRATED_PIXELS==1 && pixel_backr>0) return pixel_backr;
        return richpar.PIXEL_NOMINAL_DARKRATE;

    }


    // -------------
    public double cheff() {
    // -------------

        if(richpar.USE_CALIBRATED_PIXELS==1) return pixel_eff;
        return RICHConstants.PIXEL_NOMINAL_EFF;

    }


    // -------------
    public double chtime() {
    // -------------

        if(richpar.USE_CALIBRATED_PIXELS==1) return pixel_mtime;
        return RICHConstants.PIXEL_NOMINAL_TIME;

    }


    // -------------
    public double schtime() {
    // -------------

        if(richpar.USE_CALIBRATED_PIXELS==1) return pixel_stime;
        return richpar.PIXEL_NOMINAL_STIME;

    }


    // -------------
    public double get_chindex(int hypo_pid, int irefle) {
    // -------------
    
        return 1./get_beta(hypo_pid)/Math.cos(changle(hypo_pid,irefle));

    }


    // -------------
    private void dump_ChAngle() {
    // -------------
    
        for( int ir=0; ir<RICHConstants.N_PATH; ir++){

            System.out.format(" CHER P %6.2f  %s ",momentum,RICHConstants.PATH_TYPE[ir]);
            for (int ip=RICHConstants.N_HYPO-1; ip>-1; ip--){

                int pid = RICHConstants.HYPO_LUND[ip];
                System.out.format(" %s %7.2f ",RICHConstants.HYPO_STRING[ip],changle(pid,ir)*MRAD);
            }
            System.out.format(" --> limi %7.2f %7.2f  res %7.2f\n",min_changle(ir)*MRAD, max_changle(ir)*MRAD, schangle(ir)*MRAD );

        }
    }


    // -------------
    public int get_id() {return id;}
    // -------------

    // -------------
    public int get_ParentIndex() {return parent_index;}
    // -------------

    // -------------
    public int get_HitIndex() {return hit.get_id();}
    // -------------

    // -------------
    public int get_type() {return type;}
    // -------------

    // -------------
    public boolean is_throw() {if(get_type()>10)return true; return false;}
    // -------------

    // -------------
    public boolean is_real() {if(get_type()<10)return true; return false;}
    // -------------

    // -------------
    public int get_recofound() {return recofound;}
    // -------------

    // -------------
    public int get_Status() {return status;}
    // -------------

    // -------------
    public int get_CLASpid() {return CLASpid;}
    // -------------

    // -------------
    public int get_RICHpid() {return RICHpid;}
    // -------------

    // -------------
    public double get_StartTime() {return start_time;}
    // -------------

    // -------------
    public double get_HitTime() {return hit.get_Time();}
    // -------------

    // -------------
    public int get_HitSector() {return hit.get_sector();}
    // -------------

    // -------------
    public int get_HitPMT() {return hit.get_pmt();}
    // -------------

    // -------------
    public int get_HitAnode() {return hit.get_anode();}
    // -------------

    // -------------
    //public double get_changle(int ipar, int irefle) { return ChAngle[ipar][irefle]; }
    // -------------

    // -------------
    public double get_beta(int pid) {
    // -------------
    // return the beta depending on the given pid hypothesis and particle momentum
    
        // no need to calculate anything for photons
        if(pid==22)return 1.0;

        if (Math.abs(pid)==11 || Math.abs(pid)==211 | Math.abs(pid)==321 || Math.abs(pid)==2212) {
            double mass = get_mass(pid);
            if (this.momentum==0 && mass==0) return 0.0;
            return this.momentum/Math.sqrt(this.momentum*this.momentum+mass*mass);
        }else{
            return 0.0;
        }

    }


    // ----------------
    public double max_changle(int irefle) {
    // ----------------

        for(int ip=0 ; ip<RICHConstants.N_HYPO; ip++) {
            int pid = RICHConstants.HYPO_LUND[ip];
            if(changle(pid,irefle)>0)return changle(pid,irefle) + richpar.NSIGMA_CHERENKOV * schangle(irefle);
        }
        return 0.0;
    }


    /*
    // ----------------
    public double maxChAngle(int irefle) {
    // ----------------

        for(int k=0 ; k<4; k++) if(ChAngle[k][irefle]>0)return ChAngle[k][irefle] + 3*sChAngle[irefle];
        return 0.0;
    }*/


    // ----------------
    public double min_changle(int irefle) {
    // ----------------

        for(int ip=RICHConstants.N_HYPO-1 ; ip>-1; ip--) {
            int pid = RICHConstants.HYPO_LUND[ip];
            if(changle(pid,irefle)>0)return Math.max( RICHConstants.RICH_MIN_CHANGLE, changle(pid,irefle) - richpar.NSIGMA_CHERENKOV * schangle(irefle));
        }
        return 0.0;
    }


    /*
    // ----------------
    public double minChAngle(int irefle) {
    // ----------------
    // calculate the minimum Cherenlov angle compatible with the momentum 
  
        for(int k=3 ; k>=0; k--) if(ChAngle[k][irefle]>0) return Math.max(RICHConstants.RICH_MIN_CHANGLE, ChAngle[k][irefle] - 3*sChAngle[irefle]);
        return 0.0;
    }*/


    // ----------------
    public double nominal_sChAngle(){
    // ----------------

        return richpar.RICH_NOMINAL_SANGLE;
    }


    // ----------------
    public double nominal_ChAngle(int pid){
    // ----------------
    // calculate the cherenkov angle from the aerogel 
    // nominal refractive index for the four accepted
    // particle hypothesis

        int debugMode = 0;

        int ok = 0;
        for (int ip=0; ip<RICHConstants.N_HYPO; ip++)if(Math.abs(pid) == RICHConstants.HYPO_LUND[ip])ok=1;
        if(ok==0) return 0.0;

        double arg = 0.0;
        double beta = get_beta(pid);
        if(beta>0) arg = 1.0/beta/refi_emission;
        if(debugMode>=1)  System.out.format(" Nominal Ch Angle %8.4f  beta %8.4f  n %7.3f  arg %8.4f\n",get_mass(pid),beta,refi_emission, Math.acos(arg)*MRAD);

        if(arg>0.0 && arg<1.0) return Math.acos(arg);
        return 0.0;
    }


    // ----------------
    public double nominal_nChAngle(int pid){
    // ----------------

        int debugMode = 0;

        int ok = 0;
        for (int ip=0; ip<RICHConstants.N_HYPO; ip++)if(Math.abs(pid) == RICHConstants.HYPO_LUND[ip])ok=1;
        if(ok==0) return 0.0;

        double ratio = Math.pow(Math.sin(changle(pid,0)),2)/Math.pow(Math.sin(changle(11,0)),2);
        double nele  = richpar.RICH_NOMINAL_NELE;

        if(debugMode>=1)  System.out.format(" Expected N photo PID %5d  Ne %7.2f r %7.2f -->   n %7.3f \n",pid,nele,ratio,nele*ratio);

        return nele*ratio;
    }


    // ----------------
    public double calibrated_sChAngle(int irefle){
    // ----------------

        return schele_emission[irefle];
    }


    // ----------------
    public double calibrated_ChAngle(int pid, int irefle){
    // ----------------
    // recalculate the expected Cherenkov angle
    // for the given particle hypothesis starting
    // from the electron measured values 

        int debugMode = 0;

        int ok = 0;
        for (int ip=0; ip<RICHConstants.N_HYPO; ip++)if(Math.abs(pid) == RICHConstants.HYPO_LUND[ip])ok=1;
        if(ok==0 || irefle<0 || irefle>RICHConstants.N_PATH) return 0.0;

        double beta = get_beta(pid);
        double cose = Math.cos(chele_emission[irefle]);

        double arg = 0.0;
        if(beta>0) arg = 1.0/beta*cose;
        if(debugMode>=1)  System.out.format(" Calibrated Ch Angle %7.2f %8.4f  beta %8.4f  n %7.3f  arg %8.4f [%4d %4d]\n",
              chele_emission[irefle]*MRAD,get_mass(pid),beta,refi_emission, Math.acos(arg)*MRAD, ilay_emission, ico_emission);

        if(arg>0.0 && arg<1.0) return Math.acos(arg);
        return 0.0;
    }


    // ----------------
    public double calibrated_nChAngle(int pid, int irefle){
    // ----------------
    // recalculate the expected Cherenkov photon yield
    // for the given particle hypothesis starting
    // from the electron measured values 

        int debugMode = 0;

        int ok = 0;
        for (int ip=0; ip<RICHConstants.N_HYPO; ip++)if(Math.abs(pid) == RICHConstants.HYPO_LUND[ip])ok=1;
        if(ok==0 || irefle<0 || irefle>RICHConstants.N_PATH) return 0.0;

        double ratio = Math.pow(Math.sin(changle(pid,0)),2)/Math.pow(Math.sin(changle(11,0)),2);
        double nele  = nchele_emission[irefle];

        if(debugMode>=1)  System.out.format(" Expected N photo PID %5d  Ne %7.2f r %7.2f -->   n %7.3f \n",pid,nele,ratio,nele*ratio);

        return nele*ratio;
    }


    // ----------------
    public void set_id(int id) { this.id=id;}
    // ----------------

    // ----------------
    public void set_HitIndex(int hit_id) { this.hit.set_id(hit_id);}
    // ----------------

    // ----------------
    public void set_parent_index(int parent_id) { this.parent_index=parent_id;}
    // ----------------

    // ----------------
    public void set_type(int type) { this.type=type; }
    // ----------------

    // ----------------
    public void set_recofound(int recof) { this.recofound=recof; }
    // ----------------

    // ----------------
    public void set_Status(int sta) { this.status = sta; }
    // ----------------

    // ----------------
    public void set_sector(int sector) { this.sector=sector; }
    // ----------------

    // ----------------
    public int get_sector() { return this.sector; }
    // ----------------

    // ----------------
    public void set_momentum(double momentum) { this.momentum=momentum; }
    // ----------------

    // ----------------
    public double get_momentum() { return this.momentum; }
    // ----------------

    // ----------------
    public int charge() { return this.charge; }
    // ----------------

    // ----------------
    public void set_HitPos( Point3D impa){ this.hit.set_Position(impa); }
    // ----------------

    // ----------------
    public Point3D get_HitPos(){ return this.hit.get_Position(); }
    // ----------------

    // ----------------
    public void set_StartTime(double time){ this.start_time = time; }
    // ----------------

    // ----------------
    public void set_HitTime(double time){ this.hit.set_Time(time); }
    // ----------------

    // -------------
    public void set_RICHpid(int hpid){
    // -------------
        RICHpid = hpid;
        if(this.CLASpid<0)RICHpid*=-1;
    }

    // ----------------
    public void set_PixelProp(RICHCalibration richcal){
    // ----------------

        int debugMode = 0;
   
        int isec = hit.get_sector();
        int ipmt = hit.get_pmt();
        int ich  = hit.get_anode();

        pixel_gain  = richcal.get_PixelGain(isec, ipmt, ich);
        pixel_eff   = richcal.get_PixelEff(isec, ipmt, ich);
        pixel_mtime = richcal.get_PixelMeanTime(isec, ipmt, ich);
        pixel_stime = richcal.get_PixelRMSTime(isec, ipmt, ich);
        pixel_backr = richcal.get_PixelDarkRate(isec, ipmt, ich)*1e-9;

        if(debugMode>=1) System.out.format("Photon pixel %4d %4d %4d --> %7.2f %7.2f %7.2f %7.2f %7.3f (e-9) \n",hit.get_id(), ipmt-1, ich-1, 
            pixel_gain, pixel_eff, pixel_mtime, pixel_stime, pixel_backr*1e9);

    }


    // ----------------
    public boolean find_AerogelPoints(RICHRayTrace richtrace, RICHCalibration richcal){
    // ----------------
    /*
    * Search for the intersection points on the aerogel along forward trajectory
    * Take the first (in z) with track pointing inside as Entrance
    * Take the last (in z) with track pointing outside as Exit
    */

        int debugMode = 0;
        boolean found_exit = false;
        boolean found_entrance = false;
        RICHIntersection compo     = null;

        if(debugMode>=1){ 
            if(debugMode>=3)  System.out.println(" \n RICHParticle::set_aerogel_points \n");
            System.out.format(" from ray %s \n",direct_ray.origin().toStringBrief(2));
            if(debugMode>=3)  System.out.println(" Look for intersection with layer 201 \n");
        }
        RICHIntersection entrance  = richtrace.get_Layer(sector, "AEROGEL_2CM_B1").find_Entrance(direct_ray, -1);
        if(debugMode>=1 && entrance!=null)  System.out.format(" B1 entrance %4d %6d  %s \n",entrance.get_layer(),entrance.get_component(), entrance.get_pos().toStringBrief(2));
        RICHIntersection exit      = richtrace.get_Layer(sector, "AEROGEL_2CM_B1").find_Exit(direct_ray, -1);
        if(debugMode>=1 && exit!=null)      System.out.format(" B1 exit     %4d %6d  %s \n",exit.get_layer(),exit.get_component(), exit.get_pos().toStringBrief(2));

        if(entrance==null || exit==null){
            if(debugMode>=3)  System.out.println(" Look for intersection with layer 202 \n");
            entrance  = richtrace.get_Layer(sector, "AEROGEL_2CM_B2").find_Entrance(direct_ray, -1);
            if(debugMode>=1 && entrance!=null)  System.out.format(" B2 entrance %4d %6d  %s \n",entrance.get_layer(),entrance.get_component(), entrance.get_pos().toStringBrief(2));
            exit      = richtrace.get_Layer(sector, "AEROGEL_2CM_B2").find_Exit(direct_ray, -1);
            if(debugMode>=1 && exit!=null)      System.out.format(" B2 exit     %4d %6d  %s \n",exit.get_layer(),exit.get_component(), exit.get_pos().toStringBrief(2));
        }

        if(entrance==null || exit==null){
            if(debugMode>=3)  System.out.println(" Look for intersection with layer 203/204 \n");
            entrance  = richtrace.get_Layer(sector, "AEROGEL_3CM_L2").find_Entrance(direct_ray, -1);
            if(debugMode>=1 && entrance!=null)  System.out.format(" B3 entrance %4d %6d  %s \n",entrance.get_layer(),entrance.get_component(), entrance.get_pos().toStringBrief(2));
            exit      = richtrace.get_Layer(sector, "AEROGEL_3CM_L1").find_Exit(direct_ray, -1);
            if(debugMode>=1 && exit!=null)      System.out.format(" B3 exit     %4d %6d  %s \n",exit.get_layer(),exit.get_component(), exit.get_pos().toStringBrief(2));
        }

        if(entrance==null || exit==null){
            if(debugMode>=1)System.out.format(" No intersection with aerogel plane found \n");
            return false;
        }

        aero_entrance  = entrance.get_pos();
        aero_exit      = exit.get_pos();
        aero_normal    = exit.get_normal();

        /*
        *   Take point at 3/4 of path inside aerrogel  
        */
        Point3D amiddle = aero_exit.midpoint(aero_entrance);
        aero_middle    = aero_exit.midpoint(amiddle);
        lab_emission   = aero_middle;
        // take the downstream aerogel tile as the one with largest number of photons and average emission point
        ilay_emission  = exit.get_layer();
        ico_emission   = exit.get_component();
        ico_entrance   = entrance.get_component();
        refi_emission  = richtrace.get_Component(sector, ilay_emission,ico_emission).get_index();

        int Nqua = richpar.QUADRANT_NUMBER;
        iqua_emission = richtrace.get_Layer(sector, ilay_emission).get_Quadrant(Nqua, ico_emission, exit.get_pos());

        if(debugMode>=1){
            System.out.format(" AERO lay %3d ico %3d  qua  %3d \n",ilay_emission,ico_emission,iqua_emission);
            if(entrance.get_layer()!=ilay_emission || entrance.get_component()!=ico_emission)
                System.out.format(" AERO CROSS ilay %4d %4d  ico %4d %4d \n",entrance.get_layer(),ilay_emission,entrance.get_component(),ico_emission);
        }
        // perform photon reconstruction, to be saved in RICH::Ring
        /*if(richcal.get_AeroStatus(sector, ilay_emission, ico_emission)>0){
            if(debugMode>=1)System.out.format(" AERO bad status: disregard \n");
            return false;
        }*/
       
        if(richpar.USE_ELECTRON_ANGLES==1){
            for(int iref=0; iref<3; iref++){
                chele_emission[iref]  = richcal.get_ChElectron(sector, ilay_emission, ico_emission, iqua_emission, iref, charge);
                schele_emission[iref] = richcal.get_SChElectron(sector, ilay_emission, ico_emission, iqua_emission, iref, charge);
                nchele_emission[iref] = richcal.get_NElectron(sector, ilay_emission, ico_emission, iqua_emission, iref, charge);
                if(debugMode>=1 && iref==0)System.out.format(" AERO [%4d %4d] iref %4d --> %8.3f  %7.2f \n",
                    ilay_emission, ico_emission, iref, chele_emission[iref]*MRAD,nchele_emission[iref]);
            }
        }

        //set_changle();

        Vector3D Z_ONE = new Vector3D(0., 0., 1);
        RotAxis  = aero_normal.cross(Z_ONE).asUnit();
        RotAngle = aero_normal.angle(Z_ONE);

        double dist = aero_middle.distance(direct_ray.origin());
        if(aero_middle.z()-direct_ray.origin().z()<0) dist*=-1;
        start_time += (path + dist)/get_beta(CLASpid)/(PhysicsConstants.speedOfLight());

        if(debugMode>=3){
            System.out.println(" AERO entrance %s "+aero_entrance.toStringBrief(2));
            System.out.println(" AERO middle   %s "+aero_middle.toStringBrief(2));
            System.out.println(" AERO exit     %s "+aero_exit.toStringBrief(2));
            System.out.println(" AERO normal   %s "+aero_normal.toStringBrief(2));
            System.out.println(" AERO layer       "+ilay_emission);
            System.out.println(" AERO compo       "+ico_emission);
            System.out.println(" AERO refi        "+refi_emission);
        }

        return true;
    }


    // ----------------
    public void set_rotated_points(RICHParticle hadron){
    // ----------------

        this.reference    = hadron.reference;
        Quaternion q      = new Quaternion(hadron.RotAngle, hadron.RotAxis);

        this.ref_emission = q.rotate(this.lab_emission.vectorFrom(this.reference)).toPoint3D();
        this.ref_impact   = q.rotate(this.hit.get_Position().vectorFrom(this.reference)).toPoint3D();

        this.ref_phi      = ref_impact.toVector3D().phi();
        this.ref_theta    = ref_impact.toVector3D().theta();
        this.ref_proj     = hadron.ref_proj;

        this.EtaC_ref     = calc_EtaC(hadron, this.ref_theta);

    }


    // ----------------
    public boolean set_rotated_points() {
    // ----------------

        int debugMode = 0;
        if(lab_emission==null)return false;

        if(debugMode>=2)  System.out.println(" \n RICHParticle::set_rotated_points \n");

        // define an arbitrary reference point, here taken to be the photon emission point
        reference = lab_emission;

        // Define the rotation from aerogel normal to the z axis
        Quaternion q = new Quaternion(RotAngle, RotAxis.asUnit());

        // rotate into the new reference system
        ref_emission = q.rotate( lab_emission.vectorFrom(reference)).toPoint3D();
        ref_impact   = q.rotate( hit.get_Position().vectorFrom(reference)).toPoint3D();

        ref_proj = new Point3D(ref_emission.x(), ref_emission.y(), ref_impact.z()-ref_emission.z());

        this.ref_phi = ref_impact.toVector3D().phi();
        this.ref_theta = ref_impact.toVector3D().theta();
       
        if(debugMode>=2){
            System.out.format(" --> Track projection (P_PCP) %s ",ref_proj.toStringBrief(2));
            System.out.format(" --> Track impact (P_P)       %s ",ref_impact.toStringBrief(2));
            System.out.format(" --> Track angles             %8.2f %8.2f \n", this.ref_theta*57.3, this.ref_phi*57.3);
        }

        return true;
    }


    // ----------------
    public double calc_EtaC (RICHParticle hadron, double theta) {
    // ----------------
        // direct tracing without aerogel refraction

        double Theta_P = hadron.ref_theta;
        double Phi_P = hadron.ref_phi;

        double Cos_EtaC = Math.sin(Theta_P)* Math.sin(theta)*Math.cos(this.ref_phi-Phi_P)+Math.cos(Theta_P)*Math.cos(theta);

        return Math.acos(Cos_EtaC);

    }


    
    // ----------------
    public double time_probability(double testtime, int recotype) {
    // ----------------
    /*
    * calculate probability for a given time hypothesis
    */
        int debugMode = 0;

        RICHSolution reco = new RICHSolution();
        if(recotype==0) reco = analytic;
        if(recotype==1) reco = traced;

        // timing probability
        double meant = start_time + reco.get_time();
        double sigmat = richpar.PIXEL_NOMINAL_STIME;

        double funt = 0.0;
        double dfunt = 1;

        if(meant>0){
            funt = Math.exp((-0.5)*Math.pow((testtime - meant)/sigmat, 2) )/ (sigmat*Math.sqrt(2* Math.PI));
        }
        
        double prob = funt*dfunt;
        double back = richpar.PIXEL_NOMINAL_DARKRATE*richpar.NSIGMA_TIME*richpar.PIXEL_NOMINAL_STIME;

        if(debugMode>=1)if(prob>back)System.out.format(
                     "TIM prob   meant %8.3f  time %8.3f -->  %g  %g \n",
                     meant,testtime,funt*dfunt,Math.log(prob)); 
        return prob;

    }

    
    // ----------------
    public double pid_probability(RICHParticle hadron, int hypo_pid, int recotype) {
    // ----------------
    /*
    * calculate probability for a given pid hypothesis
    * based on the particle momentum and RICH resolution
    * at the moment, equal photon generation and particle
    * flux is assumed, actually close to Cherenkov threshold
    * the photon yiled decreases and pions outnumber koans.
    */

        int debugMode = 0;
        RICHSolution reco = new RICHSolution();
        if(recotype==0) reco = analytic;
        if(recotype==1) reco = traced;

        double func = 0.0;
        double dfunc = 1e-3;

        double funt = 0.0;
        double dfunt = 1;

        double mean = 0.0;
        double sigma = 0.0;
        double recot = 0.0;
        double meant = 0.0;
        double sigmat = 0.0;

        int pixel_flag = 0;
        if(pixel_flag==0){

            // angle probability

            int irefle = reco.get_RefleType();
            if(irefle>=0 && irefle<=2){
                mean=hadron.changle(hypo_pid, irefle);
                sigma = hadron.schangle(irefle);
            }

            if(mean>0 && sigma>0){
                func = Math.exp((-0.5)*Math.pow((reco.get_EtaC() - mean)/sigma, 2) )/ (sigma*Math.sqrt(2* Math.PI));
            }
            
            // timing probability
            recot = start_time + reco.get_time();
            meant = pixel_mtime;
            sigmat = pixel_stime;

            if(recot>0 && sigmat>0){
                funt = Math.exp((-0.5)*Math.pow((hit.get_Time() - recot - meant)/sigmat, 2) )/ (sigmat*Math.sqrt(2* Math.PI));
            }
            
        }

        double back = richpar.PIXEL_NOMINAL_DARKRATE*richpar.NSIGMA_TIME*richpar.PIXEL_NOMINAL_STIME;
        double prob = 1 + pixel_eff *func*dfunc*funt*dfunt + back;

        if(debugMode>=1)System.out.format(
                     "PID prob %4d    mean %7.2f etaC %7.2f sigma %7.2f   meant %7.2f (%7.2f + %7.2f) time %7.2f sigmat %7.2f  eff %7.2f -->  %10.4g  %10.4g  %8.4f e-3\n",hypo_pid,
                     mean*MRAD,reco.get_EtaC()*MRAD,sigma*MRAD,recot+meant,recot,meant,hit.get_Time(),sigmat,pixel_eff,func*dfunc,funt*dfunt,Math.log(prob)*1e3); 
        return prob;

    }


    // ----------------
    public double pid_LHCb(RICHParticle hadron, int hypo_pid, int recotype, int inorma) {
    // ----------------
    /*
    * calculate probability for a given pid hypothesis
    * based on the particle momentum and RICH resolution
    * at the moment, equal photon generation and particle
    * flux is assumed, actually close to Cherenkov threshold
    * the photon yiled decreases and pions outnumber koans.
    */

        int debugMode = 0;
        RICHSolution reco = new RICHSolution();
        if(recotype==0) reco = analytic;
        if(recotype==1) reco = traced;

        double func = 0.0;
        double dfunc = 1;

        double funt = 0.0;
        double dfunt = 1;

        double mean = 0.0;
        double sigma = 0.0;
        double recot = 0.0;
        double meant = 0.0;
        double sigmat = 0.0;

        int pixel_flag = 0;
        if(pixel_flag==0){

            // angle probability

            int irefle = reco.get_RefleType();
            if(irefle>=0 && irefle<=2){
                mean=hadron.changle(hypo_pid, irefle);
                sigma = hadron.schangle(irefle);
            }

            if(reco.get_EtaC()>0 && sigma>0){  
                // non-zero reconstruction and reference values exist (mean=0 below threshold)
                func = Math.exp((-0.5)*Math.pow((reco.get_EtaC() - mean)/sigma, 2) )/ (sigma*Math.sqrt(2* Math.PI));
            }
            if(inorma==1)func = 1. / (sigma*Math.sqrt(2* Math.PI));
            
            // timing probability
            recot = start_time + reco.get_time();
            meant = pixel_mtime;
            sigmat = pixel_stime;

            if(recot>0 && sigmat>0){
                // non-zero reconstruction and reference values exist 
                funt = Math.exp((-0.5)*Math.pow((hit.get_Time() - recot - meant)/sigmat, 2) )/ (sigmat*Math.sqrt(2* Math.PI));
            }
            if(inorma==1)funt = 1. / (sigmat*Math.sqrt(2* Math.PI));
            
        }
        double back = richpar.PIXEL_NOMINAL_DARKRATE*richpar.NSIGMA_TIME*richpar.PIXEL_NOMINAL_STIME;
        double prob = 1 + pixel_eff *func*dfunc*funt*dfunt + back;

        if(debugMode>=1)System.out.format(
                     "PID prob %4d    mean %7.2f etaC %7.2f sigma %7.2f   meant %7.2f (%7.2f + %7.2f) time %7.2f sigmat %7.2f  eff %7.2f -->  %10.4g  %10.4g  %8.4f\n",hypo_pid,
                     mean*MRAD,reco.get_EtaC()*MRAD,sigma*MRAD,recot+meant,recot,meant,hit.get_Time(),sigmat,pixel_eff,func*dfunc,funt*dfunt,prob); 
        return prob;

    }

    
    // ----------------
    public double calc_HypoYield(RICHParticle hadron, int hypo_pid, int recotype, double Npho, int iref) {
    // ----------------
    /*
    * calculate probability for a given pid hypothesis
    * based on the particle momentum and RICH resolution;
    * it uses the expected number of photons, the expected
    * background; iref = 1 forces a right hypothesis 
    */

        int debugMode = 0;
        RICHSolution reco = new RICHSolution();
        if(recotype==0) reco = analytic;
        if(recotype==1) reco = traced;

        // angle probability
        double mean  = 0.0;
        double sigma = 0.0;

        int irefle = reco.get_RefleType();
        if(irefle>=0 && irefle<=2){
            mean  = hadron.changle(hypo_pid, irefle);
            sigma = hadron.schangle(irefle);
        }

        double ftheta = 0.0;
        //                    fraction           rad             mrad (consistent with normalization)
        double dtheta = reco.get_dthe_pixel()*nominal_sChAngle()*MRAD;
        if(mean>0 && sigma>0){
            if(iref==0){
                ftheta = Math.exp((-0.5)*Math.pow((reco.get_EtaC()- mean)/sigma, 2) )/(sigma*MRAD*Math.sqrt(2* Math.PI));
            }else{
                ftheta = 1./(sigma*MRAD*Math.sqrt(2* Math.PI));
            }
        }
            
        // timing probability
        double recot  = start_time + reco.get_time();
        double meant  = chtime();
        double sigmat = schtime();

        double ftime  = 0.0;
        double dtime  = 1.0;
        if(recot>0 && sigmat>0){
            if(iref==0){
                ftime = Math.exp((-0.5)*Math.pow((hit.get_Time() - recot - meant)/sigmat, 2) )/ (sigmat*Math.sqrt(2* Math.PI));
            }else{
                ftime = 1./(sigmat*Math.sqrt(2* Math.PI));
            }
        }

        // background probability
        double backgr   = 0.0;
        double dphi     = reco.get_dphi_pixel()*nominal_sChAngle()/(2*Math.PI);
        //double dphi   = 1.;

        if (reco.get_dphi_pixel()>0 && dphi!=1){
            double dpixel = reco.get_dphi_pixel()*nominal_sChAngle()/(2*Math.PI);
            backgr = chbackgr() * dphi/dpixel * 2*richpar.NSIGMA_TIME*sigmat;
        }else{
            backgr = chbackgr() * 2*richpar.NSIGMA_TIME*sigmat;
        }
            
        double prob     = Npho * ftheta*dtheta * dphi;

        if(richpar.USE_PIXEL_TIME==1) prob *= ftime*dtime; 
        if(richpar.USE_PIXEL_EFF==1) prob *= cheff(); 

        if(richpar.USE_PIXEL_BACKGR==1) prob += backgr; 

        if(debugMode>=1 && iref==0){
            System.out.format("\n HYPO %4d %4d %3d: A %6.2f (%6.2f, %5.2f) ",
                hit.get_id(),hypo_pid,irefle,reco.get_EtaC()*MRAD,mean*MRAD,sigma*MRAD);
            //System.out.format(" T %6.2f (%6.2f, %5.2f) E %5.2f B %8.4g",
            //    hit.get_Time(),recot+meant,sigmat,pixel_eff,backgr);
            //System.out.format("--> %5.1f %10.4g %10.4g %10.4g --> %8.4g \n", 
            //    Npho,ftheta*dtheta,dphi,ftime*dtime,prob);
            System.out.format(" T %6.2f (%6.2f, %5.2f) ",
                hit.get_Time(),recot+meant,sigmat);
            System.out.format("--> %5.1f %10.4g %10.4g %10.4g %10.4g %10.4g %10.4g --> %8.4g \n\n", 
                Npho,ftheta,dtheta,dphi,ftime,dtime,backgr,prob);
        }
        return prob;

    }

    
    // ----------------
    public double calc_HypoC2(RICHParticle hadron, int hypo_pid, int recotype) {
    // ----------------
    /*
    * calculate chi2 for a given pid hypothesis
    * based on angle and time of RICH resolution;
    */

        int debugMode = 0;
        RICHSolution reco = new RICHSolution();
        if(recotype==0) reco = analytic;
        if(recotype==1) reco = traced;

        // angle probability
        double mean  = 0.0;
        double sigma = 0.0;

        int irefle = reco.get_RefleType();
        if(irefle>=0 && irefle<=2){
            mean  = hadron.changle(hypo_pid, irefle);
            sigma = hadron.schangle(irefle);
        }

        double ftheta = Math.pow((reco.get_EtaC()- mean)/sigma, 2);
            
        // timing probability
        double recot  = start_time + reco.get_time();
        double meant  = chtime();
        double sigmat = schtime();

        double ftime  = Math.pow((hit.get_Time() - recot - meant)/sigmat, 2);

        double prob = ftheta + ftime;
        //if(prob>12)prob=12.;

        if(debugMode>=1){
            System.out.format("HYPO %4d %4d %3d: A %6.2f (%6.2f, %5.2f) ",
                hit.get_id(),hypo_pid,irefle,reco.get_EtaC()*MRAD,mean*MRAD,sigma*MRAD);
            System.out.format(" T %6.2f (%6.2f, %5.2f) ",
                hit.get_Time(),recot+meant,sigmat);
            System.out.format("--> %10.4g %10.4g --> %10.4g \n", 
                ftheta,ftime,prob);
        }
        return prob;

    }


    /*
    // ----------------
    public double set_likelihood(double etaC) {
    // ----------------

        double pi = probability(211, etaC)
        double k = probability(211, etaC)
        double pr = probability(211, etaC)

        pi_like = Math.log(pi/(pi+k+pr));
        k_like  = Math.log(k/(pi+k+pr));
        pr_like = Math.log(pr/(pi+k+pr));

    }
    */


    // ----------------
    public void show() {
    // ----------------

        Vector3D ref_direction = ref_impact.vectorFrom(ref_emission).asUnit();
        Vector3D ori_emission  = lab_emission.vectorFrom(reference);
        Vector3D ori_impact    = hit.get_Position().vectorFrom(reference);

        System.out.format(" PART  info  pid  %d   mass %8.5f   mom %g \n", CLASpid, get_mass(CLASpid), momentum);

        for(int ir=0; ir<RICHConstants.N_PATH; ir++)
            System.out.format("       ChAngle dir (mrad) %8.2f %8.2f %8.2f %8.2f  limits %8.2f %8.2f \n", changle(3,ir)*MRAD, changle(2,ir)*MRAD, 
               changle(1,ir)*MRAD, changle(0,ir)*MRAD, min_changle(0)*MRAD, max_changle(0)*MRAD);
        System.out.println("  ");
        System.out.format(" TRACK origin     %s \n", direct_ray.origin().toStringBrief(1));
        System.out.format("       direction  %s   theta %8.2f   phi %8.2f \n", direct_ray.toVector().asUnit().toStringBrief(3), lab_theta*RAD, lab_phi*RAD);
        System.out.format("       emission   %s    time %8.2f  refind %8.4f \n", lab_emission.toStringBrief(1), start_time, refi_emission);
        System.out.format("       impact     %s    time track %8.2f   vs hit %8.2f \n", hit.get_Position().toStringBrief(1), traced.get_time(), hit.get_Time());
        System.out.println("  ");
        System.out.format(" AERO  entrance   %s \n", aero_entrance.toStringBrief(1));
        System.out.format(" AERO  middle     %s \n", aero_middle.toStringBrief(1));
        System.out.format(" AERO  exit       %s \n", aero_exit.toStringBrief(1));
        System.out.format(" AERO  normal     %s \n", aero_normal.toStringBrief(3));
        System.out.format(" AERO  RotAxis    %s  RotAngle %8.2f (deg)  %8.2f (mrad)\n", RotAxis.toStringBrief(3), RotAngle*RAD, RotAngle*MRAD);
        System.out.println("  ");
        System.out.format(" REF   direction  %s   theta  %8.2f    phi %8.2f \n", ref_direction.toStringBrief(3), ref_theta*RAD, ref_phi*RAD);
        System.out.format("       emission   %s   --> %s\n", ori_emission.toStringBrief(1), ref_emission.toStringBrief(1));
        System.out.format("       impact     %s   --> %s \n", ori_impact.toStringBrief(1), ref_impact.toStringBrief(1));
        System.out.format("       projection                              --> %s \n", ref_proj.toStringBrief(1));
        System.out.println("  ");

    }

    // ----------------
    public void shortshow() {
    // ----------------

        Vector3D ref_direction = ref_impact.vectorFrom(ref_emission).asUnit();
        Vector3D ori_emission  = lab_emission.vectorFrom(reference);
        Vector3D ori_impact    = hit.get_Position().vectorFrom(reference);

        System.out.format(" PART  id  %4d   type %4d   pid  %4d   mass %8.5f   mom %g \n", id, type, CLASpid, get_mass(CLASpid), momentum);
        System.out.println("  ");
        System.out.format(" TRACK direction  %s   theta %8.2f   phi %8.2f \n", direct_ray.toVector().toStringBrief(2), lab_theta*RAD, lab_phi*RAD);
        System.out.format("       emission   %s    time %8.2f \n", lab_emission.toStringBrief(2), start_time);
        System.out.format("       impact     %s    time track %8.2f   vs hit %8.2f \n", hit.get_Position().toStringBrief(2), traced.get_time(), hit.get_Time());
        System.out.println("  ");

    }

}
