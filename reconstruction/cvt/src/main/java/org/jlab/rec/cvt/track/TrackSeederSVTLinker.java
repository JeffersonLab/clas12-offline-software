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
    private Map <Integer, Map <Integer, List<Cross>>> svtcrs;
    private Map <Integer, Map <Integer, List<Cross>>> bmtcrs;
    public TrackSeederSVTLinker(Swim swimmer, double xb, double yb) {
        float[] b = new float[3];
        swimmer.BfieldLab(0, 0, 0, b);
        this.bfield = Math.abs(b[2]);
        this.xbeam = xb;
        this.ybeam = yb;
        trseed1 = new TrackSeederRZ();
        trseed2 = new TrackSeederXY(xb, yb);
        trseed2.unUsedHitsOnly=false;
        svtcrs= new HashMap<>();
        bmtcrs= new HashMap<>();
            
    }

    public void sortXYCrosses(List<Cross> crosses) {
        svtcrs.clear();
        bmtcrs.clear();
        
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

        
//        //Use CA to get the lines
//       List<Cell> zrnodes = tca.runCAMaker("ZR", 5, bmtC_crosses);
//       List<ArrayList<Cross>> zrtracks = tca.getCAcandidates(zrnodes);
//       this.removeCompleteZROverlaps(zrtracks);
//        
        //use new line finder
        this.sortXYCrosses(crosses);
        List<ArrayList<Cross>> zrtracks = trseed1.getSeeds(bmtC_crosses,svtcrs);
        //this.removeCompleteZROverlaps(zrtracks);
        if(Constants.getInstance().seedingDebugMode) {
            this.printListCrosses(zrtracks, "RZ Seeds");
        }
        List<Seed> cands = this.match2BST(zrtracks, svtcrs, bmtcrs);
        if(Constants.getInstance().seedingDebugMode) {
            this.printListSeedCrosses(cands, "SVT Seeds");
        }
        for (Seed seed : cands) {
            //if(this.doesnotContains(seedlist, seed))
                seedlist.add(seed);
        }
        
        for (Seed bseed : seedlist) {
            for (Cross c : bseed.getCrosses()) {
                //c.isInSeed = true;
            }
        }
        return seedlist;
    }

    
    double secAngRg[][] = new double[][]{{-87.,147.0},{27.0,153.0},{-93,33.0}};
    TrackSeederRZ trseed1 ;
    TrackSeederXY trseed2 ;
    private List<Seed> match2BST(List<ArrayList<Cross>> zrtracks, 
            Map <Integer, Map <Integer, List<Cross>>> svtcrs, 
            Map <Integer, Map <Integer, List<Cross>>> bmtcrs) {
       
        List<Seed> result = new ArrayList<>();
        
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
            boolean status = ft.fitStatus(R, Z, EZ, null, Z.size());
            if(Constants.getInstance().seedingDebugMode) {
                System.out.println("Matching to ");
                for (Cross c : zrcross) { 
                    System.out.println(c.printInfo());
                }
            }
            LineFitPars fpars = ft.getFit();
            if (fpars == null) {
                continue; 
            }
            double b = fpars.intercept();
            double m = fpars.slope();
            
            List<Cross> pass = new ArrayList<>();
            if(svtcrs.containsKey(sector)) {   
                for(int creg =1; creg<4; creg++) { 
                    if(svtcrs.get(sector).containsKey(creg)) { 
                        Collections.sort(svtcrs.get(sector).get(creg));
                       
                        for (Cross c : svtcrs.get(sector).get(creg)) {
                        
                            Point3D tref = new Point3D(xbeam, ybeam, b);
                            double Zref = 0;
                            
                            //if(Math.abs(m)>0.0000001)
                                //Zref = (c.getPoint().toVector3D().rho() - b) / m;
                            Zref = m*c.getPoint().toVector3D().rho() + b;
                            double zo = c.getCluster2().getLine().origin().z();
                            double ze = c.getCluster2().getLine().end().z();
                            double range = Math.abs(ze-zo)+SVTParameters.CROSSZCUT; 
                            if(Math.abs(Zref-zo)<range && Math.abs(Zref-ze)<range ) { 
                                Point3D end = new Point3D(c.getPoint().x(), c.getPoint().y(), Zref);
                                Vector3D dir = tref.vectorTo(end).asUnit();
                                Line3D tline = new Line3D(tref, dir); 
                                Line3D sline1 = c.getCluster1().getLine();
                                Line3D sline2 = c.getCluster2().getLine();
                                double delta1 = sline1.distance(tline).length();
                                double delta2 = sline2.distance(tline).length(); 
                                if(Constants.getInstance().seedingDebugMode) {
                                    System.out.println("Check for m = "+m+" b = "+b);
                                    System.out.println(c.printInfo());
                                    System.out.println("delta1 "+delta1+" delta2 "+delta2+ " max "+SVTParameters.getMAXDOCA2STRIP());
                                }
                                if(delta1<SVTParameters.getMAXDOCA2STRIP() && delta2<SVTParameters.getMAXDOCA2STRIP()
                                        && delta1+delta2<SVTParameters.getMAXDOCA2STRIPS()) { 
                                    pass.add(c);
                                    if(Constants.getInstance().seedingDebugMode) {
                                        System.out.println("Pass ");
                                        System.out.println(c.printInfo());
                                    }
                                   
                                }
                            }
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
                if(Constants.getInstance().seedingDebugMode) {
                    System.out.println("Looking for circle:");
                    for(Cross c : pass) 
                        System.out.println(c.printInfo());
                }
                
                List<Seed> myseeds = trseed2.findSeed(pass); //Find XY seeds matched to RZ seeds using the BST as a linker
                
                for(Seed s : myseeds) { 
                    s.getCrosses().addAll(zrcross);
                    s.setCrosses(s.getCrosses()) ;
                    s.fit(3, xbeam, ybeam, bfield);
                   
                    if(s.getChi2()<Constants.CHI2CUT*s.getCrosses().size()) {
                        s.setStatus(3);
                        for(Cross c : s.getCrosses()) {
                            c.isInSeed = true;
                        }
                        result.add(s);
                    }
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

    private void removeCompleteZROverlaps(List<ArrayList<Cross>> zrtracks) {
        List<ArrayList<Cross>>twoCros = new ArrayList<>();
        List<ArrayList<Cross>> threeCros = new ArrayList<>();
        List<ArrayList<Cross>> rmCros = new ArrayList<>();
        for(List<Cross> zrtrk : zrtracks) {
            if(zrtrk.size()==3) {
                threeCros.add((ArrayList<Cross>) zrtrk);
            }
            if(zrtrk.size()==2) 
                twoCros.add((ArrayList<Cross>) zrtrk);
        }
        int[] id3 = new int[3];
        int[] id2 = new int[3];
        for(List<Cross> zrtrk3 : threeCros) {
            for(int i =0; i<3; i++)
                id3[i] = -1;
            for(Cross c : zrtrk3) {
                id3[c.getRegion()-1] = c.getId(); 
            }
            for(List<Cross> zrtrk2 : twoCros) {
                for(int i =0; i<3; i++)
                    id2[i] = -1;
                for(Cross c : zrtrk2) {
                    id2[c.getRegion()-1] = c.getId();
                }
                int count = 0;
                for(int i =0; i<3; i++) {
                    if(id2[i] != -1 && id2[i] ==id3[i]) {
                        count++;
                    }
                }
                if(count ==2) {
                    int missRegIdx = -1;
                    for(int i =0; i<3; i++) { 
                        if(id2[i] == -1) {
                              missRegIdx = i; 
                        }
                    }
//                    double sl = (zrtrk2.get(0).getPoint().toVector3D().rho() - zrtrk2.get(1).getPoint().toVector3D().rho())/(zrtrk2.get(0).getPoint().z() - zrtrk2.get(1).getPoint().z());
//                    double in = zrtrk2.get(0).getPoint().toVector3D().rho()-sl*zrtrk2.get(0).getPoint().z();
//                    double Rm = zrtrk3.get(missRegIdx).getPoint().toVector3D().rho();
//                    double Zm = zrtrk3.get(missRegIdx).getPoint().z();
//                    double Rc = sl*Zm +in;
//                    if(Math.abs(Rc-Rm)<0.0005) {
//                        rmCros.add((ArrayList<Cross>) zrtrk2); //3-cross list is good w/in 500 microns
//                    }  else {
//                        rmCros.add((ArrayList<Cross>) zrtrk3); // 3-cross list has outlier
//                    }
                    double sl = (zrtrk2.get(0).getPoint().z() - zrtrk2.get(1).getPoint().z())/(zrtrk2.get(0).getPoint().toVector3D().rho() - zrtrk2.get(1).getPoint().toVector3D().rho());
                    double in = -sl*zrtrk2.get(0).getPoint().toVector3D().rho()+zrtrk2.get(0).getPoint().z();
                    double Rm = zrtrk3.get(missRegIdx).getPoint().toVector3D().rho();
                    double Zm = zrtrk3.get(missRegIdx).getPoint().z();
                    double Zc = sl*Rm +in;
                    double Zerr = zrtrk3.get(missRegIdx).getPointErr().z();
                    if(Math.abs(Zc-Zm)<Zerr*5) {//5sigma
                        rmCros.add((ArrayList<Cross>) zrtrk2); //3-cross list is good w/in 500 microns
                    }  else {
                        rmCros.add((ArrayList<Cross>) zrtrk3); // 3-cross list has outlier
                    }
                }
            }
        }
        zrtracks.removeAll(rmCros);
    }

    private void printListCrosses(List<ArrayList<Cross>> cands, String strg) {
        System.out.println(strg);
        System.out.println("========");
        int cnt=0;
        for(List<Cross> cand : cands) {
            System.out.println("seed "+cnt++);
            for(Cross c : cand) {
                System.out.println(c.printInfo());
            }
        }    
    }

    private void printListSeedCrosses(List<Seed> cands, String strg) {
    System.out.println(strg);
        System.out.println("========");
        int cnt=0;
        for(Seed cand : cands) {
            System.out.println("seed "+cnt++);
            for(Cross c : cand.getCrosses()) {
                System.out.println(c.printInfo());
            }
        }    
    }
}
