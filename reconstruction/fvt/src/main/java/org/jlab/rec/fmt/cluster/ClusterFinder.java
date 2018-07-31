package org.jlab.rec.fmt.cluster;


import java.util.ArrayList;
import java.util.List;

import org.jlab.rec.fmt.Constants;
import org.jlab.rec.fmt.hit.FittedHit;
import org.jlab.rec.fmt.hit.Hit;


/**
 * 
 * @author defurne
 *
 */
public class ClusterFinder  { 
	
	public ClusterFinder() {
		
	}
	
	// cluster finding algorithm
	// the loop is done over sectors 

	int nlayr = Constants.FVT_Nlayers;
	int nstrip = Constants.FVT_Nstrips;
	boolean[][] checked;
	Hit[][] HitArray;
	public ArrayList<Cluster> findClusters(List<Hit> hits2)
	{
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		
		// a boolean array to avoid double counting at the numbering discontinuities
		checked = new boolean[nstrip][nlayr] ;
		for(int l=0; l<nlayr; l++){
			for(int s=0; s<nstrip; s++)
			{
				checked[s][l]=false;
			}
		}
		
		// a Hit Array is used to identify clusters
		
		HitArray = new Hit[nstrip][nlayr] ;
		
		
		// initializing non-zero Hit Array entries
		// with valid hits
		for(Hit hit : hits2) {
			
			if(hit.get_Strip()==-1) 
				continue;
			
			int w = hit.get_Strip();
			int l = hit.get_Layer();
			
			if(w>0 && w<nstrip)	{						
				HitArray[w-1][l-1] = hit;
			}
			
		}
		int cid = 1;  // cluster id, will increment with each new good cluster
		
		// for each layer and sector, a loop over the strips
		// is done to define clusters in that module's layer
		// clusters are delimited by strips with no hits 
		for(int l=0; l<nlayr; l++)
		{		
			int si  = 0;  // strip index in the loop
			
			// looping over all strips
			while(si<nstrip)
			{
				// if there's a hit, it's a cluster candidate
				if(HitArray[si][l] != null&&!checked[si][l])
				{
					// vector of hits in the cluster candidate
					ArrayList<FittedHit> hits = new ArrayList<FittedHit>();
					
					// adding all hits in this and all the subsequent
					// strip until there's a strip with no hit
					// Strip 1 and 513 needs a particular loop
					if (si==0){
						int sj=832;
						while(HitArray[sj][l] != null  && sj<nstrip)
						{
							checked[sj][l]=true;
							hits.add(new FittedHit(HitArray[sj][l].get_Sector(),HitArray[sj][l].get_Layer(),HitArray[sj][l].get_Strip(),HitArray[sj][l].get_Edep()));
							sj++;
						}
					}
					
					if (si==512){
						int sj=320;
						while(HitArray[sj][l] != null  && sj<512)
						{
							checked[sj][l]=true;
							hits.add(new FittedHit(HitArray[sj][l].get_Sector(),HitArray[sj][l].get_Layer(),HitArray[sj][l].get_Strip(),HitArray[sj][l].get_Edep()));
							sj++;
						}
					}
					
					//For all strips
					while(HitArray[si][l] != null  && si<nstrip)
					{
						checked[si][l]=true;
						hits.add(new FittedHit(HitArray[si][l].get_Sector(),HitArray[si][l].get_Layer(),HitArray[si][l].get_Strip(),HitArray[si][l].get_Edep()));
						if (si!=511) si++; //Since strip 512 is on a edge
						else break;
					}
					
					// define new cluster 
					Cluster this_cluster = new Cluster(1, l+1, cid++); 
					
					
					// add hits to the cluster
					this_cluster.addAll(hits);
					this_cluster.calc_CentroidParams();
					
					//make arraylist
					clusters.add(this_cluster);
					
				}
				// if no hits, check for next wire coordinate
					si++;
			}
		}
       
      
		return clusters;
		
	}
	


}
