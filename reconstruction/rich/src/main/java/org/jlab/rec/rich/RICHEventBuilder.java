package org.jlab.rec.rich;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.clas.pdg.PDGDatabase;

import org.jlab.detector.base.DetectorType;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorTrack;
import org.jlab.clas.detector.DetectorData;
import org.jlab.clas.detector.RingCherenkovResponse;
import org.jlab.io.base.DataBank;

import org.jlab.detector.base.DetectorLayer;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Vector3D;

import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.detector.geom.RICH.RICHGeoFactory;
import org.jlab.detector.geom.RICH.RICHGeoConstants;


public class RICHEventBuilder{

    public int Neve = 0;
 
    /* 
    *   Reconstruction classes
    */
    private final DetectorType[] sharedDetectors = {DetectorType.FTOF,DetectorType.CTOF};
    private RICHio                   richio;
    private RICHEvent                richevent;
    private RICHGeoFactory           richgeo;
    private DetectorEvent            clasevent;  // temporarely only Sector 4 particles !!!

    private HashMap<Integer,Integer> pindex_map = new HashMap<Integer, Integer>();

    private static double Precision = 0.00000000001; //E-11 is my threshold for defying what 0 is
    private static double MRAD=1000.;
    private static double RAD=180/Math.PI;

    // ----------------
    public RICHEventBuilder(DataEvent event, RICHEvent richeve, RICHGeoFactory richgeo, RICHio richio) {
    // ----------------

        this.richevent      =   richeve;
        this.richio         =   richio;
        this.richgeo        =   richgeo;
        this.clasevent   =   new DetectorEvent();  

        clasevent.clear();
        richevent.clear();

        set_EventInfo(event);
    }


    // ----------------
    public boolean process_Data(DataEvent event, RICHParameters richpar, RICHCalibration richcal, RICHRayTrace richtrace, RICHTime richtime) {
    // ----------------

        int debugMode = 0;

        /*
        *   look for RICH - DC matches
        */
        if(debugMode>=1)System.out.format("process_DCData \n");
        if(!process_DCData(event, richpar)) return false;
        richtime.save_ProcessTime(3, richevent);

        richio.write_RECBank(event, richevent, richpar);

        /*
        *   create RICH particles
        */
        if(debugMode>=1)System.out.format("process_RICHData\n");
        if(!process_RICHData(event, richtrace, richpar, richcal)) return false;
        richtime.save_ProcessTime(4, richevent);

        /*
        *   analytic solution (direct light only)
        */
        if(debugMode>=1)System.out.format("analyze_Cherenkovs\n");
        if(!analyze_Cherenkovs(richtrace, richpar)) return false;
        richtime.save_ProcessTime(5, richevent);

        /*
        *   ray-traced solution (all photons)
        */
        if(debugMode>=1)System.out.format("reco_Cherenkovs\n");
        if(!reco_Cherenkovs(richtrace, richpar, richcal)) return false;
        richtime.save_ProcessTime(6, richevent);

        if(debugMode>=1)richevent.showEvent();

        richio.write_CherenkovBanks(event, richevent, richpar);
        richtime.save_ProcessTime(7, richevent);

        return true;

    }


    // ----------------
    public boolean process_DCData(DataEvent event, RICHParameters richpar) {
    // ----------------

        int debugMode = 0;

        /*
        Load tracks from time based tracking
        */
        if(debugMode>=1){
            System.out.println("---------------------------------");
            System.out.println("RICH EB: Load Tracks");
            System.out.println("---------------------------------");
        }

        if(!read_ForwardTracks(event)){
            if(debugMode>=1)System.out.print(" no useful track in RICH sectors --> disregard RICH reco \n");
            return false;
        }

        /*
        Load the cluster information
        */
        if(richpar.USE_SIGNAL_BANK==1){
            richevent.add_ResClus( RingCherenkovResponse.readHipoEvent(event, "RICH::Signal",DetectorType.RICH,1) );
        }else{
            richevent.add_ResClus( RingCherenkovResponse.readHipoEvent(event, "RICH::Cluster",DetectorType.RICH,1) );
        }

        if(debugMode>=1){
            if(richevent.get_nResClu()>0){
                System.out.format(" ----------------------------------------- \n");
                if(richpar.USE_SIGNAL_BANK==1){
                    System.out.format(" RICH EB: clus reloaded from RICH::Signal \n");
                }else{
                    System.out.format(" RICH EB: clus reloaded from RICH::Cluster \n");
                }
                System.out.format(" ----------------------------------------- \n");
                for (DetectorResponse rclu: richevent.get_ResClus()){
                    System.out.format("RICHEB Load Clu : --> id %4d   pmt %5d   ene %8.1f   time %8.2f   pos %s \n",
                     rclu.getHitIndex(),rclu.getDescriptor().getComponent(),rclu.getEnergy(),rclu.getTime(),rclu.getPosition().toStringBrief(1));
                }
            }
        }

        /*
        Perform the DC tracks to RICH clusters matching
        */
        if(debugMode>=1){
            System.out.println("---------------------------------");
            System.out.println("RICH EB: Perform Track-RICH Matching");
            System.out.println("---------------------------------");
        }
        process_HitMatching(richpar);

        if(clasevent.getParticles().size()>0)richevent.add_Matches(getRichResponseList());

        return true;
 
    }


    // ----------------
    public boolean process_RICHData(DataEvent event, RICHRayTrace richtrace, RICHParameters richpar, RICHCalibration richcal) {
    // ----------------

        int debugMode = 0;

        if(debugMode>=1){
            System.out.println("---------------------------------");
            System.out.format(" Reco hadrons and photons with npart %d  nmatches %d \n", clasevent.getParticles().size(), get_nMatch());
            System.out.println("---------------------------------");
        }

        /*
        Load the hit information
        */
        if(richpar.USE_SIGNAL_BANK==1){
            richevent.add_ResHits( RingCherenkovResponse.readHipoEvent(event, "RICH::Signal",DetectorType.RICH,0) );
        }else{
            richevent.add_ResHits( RingCherenkovResponse.readHipoEvent(event, "RICH::Hit",DetectorType.RICH,0) );
        }

        if(richevent.get_nResHit()>0){
            if(debugMode>=1){
                System.out.format(" --------------------------------------- \n");
                if(richpar.USE_SIGNAL_BANK==1){
                    System.out.format(" RICH EB: hits reloaded from RICH::Signal\n");
                }else{
                    System.out.format(" RICH EB: hits reloaded from RICH::Hit\n");
                }
                System.out.format(" --------------------------------------- \n");
                for (DetectorResponse rhit: richevent.get_ResHits()){
                    System.out.format("  --> id %4d   sec %3d  pmt %5d   ene %8.1f   time %8.2f   pos %8.1f %8.1f %8.1f \n",
                     rhit.getHitIndex(),rhit.getDescriptor().getSector(),rhit.getDescriptor().getComponent(),
                     rhit.getEnergy(),rhit.getTime(),rhit.getPosition().x(),rhit.getPosition().y(),rhit.getPosition().z());
                }
            }
        }

        if(clasevent.getParticles().size()>0){ 
            if(find_Hadrons(richtrace, richcal, richpar)){
                if(richevent.get_nResHit()>0){
                    if(!find_Photons(richevent.get_ResHits(), richpar, richcal)){
                        if(debugMode>=1)System.out.println("Found no RICH photon \n");
                    }
                }else{
                    if(debugMode>=1)System.out.println("Found no RICH hits \n");
                }
            }else{
                if(debugMode>=1)System.out.println("Found no RICH hadron \n");
            }
        }else{
            if(debugMode>=1)System.out.println("Found no CLAS particle in sector 4 \n");
        }

        return true;

    }


    // ----------------
    public boolean read_ForwardTracks(DataEvent event) {
    // ----------------

        int debugMode = 0;

        if(event.hasBank("REC::Track") && event.hasBank("REC::Particle") && event.hasBank("REC::Traj")){

            DataBank tbank = event.getBank("REC::Track");
            DataBank pbank = event.getBank("REC::Particle");
            DataBank rbank = event.getBank("REC::Traj");
            if(debugMode>=1)  System.out.format("Look for tracks after EB: REC:tk %3d  REC:part %3d  REC:Traj %3d \n",tbank.rows(),pbank.rows(),rbank.rows());

            for(int i = 0 ; i < tbank.rows(); i++){

                int itk = (int) tbank.getShort("index",i);
                int ipr = (int) tbank.getShort("pindex",i);
                int idet = (int) tbank.getByte("detector",i);
                int isec = (int) tbank.getByte("sector", i);

                int charge = tbank.getByte("q", i);
                Vector3D pvec = DetectorData.readVector(pbank, ipr, "px", "py", "pz");
                Vector3D vertex = DetectorData.readVector(pbank, ipr, "vx", "vy", "vz");
                int CLASpid = check_CLASpid( pbank.getInt("pid",ipr) );

                DetectorTrack  tr = new DetectorTrack(charge,pvec.mag(),i);
                tr.setVector(pvec.x(), pvec.y(), pvec.z());
                tr.setVertex(vertex.x(), vertex.y(), vertex.z());
                tr.setSector(isec);

                if(debugMode>=1){ 
                    System.out.format(" from REC::Track %3d  det %4d   -->  tk %4d   sec %4d  theta %8.2f  -->  part %4d   pid %d \n",
                          i,idet,itk,tr.getSector(), pvec.theta()*RAD,ipr,CLASpid);
                }

                /*
                *  disregard central detector tracks
                */
                if(idet!=6)continue;

                int ok = 0;
                if(!richgeo.has_RICH(isec)) continue;

                int naero_cross = 0;
                int ntraj_cross = 0;
                double traj_path[] = {0.0, 0.0, 0.0};
                double aero_path[] = {0.0, 0.0, 0.0};
                Line3D traj_cross[]  = new Line3D[3];
                Line3D aero_cross[]  = new Line3D[3];
                int    aero_lay[]    = {0,0,0};
                for (int j=0; j<rbank.rows(); j++){
                    int jpr = (int) rbank.getShort("pindex",j);
                    int jtk = (int) rbank.getShort("index",j);
                    int jdet = (int) rbank.getByte("detector",j);
                    int jlay = (int) rbank.getByte("layer",j);
                    if (jpr==ipr && jtk==itk){
                        if(debugMode>=1) System.out.format("traj %3d  part %3d  tk %3d  det %3d  lay %3d ",j,jpr,jtk,jdet,jlay);

                        /*
                        *  trajectory plane 42 till 5b.6.2 - 40 since 5c.7.0 - layer 36 since 6b.1.0
                        */
                        if( (jdet==DetectorType.DC.getDetectorId() && jlay==36) || jdet==DetectorType.RICH.getDetectorId()){
                            double jx =  (double) rbank.getFloat("x",j);
                            double jy =  (double) rbank.getFloat("y",j);
                            double jz =  (double) rbank.getFloat("z",j);
                            double jcx =  (double) rbank.getFloat("cx",j);
                            double jcy =  (double) rbank.getFloat("cy",j);
                            double jcz =  (double) rbank.getFloat("cz",j);
                            double path =  (double) rbank.getFloat("path",j);
                            
                            Vector3D vdir = new Vector3D(jcx, jcy, jcz);
                            if(!vdir.unit()) continue;

                            if(jdet==DetectorType.DC.getDetectorId() && jlay==36){
                                traj_cross[0] = new Line3D(jx, jy, jz, vdir.x(), vdir.y(), vdir.z());
                                traj_path[0] = path;
                                if(debugMode>=1) System.out.format(" --> DC3 ");
                                ntraj_cross++;
                            }
                            if(jdet==DetectorType.RICH.getDetectorId() && jlay==1){
                                traj_cross[1] = new Line3D(jx, jy, jz, vdir.x(), vdir.y(), vdir.z());
                                traj_path[1] = path;
                                if(debugMode>=1) System.out.format(" --> PMT ");
                                ntraj_cross++;
                            }
                            if(jdet==DetectorType.RICH.getDetectorId() && jlay>=2 && jlay<=4){
                                aero_cross[naero_cross] = new Line3D(jx, jy, jz, vdir.x(), vdir.y(), vdir.z());
                                aero_path[naero_cross] = path;
                                aero_lay[naero_cross] = jlay;
                                if(debugMode>=1) System.out.format(" --> AER ");
                                naero_cross++;
                            }
                            if(debugMode>=1) System.out.format(" --> %7.2f %7.2f %7.2f | %7.2f %7.2f %7.2f -> %s | path %7.2f ",
                                  jx,jy,jz,jcx,jcy,jcz,vdir.toStringBrief(2),path);
                        }

                        if(debugMode>=1) System.out.format(" [%3d % 3d]\n",ntraj_cross,naero_cross);
                    }
                }

                //overwrite DC3 with first AERO if present
                if(debugMode>=1) System.out.format(" AERO: %4d cross found \n",naero_cross);
                if(naero_cross>0){
                    double minpath = 999.;
                    for (int ia=0; ia<naero_cross; ia++){
                        if(aero_path[ia]>0 && aero_path[ia]<minpath){
                            if(traj_path[0]==0.0)ntraj_cross++;
                            traj_cross[0] = new Line3D( aero_cross[ia].origin(), aero_cross[ia].end());
                            traj_path[0] = aero_path[ia];
                            minpath = aero_path[ia];
                            if(debugMode>=1) System.out.format("  take aero %3d %3d  path %7.2f \n",ia,aero_lay[ia],traj_path[0]);
                        }
                    }
                }

                int detid = DetectorType.RICH.getDetectorId();
                if(debugMode>=1) System.out.format(" TRAJ: %4d crosses found \n",ntraj_cross);
                for (int k=0; k<ntraj_cross; k++){
                    if(traj_path[k]>0){
                        tr.addCross(traj_cross[k].origin().x(), traj_cross[k].origin().y(), traj_cross[k].origin().z(),
                                traj_cross[k].end().x(), traj_cross[k].end().y(), traj_cross[k].end().z() );
                        tr.getTrajectory().add(new DetectorTrack.TrajectoryPoint(detid, k, traj_cross[k], (float) traj_path[k], (float) 0., (float) 0.)); 
                        tr.setPath(traj_path[k]);
                        if(debugMode>=1) System.out.format(" TRAJ: store cross %4d:  %3d %3d with path %7.2f \n",k,DetectorType.RICH.getDetectorId(),k,traj_path[k]);
                    }

                }

                if(tr.getCrossCount()==0){if(debugMode>=1)System.out.format("Traj not found \n"); continue;}

                tr.setStatus(ipr);
                if(debugMode>=1) {
                    System.out.format(" Track cross N %4d  first %s %s %7.2f  last %s %s %7.2f  path %7.2f  status %3d \n \n", tr.getCrossCount(), 
                                tr.getFirstCross().origin().toStringBrief(2), tr.getFirstCross().end().toStringBrief(2), 
                                tr.getPathLength(DetectorType.RICH, 0),
                                tr.getLastCross().origin().toStringBrief(2), tr.getLastCross().end().toStringBrief(2), 
                                tr.getPathLength(DetectorType.RICH, 1), tr.getPath(),tr.getStatus() );
                }

                DetectorParticle particle = new DetectorParticle(tr);
                particle.setPid(CLASpid);
                particle.setBeta( particle.getTheoryBeta(CLASpid) );
                particle.setMass( PDGDatabase.getParticleById(CLASpid).mass() );
                // ATT: FIX ME! 
                clasevent.addParticle(particle);

            }

        }else{

            if(event.hasBank("TimeBasedTrkg::TBTracks") && event.hasBank("TimeBasedTrkg::Trajectory") && event.hasBank("TimeBasedTrkg::TBCovMat")){

                String trackBank = "TimeBasedTrkg::TBTracks";
                String tracjBank = "TimeBasedTrkg::Trajectory";
                String covBank   = "TimeBasedTrkg::TBCovMat";
              if(debugMode>=1)  System.out.format("Look for tracks before EB: %s %s %s \n",trackBank,tracjBank,covBank);

                List<DetectorTrack>  tracks = DetectorData.readDetectorTracks(event, trackBank, tracjBank, covBank);
              if(debugMode>=1){  
                    for(int i = 0 ; i < tracks.size(); i++){
                        Line3D test = tracks.get(i).getLastCross();
                        System.out.format(" %d   sec %d  %s\n", i,tracks.get(i).getSector(),test.origin().toString());
                    }
                }

                for(int i = 0 ; i < tracks.size(); i++){
                  DetectorTrack tr = tracks.get(i);
                    if(tr.getSector()==4){
                        tr.setStatus(i);
                      DetectorParticle particle = new DetectorParticle(tr);
                        particle.setPid(211);
                        clasevent.addParticle(particle);
                    }
                }

            }else{

              if(debugMode>=1)System.out.format("No track banks: Go back\n");
                return false;
            }
        }

        if(debugMode>=1){
            System.out.format(" \n CLAS-Event PARTICLE found %3d \n",clasevent.getParticles().size());
            for(int n = 0; n < clasevent.getParticles().size(); n++){
                DetectorParticle  p = clasevent.getParticle(n);
                DetectorTrack tr = p.getTrack(); 
                show_Particle(p);
                show_Track(tr); 
            }
            System.out.format(" \n");
        }

        if(clasevent.getParticles().size()>0)return true;
        return false;
    }


    // ----------------
    public void process_HitMatching(RICHParameters richpar){
    // ----------------

        int debugMode = 0;
       
        if(debugMode>=1){
            int nc=richevent.get_nResClu();
            for(int n = 0; n < nc; n++){
                DetectorResponse dt = richevent.get_ResClu(n);
                if(debugMode>=1)  System.out.format("Response n %4d %4d %s  time %8.2f   ene %8.2f   pos %8.1f %8.1f %8.1f \n",n,
                        dt.getSector(), dt.getDescriptor().getType().getName(), 
                        dt.getTime(),dt.getEnergy(),dt.getPosition().x(),dt.getPosition().y(),dt.getPosition().z());
             }
        }

        int np = clasevent.getParticles().size();
        for(int n = 0; n < np; n++){
            DetectorParticle  p = this.clasevent.getParticle(n);

            if(debugMode>=1){
                Line3D trajectory = p.getLastCross();
                Point3D ori = trajectory.origin();
                Point3D end = trajectory.end();
                System.out.format("Particle n %4d   itr %3d    path %8.1f   ori %s   end %s\n",
                            n,p.getTrackIndex(),p.getPathLength(),ori.toStringBrief(2),end.toStringBrief(2));
            }

            // Matching tracks to RICH: from LastCross minimum distance in any direction
            Double rich_match_cut = richpar.RICH_DCMATCH_CUT;
            int index = p.getDetectorHit(richevent.get_ResClus(), DetectorType.RICH, -1, rich_match_cut);
            //int index = getDetectorHit(p, richevent.get_ResClus(), DetectorType.RICH, -1, rich_match_cut);
            if(index>=0){

                DetectorResponse res = richevent.get_ResClu(index);
                Point3D ocross = p.getLastCross().origin();
                double dz = res.getPosition().z() - ocross.z();
                if(debugMode>=1)  System.out.format(" --> match found %4d  par %s  clu %s  dz %7.2f \n", index, ocross.toStringBrief(2), res.getPosition().toStringBrief(2), dz); 

                // while storing the match, calculates the matched position as middle point between track and hit 
                // and path (track last cross plus distance to hit) assuming the hit is downstream of last cross
                p.addResponse(res, true);
                if(dz<0){
                    // go backward instead of forward from LastCross
                    double extra = p.getPathLength(res.getPosition());
                    res.setPath( res.getPath()-2*extra);
                    if(debugMode>=1)  System.out.format(" --> Negative Delta z %7.2f --> correct path by %7.2 \n", dz, -2*extra); 
                }

                // Status brings the pindex of the original track to fix the hipo cross-indexes
                res.setAssociation(p.getTrackStatus());
            }

        }
        if(debugMode>=1)  System.out.format(" \n");
    }

    // ----------------
    public void set_EventInfo(DataEvent event) {
    // ----------------

        int debugMode = 0;

        long tt = System.nanoTime();
        richevent.set_CPUTime(tt);
        if(event.hasBank("REC::Event")==true){
            DataBank bankeve = event.getBank("REC::Event");

            float evttime = bankeve.getFloat("startTime",0);
            richevent.set_EventTime(evttime);

            if(debugMode>=1)System.out.format(" Create RICH Event id %8d   time %8.2f \n", richevent.get_EventID(), richevent.get_EventTime()); 
        }

        if(event.hasBank("RUN::config")==true){
            DataBank bankrun = event.getBank("RUN::config");
            richevent.set_RunID(bankrun.getInt("run",0));
            richevent.set_EventID(bankrun.getInt("event",0));

            //int phase = (int) ((bankrun.getLong("timestamp",0)%6 +1)%6) /2;
            int phase = (int) (bankrun.getLong("timestamp",0)%2);
            richevent.setFTOFphase(phase);

            if(debugMode>=1)System.out.println(" Create RICH Event id "+richevent.get_EventID()+" stamp "+bankrun.getLong("timestamp",0)+" phase "+phase);
        }
        if(debugMode>=1)System.out.println(" Create RICH Event id "+richevent.get_EventID()+" reftime"+richevent.get_CPUTime()+" "+tt);

    }


    // ----------------
    public int get_NClasParticle() {
    // ----------------
        return clasevent.getParticles().size();
    }


    // ----------------
    public int get_nMatch() {
    // ----------------
        return richevent.get_nMatch();
    }


    // ----------------
    public boolean find_Hadrons(RICHRayTrace richtrace, RICHCalibration richcal, RICHParameters richpar) {
    // ----------------

        int debugMode = 0;

        if(debugMode>=1){ 
            System.out.println("---------------------------------");
            System.out.println("Find RICH hadrons from CLAS "+get_NClasParticle()+" particles");
            System.out.println("---------------------------------");
        }

        int hindex = 0;
        for(DetectorParticle p : clasevent.getParticles()){

            double theta = p.vector().theta();
            int CLASpid  = check_CLASpid( p.getPid() );
            double CLASbeta = p.getBeta();

            if(debugMode>=1) { 
                System.out.format(" -->  track %4d  %4d  %4d  sec %4d  ori %s    P  %8.2f    the  %8.2f    time @IP %8.2f  path %7.2f\n",
                    p.getTrackIndex(),p.getTrackStatus(),CLASpid, p.getTrackSector(),
                    p.getLastCross().origin().toStringBrief(2),p.vector().mag(),theta*RAD,richevent.get_EventTime(),p.getPathLength());
            }

            DetectorResponse r = null; 
            DetectorResponse exr = null; 
            int nr = 0;
            int nexr = 0;
            double RICHtime = 0.0;
            double Match_chi2 = 0.0;
            int RICHiclu = -1;
            for(DetectorResponse rtest : p.getDetectorResponses()){
                if(rtest.getDescriptor().getType()==DetectorType.RICH){
                    r = rtest;
                    RICHtime = richevent.get_EventTime() + r.getPath()/CLASbeta/(PhysicsConstants.speedOfLight());
                    RICHiclu = r.getHitIndex();
                    double TRACKtime = richevent.get_EventTime() + p.getPathLength()/CLASbeta/(PhysicsConstants.speedOfLight());

                    if(debugMode>=1)System.out.format(" -->  cluster  %4d   hit %s   path %7.2f  -->  rich  %8.2f  vs track %8.2f  time\n \n",RICHiclu,
                         r.getPosition().toStringBrief(2), r.getPath(), r.getTime(), TRACKtime);

                    nr++;
                }
            }
            
            if( (nr==1) || (nr==0 && richpar.DO_MIRROR_HADS==1) ){ 
                if(debugMode>=1)System.out.format("EXTRAPOLATED with nresp %d \n",nr);
                exr = extrapolate_RICHResponse(p, r, richtrace);
                if(exr!=null) nexr++;
            }

            // define the response treatment in special cases
            if(nexr==0){if(debugMode>=1)System.out.format("No RICH intersection for particle with nresp %d and theta %8.2f \n",nr,theta*RAD); continue;}
            if(nr==1)Match_chi2 = 2*exr.getMatchedDistance()/richpar.RICH_HITMATCH_RMS;

            if(debugMode>=1)System.out.format(" -->  intersec  %4d   hit %s   path %7.2f  -->  time %8.2f  chi2 %7.2f \n \n",RICHiclu,
                    exr.getPosition().toStringBrief(2), exr.getPath(), exr.getTime(), Match_chi2);

            RICHParticle richhadron = new RICHParticle(hindex, p, exr, richpar);
            richhadron.set_StartTime(richevent.get_EventTime());
            richhadron.traced.set_time(exr.getTime());
            richhadron.traced.set_machi2(Match_chi2);

            //richhadron.set_HitTime(exr.getTime());
            /*if(!richhadron.set_points(p.getLastCross().origin(), p.getLastCross().end(), exr.getPosition(), exr.getStatus(), richtrace) ){
                 if(debugMode>=1)System.out.println(" ERROR: no MAPMT interesection found for hadron \n"); 
                 continue;
            }*/

            if(!richhadron.find_AerogelPoints(richtrace, richcal) ) {
                 if(debugMode>=1)System.out.println(" ERROR: no aerogel interesection found for hadron \n"); 
                 continue;
            }

            richhadron.traced.set_path((float) richhadron.get_HitPos().distance(richhadron.aero_middle));
            if(debugMode>=1)System.out.format(" Timing id %3d   beta %8.4f |   time eve %8.2f  emi  %8.2f   hit %8.2f  at light speed %8.2f (cm/ns) \n",
                 CLASpid,CLASbeta,richevent.get_EventTime(),richhadron.get_StartTime(), exr.getTime(), PhysicsConstants.speedOfLight());

            if(!richhadron.set_rotated_points() ) {
                 System.out.println(" ERROR: no rotation found \n");
                 continue;
            }

            if(debugMode>=1){
                System.out.format("  ------------------- \n");  
                System.out.format("  Hadron  %4d  id %4d  from part %4d  and clu  %4d  CLAS eve %7d  pid %5d \n ", hindex, richhadron.get_id(), 
                                     p.getTrackStatus(), RICHiclu, richevent.get_EventID(), CLASpid);
                System.out.format("  ------------------- \n");  

                richhadron.show();
                Point3D crosspos = p.getLastCross().origin();
                Point3D crossdir = richhadron.lab_emission;
                System.out.format(" track cross 1  xyz  %s   dir %s \n",crosspos.toStringBrief(3), crossdir.toStringBrief(3));
            }

            richevent.add_Hadron(richhadron);
            hindex++;
        }

        if(debugMode==1 && hindex>1)System.out.format(" MOREHADs \n");
        if(richevent.get_nHad()>0)return true;
        return false;

    }
                    
    // ----------------
    public boolean find_Photons(List<DetectorResponse>  RichHits, RICHParameters richpar, RICHCalibration richcal){
    // ----------------

        int debugMode = 0;
        if(RichHits==null) return false;

        int id = 0;
        for(RICHParticle richhadron : richevent.get_Hadrons()){

            if(debugMode>=1){ 
                System.out.format("---------------------------------\n");
                System.out.format("Find photons from hadron %3d and %6d hits \n",richhadron.get_id(), RichHits.size());
                System.out.format("---------------------------------\n");
            }

            for(int hypo=0; hypo<RICHConstants.N_HYPO; hypo++){

                if(!is_WantedHypo(richpar, hypo))continue;
                for(int k=0 ; k<RichHits.size(); k++) {
                   
                    if(richhadron.get_sector() != RichHits.get(k).getDescriptor().getSector()) continue; 

                    Point3D dummy= new Point3D(0., 0., 0.);
                    RICHParticle photon = new RICHParticle(id, richhadron, RichHits.get(k), dummy, richpar);

                    if(debugMode>=1){
                        System.out.format("  ------------------- \n");  
                        System.out.format("  Photon  id %4d from hit %4d  hadron %3d  and hypo %s\n",
                            photon.get_id(), richhadron.get_id(), RichHits.get(k).getHitIndex(), RICHConstants.HYPO_STRING[hypo]);
                        System.out.format("  ------------------- \n");  
                    }

                    photon.set_rotated_points(richhadron);
                    photon.set_PixelProp(richcal);
                    photon.set_type(hypo);
                    int hypo_pid = RICHConstants.HYPO_LUND[hypo];
                    photon.traced.set_hypo(hypo_pid);
                            
                    richevent.add_Photon(photon);
                    id++;
                }

            }
        }

        if(richevent.get_nPho()>0)return true;
        return false;

    }

    // ----------------
    public boolean analyze_Cherenkovs(RICHRayTrace richtrace, RICHParameters richpar) {
    // ----------------

        int debugMode = 0;

        if(richpar.DO_ANALYTIC==1){

            int hypo = 1;
            richevent.analyze_Photons(hypo, richtrace);
            //richevent.select_Photons(hypo, 0, richpar);

            for(RICHParticle richhadron : richevent.get_Hadrons()){
                if (richhadron.get_Status()==1){
                    //richevent.get_ChMean(richhadron, hypo, 0); 
                    richevent.get_pid(richhadron, 0, richpar); 
                }else{
                    if(debugMode>=1)System.out.format(" Hadron pointing to mirror, skip analytic analysis \n");
                }
            }

        }

        return true;

    }


    // ----------------
    public boolean is_WantedHypo(RICHParameters richpar, int hypo) {
    // ----------------

        if(hypo==0 && richpar.THROW_ELECTRONS==1) return true;
        if(hypo==1 && richpar.THROW_PIONS    ==1) return true;
        if(hypo==2 && richpar.THROW_KAONS    ==1) return true;
        if(hypo==3 && richpar.THROW_PROTONS  ==1) return true;
        return false;
    }


    // ----------------
    public boolean reco_Cherenkovs(RICHRayTrace richtrace, RICHParameters richpar, RICHCalibration richcal) {
    // ----------------

        int debugMode = 0;

        int recotype = RICHRecoType.TRACED.id();
        for(RICHParticle richhadron : richevent.get_Hadrons()){

            int Ntrials = richpar.THROW_PHOTON_NUMBER;

            for (int hypo=0; hypo<RICHConstants.N_HYPO ; hypo++){

                if(!is_WantedHypo(richpar, hypo)) continue;

                if(debugMode==1){
                    System.out.format(" ------------\n");
                    System.out.format(" RECO Hadron %3d with Hypothesis %s \n", richhadron.get_id(), RICHConstants.HYPO_LUND[hypo]);
                    System.out.format(" ------------\n");
                }

                richevent.throw_Photons(richhadron, Ntrials, hypo, richtrace, richpar, richcal);

                if(richpar.TRACE_PHOTONS==1){

                    richevent.associate_Throws(richhadron, hypo, richpar);

                    richevent.trace_Photons(richhadron, hypo, richtrace, richcal);
                    //richevent.get_ChMean(richhadron, hypo, 1); 
                }
            }

            /*for (int hypo=0; hypo<RICHConstants.N_HYPO ; hypo++){
                if(!is_WantedHypo(richpar, hypo)) continue;
                if(richpar.TRACE_PHOTONS==1){
                    richevent.select_Photons(hypo, 1, richpar);
                }
            }*/

           richevent.select_Photons(richhadron, recotype, richpar);
           if(richpar.DO_PASS2_LIKE==1)richevent.get_HypoPID(richhadron, recotype, richpar); 
           if(richpar.DO_PASS1_LIKE==1)richevent.get_pid(richhadron, recotype, richpar); 
           if(richpar.DO_LHCB_LIKE==1)richevent.get_LHCbpid(richhadron, recotype, richpar); 

        }

        return true;
    }


    // ----------------
    public DetectorResponse extrapolate_RICHResponse(DetectorParticle p, DetectorResponse r, RICHRayTrace richtrace){
    // ----------------

        int debugMode = 0;

        int imir=0;
        Point3D extra = richtrace.find_IntersectionMAPMT( p.getTrackSector(), p.getLastCross() );
        if(extra!=null){
            if(debugMode>0)System.out.format(" Extrapolation to MAPMT %s \n", extra.toStringBrief(2));
        }else{
            imir=1;
            extra = richtrace.find_IntersectionSpheMirror( p.getTrackSector(), p.getLastCross() );
            if(extra!=null && debugMode>0)System.out.format(" Extrapolation to SPHER %s \n", extra.toStringBrief(2));
        }
        if(extra==null) return null;

        // this extrapath is correct being calculated along the extrapolated trajectory
        double extrapath = extra.distance( p.getLastCross().origin() );
        if(extra.z()<p.getLastCross().origin().z()) extrapath*=-1;
        
        DetectorResponse exr = new DetectorResponse( p.getTrackSector(), imir, 0);
        exr.getDescriptor().setType(DetectorType.RICH);
        //ATT: this path is correctly extrapolatd along the trajectory
        //ATT: setPath should find r.getPath() if not zero value!
        exr.setPath(p.getPathLength() + extrapath);

        if(r!=null){
            
            Point3D rpos = r.getPosition().toPoint3D(); 
            Line3D  lmatch = new Line3D(rpos, extra);
            Point3D rmatch = lmatch.midpoint();
            exr.setPosition( rpos.x(), rpos.y(), rpos.z() );
            exr.setHitIndex( r.getHitIndex() );
            exr.setMatchPosition( rmatch.x(), rmatch.y(), rmatch.z()); 
            exr.setTime (r.getTime());
            exr.setStatus(1);
            exr.getDescriptor().setSectorLayerComponent( r.getDescriptor().getSector(), r.getDescriptor().getLayer(), r.getDescriptor().getComponent());
            if(debugMode>0)System.out.format(" Take response %s  path %7.2f  time %7.2f\n", rpos.toStringBrief(2), exr.getPath(), exr.getTime());

        }else{

            exr.setPosition( extra.x(), extra.y(), extra.z() );
            exr.setMatchPosition( extra.x(), extra.y(), extra.z()); 
            exr.setHitIndex( -1 );
            exr.setStatus(0);
            double CLASbeta = p.getBeta();
            exr.setTime( richevent.get_EventTime() + exr.getPath()/CLASbeta/(PhysicsConstants.speedOfLight()) );
            if(debugMode>0)System.out.format(" Take extra %s  path %7.2f  time %7.2f\n", extra.toStringBrief(2), exr.getPath(), exr.getTime());

        }


        if(debugMode>=1){
                  Vector3D pos = new Vector3D(0.,0.,0.); 
                  if(r!=null) pos = r.getPosition();
                  
                  System.out.format("exr: status %4d  imir %4d  ex %s  r %s  exr %s  L %7.2f (%7.2f + %7.2f) T %7.2f \n",
                  exr.getStatus(),imir,extra.toStringBrief(2), pos.toStringBrief(2),
                  exr.getPosition().toStringBrief(2),exr.getPath(),p.getPathLength(),extrapath,exr.getTime());
        }

        return exr;
    }


    // -------------
    public int check_CLASpid(int pid) {
    // -------------
    /*
    *  force electron pid when in trouble
    *  (only gamma, e, pi, proton and k are allowed)
    */
    
        int checkpid = 0;
        if (Math.abs(pid)==22 || Math.abs(pid)==11 || Math.abs(pid)==211 | Math.abs(pid)==321 || Math.abs(pid)==2212) {
            checkpid = pid;
        }else{
            checkpid=11;
            if(pid<0)checkpid*=-1;
        }
        return checkpid;
    }


    // ----------------
    public double Pi_Likelihood(double angolo) {
    // ----------------

        double mean = 0.307;
        double sigma= 0.004;
        System.out.println("Angolo: " + angolo);
        double Norm = Math.log(1+1/ sigma*(Math.sqrt(2* Math.PI)));
        double Argomento = 1+Math.exp((-0.5)*Math.pow((angolo - mean)/sigma, 2) )/ sigma*(Math.sqrt(2* Math.PI));
        System.out.println(Argomento);
        double LikeLihood=  Math.log(Argomento)/Norm;
        //System.out.println(LikeLihood);
        return LikeLihood;

    }


    // ----------------
    public Point3D Outer_Intersection(Point3D first, Point3D second, Vector3D direction ) {
    // ----------------

        int debugMode = 0;
        Point3D Emissione = new Point3D(0,0,0);
        // Define a Vector3D as: Second point of intersection  Minus first point of intersection
        Vector3D V_inter = second.vectorFrom(first);
        // See if V_int is pointing in the direction of the track. In this case the first point is the entrance and the second one is the exit
        if( V_inter.asUnit().dot(direction.asUnit()) >0  ) {
            Emissione.setX( second.x() );
            Emissione.setY( second.y() );
            Emissione.setZ( second.z() );
        }
        //Otherwise the first point is the exit point from the volume so I need to change the order
        else
        {
            Emissione.setX( first.x() );
            Emissione.setY( first.y() );
            Emissione.setZ( first.z() );
        }
        if(debugMode>=1){
          System.out.println("First    "+first);
          System.out.println("Second   "+second);
          System.out.println("Direction"+direction);
          System.out.println("V_inter  "+V_inter);
          System.out.println(" prod "+V_inter.asUnit().dot(direction.asUnit()));
          System.out.println("Emission "+Emissione);
      }
        return Emissione;
    }


    // ----------------
    public ArrayList<DetectorResponse>  getRichResponseList(){
    // ----------------
        clasevent.setAssociation();
        ArrayList<DetectorResponse> responses = new ArrayList<DetectorResponse>();
        for(DetectorParticle p : clasevent.getParticles()){
            for(DetectorResponse r : p.getDetectorResponses()){
                    if(r.getDescriptor().getType()==DetectorType.RICH)
                        responses.add(r);
                    }
                }
        return responses;
    }


    // ----------------
    public HashMap<Integer, Integer> getPindexMap() {
    // ----------------
          return this.pindex_map;
    }


    // ----------------
    public void CosmicEvent(List<RICHHit> Hits, List<RICHCluster> Clusters) {
    // ----------------

        int debugMode = 0;
        if(debugMode>=1)  System.out.println("RICH Event Builder: Event Process ");

    }


    // ----------------
    public void show_Particle(DetectorParticle pr) {
    // ----------------

        System.out.format("    Particle pid %4d   mass %9.4f   beta %8.5f   vert %8.2f %8.2f %8.2f   mom %8.3f %8.3f %8.3f \n",
            pr.getPid(),pr.getMass(),pr.getBeta(),
            pr.vertex().x(),pr.vertex().y(),pr.vertex().z(),
            pr.vector().x(),pr.vector().y(),pr.vector().z());

    }


    // ----------------
    public void show_Track(DetectorTrack tr) {
    // ----------------

        Line3D first = tr.getFirstCross();
        Line3D last= tr.getLastCross();
        Point3D ori = first.origin();
        Point3D end = last.origin();
        System.out.format("    Track id %4d %4d  sec %4d   path %8.1f   origin  %8.2f %8.2f %8.2f   end %8.2f %8.2f %8.2f \n",
            tr.getStatus(), 
            tr.getTrackIndex(), 
            tr.getSector(),
            tr.getPath(),
            ori.x(),ori.y(),ori.z(),
            end.x(),end.y(),end.z());
    }

}
