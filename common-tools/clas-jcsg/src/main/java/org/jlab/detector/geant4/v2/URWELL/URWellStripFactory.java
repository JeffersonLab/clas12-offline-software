package org.jlab.detector.geant4.v2.URWELL;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geometry.prim.Line3d;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.volume.Geant4Basic;
import java.util.List;
import org.jlab.detector.hits.DetHit;
import org.jlab.geometry.prim.Straight;



public class URWellStripFactory
{
        DatabaseConstantProvider cp = new DatabaseConstantProvider(11, "default");
        URWellGeant4Factory factory = new URWellGeant4Factory(cp);
    
    public URWellStripFactory( DatabaseConstantProvider cp )
	{
            URWellConstants.connect( cp );
  
	}
	

    
    public int[] getNumberStripSector()
        {
            int[] Nstrip = new int[3];
            for(int i=0; i<URWellConstants.nChambers;i++)
                {
                    Nstrip[i]= getNumberStripChamber( i);
                }
            return Nstrip;
        }
        
    public  int getNumberStripChamber(int aChamber)
        {
        
            double[] dim = new double[3];
            dim =  factory.CalDimensions(aChamber);
             
           
           double Xhalf_base = dim[0];
           double Xhalf_Largebase = dim[1];
           double Yhalf = dim[2];

  
            	// C-------------D //
                //  -------------  //
                //   -----------   //
                //    A-------B   //


    /*** number of strip in AB***/


	int n_AB;
        n_AB = (int)(2*Xhalf_base/(URWellConstants.strip_pitch/
                Math.sin(Math.toRadians(URWellConstants.strip_stereo_angle))));

	
	double AC = Math.sqrt((Math.pow((Xhalf_base-Xhalf_Largebase),2) + Math.pow((2*Yhalf),2)));
        double theta = Math.acos(2*Yhalf/AC);
	int n_AC = (int)(AC/(URWellConstants.strip_pitch/
                Math.cos(theta-Math.toRadians(URWellConstants.strip_stereo_angle))));

	int NStrips = n_AB + n_AC+1;

            return NStrips;
        }
    

    public int getChamberOfStrip(int SStrip){
        int ID_chamber=0;
        if(SStrip <= getNumberStripChamber(0)) ID_chamber =0;
        if (SStrip>getNumberStripChamber(0) && 
            SStrip<=(getNumberStripChamber(0) + getNumberStripChamber(1)) ) ID_chamber =1;
        if(SStrip>(getNumberStripChamber(0) + getNumberStripChamber(1))) ID_chamber =2;
        return ID_chamber;
    }
   

    
    
    public Line3d createStrip(int aSector, int ALayer,int SStrip){
               

        Line3d strip_line;

        
        int ID_chamber = getChamberOfStrip(SStrip);

        //Strip ID wrt sector -> strip ID chamber (from 1 to getNumberStripChamber)
        int[] N_strip_chamber  = new int[3];
        N_strip_chamber  = getNumberStripSector();
        int Tot_strip =0;
        if(ID_chamber>0){
            for (int i=0; i<ID_chamber; i++) Tot_strip += N_strip_chamber[i];
        }
        //Strip ID: from 1 to  getNumberStripChamber       
        int Cstrip = SStrip - Tot_strip;

        // CHAMBER reference frame
        // new numeration with stri ID_strip=0 crossing (0,0,0) of chamber
        
        double[] dim = new double[3];
        dim =  factory.CalDimensions(ID_chamber);
        
         // Y coordinate of the intersection point between the x=0 and the strip line crossing for B
        double DY = -dim[2] - Math.tan(Math.toRadians(URWellConstants.strip_stereo_angle))*dim[0];
        
        // ID of the strip 
        int N_s = (int) (DY*Math.cos(Math.toRadians(URWellConstants.strip_stereo_angle))/URWellConstants.strip_pitch);
        int NCstrip = N_s + (Cstrip -1);
        
        //strip straight line chamber reference frame -> y = mx +c; 
        double stereo_angle = URWellConstants.strip_stereo_angle;
        if (ALayer%2 !=0) stereo_angle = -URWellConstants.strip_stereo_angle;
        double m = Math.tan(Math.toRadians(stereo_angle));
        double c = NCstrip*URWellConstants.strip_pitch/Math.cos(Math.toRadians(stereo_angle));
        
        // Take 2 points in the strip straight line. They needs to define Line object 
         double O_x = -dim[1];
         double O_y = -dim[1]*m+c;
         double O_z = 0;
         Vector3d origin = new Vector3d(O_x, O_y, O_z);
         
         double E_x = dim[1];
         double E_y = dim[1]*m+c;
         double E_z = 0;
         Vector3d end = new Vector3d(E_x, E_y, E_z);
         
        // Get Chamber Volume
         Geant4Basic Cvolume = getChamberVolume(aSector, SStrip);
        // 2 point defined before wrt the GLOBAL frame     
        Vector3d Global_origin = Cvolume.getGlobalTransform().transform(origin);
        Vector3d Global_end = Cvolume.getGlobalTransform().transform(end);
        
        Straight line = new Line3d(Global_origin, Global_end);
        
        // CHECK intersections between line and volume
        Cvolume.makeSensitive();
        List<DetHit> Hits =Cvolume.getIntersections(line);
        if (Hits.size()>=1){
            strip_line = new Line3d(Hits.get(0).origin(), Hits.get(0).end());
     
       }else strip_line = null;
        
        return strip_line;
    }
    
    
    public Geant4Basic getChamberVolume(int aSector, int SStrip){
        

        Geant4Basic Cvolume;
        int ID_chamber = getChamberOfStrip(SStrip);
        int r=1;
        int s=aSector;
        int ca= ID_chamber+1;
            
        String vol_string = "rg" +r+"_s" +s+ "_c" +ca+"_cathode_gas";
        Cvolume = factory.getAllVolumes().stream()
                         .filter(volume->(volume.getName().contains(vol_string))) 
                         .findAny()
                         .orElse(null);
        return Cvolume;
    }
    
    public Geant4Basic getSectorVolume(int aSector){
        
        Geant4Basic Svolume;
        int r=1;
        int s=aSector;
        
            
        String vol_string = "region_uRwell_" +r+"_s" +s;
        Svolume = factory.getAllVolumes().stream()
                         .filter(volume->(volume.getName().contains(vol_string))) 
                         .findAny()
                         .orElse(null);
        
        return Svolume;
    }
    
    public Line3d getGLobalStrip(int aSector, int aLayer, int SStrip){
        
        Line3d stripLine = createStrip( aSector, aLayer, SStrip);
        
        return stripLine;
    }
    
    public Line3d getLocalStrip(int aSector, int aLayer, int SStrip){
        
        Line3d GlobalStripLine = createStrip( aSector, aLayer, SStrip);
        Geant4Basic Svolume = getSectorVolume(aSector);

        Vector3d origin = Svolume.getGlobalTransform().invert().transform(GlobalStripLine.origin());
        Vector3d end = Svolume.getGlobalTransform().invert().transform(GlobalStripLine.end());

        Line3d LocalStripLine = new Line3d(origin, end);
        
        return LocalStripLine;
    }
    
    public static void main(String[] args) {
            DatabaseConstantProvider cp = new DatabaseConstantProvider(11, "default");

            URWellConstants.connect(cp);
        
            URWellGeant4Factory factory = new URWellGeant4Factory(cp);
            URWellStripFactory factory2 = new URWellStripFactory(cp);
   
             Line3d strip_line = factory2.getLocalStrip(1, 0, 1600);
              System.out.println(strip_line.toString());
             
    }

	


}
