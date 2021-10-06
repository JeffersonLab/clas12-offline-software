package org.jlab.rec.rtpc.hit;

public class RecoHitVector {
	
	private int pad;
	private double x;
	private double y; 
	private double z;
	private double dt;
	private double time;
        private double _adc;
        private HitVector _smallhit;
        private HitVector _largehit;
        private double _r;
        private double _phi;
       
	
	public RecoHitVector()
	{
		pad = 0;              
		x = 0; 
		y = 0; 
		z = 0; 
		dt = 0;
		time = 0;
                _adc = 0;
	}
	
	public RecoHitVector(int padnum, double xrec, double yrec, double zrec, double rrec, double phirec, double tdiff, double t, double adc, HitVector smallhit, HitVector largehit)
	{
		pad = padnum; 
		x = xrec;
		y = yrec;
		z = zrec;
		dt = tdiff;
		time = t;
                _adc = adc;
                _smallhit = smallhit;
                _largehit = largehit;
                _r = rrec;
                _phi = phirec;
                
	}
	
	public void setpad(int padnum)
	{
            pad = padnum; 
	}
	
	public void setx(double xrec)
	{
            x = xrec;
	}
	
	public void sety(double yrec)
	{
            y = yrec;
	}
	
	public void setz(double zrec)
	{
            z = zrec;
	}
	
	public void settime(double t)
	{
            time = t; 
	}
	
	public void setdt(double tdiff)
	{
            dt = tdiff;
	}
	
	public int pad()
	{
            return pad;
	}
	
	public double x()
	{
            return x;
	}
	
	public double y()
	{
            return y;
	}
	
	public double z()
	{
            return z;
	}
	
	public double time()
	{
            return time;
	}
	
	public double dt()
	{
            return dt;
	}
	
        public double adc(){
            return _adc;
        }
        
        public double r(){
            return _r;
        }
        
        public double phi(){
            return _phi;
        }
        
        public HitVector smallhit(){
            return _smallhit;
        }
        
	public HitVector largehit(){
            return _largehit;
        }

}
