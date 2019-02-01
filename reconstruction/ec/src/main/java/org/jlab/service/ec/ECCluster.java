package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 * @author lcsmith
 * 
 */

public class ECCluster {
    
    List<ECPeak>   clusterPeaks = new ArrayList<ECPeak>(); // shareEnergy does not update this !!
    
    int            clusterMultiplicity = 0;
    Point3D        clusterHitPosition  = new Point3D();
    double         clusterHitPositionError = 1000.0;    
    double         clusterEnergy = 0.0;                    // shareEnergy can update this !!
    
    public         int UVIEW_ID = -1;
    public         int VVIEW_ID = -1;
    public         int WVIEW_ID = -1;
       
    public ECCluster(ECPeak u, ECPeak v, ECPeak w){
        
        this.clusterPeaks.add(u);
        this.clusterPeaks.add(v);
        this.clusterPeaks.add(w);
        
        this.UVIEW_ID = u.getOrder();
        this.VVIEW_ID = v.getOrder();
        this.WVIEW_ID = w.getOrder();
        
        this.clusterMultiplicity = u.getMultiplicity() + 
                                   v.getMultiplicity() + 
                                   w.getMultiplicity();
        this.intersection();
    }
    
    public ECPeak getPeak(int view){
        return this.clusterPeaks.get(view);
    }
    
    public int getMultiplicity(){
        return this.clusterMultiplicity;
    }
    
    public double getRawEnergy(){
        return getRawEnergy(0)+getRawEnergy(1)+getRawEnergy(2); 
    }
    
    public double getRawEnergy(int view){
        return  this.clusterPeaks.get(view).getEnergy();
    } 
    
    public double getRawADC(int view){
        return  this.clusterPeaks.get(view).getADC();
    } 
    
    public void setEnergy(double energy){
        this.clusterEnergy = energy;
    }  
    
    public double getEnergy(){
        return this.clusterEnergy;       
    }
 
    public double getEnergy(int view){
        return this.clusterPeaks.get(view).getEnergy(clusterHitPosition);
    }  
    
	public double getTime() {
		// For cluster time use timing from U,V,W peak with largest reconstructed energy		
		if      ((this.getEnergy(0) > this.getEnergy(1)) && 
			     (this.getEnergy(0) > this.getEnergy(2))) return this.getTime(0);
		else if ((this.getEnergy(1) > this.getEnergy(0)) && 
				 (this.getEnergy(1) > this.getEnergy(2))) return this.getTime(1);
		else                                              return this.getTime(2);
	}  
	
	public double getRawEnergyTime() {
		// For cluster time use timing from U,V,W peak with largest raw energy (no attenuation correction)		
		if      ((this.getRawEnergy(0) > this.getRawEnergy(1)) && 
			     (this.getRawEnergy(0) > this.getRawEnergy(2))) return this.getTime(0);
		else if ((this.getRawEnergy(1) > this.getRawEnergy(0)) && 
				 (this.getRawEnergy(1) > this.getRawEnergy(2))) return this.getTime(1);
		else                                                    return this.getTime(2);
	} 
	
	public double getRawADCTime() {
		// For cluster time use timing from U,V,W peak with largest raw ADC (no gain correction)		
		if      ((this.getRawADC(0) > this.getRawADC(1)) && 
			     (this.getRawADC(0) > this.getRawADC(2))) return this.getTime(0);
		else if ((this.getRawADC(1) > this.getRawADC(0)) && 
				 (this.getRawADC(1) > this.getRawADC(2))) return this.getTime(1);
		else                                              return this.getTime(2);
	} 
	
    public double getTime(int view){
        return this.clusterPeaks.get(view).getTime(clusterHitPosition);
    }	
   
    public Point3D getHitPosition(){
        return this.clusterHitPosition;
    }
    
    public double getHitPositionError(){
        return this.clusterHitPositionError;
    }
    
    public static void shareEnergy(ECCluster cluster1, ECCluster cluster2, int view){
        
        if(view==0){
            
            double en1ic = cluster1.getEnergy(1) + cluster1.getEnergy(2);
            double en2ic = cluster2.getEnergy(1) + cluster2.getEnergy(2);
            
            double e1sh  = cluster1.getEnergy(0);
            double e2sh  = cluster2.getEnergy(0);
            
            double ratio1 = en1ic/(en1ic + en2ic);
            double ratio2 = en2ic/(en1ic + en2ic);
            
            cluster1.setEnergy(en1ic + e1sh*ratio1);
            cluster2.setEnergy(en2ic + e2sh*ratio2);
                        
            return;
        }
        
        if(view==1){
            
            double en1ic = cluster1.getEnergy(0) + cluster1.getEnergy(2);
            double en2ic = cluster2.getEnergy(0) + cluster2.getEnergy(2);
            
            double e1sh  = cluster1.getEnergy(1);
            double e2sh  = cluster2.getEnergy(1);
            
            double ratio1 = en1ic/(en1ic + en2ic);
            double ratio2 = en2ic/(en1ic + en2ic);
            
            cluster1.setEnergy(en1ic + e1sh*ratio1);
            cluster2.setEnergy(en2ic + e2sh*ratio2);
                        
            return;
        }
        
        if(view==2){
            
            double en1ic = cluster1.getEnergy(0) + cluster1.getEnergy(1);
            double en2ic = cluster2.getEnergy(0) + cluster2.getEnergy(1);
            
            double e1sh  = cluster1.getEnergy(2);
            double e2sh  = cluster2.getEnergy(2);
            
            double ratio1 = en1ic/(en1ic + en2ic);
            double ratio2 = en2ic/(en1ic + en2ic);
            
            cluster1.setEnergy(en1ic + e1sh*ratio1);
            cluster2.setEnergy(en2ic + e2sh*ratio2);                                 
        }
        
    }
    
    public int sharedView(ECCluster cluster){
        if(cluster.getPeak(0).getOrder()==getPeak(0).getOrder()){
            return 0;
        }
        if(cluster.getPeak(1).getOrder()==getPeak(1).getOrder()){
            return 1;
        }
        if(cluster.getPeak(2).getOrder()==getPeak(2).getOrder()){
            return 2;
        }
        return -1;
    }  
   
    public final void   intersection(){
        Line3D uLine  = this.clusterPeaks.get(0).getLine();
        Line3D vLine  = this.clusterPeaks.get(1).getLine();
        Line3D wLine  = this.clusterPeaks.get(2).getLine();
        Line3D uvLine = uLine.distance(vLine);
        Line3D uvDistTo_w = wLine.distance(uvLine.midpoint());
        this.clusterHitPosition.copy(uvDistTo_w.midpoint());
        this.clusterHitPositionError = uvDistTo_w.length();
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("[****] CLUSTER >>>>> RE = %12.5f E = %12.5f  T=%12.5f    >>> ",this.getRawEnergy(),this.getEnergy(),this.getTime()));
        str.append(this.clusterHitPosition.toString());
        str.append(String.format("  error = %12.5f\n",this.clusterHitPositionError));
        for(int view = 0; view < 3; view++){
            str.append(this.clusterPeaks.get(view));
        }
        return str.toString();
    }
    
    public static class ECClusterIndex {
        int uIndex = -1;
        int vIndex = -1;
        int wIndex = -1;
        
        Line3D distance = new Line3D();
        
        public ECClusterIndex(int ui, int vi, int wi){
            this.uIndex = ui;
            this.vIndex = vi;
            this.wIndex = wi;
        }
        
        public void setLine(Line3D line){
            distance.copy(line);
        }
        
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            str.append(String.format("--> CLUSTER (U,V,W) : %4d %4d %4d ", uIndex,vIndex,wIndex));
            str.append(String.format(" (X,Y,Z) %8.3f ",distance.length()));
            return str.toString();
        }
    }
    
}
