package org.jlab.rec.rich;

import org.jlab.detector.geom.RICH.RICHLayer;
import org.jlab.detector.geom.RICH.RICHRay;
import org.jlab.detector.geom.RICH.RICHComponent;
import org.jlab.detector.geom.RICH.RICHIntersection;
import org.jlab.detector.geom.RICH.RICHGeoConstants;
import org.jlab.detector.geom.RICH.RICHGeoFactory;
import java.util.ArrayList;
import java.util.List;

import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.geant4.v2.RICHGeant4Factory;
import org.jlab.detector.volume.G4Stl;
import org.jlab.detector.volume.G4Box;

import org.jlab.geom.prim.Vector3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;

import org.freehep.math.minuit.FCNBase;
import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnScan;
import org.freehep.math.minuit.MnUserParameters;

import org.jlab.clas.pdg.PhysicsConstants;


/**
 *
 * @author mcontalb
 */
public class RICHRayTrace{

    private final static RICHGeoConstants geocost =  new RICHGeoConstants();

    private RICHGeoFactory richgeo;
    private RICHParameters  richpar;

    private static final double  RAD = RICHConstants.RAD;
    private static final double MRAD = RICHConstants.MRAD;


    //------------------------------
    public RICHRayTrace() {
    //------------------------------
    }


    //------------------------------
    public RICHRayTrace(RICHGeoFactory richgeo, RICHParameters richpar){
    //------------------------------

        this.richgeo = richgeo;
        this.richpar = richpar;

    }


    //------------------------------
    public RICHLayer get_Layer(int isec, String slay){
    //------------------------------

        return richgeo.get_Layer(isec, slay);

    }


    //------------------------------
    public RICHLayer get_Layer(int isec, int ilay){ 
    //------------------------------

        return richgeo.get_Layer(isec, ilay);

    }


    //------------------------------
    public RICHComponent get_Component(int isec, int ilay, int ico){ 
    //------------------------------

        return richgeo.get_Layer(isec, ilay).get(ico);

    }


    // ----------------
    public Vector3D Reflection(Vector3D vector1, Vector3D normal) {
    // ----------------

        int debugMode = 0;
        Vector3D vin = vector1.asUnit();
        Vector3D vnorm = normal.asUnit();

        double cosI  =  vin.dot(vnorm); 
        if(debugMode>=1)System.out.format("Vector in %s  vnorm %s cosI %7.3f \n ",vin.toStringBrief(3),vnorm.toStringBrief(3),cosI);
        if (cosI > 0) {
            if(debugMode>=1)System.out.format("Mirror normal parallel to impinging ray %7.3f \n",cosI);
            vnorm.scale(-1.0);
        }

        double refle = 2*(vin.dot(vnorm));
        Vector3D vout = vin.sub(vnorm.multiply(refle));

        if(debugMode>=1){
            System.out.format("Mirror normal %s\n",normal.toStringBrief(3));
            System.out.format("Reflected versor %s\n", vout.asUnit().toStringBrief(3));
        }

        return vout.asUnit();
    }

    // ----------------
    public Vector3D Transmission2(Vector3D vector1, Vector3D normal, double n_1, double n_2) {
    // ----------------

        int debugMode = 0;
        double rn = n_1 / n_2;

        Vector3D vin = vector1.asUnit();
        Vector3D vnorm = normal.asUnit();

        double cosI  =  vin.dot(vnorm); 
        if(debugMode>=1)System.out.format("Vector in %s  vnorm %s cosI %7.3f \n ",vin.toStringBrief(3),vnorm.toStringBrief(3),cosI);
        if (cosI < 0) {
            if(debugMode>=1)System.out.format("Mirror normal parallel to impinging ray %7.3f \n",cosI);
            vnorm.scale(-1.0);
        }
        if(debugMode>=1)System.out.format("Vector in %s  vnorm %s cosI %7.3f \n ",vin.toStringBrief(3),vnorm.toStringBrief(3),cosI);

        Vector3D vrot = (vnorm.cross(vin)).asUnit();
 
        double angi = Math.acos(vin.dot(vnorm)) ;
        double ango = Math.asin( rn * Math.sin(angi));

        Quaternion q = new Quaternion(ango, vrot);

        Vector3D vout = q.rotate(vnorm);

        if(debugMode>=1){
            System.out.format(" vin   %s \n", vin.toStringBrief(3));
            System.out.format(" vnorm %s \n", vnorm.toStringBrief(3)); 
            System.out.format(" angles %7.3f %7.3f \n",angi*57.3, ango*57.3);
            System.out.format(" vout  %s \n", vout.toStringBrief(3)); 
        }

        return vout;

    }
 
    // ----------------
    public RICHRay OpticalRotation(RICHRay rayin, RICHIntersection intersection) {
    // ----------------

        int debugMode = 0;
        Point3D vori = rayin.origin();
        Vector3D inVersor = rayin.direction().asUnit();
        Vector3D newVersor = new Vector3D(0.0, 0.0, 0.0);
        RICHRay rayout = null;
        int type = 0;
 
        if(debugMode>=1)System.out.format("Ray for %3d %3d %3d \n",intersection.get_sector(), intersection.get_layer(), intersection.get_component());
        RICHLayer layer = richgeo.get_Layer(intersection.get_sector(), intersection.get_layer());

        if(layer.is_optical()==true){
                
            if(debugMode>=1)System.out.format("Ray rotation at Optical compo %3d %3d  xyz %s \n", intersection.get_layer(), intersection.get_component(), vori.toStringBrief(2));
            Vector3D vnorm = intersection.get_normal();
            if(vnorm != null ){
                if(layer.is_mirror()==true){
             
                    newVersor = Reflection(inVersor, vnorm);
                    type=10000+intersection.get_layer()*100+intersection.get_component()+1;
                    if(debugMode>=1)System.out.format(" Reflection at mirror surface norm %s \n", vnorm.toStringBrief(3));

                }else{

                    newVersor = Transmission2(inVersor, vnorm, intersection.get_nin(), intersection.get_nout());
                    type=20000+intersection.get_layer()*100+intersection.get_component()+1;
                    if(debugMode>=1){
                        System.out.format(" Refraction at surface boundary norm %s \n", vnorm.toStringBrief(3));
                        System.out.format(" norm in %s %7.4f \n",vnorm.toStringBrief(3), vnorm.costheta());
                        System.out.format(" vers in %s %7.4f \n",inVersor.toStringBrief(3), inVersor.costheta());
                        System.out.format(" vers ou %s %7.4f \n",newVersor.toStringBrief(3), newVersor.costheta());
                    }
                }
            }

            if(debugMode>=1)System.out.format(" Versor in %s   --> out %s \n",inVersor.toStringBrief(3), newVersor.toStringBrief(3)); 
        }

        rayout = new RICHRay(vori, newVersor.multiply(200));
        rayout.set_type(type);
        return rayout;

    }

    // ----------------
    public ArrayList<RICHRay> RayTrace(RICHParticle photon, Vector3D vlab) {
    // ---------------- 

        int debugMode = 0;

        RICHLayer layer = get_Layer(photon.get_sector(), photon.ilay_emission);
        if(debugMode>=1)System.out.format("Raytrace gets refractive index from CCDB database %8.5f \n",layer.get(photon.ico_emission).get_index());
        return RayTrace(photon, vlab, layer.get(photon.ico_emission).get_index());

    }


    // ----------------
    public ArrayList<RICHRay> RayTrace(RICHParticle photon, Vector3D vlab, double naero) {
    // ---------------- 
    // return the hit position on the PMT plane of a photon emitted at emission with direction vlab

        int debugMode = 0;
        ArrayList<RICHRay> raytracks = new ArrayList<RICHRay>();

        int orilay = photon.ilay_emission;
        int orico  = photon.ico_emission;
        int isec   = photon.get_sector();
        Point3D emi = photon.lab_emission;
        Vector3D vdir = vlab;

        RICHRay lastray = new RICHRay(emi, vdir.multiply(200));
        if(debugMode>=1) {
            System.out.format(" --------------------------- \n");
            System.out.format("Raytrace photon ori %s  olay %3d  oco %3d  dir %s \n",emi.toStringBrief(2),orilay,orico,vdir.toStringBrief(3)); 
            System.out.format(" --------------------------- \n");
        }

        RICHLayer layer = get_Layer(isec, orilay);
        if(layer==null)return null;

        RICHIntersection first_intersection = null;
        if(richpar.DO_CURVED_AERO==1){
            first_intersection = layer.find_ExitCurved(lastray.asLine3D(), orico);
        }else{
            first_intersection = layer.find_Exit(lastray.asLine3D(), orico);
        }
        if(first_intersection==null)return null;   

        if(debugMode>=1){
            System.out.format(" first inter : ");
            first_intersection.showIntersection();
        }

        Point3D new_pos = first_intersection.get_pos();
        RICHRay oriray = new RICHRay(emi, new_pos);

        /* rewrite the refractive index to be consistent with photon theta
           only valid for initial aerogel
           the rest of components take ref index from CCDB database 
        */
        //oriray.set_refind(layer.get(orico).get_index());
        first_intersection.set_nin((float) naero);
        oriray.set_refind(naero);
        raytracks.add(oriray);

        RICHRay rayin = new RICHRay(new_pos, oriray.direction().multiply(200));
        lastray = OpticalRotation(rayin, first_intersection);
        lastray.set_refind(geocost.RICH_AIR_INDEX);
        RICHIntersection last_intersection = first_intersection;

        if(debugMode>=1){
            System.out.format(" add first ray : ");
            oriray.showRay();
            System.out.format(" get rotated ray : ");
            lastray.showRay();
        }

        int jj = 1;
        int front_nrefl = 0;
        boolean detected = false;
        boolean lost = false;
        while( detected == false && lost == false && raytracks.size()<10){

            Point3D last_ori  = lastray.origin();
            Point3D new_hit = null;
            RICHIntersection new_intersection = null;
            if(debugMode>=1)System.out.format(" ray-tracking step %d \n",jj);

            if(last_intersection.get_layer()<4){
  
                // planar mirrors
                RICHIntersection test_intersection = get_Layer(isec, "MIRROR_BOTTOM").find_Entrance(lastray.asLine3D(), -1);
                if(test_intersection==null)test_intersection = get_Layer(isec, "MIRROR_LEFT_L1").find_Entrance(lastray.asLine3D(), -1);
                if(test_intersection==null)test_intersection = get_Layer(isec, "MIRROR_RIGHT_R1").find_Entrance(lastray.asLine3D(), -1);
                if(test_intersection==null)test_intersection = get_Layer(isec, "MIRROR_LEFT_L2").find_Entrance(lastray.asLine3D(), -1);
                if(test_intersection==null)test_intersection = get_Layer(isec, "MIRROR_RIGHT_R2").find_Entrance(lastray.asLine3D(), -1);
                if(test_intersection!=null){
                    if(debugMode>=1){
                        System.out.format(" test planar (z %7.2f, step %7.2f) : ",last_ori.z(), test_intersection.get_pos().distance(last_ori));
                        test_intersection.showIntersection();
                    }
                    //if(test_intersection.get_pos().distance(last_ori)>RICHConstants.PHOTON_DISTMIN_TRACING)new_intersection = test_intersection;
                    new_intersection = test_intersection;
                }else{
                    if(debugMode>=1)System.out.format(" no lateral mirror intersection \n");
                }

                // shperical mirrors
                if(lastray.direction().costheta()>0){
                    test_intersection = get_Layer(isec, "MIRROR_SPHERE").find_EntranceCurved(lastray.asLine3D(), -1);
                    
                    if(test_intersection!=null){
                        if(debugMode>=1){
                            System.out.format(" test sphere (z %7.2f, step %7.2f) : ",last_ori.z(), test_intersection.get_pos().distance(last_ori));
                            test_intersection.showIntersection();
                        }
                        //if(test_intersection.get_pos().distance(last_ori)>RICHConstants.PHOTON_DISTMIN_TRACING){
                            if(new_intersection==null || (new_intersection!=null && test_intersection.get_pos().z()<new_intersection.get_pos().z())) {
                                new_intersection = test_intersection;
                            }
                        //}
                    }else{
                        if(debugMode>=1)System.out.format(" no sphere intersection \n");
                    }

                    RICHIntersection pmt_inter = get_Layer(isec, "MAPMT").find_Entrance(lastray.asLine3D(), -1);
                    if(pmt_inter!=null) {
                        Point3D test_hit = pmt_inter.get_pos(); 
                        //if(test_hit.distance(last_ori)>RICHConstants.PHOTON_DISTMIN_TRACING){
                            new_hit=test_hit;
                            if(debugMode>=1)System.out.format(" test PMT : Hit %s \n",new_hit.toStringBrief(2));
                        //}else{
                            //if(debugMode>=1)System.out.format(" too far PMT plane intersection \n");
                        //}
                    }else{
                        if(debugMode>=1)System.out.format(" no PMT plane intersection \n");
                    }
                }else{
                    test_intersection = get_Layer(isec, "MIRROR_FRONT_B1").find_Entrance(lastray.asLine3D(), -1);
                    if(test_intersection==null)test_intersection = get_Layer(isec, "MIRROR_FRONT_B2").find_Entrance(lastray.asLine3D(), -1);
                    if(test_intersection!=null){
                        if(debugMode>=1){
                            System.out.format(" test front (z %7.2f, step %7.2f) : ",last_ori.z(), test_intersection.get_pos().distance(last_ori));
                            test_intersection.showIntersection();
                        }
                        //if(test_intersection.get_pos().distance(last_ori)>RICHConstants.PHOTON_DISTMIN_TRACING)new_intersection = test_intersection; 
                        new_intersection = test_intersection;
                        front_nrefl++;
                    }else{
                        if(debugMode>=1)System.out.format(" no front mirror intersection \n");
                    }
                }

            }

            if(new_hit!=null){
                if(new_intersection==null || new_hit.distance(last_ori) <= new_intersection.get_pos().distance(last_ori)) {
                    detected=true;
                    if(debugMode>=1) System.out.format(" found PMT hit %s  dist %6.2f \n", new_hit.toStringBrief(2), new_hit.distance(last_ori));
                }
            }
            if(front_nrefl>richpar.RAY_NFRONT_REFLE){
                lost = true; 
                new_hit=new_intersection.get_pos();
                if(debugMode>=1)System.out.format(" double front reflection: stop at front %s \n",new_hit.toStringBrief(2));
            }
            if(new_hit==null && new_intersection==null){
                lost = true; 
                Point3D point = new Point3D(0.0, 0.0, 0.0);;
                new_hit = new Point3D(lastray.end());
                Plane3D plane = richgeo.toTriangle3D(get_Layer(isec, "MAPMT").get_Face(0)).plane();
                if(plane.intersection(lastray.asLine3D(), point)==1){ 
                    double vers = lastray.direction().costheta();
                    double Delta_z = point.z()-lastray.origin().z();
                    if(debugMode>=1) System.out.format(" forced stop at PMT plane: Delta_z %7.3f vers %7.3f \n",Delta_z, vers);
                    if(Delta_z*vers>0){
                        new_hit=point;
                        if(debugMode>=1) System.out.format(" take PMT plane hit %s \n", new_hit.toStringBrief(2));
                    }else{
                        if(debugMode>=1) System.out.format(" no Delta_z on PMT plane: take last ray end %s \n", new_hit.toStringBrief(2));
                    }
                }else{
                    if(debugMode>=1) System.out.format(" no hit on PMT plane: take last ray end %s \n", new_hit.toStringBrief(2));
                }
            }

            if(lost || detected){
                if(debugMode>=1 && lost) System.out.format("LOST! stop ray-tracing \n");
                if(debugMode>=1 && detected) System.out.format("DETECTED! stop ray-tracing \n");

                RICHRay newray = new RICHRay(last_ori, new_hit);
                newray.set_type(lastray.get_type());
                newray.set_refind((float) geocost.RICH_AIR_INDEX);
                if(detected)newray.set_detected();
                raytracks.add(newray);
                if(debugMode>=1){
                    System.out.format(" --> Add last ray (%7.4f) : ", geocost.RICH_AIR_INDEX);
                    newray.showRay();
                }

            }else{

                RICHRay newray = new RICHRay(last_ori, new_intersection.get_pos());
                newray.set_refind(new_intersection.get_nin());
                newray.set_type(lastray.get_type());
                raytracks.add(newray);

                // new ray starting at intersection, to be rotated
                rayin = new RICHRay(new_intersection.get_pos(), newray.direction().multiply(200));
                lastray = OpticalRotation(rayin, new_intersection);
                lastray.set_refind(new_intersection.get_nout());

                if(debugMode>=1){
                    System.out.format(" -->  Add new ray (%7.4f) : ",new_intersection.get_nin());
                    newray.showRay();
                    System.out.format(" -->  Get rotated ray (%7.4f) : ",new_intersection.get_nout());
                    lastray.showRay();
                }

            }
            jj++;

        }

        if(debugMode>=1) System.out.format(" --------------------------- \n");
        //if(detected==true)return raytracks;
        return raytracks;
        //return null;
   }


    // ----------------
    public void find_EtaC_raytrace_migrad(RICHParticle hadron, RICHParticle photon) {
    // ----------------

        int debugMode = 0;
        double n_a = geocost.RICH_AIR_INDEX;

        double Phi_ini = photon.trial_pho.lab_phi;
        double Theta_ini = photon.trial_pho.lab_theta;
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
                    Vector3D vpho = new Vector3D( Math.sin(theta)*Math.cos(phi), Math.sin(theta)*Math.sin(phi), Math.cos(theta));
                    ArrayList<RICHRay> rays = RayTrace(photon, vpho); 
                    double Function = 999;
                    Point3D pmt_hit = new Point3D(0.0, 0.0, 0.0);
                    if(rays!=null){
                        pmt_hit = rays.get(rays.size()-1).end();
                        Function = pmt_hit.distance(photon.get_HitPos());
                    }

                    if(debugMode>=0)System.out.format(" ==>  %8.2f %8.2f | %s vs %s -->  %10.4f \n", 
                                                 theta*RAD, phi*RAD, pmt_hit.toStringBrief(2),
                                                 photon.get_HitPos().toStringBrief(2), Function);
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

            int CLASpid = photon.get_CLASpid();
            double n_tile = 1/(hadron.get_beta(CLASpid)*(Math.sin(Theta_P)* Math.sin(Theta_Min)*Math.cos(Phi_Min-Phi_P)+Math.cos(Theta_P)*Math.cos(Theta_Min)));
            double arg = Math.pow(n_a, 2)-Math.pow(n_tile, 2)*Math.pow(Math.sin(Theta_Min), 2);
            double Denominator = 1e-4;
            if(arg>0) Denominator = Math.sqrt(arg);

            double Cos_EtaC=Math.sin(Theta_P)* Math.sin(Theta_Min)*Math.cos(Phi_Min-Phi_P)+Math.cos(Theta_P)*Math.cos(Theta_Min);

            photon.traced.set_theta((float) Theta_Min);
            photon.traced.set_phi((float) Phi_Min);
            photon.traced.set_aeron((float) n_tile);
            photon.traced.set_EtaC((float) Math.acos(Cos_EtaC));

            Vector3D vmin = new Vector3D( Math.sin(Theta_Min)*Math.cos(Phi_Min), Math.sin(Theta_Min)*Math.sin(Phi_Min), Math.cos(Theta_Min));
            ArrayList<RICHRay> raysmin = RayTrace(photon, vmin);
            if(raysmin!=null)photon.traced.set_raytracks(raysmin);

            if(debugMode>=1){
                photon.traced.show_raytrack();
                System.out.format("#### ETAC ref %8.4f  min %8.4f  path %8.2f  time %8.2f \n", photon.EtaC_ref*MRAD,  
                                         photon.traced.get_EtaC()*MRAD, photon.traced.get_raypath(), photon.traced.get_raytime());
            }

        }else{
            if(debugMode>=0) System.out.format("Missing starting values from trial photon\n");
        }

    }


    // ----------------
    public void find_EtaC_raytrace_steps(RICHParticle hadron, RICHParticle photon, int hypo) {
    // ----------------

        int debugMode = 0;

        if(hypo<0 || hypo>=RICHConstants.N_HYPO ) return;
        double n_a = geocost.RICH_AIR_INDEX;
        int hypo_pid = RICHConstants.HYPO_LUND[hypo];

        if(photon.trial_pho==null){
            if(debugMode>=1) System.out.format(" Missing starting values from trial photon\n");
            return;
        }

        double Theta_P = hadron.lab_theta;
        double Phi_P = hadron.lab_phi;
        if(debugMode>=1) {
            System.out.format(" Hadron phi %8.2f theta %8.2f \n", Phi_P*RAD, Theta_P*RAD);
            System.out.format(" Measured hit %s \n",photon.get_HitPos().toStringBrief(2));
        }

        // taking starting point (from most closed throws)
        double phi_min    = photon.trial_pho.lab_phi;
        double the_min    = photon.trial_pho.lab_theta;
        double dphi_min   = 0;
        double dthe_min   = 0;
        Point3D pmt_min  = photon.trial_pho.get_HitPos();
        int nrefle_min = photon.trial_pho.traced.get_nrefle();
        ArrayList<RICHRay> rays_min = new ArrayList(); 
        rays_min = photon.trial_pho.traced.get_raytracks();

        Vector3D vec_dist = photon.get_HitPos().vectorFrom(pmt_min);
        // ATT: this takes the projection on the z plane. Equivalent but unnecessary.
        double dist = Math.sqrt(vec_dist.x()*vec_dist.x()+vec_dist.y()*vec_dist.y());
        double Cos_EtaC = Math.sin(Theta_P)* Math.sin(the_min)*Math.cos(phi_min-Phi_P)+Math.cos(Theta_P)*Math.cos(the_min);
        double EtaCmin = 0.0;
        if(Math.abs(Cos_EtaC)<1.)EtaCmin = Math.acos(Cos_EtaC);

        int ntrials = 0;
        if(debugMode>=1)System.out.format("check %7.2f [%7.2f %7.2f %7.2f --> %7.2f] :  %4d [%4d] \n ",dist,
                       photon.nominal_sChAngle(), RICHConstants.GAP_NOMINAL_SIZE, richpar.RAYTRACE_RESO_FRAC,
                       photon.nominal_sChAngle()*RICHConstants.GAP_NOMINAL_SIZE*richpar.RAYTRACE_RESO_FRAC,ntrials,richpar.RAYTRACE_MAX_NSTEPS);
        while (dist > photon.nominal_sChAngle()*RICHConstants.GAP_NOMINAL_SIZE*richpar.RAYTRACE_RESO_FRAC && ntrials<richpar.RAYTRACE_MAX_NSTEPS){ 

            if(debugMode>=1){ 
                System.out.format(" Attempt %d  with the %7.1f (%7.2f)  phi %7.2f  EtaC  %7.2f\n",ntrials, the_min*MRAD, the_min*RAD, phi_min*RAD, EtaCmin*MRAD); 
                System.out.format(" --> (nrfl %2d) pos %s  dist %7.4f (x %7.4f  y %7.4f) \n", nrefle_min, pmt_min.toStringBrief(2), dist, vec_dist.x(), vec_dist.y());
            }
            double dthe = 0.0;
            double dphi = 0.0;
 
            for (int nthe=1; nthe<=4; nthe++){
                double theta_dthe = the_min + photon.nominal_sChAngle()/nthe;
                Vector3D vpho_dthe = new Vector3D( Math.sin(theta_dthe)*Math.cos(phi_min), Math.sin(theta_dthe)*Math.sin(phi_min), Math.cos(theta_dthe));
                double naero = 1/(hadron.get_beta(hypo_pid)*(Math.sin(Theta_P)* Math.sin(theta_dthe)*Math.cos(phi_min-Phi_P)+Math.cos(Theta_P)*Math.cos(theta_dthe)));

                ArrayList<RICHRay> rays_dthe = RayTrace(photon, vpho_dthe, naero);
                if(rays_dthe!=null && rays_dthe.get(rays_dthe.size()-1).is_detected()){
                    int nrefle_dthe = get_Nrefle(rays_dthe);
                    if(debugMode>=1) System.out.format("     test %2d  the %7.1f  nrfl %2d vs %2d ",nthe, theta_dthe*MRAD, nrefle_dthe, nrefle_min); 
                    if(nrefle_dthe==nrefle_min){
                        Point3D pmt_dthe = rays_dthe.get(rays_dthe.size()-1).end();
                        Vector3D vers_dthe = pmt_dthe.vectorFrom(pmt_min);
                        // shift corresponding to an angular sigma
                        dthe_min = pmt_dthe.distance(pmt_min)*nthe;
                        // theta step for minimization
                        dthe = (vec_dist.x()*vers_dthe.x() + vec_dist.y()*vers_dthe.y()) / (vers_dthe.x()*vers_dthe.x() + vers_dthe.y()*vers_dthe.y()) * photon.nominal_sChAngle();
                        if(debugMode>=1) {
                            System.out.format("   --> dthe pos %s  delta %7.1f (%8.2f %8.2f)  %7.2f \n", 
                                pmt_dthe.toStringBrief(2), dthe*MRAD, vers_dthe.x(), vers_dthe.y(),dthe_min);
                            dump_raytrack("TTTT",rays_dthe);
                        }
                        break;
                    }else{
                        if(debugMode>=1) System.out.format("   failed \n");
                    }
                }
            }

            for (int nphi=1; nphi<=4; nphi++){
                double phi_dphi = phi_min + photon.nominal_sChAngle()/nphi;
                Vector3D vpho_dphi = new Vector3D( Math.sin(the_min)*Math.cos(phi_dphi), Math.sin(the_min)*Math.sin(phi_dphi), Math.cos(the_min));
                double naero = 1/(hadron.get_beta(hypo_pid)*(Math.sin(Theta_P)* Math.sin(the_min)*Math.cos(phi_dphi-Phi_P)+Math.cos(Theta_P)*Math.cos(the_min)));

                ArrayList<RICHRay> rays_dphi = RayTrace(photon, vpho_dphi, naero);
                if(rays_dphi!=null && rays_dphi.get(rays_dphi.size()-1).is_detected()){
                    int nrefle_dphi = get_Nrefle(rays_dphi);
                    if(debugMode>=1) System.out.format("     test %2d  phi %7.2f  nrfl %2d vs %2d ",nphi, phi_dphi*RAD, nrefle_dphi, nrefle_min); 
                    if(nrefle_dphi==nrefle_min){
                        Point3D pmt_dphi = rays_dphi.get(rays_dphi.size()-1).end();
                        Vector3D vers_dphi = (pmt_dphi.vectorFrom(pmt_min));
                        // shift corresponding to an angular sigma
                        dphi_min = pmt_dphi.distance(pmt_min)*nphi;
                        // phi step for minimization
                        dphi = (vec_dist.x()*vers_dphi.x() + vec_dist.y()*vers_dphi.y()) / (vers_dphi.x()*vers_dphi.x() + vers_dphi.y()*vers_dphi.y()) * photon.nominal_sChAngle();
                        if(debugMode>=1) {
                            System.out.format("   --> dphi pos %s  delta %7.2f (%8.2f %8.2f)  %7.2f \n", 
                                pmt_dphi.toStringBrief(2), dphi*RAD, vers_dphi.x(), vers_dphi.y(),dphi_min);
                            dump_raytrack("TTTT",rays_dphi);
                        }
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

                    Vector3D vpho_min = new Vector3D( Math.sin(the_new)*Math.cos(phi_new), Math.sin(the_new)*Math.sin(phi_new), Math.cos(the_new));
                    double naero = 1/(hadron.get_beta(hypo_pid)*(Math.sin(Theta_P)* Math.sin(the_new)*Math.cos(phi_new-Phi_P)+Math.cos(Theta_P)*Math.cos(the_new)));

                    rays_min = RayTrace(photon, vpho_min, naero);
                    if(rays_min!=null && rays_min.get(rays_min.size()-1).is_detected()){
                        int nrefle_new = get_Nrefle(rays_min);
                        if(debugMode>=1) System.out.format("        test %2d  the %7.1f  phi %7.2f  nrfl %2d vs %2d ",nn, the_new*MRAD, phi_new*RAD, nrefle_new, nrefle_min); 
                        if(nrefle_new==nrefle_min){
                            the_min = the_new;
                            phi_min = phi_new;
                            pmt_min = rays_min.get(rays_min.size()-1).end();
                            vec_dist = photon.get_HitPos().vectorFrom(pmt_min);
                            dist = Math.sqrt(vec_dist.x()*vec_dist.x()+vec_dist.y()*vec_dist.y());
                            Cos_EtaC = Math.sin(Theta_P)* Math.sin(the_min)*Math.cos(phi_min-Phi_P)+Math.cos(Theta_P)*Math.cos(the_min);
                            EtaCmin = 0.0;
                            if(Math.abs(Cos_EtaC)<1.)EtaCmin = Math.acos(Cos_EtaC);
                            found = 1;
                            if(debugMode>=1) {
                                System.out.format("   found new point %s  dist %7.2f\n",pmt_min.toStringBrief(2), dist);
                                dump_raytrack("TTTT",rays_min);
                            }
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

        if(dist < photon.nominal_sChAngle()*RICHConstants.GAP_NOMINAL_SIZE){

            if(debugMode>=1){
                System.out.format(" -->  Matched value found using %d calls: result is %8.2f %8.2f  matched hit %s  dist %7.3f \n", 
                                  ntrials, the_min*MRAD, phi_min*RAD, pmt_min.toStringBrief(2), dist);
            }

            int CLASpid = photon.get_CLASpid();
            double n_tile = 1/(hadron.get_beta(hypo_pid)*(Math.sin(Theta_P)* Math.sin(the_min)*Math.cos(phi_min-Phi_P)+Math.cos(Theta_P)*Math.cos(the_min)));

            photon.traced.set_theta((float) the_min);
            photon.traced.set_phi((float) phi_min);
            photon.traced.set_dthe_res((float) dthe_min);
            photon.traced.set_dphi_res((float) dphi_min);
            photon.traced.set_dthe_bin((float) photon.trial_pho.traced.get_dthe_bin());
            photon.traced.set_dphi_bin((float) photon.trial_pho.traced.get_dphi_bin());
            photon.traced.set_aeron((float) n_tile);
            photon.traced.set_EtaC((float) EtaCmin);
            photon.traced.set_raytracks(rays_min);

            if(debugMode>=1){
                photon.traced.show_raytrack();
                System.out.format("#### ETAC %s TRA min %8.4f ttime %8.2f  dthe (%7.2f, %7.2f) dphi (%7.2f, %7.2f) \n", 
                    RICHConstants.HYPO_LUND[hypo], photon.traced.get_EtaC()*MRAD, photon.traced.get_time(), 
                    photon.traced.get_dthe_res(), photon.traced.get_dthe_bin(),
                    photon.traced.get_dphi_res(), photon.traced.get_dphi_bin());
            }
        }

    }


    // ----------------
    public double find_dthe_steps(RICHParticle photon) {
    // ----------------

        int debugMode = 0;

        double pho_phi = photon.traced.get_phi();
        Point3D pho_hit = photon.traced.get_hit();
        int pho_nrefle = photon.traced.get_Nrefle();

        if(debugMode>=1)System.out.format("Find_dthe %7.2f %7.2f %s ",
            photon.traced.get_theta()*MRAD,photon.nominal_sChAngle()*MRAD,pho_hit.toStringBrief(2));

        double dthe_res = RICHConstants.TRACE_NOMINAL_DTHE;
        for (int nthe=1; nthe<=4; nthe++){
            double theta_dthe = photon.traced.get_theta() + photon.nominal_sChAngle()/nthe;
            Vector3D vpho_dthe = new Vector3D( Math.sin(theta_dthe)*Math.cos(pho_phi), Math.sin(theta_dthe)*Math.sin(pho_phi), Math.cos(theta_dthe));
            ArrayList<RICHRay> rays_dthe = RayTrace(photon, vpho_dthe);
            if(rays_dthe!=null){
                int nrefle_dthe = get_Nrefle(rays_dthe);
                if(debugMode>=1) System.out.format(" --> test %2d the %7.1f  nrfl %2d vs %2d ",nthe, 
                    theta_dthe*MRAD, nrefle_dthe, pho_nrefle); 

                if(nrefle_dthe==pho_nrefle){
                    Point3D pmt_dthe = rays_dthe.get(rays_dthe.size()-1).end();
                    dthe_res = pmt_dthe.distance(pho_hit)*nthe;
                    if(debugMode>=1)System.out.format(" --> %s %7.2f \n", pmt_dthe.toStringBrief(2),dthe_res);
                    break;
                }else{
                    if(debugMode>=1) System.out.format(" \n");
                }
            }
        }

        return dthe_res;

    }


    // ----------------
    public double find_dphi_steps(RICHParticle photon) {
    // ----------------

        int debugMode = 0;

        double pho_the = photon.traced.get_theta();
        Point3D pho_hit = photon.traced.get_hit();
        int pho_nrefle = photon.traced.get_Nrefle();

        if(debugMode>=1)System.out.format("Find_dphi %7.2f %7.2f %s ",
            photon.traced.get_phi()*MRAD,photon.nominal_sChAngle()*MRAD,pho_hit.toStringBrief(2));

        double dphi_res = RICHConstants.TRACE_NOMINAL_DPHI;
        for (int nphi=1; nphi<=4; nphi++){
            double phi_dphi = photon.traced.get_phi() + photon.nominal_sChAngle()/nphi;
            Vector3D vpho_dphi = new Vector3D( Math.sin(pho_the)*Math.cos(phi_dphi), Math.sin(pho_the)*Math.sin(phi_dphi), Math.cos(pho_the));
            ArrayList<RICHRay> rays_dphi = RayTrace(photon, vpho_dphi);
            if(rays_dphi!=null){
                int nrefle_dphi = get_Nrefle(rays_dphi);
                if(debugMode>=1) System.out.format(" --> test %2d phi %7.1f  nrfl %2d vs %2d ",nphi,
                    phi_dphi*MRAD, nrefle_dphi, pho_nrefle);

                if(nrefle_dphi==pho_nrefle){
                    Point3D pmt_dphi = rays_dphi.get(rays_dphi.size()-1).end();
                    dphi_res = pmt_dphi.distance(pho_hit)*nphi;
                    if(debugMode>=1)System.out.format(" --> %s %7.2f \n", pmt_dphi.toStringBrief(2),dphi_res);
                    break;
                }else{
                    if(debugMode>=1) System.out.format(" \n");
                }
            }
        }

        return dphi_res;

    }


    // ----------------
    public void find_EtaC_analytic_migrad (RICHParticle hadron, RICHParticle photon) {
    // ----------------

        int debugMode = 0;
        double n_a = geocost.RICH_AIR_INDEX;

        // The following definition should be read by the geometry
        // ATT: mismatch con la definizione di emission a 3/4 dell'aerogel
        // ATT: L deve essere calcolato con il coseno
        double T_r = geocost.AERO_REF_THICKNESS*geocost.CM;
        double L = T_r/2.; // middle point is Thickness
        double T_g = hadron.ref_impact.z()-hadron.ref_emission.z()-L;

        Vector3D vec_b = photon.ref_impact.vectorFrom(hadron.ref_proj);
        double radius = vec_b.mag();
        if(debugMode>=1)System.out.println("  T_g "+T_g+" T_r "+T_r+" L "+L+" R "+radius);
        if(radius >=-1 ) {

            // Starting values
            double Phi = photon.ref_phi;
            double Theta_ini = photon.ref_theta;
            double Theta_P = hadron.ref_theta;
            double Phi_P = hadron.ref_phi;
            if(debugMode>=1){
                System.out.format(" Minimization starting values \n");
                System.out.format(" Hadron phi %8.2f initial Theta %8.2f \n ", Phi_P*RAD, Theta_P*RAD);
                System.out.format(" Photon radius %8.2f phi %8.2f Theta %8.2f EtaC %10.4f \n ", radius, Phi*RAD, Theta_ini*RAD, photon.EtaC_ref*MRAD);
            }

            // Minimizing function 
            int CLASpid = photon.get_CLASpid();
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
            photon.analytic.set_theta((float) Theta_Min);
            photon.analytic.set_phi((float) Phi);

            double Cos_EtaC = Math.sin(Theta_P)* Math.sin(Theta_Min)*Math.cos(Phi-Phi_P)+Math.cos(Theta_P)*Math.cos(Theta_Min);

            double n_tile = 1/(hadron.get_beta(CLASpid)*Cos_EtaC);
            double arg = Math.pow(n_a, 2)-Math.pow(n_tile, 2)*Math.pow(Math.sin(Theta_Min), 2);
            double Denominator = 1e-4;
            if(arg>0) Denominator = Math.sqrt(arg);
      
            double migrad_path = ((T_r -L)/Math.cos(Theta_Min) + (T_g*n_a/Denominator) );
            double migrad_time = ( ((T_r -L)/Math.cos(Theta_Min)/n_tile) + (T_g*n_a/Denominator) )/PhysicsConstants.speedOfLight();

            photon.analytic.set_time((float) migrad_time);
            photon.analytic.set_path((float) migrad_path);
            photon.analytic.set_aeron((float) n_tile);
            photon.analytic.set_EtaC((float) Math.acos(Cos_EtaC) );

            if(debugMode>=1){
                System.out.format("#### ETAC ALY ref %8.4f  min %8.4f  path %8.2f  time %8.2f \n", photon.EtaC_ref*MRAD, photon.analytic.get_EtaC()*MRAD, photon.analytic.get_path(), photon.analytic.get_time());
            }

        }
    }

    // ----------------
    public Point3D find_IntersectionSpheMirror(int isec, Line3D ray){
    // ----------------

        return richgeo.find_IntersectionSpheMirror(isec, ray);

    }

    // ----------------
    public Point3D find_IntersectionMAPMT(int isec, Line3D ray){
    // ----------------

        return richgeo.find_IntersectionMAPMT(isec, ray);
    }


    // ----------------
    public int get_Nrefle(ArrayList<RICHRay> rays) {
    // ----------------

        int nrfl=0;
        for (RICHRay ray : rays) {
            int refe = (int) ray.get_type()/10000;
            if(refe == 1) nrfl++;
        }
        return nrfl;
    }


    // ----------------
    public void dump_raytrack(String head, ArrayList<RICHRay> raytracks) {
    // ----------------

        int ii=0;
        for(RICHRay ray: raytracks){
            if(head!=null){
                System.out.format("%s",head);
            }
            System.out.format(" %8d %8d %8d ",ii,get_RefleLayers(raytracks),get_RefleCompos(raytracks));
            ray.dumpRay();
            ii++;
        }
    }


    // ----------------
    public int get_RefleLayers(ArrayList<RICHRay> raytracks) {
    // ----------------

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
    public int get_RefleCompos(ArrayList<RICHRay> raytracks) {
    // ----------------

        int recompo = 0 ;
        if(raytracks.size()<=2) return recompo;
        for(int i=2; i<raytracks.size(); i++){
            double off = Math.pow(10,i-2);
            int ilay = (int) ( raytracks.get(i).get_type() - 10000)/100;
            int icompo = 0;
            if (ilay==11){
                icompo = (int) ( raytracks.get(i).get_type() - 10000 - ilay*100 - 1);
            }else{
                icompo = (int) ilay;
            }
            recompo += off*icompo;
        }
        return recompo;

    }

}
