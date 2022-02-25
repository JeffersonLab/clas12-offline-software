package org.jlab.rec.cvt.banks;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.rec.cvt.trajectory.StateVec;
import org.jlab.geom.prim.Line3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.track.Seed;

public class RecoBankWriter {
    boolean debug = false;
    /**
     *
     * @param event
     * @param hitlist the list of hits that are of the type FittedHit. If the
     * hit has not been fitted, the fitted hit fields are left to their default
     * values.
     * @return hits bank
     *
     */
    public DataBank fillSVTHitsBank(DataEvent event, List<Hit> hitlist) {
        if (hitlist == null) {
            return null;
        }
        if (hitlist.isEmpty()) {
            return null;
        }

        DataBank bank
                = event.createBank("BSTRec::Hits", hitlist.size());

        for (int i = 0; i < hitlist.size(); i++) {

            bank.setShort("ID", i, (short) hitlist.get(i).getId());

            bank.setByte("layer", i, (byte) hitlist.get(i).getLayer());
            bank.setByte("sector", i, (byte) hitlist.get(i).getSector());
            bank.setInt("strip", i, hitlist.get(i).getStrip().getStrip());

            bank.setFloat("energy", i, (float) hitlist.get(i).getStrip().getEdep());
            bank.setFloat("time", i, (float) hitlist.get(i).getStrip().getTime());
            bank.setFloat("fitResidual", i, (float) hitlist.get(i).getResidual()/10);
            bank.setInt("trkingStat", i, hitlist.get(i).getTrkgStatus());

            bank.setShort("clusterID", i, (short) hitlist.get(i).getAssociatedClusterID());
            bank.setShort("trkID", i, (short) hitlist.get(i).getAssociatedTrackID());

            bank.setByte("status", i, (byte) hitlist.get(i).getStrip().getStatus());            
        }
        //bank.show();
        return bank;

    }

    /**
     *
     * @param event
     * @param cluslist the reconstructed list of fitted clusters in the event
     * @return clusters bank
     */
    public DataBank fillSVTClustersBank(DataEvent event, List<Cluster> cluslist) {
        if (cluslist == null) {
            return null;
        }
        if (cluslist.isEmpty()) {
            return null;
        }

        DataBank bank = event.createBank("BSTRec::Clusters", cluslist.size());
        int[] hitIdxArray = new int[5];

        for (int i = 0; i < cluslist.size(); i++) {
            for (int j = 0; j < hitIdxArray.length; j++) {
                hitIdxArray[j] = -1;
            }
            bank.setShort("ID", i, (short) cluslist.get(i).getId());
            bank.setByte("sector", i, (byte) cluslist.get(i).getSector());
            bank.setByte("layer", i, (byte) cluslist.get(i).getLayer());
            bank.setShort("size", i, (short) cluslist.get(i).size());
            bank.setFloat("ETot", i, (float) cluslist.get(i).getTotalEnergy());
            bank.setFloat("time", i, (float) cluslist.get(i).getTime());
            bank.setInt("seedStrip", i, cluslist.get(i).getSeedStrip().getStrip());
            bank.setFloat("centroid", i, (float) cluslist.get(i).getCentroid());
            bank.setFloat("seedE", i, (float) cluslist.get(i).getSeedStrip().getEdep());
            bank.setFloat("centroidError", i, (float) cluslist.get(i).getResolution()/10);
            bank.setFloat("centroidResidual", i, (float) cluslist.get(i).getCentroidResidual()/10);
            bank.setFloat("seedResidual", i, (float) cluslist.get(i).getSeedResidual()/10); 
            bank.setShort("trkID", i, (short) cluslist.get(i).getAssociatedTrackID());

            for (int j = 0; j < cluslist.get(i).size(); j++) {
                if (j < hitIdxArray.length) {
                    hitIdxArray[j] = cluslist.get(i).get(j).getId();
                }
            }

            for (int j = 0; j < hitIdxArray.length; j++) {
                String hitStrg = "Hit";
                hitStrg += (j + 1);
                hitStrg += "_ID";
                bank.setShort(hitStrg, i, (short) hitIdxArray[j]);
            }
            
            bank.setFloat("x1",   i, (float)cluslist.get(i).origin().x()/10);
            bank.setFloat("y1",   i, (float)cluslist.get(i).origin().y()/10);
            bank.setFloat("z1",   i, (float)cluslist.get(i).origin().z()/10);
            bank.setFloat("x2",   i, (float)cluslist.get(i).end().x()/10);
            bank.setFloat("y2",   i, (float)cluslist.get(i).end().y()/10);
            bank.setFloat("z2",   i, (float)cluslist.get(i).end().z()/10); 
            bank.setFloat("lx",   i, (float)cluslist.get(i).getL().x());
            bank.setFloat("ly",   i, (float)cluslist.get(i).getL().y());
            bank.setFloat("lz",   i, (float)cluslist.get(i).getL().z());
            bank.setFloat("sx",   i, (float)cluslist.get(i).getS().x());
            bank.setFloat("sy",   i, (float)cluslist.get(i).getS().y());
            bank.setFloat("sz",   i, (float)cluslist.get(i).getS().z());
            bank.setFloat("nx",   i, (float)cluslist.get(i).getN().x());
            bank.setFloat("ny",   i, (float)cluslist.get(i).getN().y());
            bank.setFloat("nz",   i, (float)cluslist.get(i).getN().z());
            bank.setFloat("e",    i, (float)cluslist.get(i).getResolution()/10);
//            cluslist.get(i).printInfo();
//            System.out.println("N "+cluslist.get(i).getNFromTraj().toString()+" \n"+
//                    " L "+cluslist.get(i).getL().toString()+" \n"+
//                    " S "+cluslist.get(i).getS().toString()+" \n"+
//                    " NxL "+cluslist.get(i).getN().cross(cluslist.get(i).getL()).toString()+" \n"+
//                    " NxS "+cluslist.get(i).getN().cross(cluslist.get(i).getS()).toString()+" \n"+
//                    " SxL "+cluslist.get(i).getS().cross(cluslist.get(i).getL()).toString()+" \n"+
//                    " N.L "+cluslist.get(i).getN().dot(cluslist.get(i).getL())+
//                    " N.S "+cluslist.get(i).getN().dot(cluslist.get(i).getS())+
//                    " S.L "+cluslist.get(i).getS().dot(cluslist.get(i).getL())
//            );
        }
        //bank.show();
        return bank;

    }

    /**
     *
     * @param event
     * @param crosses the reconstructed list of crosses in the event
     * @return crosses bank
     */
    public DataBank fillSVTCrossesBank(DataEvent event, List<ArrayList<Cross>> crosses) {
        if (crosses == null) {
            return null;
        }
        if (crosses.get(0).isEmpty()) {
            return null;
        }

        DataBank bank = event.createBank("BSTRec::Crosses", crosses.get(0).size());

        int index = 0;
        int i = 0;
        for (int j = 0; j < crosses.get(i).size(); j++) {
            bank.setShort("ID", index, (short) crosses.get(i).get(j).getId());
            bank.setByte("sector", index, (byte) crosses.get(i).get(j).getSector());
            bank.setByte("region", index, (byte) crosses.get(i).get(j).getRegion());
            bank.setFloat("x", index, (float) (crosses.get(i).get(j).getPoint().x()/10.));
            bank.setFloat("y", index, (float) (crosses.get(i).get(j).getPoint().y()/10.));
            bank.setFloat("z", index, (float) (crosses.get(i).get(j).getPoint().z()/10));
            bank.setFloat("err_x", index, (float) (crosses.get(i).get(j).getPointErr().x()/10.));
            bank.setFloat("err_y", index, (float) (crosses.get(i).get(j).getPointErr().y()/10.));
            bank.setFloat("err_z", index, (float) (crosses.get(i).get(j).getPointErr().z()/10.));
            bank.setShort("trkID", index, (short) crosses.get(i).get(j).getAssociatedTrackID());

            if (crosses.get(i).get(j).getDir() != null && 
                    !Double.isNaN(crosses.get(i).get(j).getDir().x()) &&
                    !Double.isNaN(crosses.get(i).get(j).getDir().y()) &&
                    !Double.isNaN(crosses.get(i).get(j).getDir().z()) ) {
                bank.setFloat("ux", index, (float) crosses.get(i).get(j).getDir().x());
                bank.setFloat("uy", index, (float) crosses.get(i).get(j).getDir().y());
                bank.setFloat("uz", index, (float) crosses.get(i).get(j).getDir().z());
            } else {
                bank.setFloat("ux", index, 0);
                bank.setFloat("uy", index, 0);
                bank.setFloat("uz", index, 0);
            }
            if (crosses.get(i).get(j).getCluster1() != null) {
                bank.setShort("Cluster1_ID", index, (short) crosses.get(i).get(j).getCluster1().getId());
            }
            if (crosses.get(i).get(j).getCluster2() != null) {
                bank.setShort("Cluster2_ID", index, (short) crosses.get(i).get(j).getCluster2().getId());
            }
            index++;
        }

        //bank.show();
        return bank;

    }

    public DataBank fillBMTHitsBank(DataEvent event, List<Hit> hitlist) {
        if (hitlist == null) {
            return null;
        }
        if (hitlist.isEmpty()) {
            return null;
        }

        DataBank bank
                = event.createBank("BMTRec::Hits", hitlist.size());

        for (int i = 0; i < hitlist.size(); i++) {

            bank.setShort("ID", i, (short) hitlist.get(i).getId());

            bank.setByte("layer", i, (byte) hitlist.get(i).getLayer());
            bank.setByte("sector", i, (byte) hitlist.get(i).getSector());
            bank.setInt("strip", i, hitlist.get(i).getStrip().getStrip());

            bank.setFloat("energy", i, (float) hitlist.get(i).getStrip().getEdep());
            bank.setFloat("time", i, (float) hitlist.get(i).getStrip().getTime());
            bank.setFloat("fitResidual", i, (float) hitlist.get(i).getResidual()/10);
            bank.setInt("trkingStat", i, hitlist.get(i).getTrkgStatus());

            bank.setShort("clusterID", i, (short) hitlist.get(i).getAssociatedClusterID());
            bank.setShort("trkID", i, (short) hitlist.get(i).getAssociatedTrackID());

            bank.setByte("status", i, (byte) hitlist.get(i).getStrip().getStatus());  
        }

        return bank;

    }

    /**
     *
     * @param event
     * @param cluslist the reconstructed list of fitted clusters in the event
     * @return clusters bank
     */
    public DataBank fillBMTClustersBank(DataEvent event, List<Cluster> cluslist) {
        if (cluslist == null) {
            return null;
        }
        if (cluslist.isEmpty()) {
            return null;
        }

        DataBank bank = event.createBank("BMTRec::Clusters", cluslist.size());
        int[] hitIdxArray = new int[5];

        for (int i = 0; i < cluslist.size(); i++) {
            for (int j = 0; j < hitIdxArray.length; j++) {
                hitIdxArray[j] = -1;
            }
            bank.setShort("ID", i, (short) cluslist.get(i).getId());
            bank.setByte("sector", i, (byte) cluslist.get(i).getSector());
            bank.setByte("layer", i, (byte) cluslist.get(i).getLayer());
            bank.setShort("size", i, (short) cluslist.get(i).size());
            bank.setFloat("ETot", i, (float) cluslist.get(i).getTotalEnergy());
            bank.setFloat("time", i, (float) cluslist.get(i).getTime());
            bank.setInt("seedStrip", i, cluslist.get(i).getSeedStrip().getStrip());
            bank.setFloat("centroid", i, (float) cluslist.get(i).getCentroid());
            if(cluslist.get(i).getType()==BMTType.C) {
                bank.setFloat("centroidValue", i, (float) cluslist.get(i).getCentroidValue()/10);
                bank.setFloat("centroidError", i, (float) cluslist.get(i).getCentroidError()/10);
            }
            else {
                bank.setFloat("centroidValue", i, (float) cluslist.get(i).getCentroidValue());
                bank.setFloat("centroidError", i, (float) cluslist.get(i).getCentroidError());                
            }
            bank.setFloat("centroidResidual", i, (float) cluslist.get(i).getCentroidResidual()/10);
            bank.setFloat("seedResidual", i, (float) cluslist.get(i).getSeedResidual()/10); 
            bank.setFloat("seedE", i, (float) cluslist.get(i).getSeedStrip().getEdep());
            bank.setShort("trkID", i, (short) cluslist.get(i).getAssociatedTrackID());
            for (int j = 0; j < cluslist.get(i).size(); j++) {
                if (j < hitIdxArray.length) {
                    hitIdxArray[j] = cluslist.get(i).get(j).getId();
                }
            }

            for (int j = 0; j < hitIdxArray.length; j++) {
                String hitStrg = "Hit";
                hitStrg += (j + 1);
                hitStrg += "_ID";
                bank.setShort(hitStrg, i, (short) hitIdxArray[j]);
            }
            bank.setFloat("x1",   i, (float)cluslist.get(i).origin().x()/10);
            bank.setFloat("y1",   i, (float)cluslist.get(i).origin().y()/10);
            bank.setFloat("z1",   i, (float)cluslist.get(i).origin().z()/10);
            bank.setFloat("x2",   i, (float)cluslist.get(i).end().x()/10);
            bank.setFloat("y2",   i, (float)cluslist.get(i).end().y()/10);
            bank.setFloat("z2",   i, (float)cluslist.get(i).end().z()/10);
            bank.setFloat("cx",   i, (float)cluslist.get(i).center().x()/10);
            bank.setFloat("cy",   i, (float)cluslist.get(i).center().y()/10);
            bank.setFloat("cz",   i, (float)cluslist.get(i).center().z()/10);
            bank.setFloat("theta",i, (float)cluslist.get(i).theta());
            bank.setFloat("ax1",  i, (float)cluslist.get(i).getAxis().origin().x()/10);
            bank.setFloat("ay1",  i, (float)cluslist.get(i).getAxis().origin().y()/10);
            bank.setFloat("az1",  i, (float)cluslist.get(i).getAxis().origin().z()/10);
            bank.setFloat("ax2",  i, (float)cluslist.get(i).getAxis().end().x()/10);
            bank.setFloat("ay2",  i, (float)cluslist.get(i).getAxis().end().y()/10);
            bank.setFloat("az2",  i, (float)cluslist.get(i).getAxis().end().z()/10);
            bank.setFloat("lx",   i, (float)cluslist.get(i).getL().x());
            bank.setFloat("ly",   i, (float)cluslist.get(i).getL().y());
            bank.setFloat("lz",   i, (float)cluslist.get(i).getL().z());
            bank.setFloat("sx",   i, (float)cluslist.get(i).getS().x());
            bank.setFloat("sy",   i, (float)cluslist.get(i).getS().y());
            bank.setFloat("sz",   i, (float)cluslist.get(i).getS().z());
            bank.setFloat("nx",   i, (float)cluslist.get(i).getN().x());
            bank.setFloat("ny",   i, (float)cluslist.get(i).getN().y());
            bank.setFloat("nz",   i, (float)cluslist.get(i).getN().z());
            bank.setFloat("e",    i, (float)cluslist.get(i).getResolution()/10);
            if(debug && cluslist.get(i).getAssociatedTrackID()>0 && cluslist.get(i).getType()==BMTType.Z) {
                Line3D cln = new Line3D(cluslist.get(i).origin(), cluslist.get(i).end());
                System.out.println("Check: N "+cluslist.get(i).getN().toString()+" \n"+
                    " L "+cluslist.get(i).getL().toString()+" \n"+
                    " S "+cluslist.get(i).getS().toString()+" \n"+
                    " NxL "+cluslist.get(i).getN().cross(cluslist.get(i).getL()).toString()+" \n"+
                    " NxS "+cluslist.get(i).getN().cross(cluslist.get(i).getS()).toString()+" \n"+
                    " SxL "+cluslist.get(i).getS().cross(cluslist.get(i).getL()).toString()+" \n"+
                    " line "+cln.toString()+" \n"
                            +" line dir . L "+cln.direction().asUnit().dot(cluslist.get(i).getL())
                            +" line dir . S "+cln.direction().asUnit().dot(cluslist.get(i).getS())
                            +" line dir . N "+cln.direction().asUnit().dot(cluslist.get(i).getN())+
                    "\n N.L "+cluslist.get(i).getN().dot(cluslist.get(i).getL())+
                    " N.S "+cluslist.get(i).getN().dot(cluslist.get(i).getS())+
                    " S.L "+cluslist.get(i).getS().dot(cluslist.get(i).getL())
            );
            
            }
        }
        //bank.show();
        return bank;

    }

    /**
     *
     * @param event
     * @param crosses the reconstructed list of crosses in the event
     * @return crosses bank
     */
    public DataBank fillBMTCrossesBank(DataEvent event, List<ArrayList<Cross>> crosses) {
        if (crosses == null) {
            return null;
        }
        if (crosses.get(1).isEmpty()) {
            return null;
        }

        DataBank bank = event.createBank("BMTRec::Crosses", crosses.get(1).size());

        int index = 0;
        int i = 1;
        for (int j = 0; j < crosses.get(i).size(); j++) {
            bank.setShort("ID", index, (short) crosses.get(i).get(j).getId());
            bank.setByte("sector", index, (byte) crosses.get(i).get(j).getSector());
            bank.setByte("region", index, (byte) crosses.get(i).get(j).getRegion());
            bank.setFloat("x", index, (float) (crosses.get(i).get(j).getPoint().x()/10.));
            bank.setFloat("y", index, (float) (crosses.get(i).get(j).getPoint().y()/10.));
            bank.setFloat("z", index, (float) (crosses.get(i).get(j).getPoint().z()/10));
            bank.setFloat("err_x", index, (float) (crosses.get(i).get(j).getPointErr().x()/10.));
            bank.setFloat("err_y", index, (float) (crosses.get(i).get(j).getPointErr().y()/10.));
            bank.setFloat("err_z", index, (float) (crosses.get(i).get(j).getPointErr().z()/10.));
            bank.setShort("trkID", index, (short) crosses.get(i).get(j).getAssociatedTrackID());
           
            if (crosses.get(i).get(j).getDir() != null && 
                    !Double.isNaN(crosses.get(i).get(j).getDir().x()) &&
                    !Double.isNaN(crosses.get(i).get(j).getDir().y()) &&
                    !Double.isNaN(crosses.get(i).get(j).getDir().z()) ) {
                bank.setFloat("ux", index, (float) crosses.get(i).get(j).getDir().x());
                bank.setFloat("uy", index, (float) crosses.get(i).get(j).getDir().y());
                bank.setFloat("uz", index, (float) crosses.get(i).get(j).getDir().z());
            } else {
                bank.setFloat("ux", index, 0);
                bank.setFloat("uy", index, 0);
                bank.setFloat("uz", index, 0);
            }
            if (crosses.get(i).get(j).getCluster1() != null) {
                bank.setShort("Cluster1_ID", index, (short) crosses.get(i).get(j).getCluster1().getId());
            }
            if (crosses.get(i).get(j).getCluster2() != null) {
                bank.setShort("Cluster2_ID", index, (short) crosses.get(i).get(j).getCluster2().getId());
            }
            index++;
        }

        return bank;

    }

    public DataBank fillSeedsBank(DataEvent event, List<Seed> seeds) {
        if (seeds == null) {
            return null;
        }
        if (seeds.isEmpty()) {
            return null;
        }

        DataBank bank = event.createBank("CVTRec::Seeds", seeds.size());
        // an array representing the ids of the crosses that belong to the track
        List<Integer> crossIdxArray = new ArrayList<>();

        for (int i = 0; i < seeds.size(); i++) {
            if(seeds.get(i)==null)
                continue;
            bank.setByte("fittingMethod", i, (byte) seeds.get(i).getStatus());
            bank.setShort("ID", i, (short) seeds.get(i).getId());
            Helix helix = seeds.get(i).getHelix();
            bank.setByte("q", i, (byte) (Math.signum(Constants.getSolenoidScale())*helix.getCharge()));
            bank.setFloat("p", i, (float) helix.getPXYZ(seeds.get(i).getHelix().B).mag());
            bank.setFloat("pt", i, (float) helix.getPt(seeds.get(i).getHelix().B));
            bank.setFloat("phi0", i, (float) helix.getPhiAtDCA());
            bank.setFloat("tandip", i, (float) helix.getTanDip());
            bank.setFloat("z0", i, (float) (helix.getZ0()/10.0));
            bank.setFloat("d0", i, (float) (helix.getDCA()/10.0));
            double[][] covmatrix = helix.getCovMatrix();
            if (covmatrix != null) {
                bank.setFloat("cov_d02", i, (float) covmatrix[0][0]/10/10 );
                bank.setFloat("cov_d0phi0", i, (float) covmatrix[0][1]/10 );
                bank.setFloat("cov_d0rho", i, (float) covmatrix[0][2] );
                bank.setFloat("cov_phi02", i, (float) covmatrix[1][1] );
                bank.setFloat("cov_phi0rho", i, (float) covmatrix[1][2]*10 );
                bank.setFloat("cov_rho2", i, (float) covmatrix[2][2]*10*10 );
                bank.setFloat("cov_z02", i, (float) covmatrix[3][3]/10/10 );
                bank.setFloat("cov_tandip2", i, (float) covmatrix[4][4] );
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
            bank.setFloat("xb", i, (float) (helix.getXb()/10.0));
            bank.setFloat("yb", i, (float) (helix.getYb()/10.0));
            // fills the list of cross ids for crosses belonging to that reconstructed track
             for (int j = 0; j < 9; j++) {
                String hitStrg = "Cross";
                hitStrg += (j + 1);
                hitStrg += "_ID";  
                bank.setShort(hitStrg, i, (short) -1);
            }
            for (int j = 0; j < seeds.get(i).getCrosses().size(); j++) {
                if(j<9) {
                    String hitStrg = "Cross";
                    hitStrg += (j + 1);
                    hitStrg += "_ID";  //System.out.println(" j "+j+" matched id "+trkcands.get(i).get(j).getId());
                    bank.setShort(hitStrg, i, (short) seeds.get(i).getCrosses().get(j).getId());
                }
            }
            bank.setFloat("circlefit_chi2_per_ndf", i, (float) seeds.get(i).getCircleFitChi2PerNDF());
            bank.setFloat("linefit_chi2_per_ndf", i, (float) seeds.get(i).getLineFitChi2PerNDF());
            bank.setFloat("chi2", i, (float) seeds.get(i).getChi2());
            bank.setShort("ndf", i, (short) seeds.get(i).getNDF());

        }
        //bank.show();
        return bank;

    }

    /**
     * 
     * @param event the event
     * @param trkcands the list of reconstructed helical tracks
     * @return track bank
     */
    public DataBank fillTracksBank(DataEvent event, List<Track> trkcands) {
        if (trkcands == null) {
            return null;
        }
        if (trkcands.isEmpty()) {
            return null;
        }

        DataBank bank = event.createBank("CVTRec::Tracks", trkcands.size());
        // an array representing the ids of the crosses that belong to the track
        List<Integer> crossIdxArray = new ArrayList<>();

        for (int i = 0; i < trkcands.size(); i++) {
            if(trkcands.get(i)==null)
                continue;
//            if(trkcands.get(i).getChi2()!=0) {
//                bank.setByte("fittingMethod", i, (byte) 2);
//            } else {
//                bank.setByte("fittingMethod", i, (byte) 0);
//            }
            bank.setByte("fittingMethod", i, (byte) trkcands.get(i).getSeed().getStatus());
            bank.setShort("ID", i, (short) trkcands.get(i).getId());
            bank.setByte("q", i, (byte)trkcands.get(i).getQ());
            bank.setFloat("p", i, (float) trkcands.get(i).getP());
            bank.setFloat("pt", i, (float) trkcands.get(i).getPt());
            Helix helix = trkcands.get(i).getHelix();
            bank.setFloat("phi0", i, (float) helix.getPhiAtDCA());
            bank.setFloat("tandip", i, (float) helix.getTanDip());
            bank.setFloat("z0", i, (float) (helix.getZ0()/10.));
            bank.setFloat("d0", i, (float) (helix.getDCA()/10.));
            bank.setFloat("xb", i, (float) (helix.getXb()/10.0));
            bank.setFloat("yb", i, (float) (helix.getYb()/10.0));
            // this is the format of the covariance matrix for helical tracks
            // cov matrix = 
            // | d_dca*d_dca                   d_dca*d_phi_at_dca            d_dca*d_curvature        0            0             |
            // | d_phi_at_dca*d_dca     d_phi_at_dca*d_phi_at_dca     d_phi_at_dca*d_curvature        0            0             |
            // | d_curvature*d_dca	    d_curvature*d_phi_at_dca      d_curvature*d_curvature         0            0             |
            // | 0                              0                             0                    d_Z0*d_Z0                     |
            // | 0                              0                             0                       0        d_tandip*d_tandip |X
            double[][] covmatrix = helix.getCovMatrix();
            if (covmatrix != null) {
                bank.setFloat("cov_d02", i, (float) covmatrix[0][0]/10/10 );
                bank.setFloat("cov_d0phi0", i, (float) covmatrix[0][1]/10 );
                bank.setFloat("cov_d0rho", i, (float) covmatrix[0][2] );
                bank.setFloat("cov_phi02", i, (float) covmatrix[1][1] );
                bank.setFloat("cov_phi0rho", i, (float) covmatrix[1][2]*10 );
                bank.setFloat("cov_rho2", i, (float) covmatrix[2][2]*10*10 );
                bank.setFloat("cov_z02", i, (float) covmatrix[3][3]/10/10 );
                bank.setFloat("cov_tandip2", i, (float) covmatrix[4][4] );
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
            if(trkcands.get(i).getTrackPosAtCTOF()!=null) {
                bank.setFloat("c_x", i, (float) (trkcands.get(i).getTrackPosAtCTOF().x() / 10.)); // convert to cm
                bank.setFloat("c_y", i, (float) (trkcands.get(i).getTrackPosAtCTOF().y() / 10.)); // convert to cm
                bank.setFloat("c_z", i, (float) (trkcands.get(i).getTrackPosAtCTOF().z() / 10.)); // convert to cm
                bank.setFloat("c_ux", i, (float) trkcands.get(i).getTrackDirAtCTOF().x());
                bank.setFloat("c_uy", i, (float) trkcands.get(i).getTrackDirAtCTOF().y());
                bank.setFloat("c_uz", i, (float) trkcands.get(i).getTrackDirAtCTOF().z());
                bank.setFloat("pathlength", i, (float) (trkcands.get(i).getPathToCTOF() / 10.)); // conversion to cm
            }
            // fills the list of cross ids for crosses belonging to that reconstructed track
            for (int j = 0; j < 9; j++) {
                String hitStrg = "Cross";
                hitStrg += (j + 1);
                hitStrg += "_ID";  
                bank.setShort(hitStrg, i, (short) -1);
                
            }
            for (int j = 0; j < trkcands.get(i).size(); j++) {
                if(j<9) {
                    String hitStrg = "Cross";
                    hitStrg += (j + 1);
                    hitStrg += "_ID";  //System.out.println(" j "+j+" matched id "+trkcands.get(i).get(j).getId());
                    bank.setShort(hitStrg, i, (short) trkcands.get(i).get(j).getId());
                }
            }
            bank.setShort("status", i, (short) ((short) trkcands.get(i).getStatus()));
//            bank.setFloat("circlefit_chi2_per_ndf", i, (float) trkcands.get(i).getCircleFitChi2PerNDF());
//            bank.setFloat("linefit_chi2_per_ndf", i, (float) trkcands.get(i).getLineFitChi2PerNDF());
            bank.setShort("seedID", i, (short) trkcands.get(i).getSeed().getId());
            bank.setFloat("chi2", i, (float) trkcands.get(i).getChi2());
            bank.setShort("ndf", i, (short) trkcands.get(i).getNDF());

        }
        //bank.show();
        return bank;

    }
    
    public DataBank fillTracksCovMatBank(DataEvent event, List<Track> trkcands) {
        if (trkcands == null) {
            return null;
        }
        if (trkcands.isEmpty()) {
            return null;
        }

        DataBank bank = event.createBank("CVTRec::TrackCovMats", trkcands.size());
        // an array representing the ids of the crosses that belong to the track
        List<Integer> crossIdxArray = new ArrayList<>();

        for (int i = 0; i < trkcands.size(); i++) {
            if(trkcands.get(i)==null || trkcands.get(i).getTrackCovMat()==null)
                continue;
            bank.setShort("ID", i, (short) trkcands.get(i).getId());
            double[][] covmatrix = trkcands.get(i).getTrackCovMat();
            if (covmatrix != null) {
                String[][] names = new String[][]{
                    {"cov_xx", "cov_xy", "cov_xz", "cov_xpx", "cov_xpy", "cov_xpz"},
                    {"cov_yx", "cov_yy", "cov_yz", "cov_ypx", "cov_ypy", "cov_ypz"},
                    {"cov_zx", "cov_zy", "cov_zz", "cov_zpx", "cov_zpy", "cov_zpz"},
                    {"cov_pxx", "cov_pxy", "cov_pxz", "cov_pxpx", "cov_pxpy", "cov_pxpz"},
                    {"cov_pyx", "cov_pyy", "cov_pyz", "cov_pypx", "cov_pypy", "cov_pypz"},
                    {"cov_pzx", "cov_pzy", "cov_pzz", "cov_pzpx", "cov_pzpy", "cov_pzpz"}
                };
              
                for(int r = 0; r<6; r++) {
                    for(int c = 0; c<6; c++) {
                        bank.setFloat(names[r][c], i, (float) covmatrix[r][c] );
                    }
                }
            }
        }
        
        //bank.show();
        return bank;

    }

    /**
     *
     * @param event the event
     * @param cosmics
     * @return cosmic bank
     */
    public DataBank fillStraightTracksBank(DataEvent event, List<StraightTrack> cosmics) {
        if (cosmics == null) {
            return null;
        }
        if (cosmics.isEmpty()) {
            return null;
        }

        DataBank bank = event.createBank("CVTRec::Cosmics", cosmics.size());
        // an array representing the ids of the crosses that belong to the track: for a helical track with the current
        // 4 regions of SVT + 1 region of BMT there can be up to 4*2 (*2: for each hemisphere) crosses of type SVT and 2*2 of type PSEUDOBMT (1 for the C detector and 1 for the Z detector)

        for (int i = 0; i < cosmics.size(); i++) {
            List<Integer> crossIdxArray = new ArrayList<>();


            bank.setShort("ID", i, (short) cosmics.get(i).getId());
            bank.setFloat("chi2", i, (float) cosmics.get(i).getchi2());
            bank.setShort("ndf", i, (short) (cosmics.get(i).size()-2));
            bank.setFloat("trkline_yx_slope", i, (float) cosmics.get(i).getRay().getYXSlope());
            bank.setFloat("trkline_yx_interc", i, (float) (cosmics.get(i).getRay().getYXInterc()/10.));
            bank.setFloat("trkline_yz_slope", i, (float) cosmics.get(i).getRay().getYZSlope());
            bank.setFloat("trkline_yz_interc", i, (float) (cosmics.get(i).getRay().getYZInterc()/10.));

            // get the cosmics ray unit direction vector
            Vector3D u = new Vector3D(cosmics.get(i).getRay().getYXSlope(), 1, cosmics.get(i).getRay().getYZSlope()).asUnit();
            // calculate the theta and phi components of the ray direction vector in degrees
            bank.setFloat("theta", i, (float) Math.toDegrees(u.theta()));
            bank.setFloat("phi", i, (float) Math.toDegrees(u.phi()));
            // the array of cross ids is filled in order of the SVT cosmic region 1 to 8 starting from the bottom-most double layer
            for (int j = 0; j < cosmics.get(i).size(); j++) {
                crossIdxArray.add(cosmics.get(i).get(j).getId());
            }
             for (int j = 0; j < 18; j++) {
                String hitStrg = "Cross";
                hitStrg += (j + 1);
                hitStrg += "_ID";  
                bank.setShort(hitStrg, i, (short) -1);
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

    public DataBank fillStraightTracksTrajectoryBank(DataEvent event, List<StraightTrack> trks) {
        if (trks == null) {
            return null;
        }
        if (trks.isEmpty()) {
            return null;
        }

        int k = 0;
        for (int i = 0; i < trks.size(); i++) {
            if (trks.get(i).getTrajectory() == null) {
                continue;
            }
            if (trks.get(i).getTrajectory() != null) {
                k += trks.get(i).getTrajectory().size();
            }

        }
        DataBank bank = event.createBank("CVTRec::Trajectory", k);

        k = 0;
        for (int i = 0; i < trks.size(); i++) {
            if (trks.get(i).getTrajectory() == null) {
                continue;
            }
            for (StateVec stVec : trks.get(i).getTrajectory()) {

                bank.setShort("id",       k, (short) trks.get(i).getId());
                bank.setByte("detector",  k, (byte) stVec.getSurfaceDetector());
                bank.setByte("sector",    k, (byte) stVec.getSurfaceSector());
                bank.setByte("layer",     k, (byte) stVec.getSurfaceLayer());
                bank.setFloat("x",        k, (float) (stVec.x()/10.));
                bank.setFloat("y",        k, (float) (stVec.y()/10.));
                bank.setFloat("z",        k, (float) (stVec.z()/10.));
                bank.setFloat("phi",      k, (float) stVec.getTrkPhiAtSurface());
                bank.setFloat("theta",    k, (float) stVec.getTrkThetaAtSurface());
                bank.setFloat("langle",   k, (float) stVec.getTrkToModuleAngle());
                bank.setFloat("centroid", k, (float) stVec.getCalcCentroidStrip());
                bank.setFloat("path",     k, (float) stVec.getPath()/10);
                k++;

            }
        }
        //bank.show();
        return bank;
    }

    public DataBank fillHelicalTracksTrajectoryBank(DataEvent event, List<Track> trks) {
        if (trks == null) {
            return null;
        }
        if (trks.isEmpty()) {
            return null;
        }
        int bankSize = 0;
        for (int i = 0; i < trks.size(); i++) {
            if(trks.get(i)==null)
                continue;
            if (trks.get(i).getTrajectory() == null) {
                continue;
            }
            for (StateVec stVec : trks.get(i).getTrajectory())
                bankSize++;
        }

        if(bankSize==0) return null;
        
        DataBank bank = event.createBank("CVTRec::Trajectory", bankSize); //  SVT layers +  BMT layers 

        int k = 0;
        for (int i = 0; i < trks.size(); i++) {
             if(trks.get(i)==null)
                continue;
            if (trks.get(i).getTrajectory() == null) {
                continue;
            }
            for (StateVec stVec : trks.get(i).getTrajectory()) {

                bank.setShort("id",       k, (short) trks.get(i).getId());
                bank.setByte("detector",  k, (byte) stVec.getSurfaceDetector());
                bank.setByte("sector",    k, (byte) stVec.getSurfaceSector());
                bank.setByte("layer",     k, (byte) stVec.getSurfaceLayer());
                bank.setFloat("x",        k, (float) (stVec.x()/10.));
                bank.setFloat("y",        k, (float) (stVec.y()/10.));
                bank.setFloat("z",        k, (float) (stVec.z()/10.));
                bank.setFloat("phi",      k, (float) stVec.getTrkPhiAtSurface());
                bank.setFloat("theta",    k, (float) stVec.getTrkThetaAtSurface());
                bank.setFloat("langle",   k, (float) stVec.getTrkToModuleAngle());
                bank.setFloat("centroid", k, (float) stVec.getCalcCentroidStrip());
                bank.setFloat("path",     k, (float) stVec.getPath()/10);
                k++;

            }
        }

        return bank;
    }

    public void appendCVTBanks(DataEvent event,
            List<Hit> sVThits, List<Hit> bMThits,
            List<Cluster> sVTclusters, List<Cluster> bMTclusters,
            List<ArrayList<Cross>> crosses, List<Seed> seeds, List<Track> trks) {

        DataBank bank1 = this.fillSVTHitsBank(event, sVThits);
        if (bank1 != null) event.appendBank(bank1);

        DataBank bank2 = this.fillBMTHitsBank(event, bMThits);
        if (bank2 != null) event.appendBank(bank2);

        DataBank bank3 = this.fillSVTClustersBank(event, sVTclusters);
        if (bank3 != null) event.appendBank(bank3);

        DataBank bank4 = this.fillBMTClustersBank(event, bMTclusters);
        if (bank4 != null) event.appendBank(bank4);

        DataBank bank5 = this.fillSVTCrossesBank(event, crosses);
        if (bank5 != null) event.appendBank(bank5);

        DataBank bank6 = this.fillBMTCrossesBank(event, crosses);
        if (bank6 != null) event.appendBank(bank6);

        DataBank bank7 = this.fillSeedsBank(event, seeds);
        if (bank7 != null) event.appendBank(bank7);

        DataBank bank8 = this.fillTracksBank(event, trks);
        if (bank8 != null) event.appendBank(bank8);

        DataBank bank9 = this.fillHelicalTracksTrajectoryBank(event, trks);
        if (bank9 != null) event.appendBank(bank9);
        
        DataBank bank10 = this.fillTracksCovMatBank(event, trks);
        if (bank10 != null) event.appendBank(bank10);
        
    }

    public void appendCVTCosmicsBanks(DataEvent event,
            List<Hit> sVThits, List<Hit> bMThits,
            List<Cluster> sVTclusters, List<Cluster> bMTclusters,
            List<ArrayList<Cross>> crosses, List<StraightTrack> trks) {
        List<DataBank> svtbanks = new ArrayList<>();
        List<DataBank> bmtbanks = new ArrayList<>();
        List<DataBank> cvtbanks = new ArrayList<>();

        DataBank bank1 = this.fillSVTHitsBank(event, sVThits);
        if (bank1 != null) event.appendBank(bank1);

        DataBank bank2 = this.fillBMTHitsBank(event, bMThits);
        if (bank2 != null) event.appendBank(bank2);
        
        DataBank bank3 = this.fillSVTClustersBank(event, sVTclusters);
        if (bank3 != null) event.appendBank(bank3);

        DataBank bank4 = this.fillBMTClustersBank(event, bMTclusters);
        if (bank4 != null) event.appendBank(bank4);

        DataBank bank5 = this.fillSVTCrossesBank(event, crosses);
        if (bank5 != null) event.appendBank(bank5);

        DataBank bank6 = this.fillBMTCrossesBank(event, crosses);
        if (bank6 != null) event.appendBank(bank6);

        //found tracks
        DataBank bank7 = this.fillStraightTracksBank(event, trks);
        if (bank7 != null) event.appendBank(bank7);

        //found trajectories
        DataBank bank8 = this.fillStraightTracksTrajectoryBank(event, trks);
        if (bank8 != null) event.appendBank(bank8);

    }

}
