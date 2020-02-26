package org.jlab.rec.fmt.cross;

import java.util.ArrayList;
import java.util.List;

import org.jlab.rec.fmt.Constants;
import org.jlab.rec.fmt.cluster.Cluster;

/**
 * Driver class to make fmt crosses
 * @author ziegler
 *
 */

public class CrossMaker {
	
	public CrossMaker() {
		
	}
	
	public ArrayList<Cross> findCrosses(List<Cluster> clusters) {

		// first separate the segments according to layers
		ArrayList<Cluster> allinnerlayrclus = new ArrayList<Cluster>();
		ArrayList<Cluster> allouterlayrclus = new ArrayList<Cluster>();

		// Sorting by layer first:
		for (Cluster theclus : clusters){
			if(theclus.get_Layer()%2==0) { 
				allouterlayrclus.add(theclus); 
			} 
			if(theclus.get_Layer()%2==1) { 
				allinnerlayrclus.add(theclus);
			}
		}

		ArrayList<Cross> crosses = new ArrayList<Cross>();

		int rid =0;
		for(Cluster inlayerclus : allinnerlayrclus){
			for(Cluster outlayerclus : allouterlayrclus){
				if(outlayerclus.get_Layer()-inlayerclus.get_Layer()!=1)
					continue;
				if(outlayerclus.get_Sector()!=inlayerclus.get_Sector())
					continue;
				if( (inlayerclus.get_MinStrip()+outlayerclus.get_MinStrip() > 1) 
						&& (inlayerclus.get_MaxStrip()+outlayerclus.get_MaxStrip() < Constants.FVT_Nstrips*2) ) { // put correct numbers to make sure the intersection is valid

					// define new cross 
					Cross this_cross = new Cross(inlayerclus.get_Sector(), inlayerclus.get_Region(),rid++);
					this_cross.set_Cluster1(inlayerclus);
					this_cross.set_Cluster2(outlayerclus);
					
					this_cross.set_CrossParams();
					//make arraylist
					crosses.add(this_cross);

				}
			}
		}
		return crosses;
	}
	
	
	
}



