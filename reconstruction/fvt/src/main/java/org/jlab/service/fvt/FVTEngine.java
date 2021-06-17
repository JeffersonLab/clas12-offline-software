package org.jlab.service.fvt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.geom.prim.Point3D;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.io.base.*;

import org.jlab.rec.fmt.Constants;
import org.jlab.rec.fmt.banks.HitReader;
import org.jlab.rec.fmt.banks.RecoBankWriter;
import org.jlab.rec.fmt.cluster.Cluster;
import org.jlab.rec.fmt.cluster.ClusterFinder;
import org.jlab.rec.fmt.cross.Cross;
import org.jlab.rec.fmt.cross.CrossMaker;
import org.jlab.rec.fmt.hit.FittedHit;
import org.jlab.rec.fmt.hit.Hit;
import org.jlab.rec.fmt.CCDBConstantsLoader;
import org.jlab.rec.fvt.track.Track;
import org.jlab.rec.fvt.track.TrackList;
import org.jlab.rec.fvt.track.fit.KFitter;

/**
 * Service to return reconstructed track candidates - the output is in hipo format
 *
 * @author ziegler
 */
public class FVTEngine extends ReconstructionEngine {

    org.jlab.rec.fmt.Geometry FVTGeom;
    String FieldsConfig = "";
    private int Run = -1;
    CrossMaker crossMake;
    TrackList trkLister;

    public FVTEngine() {
        super("FVT", "ziegler", "4.0");
    }

    @Override
    public boolean init() {
        FVTGeom = new org.jlab.rec.fmt.Geometry();
        // Get the constants for the correct variation
        String geomDBVar = this.getEngineConfigString("variation");
        if (geomDBVar!=null) {
            System.out.println("["+this.getName()+"] run with FMT geometry variation based on yaml = "+geomDBVar);
        }
        else {
            geomDBVar = System.getenv("COAT_FMT_GEOMETRYVARIATION");
            if (geomDBVar!=null) {
                System.out.println("["+this.getName()+"] run with FMT geometry variation chosen based on env = "+geomDBVar);
            }
        }
        if (geomDBVar == null) {
            System.out.println("["+this.getName()+"] run with FMT default geometry");
        }
        this.setRun(10);
        String geoVariation = Optional.ofNullable(geomDBVar).orElse("default");

        String[] tables = new String[]{
            "/geometry/beam/position",
            "/geometry/fmt/alignment"
        };
        requireConstants(Arrays.asList(tables));
        this.getConstantsManager().setVariation(geoVariation);

        // Load the geometry
        double[][] shiftsArray =
                CCDBConstantsLoader.loadAlignmentTable(this.getRun(), this.getConstantsManager());

        CCDBConstantsLoader.Load(this.getRun(), geoVariation);
        Constants.saveAlignmentTable(shiftsArray);
        Constants.applyZShifts();
        Constants.Load();
        Constants.applyXYShifts();

        crossMake  = new CrossMaker();
        trkLister  = new TrackList();

        return true;
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

    @Override
    public boolean processDataEvent(DataEvent event) {
        // Initial setup
        ClusterFinder clusFinder = new ClusterFinder();
        List<Cluster> clusters = new ArrayList<Cluster>();
        List<Cross> crosses = new ArrayList<Cross>();
        List<Track> dcTracks = null;
        KFitter kf;

        // Set run number.
        DataBank runcfgbank = event.getBank("RUN::config");
        if (runcfgbank == null || runcfgbank.rows() == 0) return true;
        this.setRun(runcfgbank.getInt("run", 0));

        // Get field.
        this.FieldsConfig = this.getFieldsConfig();
        Swim swimmer = new Swim();
        RecoBankWriter rbc = new RecoBankWriter();

        // Get beam shift.
        IndexedTable beamOffset = this.getConstantsManager().getConstants(this.getRun(), "/geometry/beam/position");
        double xB = beamOffset.getDoubleValue("x_offset", 0,0,0)/10.;
        double yB = beamOffset.getDoubleValue("y_offset", 0,0,0)/10.;

        // === HITS ================================================================================
        HitReader hitRead = new HitReader();
        hitRead.fetch_FMTHits(event);
        List<Hit> hits = hitRead.get_FMTHits();
        if (hits.size() == 0) return true;

        // === CLUSTERS ============================================================================
        clusters = clusFinder.findClusters(hits);
        if (clusters.size() == 0) return true;

        // === FITTED HITS =========================================================================
        List<FittedHit> FMThits =  new ArrayList<FittedHit>();
        for (int i = 0; i < clusters.size(); i++) FMThits.addAll(clusters.get(i));

        // === DC TRACKS ===========================================================================
        dcTracks = trkLister.getDCTracks(event);
        if (dcTracks == null) {
            rbc.appendFMTBanks(event, FMThits, clusters, null, null);
            return true;
        }

        // === CROSSES =============================================================================
        Map<Integer, List<Cross>> trj = new HashMap<Integer, List<Cross>>();
        for (int j = 0; j < clusters.size(); j++) {
            for (int i = 0; i < dcTracks.size(); i++) {
                List<Point3D> plist = dcTracks.get(i).getTraj();
                if (plist == null) continue;

                for (int k = 0; k < plist.size(); k++) {
                    if (clusters.get(j).get_Layer() != k+1) continue; // match the layers from traj
                    double x = plist.get(k).x();
                    double y = plist.get(k).y();
                    double z = plist.get(k).z();
                    double d = clusters.get(j).calcDoca(x, y, z);
                    if (d < Constants.CIRCLECONFUSION) {
                        Cross this_cross = new Cross(clusters.get(j).get_Sector(),
                                clusters.get(j).get_Layer(), crosses.size()+1);
                        this_cross.set_Point(clusters.get(j).calcCross(x, y, z));
                        this_cross.set_AssociatedTrackID(dcTracks.get(i).getId());
                        this_cross.set_Cluster1(clusters.get(j));
                        this_cross.get_Cluster1().set_AssociatedTrackID(dcTracks.get(i).getId());
                        crosses.add(this_cross);
                        // add to seed
                        if (trj.get(dcTracks.get(i).getId()) == null) {
                            trj.put(dcTracks.get(i).getId(), new ArrayList<Cross>());
                            trj.get(dcTracks.get(i).getId()).add(this_cross);
                        } else {
                            trj.get(dcTracks.get(i).getId()).add(this_cross);
                        }
                    }
                }
            }
        }

        // === TRACKS ==============================================================================
        double[] pars = new double[6];
        // iterate on hashmap to run the fit
         for (Integer id : trj.keySet()) {
            for (Track tr:dcTracks) {
                for (int p = 0; p < 6; p++)
                    pars[p] = 0;
                if (tr.getId() != id) continue;
                Map<Integer, Cluster> cls = new HashMap<Integer, Cluster>();
                List<Cross> crs = trj.get(id);
                for(Cross c : crs) {
                    // Filter clusters to use only the best cluster (minimal Tmin) per FMT layer.
                    int lyr = c.get_Cluster1().get_Layer();
                    if (cls.get(lyr) == null || c.get_Cluster1().get_Tmin() < cls.get(lyr).get_Tmin())
                        cls.put(lyr, c.get_Cluster1());
                }
                // Set status and stop if there are no measurements to fit against.
                tr.status = cls.size();
                if (cls.size() == 0) continue;

                // NOTE: KFitter expects a list, so a sneaky conversion is done here. This is not a
                //       good programming practice, and KFitter should be adapted to accept a map.
                kf = new KFitter(
                        new ArrayList<Cluster>(cls.values()), tr.getSector(), tr.getX(), tr.getY(),
                        tr.getZ(), tr.getPx(), tr.getPy(), tr.getPz(), tr.getQ(), swimmer, 0
                );

                kf.runFitter(tr.getSector());

                if (kf.finalStateVec != null) {
                    // Set the track parameters.
                    tr.setQ((int)Math.signum(kf.finalStateVec.Q));
                    tr.setChi2(kf.chi2); // TODO: I'm not sure, but we might need one last run to
                                         //       the final Chi^2.
                    pars = tr.getLabPars(kf.finalStateVec);
                    swimmer.SetSwimParameters(pars[0],pars[1],pars[2],-pars[3],-pars[4],-pars[5],
                            -tr.getQ());

                    double[] Vt = swimmer.SwimToBeamLine(xB, yB);
                    if (Vt == null) return true;

                    tr.setX(Vt[0]);
                    tr.setY(Vt[1]);
                    tr.setZ(Vt[2]);
                    tr.setPx(-Vt[3]);
                    tr.setPy(-Vt[4]);
                    tr.setPz(-Vt[5]);
                }
            }
        }
        rbc.appendFMTBanks(event, FMThits, clusters, crosses, dcTracks);

        return true;
   }
}
