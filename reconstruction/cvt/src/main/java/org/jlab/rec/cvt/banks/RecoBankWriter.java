package org.jlab.rec.cvt.banks;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.tracking.kalmanfilter.AKFitter;
import org.jlab.clas.tracking.kalmanfilter.AKFitter.HitOnTrack;

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
import org.jlab.rec.cvt.measurement.MLayer;
import org.jlab.rec.cvt.track.Seed;

public class RecoBankWriter {
    
    private static boolean debug = false;
    
    /**
     *
     * @param event
     * @param hitlist the list of hits 
     * @param bankName
     * @return 
     */
    public static DataBank fillSVTHitBank(DataEvent event, List<Hit> hitlist, String bankName) {
        if (hitlist == null || hitlist.isEmpty()) return null;

        DataBank bank = event.createBank(bankName, hitlist.size());
        
        for (int i = 0; i < hitlist.size(); i++) {

            bank.setShort("ID", i, (short) hitlist.get(i).getId());

            bank.setByte("layer", i, (byte) hitlist.get(i).getLayer());
            bank.setByte("sector", i, (byte) hitlist.get(i).getSector());
            bank.setShort("strip", i, (short) hitlist.get(i).getStrip().getStrip());

            bank.setFloat("energy", i,  (float) hitlist.get(i).getStrip().getEdep());
            bank.setFloat("time", i,  (float) hitlist.get(i).getStrip().getTime());
            bank.setFloat("fitResidual", i,  (float) hitlist.get(i).getResidual()/10);
            bank.setByte("trkingStat", i, (byte) hitlist.get(i).getTrkgStatus());

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
     * @param bankName
     * @return 
     */
    public static DataBank fillSVTClusterBank(DataEvent event, List<Cluster> cluslist, String bankName) {
        if (cluslist == null || cluslist.isEmpty()) return null;

        DataBank bank = event.createBank(bankName, cluslist.size());
        
        int[] hitIdxArray = new int[5];

        for (int i = 0; i < cluslist.size(); i++) {
            for (int j = 0; j < hitIdxArray.length; j++) {
                hitIdxArray[j] = -1;
            }
            bank.setShort("ID", i, (short) cluslist.get(i).getId());
            bank.setByte("sector", i, (byte) cluslist.get(i).getSector());
            bank.setByte("layer", i, (byte) cluslist.get(i).getLayer());
            bank.setShort("size", i, (short) cluslist.get(i).size());
            bank.setFloat("ETot", i,  (float) cluslist.get(i).getTotalEnergy());
            bank.setFloat("time", i,  (float) cluslist.get(i).getTime());
            bank.setShort("seedStrip", i, (short) cluslist.get(i).getSeedStrip().getStrip());
            bank.setFloat("centroid", i,  (float) cluslist.get(i).getCentroid());
            bank.setFloat("seedE", i,  (float) cluslist.get(i).getSeedStrip().getEdep());
            bank.setFloat("centroidError", i,  (float) cluslist.get(i).getResolution()/10);
            bank.setFloat("centroidResidual", i,  (float) cluslist.get(i).getCentroidResidual()/10);
            bank.setFloat("seedResidual", i,  (float) cluslist.get(i).getSeedResidual()/10); 
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
            
            bank.setFloat("x1",   i,  (float)cluslist.get(i).origin().x()/10);
            bank.setFloat("y1",   i,  (float)cluslist.get(i).origin().y()/10);
            bank.setFloat("z1",   i,  (float)cluslist.get(i).origin().z()/10);
            bank.setFloat("x2",   i,  (float)cluslist.get(i).end().x()/10);
            bank.setFloat("y2",   i,  (float)cluslist.get(i).end().y()/10);
            bank.setFloat("z2",   i,  (float)cluslist.get(i).end().z()/10); 
            bank.setFloat("cx",   i,  (float)cluslist.get(i).center().x()/10);
            bank.setFloat("cy",   i,  (float)cluslist.get(i).center().y()/10);
            bank.setFloat("cz",   i,  (float)cluslist.get(i).center().z()/10);
            bank.setFloat("lx",   i,  (float)cluslist.get(i).getL().x());
            bank.setFloat("ly",   i,  (float)cluslist.get(i).getL().y());
            bank.setFloat("lz",   i,  (float)cluslist.get(i).getL().z());
            bank.setFloat("sx",   i,  (float)cluslist.get(i).getS().x());
            bank.setFloat("sy",   i,  (float)cluslist.get(i).getS().y());
            bank.setFloat("sz",   i,  (float)cluslist.get(i).getS().z());
            bank.setFloat("nx",   i,  (float)cluslist.get(i).getN().x());
            bank.setFloat("ny",   i,  (float)cluslist.get(i).getN().y());
            bank.setFloat("nz",   i,  (float)cluslist.get(i).getN().z());
            bank.setFloat("e",    i,  (float)cluslist.get(i).getResolution()/10);
        }
        //bank.show();
        return bank;

    }

    /**
     *
     * @param event
     * @param crosses the reconstructed list of crosses in the event
     * @param bankName
     * @return 
     */
    public static DataBank fillSVTCrossBank(DataEvent event, List<Cross> crosses, String bankName) {
        if (crosses == null || crosses.isEmpty()) return null;

        DataBank bank = event.createBank(bankName, crosses.size());

        for (int j = 0; j < crosses.size(); j++) {
            bank.setShort("ID", j, (short) crosses.get(j).getId());
            bank.setByte("sector", j, (byte) crosses.get(j).getSector());
            bank.setByte("region", j, (byte) crosses.get(j).getRegion());
            bank.setFloat("x", j,  (float) (crosses.get(j).getPoint().x()/10.));
            bank.setFloat("y", j,  (float) (crosses.get(j).getPoint().y()/10.));
            bank.setFloat("z", j,  (float) (crosses.get(j).getPoint().z()/10));
            bank.setFloat("err_x", j,  (float) (crosses.get(j).getPointErr().x()/10.));
            bank.setFloat("err_y", j,  (float) (crosses.get(j).getPointErr().y()/10.));
            bank.setFloat("err_z", j,  (float) (crosses.get(j).getPointErr().z()/10.));
            bank.setFloat("x0", j,  (float) (crosses.get(j).getPoint0().x()/10.));
            bank.setFloat("y0", j,  (float) (crosses.get(j).getPoint0().y()/10.));
            bank.setFloat("z0", j,  (float) (crosses.get(j).getPoint0().z()/10));
            bank.setFloat("err_x0", j,  (float) (crosses.get(j).getPointErr0().x()/10.));
            bank.setFloat("err_y0", j,  (float) (crosses.get(j).getPointErr0().y()/10.));
            bank.setFloat("err_z0", j,  (float) (crosses.get(j).getPointErr0().z()/10.));
            bank.setShort("trkID", j, (short) crosses.get(j).getAssociatedTrackID());

            if (crosses.get(j).getDir() != null && 
                    !Double.isNaN(crosses.get(j).getDir().x()) &&
                    !Double.isNaN(crosses.get(j).getDir().y()) &&
                    !Double.isNaN(crosses.get(j).getDir().z()) ) {
                bank.setFloat("ux", j,  (float) crosses.get(j).getDir().x());
                bank.setFloat("uy", j,  (float) crosses.get(j).getDir().y());
                bank.setFloat("uz", j,  (float) crosses.get(j).getDir().z());
            } else {
                bank.setFloat("ux", j, 0);
                bank.setFloat("uy", j, 0);
                bank.setFloat("uz", j, 0);
            }
            if (crosses.get(j).getCluster1() != null) {
                bank.setShort("Cluster1_ID", j, (short) crosses.get(j).getCluster1().getId());
            }
            if (crosses.get(j).getCluster2() != null) {
                bank.setShort("Cluster2_ID", j, (short) crosses.get(j).getCluster2().getId());
            }
        }

        //bank.show();
        return bank;

    }

    public static DataBank fillBMTHitBank(DataEvent event, List<Hit> hitlist, String bankName) {
        if (hitlist == null || hitlist.isEmpty()) return null;

        DataBank bank = event.createBank(bankName, hitlist.size());

        for (int i = 0; i < hitlist.size(); i++) {

            bank.setShort("ID", i, (short) hitlist.get(i).getId());

            bank.setByte("layer", i, (byte) hitlist.get(i).getLayer());
            bank.setByte("sector", i, (byte) hitlist.get(i).getSector());
            bank.setShort("strip", i, (short) hitlist.get(i).getStrip().getStrip());

            bank.setFloat("energy", i,  (float) hitlist.get(i).getStrip().getEdep());
            bank.setFloat("time", i,  (float) hitlist.get(i).getStrip().getTime());
            bank.setFloat("fitResidual", i,  (float) hitlist.get(i).getResidual()/10);
            bank.setByte("trkingStat", i, (byte) hitlist.get(i).getTrkgStatus());

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
     * @param bankName
     * @return 
     */
    public static DataBank fillBMTClusterBank(DataEvent event, List<Cluster> cluslist, String bankName) {
        if (cluslist == null || cluslist.isEmpty()) return null;

        DataBank bank = event.createBank(bankName, cluslist.size());
        
        int[] hitIdxArray = new int[5];

        for (int i = 0; i < cluslist.size(); i++) {
            for (int j = 0; j < hitIdxArray.length; j++) {
                hitIdxArray[j] = -1;
            }
            bank.setShort("ID", i, (short) cluslist.get(i).getId());
            bank.setByte("sector", i, (byte) cluslist.get(i).getSector());
            bank.setByte("layer", i, (byte) cluslist.get(i).getLayer());
            bank.setShort("size", i, (short) cluslist.get(i).size());
            bank.setFloat("ETot", i,  (float) cluslist.get(i).getTotalEnergy());
            bank.setFloat("time", i,  (float) cluslist.get(i).getTime());
            bank.setShort("seedStrip", i, (short) cluslist.get(i).getSeedStrip().getStrip());
            bank.setFloat("centroid", i,  (float) cluslist.get(i).getCentroid());
            if(cluslist.get(i).getType()==BMTType.C) {
                bank.setFloat("centroidValue", i,  (float) cluslist.get(i).getCentroidValue()/10);
                bank.setFloat("centroidError", i,  (float) cluslist.get(i).getCentroidError()/10);
            }
            else {
                bank.setFloat("centroidValue", i,  (float) cluslist.get(i).getCentroidValue());
                bank.setFloat("centroidError", i,  (float) cluslist.get(i).getCentroidError());                
            }
            bank.setFloat("centroidResidual", i,  (float) cluslist.get(i).getCentroidResidual()/10);
            bank.setFloat("seedResidual", i,  (float) cluslist.get(i).getSeedResidual()/10); 
            bank.setFloat("seedE", i,  (float) cluslist.get(i).getSeedStrip().getEdep());
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
            bank.setFloat("x1",   i,  (float)cluslist.get(i).origin().x()/10);
            bank.setFloat("y1",   i,  (float)cluslist.get(i).origin().y()/10);
            bank.setFloat("z1",   i,  (float)cluslist.get(i).origin().z()/10);
            bank.setFloat("x2",   i,  (float)cluslist.get(i).end().x()/10);
            bank.setFloat("y2",   i,  (float)cluslist.get(i).end().y()/10);
            bank.setFloat("z2",   i,  (float)cluslist.get(i).end().z()/10);
            bank.setFloat("cx",   i,  (float)cluslist.get(i).center().x()/10);
            bank.setFloat("cy",   i,  (float)cluslist.get(i).center().y()/10);
            bank.setFloat("cz",   i,  (float)cluslist.get(i).center().z()/10);
            bank.setFloat("theta",i,  (float)cluslist.get(i).theta());
            bank.setFloat("ax1",  i,  (float)cluslist.get(i).getAxis().origin().x()/10);
            bank.setFloat("ay1",  i,  (float)cluslist.get(i).getAxis().origin().y()/10);
            bank.setFloat("az1",  i,  (float)cluslist.get(i).getAxis().origin().z()/10);
            bank.setFloat("ax2",  i,  (float)cluslist.get(i).getAxis().end().x()/10);
            bank.setFloat("ay2",  i,  (float)cluslist.get(i).getAxis().end().y()/10);
            bank.setFloat("az2",  i,  (float)cluslist.get(i).getAxis().end().z()/10);
            bank.setFloat("lx",   i,  (float)cluslist.get(i).getL().x());
            bank.setFloat("ly",   i,  (float)cluslist.get(i).getL().y());
            bank.setFloat("lz",   i,  (float)cluslist.get(i).getL().z());
            bank.setFloat("sx",   i,  (float)cluslist.get(i).getS().x());
            bank.setFloat("sy",   i,  (float)cluslist.get(i).getS().y());
            bank.setFloat("sz",   i,  (float)cluslist.get(i).getS().z());
            bank.setFloat("nx",   i,  (float)cluslist.get(i).getN().x());
            bank.setFloat("ny",   i,  (float)cluslist.get(i).getN().y());
            bank.setFloat("nz",   i,  (float)cluslist.get(i).getN().z());
            bank.setFloat("e",    i,  (float)cluslist.get(i).getResolution()/10);
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
     * @param bankName
     * @return 
     */
    public static DataBank fillBMTCrossBank(DataEvent event, List<Cross> crosses, String bankName) {
        if (crosses == null || crosses.isEmpty()) return null;

        DataBank bank = event.createBank(bankName, crosses.size());

        for (int j = 0; j < crosses.size(); j++) {
            bank.setShort("ID", j, (short) crosses.get(j).getId());
            bank.setByte("sector", j, (byte) crosses.get(j).getSector());
            bank.setByte("region", j, (byte) crosses.get(j).getRegion());
            bank.setFloat("x", j,  (float) (crosses.get(j).getPoint().x()/10.));
            bank.setFloat("y", j,  (float) (crosses.get(j).getPoint().y()/10.));
            bank.setFloat("z", j,  (float) (crosses.get(j).getPoint().z()/10));
            bank.setFloat("err_x", j,  (float) (crosses.get(j).getPointErr().x()/10.));
            bank.setFloat("err_y", j,  (float) (crosses.get(j).getPointErr().y()/10.));
            bank.setFloat("err_z", j,  (float) (crosses.get(j).getPointErr().z()/10.));
            bank.setFloat("x0", j,  (float) (crosses.get(j).getPoint0().x()/10.));
            bank.setFloat("y0", j,  (float) (crosses.get(j).getPoint0().y()/10.));
            bank.setFloat("z0", j,  (float) (crosses.get(j).getPoint0().z()/10));
            bank.setFloat("err_x0", j,  (float) (crosses.get(j).getPointErr0().x()/10.));
            bank.setFloat("err_y0", j,  (float) (crosses.get(j).getPointErr0().y()/10.));
            bank.setFloat("err_z0", j,  (float) (crosses.get(j).getPointErr0().z()/10.));
            bank.setShort("trkID", j, (short) crosses.get(j).getAssociatedTrackID());
           
            if (crosses.get(j).getDir() != null && 
                    !Double.isNaN(crosses.get(j).getDir().x()) &&
                    !Double.isNaN(crosses.get(j).getDir().y()) &&
                    !Double.isNaN(crosses.get(j).getDir().z()) ) {
                bank.setFloat("ux", j,  (float) crosses.get(j).getDir().x());
                bank.setFloat("uy", j,  (float) crosses.get(j).getDir().y());
                bank.setFloat("uz", j,  (float) crosses.get(j).getDir().z());
            } else {
                bank.setFloat("ux", j, 0);
                bank.setFloat("uy", j, 0);
                bank.setFloat("uz", j, 0);
            }
            if (crosses.get(j).getCluster1() != null) {
                bank.setShort("Cluster1_ID", j, (short) crosses.get(j).getCluster1().getId());
                bank.setByte("layer",  j, (byte) crosses.get(j).getCluster1().getLayer());
            }
            if (crosses.get(j).getCluster2() != null) {
                bank.setShort("Cluster2_ID", j, (short) crosses.get(j).getCluster2().getId());
            }
        }

        return bank;

    }

    public static DataBank fillSeedBank(DataEvent event, List<Seed> seeds, String bankName) {
        if (seeds == null || seeds.isEmpty()) return null;

        DataBank bank = event.createBank(bankName, seeds.size());
        
        for (int i = 0; i < seeds.size(); i++) {
            if(seeds.get(i)==null)
                continue;
            bank.setByte("fittingMethod", i, (byte) seeds.get(i).getStatus());
            bank.setShort("ID", i, (short) seeds.get(i).getId());
            Helix helix = seeds.get(i).getHelix();
            bank.setByte("q", i, (byte) (Math.signum(Constants.getSolenoidScale())*helix.getCharge()));
            bank.setFloat("p", i,  (float) helix.getPXYZ(seeds.get(i).getHelix().B).mag());
            bank.setFloat("pt", i,  (float) helix.getPt(seeds.get(i).getHelix().B));
            bank.setFloat("phi0", i,  (float) helix.getPhiAtDCA());
            bank.setFloat("tandip", i,  (float) helix.getTanDip());
            bank.setFloat("z0", i,  (float) (helix.getZ0()/10.0));
            bank.setFloat("d0", i,  (float) (helix.getDCA()/10.0));
            double[][] covmatrix = helix.getCovMatrix();
            if (covmatrix != null) {
                bank.setFloat("cov_d02", i,  (float) covmatrix[0][0]/10/10 );
                bank.setFloat("cov_d0phi0", i,  (float) covmatrix[0][1]/10 );
                bank.setFloat("cov_d0rho", i,  (float) covmatrix[0][2] );
                bank.setFloat("cov_phi02", i,  (float) covmatrix[1][1] );
                bank.setFloat("cov_phi0rho", i,  (float) covmatrix[1][2]*10 );
                bank.setFloat("cov_rho2", i,  (float) covmatrix[2][2]*10*10 );
                bank.setFloat("cov_z02", i,  (float) covmatrix[3][3]/10/10 );
                bank.setFloat("cov_z0tandip", i,  (float) covmatrix[3][4]/10 );
                bank.setFloat("cov_tandip2", i,  (float) covmatrix[4][4] );
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
            bank.setFloat("xb", i,  (float) (helix.getXb()/10.0));
            bank.setFloat("yb", i,  (float) (helix.getYb()/10.0));
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
            bank.setFloat("circlefit_chi2_per_ndf", i,  (float) seeds.get(i).getCircleFitChi2PerNDF());
            bank.setFloat("linefit_chi2_per_ndf", i,  (float) seeds.get(i).getLineFitChi2PerNDF());
            bank.setFloat("chi2", i,  (float) seeds.get(i).getChi2());
            bank.setShort("ndf", i, (short) seeds.get(i).getNDF());

        }
        //bank.show();
        return bank;

    }

    /**
     * 
     * @param event the event
     * @param trkcands the list of reconstructed helical tracks
     * @param bankName
     * @return 
     */
    public static DataBank fillTrackBank(DataEvent event, List<Track> trkcands, String bankName) {
        if (trkcands == null || trkcands.isEmpty()) return null;

        DataBank bank = event.createBank(bankName, trkcands.size());
        
        // an array representing the ids of the crosses that belong to the track
        for (int i = 0; i < trkcands.size(); i++) {
            if(trkcands.get(i)==null)
                continue;
            bank.setByte("fittingMethod", i, (byte) trkcands.get(i).getSeed().getStatus());
            bank.setShort("ID", i, (short) trkcands.get(i).getId());
            bank.setByte("q", i, (byte)trkcands.get(i).getQ());
            bank.setFloat("p", i,  (float) trkcands.get(i).getP());
            bank.setFloat("pt", i,  (float) trkcands.get(i).getPt());
            Helix helix = trkcands.get(i).getHelix();
            bank.setFloat("phi0", i,  (float) helix.getPhiAtDCA());
            bank.setFloat("tandip", i,  (float) helix.getTanDip());
            bank.setFloat("z0", i,  (float) (helix.getZ0()/10.));
            bank.setFloat("d0", i,  (float) (helix.getDCA()/10.));
            bank.setFloat("xb", i,  (float) (helix.getXb()/10.0));
            bank.setFloat("yb", i,  (float) (helix.getYb()/10.0));
            // this is the format of the covariance matrix for helical tracks
            // cov matrix = 
            // | d_dca*d_dca                   d_dca*d_phi_at_dca            d_dca*d_curvature        0            0             |
            // | d_phi_at_dca*d_dca     d_phi_at_dca*d_phi_at_dca     d_phi_at_dca*d_curvature        0            0             |
            // | d_curvature*d_dca	    d_curvature*d_phi_at_dca      d_curvature*d_curvature         0            0             |
            // | 0                              0                             0                    d_Z0*d_Z0                     |
            // | 0                              0                             0                       0        d_tandip*d_tandip |X
            double[][] covmatrix = helix.getCovMatrix();
            if (covmatrix != null) {
                bank.setFloat("cov_d02", i,  (float) covmatrix[0][0]/10/10 );
                bank.setFloat("cov_d0phi0", i,  (float) covmatrix[0][1]/10 );
                bank.setFloat("cov_d0rho", i,  (float) covmatrix[0][2] );
                bank.setFloat("cov_phi02", i,  (float) covmatrix[1][1] );
                bank.setFloat("cov_phi0rho", i,  (float) covmatrix[1][2]*10 );
                bank.setFloat("cov_rho2", i,  (float) covmatrix[2][2]*10*10 );
                bank.setFloat("cov_z02", i,  (float) covmatrix[3][3]/10/10 );
                bank.setFloat("cov_z0tandip", i,  (float) covmatrix[3][4]/10 );
                bank.setFloat("cov_tandip2", i,  (float) covmatrix[4][4] );
            } else {
                bank.setFloat("cov_d02", i, -999);
                bank.setFloat("cov_d0phi0", i, -999);
                bank.setFloat("cov_d0rho", i, -999);
                bank.setFloat("cov_phi02", i, -999);
                bank.setFloat("cov_phi0rho", i, -999);
                bank.setFloat("cov_rho2", i, -999);
                bank.setFloat("cov_z02", i, -999);
                bank.setFloat("cov_z0tandip", i, -999);
                bank.setFloat("cov_tandip2", i, -999);
            }
            if(trkcands.get(i).getTrackPosAtCTOF()!=null) {
                bank.setFloat("c_x", i,  (float) (trkcands.get(i).getTrackPosAtCTOF().x() / 10.)); // convert to cm
                bank.setFloat("c_y", i,  (float) (trkcands.get(i).getTrackPosAtCTOF().y() / 10.)); // convert to cm
                bank.setFloat("c_z", i,  (float) (trkcands.get(i).getTrackPosAtCTOF().z() / 10.)); // convert to cm
                bank.setFloat("c_ux", i,  (float) trkcands.get(i).getTrackDirAtCTOF().x());
                bank.setFloat("c_uy", i,  (float) trkcands.get(i).getTrackDirAtCTOF().y());
                bank.setFloat("c_uz", i,  (float) trkcands.get(i).getTrackDirAtCTOF().z());
                bank.setFloat("pathlength", i,  (float) (trkcands.get(i).getPathToCTOF() / 10.)); // conversion to cm
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
            bank.setShort("seedID", i, (short) trkcands.get(i).getSeed().getId());
            bank.setFloat("chi2", i,  (float) trkcands.get(i).getChi2());
            bank.setShort("ndf", i, (short) trkcands.get(i).getNDF());
            bank.setInt("pid", i, trkcands.get(i).getPID());
        }
        //bank.show();
        return bank;

    }
    
    public static DataBank fillUTrackBank(DataEvent event, List<Track> trkcands, String bankName) {
        if (trkcands == null || trkcands.isEmpty()) return null;

        DataBank bank = event.createBank(bankName, trkcands.size());
        
        // an array representing the ids of the crosses that belong to the track
        for (int i = 0; i < trkcands.size(); i++) {
            if(trkcands.get(i)==null)
                continue;
            Helix helix = trkcands.get(i).getSecondaryHelix();
            bank.setShort("ID", i, (short) trkcands.get(i).getId());
            bank.setByte("fittingMethod", i, (byte) trkcands.get(i).getSeed().getStatus());
            bank.setByte("q", i, (byte)trkcands.get(i).getQ());
            bank.setFloat("p", i,  (float) helix.getPXYZ(Constants.getSolenoidMagnitude()).mag());
            bank.setFloat("pt", i,  (float) helix.getPt(Constants.getSolenoidMagnitude()));
            bank.setFloat("phi0", i,  (float) helix.getPhiAtDCA());
            bank.setFloat("tandip", i,  (float) helix.getTanDip());
            bank.setFloat("z0", i,  (float) (helix.getZ0()/10.));
            bank.setFloat("d0", i,  (float) (helix.getDCA()/10.));
            bank.setFloat("xb", i,  (float) (helix.getXb()/10.0));
            bank.setFloat("yb", i,  (float) (helix.getYb()/10.0));
            // this is the format of the covariance matrix for helical tracks
            // cov matrix = 
            // | d_dca*d_dca                   d_dca*d_phi_at_dca            d_dca*d_curvature        0            0             |
            // | d_phi_at_dca*d_dca     d_phi_at_dca*d_phi_at_dca     d_phi_at_dca*d_curvature        0            0             |
            // | d_curvature*d_dca	    d_curvature*d_phi_at_dca      d_curvature*d_curvature         0            0             |
            // | 0                              0                             0                    d_Z0*d_Z0                     |
            // | 0                              0                             0                       0        d_tandip*d_tandip |X
            double[][] covmatrix = helix.getCovMatrix();
            if (covmatrix != null) {
                bank.setFloat("cov_d02", i,  (float) covmatrix[0][0]/10/10 );
                bank.setFloat("cov_d0phi0", i,  (float) covmatrix[0][1]/10 );
                bank.setFloat("cov_d0rho", i,  (float) covmatrix[0][2] );
                bank.setFloat("cov_phi02", i,  (float) covmatrix[1][1] );
                bank.setFloat("cov_phi0rho", i,  (float) covmatrix[1][2]*10 );
                bank.setFloat("cov_rho2", i,  (float) covmatrix[2][2]*10*10 );
                bank.setFloat("cov_z02", i,  (float) covmatrix[3][3]/10/10 );
                bank.setFloat("cov_z0tandip", i,  (float) covmatrix[3][4]/10 );
                bank.setFloat("cov_tandip2", i,  (float) covmatrix[4][4] );
            } else {
                bank.setFloat("cov_d02", i, -999);
                bank.setFloat("cov_d0phi0", i, -999);
                bank.setFloat("cov_d0rho", i, -999);
                bank.setFloat("cov_phi02", i, -999);
                bank.setFloat("cov_phi0rho", i, -999);
                bank.setFloat("cov_rho2", i, -999);
                bank.setFloat("cov_z02", i, -999);
                bank.setFloat("cov_z0tandip", i, -999);
                bank.setFloat("cov_tandip2", i, -999);
            }
            bank.setShort("status", i, (short) ((short) trkcands.get(i).getStatus()));
            bank.setShort("seedID", i, (short) trkcands.get(i).getSeed().getId());
            bank.setFloat("chi2", i,  (float) trkcands.get(i).getChi2());
            bank.setShort("ndf", i, (short) trkcands.get(i).getNDF());
            bank.setInt("pid", i, trkcands.get(i).getPID());
        }
        //bank.show();
        return bank;

    }
    
    public static DataBank fillTrackCovMatBank(DataEvent event, List<Track> trkcands, String bankName) {
        if (trkcands == null || trkcands.isEmpty()) return null;

        DataBank bank = event.createBank(bankName, trkcands.size());
        // an array representing the ids of the crosses that belong to the track

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
                        bank.setFloat(names[r][c], i,  (float) covmatrix[r][c] );
                    }
                }
            }
        }
        
        //bank.show();
        return bank;

    }
    
    public static DataBank fillTrajectoryBank(DataEvent event, List<Track> trks, String bankName) {
        if (trks == null || trks.isEmpty()) return null;
        
        int bankSize = 0;
        for (int i = 0; i < trks.size(); i++) {
            if(trks.get(i)==null)
                continue;
            if (trks.get(i).getTrajectory() == null) {
                continue;
            }
            bankSize += trks.get(i).getTrajectory().size();
        }

        if(bankSize==0) return null;
        
        DataBank bank = event.createBank(bankName, bankSize); //  SVT layers +  BMT layers 

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
                bank.setFloat("x",        k,  (float) (stVec.x()/10.));
                bank.setFloat("y",        k,  (float) (stVec.y()/10.));
                bank.setFloat("z",        k,  (float) (stVec.z()/10.));
                bank.setFloat("cx",       k,  (float) (stVec.ux()));
                bank.setFloat("cy",       k,  (float) (stVec.uy()));
                bank.setFloat("cz",       k,  (float) (stVec.uz()));
                bank.setFloat("p",        k,  (float) (stVec.getP()));
                bank.setFloat("phi",      k,  (float) stVec.getTrkPhiAtSurface());
                bank.setFloat("theta",    k,  (float) stVec.getTrkThetaAtSurface());
                bank.setFloat("langle",   k,  (float) stVec.getTrkToModuleAngle());
                bank.setFloat("centroid", k,  (float) stVec.getCalcCentroidStrip());
                bank.setFloat("path",     k,  (float) stVec.getPath()/10);
                bank.setFloat("dx",       k,  (float) stVec.getDx()/10);
                k++;

            }
        }

        return bank;
    }

    public static DataBank fillKFTrajectoryBank(DataEvent event, List<Track> trkcands, String bankName) {
        if (trkcands == null || trkcands.isEmpty()) {
            return null;
        }
        //new trajectory
        int k = 0;
        for (int i = 0; i < trkcands.size(); i++) {
            if (trkcands.get(i).getKFTrajectories() != null) {
                k += trkcands.get(i).getKFTrajectories().keySet().size();
            }
        }
        DataBank bank = event.createBank(bankName, k);
        k = 0;
        for (int i = 0; i < trkcands.size(); i++) {
            if(trkcands.get(i).getKFTrajectories()==null)
                continue;
            //Fill trajectory for Eloss debugging
            for (int index : trkcands.get(i).getKFTrajectories().keySet()) {
                HitOnTrack t = trkcands.get(i).getKFTrajectories().get(index);
                bank.setShort("id",             k, (short) trkcands.get(i).getId()); //trackid
                bank.setByte("detector",        k, (byte)  MLayer.getDetectorType(index).getDetectorId());
                bank.setByte("layer",           k, (byte)  MLayer.getType(index).getLayer());
                bank.setByte("index",           k, (byte)  index);
                bank.setFloat("x",              k, (float) (t.x/10.));
                bank.setFloat("y",              k, (float) (t.y/10.));
                bank.setFloat("z",              k, (float) (t.z/10.));
                bank.setFloat("px",             k, (float) (t.px));
                bank.setFloat("py",             k, (float) (t.py));
                bank.setFloat("pz",             k, (float) (t.pz));
                bank.setFloat("path",           k, (float) (t.path));
                bank.setFloat("dx",             k, (float) (t.dx));
                bank.setFloat("dE",             k, (float) (t.dE));
                bank.setFloat("trackRes",       k, (float) (t.residual));
                bank.setFloat("transportedRes", k, (float) (t.transportedResidual));
                bank.setFloat("filteredRes",    k, (float) (t.filteredResidual));
                bank.setFloat("smoothedRes",    k, (float) (t.smoothedResidual));
                k++;
            }
        }
        //bank.show();
        return bank;
    }
     
    public static DataBank fillStraightSeedsBank(DataEvent event, List<StraightTrack> seed, String bankName) {
        if (seed == null || seed.isEmpty()) {
            return null;
        }
        
        DataBank bank = event.createBank(bankName, seed.size());
        // an array representing the ids of the crosses that belong to the track: for a helical track with the current
        // 4 regions of SVT + 1 region of BMT there can be up to 4*2 (*2: for each hemisphere) crosses of type SVT and 2*2 of type PSEUDOBMT (1 for the C detector and 1 for the Z detector)

        for (int i = 0; i < seed.size(); i++) {
            List<Integer> crossIdxArray = new ArrayList<>();


            bank.setShort("ID", i, (short) seed.get(i).getId());
            bank.setFloat("chi2", i, (float) seed.get(i).getChi2());
            bank.setShort("ndf", i, (short) (seed.get(i).size()-2));
            bank.setFloat("trkline_yx_slope", i, (float) seed.get(i).getRay().getYXSlope());
            bank.setFloat("trkline_yx_interc", i, (float) (seed.get(i).getRay().getYXInterc()/10.));
            bank.setFloat("trkline_yz_slope", i, (float) seed.get(i).getRay().getYZSlope());
            bank.setFloat("trkline_yz_interc", i, (float) (seed.get(i).getRay().getYZInterc()/10.));

            // get the cosmics ray unit direction vector
            Vector3D u = new Vector3D(seed.get(i).getRay().getYXSlope(), 1, seed.get(i).getRay().getYZSlope()).asUnit();
            // calculate the theta and phi components of the ray direction vector in degrees
            bank.setFloat("theta", i, (float) Math.toDegrees(u.theta()));
            bank.setFloat("phi", i, (float) Math.toDegrees(u.phi()));
            
            double[][] covmatrix = seed.get(i).getCovMat();
            if(covmatrix!=null) {
                bank.setFloat("cov_x02",  i, (float) (covmatrix[0][0]/10/10));
                bank.setFloat("cov_x0z0", i, (float) (covmatrix[0][1]/10/10));
                bank.setFloat("cov_x0tx", i, (float) (covmatrix[0][2]/10));
                bank.setFloat("cov_x0tz", i, (float) (covmatrix[0][3]/10));
                bank.setFloat("cov_z02",  i, (float) (covmatrix[1][1]/10/10));
                bank.setFloat("cov_z0tx", i, (float) (covmatrix[1][2]/10));
                bank.setFloat("cov_z0tz", i, (float) (covmatrix[1][3]/10));
                bank.setFloat("cov_tx2",  i, (float) (covmatrix[2][2]));
                bank.setFloat("cov_txtz", i, (float) (covmatrix[2][3]));
                bank.setFloat("cov_tz2",  i, (float) (covmatrix[3][3]));
            }
            else {
                bank.setFloat("cov_x02",  i, -999);
                bank.setFloat("cov_x0z0", i, -999);
                bank.setFloat("cov_x0tx", i, -999);
                bank.setFloat("cov_x0tz", i, -999);
                bank.setFloat("cov_z02",  i, -999);
                bank.setFloat("cov_z0tx", i, -999);
                bank.setFloat("cov_z0tz", i, -999);
                bank.setFloat("cov_tx2",  i, -999);
                bank.setFloat("cov_txtz", i, -999);
                bank.setFloat("cov_tz2",  i, -999);                
            }
            // the array of cross ids is filled in order of the SVT cosmic region 1 to 8 starting from the bottom-most double layer
            for (int j = 0; j < seed.get(i).size(); j++) {
                crossIdxArray.add(seed.get(i).get(j).getId());
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

    public static DataBank fillStraightTracksBank(DataEvent event, List<StraightTrack> cosmics, String bankName) {
        if (cosmics == null || cosmics.isEmpty()) {
            return null;
        }
        

        DataBank bank = event.createBank(bankName, cosmics.size());
        // an array representing the ids of the crosses that belong to the track: for a helical track with the current
        // 4 regions of SVT + 1 region of BMT there can be up to 4*2 (*2: for each hemisphere) crosses of type SVT and 2*2 of type PSEUDOBMT (1 for the C detector and 1 for the Z detector)

        for (int i = 0; i < cosmics.size(); i++) {
            List<Integer> crossIdxArray = new ArrayList<>();


            bank.setShort("ID", i, (short) cosmics.get(i).getId());
            bank.setFloat("chi2", i, (float) cosmics.get(i).getChi2());
            bank.setShort("ndf", i, (short) (cosmics.get(i).getNDF()));
            bank.setShort("status", i, (short) cosmics.get(i).getStatus());
            bank.setFloat("trkline_yx_slope", i, (float) cosmics.get(i).getRay().getYXSlope());
            bank.setFloat("trkline_yx_interc", i, (float) (cosmics.get(i).getRay().getYXInterc()/10.));
            bank.setFloat("trkline_yz_slope", i, (float) cosmics.get(i).getRay().getYZSlope());
            bank.setFloat("trkline_yz_interc", i, (float) (cosmics.get(i).getRay().getYZInterc()/10.));

            // get the cosmics ray unit direction vector
            Vector3D u = new Vector3D(cosmics.get(i).getRay().getYXSlope(), 1, cosmics.get(i).getRay().getYZSlope()).asUnit();
            // calculate the theta and phi components of the ray direction vector in degrees
            bank.setFloat("theta", i, (float) Math.toDegrees(u.theta()));
            bank.setFloat("phi", i, (float) Math.toDegrees(u.phi()));
            
            double[][] covmatrix = cosmics.get(i).getCovMat();
            if(covmatrix!=null) {
                bank.setFloat("cov_x02",  i, (float) (covmatrix[0][0]/10/10));
                bank.setFloat("cov_x0z0", i, (float) (covmatrix[0][1]/10/10));
                bank.setFloat("cov_x0tx", i, (float) (covmatrix[0][2]/10));
                bank.setFloat("cov_x0tz", i, (float) (covmatrix[0][3]/10));
                bank.setFloat("cov_z02",  i, (float) (covmatrix[1][1]/10/10));
                bank.setFloat("cov_z0tx", i, (float) (covmatrix[1][2]/10));
                bank.setFloat("cov_z0tz", i, (float) (covmatrix[1][3]/10));
                bank.setFloat("cov_tx2",  i, (float) (covmatrix[2][2]));
                bank.setFloat("cov_txtz", i, (float) (covmatrix[2][3]));
                bank.setFloat("cov_tz2",  i, (float) (covmatrix[3][3]));
            }
            else {
                bank.setFloat("cov_x02",  i, -999);
                bank.setFloat("cov_x0z0", i, -999);
                bank.setFloat("cov_x0tx", i, -999);
                bank.setFloat("cov_x0tz", i, -999);
                bank.setFloat("cov_z02",  i, -999);
                bank.setFloat("cov_z0tx", i, -999);
                bank.setFloat("cov_z0tz", i, -999);
                bank.setFloat("cov_tx2",  i, -999);
                bank.setFloat("cov_txtz", i, -999);
                bank.setFloat("cov_tz2",  i, -999);                
            }
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

    public static DataBank fillStraightTracksTrajectoryBank(DataEvent event, List<StraightTrack> trks, String bankName) {
        if (trks == null || trks.isEmpty()) {
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
        DataBank bank = event.createBank(bankName, k);

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
                bank.setFloat("x",        k,  (float) (stVec.x()/10.));
                bank.setFloat("y",        k,  (float) (stVec.y()/10.));
                bank.setFloat("z",        k,  (float) (stVec.z()/10.));
                bank.setFloat("phi",      k,  (float) stVec.getTrkPhiAtSurface());
                bank.setFloat("theta",    k,  (float) stVec.getTrkThetaAtSurface());
                bank.setFloat("langle",   k,  (float) stVec.getTrkToModuleAngle());
                bank.setFloat("centroid", k,  (float) stVec.getCalcCentroidStrip());
                bank.setFloat("path",     k,  (float) stVec.getPath()/10);
                k++;

            }
        }
        //bank.show();
        return bank;
    }

    public static DataBank fillStraightTrackKFTrajectoryBank(DataEvent event, List<StraightTrack> tracks, String bankName) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        //new trajectory
        int k = 0;
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getTrajectories() != null) {
                k += tracks.get(i).getTrajectories().keySet().size();
            }
        }
        DataBank bank = event.createBank(bankName, k);
        for (int i = 0; i < tracks.size(); i++) {
            if(tracks.get(i).getTrajectories()==null)
                continue;
            //Fill trajectory for Eloss debugging
            k = 0;
            for (AKFitter.HitOnTrack t : tracks.get(i).getTrajectories().values()) {
                bank.setShort("id",             k, (short) tracks.get(i).getId()); //trackid
                bank.setByte("detector",        k, (byte)  MLayer.getDetectorType(t.layer).getDetectorId());
                bank.setByte("layer",           k, (byte)  MLayer.getType(t.layer).getLayer());
                bank.setByte("index",           k, (byte)  t.layer);
                bank.setFloat("x",              k, (float) (t.x/10.));
                bank.setFloat("y",              k, (float) (t.y/10.));
                bank.setFloat("z",              k, (float) (t.z/10.));
                bank.setFloat("px",             k, (float) (t.px));
                bank.setFloat("py",             k, (float) (t.py));
                bank.setFloat("pz",             k, (float) (t.pz));
                bank.setFloat("trackRes",       k, (float) (t.residual));
                bank.setFloat("transportedRes", k, (float) (t.transportedResidual));
                bank.setFloat("filteredRes",    k, (float) (t.filteredResidual));
                bank.setFloat("smoothedRes",    k, (float) (t.smoothedResidual));
                k++;
            }
        }
        //bank.show();
        return bank;
    }
     
    public static void appendCVTBanks(DataEvent event,
            List<Hit> sVThits, List<Hit> bMThits,
            List<Cluster> sVTclusters, List<Cluster> bMTclusters,
            List<ArrayList<Cross>> crosses, List<Seed> seeds, List<Track> tracks) {
        
        List<DataBank> banks = new ArrayList<>();

        DataBank bank1 = fillSVTHitBank(event, sVThits, "BSTRec::Hits");
        if (bank1 != null) banks.add(bank1);

        DataBank bank2 = fillBMTHitBank(event, bMThits, "BMTRec::Hits");
        if (bank2 != null) banks.add(bank2);
        
        DataBank bank3 = fillSVTClusterBank(event, sVTclusters, "BSTRec::Clusters");
        if (bank3 != null) banks.add(bank3);

        DataBank bank4 = fillBMTClusterBank(event, bMTclusters, "BMTRec::Clusters");
        if (bank4 != null) banks.add(bank4);

        DataBank bank5 = fillSVTCrossBank(event, crosses.get(0), "BSTRec::Crosses");
        if (bank5 != null) banks.add(bank5);

        DataBank bank6 = fillBMTCrossBank(event, crosses.get(1), "BMTRec::Crosses");
        if (bank6 != null) banks.add(bank6);
        
        DataBank bank7 = fillSeedBank(event, seeds, "CVTRec::Seeds");
        if (bank7 != null) banks.add(bank7);
        
        DataBank bank8 = fillTrackBank(event, tracks, "CVTRec::Tracks");
        if (bank8 != null) banks.add(bank8);
        
        DataBank bank9 = fillTrackCovMatBank(event, tracks, "CVTRec::TrackCovMat");
        if (bank9 != null) banks.add(bank9);
        
        DataBank bank10 = fillTrajectoryBank(event, tracks, "CVTRec::Trajectory");
        if (bank10 != null) banks.add(bank10);
        
        DataBank bank11 = fillKFTrajectoryBank(event, tracks, "CVTRec::KFTrajectory");
        if (bank11 != null) banks.add(bank11);
        
        event.appendBanks(banks.toArray(new DataBank[0]));
    }

    public static void appendCVTCosmicsBanks(DataEvent event,
            List<Hit> sVThits, List<Hit> bMThits,
            List<Cluster> sVTclusters, List<Cluster> bMTclusters,
            List<ArrayList<Cross>> crosses, 
            List<StraightTrack> seeds, List<StraightTrack> trks) {
        List<DataBank> banks = new ArrayList<>();

        DataBank bank1 = fillSVTHitBank(event, sVThits, "BSTRec::Hits");
        if (bank1 != null) banks.add(bank1);

        DataBank bank2 = fillBMTHitBank(event, bMThits, "BMTRec::Hits");
        if (bank2 != null) banks.add(bank2);
        
        DataBank bank3 = fillSVTClusterBank(event, sVTclusters, "BSTRec::Clusters");
        if (bank3 != null) banks.add(bank3);

        DataBank bank4 = fillBMTClusterBank(event, bMTclusters, "BMTRec::Clusters");
        if (bank4 != null) banks.add(bank4);

        DataBank bank5 = fillSVTCrossBank(event, crosses.get(0), "BSTRec::Crosses");
        if (bank5 != null) banks.add(bank5);

        DataBank bank6 = fillBMTCrossBank(event, crosses.get(1), "BMTRec::Crosses");
        if (bank6 != null) banks.add(bank6);

        DataBank bank7 = fillStraightSeedsBank(event, seeds, "CVTRec::CosmicSeeds");
        if (bank7 != null) banks.add(bank7);

        DataBank bank8 = fillStraightTracksBank(event, trks, "CVTRec::Cosmics");
        if (bank8 != null) banks.add(bank8);

        DataBank bank9 = fillStraightTracksTrajectoryBank(event, trks, "CVTRec::Trajectory");
        if (bank9 != null) banks.add(bank9);

        DataBank bank10 = fillStraightTrackKFTrajectoryBank(event, trks, "CVTRec::KFTrajectory");
        if (bank10 != null) banks.add(bank10);
        
        event.appendBanks(banks.toArray(new DataBank[0]));
    }

}
