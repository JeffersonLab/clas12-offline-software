/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.fvt.banks;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.fvt.fmt.Constants;
import static org.jlab.rec.fvt.fmt.Constants.FVT_Nlayers;
import org.jlab.rec.fvt.fmt.cluster.Cluster;
import org.jlab.rec.fvt.track.Track;

/**
 *
 * @author ziegler
 */
public class InputBanksReader {
    
    private DCSwimmer swim;
    public InputBanksReader() {
        this.swim = new DCSwimmer();
       
    }
    private final double LIGHTVEL     = 0.000299792458 ; 
    public List<Track> getTracks(DataEvent event, List<Cluster> FMTclusters, DCSwimmer swim) {
       
        if (event.hasBank("TimeBasedTrkg::TBTracks") == false) {
          // System.err.println(" no tracks");
            return null;
        }

        List<Track> DCtracks= new ArrayList<Track>();
        DataBank bank = event.getBank("TimeBasedTrkg::TBTracks");
        //DataBank bankMC = event.getBank("MC::Particle");
	DataBank config = event.getBank("RUN::config");
        double B = 5.*config.getFloat("solenoid",0);
        
        int nt = bank.rows();   // number of tracks in the cvt event

        for(int i = 0; i<nt; i++){
            Track trk = new Track();
            trk.set_Id((int) bank.getInt("id", i ) );
            int sector = bank.getByte("sector", i) ;     
            int q = bank.getByte("q", i) ;        
            double x0 = bank.getFloat("Vtx0_x", i);
            double y0 = bank.getFloat("Vtx0_y", i);
            double z0 = bank.getFloat("Vtx0_z", i);
            double px0 = bank.getFloat("p0_x", i);
            double py0 = bank.getFloat("p0_y", i);
            double pz0 = bank.getFloat("p0_z", i);
            /*
            x0 = bankMC.getFloat("vx", 0)/10.;
            y0 = bankMC.getFloat("vy", 0)/10.;
            z0 = bankMC.getFloat("vz", 0)/10.;
            px0 = bankMC.getFloat("px", 0);
            py0 = bankMC.getFloat("py", 0);
            pz0 = bankMC.getFloat("pz", 0);
            */
            trk.set_Sector(sector);
            trk.setQ(q);
            trk.setX(x0);
            trk.setY(y0);
            trk.setZ(z0);
            trk.setPx(px0);
            trk.setPy(py0);
            trk.setPz(pz0);
            
            this.swim.SetSwimParameters(x0, y0, z0, px0, py0, pz0, q);
            List<Cluster> matches = matchClusters(FMTclusters, trk);
            if(matches!=null)
                trk.set_Clusters(matches);
            DCtracks.add(trk);
        }
        return DCtracks;
    }
    
    private double[] swimpars ;
    public int findNearestStrip(Track track, int i, int layer) {
        //swim through each FMT plane and find the nearest strip
        swimpars = swim.SwimToPlaneBoundary(Constants.FVT_Zlayer[layer-1], new Vector3D(0,0,1),1);
                
        double x  = swimpars[0]; // convert back to cm
        double y  = swimpars[1]; // convert back to cm
        
        int ClosestStrip = -1;
        
        if(Math.sqrt(x*x+y*y)<Constants.FVT_Rmax && Math.sqrt(x*x+y*y)>Constants.FVT_Beamhole) {
	
            double x_loc = x*Math.cos(Constants.FVT_Alpha[layer-1])+y*Math.sin(Constants.FVT_Alpha[layer-1]);
            double y_loc = y*Math.cos(Constants.FVT_Alpha[layer-1])-x*Math.sin(Constants.FVT_Alpha[layer-1]);
            if(y_loc>-(Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.) && y_loc < (Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.)){ 
                if (x_loc<=0) 
                    ClosestStrip = (int) Math.floor(((Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.)-y_loc)/Constants.FVT_Pitch) + 1;
                if (x_loc>0) 
                    ClosestStrip = (int) (Math.floor((y_loc+(Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.))/Constants.FVT_Pitch) + 1 
                          + Constants.FVT_Halfstrips +0.5*( Constants.FVT_Nstrips-2.*Constants.FVT_Halfstrips)); 
            } else if(y_loc <= -(Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.) && y_loc > -Constants.FVT_Rmax){ 
                ClosestStrip = (int) Math.floor(((Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.)-y_loc)/Constants.FVT_Pitch) + 1; 
            }
            else if(y_loc >= (Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.) && y_loc < Constants.FVT_Rmax){ 
                ClosestStrip = (int) (Math.floor((y_loc+(Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.))/Constants.FVT_Pitch) + 1 
                      + Constants.FVT_Halfstrips+0.5*( Constants.FVT_Nstrips-2.*Constants.FVT_Halfstrips));  
            }
        } 
        return ClosestStrip;
    }
    
    public List<Cluster> matchClusters(List<Cluster> FMTClusters, Track track) {
        if(FMTClusters==null || FMTClusters.size()==0)
            return null;
        
        Collections.sort(FMTClusters);
        
        List<Cluster> matchedClusters = new ArrayList<Cluster>();
        List<Cluster> LayerClusters = new ArrayList<Cluster>();
        for(int i=0;i<FVT_Nlayers;i++) { 
		int strip = findNearestStrip( track, i, i+1);
		LayerClusters.clear();
                
                
                for(Cluster c : FMTClusters) { 
                    if(c.get_Layer()!=(i+1))
                        continue;
                    if(c.get_Layer()==(i+1))
                        LayerClusters.add(c);
                }
                Cluster cBest = this.findClosestCluster(LayerClusters, strip);
                if(cBest!=null)
                    if(Math.abs(cBest.get_SeedStrip()-strip) < 6)  {
                        //System.out.println("Matched cluster "+cBest.printInfo()+" to closest "+strip+" at loc "+
                         //       swimpars[0]+","+ swimpars[1]+","+ swimpars[2]);
                        cBest.setDCTraj(new Point3D(swimpars[0], swimpars[1], swimpars[2]));
                        matchedClusters.add(cBest);

                    }
                    
             
	}
        return matchedClusters;
    }

    private Cluster findClosestCluster(List<Cluster> LayerClusters, int strip) {
        int delStrip = 1025;
        Cluster cBest = null;
        for(Cluster c : LayerClusters) { 
            if(Math.abs(c.get_SeedStrip()-strip) <delStrip) {
                delStrip = Math.abs(c.get_SeedStrip()-strip) ;
                cBest = c;
            }                
        }
        
        return cBest;
    }
}
