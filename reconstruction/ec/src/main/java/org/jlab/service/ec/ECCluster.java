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
    
    public byte    sharedCluster = -1;
    private byte      sharedView =  0; //1=U, 2=V, 3=W, 4=UV, 5=UW, 6=VW
       
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
    
    public void setSharedView(int val) {
    	this.sharedView = (byte) val;
    }
    
    public void setSharedCluster(int val) {
    	this.sharedCluster = (byte) val;
    }   
    
    public byte getStatus() {
    	return this.sharedView;
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
    	return ECCommon.useUnsharedTime? getUnsharedRawADCTime():getRawADCTime();
    }
    
	public double getMaxEnergyTime() {
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
	
	public double getUnsharedRawADCTime() {
		// Use only U,V,W peaks with unshared views for cluster timing
		switch (sharedView) {
		case 1: return getRawADC(1)>getRawADC(2) ? getTime(1):getTime(2);
		case 2: return getRawADC(0)>getRawADC(2) ? getTime(0):getTime(2);
		case 3: return getRawADC(0)>getRawADC(1) ? getTime(0):getTime(1);
		case 4: return getTime(2);
		case 5: return getTime(1);
		case 6: return getTime(0);	
		}
		return getRawADCTime();
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
	
    	switch (view) {
    	case 1: splitClusterEnergy(cluster1,cluster2,0,1,2); break; //split view 0 using views 1,2
    	case 2: splitClusterEnergy(cluster1,cluster2,1,0,2); break; //split view 1 using views 0,2
    	case 3: splitClusterEnergy(cluster1,cluster2,2,0,1); break; //split view 2 using views 0,1
    	case 4: splitClusterEnergy(cluster1,cluster2,0,3,2); break; //split views 0,1 using view 2
    	case 5: splitClusterEnergy(cluster1,cluster2,0,1,3); break; //split views 0,2 using view 1
    	case 6: splitClusterEnergy(cluster1,cluster2,1,0,3);        //split views 1,2 using view 0
    	}
    
    }
    
    public static void splitClusterEnergy(ECCluster cluster1, ECCluster cluster2, int i, int j, int k) {
	
    	//split peak energy in view i using views j and k

    	//distance between shared clusters
    	double d = cluster2.getHitPosition().distance(cluster2.getPeak(i).getMaxECStrip().getLine().end())
    			 - cluster1.getHitPosition().distance(cluster1.getPeak(i).getMaxECStrip().getLine().end());
    
    	double en1ic = (j==3?0:cluster1.getEnergy(j)) + (k==3?0:cluster1.getEnergy(k)); //sum of peak energies in unmerged peaks of cluster 1
    	double en2ic = (j==3?0:cluster2.getEnergy(j)) + (k==3?0:cluster2.getEnergy(k)); //sum of peak energies in unmerged peaks of cluster 2

    	double e1sh  = cluster1.getEnergy(i)+ (j==3?cluster1.getEnergy(1):0)+ (k==3?cluster1.getEnergy(2):0); //merged peak energy in cluster 1
    	double e2sh  = cluster2.getEnergy(i)+ (j==3?cluster2.getEnergy(1):0)+ (k==3?cluster2.getEnergy(2):0); //merged peak energy in cluster 2
	
    	//light attenuation between clusters 1 and 2
    	double corr1 = (j==3||k==3)?1:cluster1.getPeak(i).getMaxECStrip().getEcorr(-d);
    	double corr2 = (j==3||k==3)?1:cluster2.getPeak(i).getMaxECStrip().getEcorr( d);

    	double r12 = en1ic/en2ic;
    	double r21 = en2ic/en1ic;
	
    	//same as Gagik's correction if d<<attenuation length (corr1=corr2~1)
    	cluster1.setEnergy(en1ic + e1sh/(1+r21*corr1)); 
    	cluster2.setEnergy(en2ic + e2sh/(1+r12*corr2));    	
	
    }
    
    public int sharedView(ECCluster cluster){
    	
    	boolean[] test =  new boolean[6];
    	int lay = cluster.clusterPeaks.get(0).getDescriptor().getLayer();
    	for (int i=0; i<3; i++) test[i] = cluster.getPeak(i).getOrder()==getPeak(i).getOrder();
    	test[3] = test[0]&&test[1]; test[4] = test[0]&&test[2]; test[5] = test[1]&&test[2]; 
    	for (int i=0; i<3;  i++) if(test[i]) cluster.getPeak(i).setStatus(1);
    	for (int i=5; i>=0; i--) if(test[i]) return i;
        
        return -1;
    }
        
    public final void intersection(){
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
        str.append(String.format("[****] CLUSTER >>>>> RE = %8.5f E = %8.5f  T=%8.5f Tc=%8.5f ShC=%5d ShV=%5d    >>> ",getRawEnergy(),getEnergy(),getTime(),getUnsharedRawADCTime(),sharedCluster,sharedView));
        str.append(this.clusterHitPosition.toString());
        str.append(String.format("  error = %6.5f\n",this.clusterHitPositionError));
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
