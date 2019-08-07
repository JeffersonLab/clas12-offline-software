package org.jlab.rec.cnd.cluster;

import java.io.IOException;
import java.util.ArrayList;
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
    
    public CNDClusterFinder() {
        // TODO Auto-generated constructor stub
    }
    
    public ArrayList<CNDCluster> findClusters(ArrayList<CndHit> hits) {
        ArrayList<CNDCluster> clusters = new ArrayList<CNDCluster>();
        
        //ArrayList<Hit> ctofhits = new ArrayList<Hit>();
        
        /// variables for clustering of hits
        int size = 0;
        double hitenergy_temp = 0.0;
        ArrayList<Integer> clusters_nhits;//hit multiplicity??
        ArrayList<Double>  clusters_energysum;
        ArrayList<Double>  clusters_hitenergy;
        ArrayList<Double>  clusters_x;
        ArrayList<Double>  clusters_y;
        ArrayList<Double>  clusters_z;
        ArrayList<Double>  clusters_time;
        ArrayList<Double>  clusters_xTimesEdep;
        ArrayList<Double>  clusters_yTimesEdep;
        ArrayList<Double>  clusters_zTimesEdep;
        ArrayList<Double>  clusters_timeTimesEdep;
        ArrayList<Integer> clusters_sector;
        ArrayList<Integer> clusters_layer;
        ArrayList<Integer> clusters_component;
        ArrayList<Integer> clusters_status;
        double[] closest_distance = new double[1];
        int[] subA = new int[1];
        int[] subB = new int[1];
        
        ArrayList<Integer> clusters_veto;
        ArrayList<Integer> clusters_layermultip;
        ArrayList<Integer> clusters_layer1;
        ArrayList<Integer> clusters_layer2;
        ArrayList<Integer> clusters_layer3;
        
        ///// get good hit informations for the clustering
        clusters_nhits = new ArrayList<Integer>();
        clusters_energysum = new ArrayList<Double>();
        clusters_hitenergy = new ArrayList<Double>();
        clusters_x = new ArrayList<Double>();
        clusters_y = new ArrayList<Double>();
        clusters_z = new ArrayList<Double>();
        clusters_time = new ArrayList<Double>();
        clusters_xTimesEdep = new ArrayList<Double>();
        clusters_yTimesEdep = new ArrayList<Double>();
        clusters_zTimesEdep = new ArrayList<Double>();
        clusters_timeTimesEdep = new ArrayList<Double>();
        clusters_sector = new ArrayList<Integer>();
        clusters_layer = new ArrayList<Integer>();
        clusters_component = new ArrayList<Integer>();
        clusters_status = new ArrayList<Integer>();
        
        
        //veto
        clusters_veto = new ArrayList<Integer>();
        //implement layer multiplicity
        clusters_layermultip = new ArrayList<Integer>();
        clusters_layer1 = new ArrayList<Integer>();
        clusters_layer2 = new ArrayList<Integer>();
        clusters_layer3 = new ArrayList<Integer>();
        

        
        
        size = hits.size();
        for (int i = 0; i < size; i++) {
            if( hits.get(i).Edep()<2.50 )continue; //energy threshold??
            clusters_nhits.add(1);
            //energysum and energy hit are the same?
            clusters_energysum.add(hits.get(i).Edep());
            clusters_hitenergy.add(hits.get(i).Edep());
            //// using the unit cm instead of mm, so divided by 10
            clusters_x.add(hits.get(i).X() /10.0);
            clusters_y.add(hits.get(i).Y() /10.0);
            clusters_z.add(hits.get(i).Z() /10.0);
            clusters_time.add(hits.get(i).Time());
            clusters_xTimesEdep.add(hits.get(i).X() /10.0*hits.get(i).Edep());
            clusters_yTimesEdep.add(hits.get(i).Y() /10.0*hits.get(i).Edep());
            clusters_zTimesEdep.add(hits.get(i).Z() /10.0*hits.get(i).Edep());
            clusters_timeTimesEdep.add(hits.get(i).Time()*hits.get(i).Edep());
            clusters_sector.add(hits.get(i).Sector());
            clusters_layer.add(hits.get(i).Layer());
            clusters_component.add(hits.get(i).Component());
            clusters_status.add(0);
            clusters_layermultip.add(1);
            clusters_veto.add(-99);
            
            
            if(hits.get(i).Layer()==1){
                clusters_layer1.add(1);
                clusters_layer2.add(0);
                clusters_layer3.add(0);
            }
            else if(hits.get(i).Layer()==2){
                clusters_layer1.add(0);
                clusters_layer2.add(1);
                clusters_layer3.add(0);
            }
            else{
                clusters_layer1.add(0);
                clusters_layer2.add(0);
                clusters_layer3.add(1);
            }
            
        }
        
        //// clustering of the CND hits
        /// only one hit, no need to do clustering
        if(clusters_nhits.size()==1){
            //// do nothing.
        }
        ///the case for two good cnd hits
        else if(clusters_nhits.size()==2){
            /// combine the two hits or not... s=sqrt(DeltaX^2 + DeltaY^2 + DeltaZ^2 + DeltaT^2) ???
            double distance = sqrt( (clusters_x.get(0)-clusters_x.get(1))*(clusters_x.get(0)-clusters_x.get(1))/sigmaX(clusters_x.get(0))/sigmaX(clusters_x.get(1))
                                   +(clusters_y.get(0)-clusters_y.get(1))*(clusters_y.get(0)-clusters_y.get(1))/sigmaY(clusters_y.get(0))/sigmaY(clusters_y.get(1))
                                   +(clusters_z.get(0)-clusters_z.get(1))*(clusters_z.get(0)-clusters_z.get(1))/sigmaZ(clusters_z.get(0))/sigmaZ(clusters_z.get(1))
                                   +(clusters_time.get(0)-clusters_time.get(1))*(clusters_time.get(0)-clusters_time.get(1))/sigmaTime(clusters_time.get(0))/sigmaTime(clusters_time.get(1)) );
            //the 5 thing criteria...
            if(distance < cluster_size_){
                
                
                
                
                if(clusters_layer1.get(0) ==1 || clusters_layer1.get(1)==1){
                    clusters_layer1.set(0, 1);
                }
                if(clusters_layer2.get(0) ==1 || clusters_layer2.get(1)==1){
                    clusters_layer2.set(0, 1);
                }
                if(clusters_layer3.get(0) ==1 || clusters_layer3.get(1)==1){
                    clusters_layer3.set(0, 1);
                }
                
                
                if(clusters_layer.get(0) == clusters_layer.get(1)){
                    clusters_layermultip.set(0, clusters_layermultip.get(1) );
                }
                else{
                    clusters_layermultip.set(0, clusters_layermultip.get(0) + clusters_layermultip.get(1) );
                }
                
                if(clusters_energysum.get(0)<clusters_energysum.get(1)){
                    clusters_sector.set(0, clusters_sector.get(1) );
                    clusters_layer.set(0, clusters_layer.get(1) );
                    clusters_component.set(0, clusters_component.get(1) );
                }
                
                /// the cluster information takes the Edep-weighted average
                clusters_x.set(0, (clusters_x.get(0)*clusters_energysum.get(0) + clusters_x.get(1)*clusters_energysum.get(1))
                               / (clusters_energysum.get(0) + clusters_energysum.get(1)) );
                clusters_y.set(0, (clusters_y.get(0)*clusters_energysum.get(0) + clusters_y.get(1)*clusters_energysum.get(1))
                               / (clusters_energysum.get(0) + clusters_energysum.get(1)) );
                clusters_z.set(0, (clusters_z.get(0)*clusters_energysum.get(0) + clusters_z.get(1)*clusters_energysum.get(1))
                               / (clusters_energysum.get(0) + clusters_energysum.get(1)) );
                clusters_time.set(0, (clusters_time.get(0)*clusters_energysum.get(0) + clusters_time.get(1)*clusters_energysum.get(1))
                                  / (clusters_energysum.get(0) + clusters_energysum.get(1)) );
                clusters_nhits.set(0, clusters_nhits.get(0) + 1);
                clusters_energysum.set(0, clusters_energysum.get(0) + clusters_energysum.get(1));
                if(clusters_status.get(1) !=0)clusters_status.set(0, clusters_status.get(1));
                
                
                clusters_veto.set(0,cluster_veto(clusters_energysum.get(0),clusters_nhits.get(0),clusters_layermultip.get(0)));
                
                
                clusters_nhits.remove(1);
                clusters_energysum.remove(1);
                clusters_x.remove(1);
                clusters_y.remove(1);
                clusters_z.remove(1);
                clusters_time.remove(1);
                clusters_sector.remove(1);
                clusters_layer.remove(1);
                //## layer multiplicity for 2 hits is trivial
                clusters_layermultip.remove(1);
                clusters_layer1.remove(1);
                clusters_layer2.remove(1);
                clusters_layer3.remove(1);
                clusters_component.remove(1);
                clusters_status.remove(1);
                clusters_veto.remove(1);
            }
        }
        //// more than two cnd hits
        //// hierarchiral clustering
        else if(clusters_nhits.size()>2){
            int clusters_number = clusters_x.size();
            while(true){
                closest_distance[0] = 1.0e15;
                subA[0] = -1;
                subB[0] = -1;
                find_closest(0, clusters_x, clusters_y, clusters_z, clusters_time, subA, subB, closest_distance);
                if(subA[0]==-1 || subB[0]==-1)break;
                else{
                    /*int layer1 =0;
                     int layer2 =0;
                     int layer3 =0;*/
                    
                    int clusters_number_now = clusters_x.size();
                    
                    clusters_x.set(subA[0],
                                   (clusters_x.get(subA[0])*(1+clusters_number-clusters_number_now)+clusters_x.get(subB[0]))/(2.0+clusters_number-clusters_number_now) );
                    clusters_y.set(subA[0],
                                   (clusters_y.get(subA[0])*(1+clusters_number-clusters_number_now)+clusters_y.get(subB[0]))/(2.0+clusters_number-clusters_number_now) );
                    clusters_z.set(subA[0],
                                   (clusters_z.get(subA[0])*(1+clusters_number-clusters_number_now)+clusters_z.get(subB[0]))/(2.0+clusters_number-clusters_number_now) );
                    clusters_time.set(subA[0],
                                      (clusters_time.get(subA[0])*(1+clusters_number-clusters_number_now)+clusters_time.get(subB[0]))/(2.0+clusters_number-clusters_number_now) );
                    clusters_xTimesEdep.set(subA[0], clusters_xTimesEdep.get(subA[0]) + clusters_xTimesEdep.get(subB[0]) );
                    clusters_yTimesEdep.set(subA[0], clusters_yTimesEdep.get(subA[0]) + clusters_yTimesEdep.get(subB[0]) );
                    clusters_zTimesEdep.set(subA[0], clusters_zTimesEdep.get(subA[0]) + clusters_zTimesEdep.get(subB[0]) );
                    clusters_timeTimesEdep.set(subA[0], clusters_timeTimesEdep.get(subA[0]) + clusters_timeTimesEdep.get(subB[0]) );
                    
                    /* if(clusters_layer.get(subA[0]) == 1 || clusters_layer.get(subB[0]) ==1){
                     layer1 =1;
                     }
                     if(clusters_layer.get(subA[0]) == 2 || clusters_layer.get(subB[0]) ==2){
                     layer2 =1;
                     }
                     if(clusters_layer.get(subA[0]) == 3 || clusters_layer.get(subB[0]) ==3){
                     layer3 =1;
                     }
                     
                     clusters_layermultip.set(subA[0], layer1+layer2+layer3 );*/
                    
                    
                    if(clusters_layer1.get(subA[0]) ==1 || clusters_layer1.get(subB[0])==1){
                        clusters_layer1.set(subA[0], 1);
                    }
                    if(clusters_layer2.get(subA[0]) ==1 || clusters_layer2.get(subB[0])==1){
                        clusters_layer2.set(subA[0], 1);
                    }
                    if(clusters_layer3.get(subA[0]) ==1 || clusters_layer3.get(subB[0])==1){
                        clusters_layer3.set(subA[0], 1);
                    }
                    
                    
                    if(clusters_layer.get(subA[0]) != clusters_layer.get(subB[0])){
                        
                        if(clusters_layermultip.get(subA[0])==1)
                        {
                            clusters_layermultip.set(subA[0], clusters_layermultip.get(subA[0]) + clusters_layermultip.get(subB[0]) );
                        }
                        
                        else if(clusters_layermultip.get(subA[0])==2){
                            
                            if((clusters_layer1.get(subA[0])==1 && clusters_layer.get(subB[0])==1) || (clusters_layer2.get(subA[0])==1 && clusters_layer.get(subB[0])==2) || (clusters_layer3.get(subA[0])==1 && clusters_layer.get(subB[0])==3)){
                                continue;
                            }
                            else{
                                clusters_layermultip.set(subA[0], clusters_layermultip.get(subA[0]) + clusters_layermultip.get(subB[0]) );
                            }
                            
                        }
                        
                        else{
                            continue;
                        }
                        
                    }
                    else{
                        continue;
                    }
                    
                    
                    
                    
                    
                    
                    //// mark down the sector, layer, component of the dominant hit
                    if(clusters_hitenergy.get(subA[0]) < clusters_hitenergy.get(subB[0])){
                        clusters_sector.set(subA[0], clusters_sector.get(subB[0]) );
                        clusters_layer.set(subA[0], clusters_layer.get(subB[0]) );
                        clusters_component.set(subA[0], clusters_component.get(subB[0]) );
                        clusters_hitenergy.set(subA[0], clusters_hitenergy.get(subB[0]) );
                    }
                    if(clusters_status.get(subB[0]) !=0) clusters_status.set(subA[0], clusters_status.get(subB[0]));
                    clusters_nhits.set(subA[0], clusters_nhits.get(subA[0]) + 1);
                    clusters_nhits.remove(subB[0]);
                    clusters_energysum.set(subA[0], clusters_energysum.get(subA[0])+clusters_energysum.get(subB[0]));
                    
                    clusters_veto.set(subA[0],cluster_veto(clusters_energysum.get(subA[0]),clusters_nhits.get(subA[0]),clusters_layermultip.get(subA[0])));
                    
                    
                    clusters_energysum.remove(subB[0]);
                    clusters_hitenergy.remove(subB[0]);
                    clusters_x.remove(subB[0]);
                    clusters_y.remove(subB[0]);
                    clusters_z.remove(subB[0]);
                    clusters_time.remove(subB[0]);
                    clusters_xTimesEdep.remove(subB[0]);
                    clusters_yTimesEdep.remove(subB[0]);
                    clusters_zTimesEdep.remove(subB[0]);
                    clusters_timeTimesEdep.remove(subB[0]);
                    clusters_sector.remove(subB[0]);
                    clusters_layer.remove(subB[0]);
                    //*********
                    clusters_layermultip.remove(subB[0]);
                    clusters_layer1.remove(subB[0]);
                    clusters_layer2.remove(subB[0]);
                    clusters_layer3.remove(subB[0]);
                    //*********
                    clusters_component.remove(subB[0]);
                    clusters_status.remove(subB[0]);
                    clusters_veto.remove(subB[0]);
                }
            }
            
            /// calculate the Edep-weighted average
            for(int i = 0; i < clusters_x.size(); i++){
                clusters_x.set(i, clusters_xTimesEdep.get(i)/clusters_energysum.get(i));
                clusters_y.set(i, clusters_yTimesEdep.get(i)/clusters_energysum.get(i));
                clusters_z.set(i, clusters_zTimesEdep.get(i)/clusters_energysum.get(i));
                clusters_time.set(i, clusters_timeTimesEdep.get(i)/clusters_energysum.get(i));
            }
            
        }//end of hirerchical clustering!
        
        
        for(int i = 0; i < clusters_x.size(); i++){
            CNDCluster acluster = new CNDCluster(i+1, clusters_sector.get(i), clusters_layer.get(i) );
            acluster.set_component(clusters_component.get(i));
            acluster.set_x(clusters_x.get(i));
            acluster.set_y(clusters_y.get(i));
            acluster.set_z(clusters_z.get(i));
            acluster.set_time(clusters_time.get(i));
            acluster.set_nhits(clusters_nhits.get(i));
            acluster.set_energysum(clusters_energysum.get(i));
            acluster.set_status(clusters_status.get(i));
            //add veto
            acluster.set_layermultip(clusters_layermultip.get(i));
            acluster.set_veto(clusters_veto.get(i));
            acluster.set_layer1(clusters_layer1.get(i));
            acluster.set_layer2(clusters_layer2.get(i));
            acluster.set_layer3(clusters_layer3.get(i));
            
            clusters.add(acluster);
        }
        
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
    private void find_closest(int begin, ArrayList<Double> x, ArrayList<Double> y, ArrayList<Double> z, ArrayList<Double> time,
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
    }
    
    
    
    /// Kitty's veto implementation
    private int cluster_veto(double energysum, int nhits, int layermultip){
        
        int alpha=0;//to be used with a binary AND
        
        //CND related condition for CND only veto
        if((energysum>10 && nhits<3)  ||  (energysum<=10 && nhits<4)){
            alpha++;
        }
        //CND related condition for CND and CTOF veto
        if((energysum<30 && layermultip==1)){
            alpha++;
            alpha++;
        }
        if(((energysum)<10 && nhits<3 && layermultip==2)){
            alpha++;
            alpha++;
            alpha++;
            alpha++;
        }
        
        
        return alpha;
        
    }
    
    

    
    
}

