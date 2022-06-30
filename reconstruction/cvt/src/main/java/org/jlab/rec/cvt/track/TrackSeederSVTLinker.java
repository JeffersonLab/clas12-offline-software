package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.Geometry;
import org.jlab.rec.cvt.bmt.BMTGeometry;

import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.fit.LineFitPars;
import org.jlab.rec.cvt.fit.LineFitter;
import org.jlab.rec.cvt.services.RecUtilities;
import org.jlab.rec.cvt.svt.SVTParameters;

public class TrackSeederSVTLinker {

    private final BMTGeometry bgeo = Geometry.getInstance().getBMT();
    private double xbeam;
    private double ybeam;
    private double bfield;
    private TrackSeederCA tca;
    public TrackSeederSVTLinker(Swim swimmer, double xb, double yb) {
        float[] b = new float[3];
        swimmer.BfieldLab(0, 0, 0, b);
        this.bfield = Math.abs(b[2]);
        this.xbeam = xb;
        this.ybeam = yb;
        trseed2 = new TrackSeederXY(swimmer, xb, yb);
        trseed2.unUsedHitsOnly=false;
        
        tca = new TrackSeederCA(swimmer, xb, yb);
            
    }

    
    public List<Seed> findSeed(List<Cross> svt_crosses, List<Cross> bmt_crosses) {
        
        List<Seed> seedlist = new ArrayList<>();

        ArrayList<Cross> crosses = new ArrayList<>();
        ArrayList<Cross> bmtC_crosses = new ArrayList<>();
        
        crosses.addAll(svt_crosses);
        
        for (Cross c : bmt_crosses) {
            if (c.getType() == BMTType.Z) {
                crosses.add(c);
            }
            if (c.getType() == BMTType.C) {
                bmtC_crosses.add(c);
            }
        }

        //Use CA to get the lines
        List<Cell> zrnodes = tca.runCAMaker("ZR", 5, bmtC_crosses);
        List<ArrayList<Cross>> zrtracks = tca.getCAcandidates(zrnodes);
        
        List<Seed> cands = this.match2BST(zrtracks, crosses);

        for (Seed seed : cands) {
            //if(this.doesnotContains(seedlist, seed))
                seedlist.add(seed);
        }
        
        for (Seed bseed : seedlist) {
            for (Cross c : bseed.getCrosses()) {
                c.isInSeed = true;
            }
        }
        return seedlist;
    }

    
    double secAngRg[][] = new double[][]{{-87.,147.0},{27.0,153.0},{-93,33.0}};
    TrackSeederXY trseed2 ;
    private List<Seed> match2BST(List<ArrayList<Cross>> zrtracks, List<Cross> crosses) {
       
        List<Seed> result = new ArrayList<>();
        Map <Integer, Map <Integer, List<Cross>>> svtcrs= new HashMap<>();
        Map <Integer, Map <Integer, List<Cross>>> bmtcrs= new HashMap<>();
        //sort the crosses in lists
        for(Cross c : crosses) {
            if(c.getDetector()==DetectorType.BST) {
               
                List<Integer> sec = this.getSector(c.getSector(), c.getRegion());
                for(int i =0; i<sec.size(); i++) {
                    if(svtcrs.containsKey(sec.get(i))) {  
                        if(svtcrs.get(sec.get(i)).containsKey(c.getRegion())) {
                            svtcrs.get(sec.get(i)).get(c.getRegion()).add(c);
                        } else {
                            svtcrs.get(sec.get(i)).put(c.getRegion(), new ArrayList<>());
                            svtcrs.get(sec.get(i)).get(c.getRegion()).add(c);
                        }
                    } else {
                        svtcrs.put(sec.get(i), new HashMap<>());
                        svtcrs.get(sec.get(i)).put(c.getRegion(), new ArrayList<>());
                        svtcrs.get(sec.get(i)).get(c.getRegion()).add(c);
                    }
                }
            } else {
                if(c.getType()==BMTType.Z) {
                    if(bmtcrs.containsKey(c.getSector())) {  
                        if(bmtcrs.get(c.getSector()).containsKey(c.getRegion())) {
                            bmtcrs.get(c.getSector()).get(c.getRegion()).add(c);
                        } else {
                            bmtcrs.get(c.getSector()).put(c.getRegion(), new ArrayList<>());
                            bmtcrs.get(c.getSector()).get(c.getRegion()).add(c);
                        }
                    } else {
                        bmtcrs.put(c.getSector(), new HashMap<>());
                        bmtcrs.get(c.getSector()).put(c.getRegion(), new ArrayList<>());
                        bmtcrs.get(c.getSector()).get(c.getRegion()).add(c);
                    }
                
                }
            }
        }
        
        //loop over the line cands
        for (List<Cross> zrcross : zrtracks) {  
            List<Double> R = new ArrayList<>();
            List<Double> Z = new ArrayList<>();
            List<Double> EZ = new ArrayList<>();
            int sector = zrcross.get(0).getSector();
            for (Cross c : zrcross) { 
                R.add(bgeo.getRadiusMidDrift(c.getCluster1().getLayer()));
                Z.add(c.getPoint().z());
                EZ.add(c.getPointErr().z());
            }
            if(R.size()<2) continue;
            LineFitter ft = new LineFitter();
            boolean status = ft.fitStatus(Z, R, EZ, null, Z.size());
            if (status == false) {
                //System.err.println(" BMTC FIT FAILED");
            }
            LineFitPars fpars = ft.getFit();
            if (fpars == null) {
                continue;
            }
            double b = fpars.intercept();
            double m = fpars.slope();

            List<Cross> pass = new ArrayList<>();
            if(svtcrs.containsKey(sector)) {   
                for(int creg =1; creg<4; creg++) { //find the best BST match in each region
                    if(svtcrs.get(sector).containsKey(creg)) {
                        Collections.sort(svtcrs.get(sector).get(creg));
                        double bestdeltasum = 9999;
                        Cross bestCross = null;
                        for (Cross c : svtcrs.get(sector).get(creg)) {

                            Point3D tref = new Point3D(xbeam, ybeam, b);
                            Point3D end = new Point3D(c.getPoint().x(), c.getPoint().y(), (c.getPoint().toVector3D().rho() - b) / m);
                            Vector3D dir = tref.vectorTo(end).asUnit();
                            Line3D tline = new Line3D(tref, dir);
                            Line3D sline1 = c.getCluster1().getLine();
                            Line3D sline2 = c.getCluster2().getLine();
                            double delta1 = sline1.distance(tline).length();
                            double delta2 = sline2.distance(tline).length();

                            if(delta1<SVTParameters.MAXDOCA2STRIP && delta2<SVTParameters.MAXDOCA2STRIP 
                                    && delta1+delta2<SVTParameters.MAXDOCA2STRIPS) {
                                if(delta1+delta2<bestdeltasum) {
                                   bestdeltasum= delta1+delta2;
                                   bestCross = c;
                               }
                            }
                        }
                        if(bestCross!=null) {
                            pass.add(bestCross);
                        }
                    }
                }
            }
            if(pass.size()>0) {
                //if SVT matches, search for BMT Z clusters
                if(bmtcrs.containsKey(sector)) { 
                    for(int i = 1; i<4; i++) {
                        if(bmtcrs.get(sector).containsKey(i))
                            pass.addAll(new ArrayList<>(bmtcrs.get(sector).get(i)));
                    }
                }
                
                List<Seed> myseeds = trseed2.findSeed(pass); //Find XY seeds matched to RZ seeds using the BST as a linker
                
                for(Seed s : myseeds) {
                    s.getCrosses().addAll(zrcross);
                    s.setCrosses(s.getCrosses()) ;
                    s.fit(3, xbeam, ybeam, bfield);
                   
                    if(s.getChi2()<Constants.CHI2CUT*s.getCrosses().size())
                        result.add(s);
                }
            }
        }
        
        RecUtilities.getUniqueSeedList(result);
        
        return result;
    }
   
    private List<Integer> getSector(int bstSec, int bstRg) {
        List<Integer> sec = new ArrayList<>();
        if(bstRg==1) {
            if(bstSec>0 && bstSec<5)
                sec.add(1);
            if(bstSec>3 && bstSec<9)
                sec.add(2);
            if(bstSec>7 && bstSec<11)
                sec.add(3);
            if(bstSec==1)
                sec.add(3);
        }
        if(bstRg==2) {
            if(bstSec>0 && bstSec<7)
                sec.add(1);
            if(bstSec>5 && bstSec<11)
                sec.add(2);
            if(bstSec>9 && bstSec<15)
                sec.add(3);
            if(bstSec==1)
                sec.add(3);
        }        
        if(bstRg==3) {
            if(bstSec>0 && bstSec<8)
                sec.add(1);
            if(bstSec>6 && bstSec<14)
                sec.add(2);
            if(bstSec>13 && bstSec<19)
                sec.add(3);
            if(bstSec==1)
                sec.add(3);
        }   
       
        
        return sec;
    }

}
