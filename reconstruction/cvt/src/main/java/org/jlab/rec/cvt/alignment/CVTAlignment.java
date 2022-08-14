package org.jlab.rec.cvt.alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.Trajectory;

import org.ejml.simple.SimpleMatrix;

/**
 * Service to build input information for CVT KFA alignment
 *
 * @author spaul
 *
 */
public class CVTAlignment extends ReconstructionEngine {

    private String fieldsConfig = "";
    private int run = -1;

    private boolean isSVTonly = false;
    private boolean svtTopBottomSep = true;
    private boolean isBMTonly = false;
    private boolean skipBMTC = false;
    private boolean includeBeamspot = false;

    private boolean isCosmics = false;
    private boolean curvedTracks = false;

    //total counts of clusters on each type of channel
    private int gCountBMTC = 0;
    private int gCountSVT  = 0;
    private int gCountBMTZ = 0;
    private int debugPrintEventCount = 0;

    private final boolean useDocaPhiZTandip=true;
    private final double minCosIncident = Math.cos(Math.toRadians(75));
    private final double spMax = 10;
    private int minClustersSVT  = 0;
    private int minClustersBMTC = 0;
    private int minClustersBMTZ = 0;    
    private double maxResidualCutSVT  = Double.MAX_VALUE;
    private double maxResidualCutBMTC = Double.MAX_VALUE;
    private double maxResidualCutBMTZ = Double.MAX_VALUE;
    private double maxDocaCut = 10;

    private int nAlignables;
    private int nAlignVars;
    private int orderTx;
    private int orderTy;
    private int orderTz;
    private int orderRx;
    private int orderRy;
    private int orderRz; 
    private String alignVars = "Tx Ty Tz Rx Ry Rz";

    private final static int NSVTSENSORS = 84;
    private final static double INDEXBEAMLINE = 102;        
    private final boolean useNewFillMatrices = true;

    private boolean debug = false;

    
    
    public CVTAlignment() {
        super("CVTAlignment", "spaul", "4.0");
    }


    public int getRun() {
        return run;
    }

    public void setRun(int run) {
        this.run = run;
    }

    public String getFieldsConfig() {
        return fieldsConfig;
    }

    public void setFieldsConfig(String fieldsConfig) {
        this.fieldsConfig = fieldsConfig;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        
        int runNum = event.getBank("RUN::config").getInt("run", 0);
        int eventNum = event.getBank("RUN::config").getInt("event", 0);

        if((debugPrintEventCount++) %1000==0)
            System.out.println("BMTC total clusters: " + gCountBMTC+"; BMTZ total clusters: " + gCountBMTZ+"; SVT total crosses: "+ gCountSVT);


        AlignmentBankReader reader = new AlignmentBankReader();
        List<? extends Trajectory> tracks;
        if(isCosmics) {
            tracks = reader.getCosmics(event);
        } 
        else {
            tracks = reader.getTracks(event);
        }
        if (tracks==null || tracks.size() > 2) return true;


        List<SimpleMatrix> Is = new ArrayList<>();
        List<SimpleMatrix> As = new ArrayList<>();
        List<SimpleMatrix> Bs = new ArrayList<>();
        List<SimpleMatrix> Vs = new ArrayList<>();
        List<SimpleMatrix> ms = new ArrayList<>();
        List<SimpleMatrix> cs = new ArrayList<>();
        List<SimpleMatrix> qs = new ArrayList<>();
        List<Integer> trackIDs = new ArrayList<>();


        tracksLoop : for (Trajectory track : tracks) {

            if(Math.abs(getDoca(track))>maxDocaCut)
                continue;

            int nCrossSVT = 0, nCrossBMT = 0;
            int countBMTZ = 0, countBMTC = 0;
            for(Cross c : track) {
                if (c.getDetector() == DetectorType.BST && !isBMTonly) {
                    nCrossSVT++;
                    gCountSVT++;
                }
                if (c.getDetector() == DetectorType.BMT && !isSVTonly) {
                    if (c.getCluster1().getType() != BMTType.C || !skipBMTC) {
                        nCrossBMT++;
                    }
                    if (c.getCluster1().getType() == BMTType.C) {
                        countBMTC++;
                        gCountBMTC++;
                    }
                    if (c.getCluster1().getType() == BMTType.Z) {
                        gCountBMTZ++;
                        countBMTZ++;
                    }
                    //System.out.println(c.getSector()+" "+c.getRegion() + " " + c.getCluster1().getCentroid()+" " + c.getId());
                }
                if (nCrossBMT > 12) {
                    System.out.println("Too many BMT crosses!");
                    return false;
                }
            }


            if(nCrossSVT*2<minClustersSVT)
                    continue;
            if(countBMTZ<minClustersBMTZ)
                    continue;
            if(countBMTC<minClustersBMTC)
                    continue;

            if(nCrossSVT+countBMTZ< 3) //no transverse degrees of freedom
                    continue;
            if(nCrossSVT+countBMTC< 3) //no transverse degrees of freedom
                    continue;


            Ray ray = track.getRay();
            if(ray == null) {
                ray = getRay(track.getHelix());
                if(Math.abs(track.getHelix().getCurvature())>0.001 && !curvedTracks) {
                    continue;
                }
            }

            int paramsFromBeamspot = (isCosmics || ! includeBeamspot ? 0:1);
            int cols = nAlignVars*((svtTopBottomSep ? 2*nCrossSVT : nCrossSVT) + nCrossBMT + paramsFromBeamspot);
            int rows = 2*nCrossSVT+nCrossBMT + paramsFromBeamspot;

            SimpleMatrix A = new SimpleMatrix(rows, cols);//not sure why there aren't 6 columns
            SimpleMatrix B = new SimpleMatrix(rows, 4);
            SimpleMatrix V = new SimpleMatrix(rows,rows);
            SimpleMatrix m = new SimpleMatrix(rows,1);
            SimpleMatrix c = new SimpleMatrix(rows,1);
            SimpleMatrix I = new SimpleMatrix(rows,1);
            SimpleMatrix q = new SimpleMatrix(4, 1); //track parameters, for plotting kinematic dependence.  Not used in KFA.  

            if (track.getHelix() == null) {
                track.setHelix(createHelixFromRay(track.getRay()));
            }

            q.set(0, 0, track.getHelix().getDCA());
            q.set(1, 0, track.getHelix().getPhiAtDCA());
            q.set(2, 0, track.getHelix().getZ0());
            q.set(3, 0, track.getHelix().getTanDip());

            if(debug) {
                System.out.println("track parameters");
                q.print();
            }

            int i = 0;
            if(!curvedTracks) { 
                for (Cross cross : track) {
                    if (useNewFillMatrices) {
                        if (cross.getDetector() == DetectorType.BST) {
                            Cluster cl1 = cross.getCluster1();
                            boolean ok = fillMatricesNew(i, ray, cl1, A, B, V, m, c, I, debug, false);
                            i++;
                            if (!ok) { //reject track if there's a cluster with really bad values.
                                if (debug) {
                                    System.out.println("rejecting track due to problem in an SVT layer");
                                }
                                continue tracksLoop;
                            }
                            Cluster cl2 = cross.getCluster2();
                            ok = fillMatricesNew(i, ray, cl2, A, B, V, m, c, I, debug, false);
                            i++;
                            if (!ok) { //reject track if there's a cluster with really bad values.
                                if (debug) {
                                    System.out.println("rejecting track due to problem in an SVT layer");
                                }
                                continue tracksLoop;
                            }
                        } else {
                            Cluster cl1 = cross.getCluster1();
                            boolean ok = true;
                            if (cl1.getType() == BMTType.Z || !skipBMTC) {
                                ok = fillMatricesNew(i, ray, cl1, A, B, V, m, c, I, this.debug, false);
                            }
                            i++;
                            if (!ok) { //reject track if there's a cluster with really bad values.
                                if (debug) {
                                    System.out.println("rejecting track due to problem in a BMT" + cl1.getType().name() + " layer");
                                }
                                continue tracksLoop;
                            }
                            //}
                        }
                        continue;
                    }

                }
                if (!isCosmics && includeBeamspot) {

                    //pseudo cluster for the beamspot
                    Cluster cl1 = new Cluster(null, null, 0, 0, 0);
                    cl1.setLine(new Line3D(track.getHelix().getXb(), track.getHelix().getYb(), -100, track.getHelix().getXb(), track.getHelix().getYb(), 100));

                    Vector3D n = ray.getDirVec();
                    Vector3D l = new Vector3D(0, 0, 1);
                    cl1.setN(n);
                    cl1.setL(l);
                    cl1.setS(n.cross(l));
                    cl1.setResolution(0.6);

                    fillMatricesNew(i, ray, cl1, A, B, V, m, c, I, this.debug, true);

                }
            } 
            else {
                Helix helix = track.getHelix();
                //curved tracks
                for (Cross cross : track) {
                    if (useNewFillMatrices) {
                        if (cross.getDetector() == DetectorType.BST) {
                            Cluster cl1 = cross.getCluster1();
                            boolean ok = fillMatricesNew(i, helix, cl1, A, B, V, m, c, I, debug, false);
                            i++;
                            if (!ok) { //reject track if there's a cluster with really bad values.
                                if (debug) {
                                    System.out.println("rejecting track due to problem in an SVT layer");
                                }
                                continue tracksLoop;
                            }
                            Cluster cl2 = cross.getCluster2();
                            ok = fillMatricesNew(i, helix, cl2, A, B, V, m, c, I, debug, false);
                            i++;
                            if (!ok) { //reject track if there's a cluster with really bad values.
                                if (debug) {
                                    System.out.println("rejecting track due to problem in an SVT layer");
                                }
                                continue tracksLoop;
                            }
                        } else {
                            Cluster cl1 = cross.getCluster1();
                            boolean ok = true;
                            if (cl1.getType() == BMTType.Z || !skipBMTC) {
                                ok = fillMatricesNew(i, helix, cl1, A, B, V, m, c, I, this.debug, false);
                            }
                            i++;
                            if (!ok) { //reject track if there's a cluster with really bad values.
                                if (debug) {
                                    System.out.println("rejecting track due to problem in a BMT" + cl1.getType().name() + " layer");
                                }
                                continue tracksLoop;
                            }
                            //}
                        }
                    }

                }
                if(!isCosmics && includeBeamspot) {

                    //pseudo cluster for the beamspot
                    Cluster cl1 = new Cluster(null, null, 0, 0, 0);
                    cl1.setLine(new Line3D(track.getHelix().getXb(), track.getHelix().getYb(), -100, track.getHelix().getXb(), track.getHelix().getYb(), 100));

                    Vector3D n = ray.getDirVec();
                    Vector3D l = new Vector3D(0,0,1);
                    cl1.setN(n);
                    cl1.setL(l);
                    cl1.setS(n.cross(l));
                    cl1.setResolution(0.6);

                    fillMatricesNew(i, helix, cl1, A,B,V,m,c,I, this.debug, true);


                }
            }
            As.add(A);
            Bs.add(B);
            Vs.add(V);
            ms.add(m);
            cs.add(c);
            Is.add(I);
            qs.add(q);
            trackIDs.add(track.getId());
        }

        //only include events that have tracks that will be used in alignment
        if(!As.isEmpty()) {
            AlignmentBankWriter writer = new AlignmentBankWriter();
            writer.write_Matrix(event, "I", Is);
            writer.write_Matrix(event, "A", As);
            writer.write_Matrix(event, "B", Bs);
            writer.write_Matrix(event, "V", Vs);
            writer.write_Matrix(event, "m", ms);
            writer.write_Matrix(event, "c", cs);
            writer.write_Matrix(event, "q", qs);
            fillMisc(event,runNum,eventNum,trackIDs,As,Bs,Vs,ms,cs,Is);
        }
        return true;

    }
    private Helix createHelixFromRay(Ray ray) {
        Vector3D u = ray.getDirVec();
        Vector3D xref = ray.getRefPoint().toVector3D();
        double phi = Math.atan2(u.y(),u.x());
        Vector3D uT = new Vector3D(Math.cos(phi), Math.sin(phi),0);
        Vector3D mscphi = new Vector3D(-Math.sin(phi), Math.cos(phi),0);
        double cosdip = Math.hypot(u.x(), u.y());
        double d = mscphi.dot(xref);
        double curvature = 0;
        double Z0 = xref.z()-u.z()*xref.dot(uT)/u.dot(uT);
        double tandip = u.z()/Math.hypot(u.x(), u.y());
        return new Helix(d, phi, curvature, Z0, tandip, 0,0);
    }


    private Ray getRay(Helix h) {

        double d = h.getDCA();
        double z = h.getZ0();
        double phi = h.getPhiAtDCA();
        double td = h.getTanDip();
        double cd = 1/Math.hypot(td, 1);
        double sd = td*cd;
        double xb = h.getXb();
        double yb = h.getYb();
        //Vector3D u = new Vector3D(-cd*Math.sin(phi), cd*Math.cos(phi), sd);
        //Point3D x = new Point3D(d*Math.cos(phi),d*Math.sin(phi), z);
        Vector3D u = new Vector3D(cd*Math.cos(phi), cd*Math.sin(phi), sd);


        Point3D x = new Point3D(-d*Math.sin(phi)+xb,d*Math.cos(phi)+yb, z);
        //Point3D x = new Point3D(-d*Math.sin(phi),d*Math.cos(phi), z);

        //System.out.println("xb yb from db" + xb + yb);
        //Point3D x = new Point3D(-d*Math.sin(phi),d*Math.cos(phi), z);
        //if(u.y() <0)
        //	u = u.multiply(-1);
        //x = x.toVector3D().add(u.multiply(-x.y()/u.y())).toPoint3D();
        Ray ray = new Ray(x, u);
        //System.out.println("doca " + d);
        //System.out.println("td " + td);

        return ray;
    }



    private double getDoca(Trajectory track) {
        if(track instanceof StraightTrack) {
                Ray ray = track.getRay();
                double intercept = ray.getYXInterc();
                double slope = ray.getYXSlope();
                return Math.abs(intercept)/Math.hypot(1, slope);
        } else return track.getHelix().getDCA();
    }

    private void fillMisc(DataEvent event, int runNum, int eventNum, List<Integer> trackIDs, 
                    List<SimpleMatrix> As, List<SimpleMatrix> Bs, List<SimpleMatrix> Vs, 
                    List<SimpleMatrix> ms, List<SimpleMatrix> cs, List<SimpleMatrix> is) {
            DataBank bank = event.createBank("Align::misc", trackIDs.size());
            for(int i = 0; i<trackIDs.size(); i++) {
                    bank.setInt("run", i, runNum);
                    bank.setInt("event", i, eventNum);
                    SimpleMatrix c = cs.get(i), m = ms.get(i), V = Vs.get(i);
                    if(V.determinant()!= 0)
                            bank.setFloat("chi2", i, (float)(m.minus(c)).transpose().mult(V.invert()).mult(m.minus(c)).get(0, 0));
                    else {
                            System.out.println("Error:  V is singular: ");
                            V.print();
                    }
                    bank.setShort("ndof", i, (short)(Vs.get(i).numRows()-4));
                    bank.setShort("track", i, (short)(int)trackIDs.get(i));
                    bank.setShort("nalignables", i, (short)this.nAlignables);
                    bank.setShort("nparameters", i, (short)this.nAlignVars);
            }

            event.appendBank(bank);
    }

    private boolean fillMatricesBeamspot(int i, Ray ray, SimpleMatrix A, SimpleMatrix B, SimpleMatrix V, 
                                         SimpleMatrix m, SimpleMatrix c, SimpleMatrix I, double xb, double yb){
            // a point along the beam
        Vector3D xref = ray.getRefPoint().toVector3D();
        //System.out.println("xref:  " + xref.toStlString());
        Vector3D u = ray.getDirVec();

        Vector3D e = new Vector3D(xb, yb, 0);
        Vector3D l = new Vector3D(0, 0, 1);

        //in this case 
        Vector3D n = new Vector3D(u.x(), u.y(), 0);
        n = n.asUnit();
        Vector3D s = l.cross(n);

        double udotn = u.dot(n);
        if (Math.abs(udotn) < minCosIncident) {
            return false;
        }
        double sdotu = s.dot(u);
        Vector3D extrap = xref.clone().add(u.multiply(n.dot(e.clone().sub(xref)) / udotn));

        //this should be about equal to the beam width
        double resolution = 0.5;

        V.set(i, i, Math.pow(resolution, 2));

        Vector3D sp = s.clone().sub(n.multiply(sdotu / udotn));
        if (sp.mag() > spMax) {  //this can only happen if the angle between the track and the normal is small
            //System.out.println("rejecting track");
            return false;
        }
        int index = nAlignables - 1;

        //System.out.println("i = " + i + "; rows = " + A.getRowDimension() + "; cols = " + + A.getColumnDimension());
        Vector3D dmdr = sp.cross(xref).sub(sp.cross(u).multiply(n.dot(xref.clone().sub(e)) / udotn));
        if (orderTx >= 0) {
            A.set(i, i * nAlignVars + orderTx, sp.x());
        }
        if (orderTy >= 0) {
            A.set(i, i * nAlignVars + orderTy, sp.y());
        }
        if (orderTz >= 0) {
            A.set(i, i * nAlignVars + orderTz, sp.z());
        }
        if (orderRx >= 0) {
            A.set(i, i * nAlignVars + orderRx, -dmdr.x());
        }
        if (orderRy >= 0) {
            A.set(i, i * nAlignVars + orderRy, -dmdr.y());
        }
        if (orderRz >= 0) {
            A.set(i, i * nAlignVars + orderRz, -dmdr.z());
        }

        I.set(i, 0, index);

        Vector3D dmdu = sp.multiply(e.clone().sub(xref).dot(n) / udotn);
        if (!this.useDocaPhiZTandip) {
            B.set(i, 0, sp.x());
            B.set(i, 1, sp.z());
            B.set(i, 2, dmdu.x());
            B.set(i, 3, dmdu.z());
        } else {

            double phi = Math.atan2(u.y(), u.x());
            Vector3D csphi = new Vector3D(Math.cos(phi), Math.sin(phi), 0);
            Vector3D mscphi = new Vector3D(-Math.sin(phi), Math.cos(phi), 0);
            double cosdip = Math.hypot(u.x(), u.y());
            double d = mscphi.dot(xref);
            B.set(i, 0, -sp.dot(mscphi));
            B.set(i, 1, -sp.dot(mscphi) * n.dot(e.clone().sub(xref)) * cosdip / udotn + sp.dot(csphi) * d);
            B.set(i, 2, -sp.z());
            B.set(i, 3, -sp.z() * n.dot(e.clone().sub(xref)) / udotn);

        }
        //dm.set(i,0, s.dot(e.minus(extrap)));

        double ci = s.dot(extrap);
        double mi = s.dot(e);

        c.set(i, 0, ci);
        m.set(i, 0, mi);

        return true;
    }


    private boolean fillMatricesNew(int i, Helix helix, Cluster cl, SimpleMatrix A, SimpleMatrix B, SimpleMatrix V, 
                                    SimpleMatrix m, SimpleMatrix c, SimpleMatrix I, boolean debug, boolean isBeamspot) {
        Vector3D u= helix.getTrackDirectionAtRadius(cl.getRadius());
        Point3D xref = helix.getPointAtRadius(cl.getRadius());
        Ray ray = new Ray(xref, u);
        return fillMatricesNew(i, ray, cl, A, B, V, m, c, I, debug, isBeamspot);
    }

    /**
     * generic method that uses any type of cluster.  
     * @param i
     * @param ray
     * @param cl
     * @param A
     * @param B
     * @param V
     * @param m
     * @param c
     * @param I
     * @param string 
     * @return
     */
    private boolean fillMatricesNew(int i, Ray ray, Cluster cl, SimpleMatrix A, SimpleMatrix B, SimpleMatrix V, 
                                   SimpleMatrix m, SimpleMatrix c, SimpleMatrix I, boolean debug, boolean isBeamspot) {
            int layer = cl.getLayer();
            int sector = cl.getSector();

        Vector3D l;
        Vector3D s;
        Vector3D n;

        DetectorType detector = cl.getDetector();
        BMTType bmtType = cl.getType();

        if (debug) {
            System.out.println("\n\nNew method " + detector + " layer " + layer + " sector " + sector);

        }
        Vector3D xref = ray.getRefPoint().toVector3D();
        Vector3D u = ray.getDirVec();

        Vector3D e = null;
        Vector3D extrap = null;
        if (detector == DetectorType.BST || (detector == DetectorType.BMT && bmtType == BMTType.Z) || isBeamspot) {
            l = cl.getL();
            s = cl.getS();
            n = cl.getN();
            
            e = cl.getLine().midpoint().toVector3D();

            double udotn = u.dot(n);
            extrap = xref.clone().add(u.multiply(n.dot(e.clone().sub(xref)) / udotn));
        } 
        else { // BMTC
            Vector3D a = cl.getArc().normal();

            if (debug) {
                System.out.println("a: " + a);
            }
            Vector3D cc = cl.getArc().center().toVector3D();
            Vector3D uT = perp(u, a);

            Vector3D tmp1 = perp(xref.clone().sub(cc), a);

            Vector3D endpoint = cl.getArc().origin().toVector3D();

            double R = perp(endpoint.clone().sub(cc), a).mag();
            if (debug) {
                System.out.println("center: " + cc.toStringBrief());
                System.out.println("R:  " + R);
            }
            double AA = uT.mag2();

            double BB = 2 * tmp1.dot(uT);
            double CC = tmp1.mag2() - R * R;
            double lambda_plus = (-BB + Math.sqrt(BB * BB - 4 * AA * CC)) / (2 * AA);
            double lambda_minus = (-BB - Math.sqrt(BB * BB - 4 * AA * CC)) / (2 * AA);
            Vector3D extrap_plus = xref.clone().add(u.multiply(lambda_plus));
            Vector3D extrap_minus = xref.clone().add(u.multiply(lambda_minus));

            if (debug) {
                System.out.println("extrap is on cylinder:  this should be zero: " + (perp(extrap_plus.clone().sub(cc), a).mag() - R));
            }

            //choose the extrapolated point that is closer in z to the measured cluster.  
            if (Math.abs(extrap_plus.clone().sub(cc).z()) < Math.abs(extrap_minus.clone().sub(cc).z())) {
                extrap = extrap_plus;
            } else {
                extrap = extrap_minus;
            }
            e = extrap.clone().add(endpoint.clone().sub(extrap).projection(a));
            s = a;
            n = perp(extrap.clone().sub(cc), a).asUnit();
            l = s.cross(n);
        }

        n = n.sub(l.multiply(n.dot(l))).asUnit();
        s = s.sub(l.multiply(s.dot(l))).asUnit();

        double udotn = u.dot(n);
        if (Math.abs(udotn) < minCosIncident) {
            if (debug) {
                System.out.println("rejecting track:  abs(udotn)<" + minCosIncident);
                System.out.println("u = " + u.toString());
                System.out.println("n = " + n.toString());
            }
            return false;
        }
        double sdotu = s.dot(u);

        if (debug) {
            System.out.println("e: " + e.toString());
        }

        if (detector == DetectorType.BMT && bmtType == BMTType.Z && debug) {
            Vector3D diff = xref.clone();
            double check = l.cross(u).dot(diff);
            System.out.println("distance between track and strip, phi,r: " + check + " " + u.phi() + " " + e.mag());
        }

        double resolution = cl.getResolution();

        V.set(i, i, Math.pow(resolution, 2));
        if (debug) {
            System.out.println("resolution " + resolution);
        }

        Vector3D sp = s.clone().sub(n.multiply(sdotu / udotn));
        if (sp.mag() > spMax) {  //this can only happen if the angle between the track and the normal is small
            if (debug) {
                System.out.println("rejecting track:  sp.magnitude() > " + spMax);
            }
            return false;
        }

        Vector3D dmdr = sp.cross(xref).sub(sp.cross(u).multiply(n.dot(xref.clone().sub(e)) / udotn));

        if (orderTx >= 0) {
            A.set(i, (svtTopBottomSep ? i : i / 2) * nAlignVars + orderTx, sp.x());
        }
        if (orderTy >= 0) {
            A.set(i, (svtTopBottomSep ? i : i / 2) * nAlignVars + orderTy, sp.y());
        }
        if (orderTz >= 0) {
            A.set(i, (svtTopBottomSep ? i : i / 2) * nAlignVars + orderTz, sp.z());
        }
        if (orderRx >= 0) {
            A.set(i, (svtTopBottomSep ? i : i / 2) * nAlignVars + orderRx, -dmdr.x());
        }
        if (orderRy >= 0) {
            A.set(i, (svtTopBottomSep ? i : i / 2) * nAlignVars + orderRy, -dmdr.y());
        }
        if (orderRz >= 0) {
            A.set(i, (svtTopBottomSep ? i : i / 2) * nAlignVars + orderRz, -dmdr.z());
        }

        if (detector == DetectorType.BST) {
            I.set(i, 0, getIndexSVT(layer - 1, sector - 1));
        } else if (detector == DetectorType.BMT) {
            I.set(i, 0, getIndexBMT(layer - 1, sector - 1));
        } else {
            I.set(i, 0, INDEXBEAMLINE);
        }
        Vector3D dmdu = sp.multiply(e.clone().sub(xref).dot(n) / udotn);
        if (!this.useDocaPhiZTandip) {
            B.set(i, 0, -sp.x());
            B.set(i, 1, -sp.z());
            B.set(i, 2, -dmdu.x());
            B.set(i, 3, -dmdu.z());
        } else {

            double phi = Math.atan2(u.y(), u.x());
            /*if(detector == DetectorType.BMT && bmtType==BMTType.C) {
                            System.out.println("phi is " + phi);
                    }*/
            Vector3D csphi = new Vector3D(Math.cos(phi), Math.sin(phi), 0);
            Vector3D mscphi = new Vector3D(-Math.sin(phi), Math.cos(phi), 0);
            double cosdip = Math.hypot(u.x(), u.y());
            double d = mscphi.dot(xref);
            B.set(i, 0, -sp.dot(mscphi));
            B.set(i, 1, -sp.dot(mscphi) * n.dot(e.clone().sub(xref)) * cosdip / udotn + sp.dot(csphi) * d);
            B.set(i, 2, -sp.z());
            B.set(i, 3, -sp.z() * n.dot(e.clone().sub(xref)) / udotn);

        }
        //dm.set(i,0, s.dot(e.minus(extrap)));

        double ci = s.dot(extrap);
        double mi = s.dot(e);

        c.set(i, 0, ci);
        m.set(i, 0, mi);
        if (debug) {
            System.out.println("n.(e-xref): " + n.dot(e.clone().sub(xref)));
            System.out.println("u.n: " + udotn);
            System.out.println("s: " + s.toString());
            System.out.println("sp: " + sp.toString());
            System.out.println("extrap: " + extrap.toString());
            System.out.println("n: " + n.toString());
            System.out.println("e: " + e.toString());
            System.out.println("xref: " + xref.toString());
            System.out.println("u: " + u.toString());
            System.out.println("m: " + mi);
            System.out.println("c: " + ci);
        }
        if (Math.abs(ci - mi) > maxResidualCutSVT && detector == DetectorType.BST
                || Math.abs(ci - mi) > maxResidualCutBMTZ && detector == DetectorType.BMT && bmtType == BMTType.Z
                || Math.abs(ci - mi) > maxResidualCutBMTC && detector == DetectorType.BMT && bmtType == BMTType.C) {
            if (debug) {
                System.out.println("rejecting track:  Math.abs(ci-mi)>maxResidualCut");
            }
            return false;
        }
        return true;
    }
    

    Vector3D perp(Vector3D v, Vector3D a) {
            return v.clone().sub(a.multiply(v.dot(a)));
    }

    private int getIndexBMT(int layer, int sector) {
        if (layer < 0 || sector < 0) {
            return -1;
        }
        return NSVTSENSORS+layer*3+sector;
    }

    private int getIndexSVT(int layer, int sector){
        int index = -1;
        int region = layer/2;
        switch (region) {
            case 0:
                index = sector;
                break;
            case 1:
                index =  SVTGeometry.NSECTORS[0] + sector;
                break;
            case 2:
                index = SVTGeometry.NSECTORS[0] +
                        SVTGeometry.NSECTORS[2] + sector;
                break;
            default:
                break;
        }
            if(svtTopBottomSep && layer%2==1) {
                    index += NSVTSENSORS/2;
            }
            return index;

    }


    @Override
    public boolean init() {
        if(this.getEngineConfiguration() == null || "null".equals(this.getEngineConfiguration())) {
                return true; //prevents init from being run twice.
        }

        if (this.getEngineConfigString("svtOnly")!=null) {
            this.isSVTonly= Boolean.valueOf(this.getEngineConfigString("svtOnly"));
        }
        System.out.println("["+this.getName()+"] align SVT only set to " + this.isSVTonly);

        if (this.getEngineConfigString("bmtOnly")!=null) {
            this.isBMTonly= Boolean.valueOf(this.getEngineConfigString("bmtOnly"));
        }
        System.out.println("["+this.getName()+"] align BMT only set to " + this.isBMTonly);

        if (this.getEngineConfigString("skipBMTC")!=null) {
            this.skipBMTC= Boolean.valueOf(this.getEngineConfigString("skipBMTC"));
        }
        System.out.println("["+this.getName()+"] skip BMTC set to " + this.skipBMTC);

        if (this.getEngineConfigString("svtAlignTopBottomSeparately")!=null) {
            this.svtTopBottomSep= Boolean.valueOf(this.getEngineConfigString("svtAlignTopBottomSeparately"));
        }
        System.out.println("["+this.getName()+"] align SVT top-bottom separately set to " + this.svtTopBottomSep);

        if (this.getEngineConfigString("alignVariables")!=null) {
            this.alignVars = this.getEngineConfigString("alignVariables");
        }
        System.out.println("["+this.getName()+"] align variables set to " + this.alignVars);

        if (this.getEngineConfigString("cosmics")!=null) {
            this.isCosmics= Boolean.valueOf(this.getEngineConfigString("cosmics"));
        }
        System.out.println("["+this.getName()+"] use cosmics bank set to " + this.isCosmics);

        if(this.getEngineConfigString("maxDocaCut") != null) {
            this.maxDocaCut = Double.parseDouble(this.getEngineConfigString("maxDocaCut"));
        }
        if(isCosmics) this.maxDocaCut = Double.MAX_VALUE;
        System.out.println("["+this.getName()+"] track DOCA cut set to " + this.maxDocaCut + " mm");


        if (this.getEngineConfigString("maxResidual")!=null) {
            this.maxResidualCutBMTZ = this.maxResidualCutBMTC = this.maxResidualCutSVT =  Double.valueOf(this.getEngineConfigString("maxResidual"));
        }
        if (this.getEngineConfigString("maxResidualSVT")!=null) {
            this.maxResidualCutSVT =  Double.valueOf(this.getEngineConfigString("maxResidualSVT"));
        }
        if (this.getEngineConfigString("maxResidualBMTZ")!=null) {
            this.maxResidualCutBMTZ =  Double.valueOf(this.getEngineConfigString("maxResidualBMTZ"));
        }
        if (this.getEngineConfigString("maxResidualBMTC")!=null) {
            this.maxResidualCutBMTC =  Double.valueOf(this.getEngineConfigString("maxResidualBMTC"));
        }
        System.out.println("["+this.getName()+"] max residual for SVT clusters set to "  + this.maxResidualCutSVT + " mm");
        System.out.println("["+this.getName()+"] max residual for BMTC clusters set to " + this.maxResidualCutBMTC + " mm");
        System.out.println("["+this.getName()+"] max residual for BMTZ clusters set to " + this.maxResidualCutBMTZ + " mm");

        if (this.getEngineConfigString("minClustersSVT")!=null) {
            this.minClustersSVT =  Integer.valueOf(this.getEngineConfigString("minClustersSVT"));
        }
       if (this.getEngineConfigString("minClustersBMTZ")!=null) {
            this.minClustersBMTZ =  Integer.valueOf(this.getEngineConfigString("minClustersBMTZ"));
        }
        if (this.getEngineConfigString("minClustersBMTC")!=null) {
            this.minClustersBMTC =  Integer.valueOf(this.getEngineConfigString("minClustersBMTC"));
        }
        System.out.println("["+this.getName()+"] min number of SVT clusters set to "  + this.minClustersSVT);
        System.out.println("["+this.getName()+"] min number of BMTC clusters set to " + this.minClustersBMTC);
        System.out.println("["+this.getName()+"] min number of BMTZ clusters set to " + this.minClustersBMTZ);

        if (this.getEngineConfigString("useBeamspot")!=null) {
            this.includeBeamspot =  Boolean.valueOf(this.getEngineConfigString("useBeamspot"));
        } 
        System.out.println("["+this.getName()+"] treat beamspot as an additional measurement set to " + this.includeBeamspot);

        if (this.getEngineConfigString("debug")!=null) {
                this.debug =  Boolean.parseBoolean(this.getEngineConfigString("debug"));
        } 
        
        this.nAlignables = ((this.svtTopBottomSep ? NSVTSENSORS : NSVTSENSORS/2) + (this.isSVTonly ? 0: 18) + (includeBeamspot? 1 : 0));
        this.setAlignVars(alignVars);

        return true;
    }


 
    private void setAlignVars(String alignVars) {
        orderTx = -1;
        orderTy = -1;
        orderTz = -1;
        orderRx = -1;
        orderRy = -1;
        orderRz = -1;
        if (alignVars.length() > 2 && !alignVars.contains(" ")) {
            for (int i = 0; i < alignVars.length() / 2; i++) {
                String s = alignVars.substring(2 * i, 2 * i + 2);
                if (s.equals("Tx")) {
                    orderTx = i;
                } else if (s.equals("Ty")) {
                    orderTy = i;
                } else if (s.equals("Tz")) {
                    orderTz = i;
                } else if (s.equals("Rx")) {
                    orderRx = i;
                } else if (s.equals("Ry")) {
                    orderRy = i;
                } else if (s.equals("Rz")) {
                    orderRz = i;
                }
                nAlignVars = i + 1;
            }
            if (debug) {
                System.out.println(nAlignVars + " alignment variables requested");
            }
            return;
        }
        //old version
        String split[] = alignVars.split("[ \t]+");
        int i = 0;
        for (String s : split) {
            if (s.equals("Tx")) {
                orderTx = i;
                i++;
            } else if (s.equals("Ty")) {
                orderTy = i;
                i++;
            } else if (s.equals("Tz")) {
                orderTz = i;
                i++;
            } else if (s.equals("Rx")) {
                orderRx = i;
                i++;
            } else if (s.equals("Ry")) {
                orderRy = i;
                i++;
            } else if (s.equals("Rz")) {
                orderRz = i;
                i++;
            }
        }
        nAlignVars = i;
        if(debug) System.out.println(nAlignVars + " alignment variables requested");
    }


}
