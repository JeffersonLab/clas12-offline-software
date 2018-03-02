package org.jlab.service.fvt;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;

import org.jlab.io.base.*;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.fvt.banks.InputBanksReader;
import org.jlab.rec.fvt.fmt.Constants;
//import org.jlab.rec.fmt.Constants;
import org.jlab.rec.fvt.fmt.banks.HitReader;
import org.jlab.rec.fvt.fmt.banks.RecoBankWriter;
//import org.jlab.rec.fmt.CCDBConstantsLoader;
import org.jlab.rec.fvt.fmt.cluster.Cluster;
import org.jlab.rec.fvt.fmt.cluster.ClusterFinder;
import org.jlab.rec.fvt.fmt.cross.Cross;
import org.jlab.rec.fvt.fmt.cross.CrossMaker;
import org.jlab.rec.fvt.fmt.hit.FittedHit;
import org.jlab.rec.fvt.fmt.hit.Hit;
import org.jlab.rec.fvt.track.Track;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;

import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.fvt.GeometryLoader;
import org.jlab.rec.fvt.fmt.CCDBConstantsLoader;
import org.jlab.rec.fvt.track.fit.KFitter;

/**
 * Service to return reconstructed  track candidates- the output is in hipo
 * format
 *
 * @author ziegler
 *
 */
public class FMTReconstruction extends ReconstructionEngine {

    org.jlab.rec.fvt.fmt.Geometry FVTGeom;
   
    DCSwimmer dcSwim;
    public FMTReconstruction() {
        super("FMTTracks", "ziegler", "4.0");
        
        FVTGeom = new org.jlab.rec.fvt.fmt.Geometry();
        
        GeometryLoader.Load(10, "default");
        GeometryLoader gl = new GeometryLoader();
        gl.LoadSurfaces();
        CCDBConstantsLoader.Load(10);
    }

    String FieldsConfig = "";
    private int Run = -1;
  
 public void setRunConditionsParameters(DataEvent event, String FieldsConfig, int iRun, boolean addMisAlignmts, String misAlgnFile) {
        if (event.hasBank("RUN::config") == false) {
            System.err.println("RUN CONDITIONS NOT READ!");
            return;
        }

        int Run = iRun;

        boolean isMC = false;
        boolean isCosmics = false;
        DataBank bank = event.getBank("RUN::config");
        //System.out.println(bank.getInt("Event")[0]);
        if (bank.getByte("type", 0) == 0) {
            isMC = true;
        }
        if (bank.getByte("mode", 0) == 1) {
            isCosmics = true;
        }


       // Load the fields
	//-----------------
//String newConfig = "SOLENOID"+bank.getFloat("solenoid",0)+"TORUS"+bank.getFloat("torus",0)+"RUN"+bank.getInt("run", 0);		
		//System.out.println(" fields "+newConfig);
//if (FieldsConfig.equals(newConfig)==false) {			
       // DCSwimmer.setMagneticFieldsScales((double)bank.getFloat("solenoid",0), (double)bank.getFloat("torus",0)); // something changed in the configuration ... 
//}
//FieldsConfig = newConfig;

        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);

        if (Run != newRun) {
            
            double shift =0;
            if(Run>1890)
                shift = -1.9;
            DCSwimmer.setMagneticFieldsScales((double)bank.getFloat("solenoid", 0), (double)bank.getFloat("torus", 0), shift);
            
            
        }
        Run = newRun;
        this.setRun(Run);
    }

    public int getRun() {
        return Run;
    }

    public void setRun(int run) {
        Run = run;
    }

    public String getFieldsConfig() {
        return FieldsConfig;
    }

    public void setFieldsConfig(String fieldsConfig) {
        FieldsConfig = fieldsConfig;
    }
    InputBanksReader ir ;
    List<Cluster> clusters;
    CrossMaker crossMake;
    ClusterFinder clusFinder;
    List<Cross> crosses;
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        if(event.hasBank("RUN::config")==false ) {
		System.err.println("RUN CONDITIONS NOT READ!");
		return true;
	}
		
        DataBank bank = event.getBank("RUN::config");
		
        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);

        if(Run!=newRun) {
            
            double TORSCALE = (double)bank.getFloat("torus", 0);
            double SOLSCALE = (double)bank.getFloat("solenoid", 0);
            double shift =0;
            if(Run>1890)
                shift = -1.9;
            DCSwimmer.setMagneticFieldsScales(SOLSCALE, TORSCALE, shift);
            Run = newRun;
        }
        clusters.clear();
        crosses.clear();
        
        this.FieldsConfig = this.getFieldsConfig();
        this.Run = this.getRun();
        
        RecoBankWriter rbc = new RecoBankWriter();
        //I) get the hits
        HitReader hitRead = new HitReader();
        hitRead.fetch_FMTHits(event);
        List<Hit> hits = hitRead.get_FMTHits();
        
        //II) process the hits	
        //1) exit if hit list is empty
        if (hits.size() != 0) {
            //2) find the clusters from these hits
            clusters = clusFinder.findClusters(hits);
            List<FittedHit> FMThits =  new ArrayList<FittedHit>();
            if (clusters.size() != 0) {
                if (clusters.size() != 0) {
                    for (int i = 0; i < clusters.size(); i++) {
                        FMThits.addAll(clusters.get(i));
                    }
                    crosses = crossMake.findCrosses(clusters);
                }
            }
            rbc.appendFMTBanks(event, FMThits, clusters, crosses);
        }
        
        List<Track> tracks = ir.getTracks(event, clusters, dcSwim);
        KFitter kf; 
        
        if(tracks!=null && clusters.size() != 0)
            for(Track track : tracks) {
                kf = new KFitter(track, event, dcSwim);
                kf.runFitter(track);
            }

        
        if(tracks== null)
            return true;
        this.fillTrajectoryBank(event, tracks);
        //event.show();
        return true;
    }
    
    public void fillTrajectoryBank(DataEvent event, List<Track> tracks) {
        DataBank bank = event.createBank("REC::Traj", tracks.size()*19);
        int i1=0;
        for (int i = 0; i < tracks.size(); i++) {
            if(tracks.get(i)==null)
                continue;
            bank.setShort("detId", i1, (short) -1);
            bank.setShort("trkId", i1, (short) tracks.get(i).get_Id());
            bank.setByte("q", i1, (byte) tracks.get(i).getQ());
            bank.setFloat("x", i1, (float) tracks.get(i).getX());
            bank.setFloat("y", i1, (float) tracks.get(i).getY());
            bank.setFloat("z", i1, (float) tracks.get(i).getZ());
            bank.setFloat("px", i1, (float) tracks.get(i).getPx());
            bank.setFloat("py", i1, (float) tracks.get(i).getPy());
            bank.setFloat("pz", i1, (float) tracks.get(i).getPz());
            bank.setFloat("pathlength", i1, (float) 0);
            /*    System.out.println(tracks.get(i).get_Id()+" "+tracks.get(i).getQ()+" ("+(-1)+") "+
                            (float)tracks.get(i).getX()+", "+
                            (float)tracks.get(i).getY()+", "+
                            (float)tracks.get(i).getZ()+", "+
                            (float)tracks.get(i).getPx()+", "+
                            (float)tracks.get(i).getPy()+", "+
                            (float)tracks.get(i).getPz()+", "+          
                            (float)0+" "
                            ); */
            i1++;
            tracks.get(i).calcTrajectory(dcSwim);
            for(int j = 0; j< tracks.get(i).trajectory.size(); j++) {
                if(tracks.get(i).trajectory.get(j).getDetName().startsWith("DC") && (j-6)%6!=0)
                    continue;  // save the last layer in a superlayer
                bank.setShort("detId", i1, (short) tracks.get(i).trajectory.get(j).getDetId());
                bank.setShort("trkId", i1, (short) tracks.get(i).get_Id());
                bank.setByte("q", i1, (byte) tracks.get(i).getQ());
                bank.setFloat("x", i1, (float) tracks.get(i).trajectory.get(j).getX());
                bank.setFloat("y", i1, (float) tracks.get(i).trajectory.get(j).getY());
                bank.setFloat("z", i1, (float) tracks.get(i).trajectory.get(j).getZ());
                bank.setFloat("px", i1, (float) tracks.get(i).trajectory.get(j).getpX());
                bank.setFloat("py", i1, (float) tracks.get(i).trajectory.get(j).getpY());
                bank.setFloat("pz", i1, (float) tracks.get(i).trajectory.get(j).getpZ());
                bank.setFloat("pathlength", i1, (float) tracks.get(i).trajectory.get(j).getPathLen());
                i1++;
                /*     System.out.println(tracks.get(i).get_Id()+" "+tracks.get(i).getQ()
                            +" ("+tracks.get(i).trajectory.get(j).getDetId()+") "
                            + tracks.get(i).trajectory.get(j).getDetName()+"] "+
                            (float)tracks.get(i).trajectory.get(j).getX()+", "+
                            (float)tracks.get(i).trajectory.get(j).getY()+", "+
                            (float)tracks.get(i).trajectory.get(j).getZ()+", "+
                            (float)tracks.get(i).trajectory.get(j).getpX()+", "+
                            (float)tracks.get(i).trajectory.get(j).getpY()+", "+
                            (float)tracks.get(i).trajectory.get(j).getpZ()+", "+
                            (float)tracks.get(i).trajectory.get(j).getPathLen()+" "
                            ); */
            }
        }
       // bank.show();
        event.appendBank(bank);
    }
    @Override
    public boolean init() {
       DCSwimmer.getMagneticFields();
       Constants.Load();
       ir = new InputBanksReader();
       clusFinder = new ClusterFinder();
       crossMake = new CrossMaker();
       crosses = new ArrayList<Cross>();
       clusters = new ArrayList<Cluster>() ;
       dcSwim = new DCSwimmer();
       return true;
    }

     
    public static void main(String[] args) {

        String inputFile = "/Users/ziegler/Desktop/Work/CLARA/CLARA/data/input/Run2960.tb.skimGoodTrks.hipo";
        //String inputFile = "/Users/ziegler/Workdir/Distribution/CLARA/CLARA_INSTALL/data/output/out_pion_smearz_gen_1.hipo";

        System.err.println(" \n[PROCESSING FILE] : " + inputFile);
        DCHBEngine en0 = new DCHBEngine();
        en0.init();
        DCTBEngine en1 = new DCTBEngine();
        en1.init();
        FMTReconstruction en = new FMTReconstruction();
        en.init();

        int counter = 0;

        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);

        HipoDataSync writer = new HipoDataSync();
        //Writer
        //String outputFile = "/Users/ziegler/Workdir/Files/GEMC/out_pion_smearz_gen_1FMTrec.hipo";
        String outputFile = "/Users/ziegler/Desktop/Work/Files/FMTtest.hipo";
        writer.open(outputFile);

        long t1 = 0;
        while (reader.hasEvent()) {
            counter++;

            DataEvent event = reader.getNextEvent();
            if (counter > 0) {
                t1 = System.currentTimeMillis();
            }

            // Processing    
            en0.processDataEvent(event);
            en1.processDataEvent(event);
            en.processDataEvent(event);
          
            writer.writeEvent(event);
            
            System.out.println("  EVENT " + counter);
            
            /*
			event.show();
			if(event.hasBank("FMTRec::Tracks")) {
				 HipoDataEvent de = (HipoDataEvent) event;
				 HipoEvent dde = de.getHipoEvent();
				 HipoGroup group = dde.getGroup("FMTRec::Tracks");
				 dde.show();
				 dde.removeGroup("FMTRec::Tracks");
				 dde.show();
				 dde.writeGroup(group);
				 dde.show();
			}
             */
            if(counter>41) break;
            event.show();
            //if(counter%100==0)
            //System.out.println("run "+counter+" events");

        }
        writer.close();
        double t = System.currentTimeMillis() - t1;
        System.out.println(t1 + " TOTAL  PROCESSING TIME = " + (t / (float) counter));
    }

}
