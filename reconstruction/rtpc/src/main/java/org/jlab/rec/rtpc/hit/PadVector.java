package org.jlab.rec.rtpc.hit;

public class PadVector {
	
    private double _x;
    private double _y; 
    private double _z; 
    private double _phi;
    private double PAD_W = 2.79; // in mm
    private double PAD_S = 80.0; //in mm
    private double PAD_L = 4.0; // in mm
    private double RTPC_L= 384.0; // in mm	    
    private double phi_pad;		    
    private double Num_of_Cols = RTPC_L/PAD_L;
    private double PI=Math.PI;
    private double z0 = -(RTPC_L/2.0);
    private double phi_per_pad = PAD_W/PAD_S; // in rad	
    private double chan; 
    private double col;
    private double row;
    private double z_shift; 
    private double z_pad;
	
    public PadVector(int padnum){			
        chan = (double)padnum;       
        col = chan%Num_of_Cols;
        row=(chan-col)/Num_of_Cols;
        z_shift = row%4;

        phi_pad=(row*phi_per_pad)+(phi_per_pad/2.0);

        if(phi_pad>= 2.0*PI) {
            phi_pad -= 2.0*PI;
        }
        if(phi_pad<0){
            phi_pad += 2.0*PI;
        }

        z_pad=z0+(col*PAD_L)+(PAD_L/2.0)+z_shift;
        
        set_x(PAD_S*Math.cos(phi_pad));
        set_y(PAD_S*Math.sin(phi_pad));
        set_z(z_pad);
        set_phi(phi_pad);       
    }

    private void set_x(double x){
        _x = x; 
    }

    private void set_y(double y){
        _y = y; 
    }

    private void set_z(double z){
        _z = z; 
    }

    private void set_phi(double phi){
        _phi = phi; 
    }

    public double x(){
            return _x; 
    }

    public double y(){
            return _y;
    }

    public double z(){
            return _z;
    }

    public double phi(){
            return _phi;
    }
}
