/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2.URWELL;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.geant4.v2.Geant4Factory;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;




///////////////////////////////////////////////////
public final class URWellGeant4Factory extends Geant4Factory {
    
    
    ///////////////////////////////////////////////////
    public URWellGeant4Factory( DatabaseConstantProvider cp) {
        URWellConstants.load( cp );
        motherVolume = new G4World("fc");

        for (int iregion = 0; iregion < 1; iregion++) {
            for (int isector = 0; isector < 6; isector++) {
                Geant4Basic regionVolume = createRegion(isector, iregion);
                regionVolume.setMother(motherVolume);
            }
        }
    }

    public double chamber_thickness(){
        double chamber_t =0;
         for (int i=0; i< URWellConstants.chamber_volumes_thickness.length; i++ )chamber_t+=URWellConstants.chamber_volumes_thickness[i];
         return chamber_t;
}

    ///////////////////////////////////////////////////
    public Geant4Basic createRegion(int isector, int iregion) {

       
       double reg_dz = (this.chamber_thickness())/2. + URWellConstants.z_enlargement ;
       double reg_dy = URWellConstants.sector_height/2 + URWellConstants.y_enlargement ;
       double reg_dx0 = URWellConstants.dx0_chamber0 + URWellConstants.x_enlargement ;
       double reg_dx1 = (reg_dy*2)*Math.tan(Math.toRadians(URWellConstants.th_open/2))+reg_dx0 ;  
       double reg_thtilt = Math.toRadians(URWellConstants.th_tilt);

        // baricenter coordinate in CLAS12 frame 

        Vector3d vcenter = new Vector3d(0, 0, 0);
        vcenter.x = 0 ;
        vcenter.y =URWellConstants.sector_height/2*Math.cos(reg_thtilt)+URWellConstants.y_min;
        vcenter.z =-URWellConstants.sector_height/2*Math.sin(reg_thtilt)+URWellConstants.z_min;
        vcenter.rotateZ(-Math.toRadians(90 - isector * 60));

                // Sector construction
        Geant4Basic sectorVolume = new G4Trap("region_uRwell_" + (iregion + 1) + "_s" + (isector + 1),
                reg_dz, -reg_thtilt, Math.toRadians(90.0),
                reg_dy, reg_dx0, reg_dx1, 0.0,
                reg_dy, reg_dx0, reg_dx1, 0.0);

        sectorVolume.rotate("yxz", 0.0, reg_thtilt, Math.toRadians(90.0 - isector * 60.0));
        sectorVolume.translate(vcenter.x, vcenter.y, vcenter.z);
        sectorVolume.setId(isector + 1, iregion + 1, 0, 0);
        
               // Chambers construction
         for (int ich = 0; ich < URWellConstants.nChambers; ich++) {
             double y_chamber = (2*ich+1)*(URWellConstants.sector_height/URWellConstants.nChambers/2+0.05);
             //double y_chamber = (2*ich+1)*(sector_height/nChambers/2);
             Geant4Basic chamberVolume = this.createChamber(ich, iregion, isector);
             chamberVolume.setName("rg" + (iregion + 1) + "_s" + (isector + 1) + "_c" + (ich +1));
             chamberVolume.setMother(sectorVolume);
             chamberVolume.translate(0.0,y_chamber-URWellConstants.sector_height/2,0. );
             chamberVolume.setId(isector + 1, iregion + 1, ich +1, 0);
         }
               
        return sectorVolume;
    }

    ///////////////////////////////////////////////////
    
    public Geant4Basic createChamber(int iChamber, int iRegion, int iSector) {
        
  
        double ch_dz = (this.chamber_thickness())/2. + URWellConstants.z_enlargement/2;
        double ch_dx0 = (iChamber*URWellConstants.sector_height/URWellConstants.nChambers)*Math.tan(Math.toRadians(URWellConstants.th_open/2.))+ URWellConstants.dx0_chamber0 +0.1; 
 
        double ch_dx1 = (URWellConstants.sector_height/URWellConstants.nChambers)*Math.tan(Math.toRadians(URWellConstants.th_open/2.))+ch_dx0;  
        double ch_dy = URWellConstants.sector_height/URWellConstants.nChambers/2+0.05;
        double ch_thtilt = Math.toRadians(URWellConstants.th_tilt);
        Geant4Basic chamberVolume = new G4Trap("r" + (iRegion + 1) + "_s" + (iSector + 1) + "_c" + (iChamber+1),
                ch_dz, -ch_thtilt, Math.toRadians(90.0),
                ch_dy, ch_dx0, ch_dx1, 0.0,
                ch_dy, ch_dx0, ch_dx1, 0.0);

        double  daughter_volume_z_pos =0;
        double  daughter_volume_y_pos =0;
        double[] Dim = new double[3]; 
               Dim =CalDimensions(iChamber);
       
       double ch_dx0_daughter = Dim[0];
       double ch_dx1_daughter = Dim[1];
       double ch_dy_daughter = Dim[2];
       
       for (int i=0; i< URWellConstants.chamber_volumes_thickness.length; i++ ){
 
            if(i==0) {daughter_volume_z_pos = URWellConstants.chamber_volumes_thickness[i]/2 - (this.chamber_thickness())/2.;
             } else daughter_volume_z_pos += URWellConstants.chamber_volumes_thickness[i-1]/2 + URWellConstants.chamber_volumes_thickness[i]/2;
            
            daughter_volume_y_pos = -daughter_volume_z_pos *Math.tan(Math.toRadians(URWellConstants.th_tilt));
          
            Geant4Basic daughter_volume = new G4Trap("daughter_volume",
                URWellConstants.chamber_volumes_thickness[i]/2, -ch_thtilt, Math.toRadians(90.0),
                ch_dy_daughter, ch_dx0_daughter, ch_dx1_daughter, 0.0,
                ch_dy_daughter, ch_dx0_daughter, ch_dx1_daughter, 0.0);
        
            daughter_volume.setName("rg" + (iRegion + 1) + "_s" + (iSector + 1) + "_c" + (iChamber +1) +"_"+URWellConstants.chamber_volumes_string[i] );
            daughter_volume.setMother(chamberVolume);
            daughter_volume.setPosition(0.0, daughter_volume_y_pos,daughter_volume_z_pos);
        }
        return  chamberVolume;
    }
    
    public double[] CalDimensions(int aChamber){
    
        double[] chamberDimensions = new double[3];
        chamberDimensions[0] = (aChamber*URWellConstants.sector_height/URWellConstants.nChambers)
                           *Math.tan(Math.toRadians(URWellConstants.th_open/2.))
                           + URWellConstants.dx0_chamber0 ;
    
        chamberDimensions[1] = (URWellConstants.sector_height/URWellConstants.nChambers)*
            Math.tan(Math.toRadians(URWellConstants.th_open/2.))+chamberDimensions[0];
    
        chamberDimensions[2] = URWellConstants.sector_height/URWellConstants.nChambers/2 ;
    
    return chamberDimensions;
}

    
    public static void main(String[] args) {
        DatabaseConstantProvider cp = new DatabaseConstantProvider(11, "default");

        URWellConstants.connect(cp);
        
        URWellGeant4Factory factory = new URWellGeant4Factory(cp);
            
        factory.getAllVolumes().forEach(volume -> {
            System.out.println(volume.gemcString());
       });
        
     

    }

}
