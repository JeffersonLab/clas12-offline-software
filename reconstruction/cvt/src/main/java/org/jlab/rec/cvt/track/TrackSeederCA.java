package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTGeometry;

import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.fit.LineFitPars;
import org.jlab.rec.cvt.fit.LineFitter;
import org.jlab.rec.cvt.svt.SVTGeometry;

public class TrackSeederCA {

    private final SVTGeometry sgeo = Constants.getInstance().SVTGEOMETRY;
    private final BMTGeometry bgeo = Constants.getInstance().BMTGEOMETRY;
    private double xbeam;
    private double ybeam;
    private double bfield;

    public TrackSeederCA(Swim swimmer, double xb, double yb) {
        float[] b = new float[3];
        swimmer.BfieldLab(0, 0, 0, b);
        this.bfield = Math.abs(b[2]);
        this.xbeam = xb;
        this.ybeam = yb;
    }

    // Retrieve lists of crosses as track candidates
    // from the output of the cellular automaton   
    // it looks only for the maximum state, TODO: remove found candidate and continue
    public List<ArrayList<Cross>> getCAcandidates(List<Cell> nodes) {
//System.out.println("\n\n\t ____inside get candidates___");
        List<ArrayList<Cross>> trCands = new ArrayList<>();
        List<ArrayList<Cell>> cellCands = new ArrayList<>();
        if (nodes.isEmpty()) {
            return trCands;
        }
        Collections.sort(nodes);
        int mstate = nodes.get(0).getState();
//        System.out.println( mstate );
        for (Cell cell : nodes) {
            if (cell.getState() >= mstate - 1) {

//             if the cell has been already used for one track candidate, then skip it
                if (cell.isUsed()) {
                    continue;
                }
                if (cell.getPlane().equalsIgnoreCase("XY")) {
                    if (cell.getC1().is_usedInXYcand() || cell.getC2().is_usedInXYcand()) {
                        continue;
                    }
                }

                //if( cell.getPlane().equalsIgnoreCase("ZR") ) {
                //if( cell.getC1().is_usedInZRcand() || cell.getC2().is_usedInZRcand() ) continue;
                //}
                //if( cell.getPlane().equalsIgnoreCase("ZR") ) {
                //if( cell.getC1().is_usedInZRcand() || cell.getC2().is_usedInZRcand() ) continue;
                //}
                int candlen = 1;
                ArrayList<Cell> cand = new ArrayList<>();
                cand.add(cell);
                Cell neighbour = cell;
                while (neighbour.getNeighbors().size() > 0) {
                    Collections.sort(neighbour.getNeighbors());
                    int ms = 0; // max state neighbors
                    double dist = 99999.;
                    double cos = 0.;
                    int id = -1;
                    for (int ic = 0; ic < neighbour.getNeighbors().size(); ic++) {
                        Cell cn = neighbour.getNeighbors().get(ic);
//      				  if(cn.isUsed() ) continue;
//      	      		  if( cn.getPlane().equalsIgnoreCase("XY") ) {
//      	      			  if( cn.getC1().is_usedInXYcand() || cn.getC2().is_usedInXYcand() ) { continue;}
//      	      		  }
//
//      	      		  if( cell.getPlane().equalsIgnoreCase("ZR") ) {
//      	      			  if( cn.getC1().is_usedInZRcand() || cn.getC2().is_usedInZRcand() ) continue;
//      	      		  }      				  
                        if (cn.getState() != neighbour.getState() - 1) {
                            continue;
                        }
                        if (cn.getState() >= ms) {
                            ms = cn.getState();
                            if (neighbour.getPlane().equalsIgnoreCase("ZR")
                                    && neighbour.getC1().getDetector() == DetectorType.BST) {
                                if (cn.getLength() < dist) {
                                    dist = cn.getLength();
                                    id = ic;
                                }
                            } else {
                                if (neighbour.getDir2D().dot(cn.getDir2D()) > cos) {
                                    cos = neighbour.getDir2D().dot(cn.getDir2D());
                                    id = ic;
                                }
                            }
                        } else {
                            break; // neighbors are sorted, exit if not max state
                        }
                    }
                    if (id < 0) {
                        break;
                    }
                    Cell n = neighbour.getNeighbors().get(id);

                    // avoid clones. Set the node and its upper cross to "already used"
                    // TODO: should we assign "used" only if a Good candidate is found?
//      			  n.setUsed(true); 
//      			  if( n.getPlane().equalsIgnoreCase("XY") ) {
////      				  n.getC1().setusedInXYcand( true );
//      				  n.getC2().setusedInXYcand( true );
//      			  }
//      			  if( n.getPlane().equalsIgnoreCase("ZR") ) {
////      				  n.getC1().setusedInZRcand( true );
//      				  n.getC2().setusedInZRcand( true );
//      			  }
//              	  System.out.println(" - " + n);
                    cand.add(n);
                    neighbour = n;
                    candlen += 1;
                }

//      		  System.out.println(" ");
                if (cand.get(0).getPlane().equalsIgnoreCase("XY")) {
                    if (candlen > 2) {
                        Seed seed = new Seed(getCrossFromCells(cand));
                        if (seed.fit(2, xbeam, ybeam, bfield)) {
                            cellCands.add(cand);

                            for (Cell n : cand) {
                                n.setUsed(true);
                                if (n.getPlane().equalsIgnoreCase("XY")) {
//	  		      				  n.getC1().setusedInXYcand( true );
                                    n.getC2().setusedInXYcand(true);
                                }
                                if (n.getPlane().equalsIgnoreCase("ZR")) {
//	  		      				  n.getC1().setusedInZRcand( true );
                                    n.getC2().setusedInZRcand(true);
                                }
                            }
                        }
                    }
                } else {
                    if (candlen > 0) {
                        cellCands.add(cand);
                    }
                }

            } else {
                break; // nodes are sorted, if it is different to the max state, exit
            }//      	  else continue; // nodes are sorted, if it is different to the max state, exit
        }

//        System.out.println(" cellCands " + cellCands.size() );
        for (List<Cell> candcell : cellCands) {
            if (candcell.isEmpty()) {
                continue;
            }
            trCands.add(getCrossFromCells(candcell));
//      	  trCands.add( new ArrayList<Cross>());
//  		  trCands.get(trCands.size()-1).add( candcell.get(0).getC2() );
//      	  for( Cell c : candcell ){
//      		  trCands.get(trCands.size()-1).add( c.getC1() );
//      	  }
        }
        return trCands;
    }

    private ArrayList<Cross> getCrossFromCells(List<Cell> l) {
        if (l == null) {
            return null;
        }
        ArrayList<Cross> crs = new ArrayList<>();
        crs.add(l.get(0).getC2());
        for (Cell c : l) {
            crs.add(c.getC1());
        }

        return crs;
    }

    // create and run the cellular automaton
    public List<Cell> runCAMaker(String plane, int nepochs, ArrayList<Cross> crs) {
        MakerCA camaker = new MakerCA(false);
        camaker.setPlane(plane);
        if (plane.equalsIgnoreCase("XY")) {
            camaker.setcosBtwCells(0.95);  // min dot product between neighbours 
            camaker.setabCrs(20);         // max angle between crosses to form a cell
            camaker.setaCvsR(45);         // max angle between the cell and the radius to the first cell
        }
        if (plane.equalsIgnoreCase("ZR")) {
            camaker.setcosBtwCells(0.9); // it only applies to the BMTC cross only cells
            camaker.setabCrs(30.);
            camaker.setaCvsR(90.);
        }

        camaker.createCells(crs);
        camaker.findNeigbors();
        camaker.evolve(nepochs);
        return camaker.getNodes();
    }

    public List<Seed> findSeed(List<Cross> svt_crosses, List<Cross> bmt_crosses) {

        List<Seed> seedlist = new ArrayList<>();

        ArrayList<Cross> crosses = new ArrayList<>();
        List<ArrayList<Cross>> bmtC_crosses = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            bmtC_crosses.add(new ArrayList<>());
        }

        crosses.addAll(svt_crosses);

//        Collections.sort(crosses);
        for (Cross c : bmt_crosses) {
            if (c.getType() == BMTType.Z) {
                crosses.add(c);
            }
            if (c.getType() == BMTType.C) {
                bmtC_crosses.get(c.getSector() - 1).add(c);
            }
        }

        // look for candidates in the XY plane
        // run the cellular automaton over SVT and BMT_Z crosses
        List<Cell> xynodes = runCAMaker("XY", 5, crosses);
        List<ArrayList<Cross>> xytracks = getCAcandidates(xynodes);

//        System.out.println( " XY tracks " + xytracks );
        //// TODO: TEST TEST TEST
        // test if a first fit to move the SVT crosses helps
//        for( ArrayList<Cross> acr : xytracks ) {
//		    Track xycand = fit(acr, svt_geo, Constants.SEEDFITITERATIONS, false);
//		    // update
//        }
        List<ArrayList<Cross>> seedCrosses = CAonRZ(xytracks, bmtC_crosses);

        List<Seed> cands = new ArrayList<>();
//        System.out.println(seedlist.size());
        for (int s = 0; s < seedCrosses.size(); s++) {
//	    	Collections.sort(seedCrosses.get(s));      // TODO: check why sorting matters
//                Track cand = fit(seedCrosses.get(s), svt_geo, bmt_geo, Constants.SEEDFITITERATIONS, false, swimmer);
            Seed candSeed = new Seed(seedCrosses.get(s));
            boolean fitStatus = candSeed.fit(Constants.SEEDFITITERATIONS, xbeam, ybeam, bfield);
            if (fitStatus && candSeed.isGood()) {
                cands.add(candSeed);
            }
        }
//	    for( int i=0;i<cands.size();i++)cands.get(i).setId(i+1);
//	    cands = rmDuplicate( cands ); // TODO
        Seed.removeOverlappingSeeds(cands);

        for (Seed seed : cands) {
            seed.setStatus(1);
            seedlist.add(seed);
        }
        for (Seed bseed : seedlist) {
            for (Cross c : bseed.getCrosses()) {
                c.isInSeed = true;
            }
        }
        return seedlist;
    }

    public List<ArrayList<Cross>> CAonRZ(List<ArrayList<Cross>> xytracks,
            List<ArrayList<Cross>> bmtC_crosses) {

        List<ArrayList<Cross>> seedCrosses = new ArrayList<>();

        if (bmtC_crosses == null) {
            return null;
        }
//      System.out.println("not null bmtc");
        // loop over each xytrack to find ZR candidates
        // ---------------------------------------------
//      for( List<Cross> xycross : xytracks ){ // ALERT: this throw a concurrent modification exception 
        for (int ixy = 0; ixy < xytracks.size(); ixy++) {
            List<Cross> xycross = xytracks.get(ixy);
            ArrayList<Cross> crsZR = new ArrayList<>();
            // get the SVT crosses
            ArrayList<Cross> svtcrs = new ArrayList<>();

            // look for svt crosses and determine the sector from bmt z crosses
            //------------------------------------------------------------------
            int sector = -1;
            for (Cross c : xycross) {
                if (c.getDetector() == DetectorType.BST) {
                    svtcrs.add(c);
//            System.out.print( " " + c.getId() + " " +c.getDetector() + " " + c.getDetectorType() + " ; " );
                } else {
                    sector = c.getSector() - 1;
                }
            }
//        System.out.println(sector);
            if (sector < 0) {
                continue;
            }
            Collections.sort(svtcrs);
//        Collections.sort(svtcrs,Collections.reverseOrder());
//        for( Cross c : svtcrs ){
//            System.out.print( " " + c.getId() + " " +c.getDetector() + " " + c.getDetectorType() + " ; " );
//        }
//        System.out.println();
//        crsZR.addAll(svtcrs);

            // add all the BMT_C crosses
            //--------------------------
//        for( Cross c : bmtC_crosses.get(sector) ){
//            System.out.print( " " + c.getId() + " " +c.getDetector() + " " + c.getDetectorType() + " ; " );
//        }
//        System.out.println();
            if (bmtC_crosses.get(sector) == null || bmtC_crosses.get(sector).isEmpty()) {
                continue;
            }
            crsZR.addAll(bmtC_crosses.get(sector));

//        System.out.println("\n....\t"+crsZR);
            // sort 
//        Collections.sort(crsZR,Collections.reverseOrder());
//        Collections.sort(crsZR);
            // run the CAmaker
            List<Cell> zrnodes = runCAMaker("ZR", 5, crsZR);
//System.out.println(zrnodes);
            List<ArrayList<Cross>> zrtracks = getCAcandidates(zrnodes);

//        System.out.println("sector" + sector + " len " + zrtracks.size());  
            // collect crosses for candidates
            //--------------------------------
            for (List<Cross> zrcross : zrtracks) {
                // count svt crosses. If none, skip the candidate // TODO
                //int Nsvt = 0;
                //for( Cross c : zrcross ){
                //if( c.getDetector().equalsIgnoreCase("SVT")){
                //Nsvt++;
                //}
                //}
                //if( Nsvt == 0 ) continue;

                // FIT ZR BMT
                // ---------------------------
                List<Double> R = new ArrayList<>();
                List<Double> Z = new ArrayList<>();
                List<Double> EZ = new ArrayList<>();

                for (Cross c : zrcross) {
                    R.add(bgeo.getRadiusMidDrift(c.getCluster1().getLayer()));
                    Z.add(c.getPoint().z());
                    EZ.add(c.getPointErr().z());
                }

                LineFitter ft = new LineFitter();
                boolean status = ft.fitStatus(Z, R, EZ, null, Z.size());
                if (status == false) {
                    System.err.println(" BMTC FIT FAILED");
                }
                LineFitPars fpars = ft.getFit();
                if (fpars == null) {
                    continue;
                }
                double b = fpars.intercept();
                double m = fpars.slope();

                seedCrosses.add(new ArrayList<>());
                int scsize = seedCrosses.size();
                // add svt
                for (Cross c : svtcrs) {
                    int l1 = c.getCluster1().getLayer();
                    int s1 = c.getCluster1().getSector();
                    double c1 = c.getCluster1().getCentroid();
                    double r1 = SVTGeometry.getLayerRadius(l1);
                    double nstr1 = sgeo.calcNearestStrip(c.getPoint().x(), c.getPoint().y(), (r1 - b) / m, l1, s1);

                    int l2 = c.getCluster2().getLayer();
                    int s2 = c.getCluster2().getSector();
                    double c2 = c.getCluster2().getCentroid();
                    double r2 = SVTGeometry.getLayerRadius(l2);
                    double nstr2 = sgeo.calcNearestStrip(c.getPoint().x(), c.getPoint().y(), (r2 - b) / m, l2, s2);

                    if (Math.abs(c1 - nstr1) < 8 && Math.abs(c2 - nstr2) < 8) {
                        seedCrosses.get(scsize - 1).add(c);
                    }
                }

//          for( Cross c : zrcross ){
//            if( c.getDetector().equalsIgnoreCase("SVT")){
//              seedCrosses.get(scsize-1).add(c); 
//            }
//          }
                // add bmt z
                for (Cross c : xycross) {
                    if (c.getDetector() == DetectorType.BMT) {
                        seedCrosses.get(scsize - 1).add(c);
                    }
                }

                // add bmt c
                for (Cross c : zrcross) {
                    if (c.getDetector() == DetectorType.BMT) {
                        seedCrosses.get(scsize - 1).add(c);
                    }
                }
            }
        }

        return seedCrosses;
    }

}
