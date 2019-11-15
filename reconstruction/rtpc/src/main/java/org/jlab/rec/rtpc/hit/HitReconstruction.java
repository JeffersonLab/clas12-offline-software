package org.jlab.rec.rtpc.hit;

import java.util.List;
import java.util.Vector;

public class HitReconstruction {

	public HitReconstruction() {
		// TODO Auto-generated constructor stub
	}

	public void Reco(HitParameters params) {
		List<Integer> PadList = params.get_PadList();
		List<Double> weightave = params.get_weightave();
		List<Double> maxinte = params.get_maxinte();

		int p = 0;
		//for(Hit hit : rawHits) {
		for(p = 0; p<PadList.size(); p++)	{
			//reco here
			
			int cellID = PadList.get(p);
			double Time = weightave.get(p);
			//double Edep = maxinte.get(p);
			//double X = hit.get_PosXTrue();
			//double Y = hit.get_PosYTrue();
			//double Z = hit.get_PosZTrue();
			
			
			//char filename[100];
			int NEve = 10;
			    
			double PAD_W = 2.79; // in mm
			double PAD_S = 80.0; //in mm
	        double PAD_L = 4.0; // in mm
		    double RTPC_L = 400.0; // in mm
			    
		    double Num_of_Rows = (2.0*(Math.PI)*PAD_S)/PAD_W;
	        double Num_of_Cols = RTPC_L/PAD_L;
		    double TotChan = Num_of_Rows*Num_of_Cols;
			    
		    double PI=Math.PI;
			    
		    double z0 = -(RTPC_L/2.0); // front of RTPC in mm at the center of the pad

		    double phi_per_pad = PAD_W/PAD_S; // in rad


		    
		 // MagBoltz parameters
		    double a_t=1741.179712, b_t=-1.25E+02; // for f(x)=time(radius)
		    double a_phi=0.161689123, b_phi=0.023505021; // for f(x)=dphi(radius)
		    
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
		           
		           double x_pad=0;
		           double x_rec=0;
		           double delta_x=0;
		           
		           double y_pad=0;
		           double y_rec=0;
		           double delta_y=0;
		           
		           double z_pad=0;
		           double z_rec=0;
		           double z_hit=0;  // position of the hit on a single pad in z
		           double delta_z=0;
		           
		           double r_pos=0;
		           double r_rec=0;
		           double r_temp=0;
		           double delta_r=0;
		           
		           double phi_pos=0;
		           double phi_rt=0;
		           double phi_hit=0; // position of the hit on a single pad in phi
		           double phi_pad=0;
		           double phi_rec=0;
		           double phi_rad=0;
		           double phi_rad_temp=0;
		           double delta_phi=0;

		           
		           
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
		           
		           
		           // find reconstructed position of ionization from Time info

		           t_s2pad = Time-t_gap;

		           r_rec=((-(Math.sqrt(a_t*a_t+(4.*b_t*t_s2pad)))+a_t+(14.*b_t))/(2.*b_t))*10.0; //in mm
		           

		           dphi=a_phi*(7.-r_rec/10.)+b_phi*(7.-r_rec/10.)*(7.-r_rec/10.); // in rad
		           

		           dz=0;

		           phi_rec=phi_pad-dphi-phi_gap;
		           if( phi_rec<0.0 )  
		           {
		        	   phi_rec+=2.0*PI;
		           }
		           if( phi_rec>2.0*PI ) 
		           {
		        	   phi_rec-=2.0*PI;
		           }
		           
		           // x,y,z pos of reconstructed track
		           x_rec=r_rec*(Math.cos(phi_rec));
		           y_rec=r_rec*(Math.sin(phi_rec));
		           z_rec=z_pad-dz;
		           
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
		    			hit.set_PosZ(z_rec);*/
		           /*TimeVec.add(t_s2pad);
		           XVec.add(x_rec);
		           YVec.add(y_rec);
		           ZVec.add(z_rec);
		           */
		           
		    			
		    			
		}
		/*params.set_time(TimeVec);
		params.set_XVec(XVec);
		params.set_YVec(YVec);
		params.set_ZVec(ZVec);*/
		//System.out.println(XVec.size() + " " + PadList.size());
		//XVec.clear();
		//YVec.clear();
		//ZVec.clear();
		//TimeVec.clear();
		
	}
}
