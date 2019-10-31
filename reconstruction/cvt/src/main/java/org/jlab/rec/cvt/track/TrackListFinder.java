package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.geom.base.Detector;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.rec.cvt.trajectory.Trajectory;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;

import Jama.Matrix;

import org.jlab.rec.cvt.track.fit.StateVecs.StateVec;
import org.jlab.detector.base.DetectorType;

public class TrackListFinder {
    
    public TrackListFinder() {
        // TODO Auto-generated constructor stub
    }

    /**
     *
     * @param cands the list of track candidates
     * @param svt_geo the svt geometry
     * @return the list of selected tracks
     */
    public List<Track> getTracks(List<Track> cands, 
            org.jlab.rec.cvt.svt.Geometry svt_geo, org.jlab.rec.cvt.bmt.Geometry bmt_geo,
            CTOFGeant4Factory ctof_geo, Detector cnd_geo,
            Swim bstSwim) {
        List<Track> tracks = new ArrayList<Track>();
        if (cands.size() == 0) {
            System.err.print("Error no tracks found");
            return cands;
        }

        // loop over candidates and set the trajectories
        
        for (Track trk : cands) {
            if(trk.get_helix()!=null) {
                this.assignTrkPID(trk);
                //KalFit kf = new KalFit(trk, svt_geo);
                //kf.runKalFit(trk, svt_geo);
                //EnergyLossCorr elc = new EnergyLossCorr(trk);
                //System.out.println("******* before EL "+trk.get_P());
                //elc.doCorrection(trk, svt_geo);
                //System.out.println("*******  after EL "+trk.get_P());

                int charge = trk.get_Q();
                double maxPathLength = 5.0;//very loose cut 
                bstSwim.SetSwimParameters(trk.get_helix().xdca() / 10, trk.get_helix().ydca() / 10, trk.get_helix().get_Z0() / 10, 
                        Math.toDegrees(trk.get_helix().get_phi_at_dca()), Math.toDegrees(Math.acos(trk.get_helix().costheta())),
                        trk.get_P(), charge, 
                        maxPathLength) ;

                double[] pointAtCylRad = bstSwim.SwimToCylinder(Constants.CTOFINNERRADIUS/10);
                trk.set_TrackPointAtCTOFRadius(new Point3D(pointAtCylRad[0]*10, pointAtCylRad[1]*10, pointAtCylRad[2]*10));
                trk.set_TrackDirAtCTOFRadius(new Vector3D(pointAtCylRad[3]*10, pointAtCylRad[4]*10, pointAtCylRad[5]*10));

                trk.set_pathLength(pointAtCylRad[6]*10);

                TrajectoryFinder trjFind = new TrajectoryFinder();

                Trajectory traj = trjFind.findTrajectory(trk.get_Id(), trk, svt_geo, bmt_geo, ctof_geo, cnd_geo, bstSwim, "final");

                trk.set_Trajectory(traj.get_Trajectory());

                //if(trk.passCand == true)
                tracks.add(trk);
            }

        }
        return tracks;
    }

    private void assignTrkPID(Track trk) {

        int NbHits = 0;
        double TotE = 0;
        for (int i = 0; i < trk.size(); i++) {
            if (trk.get(i).get_Detector() != "SVT") {
                continue;
            }
            for (int j = 0; j < trk.get(i).get_Cluster1().size(); j++) {
                TotE += trk.get(i).get_Cluster1().get(j).get_Strip().get_Edep();
                NbHits++;
            }
            /*for (int j = 0; j < trk.get(i).get_Cluster2().size(); j++) {
                TotE += trk.get(i).get_Cluster2().get(j).get_Strip().get_Edep();
                NbHits++;
            }*/
        }
        TotE /= (double) NbHits;

        if (TotE <= org.jlab.rec.cvt.Constants.PIDCUTOFF) {
            trk.set_PID("pion");
        }
        if (TotE > org.jlab.rec.cvt.Constants.PIDCUTOFF) {
            trk.set_PID("proton");
        }
    }
    
    public void removeBadTracks(List<Track> trkcands) {
    	if(trkcands==null)
            return;
        
    	int initial_size=trkcands.size();
    	
        for(int i =1; i<initial_size+1; i++) {
        	if (Double.isNaN(trkcands.get(initial_size-i).getChi2())) {
        		trkcands.remove(initial_size-i);
        		continue;
        	}
        	if (trkcands.get(initial_size-i).getChi2()>Constants.Nsigma_per_point*Constants.Nsigma_per_point*(trkcands.get(initial_size-i).getNDF()+5)) {
        		trkcands.remove(initial_size-i);
        		continue;
        	}
        	if (trkcands.get(initial_size-i).getNDF()<Constants.NDF_Min) {
        		trkcands.remove(initial_size-i);
        		continue;
        	}
        	if (trkcands.get(initial_size-i).get_Pt()<Constants.Pt_Min) {
        		trkcands.remove(initial_size-i);
        		continue;
        	}
        }
    }
     
    public void removeOverlappingTracks(List<Track> trkcands, String plane) {
            if(trkcands==null) {
            	System.out.println("Pas de trace dans le overlap study");
            	return;
            }
            
            List<Track> selectedTracks =new ArrayList<Track>(); //List of tracks we will keep while we analyze all candidates in trkcands
            List<Track> list = new  ArrayList<Track>(); //Container for overlapping tracks, reinitialize at each events
            List<Track> rejected = new  ArrayList<Track>(); //List of tracks already analyzed and considered as bad ones.
            
            for(int i =0; i<trkcands.size(); i++) { 
                
                list.clear();//We look at a new candidate... we reinitialize this container to store the overlapping track with trkcands.get(i)
                
                if(trkcands.get(i)==null) {
                    continue;
                }
                
                if( rejected.contains( trkcands.get(i) ) ) continue; //If we have already found a track that is better than this candidate, there is no need to re-analyze it.
                
                this.getOverlapLists(trkcands.get(i), trkcands, list, plane); //Compare trkcands.get(i) with all tracks to find overlaps... list must contains at least trkcands.get(i)
                
                Track selectedTrk = this.FindBestTrack(list); //Return the best track among the candidates. If only trkcands.get(i) in list, it will return trkcands.get(i)
               
                if(selectedTrk==null&&list.size()>0) System.out.println("Houston... there is a problem with FindBestTrack in removeOverlappingTracks");
                
                if(selectedTracks.contains(selectedTrk)==false)
                        selectedTracks.add(selectedTrk);
               
                list.remove(selectedTrk);

                for( Track t : list ) {
                	if( ! rejected.contains(t) ) rejected.add(t);
                }

            }
           
            if(trkcands!=null)
                trkcands.removeAll(trkcands);
            
            if(selectedTracks!=null)
                trkcands.addAll(selectedTracks);
            
            //Let's assign the id of tracks since now they are supposed to be the best.
            for (int c = 0; c < trkcands.size(); c++) {
                trkcands.get(c).set_Id(c + 1);
            }
    }

    private void getOverlapLists(Track track, List<Track> trkcands, List<Track> list, String plane) {
    // --------------------------------------------------------------------
    //  two tracks are considered the same if they share at least 2 crosses
    // --------------------------------------------------------------------
		//NB: track is not excluded from trkcands... so list will at least contain track itself
    	
    	for( Track t : trkcands ) {
    		int N = 0;
    		int Nc = 0;
    		for( Cross c : t ) {
//    			if( track.contains(c)&& c.get_DetectorType().equals("Z") ) { N++;  } //For Micromegas... one cross is uniquely assigned to one cluster
//    			if (c.get_Detector().equals("SVT")) { //For SVT... a cluster can be assigned to several crosses
//    				for( Cross cr : track ) {
//    					if (cr.get_Detector().equals("SVT")) {
//    						if (cr.get_Cluster1().get_Id()==c.get_Cluster1().get_Id()) { N++;  } 
//    					}
//    				}
//    			}
    			if( track.contains(c)) {
    				if( c.get_Detector().equals("SVT") || c.get_DetectorType().equals("Z") ) N++;
    				if( c.get_DetectorType() == "C") Nc++;
    			}
    		}
    		if( plane.equalsIgnoreCase("XY") ) {
    			if( N >= 2 && Nc > 0) { list.add( t );}
    		}
    		else {
    			if( Nc > 0 ) { list.add( t );}
    		}

    	}
    	
    }

    private Track FindBestTrack(List<Track> trkList) {
    // --------------------------------------------------------------------
    //  Select the candidate with the highest number of NDF
    //    if two have the same ndf, get the one with the better chi2/ndf
    // --------------------------------------------------------------------
            double bestChi2 = trkList.get(0).getChi2();
            int ndf = trkList.get(0).getNDF();
            Track bestTrk = trkList.get(0);
            //The best and tallest track
            for (int i =0; i<trkList.size(); i++) {
//            	if (trkList.get(i).getNDF()>ndf||(trkList.get(i).getNDF()==ndf&&trkList.get(i).getChi2()<bestChi2)) {
            	if ((trkList.get(i).getChi2()/trkList.get(i).getNDF()<bestChi2/ndf)) {
            		ndf=trkList.get(i).getNDF();
            		bestTrk=trkList.get(i);
            		bestChi2=trkList.get(i).getChi2();
            	}
            }
            
            //Once we have the best and smallest track, we look if each point added to this track are not too far off (for the moment we set the limit at N_sigma_per_point)
            for(int i =0; i<trkList.size(); i++) {
            	if (trkList.get(i).getNDF()<ndf&&trkList.get(i).getChi2()<bestChi2+(trkList.get(i).getNDF()-ndf)* Constants.Nsigma_per_point* Constants.Nsigma_per_point){
            		ndf=trkList.get(i).getNDF();
            		bestTrk=trkList.get(i);
            		bestChi2=trkList.get(i).getChi2();
            	}
                   
            }
            return bestTrk;
    }

	public void updateCrosses(List<Track> trks, List<ArrayList<Cross>> crosses) {
				
		//Loop over the track
		for (int t = 0; t < trks.size(); t++) {
			//Loop ovver the measurements/state vectors
			for (int tp = 0; tp < trks.get(t).getTrajectory().size(); tp++) {

               if (trks.get(t).getTrajectory().get(tp).layer<7) {//It is SVT
                	for (int jj=0 ; jj < crosses.get(0).size(); jj++) {//So we loop over svt crosses
                		//If we have the cluster 1
                		if (trks.get(t).getTrajectory().get(tp).clusID==crosses.get(0).get(jj).get_Cluster1().get_Id()) {
                			crosses.get(0).get(jj).set_Point(new Point3D(trks.get(t).getTrajectory().get(tp).xdet,trks.get(t).getTrajectory().get(tp).ydet,trks.get(t).getTrajectory().get(tp).zdet));
            	   			crosses.get(0).get(jj).set_Dir(new Vector3D(trks.get(t).getTrajectory().get(tp).dirx,trks.get(t).getTrajectory().get(tp).diry,trks.get(t).getTrajectory().get(tp).dirz));
            	   			crosses.get(0).get(jj).get_Cluster1().set_CentroidResidual(trks.get(t).getTrajectory().get(tp).residual);
            	   			crosses.get(0).get(jj).get_Cluster1().set_SeedResidual(trks.get(t).getTrajectory().get(tp).excl_residual);
            	   			crosses.get(0).get(jj).get_Cluster1().set_AssociatedTrackID(trks.get(t).get_Id());
            	   			crosses.get(0).get(jj).set_AssociatedTrackID(trks.get(t).get_Id());
            	   			for (FittedHit h : crosses.get(0).get(jj).get_Cluster1()) {
                                h.set_AssociatedTrackID(trks.get(t).get_Id());
                            }
        					
                		}
                		
                		
                	}
               }
               else {
            	   	for (int jj=0 ; jj < crosses.get(1).size(); jj++) {//So we loop over bmt crosses
            	   		if (trks.get(t).getTrajectory().get(tp).clusID==crosses.get(1).get(jj).get_Cluster1().get_Id()) {
            	   			crosses.get(1).get(jj).set_Point(new Point3D(trks.get(t).getTrajectory().get(tp).xdet,trks.get(t).getTrajectory().get(tp).ydet,trks.get(t).getTrajectory().get(tp).zdet));
            	   			crosses.get(1).get(jj).set_Dir(new Vector3D(trks.get(t).getTrajectory().get(tp).dirx,trks.get(t).getTrajectory().get(tp).diry,trks.get(t).getTrajectory().get(tp).dirz));
            	   			crosses.get(1).get(jj).get_Cluster1().set_CentroidResidual(trks.get(t).getTrajectory().get(tp).residual);
            	   			crosses.get(1).get(jj).get_Cluster1().set_SeedResidual(trks.get(t).getTrajectory().get(tp).excl_residual);
            	   			crosses.get(1).get(jj).get_Cluster1().set_AssociatedTrackID(trks.get(t).get_Id());
            	   			crosses.get(1).get(jj).set_AssociatedTrackID(trks.get(t).get_Id());
            	   			for (FittedHit h : crosses.get(1).get(jj).get_Cluster1()) {
                                h.set_AssociatedTrackID(trks.get(t).get_Id());
                            }
            	   		}
            	   	}
               }
               
			}
		}
		
	}

     public void FinalizeTrackToCTOF_CND(List<Track> trks,  CTOFGeant4Factory ctof_geo, Detector cnd_geo, Swim swimmer) {
       
        double CTOF_radius=ctof_geo.getRadius(1);
        double px = 0;
        double py = 0;
        double pz = 0;
        org.jlab.rec.cvt.track.fit.StateVecs sv=new org.jlab.rec.cvt.track.fit.StateVecs();//Stupid class... need to change it when I am bored.
        
        double maxPathLength = 10;//very loose cut 
        double accuracy = 0.001; // accuracy of stopper in cm
        double stepsize = 0.1; // initial step size
       
        for (int t = 0; t < trks.size(); t++) {
        	boolean keep_swimming=true;// boolean to kill the search of intersection if swimmer cannot catch the previous layer
            int trksize = trks.get(t).getTrajectory().size();
            StateVec ctof_inter=sv.new StateVec(trksize +1);
            StateVec[] cnd_inter=new StateVec[cnd_geo.getSector(0).getSuperlayer(0).getNumLayers()];
            Helix particle=sv.setSvPars(trks.get(t).getTrajectory().get(trksize - 1), new Matrix(3,3));
            double csAtRadius=particle.getCurvAbsAtRadius(CTOF_radius*10, new Vector3D(0,0,0));
            Vector3D intersection=particle.getHelixPoint(csAtRadius);
            Vector3D dir_intersection=particle.getHelixDir(csAtRadius);
            
            ctof_inter.sector=1; ctof_inter.layer=1;ctof_inter.DetectorType=DetectorType.CTOF.getDetectorId();
            ctof_inter.xdet=intersection.x();ctof_inter.ydet=intersection.y();ctof_inter.zdet=intersection.z();
            ctof_inter.dirx=dir_intersection.x();ctof_inter.diry=dir_intersection.y();ctof_inter.dirz=dir_intersection.z();
            ctof_inter.pathlength=csAtRadius/10. + trks.get(t).getTrajectory().get(trksize - 1).pathlength / 10.;
                        
            trks.get(t).getTrajectory().add(ctof_inter);
            trks.get(t).set_TrackDirAtCTOFRadius(new Vector3D(ctof_inter.dirx,ctof_inter.diry,ctof_inter.dirz));
            trks.get(t).set_TrackPointAtCTOFRadius(new Point3D(new Vector3D(ctof_inter.xdet,ctof_inter.ydet,ctof_inter.zdet)));
            trks.get(t).set_pathLength(ctof_inter.pathlength);
            
           for(int ilayer=0; ilayer<cnd_geo.getSector(0).getSuperlayer(0).getNumLayers(); ilayer++) {
            	trksize = trks.get(t).getTrajectory().size();
            	if (!keep_swimming) break;
            	cnd_inter[ilayer]= sv.new StateVec(trksize +1);
            	Point3D center = cnd_geo.getSector(0).getSuperlayer(0).getLayer(ilayer).getComponent(0).getMidpoint();
            	double radius  = Math.sqrt(center.x()*center.x()+center.y()*center.y());
            	csAtRadius=particle.getCurvAbsAtRadius(radius*10, new Vector3D(0,0,0));
            	intersection=particle.getHelixPoint(csAtRadius);
                dir_intersection=particle.getHelixDir(csAtRadius);
            	cnd_inter[ilayer].layer=ilayer; cnd_inter[ilayer].sector=1;cnd_inter[ilayer].DetectorType=DetectorType.CND.getDetectorId();
            	cnd_inter[ilayer].xdet=intersection.x();cnd_inter[ilayer].ydet=intersection.y();cnd_inter[ilayer].zdet=intersection.z();
                cnd_inter[ilayer].dirx=dir_intersection.x();cnd_inter[ilayer].diry=dir_intersection.y();cnd_inter[ilayer].dirz=dir_intersection.z();
            	cnd_inter[ilayer].pathlength=csAtRadius/10. + trks.get(t).getTrajectory().get(trksize - 1).pathlength;
            	trks.get(t).getTrajectory().add(cnd_inter[ilayer]);
            	
            }
        }

    }

}
