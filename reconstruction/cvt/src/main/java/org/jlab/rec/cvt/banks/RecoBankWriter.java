package org.jlab.rec.cvt.banks;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.rec.cvt.trajectory.StateVec;

import Jama.Matrix;

public class RecoBankWriter {

    /**
     *
     * @param hitlist the list of hits that are of the type FittedHit. If the
     * hit has not been fitted, the fitted hit fields are left to their default
     * values.
     * @return hits bank
     *
     */
    public DataBank fillSVTHitsBank(DataEvent event, List<FittedHit> hitlist) {
        if (hitlist == null) {
            return null;
        }
        if (hitlist.size() == 0) {
            return null;
        }

        DataBank bank
                = event.createBank("BSTRec::Hits", hitlist.size());

        for (int i = 0; i < hitlist.size(); i++) {

            bank.setShort("ID", i, (short) hitlist.get(i).get_Id());

            bank.setByte("layer", i, (byte) hitlist.get(i).get_Layer());
            bank.setByte("sector", i, (byte) hitlist.get(i).get_Sector());
            bank.setInt("strip", i, hitlist.get(i).get_Strip().get_Strip());

            bank.setFloat("fitResidual", i, (float) hitlist.get(i).get_Residual());
            bank.setInt("trkingStat", i, hitlist.get(i).get_TrkgStatus());

            bank.setShort("clusterID", i, (short) hitlist.get(i).get_AssociatedClusterID());
            bank.setShort("trkID", i, (short) hitlist.get(i).get_AssociatedTrackID());

        }
        //bank.show();
        return bank;

    }

    /**
     *
     * @param cluslist the reconstructed list of fitted clusters in the event
     * @return clusters bank
     */
    public DataBank fillSVTClustersBank(DataEvent event, List<Cluster> cluslist) {
        if (cluslist == null) {
            return null;
        }
        if (cluslist.size() == 0) {
            return null;
        }

        DataBank bank = event.createBank("BSTRec::Clusters", cluslist.size());
        int[] hitIdxArray = new int[5];

        for (int i = 0; i < cluslist.size(); i++) {
            for (int j = 0; j < hitIdxArray.length; j++) {
                hitIdxArray[j] = -1;
            }
            bank.setShort("ID", i, (short) cluslist.get(i).get_Id());
            bank.setByte("sector", i, (byte) cluslist.get(i).get_Sector());
            bank.setByte("layer", i, (byte) cluslist.get(i).get_Layer());
            bank.setShort("size", i, (short) cluslist.get(i).size());
            bank.setFloat("ETot", i, (float) cluslist.get(i).get_TotalEnergy());
            bank.setInt("seedStrip", i, cluslist.get(i).get_SeedStrip());
            bank.setFloat("centroid", i, (float) cluslist.get(i).get_Centroid());
            bank.setFloat("seedE", i, (float) cluslist.get(i).get_SeedEnergy());
            bank.setFloat("centroidResidual", i, (float) cluslist.get(i).get_CentroidResidual());
            bank.setFloat("seedResidual", i, (float) cluslist.get(i).get_SeedResidual()); 
            bank.setShort("trkID", i, (short) cluslist.get(i).get_AssociatedTrackID());

            for (int j = 0; j < cluslist.get(i).size(); j++) {
                if (j < hitIdxArray.length) {
                    hitIdxArray[j] = cluslist.get(i).get(j).get_Id();
                }
            }

            for (int j = 0; j < hitIdxArray.length; j++) {
                String hitStrg = "Hit";
                hitStrg += (j + 1);
                hitStrg += "_ID";
                bank.setShort(hitStrg, i, (short) hitIdxArray[j]);
            }

        }
        //bank.show();
        return bank;

    }

    /**
     *
     * @param crosses the reconstructed list of crosses in the event
     * @return crosses bank
     */
    public DataBank fillSVTCrossesBank(DataEvent event, List<ArrayList<Cross>> crosses, double zShift) {
        if (crosses == null) {
            return null;
        }
        if (crosses.get(0).size() == 0) {
            return null;
        }

        DataBank bank = event.createBank("BSTRec::Crosses", crosses.get(0).size());

        int index = 0;
        int i = 0;
        for (int j = 0; j < crosses.get(i).size(); j++) {
            bank.setShort("ID", index, (short) crosses.get(i).get(j).get_Id());
            bank.setByte("sector", index, (byte) crosses.get(i).get(j).get_Sector());
            bank.setByte("region", index, (byte) crosses.get(i).get(j).get_Region());
            bank.setFloat("x", index, (float) (crosses.get(i).get(j).get_Point().x()/10.));
            bank.setFloat("y", index, (float) (crosses.get(i).get(j).get_Point().y()/10.));
            bank.setFloat("z", index, (float) (crosses.get(i).get(j).get_Point().z()/10.+zShift));
            bank.setFloat("err_x", index, (float) (crosses.get(i).get(j).get_PointErr().x()/10.));
            bank.setFloat("err_y", index, (float) (crosses.get(i).get(j).get_PointErr().y()/10.));
            bank.setFloat("err_z", index, (float) (crosses.get(i).get(j).get_PointErr().z()/10.));
            bank.setShort("trkID", index, (short) crosses.get(i).get(j).get_AssociatedTrackID());

            if (crosses.get(i).get(j).get_Dir() != null && 
                    !Double.isNaN(crosses.get(i).get(j).get_Dir().x()) &&
                    !Double.isNaN(crosses.get(i).get(j).get_Dir().y()) &&
                    !Double.isNaN(crosses.get(i).get(j).get_Dir().z()) ) {
                bank.setFloat("ux", index, (float) crosses.get(i).get(j).get_Dir().x());
                bank.setFloat("uy", index, (float) crosses.get(i).get(j).get_Dir().y());
                bank.setFloat("uz", index, (float) crosses.get(i).get(j).get_Dir().z());
            } else {
                bank.setFloat("ux", index, 0);
                bank.setFloat("uy", index, 0);
                bank.setFloat("uz", index, 0);
            }
            if (crosses.get(i).get(j).get_Cluster1() != null) {
                bank.setShort("Cluster1_ID", index, (short) crosses.get(i).get(j).get_Cluster1().get_Id());
            }
            if (crosses.get(i).get(j).get_Cluster2() != null) {
                bank.setShort("Cluster2_ID", index, (short) crosses.get(i).get(j).get_Cluster2().get_Id());
            }
            index++;
        }

        //bank.show();
        return bank;

    }

    public DataBank fillBMTHitsBank(DataEvent event, List<FittedHit> hitlist) {
        if (hitlist == null) {
            return null;
        }
        if (hitlist.size() == 0) {
            return null;
        }

        DataBank bank
                = event.createBank("BMTRec::Hits", hitlist.size());

        for (int i = 0; i < hitlist.size(); i++) {

            bank.setShort("ID", i, (short) hitlist.get(i).get_Id());

            bank.setByte("layer", i, (byte) hitlist.get(i).get_Layer());
            bank.setByte("sector", i, (byte) hitlist.get(i).get_Sector());
            bank.setInt("strip", i, hitlist.get(i).get_Strip().get_Strip());

            bank.setFloat("fitResidual", i, (float) hitlist.get(i).get_Residual());
            bank.setInt("trkingStat", i, hitlist.get(i).get_TrkgStatus());

            bank.setShort("clusterID", i, (short) hitlist.get(i).get_AssociatedClusterID());
            bank.setShort("trkID", i, (short) hitlist.get(i).get_AssociatedTrackID());

        }

        return bank;

    }

    /**
     *
     * @param cluslist the reconstructed list of fitted clusters in the event
     * @return clusters bank
     */
    public DataBank fillBMTClustersBank(DataEvent event, List<Cluster> cluslist) {
        if (cluslist == null) {
            return null;
        }
        if (cluslist.size() == 0) {
            return null;
        }

        DataBank bank = event.createBank("BMTRec::Clusters", cluslist.size());
        int[] hitIdxArray = new int[5];

        for (int i = 0; i < cluslist.size(); i++) {
            for (int j = 0; j < hitIdxArray.length; j++) {
                hitIdxArray[j] = -1;
            }
            bank.setShort("ID", i, (short) cluslist.get(i).get_Id());
            bank.setByte("sector", i, (byte) cluslist.get(i).get_Sector());
            bank.setByte("layer", i, (byte) cluslist.get(i).get_Layer());
            bank.setShort("size", i, (short) cluslist.get(i).size());
            bank.setFloat("ETot", i, (float) cluslist.get(i).get_TotalEnergy());
            bank.setInt("seedStrip", i, cluslist.get(i).get_SeedStrip());
            bank.setFloat("centroid", i, (float) cluslist.get(i).get_Centroid());
            bank.setFloat("centroidResidual", i, (float) cluslist.get(i).get_CentroidResidual());
            bank.setFloat("seedResidual", i, (float) cluslist.get(i).get_SeedResidual()); 
            bank.setFloat("seedE", i, (float) cluslist.get(i).get_SeedEnergy());
            bank.setShort("trkID", i, (short) cluslist.get(i).get_AssociatedTrackID());
            for (int j = 0; j < cluslist.get(i).size(); j++) {
                if (j < hitIdxArray.length) {
                    hitIdxArray[j] = cluslist.get(i).get(j).get_Id();
                }
            }

            for (int j = 0; j < hitIdxArray.length; j++) {
                String hitStrg = "Hit";
                hitStrg += (j + 1);
                hitStrg += "_ID";
                bank.setShort(hitStrg, i, (short) hitIdxArray[j]);
            }

        }

        return bank;

    }

    /**
     *
     * @param crosses the reconstructed list of crosses in the event
     * @return crosses bank
     */
    public DataBank fillBMTCrossesBank(DataEvent event, List<ArrayList<Cross>> crosses, double zShift) {
        if (crosses == null) {
            return null;
        }
        if (crosses.get(1).size() == 0) {
            return null;
        }

        DataBank bank = event.createBank("BMTRec::Crosses", crosses.get(1).size());

        int index = 0;
        int i = 1;
        for (int j = 0; j < crosses.get(i).size(); j++) {
            bank.setShort("ID", index, (short) crosses.get(i).get(j).get_Id());
            bank.setByte("sector", index, (byte) crosses.get(i).get(j).get_Sector());
            bank.setByte("region", index, (byte) crosses.get(i).get(j).get_Region());
            bank.setFloat("x", index, (float) (crosses.get(i).get(j).get_Point().x()/10.));
            bank.setFloat("y", index, (float) (crosses.get(i).get(j).get_Point().y()/10.));
            bank.setFloat("z", index, (float) (crosses.get(i).get(j).get_Point().z()/10.+zShift));
            bank.setFloat("err_x", index, (float) (crosses.get(i).get(j).get_PointErr().x()/10.));
            bank.setFloat("err_y", index, (float) (crosses.get(i).get(j).get_PointErr().y()/10.));
            bank.setFloat("err_z", index, (float) (crosses.get(i).get(j).get_PointErr().z()/10.));
            bank.setShort("trkID", index, (short) crosses.get(i).get(j).get_AssociatedTrackID());
           
            if (crosses.get(i).get(j).get_Dir() != null && 
                    !Double.isNaN(crosses.get(i).get(j).get_Dir().x()) &&
                    !Double.isNaN(crosses.get(i).get(j).get_Dir().y()) &&
                    !Double.isNaN(crosses.get(i).get(j).get_Dir().z()) ) {
                bank.setFloat("ux", index, (float) crosses.get(i).get(j).get_Dir().x());
                bank.setFloat("uy", index, (float) crosses.get(i).get(j).get_Dir().y());
                bank.setFloat("uz", index, (float) crosses.get(i).get(j).get_Dir().z());
            } else {
                bank.setFloat("ux", index, 0);
                bank.setFloat("uy", index, 0);
                bank.setFloat("uz", index, 0);
            }
            if (crosses.get(i).get(j).get_Cluster1() != null) {
                bank.setShort("Cluster1_ID", index, (short) crosses.get(i).get(j).get_Cluster1().get_Id());
            }
            if (crosses.get(i).get(j).get_Cluster2() != null) {
                bank.setShort("Cluster2_ID", index, (short) crosses.get(i).get(j).get_Cluster2().get_Id());
            }
            index++;
        }

        return bank;

    }

    /**
     *
     * @param event the event
     * @param trkcands the list of reconstructed helical tracks
     * @return track bank
     */
    public DataBank fillTracksBank(DataEvent event, List<Track> trkcands, double zShift) {
        if (trkcands == null) {
            return null;
        }
        if (trkcands.size() == 0) {
            return null;
        }

        DataBank bank = event.createBank("CVTRec::Tracks", trkcands.size());
        // an array representing the ids of the crosses that belong to the track: for a helical track with the current
        // 4 regions of SVT + 1 region of BMT there can be up to 4 crosses of type SVT and 2 of type BMT (1 for the C detector and 1 for the Z detector)
        List<Integer> crossIdxArray = new ArrayList<Integer>();

        for (int i = 0; i < trkcands.size(); i++) {
            if(trkcands.get(i)==null)
                continue;
            if(trkcands.get(i).getChi2()!=0) {
                bank.setByte("fittingMethod", i, (byte) 2);
            } else {
                bank.setByte("fittingMethod", i, (byte) 0);
            }
            bank.setShort("ID", i, (short) trkcands.get(i).get_Id());
            bank.setByte("q", i, (byte)trkcands.get(i).get_Q());
            bank.setFloat("p", i, (float) trkcands.get(i).get_P());
            bank.setFloat("pt", i, (float) trkcands.get(i).get_Pt());
            Helix helix = trkcands.get(i).get_helix();
            bank.setFloat("phi0", i, (float) helix.get_phi_at_dca());
            bank.setFloat("tandip", i, (float) helix.get_tandip());
            bank.setFloat("z0", i, (float) (helix.get_Z0()/10.+zShift));
            bank.setFloat("d0", i, (float) (helix.get_dca()/10.));
            bank.setFloat("xb", i, (float) (org.jlab.rec.cvt.Constants.getXb()/10.0));
            bank.setFloat("yb", i, (float) (org.jlab.rec.cvt.Constants.getYb()/10.0));
            // this is the format of the covariance matrix for helical tracks
            // cov matrix = 
            // | d_dca*d_dca                   d_dca*d_phi_at_dca            d_dca*d_curvature        0            0             |
            // | d_phi_at_dca*d_dca     d_phi_at_dca*d_phi_at_dca     d_phi_at_dca*d_curvature        0            0             |
            // | d_curvature*d_dca	    d_curvature*d_phi_at_dca      d_curvature*d_curvature         0            0             |
            // | 0                              0                             0                    d_Z0*d_Z0                     |
            // | 0                              0                             0                       0        d_tandip*d_tandip |X
            Matrix covmatrix = helix.get_covmatrix();
            if (covmatrix != null) {
                bank.setFloat("cov_d02", i, (float) covmatrix.get(0, 0));
                bank.setFloat("cov_d0phi0", i, (float) covmatrix.get(0, 1));
                bank.setFloat("cov_d0rho", i, (float) covmatrix.get(0, 2));
                bank.setFloat("cov_phi02", i, (float) covmatrix.get(1, 1));
                bank.setFloat("cov_phi0rho", i, (float) covmatrix.get(1, 2));
                bank.setFloat("cov_rho2", i, (float) covmatrix.get(2, 2));
                bank.setFloat("cov_z02", i, (float) covmatrix.get(3, 3));
                bank.setFloat("cov_tandip2", i, (float) covmatrix.get(4, 4));
            } else {
                bank.setFloat("cov_d02", i, -999);
                bank.setFloat("cov_d0phi0", i, -999);
                bank.setFloat("cov_d0rho", i, -999);
                bank.setFloat("cov_phi02", i, -999);
                bank.setFloat("cov_phi0rho", i, -999);
                bank.setFloat("cov_rho2", i, -999);
                bank.setFloat("cov_z02", i, -999);
                bank.setFloat("cov_tandip2", i, -999);
            }
            bank.setFloat("c_x", i, (float) (trkcands.get(i).get_TrackPointAtCTOFRadius().x() / 10.)); // convert to cm
            bank.setFloat("c_y", i, (float) (trkcands.get(i).get_TrackPointAtCTOFRadius().y() / 10.)); // convert to cm
            bank.setFloat("c_z", i, (float) (trkcands.get(i).get_TrackPointAtCTOFRadius().z() / 10. + zShift)); // convert to cm
            bank.setFloat("c_ux", i, (float) trkcands.get(i).get_TrackDirAtCTOFRadius().x());
            bank.setFloat("c_uy", i, (float) trkcands.get(i).get_TrackDirAtCTOFRadius().y());
            bank.setFloat("c_uz", i, (float) trkcands.get(i).get_TrackDirAtCTOFRadius().z());
            bank.setFloat("pathlength", i, (float) (trkcands.get(i).get_pathLength() / 10.)); // conversion to cm

            //for status word:
            int a = 0;
            int b = 0;
            int c = 0;
            // fills the list of cross ids for crosses belonging to that reconstructed track
            for (int j = 0; j < trkcands.get(i).size(); j++) {
                if(j<9) {
                String hitStrg = "Cross";
                hitStrg += (j + 1);
                hitStrg += "_ID";  //System.out.println(" j "+j+" matched id "+trkcands.get(i).get(j).get_Id());
                bank.setShort(hitStrg, i, (short) trkcands.get(i).get(j).get_Id());
                }
                // counter to get status word    
                if(trkcands.get(i).get(j).get_Detector().equalsIgnoreCase("SVT"))
                    a++;
                if(trkcands.get(i).get(j).get_Detector().equalsIgnoreCase("BMT") 
                    && trkcands.get(i).get(j).get_DetectorType().equalsIgnoreCase("Z"))
                    b++;
                if(trkcands.get(i).get(j).get_Detector().equalsIgnoreCase("BMT") 
                    && trkcands.get(i).get(j).get_DetectorType().equalsIgnoreCase("C"))
                    c++;
            }
            bank.setShort("status", i, (short) ((short) 1000+a*100+b*10+c));
            bank.setFloat("circlefit_chi2_per_ndf", i, (float) trkcands.get(i).get_circleFitChi2PerNDF());
            bank.setFloat("linefit_chi2_per_ndf", i, (float) trkcands.get(i).get_lineFitChi2PerNDF());
            bank.setFloat("chi2", i, (float) trkcands.get(i).getChi2());
            bank.setShort("ndf", i, (short) trkcands.get(i).getNDF());

        }
        //bank.show();
        return bank;

    }

    /**
     *
     * @param event the event
     * @param trkcands the list of reconstructed straight tracks
     * @return cosmic bank
     */
    public DataBank fillStraightTracksBank(DataEvent event,
            List<StraightTrack> cosmics, double zShift) {
        if (cosmics == null) {
            return null;
        }
        if (cosmics.size() == 0) {
            return null;
        }

        DataBank bank = event.createBank("CVTRec::Cosmics", cosmics.size());
        // an array representing the ids of the crosses that belong to the track: for a helical track with the current
        // 4 regions of SVT + 1 region of BMT there can be up to 4*2 (*2: for each hemisphere) crosses of type SVT and 2*2 of type PSEUDOBMT (1 for the C detector and 1 for the Z detector)

        for (int i = 0; i < cosmics.size(); i++) {
            List<Integer> crossIdxArray = new ArrayList<Integer>();


            bank.setShort("ID", i, (short) cosmics.get(i).get_Id());
            bank.setFloat("chi2", i, (float) cosmics.get(i).get_chi2());
            bank.setShort("ndf", i, (short) cosmics.get(i).get_ndf());
            bank.setFloat("trkline_yx_slope", i, (float) cosmics.get(i).get_ray().get_yxslope());
            bank.setFloat("trkline_yx_interc", i, (float) (cosmics.get(i).get_ray().get_yxinterc()/10.));
            bank.setFloat("trkline_yz_slope", i, (float) cosmics.get(i).get_ray().get_yzslope());
            bank.setFloat("trkline_yz_interc", i, (float) (cosmics.get(i).get_ray().get_yzinterc()/10.+zShift));

            // get the cosmics ray unit direction vector
            Vector3D u = new Vector3D(cosmics.get(i).get_ray().get_yxslope(), 1, cosmics.get(i).get_ray().get_yzslope()).asUnit();
            // calculate the theta and phi components of the ray direction vector in degrees
            bank.setFloat("theta", i, (float) Math.toDegrees(u.theta()));
            bank.setFloat("phi", i, (float) Math.toDegrees(u.phi()));

            // the array of cross ids is filled in order of the SVT cosmic region 1 to 8 starting from the bottom-most double layer
            for (int j = 0; j < cosmics.get(i).size(); j++) {
                crossIdxArray.add(cosmics.get(i).get(j).get_Id());
                
            }
            

            for (int j = 0; j < crossIdxArray.size(); j++) { 
                if(j<18) {
                    //only 18 entries in bank
                    String hitStrg = "Cross";
                    hitStrg += (j + 1);
                    hitStrg += "_ID";
                    bank.setShort(hitStrg, i, (short) crossIdxArray.get(j).shortValue());
                }
            }
        }
        //bank.show();
        return bank;
    }

    public DataBank fillStraightTracksTrajectoryBank(DataEvent event,
            List<StraightTrack> trks, double zShift) {
        if (trks == null) {
            return null;
        }
        if (trks.size() == 0) {
            return null;
        }

        int k = 0;
        for (int i = 0; i < trks.size(); i++) {
            if (trks.get(i).get_Trajectory() == null) {
                continue;
            }
            if (trks.get(i).get_Trajectory() != null) {
                k += trks.get(i).get_Trajectory().size();
            }

        }
        DataBank bank = event.createBank("CVTRec::Trajectory", k);

        k = 0;
        for (int i = 0; i < trks.size(); i++) {
            if (trks.get(i).get_Trajectory() == null) {
                continue;
            }
            for (StateVec stVec : trks.get(i).get_Trajectory()) {

                bank.setShort("id",       k, (short) trks.get(i).get_Id());
                bank.setByte("detector",  k, (byte) stVec.get_SurfaceDetector());
                bank.setByte("sector",    k, (byte) stVec.get_SurfaceSector());
                bank.setByte("layer",     k, (byte) stVec.get_SurfaceLayer());
                bank.setFloat("x",        k, (float) (stVec.x()/10.));
                bank.setFloat("y",        k, (float) (stVec.y()/10.));
                bank.setFloat("z",        k, (float) (stVec.z()/10. + zShift));
                bank.setFloat("phi",      k, (float) stVec.get_TrkPhiAtSurface());
                bank.setFloat("theta",    k, (float) stVec.get_TrkThetaAtSurface());
                bank.setFloat("langle",   k, (float) stVec.get_TrkToModuleAngle());
                bank.setFloat("centroid", k, (float) stVec.get_CalcCentroidStrip());
                bank.setFloat("path",     k, (float) stVec.get_Path()/10);
                k++;

            }
        }
        //bank.show();
        return bank;
    }

    public DataBank fillHelicalTracksTrajectoryBank(DataEvent event,
            List<Track> trks, double zShift) {
        if (trks == null) {
            return null;
        }
        if (trks.size() == 0) {
            return null;
        }
        int bankSize = 0;
        for (int i = 0; i < trks.size(); i++) {
            if(trks.get(i)==null)
                continue;
            if (trks.get(i).get_Trajectory() == null) {
                continue;
            }
            for (StateVec stVec : trks.get(i).get_Trajectory())
                bankSize++;
        }

        if(bankSize==0) return null;
        
        DataBank bank = event.createBank("CVTRec::Trajectory", bankSize); //  SVT layers +  BMT layers 

        int k = 0;
        for (int i = 0; i < trks.size(); i++) {
             if(trks.get(i)==null)
                continue;
            if (trks.get(i).get_Trajectory() == null) {
                continue;
            }
            for (StateVec stVec : trks.get(i).get_Trajectory()) {

                bank.setShort("id",       k, (short) trks.get(i).get_Id());
                bank.setByte("detector",  k, (byte) stVec.get_SurfaceDetector());
                bank.setByte("sector",    k, (byte) stVec.get_SurfaceSector());
                bank.setByte("layer",     k, (byte) stVec.get_SurfaceLayer());
                bank.setFloat("x",        k, (float) (stVec.x()/10.));
                bank.setFloat("y",        k, (float) (stVec.y()/10.));
                bank.setFloat("z",        k, (float) (stVec.z()/10. + zShift));
                bank.setFloat("phi",      k, (float) stVec.get_TrkPhiAtSurface());
                bank.setFloat("theta",    k, (float) stVec.get_TrkThetaAtSurface());
                bank.setFloat("langle",   k, (float) stVec.get_TrkToModuleAngle());
                bank.setFloat("centroid", k, (float) stVec.get_CalcCentroidStrip());
                bank.setFloat("path",     k, (float) stVec.get_Path()/10);
                k++;

            }
        }

        return bank;
    }

    public void appendCVTBanks(DataEvent event,
            List<FittedHit> sVThits, List<FittedHit> bMThits,
            List<Cluster> sVTclusters, List<Cluster> bMTclusters,
            List<ArrayList<Cross>> crosses, List<Track> trks, double zShift) {
        List<DataBank> svtbanks = new ArrayList<DataBank>();
        List<DataBank> bmtbanks = new ArrayList<DataBank>();
        List<DataBank> cvtbanks = new ArrayList<DataBank>();

        DataBank bank1 = this.fillSVTHitsBank(event, sVThits);
        if (bank1 != null) {
            svtbanks.add(bank1);
        }

        DataBank bank2 = this.fillBMTHitsBank(event, bMThits);
        if (bank2 != null) {
            bmtbanks.add(bank2);
        }

        DataBank bank3 = this.fillSVTClustersBank(event, sVTclusters);
        if (bank3 != null) {
            svtbanks.add(bank3);
        }

        DataBank bank4 = this.fillBMTClustersBank(event, bMTclusters);
        if (bank4 != null) {
            bmtbanks.add(bank4);
        }

        DataBank bank5 = this.fillSVTCrossesBank(event, crosses, zShift);
        if (bank5 != null) {
            svtbanks.add(bank5);
        }

        DataBank bank6 = this.fillBMTCrossesBank(event, crosses, zShift);
        if (bank6 != null) {
            bmtbanks.add(bank6);
        }

        //found tracks
        DataBank bank7 = this.fillTracksBank(event, trks, zShift);
        if (bank7 != null) {
            cvtbanks.add(bank7);
        }

        //found trajectories
        DataBank bank8 = this.fillHelicalTracksTrajectoryBank(event, trks, zShift);
        if (bank8 != null) {
            cvtbanks.add(bank8);
        }

        if (svtbanks.size() == 3) {
            event.appendBanks(svtbanks.get(0), svtbanks.get(1), svtbanks.get(2));
        }
        if (svtbanks.size() == 2) {
            event.appendBanks(svtbanks.get(0), svtbanks.get(1));
        }
        if (svtbanks.size() == 1) {
            event.appendBanks(svtbanks.get(0));
        }
        if (bmtbanks.size() == 3) {
            event.appendBanks(bmtbanks.get(0), bmtbanks.get(1), bmtbanks.get(2));
        }
        if (bmtbanks.size() == 2) {
            event.appendBanks(bmtbanks.get(0), bmtbanks.get(1));
        }
        if (bmtbanks.size() == 1) {
            event.appendBanks(bmtbanks.get(0));
        }
        if (cvtbanks.size() == 2) {
            event.appendBanks(cvtbanks.get(0), cvtbanks.get(1));
        }
        if (cvtbanks.size() == 1) {
            event.appendBanks(cvtbanks.get(0));
        }

        //event.show();
    }

    public void appendCVTCosmicsBanks(DataEvent event,
            List<FittedHit> sVThits, List<FittedHit> bMThits,
            List<Cluster> sVTclusters, List<Cluster> bMTclusters,
            List<ArrayList<Cross>> crosses, List<StraightTrack> trks, double zShift) {
        List<DataBank> svtbanks = new ArrayList<DataBank>();
        List<DataBank> bmtbanks = new ArrayList<DataBank>();
        List<DataBank> cvtbanks = new ArrayList<DataBank>();

        DataBank bank1 = this.fillSVTHitsBank(event, sVThits);
        if (bank1 != null) {
            svtbanks.add(bank1);
        }

        DataBank bank2 = this.fillBMTHitsBank(event, bMThits);
        if (bank2 != null) {
            bmtbanks.add(bank2);
        }

        DataBank bank3 = this.fillSVTClustersBank(event, sVTclusters);
        if (bank3 != null) {
            svtbanks.add(bank3);
        }

        DataBank bank4 = this.fillBMTClustersBank(event, bMTclusters);
        if (bank4 != null) {
            bmtbanks.add(bank4);
        }

        DataBank bank5 = this.fillSVTCrossesBank(event, crosses, zShift);
        if (bank5 != null) {
            svtbanks.add(bank5);
        }

        DataBank bank6 = this.fillBMTCrossesBank(event, crosses, zShift);
        if (bank6 != null) {
            bmtbanks.add(bank6);
        }

        //found tracks
        DataBank bank7 = this.fillStraightTracksBank(event, trks, zShift);
        if (bank7 != null) {
            cvtbanks.add(bank7);
        }

        //found trajectories
        DataBank bank8 = this.fillStraightTracksTrajectoryBank(event, trks, zShift);
        if (bank8 != null) {
            cvtbanks.add(bank8);
        }

        if (svtbanks.size() == 3) {
            event.appendBanks(svtbanks.get(0), svtbanks.get(1), svtbanks.get(2));
        }

        if (svtbanks.size() == 2) {
            event.appendBanks(svtbanks.get(0), svtbanks.get(1));
        }
        if (svtbanks.size() == 1) {
            event.appendBanks(svtbanks.get(0));
        }
        if (bmtbanks.size() == 3) {
            event.appendBanks(bmtbanks.get(0), bmtbanks.get(1), bmtbanks.get(2));
        }
        if (bmtbanks.size() == 2) {
            event.appendBanks(bmtbanks.get(0), bmtbanks.get(1));
        }
        if (bmtbanks.size() == 1) {
            event.appendBanks(bmtbanks.get(0));
        }
        if (cvtbanks.size() == 2) {
            event.appendBanks(cvtbanks.get(0), cvtbanks.get(1));
        }
        if (cvtbanks.size() == 1) {
            event.appendBanks(cvtbanks.get(0));
        }

    }

}
