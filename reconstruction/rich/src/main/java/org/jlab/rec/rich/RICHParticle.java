package org.jlab.rec.rich;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;

import java.util.ArrayList;

import org.jlab.io.base.DataEvent;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import eu.mihosoft.vrl.v3d.Polygon;

import org.jlab.geometry.prim.Line3d;

import org.freehep.math.minuit.FCNBase;
import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnScan;
import org.freehep.math.minuit.MnUserParameters;

import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.pdg.PDGDatabase;


public class RICHParticle {

     /**
     * A particle in the RICH consists of an array of ray lines plus particle information 
     */

    private int id = -999;
    private int parent_index = -999;
    private int hit_index = -999;
    private int type = 0;
    private int status = -1;
    private int CLASpid = -999;
    private int RICHpid = -999;
    private double momentum = 0; 
    private double[] ChAngle = new double[4];

    public Vector3d meas_hit = null;
    private double  meas_time = 0.0;

    public Point3D lab_origin = null;
    public Vector3d lab_emission = null;
    public int ilay_emission = 0;
    public int ico_emission = 0;
    public float refi_emission = 0;
    public double lab_phi = 0.0;
    public double lab_theta = 0.0;

    // rotated frame into RICH local coordinates (used for the analytic solution)
    public double RotAngle = 0.0;
    public Vector3d RotAxis = null;
    public Vector3d reference = null;
    public Vector3d ref_emission = null;
    public Vector3d ref_impact = null;    // P_P in Evaristo's note
    public Vector3d ref_proj = null;      // P_PCP in Evaristo's note
    public double ref_phi = 0.0;
    public double ref_theta = 0.0;
    public double EtaC_ref = 0.0;

    private double start_time = 0.0;

    public RICHSolution analytic = new RICHSolution(0);
    public RICHSolution traced = new RICHSolution(1);

    public RICHParticle trial_pho = null;

    public Vector3d aero_entrance = new Vector3d(0.,0.,9999.);
    public Vector3d aero_exit     = new Vector3d(0.,0.,0.);
    public Vector3d aero_middle = null;
    public Vector3d aero_normal = null;

    public Line3d direct_ray;

    private static double MRAD = 1000.;
    private static double RAD = 180./Math.PI;

    private RICHConstants recopar = null;


    // -------------
    public RICHParticle(int id, int parent_index, int hit_index, double mom, int CLASpid, RICHTool tool){
    // -------------
    /**
    * Define a generic particle with given momentum
    */

        set_id(id);
        set_parent_index(parent_index);
        set_hit_index(hit_index);
        set_CLASpid(CLASpid);
        set_momentum(mom);

        recopar = tool.get_Constants();

        //if(CLASpid!=22)set_changle();

    }


    // -------------
    public void set_CLASpid(int pid)
    // -------------
    /*
    * Set the particle pid that generates the track 
    * @param int pid, the identifier of the particle  (only gamma, e, pi, proton and k are allowed)
    */
    {
        if (Math.abs(pid)==22 || Math.abs(pid)==11 || Math.abs(pid)==211 | Math.abs(pid)==321 || Math.abs(pid)==2212) {
            this.CLASpid = pid;
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


    // -------------
    private void set_changle() {
    // -------------
    // define only one Cherenkov angle depending on the pid hypothesis
    
        int debugMode = 0;
        for(int k=0 ; k<4; k++) ChAngle[k] = 0.0;
        ChAngle[0] = expectedChAngle(11);      // expected angle for electron
        ChAngle[1] = expectedChAngle(211);     // pion
        ChAngle[2] = expectedChAngle(321);     // kaon
        ChAngle[3] = expectedChAngle(2212);    // proton

        if(debugMode>=1)  {
            System.out.format(" Create RICH particle mom %8.2f     Ch %8.2f %8.2f %8.2f %8.2f \n",momentum,ChAngle[0]*MRAD,ChAngle[1]*MRAD,ChAngle[2]*MRAD,ChAngle[3]*MRAD);
            System.out.format("      --> Ch angle limits %8.2f %8.2f \n",minChAngle()*MRAD,maxChAngle()*MRAD);
        }
    }


    // -------------
    public int get_id() {return id;}
    // -------------

    // -------------
    public int get_ParentIndex() {return parent_index;}
    // -------------

    // -------------
    public int get_hit_index() {return hit_index;}
    // -------------

    // -------------
    public int get_type() {return type;}
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
    public double get_start_time() {return start_time;}
    // -------------

    // -------------
    public double get_meas_time() {return meas_time;}
    // -------------

    // -------------
    public double get_changle(int i) { return ChAngle[i]; }
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
    public double maxChAngle() {
    // ----------------

        for(int k=0 ; k<4; k++) if(ChAngle[k]>0)return ChAngle[k] + 3*recopar.RICH_DIRECT_RMS;
        return 0.0;
    }


    // ----------------
    public double minChAngle() {
    // ----------------
    // calculate the minimum Cherenlov angle compatible with the momentum 
  
        for(int k=3 ; k>=0; k--) if(ChAngle[k]>0)return Math.max(recopar.RICH_MIN_CHANGLE, ChAngle[k] - 3*recopar.RICH_DIRECT_RMS);
        return 0.0;
    }


    // ----------------
    public double expectedChAngle(int pid){
    // ----------------

        int debugMode = 0;
        double arg = 0.0;
        double beta = get_beta(pid);
        if(beta>0) arg = 1.0/beta/refi_emission;
        if(debugMode>=1)  System.out.format(" Expected Ch Angle %8.4f  beta %8.4f  n %7.3f  arg %8.4f\n",get_mass(pid),beta,refi_emission, Math.acos(arg)*MRAD);
        if(arg>0.0 && arg<1.0) return Math.acos(arg);
        return 0.0;
    }

    // ----------------
    public void set_id(int id) { this.id=id;}
    // ----------------

    // ----------------
    public void set_hit_index(int hit_id) { this.hit_index=hit_id;}
    // ----------------

    // ----------------
    public void set_parent_index(int parent_id) { this.parent_index=parent_id;}
    // ----------------

    // ----------------
    public void set_type(int type) { this.type=type; }
    // ----------------

    // ----------------
    public void set_Status(int sta) { this.status = sta; }
    // ----------------

    // ----------------
    public void set_momentum(double momentum) { this.momentum=momentum; }
    // ----------------

    // ----------------
    public double get_momentum() { return this.momentum; }
    // ----------------

    // ----------------
    public void set_origin(Point3D ori){ this.lab_origin = ori; }
    // ----------------

    // ----------------
    public Point3D get_origin(){ return this.lab_origin; }
    // ----------------

    // ----------------
    public void set_meas_hit(Vector3d impa){ this.meas_hit = impa; }
    // ----------------

    // ----------------
    public Vector3d get_meas_hit(){ return this.meas_hit; }
    // ----------------

    // ----------------
    public void set_start_time(double time){ this.start_time = time; }
    // ----------------

    // ----------------
    public void set_meas_time(double time){ this.meas_time = time; }
    // ----------------

    // -------------
    public void set_RICHpid(int hpid){
    // -------------
        if(hpid==3)this.RICHpid=211;
        if(hpid==4)this.RICHpid=321;
        if(hpid==5)this.RICHpid=2212;
        if(this.CLASpid<0)RICHpid*=-1;
    }

    // ----------------
    public void set_points(RICHParticle hadron, Vector3d impa){
    // ----------------

        //this.lab_origin    = hadron.lab_emission;
        this.lab_emission  = hadron.lab_emission;
        this.meas_hit = impa;
        this.direct_ray = new Line3d(this.lab_emission, impa);
        this.lab_phi = Math.atan2(direct_ray.diff().y, direct_ray.diff().x);
        this.lab_theta = direct_ray.diff().angle(Vector3d.Z_ONE);

        this.ilay_emission = hadron.ilay_emission;
        this.ico_emission = hadron.ico_emission;

        this.reference = hadron.reference;
        Quaternion q = new Quaternion(hadron.RotAngle, hadron.RotAxis);

        this.ref_emission = q.rotate(this.lab_emission.minus(this.reference));
        this.ref_impact = q.rotate(this.meas_hit.minus(this.reference));

        this.ref_phi = Math.atan2(ref_impact.y, ref_impact.x);
        this.ref_theta = ref_impact.angle(Vector3d.Z_ONE);
        this.ref_proj  = hadron.ref_proj;

        this.EtaC_ref  = calc_EtaC(hadron, this.ref_theta);

    }

    // ----------------
    public boolean set_points(Point3D ori, Point3D end, Vector3D impa, int status, RICHTool tool) {
    // ----------------
    
        int debugMode = 0;

        if(debugMode>=1){
            System.out.println(" \n RICHParticle::set_points with OFFSETs\n");
            System.out.println(" TRACK origin "+ ori);
            System.out.println(" TRACK end    "+ end);
            System.out.println(" RESP  impact "+impa);
            System.out.println(" RESP  status "+status);
        }
        this.lab_origin = ori;

        if(tool.get_Constants().FORCE_DC_MATCH==1){
            if(debugMode>=1)System.out.println(" FORCE DC-RICH match \n");
            this.direct_ray = new Line3d(tool.toVector3d(ori), tool.toVector3d(impa));
            this.meas_hit = tool.toVector3d(impa);
        }else{
            if(debugMode>=1)System.out.println(" Take hadron track as from DC\n");
            this.direct_ray = new Line3d(tool.toVector3d(ori), tool.toVector3d(end));
            Line3D temp = new Line3D(ori, end);
            if(status==1){
                // status = 1 means a matching MAPMT cluster has been found, track pointing to MaPMT
                this.meas_hit = tool.find_intersection_MAPMT(temp);
            }else{
                // status != 1 means no matching cluster, track pointing to RICH 
                this.meas_hit = tool.toVector3d(impa);
            }
        }

        if(this.meas_hit==null){
            if(debugMode>=1)System.out.format("No extrapolatd hit found for track ! \n");
            return false;
        }

        this.status = status;
        this.lab_phi = Math.atan2(direct_ray.diff().y, direct_ray.diff().x);
        this.lab_theta = direct_ray.diff().angle(Vector3d.Z_ONE);
        if(debugMode>=1)System.out.format("New hadron STATUS %3d\n",this.status);
        return true;

    }


    // ----------------
    public boolean find_aerogel_points(RICHTool tool){
    // ----------------
    /*
    * Search for the intersection points on the aerogel along forward trajectory
    * Take the first (in z) with track pointing inside as Entrance
    * Take the last (in z) with track pointing outside as Exit
    */

        int debugMode = 0;
        boolean found_exit = false;
        boolean found_entrance = false;

        if(debugMode>=3)  System.out.println(" \n RICHParticle::set_aerogel_points \n");
        RICHIntersection compo     = null;

        Line3D temp = new Line3D(tool.toPoint3D(direct_ray.origin()), tool.toPoint3D(direct_ray.end()));

        if(debugMode>=3)  System.out.println(" Look for intersection with layer 201 \n");
        RICHIntersection entrance  = tool.get_Layer("aerogel_2cm_B1").find_Entrance(temp, -1);
        RICHIntersection exit      = tool.get_Layer("aerogel_2cm_B1").find_Exit(temp, -1);

        if(entrance==null || exit==null){
            if(debugMode>=3)  System.out.println(" Look for intersection with layer 202 \n");
            entrance  = tool.get_Layer("aerogel_2cm_B2").find_Entrance(temp, -1);
            exit      = tool.get_Layer("aerogel_2cm_B2").find_Exit(temp, -1);
        }

        if(entrance==null || exit==null){
            if(debugMode>=3)  System.out.println(" Look for intersection with layer 203/204 \n");
            entrance  = tool.get_Layer("aerogel_3cm_L2").find_Entrance(temp, -1);
            exit      = tool.get_Layer("aerogel_3cm_L1").find_Exit(temp, -1);
            if(debugMode>=1){
                if(entrance!=null)  System.out.format(" 3cm entrance %4d %6d  %s \n",entrance.get_layer(),entrance.get_component(), entrance.get_pos().toStringBrief(2));
                if(exit!=null)      System.out.format(" 3cm exit     %4d %6d  %s \n",exit.get_layer(),exit.get_component(), exit.get_pos().toStringBrief(2));
            }
        }

        if(entrance==null || exit==null){
            if(debugMode>=1)System.out.format(" No intersection with aerogel plane found \n");
            return false;
        }

        aero_entrance  = tool.toVector3d(entrance.get_pos());
        aero_exit      = tool.toVector3d(exit.get_pos());
        aero_normal    = tool.toVector3d(exit.get_normal());
        /*
        *   Take point at 3/4 of path inside aerrogel  
        */
        Vector3d amiddle = aero_exit.midpoint(aero_entrance);
        aero_middle    = aero_exit.midpoint(amiddle);
        lab_emission   = aero_middle;
        // take the downstream aerogel tile as the one with largest number of phtoons and average emisison point
        ilay_emission  = exit.get_layer();
        ico_emission   = exit.get_component();
        if(debugMode>=1)System.out.format(" AERO lay %3d ico %3d \n",ilay_emission,ico_emission);
        refi_emission  = tool.get_Component(ilay_emission,ico_emission).get_index();

        set_changle();

        RotAxis  = aero_normal.cross(Vector3d.Z_ONE).normalized();
        RotAngle = aero_normal.angle(Vector3d.Z_ONE);

        if(debugMode>=1){
            System.out.format(" Qua %d %f \n",CLASpid,meas_hit.x);
        }
        if(start_time==0.0)start_time = traced.get_time() - meas_hit.distance(aero_middle)/get_beta(CLASpid)/(PhysicsConstants.speedOfLight());

        if(debugMode>=3){
            System.out.println(" AERO entrance "+aero_entrance);
            System.out.println(" AERO middle   "+aero_middle);
            System.out.println(" AERO exit     "+aero_exit);
            System.out.println(" AERO normal   "+aero_normal);
            System.out.println(" AERO layer    "+ilay_emission);
            System.out.println(" AERO compo    "+ico_emission);
            System.out.println(" AERO refi     "+refi_emission);
        }

        return true;
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
        Quaternion q = new Quaternion(RotAngle, RotAxis.normalized());

        // rotate into the new reference system
        ref_emission = q.rotate( lab_emission.minus(reference)) ;
        ref_impact   = q.rotate( meas_hit.minus(reference)) ;

        ref_proj = new Vector3d(ref_emission.x, ref_emission.y, ref_impact.z-ref_emission.z);

        this.ref_phi = Math.atan2(ref_impact.y, ref_impact.x);
        this.ref_theta = ref_impact.angle(Vector3d.Z_ONE);
       
        if(debugMode>=2){
            System.out.format(" --> Track projection (P_PCP) %8.2f ",ref_proj);
            System.out.format(" --> Track impact (P_P)       %8.2f ",ref_impact);
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
    public void get_EtaC_dir(RICHParticle hadron) {
    // ----------------

    }

    // ----------------
    public void find_EtaC_raytrace_migrad(RICHParticle hadron, RICHTool tool) {
    // ----------------

        int debugMode = 0;
        double n_a = recopar.RICH_AIR_INDEX;

        double Phi_ini = trial_pho.lab_phi;
        double Theta_ini = trial_pho.lab_theta;
        if(Phi_ini!=0 && Theta_ini!=0) {

            double Theta_P = hadron.lab_theta;
            double Phi_P = hadron.lab_phi;
            double start_EtaC=Math.sin(Theta_P)* Math.sin(Theta_ini)*Math.cos(Phi_ini-Phi_P)+Math.cos(Theta_P)*Math.cos(Theta_ini);
            if(debugMode>=0){
                System.out.format(" Tracing starting values \n");
                System.out.format(" Hadron phi %8.2f theta %8.2f \n ", Phi_P*RAD, Theta_P*RAD);
                System.out.format(" Photon initial phi %8.2f theta %8.2f EtaC %10.4f \n ", Phi_ini*RAD, Theta_ini*RAD, start_EtaC*MRAD);
            }

            // Minimizing function
            FCNBase myFunction = new FCNBase() {
                public double valueOf(double[] par) {
                    
                    double theta = par[0];
                    double phi = par[1];
                    Vector3d vpho = new Vector3d( Math.sin(theta)*Math.cos(phi), Math.sin(theta)*Math.sin(phi), Math.cos(theta));
                    ArrayList<RICHRay> rays = tool.RayTrace(lab_emission, ilay_emission, ico_emission, vpho); 
                    double Function = 999;
                    Vector3d pmt_hit = new Vector3d(0.0, 0.0, 0.0);
                    if(rays!=null){
                        pmt_hit = tool.toVector3d(rays.get(rays.size()-1).end());
                        Function = pmt_hit.distance(meas_hit);
                    }

                    if(debugMode>=0)System.out.format(" ==>  %8.2f %8.2f | %8.2f %8.2f %8.2f vs %8.2f %8.2f %8.2f -->  %10.4f \n", 
                                                 theta*RAD, phi*RAD, pmt_hit.x, pmt_hit.y, pmt_hit.z, meas_hit.x, meas_hit.y, meas_hit.z, Function);
                    return Function;
                }
            };

            if(debugMode>=0)System.out.format(" Start minimization %8.2f %8.2f | %8.2f %8.2f \n",Theta_P*RAD, Phi_P*RAD, Theta_ini*RAD, Phi_ini*RAD);
            MnUserParameters myParameters = new MnUserParameters();
            myParameters.add("Theta",Theta_ini, 0.01);
            myParameters.add("Phi",Phi_ini, 0.01);
            MnMigrad migrad = new MnMigrad(myFunction, myParameters);
            FunctionMinimum min = migrad.minimize();
            if(debugMode>=0){
                System.out.format(" -->  Minimum value %g found using %d calls: result is %8.2f  %8.2f\n", 
                                     min.fval(), min.nfcn(), min.userParameters().value(0)*RAD, min.userParameters().value(1)*RAD);
            }
            double Theta_Min = min.userParameters().value(0);
            double Phi_Min = min.userParameters().value(1);

            double n_tile = 1/(hadron.get_beta(CLASpid)*(Math.sin(Theta_P)* Math.sin(Theta_Min)*Math.cos(Phi_Min-Phi_P)+Math.cos(Theta_P)*Math.cos(Theta_Min)));
            double arg = Math.pow(n_a, 2)-Math.pow(n_tile, 2)*Math.pow(Math.sin(Theta_Min), 2);
            double Denominator = 1e-4;
            if(arg>0) Denominator = Math.sqrt(arg);

            double Cos_EtaC=Math.sin(Theta_P)* Math.sin(Theta_Min)*Math.cos(Phi_Min-Phi_P)+Math.cos(Theta_P)*Math.cos(Theta_Min);

            traced.set_theta((float) Theta_Min);
            traced.set_phi((float) Phi_Min);
            traced.set_aeron((float) n_tile);
            traced.set_EtaC((float) Math.acos(Cos_EtaC));

            Vector3d vmin = new Vector3d( Math.sin(Theta_Min)*Math.cos(Phi_Min), Math.sin(Theta_Min)*Math.sin(Phi_Min), Math.cos(Theta_Min));
            ArrayList<RICHRay> raysmin = tool.RayTrace(lab_emission, ilay_emission, ico_emission, vmin);
            if(raysmin!=null)traced.set_raytracks(raysmin);

            if(debugMode>=1){
                traced.show_raytrack();
                System.out.format("#### ETAC ref %8.4f  min %8.4f  path %8.2f  time %8.2f \n", EtaC_ref*MRAD, traced.get_EtaC()*MRAD, traced.get_raypath(), traced.get_raytime());
            }

        }else{
            if(debugMode>=0) System.out.format("Missing starting values from trial photon\n");
        }

    }


    // ----------------
    public void find_EtaC_raytrace_steps(RICHParticle hadron, RICHTool tool) {
    // ----------------

        int debugMode = 0;
        double n_a = recopar.RICH_AIR_INDEX;

        if(trial_pho==null){
            if(debugMode>=1) System.out.format(" Missing starting values from trial photon\n");
            return;
        }

        double Theta_P = hadron.lab_theta;
        double Phi_P = hadron.lab_phi;
        if(debugMode>=1) {
            System.out.format(" Hadron phi %8.2f theta %8.2f \n", Phi_P*RAD, Theta_P*RAD);
            System.out.format(" Measured hit %7.2f %7.2f %7.2f \n",meas_hit.x,meas_hit.y,meas_hit.z);
        }

        double phi_min    = trial_pho.lab_phi;
        double the_min    = trial_pho.lab_theta;
        Vector3d pmt_min  = trial_pho.meas_hit;
        int nrefle_min = trial_pho.traced.get_nrefle();
        ArrayList<RICHRay> rays_min = new ArrayList(); 
        rays_min = trial_pho.traced.get_raytracks();

        Vector3d vec_dist = meas_hit.minus(pmt_min);
        double dist = Math.sqrt(vec_dist.x*vec_dist.x+vec_dist.y*vec_dist.y);
        double Cos_EtaC = Math.sin(Theta_P)* Math.sin(the_min)*Math.cos(phi_min-Phi_P)+Math.cos(Theta_P)*Math.cos(the_min);
        double EtaCmin = 0.0;
        if(Math.abs(Cos_EtaC)<1.)EtaCmin = Math.acos(Cos_EtaC);
        int ntrials = 0;
        while (dist > recopar.RICH_DIRECT_RMS*100/2. && ntrials<10){

            if(debugMode>=1){ 
                System.out.format(" Attempt %d  with the %7.1f (%7.2f)  phi %7.2f  EtaC  %7.2f\n",ntrials, the_min*MRAD, the_min*RAD, phi_min*RAD, EtaCmin*MRAD); 
                System.out.format(" --> (nrfl %2d) pos %7.2f %7.2f %7.2f  dist %7.4f (x %7.4f  y %7.4f) \n", nrefle_min, pmt_min.x, pmt_min.y, pmt_min.z, dist, vec_dist.x, vec_dist.y);
            }
            double dthe = 0.0;
            double dphi = 0.0;
 
            int nthe = 0;
            for (nthe=1; nthe<=4; nthe++){
                double theta_dthe = the_min + recopar.RICH_DIRECT_RMS/nthe;
                Vector3d vpho_dthe = new Vector3d( Math.sin(theta_dthe)*Math.cos(phi_min), Math.sin(theta_dthe)*Math.sin(phi_min), Math.cos(theta_dthe));
                ArrayList<RICHRay> rays_dthe = tool.RayTrace(lab_emission, ilay_emission, ico_emission, vpho_dthe);
                if(rays_dthe!=null){
                    int nrefle_dthe = get_rayrefle(rays_dthe);
                    if(debugMode>=1) System.out.format("     test %2d  the %7.1f  nrfl %2d vs %2d ",nthe, theta_dthe*MRAD, nrefle_dthe, nrefle_min); 
                    if(nrefle_dthe==nrefle_min){
                        Vector3d pmt_dthe = tool.toVector3d(rays_dthe.get(rays_dthe.size()-1).end());
                        Vector3d vers_dthe = pmt_dthe.minus(pmt_min);
                        dthe = (vec_dist.x*vers_dthe.x + vec_dist.y*vers_dthe.y) / (vers_dthe.x*vers_dthe.x + vers_dthe.y*vers_dthe.y) * recopar.RICH_DIRECT_RMS;
                        if(debugMode>=1) System.out.format("   --> dthe pos %7.2f %7.2f %7.2f  delta %7.1f (%8.2f %8.2f) \n", 
                                     pmt_dthe.x, pmt_dthe.y, pmt_dthe.z, dthe*MRAD, vers_dthe.x, vers_dthe.y);
                        break;
                    }else{
                        if(debugMode>=1) System.out.format("   failed \n");
                    }
                }
            }

            for (int nphi=1; nphi<=4; nphi++){
                double phi_dphi = phi_min + recopar.RICH_DIRECT_RMS/nphi;
                Vector3d vpho_dphi = new Vector3d( Math.sin(the_min)*Math.cos(phi_dphi), Math.sin(the_min)*Math.sin(phi_dphi), Math.cos(the_min));
                ArrayList<RICHRay> rays_dphi = tool.RayTrace(lab_emission, ilay_emission, ico_emission, vpho_dphi);
                if(rays_dphi!=null){
                    int nrefle_dphi = get_rayrefle(rays_dphi);
                    if(debugMode>=1) System.out.format("     test %2d  phi %7.2f  nrfl %2d vs %2d ",nphi, phi_dphi*RAD, nrefle_dphi, nrefle_min); 
                    if(nrefle_dphi==nrefle_min){
                        Vector3d pmt_dphi = tool.toVector3d(rays_dphi.get(rays_dphi.size()-1).end());
                        Vector3d vers_dphi = (pmt_dphi.minus(pmt_min));
                        dphi = (vec_dist.x*vers_dphi.x + vec_dist.y*vers_dphi.y) / (vers_dphi.x*vers_dphi.x + vers_dphi.y*vers_dphi.y) * recopar.RICH_DIRECT_RMS;
                        if(debugMode>=1) System.out.format("   --> dphi pos %7.2f %7.2f %7.2f  delta %7.2f (%8.2f %8.2f) \n", 
                                     pmt_dphi.x, pmt_dphi.y, pmt_dphi.z, dphi*RAD, vers_dphi.x, vers_dphi.y);
                        break;
                    }else{
                        if(debugMode>=1) System.out.format("   failed \n");
                    }
                }
            }
  
	    if(dthe!=0 && dphi!=0){
                int found = 0;
                for (int nn=1; nn<=4; nn++){
                    double the_new  = the_min + dthe/nn;
                    double phi_new  = phi_min + dphi/nn;
                    if(debugMode>=1) System.out.format("        do step nn %3d  the %7.1f  phi %7.2f  (from %7.1f  %7.2f) \n",nn,the_new*MRAD,phi_new*RAD,the_min*MRAD,phi_min*RAD);

                    Vector3d vpho_min = new Vector3d( Math.sin(the_new)*Math.cos(phi_new), Math.sin(the_new)*Math.sin(phi_new), Math.cos(the_new));
                    rays_min = tool.RayTrace(lab_emission, ilay_emission, ico_emission, vpho_min);
                    if(rays_min!=null){
                        int nrefle_new = get_rayrefle(rays_min);
                        if(debugMode>=1) System.out.format("        test %2d  the %7.1f  phi %7.2f  nrfl %2d vs %2d ",nn, the_new*MRAD, phi_new*RAD, nrefle_new, nrefle_min); 
                        if(nrefle_new==nrefle_min){
                            the_min=the_new;
                            phi_min=phi_new;
                            pmt_min = tool.toVector3d(rays_min.get(rays_min.size()-1).end());
                            vec_dist = meas_hit.minus(pmt_min);
                            dist = Math.sqrt(vec_dist.x*vec_dist.x+vec_dist.y*vec_dist.y);
                            Cos_EtaC = Math.sin(Theta_P)* Math.sin(the_min)*Math.cos(phi_min-Phi_P)+Math.cos(Theta_P)*Math.cos(the_min);
                            EtaCmin = 0.0;
                            if(Math.abs(Cos_EtaC)<1.)EtaCmin = Math.acos(Cos_EtaC);
                            found = 1;
                            if(debugMode>=1) System.out.format("   found new point \n");
                            break;
                        }else{
                            if(debugMode>=1) System.out.format("   failed \n");
                        }
                    }else{
                        if(debugMode>=1) System.out.format("   no PMT hit \n");
                    }
                }
                if(found==0){
                   if(debugMode>=1) System.out.format(" No Raytrace solution; give up \n");
                   return;
                }
            }else{
                if(debugMode>=1) System.out.format(" No Raytrace solution; give up \n");
                return;
            }

            ntrials++;
        }

        if(dist < recopar.RICH_DIRECT_RMS*100.){

            if(debugMode>=1){
                System.out.format(" -->  Matched value found using %d calls: result is %8.2f %8.2f  matched hit %7.2f %7.2f %7.2f  dist %7.3f \n", 
                                  ntrials, the_min*MRAD, phi_min*RAD, pmt_min.x, pmt_min.y, pmt_min.z, dist);
            }

            double n_tile = 1/(hadron.get_beta(CLASpid)*(Math.sin(Theta_P)* Math.sin(the_min)*Math.cos(phi_min-Phi_P)+Math.cos(Theta_P)*Math.cos(the_min)));

            traced.set_theta((float) the_min);
            traced.set_phi((float) phi_min);
            traced.set_aeron((float) n_tile);
            traced.set_EtaC((float) EtaCmin);
            traced.set_raytracks(rays_min);

            if(debugMode>=1){
                traced.show_raytrack();
                System.out.format("#### ETAC TRA ref %8.4f  min %8.4f  path %8.2f  time %8.2f \n", EtaC_ref*MRAD, traced.get_EtaC()*MRAD, traced.get_path(), traced.get_time());
            }
        }

    }

    // ----------------
    public void find_EtaC_analytic_migrad (RICHParticle hadron) {
    // ----------------

        int debugMode = 0;
        double n_a = recopar.RICH_AIR_INDEX;

        // The following definition should be read by the geometry
        // ATT: mismatch con la definizione di emission a 3/4 dell'aerogel
        // ATT: L deve essere calcolato con il coseno
        double T_r = recopar.RICH_AERO_THICKNESS;
        double L = T_r/2; // middle point is Thickness
        double T_g = hadron.ref_impact.z-hadron.ref_emission.z-L;

        Vector3d vec_b = ref_impact.minus(hadron.ref_proj);
        double radius = vec_b.magnitude();
        if(debugMode>=1)System.out.println("  T_g "+T_g+" T_r "+T_r+" L "+L+" R "+radius);
        if(radius >=-1 ) {

            // Starting values
            double Phi = ref_phi;
            double Theta_ini = ref_theta;
            double Theta_P = hadron.ref_theta;
            double Phi_P = hadron.ref_phi;
            if(debugMode>=1){
                System.out.format(" Minimization starting values \n");
                System.out.format(" Hadron phi %8.2f initial Theta %8.2f \n ", Phi_P*RAD, Theta_P*RAD);
                System.out.format(" Photon radius %8.2f phi %8.2f Theta %8.2f EtaC %10.4f \n ", radius, Phi*RAD, Theta_ini*RAD, EtaC_ref*MRAD);
            }

            // Minimizing function 
            FCNBase myFunction = new FCNBase() {
                public double valueOf(double[] par) {
                    double Theta = par[0];
                    double nn_tile = 1/(hadron.get_beta(CLASpid)*(Math.sin(Theta_P)* Math.sin(Theta)*Math.cos(Phi-Phi_P)+Math.cos(Theta_P)*Math.cos(Theta)));
                    double n_tile = nn_tile;
                    //double n_tile = 1.05;
                    double arg = Math.pow(n_a, 2)-Math.pow(n_tile, 2)*Math.pow(Math.sin(Theta), 2);
                    double Denominator = 1e-4;
                    if(arg>0) Denominator = Math.sqrt(arg);

                    double Fun = (T_r -L) * Math.tan(Theta)+T_g* (n_tile * Math.sin(Theta))/Denominator;
                    double Function = Math.pow(radius - Fun, 2);
                    if(debugMode>=1)System.out.format(" ==> %8.2f %8.2f | %8.2f %8.2f | %9.3f %10.4f \n",Theta_P*RAD, Phi_P*RAD, Theta*RAD, Phi*RAD, nn_tile, Function);
                    return Function;
                }
            };

            if(debugMode>=1)System.out.format(" Start minimization %8.2f %8.2f | %8.2f %8.2f \n",Theta_P*RAD, Phi_P*RAD, Theta_ini*RAD, Phi*RAD);
            MnUserParameters myParameters = new MnUserParameters();
            myParameters.add("Theta",Theta_ini, 0.01);
            MnMigrad migrad = new MnMigrad(myFunction, myParameters);
            FunctionMinimum min = migrad.minimize();
            if(debugMode>=1){
                System.out.format(" -->  Minimum value %g found using %d calls: result is %8.2f \n", min.fval(), min.nfcn(), min.userParameters().value(0)*RAD);
            }
            double Theta_Min = min.userParameters().value(0);
            analytic.set_theta((float) Theta_Min);
            analytic.set_phi((float) Phi);

            double Cos_EtaC = Math.sin(Theta_P)* Math.sin(Theta_Min)*Math.cos(Phi-Phi_P)+Math.cos(Theta_P)*Math.cos(Theta_Min);

            double n_tile = 1/(hadron.get_beta(CLASpid)*Cos_EtaC);
            double arg = Math.pow(n_a, 2)-Math.pow(n_tile, 2)*Math.pow(Math.sin(Theta_Min), 2);
            double Denominator = 1e-4;
            if(arg>0) Denominator = Math.sqrt(arg);
      
            double migrad_path = ((T_r -L)/Math.cos(Theta_Min) + (T_g*n_a/Denominator) );
            double migrad_time = ( ((T_r -L)/Math.cos(Theta_Min)/n_tile) + (T_g*n_a/Denominator) )/PhysicsConstants.speedOfLight();

            analytic.set_time((float) migrad_time);
            analytic.set_path((float) migrad_path);
            analytic.set_aeron((float) n_tile);
            analytic.set_EtaC((float) Math.acos(Cos_EtaC) );

            if(debugMode>=1){
                System.out.format("#### ETAC ALY ref %8.4f  min %8.4f  path %8.2f  time %8.2f \n", EtaC_ref*MRAD, analytic.get_EtaC()*MRAD, analytic.get_path(), analytic.get_time());
            }

        }
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
        double sigmat = recopar.RICH_TIME_RMS;

        double funt = 0.0;
        double dfunt = 1;

        if(meant>0){
            funt = Math.exp((-0.5)*Math.pow((testtime - meant)/sigmat, 2) )/ (sigmat*Math.sqrt(2* Math.PI));
        }
        
        double prob = 1 + funt*dfunt + recopar.RICH_BKG_PROBABILITY;

        if(debugMode>=1)if(prob-1>recopar.RICH_BKG_PROBABILITY)System.out.format(
                     "TIM prob   meant %8.3f  time %8.3f -->  %g  %g \n",
                     meant,testtime,funt*dfunt,Math.log(prob)); 
        return prob;

    }

    
    // ----------------
    public double pid_probability(RICHParticle hadron, int pid, int recotype) {
    // ----------------
    /*
    * calculate probability for a given pid hypothesis
    * based on the particle momentum and RICH resolution
    * at the moment, equal photon generation and particle
    * flux is assumed, actually close to Cherenkov threshold
    * the photon yiled decreases and pion outnumber koans.
    */

        int debugMode = 0;
        RICHSolution reco = new RICHSolution();
        if(recotype==0) reco = analytic;
        if(recotype==1) reco = traced;

        // angle probability
        double mean = 0.0;
        if(Math.abs(pid)==11)mean=hadron.get_changle(0);
        if(Math.abs(pid)==211)mean=hadron.get_changle(1);
        if(Math.abs(pid)==321)mean=hadron.get_changle(2);
        if(Math.abs(pid)==2212)mean=hadron.get_changle(3);

        double func = 0.0;
        double dfunc = 1e-3;
        double sigma = recopar.RICH_DIRECT_RMS;

        if(mean>0){
            func = Math.exp((-0.5)*Math.pow((reco.get_EtaC() - mean)/sigma, 2) )/ (sigma*Math.sqrt(2* Math.PI));
        }
        
        // timing probability
        double meant = start_time + reco.get_time();
        double sigmat = recopar.RICH_TIME_RMS;

        double funt = 0.0;
        double dfunt = 1;

        if(meant>0){
            funt = Math.exp((-0.5)*Math.pow((meas_time - meant)/sigmat, 2) )/ (sigmat*Math.sqrt(2* Math.PI));
        }
        
        double prob = 1 + func*dfunc*funt*dfunt + recopar.RICH_BKG_PROBABILITY;

        if(debugMode>=1)if(prob-1>recopar.RICH_BKG_PROBABILITY)System.out.format(
                     "PID prob %4d    mean %8.3f   etaC %8.3f   meant %8.3f  time %8.3f -->  %g  %g  %g \n",pid,
                     mean*MRAD,reco.get_EtaC()*MRAD,meant,meas_time,func*dfunc,funt*dfunt,Math.log(prob)); 
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

        Vector3d lab_direction = direct_ray.diff().normalized();
        Vector3d ref_direction = ref_impact.minus(ref_emission).normalized();
        Vector3d ori_emission  = lab_emission.minus(reference);
        Vector3d ori_impact    = meas_hit.minus(reference);

        System.out.format(" PART  info  pid  %d   mass %8.5f   mom %g \n", CLASpid, get_mass(CLASpid), momentum);
        System.out.format("       ChAngle (mrad)    %8.2f %8.2f %8.2f %8.2f  limits %8.2f %8.2f \n", ChAngle[3]*MRAD, ChAngle[2]*MRAD, ChAngle[1]*MRAD, ChAngle[0]*MRAD,
                                             minChAngle()*MRAD, maxChAngle()*MRAD);
        System.out.println("  ");
        System.out.format(" TRACK origin     %8.1f %8.1f %8.1f \n", lab_origin.x(), lab_origin.y(), lab_origin.z());
        System.out.format("       direction  %8.3f %8.3f %8.3f   theta %8.2f   phi %8.2f \n", lab_direction.x, lab_direction.y, lab_direction.z, lab_theta*RAD, lab_phi*RAD);
        System.out.format("       emission   %8.1f %8.1f %8.1f    time %8.2f  refind %8.4f \n", lab_emission.x, lab_emission.y, lab_emission.z, start_time, refi_emission);
        System.out.format("       impact     %8.1f %8.1f %8.1f    time track %8.2f   vs hit %8.2f \n", meas_hit.x, meas_hit.y, meas_hit.z, traced.get_time(), meas_time);
        System.out.println("  ");
        System.out.format(" AERO  entrance   %8.1f %8.1f %8.1f \n", aero_entrance.x, aero_entrance.y, aero_entrance.z);
        System.out.format(" AERO  middle     %8.1f %8.1f %8.1f \n", aero_middle.x, aero_middle.y, aero_middle.z);
        System.out.format(" AERO  exit       %8.1f %8.1f %8.1f \n", aero_exit.x, aero_exit.y, aero_exit.z);
        System.out.format(" AERO  normal     %8.3f %8.3f %8.3f \n", aero_normal.x, aero_normal.y, aero_normal.z);
        System.out.format(" AERO  RotAxis    %8.3f %8.3f %8.3f RotAngle %8.2f (deg)  %8.2f (mrad)\n", RotAxis.x, RotAxis.y, RotAxis.z, RotAngle*RAD, RotAngle*MRAD);
        System.out.println("  ");
        System.out.format(" REF   direction  %8.3f %8.3f %8.3f   theta  %8.2f    phi %8.2f \n", ref_direction.x, ref_direction.y, ref_direction.z, ref_theta*RAD, ref_phi*RAD);
        System.out.format("       emission   %8.1f %8.1f %8.1f   --> %8.1f %8.1f %8.1f\n", ori_emission.x, ori_emission.y, ori_emission.z, ref_emission.x, ref_emission.y, ref_emission.z);
        System.out.format("       impact     %8.1f %8.1f %8.1f   --> %8.1f %8.1f %8.1f \n", ori_impact.x, ori_impact.y, ori_impact.z, ref_impact.x, ref_impact.y, ref_impact.z);
        System.out.format("       projection                              --> %8.1f %8.1f %8.1f \n", ref_proj.x, ref_proj.y, ref_proj.z);
        System.out.println("  ");

    }

    // ----------------
    public void shortshow() {
    // ----------------

        Vector3d lab_direction = direct_ray.diff().normalized();
        Vector3d ref_direction = ref_impact.minus(ref_emission).normalized();
        Vector3d ori_emission  = lab_emission.minus(reference);
        Vector3d ori_impact    = meas_hit.minus(reference);

        System.out.format(" PART  id  %4d   type %4d   pid  %4d   mass %8.5f   mom %g \n", id, type, CLASpid, get_mass(CLASpid), momentum);
        System.out.println("  ");
        System.out.format(" TRACK direction  %8.3f %8.3f %8.3f   theta %8.2f   phi %8.2f \n", lab_direction.x, lab_direction.y, lab_direction.z, lab_theta*RAD, lab_phi*RAD);
        System.out.format("       emission   %8.1f %8.1f %8.1f    time %8.2f \n", lab_emission.x, lab_emission.y, lab_emission.z, start_time);
        System.out.format("       impact     %8.1f %8.1f %8.1f    time track %8.2f   vs hit %8.2f \n", meas_hit.x, meas_hit.y, meas_hit.z, traced.get_time(), meas_time);
        System.out.println("  ");
        //System.out.format(" ANGLE theta (deg)  %8.2f    EtaC  (mrad) %8.3f  time %8.3f vs hit %8.3f prob %8.5f %8.5f %8.5f %8.5f %8.5f xy %6.1f %6.1f mom %8.2f pid %3d\n", 
        //       analytic.get_EtaC()*RAD, analytic.get_EtaC()*MRAD, start_time+analytic.get_time(),meas_time,elprob,piprob,kprob,prprob,bgprob,meas_hit.x, meas_hit.y, momentum, CLASpid);

    }

    // ----------------
    public int get_rayrefle(ArrayList<RICHRay> rays) {
    // ----------------

        int nrfl=0;
        for (RICHRay ray : rays) {
            int refe = (int) ray.get_type()/10000;
            if(refe == 1) nrfl++;
        }
        return nrfl;
    }

}
