package org.jlab.rec.rtpc.hit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import org.jlab.clas.physics.Vector3;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.rec.rtpc.hit.RecoHitVector;

public class TrackHitReco2 {
	
	public void Reco(List<Hit> rawHits, HitParameters params, boolean draw) {
		HashMap<Integer, HashMap<Integer, Vector<Integer>>> TIDMap = params.get_strkTIDMap();
		HashMap<Integer, double[]> ADCMap = params.get_R_adc();
		HashMap<Integer, GraphErrors> graphmap = new HashMap<Integer, GraphErrors>();
		HashMap<Integer, GraphErrors> graphmap2 = new HashMap<Integer, GraphErrors>();
		HashMap<Integer, Vector<HitVector>> alltracks = params.get_alltracks();
		HashMap<Integer, Double> largetmap = params.get_largetmap();
		HashMap<Integer, Double> tdiffmap = new HashMap<Integer, Double>();
		HashMap<Integer, Vector<RecoHitVector>> recohitvector = new HashMap<Integer, Vector<RecoHitVector>>();
		Vector<Double> TimeVec = new Vector<Double>();
		Vector<Double> XVec = new Vector<Double>();
		Vector<Double> YVec = new Vector<Double>();
		Vector<Double> ZVec = new Vector<Double>();
		Vector<Integer> Pads = new Vector<Integer>();
		int p = 0;
		EmbeddedCanvas c = new EmbeddedCanvas();
		EmbeddedCanvas c2 = new EmbeddedCanvas();
		EmbeddedCanvas c3 = new EmbeddedCanvas();
		EmbeddedCanvas c4 = new EmbeddedCanvas();
		JFrame j = new JFrame();
		j.setSize(800,600);
		JFrame j2 = new JFrame();
		j2.setSize(800,600);
		JFrame j3 = new JFrame();
		j3.setSize(800,600);
		JFrame j4 = new JFrame();
		j4.setSize(800,600);
		
		double a_t1 = -2.48491E-4;
		double a_t2 = 2.21413E-4;
		double a_t3 = -3.11195E-3;
		double a_t4 = -2.75206E-1;
		double a_t5 = 1.74281E3;
			
		double b_t1 = 2.48873E-5;
		double b_t2 = -1.19976E-4;
		double b_t3 = -3.75962E-3;
		double b_t4 = 5.33100E-2;
		double b_t5 = -1.25647E2;
		
		double a_phi1 = -3.32718E-8;
		double a_phi2 = 1.92110E-7;
		double a_phi3 = 2.16919E-6;
		double a_phi4 = -8.10207E-5;
		double a_phi5 = 1.68481E-1;
		
		double b_phi1 = -3.23019E-9;
		double b_phi2 = -6.92075E-8;
		double b_phi3 = 1.24731E-5;
		double b_phi4 = 2.57684E-5;
		double b_phi5 = 2.10680E-2;
		
		H1F largetdist = new H1F("largetdist",100,0,10000);
		
		double timewindow = 2000;
		double smalltcut = 300;
		double largetcut = 8000;
		int tracksizecut = 4;
		double larget = 0;
		double tdiff = 0;
		double Time = 0;
		int cellID = 0;
		int outsidedriftcounter = 0;
		int allhitscounter = 0;
		/*Vector<Integer> PadList = new Vector<Integer>();
		double maxvalue = 0;
		double thresh = 0;
		int TrigWindSize = params.get_TrigWindSize();
		double sumnumer = 0;
		double sumdenom = 0;
		int count = 0;*/
		
		//HashMap<Integer, Double> weightave = new HashMap<Integer, Double>();
		//System.out.println(TIDMap.size());
		//for(Hit hit : rawHits) {
		//for(int TID : TIDMap.keySet()){
		//System.out.println(alltracks.size() + " " + alltracks.get(1).size());
		for(int TID : alltracks.keySet()) {
			Pads.clear();
			/*PadList.clear();
		
			double larget = 0;
			for(int time : TIDMap.get(TID).keySet())
			{
				for(int pad = 0; pad < TIDMap.get(TID).get(time).size(); pad++)
				{
					if(!PadList.contains(TIDMap.get(TID).get(time).get(pad)))
					{
						PadList.add(TIDMap.get(TID).get(time).get(pad));
					}
				}
			}
			
			for(int pad = 0; pad < PadList.size(); pad++)
			{
				for(int time : TIDMap.get(TID).keySet())
				{
					if(TIDMap.get(TID).get(time).contains(PadList.get(pad)))
					{
						if(ADCMap.get(PadList.get(pad))[time]>maxvalue)
						{
							maxvalue = ADCMap.get(PadList.get(pad))[time];
						}
					}
				}
				thresh = maxvalue/2;
				for(int time = 0; time < TrigWindSize; time++)
				{
					if(ADCMap.get(PadList.get(pad))[time] > thresh)
					{
						sumnumer += ADCMap.get(PadList.get(pad))[time]*time;
						sumdenom += ADCMap.get(PadList.get(pad))[time];	
						//System.out.println(PadList.get(pad) + " " + time);
					}				
				}
				if(sumnumer/sumdenom >= larget)
				{
					larget = sumnumer/sumdenom;
				}
				//System.out.println("weightave " + PadList.get(pad) + " " + sumnumer/sumdenom);
				weightave.put(PadList.get(pad), sumnumer/sumdenom);
				sumnumer = 0; 
				sumdenom = 0;
				maxvalue = 0;
				//padvec = PadCoords(PadList.get(pad));
				//gZvsT.addPoint(weightave.get(PadList.get(pad)), padvec.z(), 0, 0);
				//gPhivsT.addPoint(weightave.get(PadList.get(pad)), Math.atan2(padvec.y(),padvec.x()),0,0);
			}*/
			//System.out.println("large t " + larget);
			larget = largetmap.get(TID);
			double smallt = 1000000;
			for(int i = 0; i < alltracks.get(TID).size(); i ++)
			{
				if(alltracks.get(TID).get(i).time() < smallt) {smallt = alltracks.get(TID).get(i).time();}
			}
			//double smallt = alltracks.get(TID).get(alltracks.get(TID).size()-1).time();
			

			/*try {
				File f = new File("/Users/davidpayette/Documents/FileOutput/smallt.txt");
				//f.delete();
				FileWriter out = new FileWriter("/Users/davidpayette/Documents/FileOutput/smallt.txt",true);
				out.write(smallt + "\n");
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			largetdist.fill(larget);
			//System.out.println("large t is " + larget);
			//if(TID != 2) {continue;}
			//System.out.println(smallt + " small t");
			if(smallt < smalltcut || larget > largetcut || alltracks.get(TID).size() <= tracksizecut) {continue;}
			//add smallt check as well for smallt too small
			
			graphmap.put(TID, new GraphErrors());
			graphmap2.put(TID, new GraphErrors());
			
			tdiff = 6000 - larget;
			//if(larget + tdiff > 7500) {continue;}
			tdiffmap.put(TID, tdiff);
			recohitvector.put(TID, new Vector<RecoHitVector>());
			
			//tdiff = 0;
			//System.out.println("hqwd" + TID);
			//for(int cellID : weightave.keySet()){
			//System.out.println("large t " + larget);
			for(int k = 0; k < alltracks.get(TID).size(); k++) {
				//for(int padindex = 0; padindex < TIDMap.get(TID).get(t).size(); padindex++){
			
					allhitscounter++;
					//int cellID = TIDMap.get(TID).get(t).get(padindex);
					cellID = (int)alltracks.get(TID).get(k).pad();
					if(!Pads.contains(cellID)) {
					Pads.add(cellID);
					}
					//double Time = weightave.get(cellID);
					Time = alltracks.get(TID).get(k).time();
					//System.out.println("reco " + Time);
					//System.out.println("tdiff " + tdiff);
					Time+=tdiff;
					
					//System.out.println(Time + " " + cellID);
			
			
					//int NEve = 10;
			    
					double PAD_W = 2.79; // in mm
					double PAD_S = 80.0; //in mm
					double PAD_L = 4.0; // in mm
					double RTPC_L = 384.0; // in mm
			    
					//double Num_of_Rows = (2.0*(Math.PI)*PAD_S)/PAD_W;
					double Num_of_Cols = RTPC_L/PAD_L;
					//double TotChan = Num_of_Rows*Num_of_Cols;
			    
				    double PI=Math.PI;
					    
				    double z0 = -(RTPC_L/2.0); // front of RTPC in mm at the center of the pad
		
				    double phi_per_pad = PAD_W/PAD_S; // in rad


		    
				 // MagBoltz parameters
				    //double a_t=1741.179712, b_t=-1.25E+02; // for f(x)=time(radius)
				    //double a_phi=0.161689123, b_phi=0.023505021; // for f(x)=dphi(radius)
				    
				    double t_2GEM2 = 296.082;
				    double t_2GEM3 = 296.131;
				    double t_2PAD = 399.09;
				    double t_gap = t_2GEM2 + t_2GEM3 + t_2PAD;
				    
				    double phi_2GEM2 = 0.0492538;
				    double phi_2GEM3 = 0.0470817;
				    double phi_2PAD = 0.0612122;
				    double phi_gap = phi_2GEM2 + phi_2GEM3 + phi_2PAD;
				    
			 // find postition from Cell ID
			       //for (double s = 0.0; s < cellID.size(); s++) {
	
			           double chan=0;
			           double t_s2pad = 0;
			           double dphi=0;
			           double dz=0;
			           double x_rec = 0;
			           double y_rec = 0; 
			           
			           
			           double z_pad=0;
			           double z_rec=0;
			           double z_hit=0;  // position of the hit on a single pad in z
			           double delta_z=0;
			           
			           double r_pos=0;
			           double r_rec=0;
			           double r_temp=0;
			           double delta_r=0;
			           
			           double phi_pad=0;
			           double phi_rec=0;

		           
		           
		           // generated position of ionization in phi

		           //phi_pos = Math.atan2(Y, X);
		           
		           // generated position of ionization in s
		           //r_pos=Math.sqrt(((X)*(X))+((Y)*(Y)));

		           
		           // ------------------ find z and phi of pad from CellID ------------------
		                chan = (double)cellID;
		        
		                double col = chan%Num_of_Cols;
		                double row=(chan-col)/Num_of_Cols;
		                double z_shift = row%4;
  	                    //double z_shift = 0.;
		           
		                phi_pad=(row*phi_per_pad)+(phi_per_pad/2.0);
		                if(phi_pad>= 2.0*PI) {
		                	phi_pad -= 2.0*PI;
		                }
		                if(phi_pad<0) 
		                	{
		                	phi_pad += 2.0*PI;
		                	}
		           
		                z_pad=z0+(col*PAD_L)+(PAD_L/2.0)+z_shift;
		           // -----------------------------------------------------------------------
		                dz=0;
		                z_rec=z_pad-dz;
		           
		           // find reconstructed position of ionization from Time info
		                
		           t_s2pad = Time-t_gap;
		           //if(t_s2pad > 6000) {System.out.println("oooops");}
		           //System.out.println("time stuff " + t_s2pad + " " + Time);

		           double a_t = get_rec_coef(a_t1,a_t2,a_t3,a_t4,a_t5,z_rec);
		           double b_t = get_rec_coef(b_t1,b_t2,b_t3,b_t4,b_t5,z_rec);
		           
		           r_rec=((-(Math.sqrt(a_t*a_t+(4.*b_t*t_s2pad)))+a_t+(14.*b_t))/(2.*b_t))*10.0; //in mm
		           
		           double a_phi = get_rec_coef(a_phi1,a_phi2,a_phi3,a_phi4,a_phi5,z_rec);
		           double b_phi = get_rec_coef(b_phi1,b_phi2,b_phi3,b_phi4,b_phi5,z_rec);
		           
		           dphi=a_phi*(7.-r_rec/10.)+b_phi*(7.-r_rec/10.)*(7.-r_rec/10.); // in rad
		           //System.out.println("r and phi " + r_rec + " " + dphi);

		           
		           //System.out.println("phi stuff " + phi_pad + " " + dphi + " " + phi_gap);
		           phi_rec=phi_pad-dphi-phi_gap;
		           if( phi_rec<0.0 )  
		           {
		        	   phi_rec+=2.0*PI;
		           }
		           if( phi_rec>2.0*PI ) 
		           {
		        	   phi_rec-=2.0*PI;
		           }
		           if(r_rec > 70 || r_rec < 30)
		           {
		        	   	outsidedriftcounter++;
		           }
		           // x,y,z pos of reconstructed track
		           x_rec=r_rec*(Math.cos(phi_rec));
		           y_rec=r_rec*(Math.sin(phi_rec));
		           
		           
		           recohitvector.get(TID).add(new RecoHitVector(cellID,x_rec,y_rec,z_rec,tdiff,Time));
		           //if(r_rec > 30 && r_rec < 70)
		           //{
		           graphmap.get(TID).addPoint(r_rec, z_rec, 0, 0);
		           graphmap2.get(TID).addPoint(x_rec, y_rec, 0, 0);
		           
		           
		           
		           //System.out.println("TID time phi x y " + TID + " " + Time + " " + phi_rec + " " + x_rec + " " + y_rec);
		           //}
		           // x,y,z pos of pad hit
		           //x_pad=(PAD_S)*(Math.cos(phi_pad));
		           //y_pad=(PAD_S)*(Math.sin(phi_pad));
		           
		           // actual position on pad of hits
		           //phi_hit=phi_rad-(row*phi_per_pad);
		           //z_hit=Z-z0-(col*PAD_L)-z_shift;
		           
		           // find differences (delta = generated-reconstructed)
		           //delta_x=X-x_rec;
		           //delta_y=Y-y_rec;
		           //delta_z=Z-z_rec;
		           //delta_r=r_pos-r_rec;
		           //delta_phi = phi_pos-phi_rec;


		       
		       
		    			/*hit.set_cellID(cellID);
		    			hit.set_Time(t_s2pad);
		    			hit.set_Edep(Edep);
		    			hit.set_PosX(x_rec);
		    			hit.set_PosY(y_rec);
		    			hit.set_PosZ(z_rec);
		           TimeVec.add(t_s2pad);
		           XVec.add(x_rec);
		           YVec.add(y_rec);
		           ZVec.add(z_rec);*/
		           
		           
				
			}
			//System.out.println("num of pads " + Pads.size());
		}
		GraphErrors grz = new GraphErrors();
		GraphErrors g1rz = new GraphErrors();
		GraphErrors gxy = new GraphErrors();
		GraphErrors g1xy = new GraphErrors();
		GraphErrors g2xy = new GraphErrors();
		GraphErrors gerrorxvsphi = new GraphErrors();
		GraphErrors gerroryvsphi = new GraphErrors();
		int counter = 1;

		for(Hit hit : rawHits)
		{
			if(true) {
			cellID = hit.get_cellID();
			
			Time = hit.get_Time();
			//double T_noshift = Time; 
			if(Time <= 0) {continue;}
			Time -= hit.get_TShift();
			//System.out.println("Time " + hit.get_Time() + " " + hit.get_TShift());
			
			double X = hit.get_PosXTrue();
			double Y = hit.get_PosYTrue();
			double Z = hit.get_PosZTrue();
			//double TimeNoShift = Time - hit.get_TShift();
			//if(Pads.contains(cellID))
			//{
			//ystem.out.println(Time + " " + cellID);
	
	
			//int NEve = 10;
	    
			double PAD_W = 2.79; // in mm
			double PAD_S = 80.0; //in mm
			double PAD_L = 4.0; // in mm
			double RTPC_L = 384.0; // in mm
	    
			//double Num_of_Rows = (2.0*(Math.PI)*PAD_S)/PAD_W;
			double Num_of_Cols = RTPC_L/PAD_L;
			//double TotChan = Num_of_Rows*Num_of_Cols;
	    
		    double PI=Math.PI;
			    
		    double z0 = -(RTPC_L/2.0); // front of RTPC in mm at the center of the pad

		    double phi_per_pad = PAD_W/PAD_S; // in rad


    
		 // MagBoltz parameters
		    //double a_t=1741.179712, b_t=-125; // for f(x)=time(radius)
		    //double a_phi=0.161689123, b_phi=0.023505021; // for f(x)=dphi(radius)
		    
		    double t_2GEM2 = 296.082;
		    double t_2GEM3 = 296.131;
		    double t_2PAD = 399.09;
		    double t_gap = t_2GEM2 + t_2GEM3 + t_2PAD;
		    
		    double phi_2GEM2 = 0.0492538;
		    double phi_2GEM3 = 0.0470817;
		    double phi_2PAD = 0.0612122;
		    double phi_gap = phi_2GEM2 + phi_2GEM3 + phi_2PAD;
		    
	 // find position from Cell ID
	       //for (double s = 0.0; s < cellID.size(); s++) {

	           double chan=0;
	           double t_s2pad = 0;
	           double dphi=0;
	           double dz=0;
	           double x_rec = 0;
	           double y_rec = 0; 
	           
	           
	           double z_pad=0;
	           double z_rec=0;
	           double z_hit=0;  // position of the hit on a single pad in z
	           double delta_z=0;
	           
	           double r_pos=0;
	           double r_rec=0;
	           double r_temp=0;
	           double delta_r=0;
	           
	           double phi_pad=0;
	           double phi_rec=0;
	           double phi_pos = 0;
	           
	           double t_calc = 0;
	           
           
           
           // generated position of ionization in phi

           phi_pos = Math.atan2(Y, X);
           
           // generated position of ionization in s
           r_pos=Math.sqrt(((X)*(X))+((Y)*(Y)));
           //System.out.println(r_pos + " " + X + " " + Y);

           
           // ------------------ find z and phi of pad from CellID ------------------
                chan = (double)cellID;
        
                double col = chan%Num_of_Cols;
                double row=(chan-col)/Num_of_Cols;
                double z_shift = row%4;
                  //double z_shift = 0.;
                //System.out.println(row + " " + chan + " " + col + " " + phi_per_pad);
                phi_pad=(row*phi_per_pad)+(phi_per_pad/2.0);
                while(phi_pad >= 2*PI || phi_pad < 0)
                {
	                if(phi_pad>= 2.0*PI) {
	                	phi_pad -= 2.0*PI;
	                }
	                if(phi_pad<0) 
	                	{
	                	phi_pad += 2.0*PI;
	                	}
                }
                z_pad=z0+(col*PAD_L)+(PAD_L/2.0)+z_shift;
           // -----------------------------------------------------------------------
                dz=0;
                z_rec=z_pad-dz;
                
           // find reconstructed position of ionization from Time info

           double a_t = get_rec_coef(a_t1,a_t2,a_t3,a_t4,a_t5,z_rec);
		   double b_t = get_rec_coef(b_t1,b_t2,b_t3,b_t4,b_t5,z_rec);
		           
		   
		           
           double a_phi = get_rec_coef(a_phi1,a_phi2,a_phi3,a_phi4,a_phi5,z_rec);
           double b_phi = get_rec_coef(b_phi1,b_phi2,b_phi3,b_phi4,b_phi5,z_rec);
             
           //System.out.println(a_t + " " + b_t + " " + a_phi + " " + b_phi + " " + z_rec);
           
           t_s2pad = Time-t_gap;

           r_rec  = ((-(Math.sqrt(a_t*a_t+(4*b_t*t_s2pad)))+a_t+(14*b_t))/(2*b_t))*10.0; //in mm
           t_calc = ((((2*b_t*r_pos*0.1)-(14*b_t)-a_t)*((2*b_t*r_pos*0.1)-14*b_t-a_t))-(a_t*a_t))*(1/(4*b_t)) + t_gap;
           

           dphi=a_phi*(7-r_rec/10)+b_phi*(7-r_rec/10)*(7.-r_rec/10); // in rad
           

           phi_rec=phi_pad-dphi-phi_gap;
           while(phi_rec < 0 || phi_rec >= 2*PI)
           {
	           if( phi_rec<0.0 )  
	           {
	        	   phi_rec+=2.0*PI;
	           }
	           if( phi_rec>2.0*PI ) 
	           {
	        	   phi_rec-=2.0*PI;
	           }
           }
           //System.out.println("reconstructed phi " + phi_rec + " actual phi " + Math.atan(Y/X));
           
           // x,y,z pos of reconstructed track
           x_rec=r_rec*(Math.cos(phi_rec));
           y_rec=r_rec*(Math.sin(phi_rec));
           //System.out.println("rec " + x_rec + " " + y_rec + " " + phi_rec + " " + r_rec + " " + Time);
           //if(true)
           //if(counter > 2500)
           //if(r_rec > 30 && r_rec < 70)
           //if(Math.abs(x_rec-X) < 3 && Math.abs(y_rec-Y) < 3 && Math.abs(z_rec-Z) < 3)
           //if(true)
           //if(Math.abs(t_calc - Time) < timewindow)
           if(hit.get_TShift() == 0)
           {
        	   //System.out.println(t_calc + " " + Time);
           grz.addPoint(r_rec, z_rec, 0, 0);
           gxy.addPoint(x_rec, y_rec, 0, 0);
           //if(counter <= 53)
           //{
        	   g1rz.addPoint(Math.sqrt(X*X+Y*Y), Z, 0, 0);
        	   g1xy.addPoint(X, Y, 0, 0);
        	   g2xy.addPoint(0, 0, 0, 0);
        	   gerrorxvsphi.addPoint(Math.atan2(Y,X),Math.pow(Math.abs(x_rec-X),2)/X,0,0);
        	   gerroryvsphi.addPoint(Math.atan2(Y,X),Math.pow(Math.abs(y_rec-Y),2)/Y,0,0);
           //}
        	   //System.out.println(counter);
           }
           else
           {
        	   //g2xy.addPoint(X, Y, 0, 0);
        	   g2xy.addPoint(0, 0, 0, 0);
        	   grz.addPoint(0, 0, 0, 0);
        	   g1rz.addPoint(0, 0, 0, 0);
        	   gxy.addPoint(0, 0, 0, 0);
        	   g1xy.addPoint(0, 0, 0, 0);
           }
           //}
           // x,y,z pos of pad hit
           //x_pad=(PAD_S)*(Math.cos(phi_pad));
           //y_pad=(PAD_S)*(Math.sin(phi_pad));
           
           // actual position on pad of hits
           //phi_hit=phi_rad-(row*phi_per_pad);
           //z_hit=Z-z0-(col*PAD_L)-z_shift;
           
           // find differences (delta = generated-reconstructed)
           //delta_x=X-x_rec;
           //delta_y=Y-y_rec;
           //delta_z=Z-z_rec;
           //delta_r=r_pos-r_rec;
           //delta_phi = phi_pos-phi_rec;
           //System.out.println(X + " " + x_rec);
           //System.out.println(r_pos + " " + r_rec);

       
       
    			/*hit.set_cellID(cellID);
    			hit.set_Time(t_s2pad);
    			hit.set_Edep(Edep);
    			hit.set_PosX(x_rec);
    			hit.set_PosY(y_rec);
    			hit.set_PosZ(z_rec);
           TimeVec.add(t_s2pad);
           XVec.add(x_rec);
           YVec.add(y_rec);
           ZVec.add(z_rec);*/
			//}
           counter++;
		}
		}
		//System.out.println(counter);
		GraphErrors g2 = new GraphErrors();
		double theta = 0;
		double test_x = 0;
		double test_y = 0;
		double test_x2 = 0;
		double test_y2 = 0;
		while(theta <= 2*Math.PI)
		{
			test_x = 30 * Math.cos(theta);
			test_y = 30 * Math.sin(theta);
			test_x2 = 70 * Math.cos(theta);
			test_y2 = 70 * Math.sin(theta);
			g2.addPoint(test_x, test_y, 0, 0);
			g2.addPoint(test_x2, test_y2, 0, 0);
			theta+=0.01;
		}
		int color = 2;
		int shape = 1;
		for(int key : graphmap.keySet())
		{
			double szPos[][] = new double[recohitvector.get(key).size()][3];
			for(int i = 0; i < recohitvector.get(key).size(); i++) {
				szPos[i][0] = recohitvector.get(key).get(i).x();
				szPos[i][1] = recohitvector.get(key).get(i).y();
				szPos[i][2] = recohitvector.get(key).get(i).z();
			}
			//double R = 0; double A = 0; double B = 0; double Phi_deg = 0; double Theta_deg = 0; double Z0 = 0; int fit_track_to_beamline = 1;
			//HelixFitJava hf = new HelixFitJava();
	        //hf.HelixFit(recohitvector.get(key).size(), szPos, R, A, B, Phi_deg, Theta_deg, Z0, fit_track_to_beamline);
	        //System.out.println(key + " " + R);
			if(false)
			//if(key == 3)
			{
				graphmap.get(key).setMarkerSize(6);
				graphmap2.get(key).setMarkerSize(6);
			}
			else {
			graphmap.get(key).setMarkerSize(3);
			graphmap.get(key).setMarkerColor(color);
			graphmap.get(key).setMarkerStyle(shape);
			
			
			graphmap2.get(key).setMarkerSize(3);
			graphmap2.get(key).setMarkerColor(color);
			graphmap2.get(key).setMarkerStyle(shape);
			
			}
			//c.draw(graphmap.get(key),"same");		
			//c2.draw(graphmap2.get(key),"same");
			color++;
			if(color > 7) 
			{
				color -= 6;
				shape++;
			}
		}
		grz.setMarkerSize(3);
		grz.setMarkerColor(1);
		g1rz.setMarkerSize(2);
		g1rz.setMarkerColor(2);
		//grz.addPoint(30, -200, 0, 0);
		//grz.addPoint(70, 200, 0, 0);
		gxy.setMarkerSize(2);
		gxy.setMarkerColor(2);
		g1xy.setMarkerSize(2);
		g1xy.setMarkerColor(1);
		g2xy.setMarkerSize(2);
		g2xy.setMarkerColor(7);
		g2.setMarkerSize(0);
		g2.setMarkerColor(3);
		gerrorxvsphi.setMarkerSize(2);
		gerroryvsphi.setMarkerSize(2);
		c.draw(grz,"same");
		c.draw(g1rz,"same");
		c2.draw(gxy,"same");
		c2.draw(g1xy,"same");
		c2.draw(g2,"same");		
		c2.draw(g2xy,"same");
		c3.divide(1, 2);
		c3.cd(0);
		c3.draw(gerrorxvsphi);
		c3.cd(1);
		c3.draw(gerroryvsphi);
		c4.draw(largetdist);
		j.setTitle("RZ");
		j2.setTitle("XY");
		if(draw == true) {
			j.add(c);
			j.setVisible(true);
			j2.add(c2);
			j2.setVisible(true);
			//j3.add(c3);
			//j3.setVisible(true);
			j4.add(c4);
			//j4.setVisible(true);
		}
	
		//System.out.println(outsidedriftcounter + "/" + allhitscounter);
		params.set_alltracks(alltracks);
		params.set_recohitvector(recohitvector);
	}	
	
	double get_rec_coef(double t1, double t2, double t3, double t4, double t5, double z2) {
		double z = z2/1000;
		return t1*z*z*z*z + t2*z*z*z + t3*z*z + t4*z + t5;
		
	}

	
}
