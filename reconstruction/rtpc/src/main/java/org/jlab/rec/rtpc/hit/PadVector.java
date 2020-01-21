package org.jlab.rec.rtpc.hit;

import org.jlab.clas.physics.Vector3;

public class PadVector {
	
    private Vector3 _vec; 
    /*private double _x;
    private double _y; 
    private double _z; 
    private double _phi;*/
    private final double PAD_W = 2.79; // in mm
    private final double PAD_S = 80.0; //in mm
    private final double PAD_L = 4.0; // in mm
    private final double RTPC_L= 384.0; // in mm	    
    private double phi_pad;		    
    private final double Num_of_Cols = RTPC_L/PAD_L;
    private final double PI=Math.PI;
    private final double z0 = -(RTPC_L/2.0);
    private final double phi_per_pad = PAD_W/PAD_S; // in rad	
    private double chan; 
    private double col;
    private double row;
    private double z_shift; 
    private double z_pad;
	
    public PadVector(int padnum){			
        chan = (double)padnum;       
        col = (chan-1)%Num_of_Cols+1;
        row=(chan-col)/Num_of_Cols;
        z_shift = row%4;

        phi_pad=(row*phi_per_pad)+(phi_per_pad/2.0);

        if(phi_pad>= 2.0*PI) {
            phi_pad -= 2.0*PI;
        }
        if(phi_pad<0){
            phi_pad += 2.0*PI;
        }

        z_pad=z0+((col-1)*PAD_L)+(PAD_L/2.0)+z_shift;
        
        _vec = new Vector3(
            PAD_S*Math.cos(phi_pad),
            PAD_S*Math.sin(phi_pad),
            z_pad);
        
        /*
        set_x(PAD_S*Math.cos(phi_pad));
        set_y(PAD_S*Math.sin(phi_pad));
        set_z(z_pad);
        set_phi(phi_pad);   */    
    }

    public double x(){
            return _vec.x(); 
    }

    public double y(){
            return _vec.y();
    }

    public double z(){
            return _vec.z();
    }

    public double phi(){
            return _vec.phi();
    }
}
