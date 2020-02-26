package org.jlab.rec.rich;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import org.jlab.detector.base.DetectorType;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorTrack;
import org.jlab.clas.detector.DetectorData;
import org.jlab.clas.detector.RingCherenkovResponse;
import org.jlab.io.base.DataBank;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.clas.physics.Vector3;

import org.jlab.geometry.prim.Line3d;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import eu.mihosoft.vrl.v3d.Polygon;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;

import org.jlab.clas.pdg.PhysicsConstants;

public class RICHEventBuilder{

    public int Neve = 0;
 
    /* 
    *   Reconstruction classes
    */
    private RICHTool                 tool;
    private HashMap<Integer,Integer> pindex_map = new HashMap<Integer, Integer>();

    /* 
    *   DATA Event managing classes 
    */
    private DetectorEvent            sector4Event;  // temporarely only Sector 4 particles !!!
    private RICHio                   richio;
    private RICHEvent                richevent;

    private static double Precision = 0.00000000001; //E-11 is my threshold for defying what 0 is
    private static double MRAD=1000.;
    private static double RAD=180/Math.PI;

    // ----------------
    public RICHEventBuilder(RICHEvent richeve, RICHTool richtool, RICHio io) {
    // ----------------

        tool         = richtool;
        richevent    = richeve;
        richio       = io;
        sector4Event = new DetectorEvent();  

    }

    // ----------------
    public void init_Event(DataEvent event) {
    // ----------------

        tool.start_ProcessTime();

        // clear RICH reconstruction classes
	sector4Event.clear();
        richevent.clear();

        set_EventInfo(event);

    }


    // ----------------
    public boolean process_Data(DataEvent event) {
    // ----------------

        int debugMode = 0;

        /*
        *   look for RICH - DC matches
        */
        if(!process_DCData(event)) return false;
        tool.save_ProcessTime(1);

        richio.write_RECBank(event, richevent, tool.get_Constants());

        /*
        *   create RICH particles
        */
        if(!process_RICHData(event)) return false;
        tool.save_ProcessTime(2);

        /*
        *   analytic solution (direct light only)
        */
        if(!analyze_Cherenkovs()) return false;
        tool.save_ProcessTime(3);

        /*
        *   ray-traced solution (all photons)
        */
        if(!reco_Cherenkovs()) return false;
        tool.save_ProcessTime(4);

        if(debugMode>=1)richevent.showEvent();

        richio.write_CherenkovBanks(event, richevent, tool.get_Constants());
        tool.save_ProcessTime(5);

        return true;

    }


    // ----------------
    public boolean process_DCData(DataEvent event) {
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
            if(debugMode>=1)System.out.print(" no track in sector 4 --> disregard RICH reco \n");
            return false;
        }

        /*
        Load the cluster information
        */
        
        richevent.add_ResClus( RingCherenkovResponse.readHipoEvent(event, "RICH::clusters",DetectorType.RICH) );
        if(richevent.get_nResClu()>0){
            if(debugMode>=1){
                System.out.format(" --------------------------------------- \n");
                System.out.format(" RICH EB: clus reloaded from RICH::clusters\n");
                System.out.format(" --------------------------------------- \n");
                for (DetectorResponse rclu: richevent.get_ResClus()){
                    System.out.format("RICHEB Load Clu : --> id %4d   pmt %5d   ene %8.1f   time %8.2f   pos %8.1f %8.1f %8.1f \n",
                     rclu.getHitIndex(),rclu.getDescriptor().getComponent(),rclu.getEnergy(),rclu.getTime(),rclu.getPosition().x(),rclu.getPosition().y(),rclu.getPosition().z());
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
        process_Hit_Matching();

        if(get_nPar()>0)richevent.add_Matches(getRichResponseList());

        return true;
 
    }


    // ----------------
    public boolean process_RICHData(DataEvent event) {
    // ----------------

        int debugMode = 0;

        if(debugMode>=1){
            System.out.println("---------------------------------");
            System.out.format(" Reco hadrons and photons with npart %d  nmatches %d \n",get_nPar(), get_nMatch());
            System.out.println("---------------------------------");
        }

        /*
        Load the hit information
        */
        richevent.add_ResHits( RingCherenkovResponse.readHipoEvent(event, "RICH::hits",DetectorType.RICH) );
        if(richevent.get_nResHit()>0){
            if(debugMode>=1){
                System.out.format(" --------------------------------------- \n");
                System.out.format(" RICH EB: hits reloaded from RICH::hists\n");
                System.out.format(" --------------------------------------- \n");
                for (DetectorResponse rhit: richevent.get_ResHits()){
                    System.out.format("  --> id %4d   pmt %5d   ene %8.1f   time %8.2f   pos %8.1f %8.1f %8.1f \n",
                     rhit.getHitIndex(),rhit.getDescriptor().getComponent(),rhit.getEnergy(),rhit.getTime(),rhit.getPosition().x(),rhit.getPosition().y(),rhit.getPosition().z());
                }
            }
        }

        if(get_nPar()>0){ 
            if(find_Hadrons()){
                if(richevent.get_nResHit()>0){
                    if(!find_Photons(richevent.get_ResHits())){
                        if(debugMode>=1)System.out.println("ATT: Found no RICH photon \n");
                    }
                }else{
                    if(debugMode>=1)System.out.println("ATT: Found no RICH hits \n");
                }
            }else{
                if(debugMode>=1)System.out.println("ATT: Found no RICH hadron \n");
            }
        }else{
            if(debugMode>=1)System.out.println("ATT: Found no CLAS particle in sector 4 \n");
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

                int charge = tbank.getByte("q", i);
                Vector3D pvec = DetectorData.readVector(pbank, ipr, "px", "py", "pz");
                Vector3D vertex = DetectorData.readVector(pbank, ipr, "vx", "vy", "vz");
                int PID = pbank.getInt("pid",ipr);

                DetectorTrack  tr = new DetectorTrack(charge,pvec.mag(),i);
                tr.setVector(pvec.x(), pvec.y(), pvec.z());
                tr.setVertex(vertex.x(), vertex.y(), vertex.z());
                tr.setSector(tbank.getByte("sector", i));

	        if(debugMode>=1){ 
                    double px = pvec.x();
                    double py = pvec.y();
                    double pz = pvec.z();
                    System.out.format(" from REC::Track %3d  det %4d   -->  tk %4d   sec %4d  theta %8.2f  -->  part %4d   pid %d \n",
                          i,idet,itk,tr.getSector(), Math.acos(pz/Math.sqrt(px*px+py*py+pz*pz))*RAD,ipr,PID);
                }

                /*
                *  disregard central detector tracks
                */
                if(idet!=6)continue;

                for (int j=0; j<rbank.rows(); j++){
                    int jpr = (int) rbank.getShort("pindex",j);
                    int jtk = (int) rbank.getShort("index",j);
                    int jdet = (int) rbank.getByte("detector",j);
                    int jlay = (int) rbank.getByte("layer",j);
                    if(debugMode>=1) System.out.format("traj %3d  part %3d  tk %3d  det %3d  lay %3d ",j,jpr,jtk,jdet,jlay);

                    /*
                    *  trajectory plane 42 till 5b.6.2 - 40 since 5c.7.0 - layer 36 since 6b.1.0
                    */
                    if (jpr==ipr && jtk==itk && jdet==DetectorType.DC.getDetectorId() && jlay==36){
                        double jx =  (double) rbank.getFloat("x",j);
                        double jy =  (double) rbank.getFloat("y",j);
                        double jz =  (double) rbank.getFloat("z",j);
                        double jcx =  (double) rbank.getFloat("cx",j);
                        double jcy =  (double) rbank.getFloat("cy",j);
                        double jcz =  (double) rbank.getFloat("cz",j);
                        Vector3d vdir = (new Vector3d(jcx, jcy, jcz)).normalized();
                        tr.addCross(jx, jy, jz, vdir.x, vdir.y, vdir.z);
                        tr.setPath(rbank.getFloat("path", j));
                        if(debugMode>=1) System.out.format(" --> %7.2f %7.2f %7.2f | %7.2f %7.2f %7.2f -> %7.2f %7.2f %7.2f \n",
                              jx,jy,jz,jcx,jcy,jcz,vdir.x,vdir.y,vdir.z);
                    }else{
                        if(debugMode>=1) System.out.format("\n");
                    }
                }
                if(tr.getCrossCount()==0){if(debugMode>=1)System.out.format("Traj not found \n"); continue;}

                if(debugMode>=1) {
                    Point3D ori = tr.getLastCross().origin();
                    Point3D dir = tr.getLastCross().end();
                    System.out.format("lastcross  %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f\n",ori.x(),ori.y(),ori.z(),dir.x(),dir.y(),dir.z());
                }

               if(tr.getSector()==4){
                    tr.setStatus(ipr);
                    DetectorParticle particle = new DetectorParticle(tr);
                    particle.setPid(PID);
                    // FIX ME! 
                    if(debugMode>=1){showTrack(tr); showParticle(particle);}
                    sector4Event.addParticle(particle);
                    if(debugMode>=1) System.out.format(" ECCOLO !!!!! %7.2f size %6d \n",tr.getLastCross().origin().x(), sector4Event.getParticles().size());
                }

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
                        if(debugMode>=1){showTrack(tr); showParticle(particle);}
                        sector4Event.addParticle(particle);
                    }
                }

            }else{

	        if(debugMode>=1)System.out.format("No track banks: Go back\n");
                return false;
            }
        }

	if(debugMode>=1)System.out.format(" SEC4 PARTICLE founds %3d \n",sector4Event.getParticles().size());
        if(sector4Event.getParticles().size()>0)return true;
        return false;
    }


    // ----------------
    public void process_Hit_Matching(){
    // ----------------

        int debugMode = 0;
        int np=richevent.get_nResClu();
        for(int n = 0; n < np; n++){
            DetectorResponse dt = richevent.get_ResClu(n);
            if(debugMode>=1)  System.out.format("Response n %4d   time %8.2f   ene %8.2f   pos %8.1f %8.1f %8.1f \n",n,dt.
                   getTime(),dt.getEnergy(),dt.getPosition().x(),dt.getPosition().y(),dt.getPosition().z());

        }

        np = sector4Event.getParticles().size();
        for(int n = 0; n < np; n++){
            DetectorParticle  p = this.sector4Event.getParticle(n);
            Line3D trajectory = p.getLastCross();
            Point3D ori = trajectory.origin();
            Point3D end = trajectory.end();
            if(debugMode>=1)  System.out.format("Particle n %4d   itr %3d    path %8.1f   ori %8.1f %8.1f %8.1f   end %8.1f %8.1f %8.1f\n",
                   n,p.getTrackIndex(),p.getPathLength(),ori.x(),ori.y(),ori.z(),end.x(),end.y(),end.z());

            // Matching tracks to RICH:
            Double rich_match_cut = tool.get_Constants().RICH_DCMATCH_CUT;
            int index = p.getDetectorHit(richevent.get_ResClus(), DetectorType.RICH, 1, rich_match_cut);
            if(index>=0){
		// while storing the match, calculates the matched position as middle point between track and hit and path (track last cross plus distance to hit)
                p.addResponse(richevent.get_ResClu(index), true);
                richevent.get_ResClu(index).setAssociation(n);
                if(debugMode>=1)  System.out.println(" --> match found "+index+" for particle "+n); 
            }

        }
    }

    // ----------------
    public void set_EventInfo(DataEvent event) {
    // ----------------

        int debugMode = 0;

        richevent.set_exeStart(System.nanoTime());
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
    }

    // ----------------
    public int getEventID() {return richevent.get_EventID();}
    // ----------------

    // ----------------
    public DetectorEvent  getEvent(){return this.sector4Event;}
    // ----------------

    // ----------------
    public int get_nPar() {
    // ----------------
        return this.sector4Event.getParticles().size();
    }

    // ----------------
    public int get_nMatch() {
    // ----------------
        return richevent.get_nMatch();
    }


    // ----------------
    public boolean find_Hadrons() {
    // ----------------

        int debugMode = 0;

        if(debugMode>=1){ 
            System.out.println("---------------------------------");
            System.out.println("Find RICH hadrons from CLAS "+get_nPar()+" particles");
            System.out.println("---------------------------------");
        }

        int hindex = 0;
        for(DetectorParticle p : sector4Event.getParticles()){

            double theta = p.vector().theta();
            int CLASpid = p.getPid();
            if(Math.abs(CLASpid)!=11 && Math.abs(CLASpid)!=211 && Math.abs(CLASpid)!=321 && Math.abs(CLASpid)!=2212){
                if(debugMode>=1)System.out.format("Good CLASpid not found %4d: assume default electron\n", CLASpid);
                CLASpid = 11;
            }
            double CLASbeta = p.getTheoryBeta(CLASpid);

            if(debugMode>=1) { 
                System.out.format(" -->  track    %4d  %4d  ori %s    P  %8.2f    the  %8.2f    time @IP %8.2f \n",p.getTrackIndex(),CLASpid,
                    tool.toString(p.getLastCross().origin()),p.vector().mag(),theta*RAD,richevent.get_EventTime());
            }

            DetectorResponse r = null; 
            int nresp = 0;
            for(DetectorResponse rtest : p.getDetectorResponses()){
                if(rtest.getDescriptor().getType()==DetectorType.RICH){
                    r = rtest;
                    nresp++;
                }
            }
            if(tool.get_Constants().DO_MIRROR_HADS==1 && nresp==0){ 
                if(debugMode>=1)System.out.format("EXTRAPOLATED \n");
                r = extrapolate_RICHResponse(p);
                if(r!=null) nresp++;
            }

            // ATT: define the response tratment in special cases
            if(nresp==0){if(debugMode>=1)System.out.format("No RICH intersection for particle %8.2f \n",theta*RAD); continue;}
            if(nresp>1){if(debugMode>=1)System.out.format("Too many RICH responses for particle \n"); continue;}

            // ATT: time taken at the RICH matching point
            double CLAStime = richevent.get_EventTime() + r.getPath()/CLASbeta/(PhysicsConstants.speedOfLight());
            //double CLAStime = richevent.get_EventTime() + p.getPathLength()/CLASbeta/(PhysicsConstants.speedOfLight());

            if(debugMode>=1) { 
                System.out.format(" -->  response  %4d   hit %s   path %7.2f  -->  clas  %8.2f  vs rich %8.2f  time\n \n",r.getHitIndex(),
                    tool.toString(r.getPosition()), r.getPath(), CLAStime, r.getTime());
            }

            RICHParticle richhadron = new RICHParticle(hindex, p.getTrackStatus(), r.getHitIndex(), p.vector().mag(), CLASpid, tool);
            richhadron.traced.set_time((float) CLAStime);
            richhadron.set_meas_time(r.getTime());
            if(!richhadron.set_points(p.getLastCross().origin(), p.getLastCross().end(), r.getPosition(), r.getStatus(), tool) ){
                 if(debugMode>=1)System.out.println(" ERROR: no MAPMT interesection found for hadron \n"); 
                 continue;
            }

            if(!richhadron.find_aerogel_points(tool) ) {
                 if(debugMode>=1)System.out.println(" ERROR: no aerogel interesection found for hadron \n"); 
                 continue;
            }

            richhadron.traced.set_path((float) richhadron.get_meas_hit().distance(richhadron.aero_middle));
            if(debugMode>=1)System.out.format(" Timing id %3d   beta %8.4f |   time eve %8.2f  emi  %8.2f   hit %8.2f  at light speed %8.2f (cm/ns) \n",
                 CLASpid,CLASbeta,richevent.get_EventTime(),richhadron.get_start_time(),CLAStime, PhysicsConstants.speedOfLight());

            if(!richhadron.set_rotated_points() ) {
                 System.out.println(" ERROR: no rotation found \n");
                 continue;
            }

	    if(debugMode>=1){
                System.out.format("  ------------------- \n");  
                System.out.format("  Hadron  %4d  id %4d  from part %4d  and clu  %4d  CLAS eve %7d  pid %5d \n ", hindex, richhadron.get_id(), 
                                     p.getTrackStatus(), r.getHitIndex(), richevent.get_EventID(), CLASpid);
                System.out.format("  ------------------- \n");  

                richhadron.show();
                Vector3d crosspos = tool.toVector3d(p.getLastCross().origin());
                Vector3d crossdir = richhadron.lab_emission;
                System.out.format(" track cross 1  xyz  %s   dir %s \n",tool.toString(crosspos), tool.toString(crossdir));
            }

            richevent.add_Hadron(richhadron);
            hindex++;
        }

        if(richevent.get_nHad()>0)return true;
        return false;

    }
                    
    // ----------------
    public boolean find_Photons(List<DetectorResponse>  RichHits){
    // ----------------

        int debugMode = 0;
        if(RichHits==null) return false;

        if(debugMode>=1){ 
            System.out.println("---------------------------------");
            System.out.println("Find RICH photons from "+RichHits.size()+" PMT hits");
            System.out.println("---------------------------------");
        }

        int hindex = 0;
        for(RICHParticle richhadron : richevent.get_Hadrons()){

            double medeta = 0.0;
            double rmseta = 0.0;
            int neta = 0;
            for(int k=0 ; k<RichHits.size(); k++) {

                int id = hindex*RichHits.size()+k;
                RICHParticle photon = new RICHParticle(id, richhadron.get_id(), RichHits.get(k).getHitIndex(), 1.e-6, 22, tool);

	        if(debugMode>=1){
                    System.out.format("  ------------------- \n");  
                    System.out.format("  Photon  %4d   id %4d   from had %4d  and hit %4d \n",k, photon.get_id(), hindex, RichHits.get(k).getHitIndex());
                    System.out.format("  ------------------- \n");  
                }

                photon.set_points(richhadron, tool.toVector3d(RichHits.get(k).getPosition()));
                photon.set_meas_time(RichHits.get(k).getTime());
                photon.set_start_time(richhadron.get_start_time());
                        
                richevent.add_Photon(photon);

            }
            hindex++;
        }

        if(richevent.get_nPho()>0)return true;
        return false;

    }

    // ----------------
    public boolean analyze_Cherenkovs() {
    // ----------------

        int debugMode = 0;

        if(tool.get_Constants().DO_ANALYTIC==1){

            richevent.analyze_Photons();

            for(RICHParticle richhadron : richevent.get_Hadrons()){
                richevent.get_pid(richhadron,0); 
            }

        }

        return true;

    }

    // ----------------
    public boolean reco_Cherenkovs() {
    // ----------------

        int debugMode = 0;

        for(RICHParticle richhadron : richevent.get_Hadrons()){

            int trials = tool.get_Constants().THROW_PHOTON_NUMBER;

            if(tool.get_Constants().THROW_ELECTRONS==1){
                double chel = richhadron.get_changle(0);
                if(chel>0)richevent.throw_Photons(richhadron, trials, chel, 5, tool);
            }

            if(tool.get_Constants().THROW_PIONS==1){
                double chpi = richhadron.get_changle(1);
                if(chpi>0)richevent.throw_Photons(richhadron, trials, chpi, 1, tool);
            }


            if(tool.get_Constants().THROW_KAONS==1){
                double chk = richhadron.get_changle(2);
                if(chk>0)richevent.throw_Photons(richhadron, trials, chk, 2, tool);
            }

            if(tool.get_Constants().THROW_PROTONS==1){
                double chpr = richhadron.get_changle(3);
                if(chpr>0)richevent.throw_Photons(richhadron, trials, chpr, 3, tool);
            }


       }

       if(tool.get_Constants().TRACE_PHOTONS==1){

            richevent.associate_Throws(tool);

            richevent.trace_Photons(tool);
            
            for(RICHParticle richhadron : richevent.get_Hadrons()){
                richevent.get_pid(richhadron,1); 
            }

        }

        return true;
    }


    // ----------------
    public DetectorResponse extrapolate_RICHResponse(DetectorParticle p){
    // ----------------

        int debugMode = 0;

        DetectorResponse r = new DetectorResponse(4, 18, 1);

        Vector3d extra = tool.find_intersection_UpperHalf_RICH( p.getLastCross());
        if(extra==null){
            extra = tool.find_intersection_MAPMT( p.getLastCross());
        }

        if(extra==null) return null;

        double dist = extra.distance( tool.toVector3d(p.getLastCross().origin()) );

        r.setPosition(extra.x, extra.y, extra.z);
        r.setPath(p.getPathLength()+dist);
        r.setStatus(0);

        int CLASpid = p.getPid();
        if(CLASpid==0)CLASpid = 211;
        double CLASbeta = p.getTheoryBeta(CLASpid);
        r.setTime( richevent.get_EventTime() + r.getPath()/CLASbeta/(PhysicsConstants.speedOfLight()) );

        return r;
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
    public Point3D Outer_Intersection(Point3D first, Point3D second, Vector3d direction ) {
    // ----------------

        int debugMode = 0;
        Point3D Emissione = new Point3D(0,0,0);
        // Define a Vector3d as: Second point of intersection  Minus first point of intersection
        Vector3d V_inter = new Vector3d(second.x()-first.x(),second.y()-first.y(),second.z()-first.z());
        // See if V_int is pointing in the direction of the track. In this case the first point is the entrance and the second one is the exit
        if( V_inter.normalized().dot(direction.normalized()) >0  ) {
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
	    System.out.println(" prod "+V_inter.normalized().dot(direction.normalized()));
	    System.out.println("Emission "+Emissione);
	}
        return Emissione;
    }


    // ----------------
    public ArrayList<DetectorResponse>  getRichResponseList(){
    // ----------------
        sector4Event.setAssociation();
        ArrayList<DetectorResponse> responses = new ArrayList<DetectorResponse>();
        for(DetectorParticle p : sector4Event.getParticles()){
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
    public void showParticle(DetectorParticle pr) {
    // ----------------

	System.out.format("    Particle pid %4d   mass %8.3f   beta %8.5f   vert %8.1f %8.1f %8.1f   mom %8.3f %8.3f %8.3f \n",
                          pr.getPid(),pr.getMass(),pr.getBeta(),
			  pr.vertex().x(),pr.vertex().y(),pr.vertex().z(),
			  pr.vector().x(),pr.vector().y(),pr.vector().z());

    }


    // ----------------
    public void showTrack(DetectorTrack tr) {
    // ----------------

        Line3D trajectory = tr.getLastCross();
        Point3D ori = trajectory.origin();
	Point3D end = trajectory.end();
	System.out.format("    Track id %4d   sec %4d   path %8.1f   lastX %8.1f %8.1f %8.1f   extraX %8.1f %8.1f %8.1f \n",
			  tr.getTrackIndex(), 
			  tr.getSector(),
			  tr.getPath(),
			  ori.x(),ori.y(),ori.z(),
			  end.x(),end.y(),end.z());
    }

}
