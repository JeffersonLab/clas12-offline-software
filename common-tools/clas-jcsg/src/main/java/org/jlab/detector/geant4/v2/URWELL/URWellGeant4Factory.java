package org.jlab.detector.geant4.v2.URWELL;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.geant4.v2.Geant4Factory;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;



/**
 * Generate GEANT4 volume for the URWELL detector
 * 
 * @author bondi
 */
public final class URWellGeant4Factory extends Geant4Factory {
    
    

    /**
     * Create the URWELL full geometry
     * @param cp
     */
    public URWellGeant4Factory( DatabaseConstantProvider cp) {
        URWellConstants.connect(cp );
        motherVolume = new G4World("fc");

        for (int iregion = 0; iregion < 1; iregion++) {
            for (int isector = 0; isector < 6; isector++) {
                Geant4Basic regionVolume = createRegion(isector, iregion);
                regionVolume.setMother(motherVolume);
            }
        }
    }

    /**
     * Calculates the total detector thickness from the sum of the individual
     * layers thicknesses
     * 
     * @return thickness in cm
     */
    public double getChamberThickness(){
        double chamberT =0;
         for (int i=0; i< URWellConstants.CHAMBERVOLUMESTHICKNESS.length; i++ )
             chamberT+=URWellConstants.CHAMBERVOLUMESTHICKNESS[i];
         return chamberT;
    }


    /**
     * Creates and positions the region volume in the given sector, and 
     * populates it with the three chamber volumes
     * 
     * @param isector (0-5)
     * @param iregion (0)
     * @return the region volume
     */
    public Geant4Basic createRegion(int isector, int iregion) {

       
       double regionDZ    = (this.getChamberThickness())/2. + URWellConstants.ZENLARGEMENT ;
       double regionDY    = URWellConstants.SECTORHEIGHT/2 + URWellConstants.YENLARGEMENT ;
       double regionDX0   = URWellConstants.DX0CHAMBER0 + URWellConstants.XENLARGEMENT ;
       double regionDX1   = (regionDY*2)*Math.tan(Math.toRadians(URWellConstants.THOPEN/2))+regionDX0 ;  
       double regionThilt = Math.toRadians(URWellConstants.THTILT);

        // baricenter coordinate in CLAS12 frame 

        Vector3d vCenter = new Vector3d(0, 0, 0);
        vCenter.x = 0 ;
        vCenter.y =URWellConstants.SECTORHEIGHT/2*Math.cos(regionThilt)+URWellConstants.YMIN;
        vCenter.z =-URWellConstants.SECTORHEIGHT/2*Math.sin(regionThilt)+URWellConstants.ZMIN;
        vCenter.rotateZ(-Math.toRadians(90 - isector * 60));

                // Sector construction
        Geant4Basic sectorVolume = new G4Trap("region_uRwell_" + (iregion + 1) + "_s" + (isector + 1),
                regionDZ, -regionThilt, Math.toRadians(90.0),
                regionDY, regionDX0, regionDX1, 0.0,
                regionDY, regionDX0, regionDX1, 0.0);

        sectorVolume.rotate("yxz", 0.0, regionThilt, Math.toRadians(90.0 - isector * 60.0));
        sectorVolume.translate(vCenter.x, vCenter.y, vCenter.z);
        sectorVolume.setId(isector + 1, iregion + 1, 0, 0);
        
               // Chambers construction
         for (int ich = 0; ich < URWellConstants.NCHAMBERS; ich++) {
             double y_chamber = (2*ich+1)*(URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS/2+0.05);
             //double y_chamber = (2*ich+1)*(SECTORHEIGHT/nChambers/2);
             Geant4Basic chamberVolume = this.createChamber(isector, iregion, ich);
             chamberVolume.setName("rg" + (iregion + 1) + "_s" + (isector + 1) + "_c" + (ich +1));
             chamberVolume.setMother(sectorVolume);
             chamberVolume.translate(0.0,y_chamber-URWellConstants.SECTORHEIGHT/2,0. );
             chamberVolume.setId(isector + 1, iregion + 1, ich +1, 0);
         }
               
        return sectorVolume;
    }

    

    /**
     * Creates the chamber volume 
     * 
     * @param iSector (0-5)
     * @param iRegion (0)
     * @param iChamber (0, 1, 2)
     * @return the chamber volume
     */
    
    public Geant4Basic createChamber(int iSector, int iRegion, int iChamber) {
        
  
        double chamberDZ    = (this.getChamberThickness())/2. + URWellConstants.ZENLARGEMENT/2;
        double chamberDX0   = (iChamber*URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS)*Math.tan(Math.toRadians(URWellConstants.THOPEN/2.))+ URWellConstants.DX0CHAMBER0 +0.1; 
        double chamberDX1   = (URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS)*Math.tan(Math.toRadians(URWellConstants.THOPEN/2.))+chamberDX0;  
        double chamberDY    = URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS/2+0.05;
        double chamberThilt = Math.toRadians(URWellConstants.THTILT);
        Geant4Basic chamberVolume = new G4Trap("r" + (iRegion + 1) + "_s" + (iSector + 1) + "_c" + (iChamber+1),
                chamberDZ, -chamberThilt, Math.toRadians(90.0),
                chamberDY, chamberDX0, chamberDX1, 0.0,
                chamberDY, chamberDX0, chamberDX1, 0.0);

        double  daugtherVolumeZ =0;
        double  daughterVolumeY =0;
        double[] chamberDim = getChamberDimensions(iChamber);
       
        double daugtherDX0 = chamberDim[0];
        double daugtherDX1 = chamberDim[1];
        double daugtherDY  = chamberDim[2];
       
        for (int i=0; i< URWellConstants.CHAMBERVOLUMESTHICKNESS.length; i++ ){
 
            if(i==0) {daugtherVolumeZ = URWellConstants.CHAMBERVOLUMESTHICKNESS[i]/2 - (this.getChamberThickness())/2.;
             } else daugtherVolumeZ += URWellConstants.CHAMBERVOLUMESTHICKNESS[i-1]/2 + URWellConstants.CHAMBERVOLUMESTHICKNESS[i]/2;
            
            daughterVolumeY = -daugtherVolumeZ *Math.tan(Math.toRadians(URWellConstants.THTILT));
          
            Geant4Basic daugtherVolume = new G4Trap("daughter_volume",
                URWellConstants.CHAMBERVOLUMESTHICKNESS[i]/2, -chamberThilt, Math.toRadians(90.0),
                daugtherDY, daugtherDX0, daugtherDX1, 0.0,
                daugtherDY, daugtherDX0, daugtherDX1, 0.0);
        
            daugtherVolume.setName("rg" + (iRegion + 1) + "_s" + (iSector + 1) + "_c" + (iChamber +1) +"_"+URWellConstants.CHAMBERVOLUMESNAME[i] );
            daugtherVolume.setMother(chamberVolume);
            daugtherVolume.setPosition(0.0, daughterVolumeY,daugtherVolumeZ);
        }
        return  chamberVolume;
    }
    
    /**
     * Calculates the chamber dimensions
     * 
     * @param ichamber (0, 1, 2)
     * @return an array of doubles containing the x, y, z, dimensions
     */
    public double[] getChamberDimensions(int ichamber){
    
        double[] chamberDimensions = new double[3];
        chamberDimensions[0] = (ichamber*URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS)
                             * Math.tan(Math.toRadians(URWellConstants.THOPEN/2.))
                             + URWellConstants.DX0CHAMBER0 ;
    
        chamberDimensions[1] = (URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS)
                             * Math.tan(Math.toRadians(URWellConstants.THOPEN/2.))+chamberDimensions[0];
    
        chamberDimensions[2] = URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS/2 ;
    
        return chamberDimensions;
    }

    /**
     * Returns the chamber volume for the chosen sector and chamber
     * 
     * @param sector (1-6)
     * @param chamber (1, 2, 3)
     * @return the chamber volume
     */
    public Geant4Basic getChamberVolume(int sector, int chamber) {

        int r = 1;
        int s = sector;
        int c = chamber;

        String volumeName = "rg" + r + "_s" + s + "_c" + c + "_cathode_gas";
        return this.getAllVolumes().stream()
                      .filter(volume -> (volume.getName().contains(volumeName)))
                      .findAny()
                      .orElse(null);
    }

    /**
     * Returns the sector volume for the given sector number
     * 
     * @param sector (1-6)
     * @return the sector volume
     */
    public Geant4Basic getSectorVolume(int sector) {

        int r = 1;
        int s = sector;

        String volName = "region_uRwell_" + r + "_s" + s;
        return this.getAllVolumes().stream()
                      .filter(volume -> (volume.getName().contains(volName)))
                      .findAny()
                      .orElse(null);
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
