package org.jlab.rec.ft;

import java.util.ArrayList;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.rec.ft.cal.FTCALCluster;
import org.jlab.rec.ft.cal.FTCALConstantsLoader;
import org.jlab.rec.ft.hodo.FTHODOSignal;

public class FTCALHODOMatching{

	public int debugMode = 0;
	
	public void processEvent(EvioDataEvent event) {
		
		EvioDataBank bankcal   = null;
		EvioDataBank bankhodo  = null;
		EvioDataBank banktrack = null;
		
		ArrayList<FTCALCluster> allclusters = new ArrayList<FTCALCluster>();
		ArrayList<FTHODOSignal> allsignals  = new ArrayList<FTHODOSignal>();
		ArrayList<FTTrack> tracks  = new ArrayList<FTTrack>();
		
//		1) get FTCAL cluster information from evio bank
		if(event.hasBank("FTCALRec::clusters")==true) {
			bankcal = (EvioDataBank) event.getBank("FTCALRec::clusters");
			if(bankcal==null) return;
			allclusters = FTCALCluster.getClusters(bankcal);
			if(debugMode>=1) {
				System.out.println("\nFound " + allclusters.size() + " clusters in FTCAL");
				for(int i=0; i<allclusters.size(); i++) {
					System.out.println(i+ " E = "     + allclusters.get(i).get_clusEnergy()
							            + " Theta = " + allclusters.get(i).get_clusTheta()
							            + " Phi = "   + allclusters.get(i).get_clusPhi()
							            + " Time = "  + allclusters.get(i).get_clusTime()
							            + " Size = "  + allclusters.get(i).get_clusSize()
							            );
				}
			}

		}
			
//		2) get FTHODO signals from evio bank	
		if(event.hasBank("FTHODORec::signals")==true) {
			bankhodo = (EvioDataBank) event.getBank("FTHODORec::signals");
			if(bankcal!=null) allsignals = FTHODOSignal.getSignals(bankhodo);
			if(debugMode>=1) {
				System.out.println("Found " + allsignals.size() + " signals in FTHODO");
				for(int i=0; i<allsignals.size(); i++) {
					System.out.println(i+ " E = "     + allsignals.get(i).get_signalEnergy()
										+ " Theta = " + allsignals.get(i).get_signalTheta()
										+ " Phi = "   + allsignals.get(i).get_signalPhi()
							);
				}	
			}	
		}
		
//		3) do matching starting from FTCAL clusters
		for(int i=0; i<allclusters.size(); i++) {
			if(allclusters.get(i).isgoodCluster()) {
				FTTrack track = new FTTrack(i);
				// start assuming the cluster to be associated to a photon
				track.set_trackCharge(0);
				track.set_trackSize(allclusters.get(i).get_clusSize());
				track.set_trackEnergy(allclusters.get(i).get_clusEnergy());
				track.set_trackX(allclusters.get(i).get_clusX());
				track.set_trackY(allclusters.get(i).get_clusY());
				track.set_trackZ(FTCALConstantsLoader.CRYS_ZPOS+FTCALConstantsLoader.depth_z);
				track.set_trackTheta(allclusters.get(i).get_clusTheta());
				track.set_trackPhi(allclusters.get(i).get_clusPhi());
				track.set_trackTime(allclusters.get(i).get_clusTime()
						-Math.sqrt(track.get_trackX()*track.get_trackX()+
								   track.get_trackY()*track.get_trackY()+
								   track.get_trackZ()*track.get_trackZ())/297.);
				track.set_trackCluster(i);
				track.set_trackSignal(-1);
				track.set_trackCross(-1);
				if(debugMode>=1) {
					System.out.println("Found good cluster with " +
									   " E = "     + track.get_trackEnergy() +
									   " Theta = " + track.get_trackTheta() +
									   " Phi = "   + track.get_trackPhi() +
									   " Time = "  + track.get_trackTime());
				}
				// look for matching signal in the hodoscope
				if(allsignals.size()>0) {
					if(debugMode>=1) System.out.println("Searching for matching signal in the hodoscope:");
					double cluster_2_signal_min=20;
					for(int j=0; j<allsignals.size(); j++) {
						double cluster_2_signal=Math.sqrt(Math.pow(track.get_trackX()-allsignals.get(j).get_signalX(),2.)+
								                          Math.pow(track.get_trackY()-allsignals.get(j).get_signalY(),2.));
						if(cluster_2_signal<cluster_2_signal_min) {
							cluster_2_signal_min=cluster_2_signal;
							track.set_trackSignal(j);
						}
						if(debugMode>=1) System.out.println("found signal " + j);
						track.set_trackCharge(track.get_trackCharge()+1);
					}						
				}
				// if matching signal is found then cluster is associated to an electron
				if(track.get_trackCharge()>=2) {
					track.set_trackCharge(1);
					// correct angles
					double clusEnergy=track.get_trackEnergy();
					double thetaCorr = Math.exp(FTCALConstantsLoader.theta_corr[0]+FTCALConstantsLoader.theta_corr[1]*clusEnergy)+
							     	   Math.exp(FTCALConstantsLoader.theta_corr[2]+FTCALConstantsLoader.theta_corr[3]*clusEnergy);
					double phiCorr   = Math.exp(FTCALConstantsLoader.phi_corr[0]+FTCALConstantsLoader.phi_corr[1]*clusEnergy)+
							     	   Math.exp(FTCALConstantsLoader.phi_corr[2]+FTCALConstantsLoader.phi_corr[3]*clusEnergy)+
							     	   Math.exp(FTCALConstantsLoader.phi_corr[4]+FTCALConstantsLoader.phi_corr[5]*clusEnergy);
					track.set_trackTheta(track.get_trackTheta()+thetaCorr);
					track.set_trackPhi(track.get_trackPhi()-phiCorr);
				}
				else {
					track.set_trackCharge(0);
				}
				tracks.add(track);
			}
		}
		
		
//		4) write track information to evio bank
		if(tracks.size()!=0){
			banktrack = (EvioDataBank) event.getDictionary().createBank("FTRec::tracks",tracks.size());
			if(debugMode>=1) System.out.println("Creating output track bank with " + tracks.size() + " tracks");
			for(int i =0; i< tracks.size(); i++) {
				banktrack.setInt("ID", i,tracks.get(i).get_trackID());
				banktrack.setInt("Charge", i,tracks.get(i).get_trackCharge());
				banktrack.setDouble("Energy", i,tracks.get(i).get_trackEnergy());
				banktrack.setDouble("Cx", i,tracks.get(i).get_trackCX());
				banktrack.setDouble("Cy", i,tracks.get(i).get_trackCY());
				banktrack.setDouble("Cz", i,tracks.get(i).get_trackCZ());
				banktrack.setDouble("Time", i,tracks.get(i).get_trackTime());
				banktrack.setInt("Cluster",i,tracks.get(i).get_trackCluster());
				banktrack.setInt("Signal",i,tracks.get(i).get_trackSignal());
				banktrack.setInt("Cross", i,tracks.get(i).get_trackCross());
				if(debugMode>=1) {
					System.out.println("Final track info " +
										" Charge = "+ tracks.get(i).get_trackCharge() +
										" E = "     + tracks.get(i).get_trackEnergy() +
										" Theta = " + tracks.get(i).get_trackTheta() +
										" Phi = "   + tracks.get(i).get_trackPhi() +
										" Time = "  + tracks.get(i).get_trackTime());
				}
			}

			// If there are no clusters, punt here but save the reconstructed hits 
			if(banktrack!=null) {
				event.appendBanks(banktrack);
			}
		}

	}

}
