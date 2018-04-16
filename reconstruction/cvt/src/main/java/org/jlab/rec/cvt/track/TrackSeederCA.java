package org.jlab.rec.cvt.track;
import org.jlab.rec.cvt.track.MakerCA;
import org.jlab.rec.cvt.track.Cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.fit.CircleFitter;
import org.jlab.rec.cvt.fit.HelicalTrackFitter;
import org.jlab.rec.cvt.svt.Constants;

public class TrackSeederCA {

    public TrackSeederCA() {
        
        sortedCrosses = new ArrayList<ArrayList<ArrayList<Cross>>>();
        
        //for(int b =0; b<36; b++) {
            //sortedCrosses.add(b, new ArrayList<ArrayList<Cross>>() );
            //for(int l =0; l<6; l++) {
                //sortedCrosses.get(b).add(l,new ArrayList<Cross>() );
            //}
        //}
    }

    private List<ArrayList<Cross>> seedCrosses = new ArrayList<ArrayList<Cross>>();
    private List<ArrayList<ArrayList<Cross>>> sortedCrosses;


    // Retrieve lists of crosses as track candidates
    // from the output of the cellular automaton   
    // it looks only for the maximum state, TODO: remove found candidate and continue
    public List<ArrayList<Cross>> getCAcandidates( List<Cell> nodes ) {
//System.out.println("\n\n\t ____inside get candidates___");
        List<ArrayList<Cross>> trCands = new ArrayList<ArrayList<Cross>>();
        List<ArrayList<Cell>> cellCands = new ArrayList<ArrayList<Cell>>();
        if( nodes.size() == 0 ) return trCands;
        Collections.sort( nodes );
        int mstate = nodes.get(0).get_state();
//        System.out.println( mstate );
        for( Cell cell : nodes ){
      	  if( cell.get_state() >= mstate-1 ){
          
            // if the cell has been already used for one track candidate, then skip it
      		  if( cell.is_used()) continue;
      		  int candlen = 1;
      		  ArrayList<Cell> cand = new ArrayList<Cell>();
      		  cand.add( cell );
      		  Cell neighbour = cell;
      		  while( neighbour.get_neighbors().size() > 0 ){
      			  Collections.sort(neighbour.get_neighbors());
      			  int ms = 0; // max state neighbors
      			  double dist = 99999.;
      			  double cos = 0.;
      			  int id = -1;
      			  for( int ic=0; ic < neighbour.get_neighbors().size();ic++ ){
      				  Cell cn = neighbour.get_neighbors().get(ic);
      				  if(cn.is_used() ) continue;
      				  if( cn.get_state() != neighbour.get_state()-1) continue;
      				  if( cn.get_state() >= ms ){
      					  ms = cn.get_state();
      					  if( neighbour.get_plane().equalsIgnoreCase("ZR") &&
      							neighbour.get_c1().get_Detector().equalsIgnoreCase("SVT")  ){
	      					  if( cn.get_length() < dist ){
	      						  dist = cn.get_length();
	      						  id = ic;
	      					  }
      					  }
      					  else {
	      					  if( neighbour.get_dir2D().dot( cn.get_dir2D() ) > cos ){
	      						  cos = neighbour.get_dir2D().dot( cn.get_dir2D() );
	      						  id = ic;
	      					  }
      					  }
      				  }
      				  else break; // neighbors are sorted, exit if not max state
      			  }
      			  if( id < 0 ) break;
      			  Cell n = neighbour.get_neighbors().get(id);
      			  n.set_used(true);

//              	  System.out.println(" - " + n);
      			  cand.add(n);
      			  neighbour = n;
      			  candlen += 1;
      		  }
      		  
//      		  System.out.println(" ");
      		  if( cand.get(0).get_plane().equalsIgnoreCase("XY")) {
	  			  if( candlen > 2 ){
	  				  cellCands.add(cand);
	  		  	  }
      		  }
      		  else {
	  			  if( candlen > 1 ){
	  				  cellCands.add(cand);
	  		  	  }
      		  }
      		  
      	  }
      	  else break; // nodes are sorted, if it is different to the max state, exit
//      	  else continue; // nodes are sorted, if it is different to the max state, exit
        }
        
//        System.out.println(" cellCands " + cellCands.size() );
         
        for( List<Cell> candcell : cellCands ){
//        	System.out.println(candcell);
          if(candcell.size() == 0 ) continue;
//          System.out.print( " +++ ");
      	  trCands.add( new ArrayList<Cross>());
  		  trCands.get(trCands.size()-1).add( candcell.get(0).get_c2() );
//          System.out.print( " " + trCands.get(trCands.size()-1).get(0).get_Id() + " " + trCands.get(trCands.size()-1).get(0).get_Detector() +trCands.get(trCands.size()-1).get(0).get_DetectorType());
      	  for( Cell c : candcell ){
//              System.out.print( " " + c.get_c1().get_Id() + " " + c.get_c1().get_Detector() +c.get_c1().get_DetectorType());
      		  trCands.get(trCands.size()-1).add( c.get_c1() );
      	  }
//          System.out.println( " +++ " + trCands.get(trCands.size()-1).size() + ";   ");
        }
        return trCands;      
    }


    // create and run the cellular automaton
    public List<Cell> runCAMaker( String plane, int nepochs, ArrayList<Cross> crs, org.jlab.rec.cvt.bmt.Geometry bgeom  ){
        MakerCA camaker = new MakerCA();
        camaker.set_plane( plane );
        if( plane.equalsIgnoreCase("XY") ){
          camaker.set_cosBtwCells(0.95);  // min dot product between neighbours 
          camaker.set_abCrs(20);         // max angle between crosses to form a cell
          camaker.set_aCvsR(45);         // max angle between the cell and the radius to the first cell
        }
        if( plane.equalsIgnoreCase("ZR") ){
          camaker.set_cosBtwCells(0.1);
          camaker.set_abCrs(30.);
          camaker.set_aCvsR(90.);
        }
        
        camaker.createCells(crs, bgeom);
        camaker.findNeigbors();
        camaker.evolve( nepochs );
        return camaker.getNodes();  
    }
    
    private List<Double> Xs = new ArrayList<Double>();
    private List<Double> Ys = new ArrayList<Double>();
    private List<Double> Ws = new ArrayList<Double>();

    private List<Seed> BMTmatches = new ArrayList<Seed>();

    public List<Seed> findSeed(List<Cross> svt_crosses, List<Cross> bmt_crosses, 
    						   org.jlab.rec.cvt.svt.Geometry svt_geo, 
    						   org.jlab.rec.cvt.bmt.Geometry bmt_geo) {
       
        List<Seed> seedlist = new ArrayList<Seed>();

        ArrayList<Cross> crosses = new ArrayList<Cross>();
        List<ArrayList<Cross>> bmtC_crosses = new ArrayList<ArrayList<Cross>>();
        for( int i=0;i<3;i++) bmtC_crosses.add( new ArrayList<Cross>() );
        
        crosses.addAll(svt_crosses);

        Collections.sort(crosses);
        
        for(Cross c : bmt_crosses) { 
            if(c.get_DetectorType().equalsIgnoreCase("Z"))
                crosses.add(c);
            if(c.get_DetectorType().equalsIgnoreCase("C")) {
                bmtC_crosses.get(c.get_Sector()-1).add(c);	
            }
        }


        // look for candidates in the XY plane
        // run the cellular automaton over SVT and BMT_Z crosses

        List<Cell> xynodes = runCAMaker( "XY", 5, crosses, bmt_geo ); 
        List<ArrayList<Cross>> xytracks =  getCAcandidates( xynodes );
       
//        seedCrosses = xytracks;
//        System.out.println(xytracks.size());

        
        seedlist = CAonRZ( seedlist, xytracks, bmtC_crosses, svt_geo, bmt_geo);
        
//        System.out.println(seedlist.size());

        return seedlist;
    }
    
    public List<Seed> CAonRZ( List<Seed> seedlist, 
    						   List<ArrayList<Cross>>xytracks , 
    						   List<ArrayList<Cross>> bmtC_crosses,
    						   org.jlab.rec.cvt.svt.Geometry svt_geo, 
    						   org.jlab.rec.cvt.bmt.Geometry bmt_geo) {

      if( bmtC_crosses == null ) return null;
      // loop over each xytrack to find ZR candidates
      // ---------------------------------------------
//      for( List<Cross> xycross : xytracks ){ // ALERT: this throw a concurrent modification exception 
      for( int ixy=0; ixy< xytracks.size();ixy++ ){
    	List<Cross> xycross = xytracks.get(ixy);
        ArrayList<Cross> crsZR = new ArrayList<Cross>();
        // get the SVT crosses
        ArrayList<Cross> svtcrs = new ArrayList<Cross>();

        // look for svt crosses and determine the sector from bmt z crosses
        //------------------------------------------------------------------
        int sector = -1;
        for( Cross c : xycross ){
          if( c.get_Detector().equalsIgnoreCase("SVT")){
            svtcrs.add(c);
//            System.out.print( " " + c.get_Id() + " " +c.get_Detector() + " " + c.get_DetectorType() + " ; " );
          }
          else {
        	  sector = c.get_Sector()-1;
          }
        }
        if( sector < 0 ) continue;
        Collections.sort(svtcrs);
//        Collections.sort(svtcrs,Collections.reverseOrder());
//        for( Cross c : svtcrs ){
//            System.out.print( " " + c.get_Id() + " " +c.get_Detector() + " " + c.get_DetectorType() + " ; " );
//        }
//        System.out.println();
        crsZR.addAll(svtcrs);

        // add all the BMT_C crosses
        //--------------------------
//        for( Cross c : bmtC_crosses.get(sector) ){
//            System.out.print( " " + c.get_Id() + " " +c.get_Detector() + " " + c.get_DetectorType() + " ; " );
//        }
//        System.out.println();
        if( bmtC_crosses.get(sector) == null  || bmtC_crosses.get(sector).size() == 0 ) continue;
        crsZR.addAll( bmtC_crosses.get(sector) );

        // sort 
//        Collections.sort(crsZR,Collections.reverseOrder());
//        Collections.sort(crsZR);
        
        // run the CAmaker
        List<Cell> zrnodes = runCAMaker( "ZR", 5, crsZR, bmt_geo );

        List<ArrayList<Cross>> zrtracks =  getCAcandidates( zrnodes );

//        System.out.println("sector" + sector + " len " + zrtracks.size());  
        
        // collect crosses for candidates
        //--------------------------------
        for( List<Cross> zrcross : zrtracks ){
          seedCrosses.add( new ArrayList<Cross>() );
          int scsize = seedCrosses.size();
          // add svt
          for( Cross c : zrcross ){
            if( c.get_Detector().equalsIgnoreCase("SVT")){
              seedCrosses.get(scsize-1).add(c); 
            }              
          }

          // add bmt z
          for( Cross c : xycross ){
            if( c.get_Detector().equalsIgnoreCase("BMT")){
              seedCrosses.get(scsize-1).add(c); 
            }              
          }

          // add bmt c
          for( Cross c : zrcross ){
            if( c.get_Detector().equalsIgnoreCase("BMT")){
              seedCrosses.get(scsize-1).add(c); 
            }              
          }
        }
      }
      
	    for (int s = 0; s < seedCrosses.size(); s++) {
	    	Collections.sort(seedCrosses.get(s));      // TODO: check why sorting matters
		    Track cand = fitSeed(seedCrosses.get(s), svt_geo, 5, false);
		    if (cand != null) {
		        Seed seed = new Seed();
		        seed.set_Crosses(seedCrosses.get(s));
		        seed.set_Helix(cand.get_helix());
		        seedlist.add(seed);
		        List<Cluster> clusters = new ArrayList<Cluster>();
		        for(Cross c : seed.get_Crosses()) { 
		            if(c.get_Detector().equalsIgnoreCase("SVT")) {
		                clusters.add(c.get_Cluster1());
		                clusters.add(c.get_Cluster2());
	            	} else {
	                	clusters.add(c.get_Cluster1());
	            	}
	        	}
	        	seed.set_Clusters(clusters);
	    	}
	    }

      return seedlist;
    }

    private List<Double> X = new ArrayList<Double>();
    private List<Double> Y = new ArrayList<Double>();
    private List<Double> Z = new ArrayList<Double>();
    private List<Double> Rho = new ArrayList<Double>();
    private List<Double> ErrZ = new ArrayList<Double>();
    private List<Double> ErrRho = new ArrayList<Double>();
    private List<Double> ErrRt = new ArrayList<Double>();
    List<Cross> BMTCrossesC = new ArrayList<Cross>();
    List<Cross> BMTCrossesZ = new ArrayList<Cross>();
    List<Cross> SVTCrosses = new ArrayList<Cross>();

    public Track fitSeed(List<Cross> VTCrosses, org.jlab.rec.cvt.svt.Geometry svt_geo, int fitIter, boolean originConstraint) {
        double chisqMax = Double.POSITIVE_INFINITY;
        
        Track cand = null;
        HelicalTrackFitter fitTrk = new HelicalTrackFitter();
        for (int i = 0; i < fitIter; i++) {
            //	if(originConstraint==true) {
            //		X.add(0, (double) 0);
            //		Y.add(0, (double) 0);
            //		Z.add(0, (double) 0);
            //		Rho.add(0, (double) 0);
            //		ErrRt.add(0, (double) org.jlab.rec.cvt.svt.Constants.RHOVTXCONSTRAINT);
            //		ErrZ.add(0, (double) org.jlab.rec.cvt.svt.Constants.ZVTXCONSTRAINT);		
            //		ErrRho.add(0, (double) org.jlab.rec.cvt.svt.Constants.RHOVTXCONSTRAINT);										
            //	}
            X.clear();
            Y.clear();
            Z.clear();
            Rho.clear();
            ErrZ.clear();
            ErrRho.clear();
            ErrRt.clear();

            int svtSz = 0;
            int bmtZSz = 0;
            int bmtCSz = 0;

            BMTCrossesC.clear();
            BMTCrossesZ.clear();
            SVTCrosses.clear();

            for (Cross c : VTCrosses) {
                if (!(Double.isNaN(c.get_Point().z()) || Double.isNaN(c.get_Point().x()))) {
                    SVTCrosses.add(c);
                }

                if (Double.isNaN(c.get_Point().x())) {
                    BMTCrossesC.add(c);
                }
                if (Double.isNaN(c.get_Point().z())) {
                    BMTCrossesZ.add(c);
                }
            }
            svtSz = SVTCrosses.size();
            if (BMTCrossesZ != null) {
                bmtZSz = BMTCrossesZ.size();
            }
            if (BMTCrossesC != null) {
                bmtCSz = BMTCrossesC.size();
            }

            int useSVTdipAngEst = 1;
            if (bmtCSz >= 2) {
                useSVTdipAngEst = 0;
            }

            ((ArrayList<Double>) X).ensureCapacity(svtSz + bmtZSz);
            ((ArrayList<Double>) Y).ensureCapacity(svtSz + bmtZSz);
            ((ArrayList<Double>) Z).ensureCapacity(svtSz * useSVTdipAngEst + bmtCSz);
            ((ArrayList<Double>) Rho).ensureCapacity(svtSz * useSVTdipAngEst + bmtCSz);
            ((ArrayList<Double>) ErrZ).ensureCapacity(svtSz * useSVTdipAngEst + bmtCSz);
            ((ArrayList<Double>) ErrRho).ensureCapacity(svtSz * useSVTdipAngEst + bmtCSz); // Try: don't use svt in dipdangle fit determination
            ((ArrayList<Double>) ErrRt).ensureCapacity(svtSz + bmtZSz);

            cand = new Track(null);
            cand.addAll(SVTCrosses);
            for (int j = 0; j < SVTCrosses.size(); j++) {
                X.add(j, SVTCrosses.get(j).get_Point().x());
                Y.add(j, SVTCrosses.get(j).get_Point().y());
                if (useSVTdipAngEst == 1) {
                    Z.add(j, SVTCrosses.get(j).get_Point().z());
                    Rho.add(j, Math.sqrt(SVTCrosses.get(j).get_Point().x() * SVTCrosses.get(j).get_Point().x()
                            + SVTCrosses.get(j).get_Point().y() * SVTCrosses.get(j).get_Point().y()));
                    ErrRho.add(j, Math.sqrt(SVTCrosses.get(j).get_PointErr().x() * SVTCrosses.get(j).get_PointErr().x()
                            + SVTCrosses.get(j).get_PointErr().y() * SVTCrosses.get(j).get_PointErr().y()));
                    ErrZ.add(j, SVTCrosses.get(j).get_PointErr().z());
                }
                ErrRt.add(j, Math.sqrt(SVTCrosses.get(j).get_PointErr().x() * SVTCrosses.get(j).get_PointErr().x()
                        + SVTCrosses.get(j).get_PointErr().y() * SVTCrosses.get(j).get_PointErr().y()));
            }

            if (bmtZSz > 0) {
                for (int j = svtSz; j < svtSz + bmtZSz; j++) {
                    X.add(j, BMTCrossesZ.get(j - svtSz).get_Point().x());
                    Y.add(j, BMTCrossesZ.get(j - svtSz).get_Point().y());
                    ErrRt.add(j, Math.sqrt(BMTCrossesZ.get(j - svtSz).get_PointErr().x() * BMTCrossesZ.get(j - svtSz).get_PointErr().x()
                            + BMTCrossesZ.get(j - svtSz).get_PointErr().y() * BMTCrossesZ.get(j - svtSz).get_PointErr().y()));
                }
            }
            if (bmtCSz > 0) {
                for (int j = svtSz * useSVTdipAngEst; j < svtSz * useSVTdipAngEst + bmtCSz; j++) {
                    Z.add(j, BMTCrossesC.get(j - svtSz * useSVTdipAngEst).get_Point().z());
                    Rho.add(j, org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[BMTCrossesC.get(j - svtSz * useSVTdipAngEst).get_Region() - 1]
                            + org.jlab.rec.cvt.bmt.Constants.hStrip2Det);
                    
                    ErrRho.add(j, org.jlab.rec.cvt.bmt.Constants.hStrip2Det / Math.sqrt(12.));
                    ErrZ.add(j, BMTCrossesC.get(j - svtSz * useSVTdipAngEst).get_PointErr().z());
                }
            }
            X.add((double) 0);
            Y.add((double) 0);

            ErrRt.add((double) 0.1);
            
            fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ);
            
            if (fitTrk.get_helix() == null) { 
                return null;
            }

            cand = new Track(fitTrk.get_helix());
            //cand.addAll(SVTCrosses);
            cand.addAll(SVTCrosses);
            
            cand.set_HelicalTrack(fitTrk.get_helix());
            //if(shift==0)
            if (fitTrk.get_chisq()[0] < chisqMax) {
                chisqMax = fitTrk.get_chisq()[0];
                if(chisqMax<Constants.CIRCLEFIT_MAXCHI2)
                    cand.update_Crosses(svt_geo);
                //i=fitIter;
            }
        }
        //System.out.println(" Seed fitter "+fitTrk.get_chisq()[0]+" "+fitTrk.get_chisq()[1]); 
        if(chisqMax>Constants.CIRCLEFIT_MAXCHI2)
            cand=null;
        return cand;
    }

}
