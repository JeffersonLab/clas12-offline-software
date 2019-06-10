package org.jlab.rec.cvt;

import java.util.HashMap;
import java.util.ArrayList;
import java.lang.*;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.hit.FittedHit;



public class CentralTracker {
	 	HashMap <Integer, ArrayList<FittedHit>> CVThits   = null;
	 	HashMap <Integer, ArrayList<Cluster>> CVTclusters   = null;
	 	ArrayList<Integer> SVTSectorWithHits;
	 	ArrayList<Integer> SVTLayerWithHits;
	 	ArrayList<Integer> MVTSectorWithHits;
	 	ArrayList<Integer> MVTLayerWithHits;
	 	int NbSVTHits;
	 	int NbMVTHits;
	 	int NbMVTClusters;
	 	int NbSVTClusters;
	 		 	
	 	public CentralTracker() {
	 		CVThits = new HashMap <Integer,ArrayList<FittedHit>>();
	 		CVTclusters = new HashMap <Integer,ArrayList<Cluster>>();
	 		SVTSectorWithHits=new ArrayList<Integer>();
	 		MVTSectorWithHits=new ArrayList<Integer>();
	 		SVTLayerWithHits=new ArrayList<Integer>();
	 		MVTLayerWithHits=new ArrayList<Integer>();
	 		NbSVTHits=0;
	 		NbMVTHits=0;
	 		NbSVTClusters=0;
	 		NbMVTClusters=0;
	 	}
	 	
	 	public void addCluster(Cluster clus) {
	 		int layer=clus.get_Layer();
	 		int sector=clus.get_Sector();
	 		if (clus.get(0).get_Detector()==1) layer=layer+6;
	 		
	 		//If no cluster in sector/layer, then create it
	 		if (!CVTclusters.containsKey(layer*100+sector)) {
	 			ArrayList<Cluster> tempCluster=new ArrayList<Cluster>();
	 			CVTclusters.put(layer*100+sector,tempCluster);
	 			if (clus.get(0).get_Detector()==0) {
	 				SVTLayerWithHits.add(layer);
	 				SVTSectorWithHits.add(sector);
	 			}
	 			if (clus.get(0).get_Detector()==1) {
	 				MVTLayerWithHits.add(layer);
	 				MVTSectorWithHits.add(sector);
	 			}
	 			
	 		}
	 		CVTclusters.get(layer*100+sector).add(clus);
	 		if (clus.get(0).get_Detector()==0) NbSVTClusters++;
	 		if (clus.get(0).get_Detector()==1) NbMVTClusters++;
	 		for (int i=0;i<clus.size();i++) this.addHit(layer, sector, clus.get(i));
	 	}
	 	
	 	public void addHit(int layer, int sector, FittedHit hit) {
	 		//If no hit in sector/layer, then create it
	 		if (!CVThits.containsKey(layer*100+sector)) {
	 			ArrayList<FittedHit> tempHit=new ArrayList<FittedHit>();
	 			CVThits.put(layer*100+sector,tempHit);
	 		}
	 		if (hit.get_Detector()==0) NbSVTHits++;
	 		if (hit.get_Detector()==1) NbMVTHits++;
	 		CVThits.get(layer*100+sector).add(hit);
	 	}
	 	
	 	public ArrayList<Cluster> getClusters(int layer, int sector){
	 		return CVTclusters.get(layer*100+sector);
	 	}
	 	
	 	public ArrayList<FittedHit> getHits(int layer, int sector){
	 		return CVThits.get(layer*100+sector);
	 	}
	 	
	 	public int getNbHits(int layer, int sector) {
	 		return CVThits.get(layer*100+sector).size();
	 	}
	 	
	 	public int getTotalSVThits() {
	 		return NbSVTHits;
	 	}
	 	
	 	public int getTotalMVThits() {
	 		return NbMVTHits;
	 	}
	 	
	 	public int getTotalSVTclusters() {
	 		return NbSVTClusters;
	 	}
	 	
	 	public int getTotalMVTclusters() {
	 		return NbMVTClusters;
	 	}
	 	
	 	public int getNbSVTModuleWithHit() {
	 		return SVTSectorWithHits.size();
	 	}
	 	
	 	public int getNbMVTTileWithHit() {
	 		return MVTSectorWithHits.size();
	 	}
	 	
	 	public ArrayList<Cluster> getSVTClustersByID(int IDModule){
	 		return CVTclusters.get(SVTLayerWithHits.get(IDModule)*100+SVTSectorWithHits.get(IDModule));
	 	}
	 	
	 	public ArrayList<FittedHit> getSVTHitsByID(int IDModule){
	 		return CVThits.get(SVTLayerWithHits.get(IDModule)*100+SVTSectorWithHits.get(IDModule));
	 	}
	 	
	 	public ArrayList<Cluster> getMVTClustersByID(int IDModule){
	 		return CVTclusters.get(MVTLayerWithHits.get(IDModule)*100+MVTSectorWithHits.get(IDModule));
	 	}
	 	
	 	public ArrayList<FittedHit> getMVTHitsByID(int IDModule){
	 		return CVThits.get(MVTLayerWithHits.get(IDModule)*100+MVTSectorWithHits.get(IDModule));
	 	}
}
