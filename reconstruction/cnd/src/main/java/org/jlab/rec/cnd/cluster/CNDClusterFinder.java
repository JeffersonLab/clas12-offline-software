package org.jlab.rec.cnd.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import org.jlab.utils.groups.IndexedList;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cnd.hit.CndHit;

import org.jlab.rec.cnd.cluster.CNDCluster;

import java.lang.String;
import java.lang.Double;
import java.lang.Integer;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import org.jlab.clas.physics.LorentzVector;


public class CNDClusterFinder {
    
    private double cluster_size_ = 5;//apparently 5 cm according to Rong, no!!!
    private boolean debug = false;
    
    
    
    public CNDClusterFinder() {
        // TODO Auto-generated constructor stub
    }
    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    
    public ArrayList<CNDCluster> findClusters(ArrayList<CndHit> hits) {
        
        
        
        // sort hits by energy
        Collections.sort(hits, new sortByEnergy());
        if(debug) {
            for(int i=0; i<hits.size(); i++) {
                CndHit thisHit=hits.get(i);
                System.out.println(thisHit.Sector() + " " + thisHit.Edep() + " " + thisHit.Time());
            }
        }
        
  
        
        ArrayList<CNDCluster> clusters = new ArrayList<CNDCluster>();
        
        IndexedList<ArrayList<CndHit>> clustered_hits = new IndexedList<ArrayList<CndHit>>(1);
        
        
        //loop over hits to group hits in clusters based on the mesure "distance", to be improved
        int j=0;
        double closest_distance = 1.0e15;
        int good_index = -1;
        int size = hits.size();
        
        
        for (int i = 0; i < size; i++) {
            
            if( hits.get(i).Edep()<0.5 )continue; //energy threshold
            
            CndHit thisHit = (CndHit) hits.get(i);
            
            closest_distance = 1.0e15;
            good_index = -1;
            
            for(int k =0; k<j/*clustered_hits.size()*/; k++)
            {
                
                for(int l=0; l<1/*clustered_hits.getItem(k).size()*/; l++)
                {
                    //l is always 0, check with seed hit!!
                    CndHit otherHit = (CndHit) clustered_hits.getItem(k).get(l);
                    
                    double x1 = thisHit.X()    /10.0;
                    double y1 = thisHit.Y()    /10.0;
                    double z1 = thisHit.Z()    /10.0;
                    double t1 = thisHit.Time() ;
                    
                    double x2 = otherHit.X()    /10.0;
                    double y2 = otherHit.Y()    /10.0;
                    double z2 = otherHit.Z()    /10.0;
                    double t2 = otherHit.Time() ;
                    
                    double distance = sqrt( (x1-x2)*(x1-x2)/sigmaX(x1)/sigmaX(x2)
                                           +(y1-y2)*(y1-y2)/sigmaY(y1)/sigmaY(y2)
                                           +(z1-z2)*(z1-z2)/sigmaZ(z1)/sigmaZ(z2)
                                           +(t1-t2)*(t1-t2)/sigmaTime(t1)/sigmaTime(t2) );
                    
                    if(distance > cluster_size_)
                    {
                        
                        continue;
                        
                    }
                    else
                    {
                        if(distance<closest_distance)
                        {
                            closest_distance = distance;
                            good_index = k;
                        }
                    }
                }
                
                
            }
            
            if(good_index==-1)
            {
                
                clustered_hits.add(new ArrayList<CndHit>(), j);
                clustered_hits.getItem(j).add(thisHit);
                j++;
                
            }
            else
            {
                
                clustered_hits.getItem(good_index).add(thisHit);
                
            }
            
            
            
        }
        
        
        
        // sort lists by energy
        if(debug) System.out.println("Sorted");
        for (int index = 0; index <j/*clustered_hits.size()*/; index++) {
            
            if(clustered_hits.hasItem(index)) {
                if(debug) System.out.println(index);
                ArrayList<CndHit> hitList = clustered_hits.getItem(index);
                Collections.sort(hitList, new sortByEnergy());
                if(debug) {
                    for(int i=0; i<hitList.size(); i++) {
                        CndHit thisHit=hitList.get(i);
                        System.out.println(thisHit.Sector() + " " + thisHit.Edep() + " " + thisHit.Time());
                    }
                }
            }
            
        }
        
        
        
        
        //calculate cluster parameters
        
        double energy_cluster;
        double pathlengththroughbar;
        
        for(int k =0; k<j/*clustered_hits.size()*/; k++)
        {
            
            energy_cluster=0;
            pathlengththroughbar=0;
            CndHit seedHit = (CndHit) clustered_hits.getItem(k).get(0);
            
            CNDCluster acluster = new CNDCluster(k+1, seedHit.Sector(), seedHit.Layer() );
            
            for(int l=0; l<clustered_hits.getItem(k).size(); l++)
            {
                CndHit theHit  = (CndHit) clustered_hits.getItem(k).get(l);
                
                theHit.set_AssociatedClusterID(k+1);
                
                energy_cluster += theHit.Edep();
                pathlengththroughbar += theHit.tLength()/10.0;
                
                
                if(theHit.Layer()==1){
                    acluster.set_layer1(1);
                }
                
                if(theHit.Layer()==2){
                    acluster.set_layer2(1);
                }
                
                if(theHit.Layer()==3){
                    acluster.set_layer3(1);
                }
                
                acluster.set_layermultip(acluster.get_layer1()+acluster.get_layer2()+acluster.get_layer3());
                acluster.set_component(seedHit.Component());
                acluster.set_x(seedHit.X()/10.0);
                acluster.set_y(seedHit.Y()/10.0);
                acluster.set_z(seedHit.Z()/10.0);
                acluster.set_time(seedHit.Time());
                acluster.set_nhits(clustered_hits.getItem(k).size());
                acluster.set_energysum(energy_cluster);
                acluster.set_status(0);
                acluster.set_pathLengthThruBar(pathlengththroughbar);
                
                
            }
            clusters.add(acluster);
            
        }
        
        
        
        
        
        /*/// calculate the Edep-weighted average
         for(int i = 0; i < clusters_x.size(); i++){
         clusters_x.set(i, clusters_xTimesEdep.get(i)/clusters_energysum.get(i));
         clusters_y.set(i, clusters_yTimesEdep.get(i)/clusters_energysum.get(i));
         clusters_z.set(i, clusters_zTimesEdep.get(i)/clusters_energysum.get(i));
         clusters_time.set(i, clusters_timeTimesEdep.get(i)/clusters_energysum.get(i));
         }*/
        
        
        
        
        
        return clusters;// returning ArrayList<CNDCluster> type; array of cluster found in the event
    }
    
    /// resolutions of CND hits
    /// unit : cm
    private double sigmaX(double x){ return 1.6; }
    /// unit : cm
    private double sigmaY(double y){ return 1.6; }
    /// unit : cm
    private double sigmaZ(double z){ return 3.0; }
    /// unit : ns
    private double sigmaTime(double t){ return 0.14; }
    /// unit : deg.
    private double sigmaTheta(double theta){ return 2.5; }
    /// unit : deg.
    private double sigmaPhi(double phi){ return 2.8; }
    /// beta = v/c.
    private double sigmaBeta(double beta){ return 0.065*beta; }
    
    /// find the two closest hits among all the hits
    /* private void find_closest(int begin, ArrayList<Double> x, ArrayList<Double> y, ArrayList<Double> z, ArrayList<Double> time,
     int[] subA, int[] subB, double[] closest_distance){
     if((begin+1)>=x.size())return;
     
     
     
     for(int i=begin+1;i<x.size();i++){
     double distance = sqrt( (x.get(begin)-x.get(i))*(x.get(begin)-x.get(i))/sigmaX(x.get(begin))/sigmaX(x.get(i))
     +(y.get(begin)-y.get(i))*(y.get(begin)-y.get(i))/sigmaY(y.get(begin))/sigmaY(y.get(i))
     +(z.get(begin)-z.get(i))*(z.get(begin)-z.get(i))/sigmaZ(z.get(begin))/sigmaZ(z.get(i))
     +(time.get(begin)-time.get(i))*(time.get(begin)-time.get(i))/sigmaTime(time.get(begin))/sigmaTime(time.get(i)) );
     
     
     if(distance > cluster_size_){
     //
     continue;
     }
     else{
     if(distance<closest_distance[0]){
     subA[0] =  begin ;
     subB[0] =  i ;
     closest_distance[0] = distance;
     }
     }
     }
     find_closest(begin+1, x, y, z, time, subA, subB, closest_distance);//recursive. Hirarchical clustering
     }*/
    
    class sortByEnergy implements Comparator<CndHit> {
        @Override
        public int compare(CndHit a, CndHit b) {
            if(a.Edep()<b.Edep()) {
                    return 1;
            } else {
                    return -1;
            }
        }
    }
    
    
}

