package org.jlab.rec.rtpc.hit;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Vector;

import javax.swing.JFrame;

import org.jlab.clas.physics.Vector3;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvas;

public class HitDistance {

	public void FindDistance(HitParameters params) {
		HashMap<Integer, double[]> ADCMap = params.get_R_adc();
		Vector<Integer> PadNum = params.get_PadNum();
		int Pad = 0; //initializing pad
		int Pad2 = 0;
		int TrigWindSize = params.get_TrigWindSize(); //Trigger Window Size = 10000
		//int StepSize = params.get_StepSize();
		int StepSize = 120; //Time stepsize
		double thresh = 1e-5; //Arbitrary ADC threshold
		double ADC = 0;
		double ADC2 = 0;
		Vector3 TempPadCoords = new Vector3();
		Vector3 CheckPadCoords = new Vector3();
		Vector3 CheckPadPrevCoords = new Vector3();
		EmbeddedCanvas c1 = new EmbeddedCanvas();
		EmbeddedCanvas c2 = new EmbeddedCanvas();
		EmbeddedCanvas c3 = new EmbeddedCanvas();
		H1F t1 = new H1F("t1","t1",100,0,50);
		H1F t2 = new H1F("t2","t2",100,0,8);
		H1F t3 = new H1F("t3","t3",100,0,10);
		t1.setTitleX("xy in mm");
		t2.setTitleX("z in mm");
		t3.setTitleX("ellipse formula result");
		JFrame j1 = new JFrame();
		j1.setSize(800,600);
		j1.setTitle("xy");
		JFrame j2 = new JFrame();
		j2.setSize(800,600);
		j2.setTitle("z");
		JFrame j3 = new JFrame();
		j3.setSize(800,600);
		j3.setTitle("ellipse");
		double EllipseDeltax = 0;
		double EllipseDeltay = 0;
		double EllipseDeltaz = 0;
		double EllipseTotal = 0;
		double PhiDelta = Math.pow(3, 2);
		double ZDelta = Math.pow(6, 2);
		for(int t = 0; t < TrigWindSize; t+=StepSize)
		{
			for(int p = 0; p < PadNum.size(); p ++)
			{
				Pad = PadNum.get(p);
				ADC = ADCMap.get(Pad)[t];
				if(ADC > thresh)
				{	
					Vector3 PadCoords = PadCoords(Pad);
					for(int p2 = 0; p2 < PadNum.size(); p2++)
					{
						if(p2 != p)
						{
							Pad2 = PadNum.get(p2);
							ADC2 = ADCMap.get(Pad2)[t];
							if(ADC2 > thresh)
							{
								CheckPadCoords = PadCoords(Pad2);
								EllipseDeltax = Math.pow(Math.abs(PadCoords.x()-CheckPadCoords.x()),2);
								EllipseDeltay = Math.pow(Math.abs(PadCoords.y()-CheckPadCoords.y()),2);
								EllipseDeltaz = Math.pow(Math.abs(PadCoords.z()-CheckPadCoords.z()),2);
								EllipseTotal = ((EllipseDeltax+EllipseDeltay)/PhiDelta) + (EllipseDeltaz/ZDelta);
								t1.fill(Math.sqrt(EllipseDeltax+EllipseDeltay));
								t2.fill(Math.sqrt(EllipseDeltaz));	
								t3.fill(EllipseTotal);
							}
						}
					}
				}
			}
		}
		c1.draw(t1);
		c2.draw(t2);
		c3.draw(t3);
		j1.add(c1);
		j2.add(c2);
		j3.add(c3);
		j1.setVisible(true);
		j2.setVisible(true);
		j3.setVisible(true);
	}
	private Vector3 PadCoords(int cellID) {
		
		double PAD_W = 2.79; // in mm
		double PAD_S = 80.0; //in mm
        double PAD_L = 4.0; // in mm
	    double RTPC_L=384.0; // in mm	    
	    double phi_pad = 0;		    
	    //double Num_of_Rows = (2.0*(Math.PI)*PAD_S)/PAD_W;
        double Num_of_Cols = RTPC_L/PAD_L;
	    double PI=Math.PI;
	    double z0 = -(RTPC_L/2.0);
	    double phi_per_pad = PAD_W/PAD_S; // in rad		
        double chan = (double)cellID;       
        double col = chan%Num_of_Cols;
        double row=(chan-col)/Num_of_Cols;
        double z_shift = row%4;
   
        phi_pad=(row*phi_per_pad)+(phi_per_pad/2.0);
        
        if(phi_pad>= 2.0*PI) 
        {
        		phi_pad -= 2.0*PI;
        }
        if(phi_pad<0) 
        	{
        		phi_pad += 2.0*PI;
        	}
   
        double z_pad=z0+(col*PAD_L)+(PAD_L/2.0)+z_shift;
        
        Vector3 PadCoords = new Vector3(PAD_S*Math.cos(phi_pad),PAD_S*Math.sin(phi_pad),z_pad);
        return PadCoords;
		
	}
}
