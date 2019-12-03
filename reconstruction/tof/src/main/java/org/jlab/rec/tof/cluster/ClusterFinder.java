package org.jlab.rec.tof.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.lang.Math.sqrt;
import java.util.stream.IntStream;

import org.jlab.rec.tof.hit.AHit;
import org.jlab.rec.tof.hit.ctof.Hit;
/**
 *
 * @author ziegler
 * @author ajhobart
 *
 */
public class ClusterFinder {
    
    private double cluster_size_ = 3;
    
    public ClusterFinder() {
        clusters_hitenergy     = new ArrayList<Double>();
        clusters_x             = new ArrayList<Double>();
        clusters_y             = new ArrayList<Double>();
        clusters_z             = new ArrayList<Double>();
        clusters_time          = new ArrayList<Double>();
        clusters_ID            = new ArrayList<Integer>();
        clusters_sector        = new ArrayList<Integer>();
        clusters_panel         = new ArrayList<Integer>();
        clusters_dummy         = new ArrayList<Integer>();           
    }
    
    ArrayList<Double>  clusters_hitenergy;
    ArrayList<Double>  clusters_x;
    ArrayList<Double>  clusters_y;
    ArrayList<Double>  clusters_z;
    ArrayList<Double>  clusters_time;
    ArrayList<Integer> clusters_ID;
    ArrayList<Integer> clusters_sector;
    ArrayList<Integer> clusters_panel;
    ArrayList<Integer> clusters_dummy; //define: a dummy variable to track which hit has been clustered and to which cluster it is associated
    
    /**
     * 
     * @param hits2 list of hits
     * @return list of clusters
     */
    public ArrayList<Cluster> findClusters(List<?> hits2) {
        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
        if (hits2 != null) {
            
            clusters_hitenergy.clear();
            clusters_x.clear();
            clusters_y.clear();
            clusters_z.clear();
            clusters_time.clear();
            clusters_ID.clear();
            clusters_sector.clear();
            clusters_panel.clear();
            clusters_dummy.clear();
            
            int cid = 1; // cluster id, will increment with each new good cluster
            
            //array of hits ID
            ArrayList<ArrayList<Integer>> clusters_ID_array = new ArrayList<ArrayList<Integer>>();
            
            //minimum energy cut to pass the cluster
            double minEn=(hits2.get(0) instanceof org.jlab.rec.tof.hit.ftof.Hit)?
            org.jlab.rec.ftof.Constants.MINENERGY:org.jlab.rec.ctof.Constants.MINENERGY;
            
            for (int i = 0; i < hits2.size(); i++) {
                if (((AHit) hits2.get(i)).get_Energy() < minEn) {
                    continue;
                }
                //if(hits2.get(0) instanceof org.jlab.rec.tof.hit.ctof.Hit)
                //    System.out.println("passed hit :"+(((AHit) hits2.get(i)).get_Id())+", "
                //        +(((AHit) hits2.get(i)).get_Panel())+ " position "+((AHit) hits2.get(i)).get_Position().toString());
                // using the unit cm instead of mm, so divided by 10
                // the units are already in cm ? then move to cm do not devide by 10
                clusters_hitenergy.add (((AHit) hits2.get(i)).get_Energy());
                //clusters_x.add         (((AHit) hits2.get(i)).get_Position().x() /10.0);
                //clusters_y.add         (((AHit) hits2.get(i)).get_Position().y() /10.0);
                //clusters_z.add         (((AHit) hits2.get(i)).get_Position().z() /10.0);
                clusters_x.add         (((AHit) hits2.get(i)).get_Position().x());
                clusters_y.add         (((AHit) hits2.get(i)).get_Position().y());
                clusters_z.add         (((AHit) hits2.get(i)).get_Position().z());
                clusters_time.add      (((AHit) hits2.get(i)).get_t());
                clusters_ID.add        (((AHit) hits2.get(i)).get_Id());
                clusters_sector.add    (((AHit) hits2.get(i)).get_Sector());
                clusters_panel.add     (((AHit) hits2.get(i)).get_Panel());
                
                clusters_dummy.add(-99);
                
                
            }
            

            
            if(clusters_x.size()==1){
                //// do nothing.
                ArrayList<Integer> dummy = new ArrayList<Integer>();
                dummy.add(clusters_ID.get(0));
                clusters_ID_array.add(dummy);
                clusters_dummy.set(0,0);
                
            }
            else if(clusters_x.size()==2){
                //double distance = sqrt( (clusters_x.get(0)-clusters_x.get(1))*(clusters_x.get(0)-clusters_x.get(1))/sigmaX(clusters_x.get(0))/sigmaX(clusters_x.get(1))
                //                       +(clusters_y.get(0)-clusters_y.get(1))*(clusters_y.get(0)-clusters_y.get(1))/sigmaY(clusters_y.get(0))/sigmaY(clusters_y.get(1))
                //                       +(clusters_z.get(0)-clusters_z.get(1))*(clusters_z.get(0)-clusters_z.get(1))/sigmaZ(clusters_z.get(0))/sigmaZ(clusters_z.get(1))
                //                       +(clusters_time.get(0)-clusters_time.get(1))*(clusters_time.get(0)-clusters_time.get(1))/sigmaTime(clusters_time.get(0))/sigmaTime(clusters_time.get(1)) );
                
                double distance = this.calc_distance(clusters_x.get(0), clusters_x.get(1),
                        clusters_y.get(0), clusters_y.get(1),
                        clusters_z.get(0), clusters_z.get(1),
                        clusters_time.get(0), clusters_time.get(1));
                
                if(distance < cluster_size_){
                    
                    if(clusters_hitenergy.get(0) < clusters_hitenergy.get(1)){
                        clusters_x.set(0, clusters_x.get(1));
                        clusters_y.set(0, clusters_y.get(1));
                        clusters_z.set(0, clusters_z.get(1));
                        clusters_time.set(0, clusters_time.get(1));
                        
                        clusters_sector.set(0, clusters_sector.get(1));
                        clusters_panel.set(0, clusters_panel.get(1));
                    }
                    ArrayList<Integer> dummy = new ArrayList<Integer>();
                    dummy.add(clusters_ID.get(0));
                    dummy.add(clusters_ID.get(1));
                    clusters_ID_array.add(dummy);
                    
                    clusters_dummy.set(0,0);
                    
                    clusters_hitenergy.remove(1);
                    clusters_x.remove(1);
                    clusters_y.remove(1);
                    clusters_z.remove(1);
                    clusters_time.remove(1);
                    clusters_ID.remove(1);
                    clusters_dummy.remove(1);
                    clusters_sector.remove(1);
                    clusters_panel.remove(1);
                    
                    
                }
                else{
                    ArrayList<Integer> dummy = new ArrayList<Integer>();
                    dummy.add(clusters_ID.get(0));
                    ArrayList<Integer> dummy1 = new ArrayList<Integer>();
                    dummy1.add(clusters_ID.get(1));
                    clusters_ID_array.add(dummy);
                    clusters_ID_array.add(dummy1);
                    clusters_dummy.set(0,0);
                    clusters_dummy.set(1,1);
                    
                    
                }
                
                
                
            }
            else{
                //// hierarchiral clustering
                // put comments: group hits based on the separating distance between them
                double[] closest_distance = new double[1];
                int[] subA = new int[1]; //define: index of hit 1 in the container of hits
                int[] subB = new int[1]; //define: index of hit 2 that is the closest to hit 1 in the container of hits
                int counter=-1;
                
                while(true){
                    closest_distance[0] = 1.0e15;
                    subA[0] = -1;
                    subB[0] = -1;
                    
                    
                    find_closest(0, clusters_x, clusters_y, clusters_z, clusters_time, subA, subB, closest_distance);
                    if(subA[0]==-1 || subB[0]==-1){break;} //the two hits did not satisfy the disctance condition, they are not grouped
                    else{
                        
                        // checking the value of dummy; if it is -99 then it means that the hit has not been clustered yet
                        // tha value of dummy gives the grouped hits index, each index corresponds to a cluster
                        if(clusters_dummy.get(subA[0])==-99 && clusters_dummy.get(subB[0])==-99)
                        {
                            counter++;
                            ArrayList<Integer> dummy = new ArrayList<Integer>();
                            dummy.add(clusters_ID.get(subA[0]));
                            dummy.add(clusters_ID.get(subB[0]));
                            clusters_ID_array.add(dummy);
                            clusters_dummy.set(subA[0], counter);
                        }
                        else if(clusters_dummy.get(subA[0])!=-99 && clusters_dummy.get(subB[0])==-99)
                        {
                            clusters_ID_array.get(clusters_dummy.get(subA[0])).add(clusters_ID.get(subB[0]));
                            
                        }
                        else if(clusters_dummy.get(subB[0])!=-99 && clusters_dummy.get(subA[0])==-99)
                        {
                            clusters_ID_array.get(clusters_dummy.get(subB[0])).add(clusters_ID.get(subA[0]));
                            clusters_dummy.set(subA[0],clusters_dummy.get(subB[0]));
                        }
                        else{
                            if(clusters_dummy.get(subA[0]) < clusters_dummy.get(subB[0]))
                            {
                                (clusters_ID_array.get(clusters_dummy.get(subA[0]))).addAll(clusters_ID_array.get(clusters_dummy.get(subB[0])));
                                clusters_ID_array.get(clusters_dummy.get(subB[0])).removeAll(clusters_ID_array.get(clusters_dummy.get(subB[0])));
                                clusters_ID_array.remove(clusters_ID_array.get(clusters_dummy.get(subB[0])));
                                
                                for(int unicorn=0;unicorn<clusters_dummy.size();unicorn++)
                                {
                                    
                                    if(clusters_dummy.get(unicorn)>clusters_dummy.get(subB[0]))
                                    {clusters_dummy.set(unicorn,clusters_dummy.get(unicorn)-1);}
                                    
                                }
                                
                            }
                            else /*if (clusters_dummy.get(subA[0]) > clusters_dummy.get(subB[0]))*/
                            {
                                (clusters_ID_array.get(clusters_dummy.get(subB[0]))).addAll(clusters_ID_array.get(clusters_dummy.get(subA[0])));
                                clusters_ID_array.get(clusters_dummy.get(subA[0])).removeAll(clusters_ID_array.get(clusters_dummy.get(subA[0])));
                                clusters_ID_array.remove(clusters_ID_array.get(clusters_dummy.get(subA[0])));
                                clusters_dummy.set(subA[0],clusters_dummy.get(subB[0]));
                                
                                for(int rainbow=0;rainbow<clusters_dummy.size();rainbow++)
                                {
                                    
                                    if(clusters_dummy.get(rainbow)>clusters_dummy.get(subA[0]))
                                    {clusters_dummy.set(rainbow,clusters_dummy.get(rainbow)-1);}
                                    
                                }
                                
                            }
                            
                            counter--;
                        }
                        
                        
                        
                        
                        //looking at seed hit
                        if(clusters_hitenergy.get(subA[0]) < clusters_hitenergy.get(subB[0])){
                            clusters_x.set(subA[0], clusters_x.get(subB[0]));
                            clusters_y.set(subA[0], clusters_y.get(subB[0]));
                            clusters_z.set(subA[0], clusters_z.get(subB[0]));
                            clusters_time.set(subA[0], clusters_time.get(subB[0]));
                            
                            clusters_sector.set(subA[0], clusters_sector.get(subB[0]));
                            clusters_panel.set(subA[0], clusters_panel.get(subB[0]));
                        }
                        
                        
                        
                        clusters_hitenergy.remove(subB[0]);
                        clusters_x.remove(subB[0]);
                        clusters_y.remove(subB[0]);
                        clusters_z.remove(subB[0]);
                        clusters_time.remove(subB[0]);
                        clusters_ID.remove(subB[0]);
                        clusters_dummy.remove(subB[0]);
                        clusters_sector.remove(subB[0]);
                        clusters_panel.remove(subB[0]);
                    }
                }
                
                
                //for loop on hits and check which hits are left unassociated and consider them as single-hit cluster
                //(this could actually replace the nhit=1 and nhit=2 conditions)
                for (int i = 0; i < clusters_x.size(); i++) {
                    if(clusters_dummy.get(i)==-99)
                    {
                        counter++;
                        ArrayList<Integer> dummy = new ArrayList<Integer>();
                        dummy.add(clusters_ID.get(i));
                        clusters_ID_array.add(dummy);
                    }
                    
                }
                
                
      
                
            }
            int index = 0;
            
            for (int i = 0; i < clusters_x.size(); i++) {
                ArrayList<AHit> hits = new ArrayList<AHit>();
                
                for (int j = 0; j < clusters_ID_array.get(i).size(); j++) {
                    
                    for(int k=0;k<hits2.size();k++)
                    {
                        if(((AHit) hits2.get(k)).get_Id()==clusters_ID_array.get(i).get(j)){
                            index = k;
                            break;
                        }
                    }
                    
                    AHit clusteredHit = (AHit) hits2.get(index);
                    hits.add(clusteredHit);
                    
                }
                // define new cluster (sector and panel corresponding to seed hit)
                Cluster this_cluster = new Cluster(clusters_sector.get(i), clusters_panel.get(i),cid++);
                
                // add hits to the cluster
                hits = this.HitsToCluster(hits);
                this_cluster.addAll(hits);
                // make arraylist, problematic maybe??
                for (AHit hit : this_cluster) {
                    hit.set_AssociatedClusterID(this_cluster.get_Id());
                }
                
                this_cluster.calc_coord();//replaced calc_Centroids
                this_cluster.matchToTrack();
                clusters.add(this_cluster);
                
                
            }
            
            
            
            
        }
        return clusters;
        
    }
    
    /**
     * 
     * @param hits list of hits
     * @return sorted list by paddle and then energy
     */
    private ArrayList<AHit> HitsToCluster(List<?> hits) {
        //Sort hits by energy
        ArrayList<AHit> hits2 = (ArrayList<AHit>) hits;
        hits2.sort(Comparator.comparing(AHit::get_Paddle).thenComparing(AHit::get_Energy));
        // Energy Selection goes here:
        return (ArrayList<AHit>) hits2;
    }
    
    /**
     * define parameters below:
     * @param begin
     * @param x array of hits
     * @param y array of hits
     * @param z array of hits
     * @param time array of hits
     * @param subA hit 1 index in array
     * @param subB hit 2 index in array
     * @param closest_distance 
     */
    private void find_closest(int begin, ArrayList<Double> x, ArrayList<Double> y, ArrayList<Double> z, ArrayList<Double> time,
                              int[] subA, int[] subB, double[] closest_distance){
        if((begin+1)>=x.size())return;
        
        for(int i=begin+1;i<x.size();i++){
            //double distance = sqrt( (x.get(begin)-x.get(i))*(x.get(begin)-x.get(i))/sigmaX(x.get(begin))/sigmaX(x.get(i))
            //                       +(y.get(begin)-y.get(i))*(y.get(begin)-y.get(i))/sigmaY(y.get(begin))/sigmaY(y.get(i))
            //                       +(z.get(begin)-z.get(i))*(z.get(begin)-z.get(i))/sigmaZ(z.get(begin))/sigmaZ(z.get(i))
            //                       +(time.get(begin)-time.get(i))*(time.get(begin)-time.get(i))/sigmaTime(time.get(begin))/sigmaTime(time.get(i)) );
            
            double distance = this.calc_distance(x.get(begin), x.get(i), 
                    y.get(begin), y.get(i), 
                    z.get(begin), z.get(i), 
                    time.get(begin), time.get(i));
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
    }
    
    
    
      /// resolutions of CTOF
      /// unit : cm
      private double sigmaX(double x){ return 5; }
      /// unit : cm
      private double sigmaY(double y){ return 1; }
      /// unit : cm
      private double sigmaZ(double z){ return 10; }
      /// unit : ns
        private double sigmaTime(double t){ return 0.3; }


    /**
     * 
     * @param x0
     * @param x1
     * @param y0
     * @param y1
     * @param z0
     * @param z1
     * @param t0
     * @param t1
     * @return weighted distance taking into account the detector uncertainties
     */  
    private double calc_distance(Double x0, Double x1, Double y0, Double y1, Double z0, Double z1, Double t0, Double t1) {
        return sqrt( (x0-x1)*(x0-x1)/sigmaX(x0)/sigmaX(x1)
            + (y0-y1)*(y0-y1)/sigmaY(y0)/sigmaY(y1)
            + (z0-z1)*(z0-z1)/sigmaZ(z0)/sigmaZ(z1)
            + (t0-t1)*(t0-t1)/sigmaTime(t0)/sigmaTime(t1) );

    }
    
    
}
