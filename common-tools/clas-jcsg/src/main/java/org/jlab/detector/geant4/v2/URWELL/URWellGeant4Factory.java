package org.jlab.detector.geant4.v2.URWELL;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.geant4.v2.Geant4Factory;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;


/**
 * Generate GEANT4 volume for the URWELL detector
 * 
 * @author bondi
 */
public final class URWellGeant4Factory extends Geant4Factory {
    
    private int nRegions  = URWellConstants.NREGIONS;
    private int nSectors  = URWellConstants.NSECTORS;
    private int nChambers = URWellConstants.NCHAMBERS;
    private boolean isProto = false;
    

    /**
     * Create the URWELL full geometry
     * @param cp
     * @param prototype
     * @param nRegions
     */
    public URWellGeant4Factory( DatabaseConstantProvider cp, boolean prototype, int nRegions) {
        URWellConstants.connect(cp );
        this.init(cp, prototype, nRegions);
    }
    
    public void init(DatabaseConstantProvider cp, boolean prototype, int regions ) {
   
        motherVolume = new G4World("fc");
        isProto = prototype;
        if (prototype == false) {
            nRegions = Math.min(URWellConstants.NMAXREGIONS, regions);
        } else {
            nRegions  = URWellConstants.NREGIONS_PROTO;
            nSectors  = URWellConstants.NSECTORS_PROTO;
            nChambers = URWellConstants.NCHAMBERS_PROTO;
        }

        for (int iregion = 0; iregion <regions ; iregion++) {
            for (int isector = 0; isector < nSectors; isector++) {
                Geant4Basic sectorVolume = createSector(isector, iregion, nChambers);
                sectorVolume.setMother(motherVolume);
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
     * Calculates the sector dimensions
     * @p    * @return an array of doubles containing trapezoid dimensions:     
     **  half thickness, half small base , half large base, half height, tilt angle
     */
    
    public double[] getSectorDimensions(){
        double[] SectorDimensions = new double[5];
        if(isProto==false){
            SectorDimensions[0] = (this.getChamberThickness())/2. + URWellConstants.ZENLARGEMENT ;
            SectorDimensions[1] = URWellConstants.SECTORHEIGHT/2 + URWellConstants.YENLARGEMENT ;
            SectorDimensions[2] = URWellConstants.DX0CHAMBER0 + URWellConstants.XENLARGEMENT ;
            SectorDimensions[3] = (SectorDimensions[1]*2)*Math.tan(Math.toRadians(URWellConstants.THOPEN/2))+SectorDimensions[2];  
            SectorDimensions[4] = Math.toRadians(URWellConstants.THTILT);  
        }else{
            
            Line3D AB = new Line3D(URWellConstants.Apoint, URWellConstants.Bpoint);
            Line3D CD = new Line3D(URWellConstants.Cpoint, URWellConstants.Dpoint);
            
            double DX0_PROTO = URWellConstants.Cpoint.distance(URWellConstants.Dpoint);
            double DX1_PROTO = URWellConstants.Apoint.distance(URWellConstants.Bpoint);
            
            Point3D mAB = AB.midpoint();
            Point3D mCD = CD.midpoint();
            
            double h_proto = mAB.distance(mCD);
            
            SectorDimensions[0] = (this.getChamberThickness())/2. + URWellConstants.ZENLARGEMENT ;
            SectorDimensions[1] = h_proto/2 + URWellConstants.YENLARGEMENT ;
            SectorDimensions[2] = DX0_PROTO/2 + URWellConstants.XENLARGEMENT ;
            SectorDimensions[3] = DX1_PROTO/2 + URWellConstants.XENLARGEMENT ;
            SectorDimensions[4] = Math.toRadians(URWellConstants.THTILT);  
           
             
        }
        
        return SectorDimensions;
    }
    
        // Baricenter coordinate in CLAS12 frame
    
    /**
    * Calculates sector baricenter coordinate in CLAS12 frame
     * @param isector
     * @param iregion
     * @param false: all detector, true: is Proto
     * @param isProto
     * @return Vector3d (X,Y,Z)
    */
    
    public Vector3d getCenterCoordinate(int isector, int iregion)
    {
        
        Vector3d vCenter = new Vector3d(0, 0, 0);
        
        if(isProto==true)
        {
            Point3D center;
            Line3D AB = new Line3D(URWellConstants.Apoint, URWellConstants.Bpoint);
            Line3D CD = new Line3D(URWellConstants.Cpoint, URWellConstants.Dpoint);
             
            Point3D mAB = AB.midpoint();
            Point3D mCD = CD.midpoint();
 
            Line3D m = new Line3D(mAB, mCD);
        
            center = m.midpoint();
            
            vCenter.x = center.x();
            vCenter.y = center.y();
            vCenter.z = center.z();
        }else
        {
      
            vCenter.x = 0 ;
            vCenter.y = URWellConstants.SECTORHEIGHT/2*Math.cos(Math.toRadians(URWellConstants.THTILT))+URWellConstants.YMIN[iregion];
            vCenter.z =-URWellConstants.SECTORHEIGHT/2*Math.sin(Math.toRadians(URWellConstants.THTILT))+URWellConstants.ZMIN[iregion];
            vCenter.rotateZ(-Math.toRadians(90 - isector * 60));
 
        }
        return vCenter;
    }
    

    /**
     * Creates and positions the region volume in the given sector, and 
        populates it with the three chamber volumes
     * @param isector (0-5)
     * @param iregion (0)
     * @param Nchambers : number of chambers in each sector
     * @param isProto
     * @return the region volume
     */
    public Geant4Basic createSector(int isector, int iregion, int Nchambers) {

        double[] dimSect = this.getSectorDimensions();
        /*
       double regionDZ    = (this.getChamberThickness())/2. + URWellConstants.ZENLARGEMENT ;
       double regionDY    = URWellConstants.SECTORHEIGHT/2 + URWellConstants.YENLARGEMENT ;
       double regionDX0   = URWellConstants.DX0CHAMBER0 + URWellConstants.XENLARGEMENT ;
       double regionDX1   = (regionDY*2)*Math.tan(Math.toRadians(URWellConstants.THOPEN/2))+regionDX0 ;  
       double regionThilt = Math.toRadians(URWellConstants.THTILT);
       */
        double regionDZ    = dimSect[0] ;
        double regionDY    = dimSect[1] ;
        double regionDX0   = dimSect[2] ;
        double regionDX1   = dimSect[3] ; 
        double regionThilt = dimSect[4] ;
        // baricenter coordinate in CLAS12 frame 

        /*
        Vector3d vCenter = new Vector3d(0, 0, 0);
            
        vCenter.x = 0 ;
        vCenter.y =URWellConstants.SECTORHEIGHT/2*Math.cos(regionThilt)+URWellConstants.YMIN[iregion];
        vCenter.z =-URWellConstants.SECTORHEIGHT/2*Math.sin(regionThilt)+URWellConstants.ZMIN[iregion];
        vCenter.rotateZ(-Math.toRadians(90 - isector * 60));
        */
        Vector3d vCenter = this.getCenterCoordinate(isector, iregion);
        if(isProto == true) isector =5;
                // Sector construction
        Geant4Basic sectorVolume = new G4Trap("region_uRwell_" + (iregion + 1) + "_s" + (isector + 1),
                regionDZ, -regionThilt, Math.toRadians(90.0),
                regionDY, regionDX0, regionDX1, 0.0,
                regionDY, regionDX0, regionDX1, 0.0);
  
        if(isProto==true) regionThilt = regionThilt + Math.toRadians(180);
        sectorVolume.rotate("yxz", 0.0, regionThilt, Math.toRadians(90.0 - isector * 60.0));
        sectorVolume.translate(vCenter.x, vCenter.y, vCenter.z);
        sectorVolume.setId(isector + 1, iregion + 1, 0, 0);
        
        if (isProto==true) sectorVolume.setName("region_uRwell_" + (iregion + 1) + "_s" + (isector + 1) + "_proto");
        
               // Chambers construction
        for (int ich = 0; ich < Nchambers; ich++) {

            double y_chamber = (2*ich+1)*(URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS/2+0.05);

            Geant4Basic chamberVolume = this.createChamber(isector, iregion, ich);

            if (isProto==true) {
                chamberVolume.setName("rg" + (iregion + 1) + "_s" + (isector + 1) + "_c" + (ich +1) + "_proto");
            }else{
                chamberVolume.setName("rg" + (iregion + 1) + "_s" + (isector + 1) + "_c" + (ich +1));
            }
             
            chamberVolume.setMother(sectorVolume);
            if(isProto==false) chamberVolume.translate(0.0,y_chamber-URWellConstants.SECTORHEIGHT/2,0. );
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
     * @param isProto (true, false)
     * @return the chamber volume
     */
    
    public Geant4Basic createChamber(int iSector, int iRegion, int iChamber) {
        
        double[] dimChamb = this.getChamber_Dimensions(iChamber);
        
        double chamberDZ    = dimChamb[0];
        double chamberDY    = dimChamb[1];
        double chamberDX0   = dimChamb[2];     
        double chamberDX1   = dimChamb[3];
        double chamberThilt = dimChamb[4];

        Geant4Basic chamberVolume = new G4Trap("r" + (iRegion + 1) + "_s" + (iSector + 1) + "_c" + (iChamber+1),
                chamberDZ, -chamberThilt, Math.toRadians(90.0),
                chamberDY, chamberDX0, chamberDX1, 0.0,
                chamberDY, chamberDX0, chamberDX1, 0.0);

        double  daughterVolumeZ =0;
        double  daughterVolumeY =0;
        double[] chamberDim = getChamber_daughter_Dimensions(iChamber);
       
        double daughterDY  = chamberDim[0];
        double daughterDX0 = chamberDim[1];
        double daughterDX1 = chamberDim[2];
        
       
        for (int i=0; i< URWellConstants.CHAMBERVOLUMESTHICKNESS.length; i++ ){
 
            if(i==0) {daughterVolumeZ = URWellConstants.CHAMBERVOLUMESTHICKNESS[i]/2 - (this.getChamberThickness())/2.;
             } else daughterVolumeZ += URWellConstants.CHAMBERVOLUMESTHICKNESS[i-1]/2 + URWellConstants.CHAMBERVOLUMESTHICKNESS[i]/2;
            
            daughterVolumeY = -daughterVolumeZ *Math.tan(Math.toRadians(URWellConstants.THTILT));
          
            
            Geant4Basic daughterVolume = new G4Trap("daughter_volume",
                URWellConstants.CHAMBERVOLUMESTHICKNESS[i]/2, -chamberThilt, Math.toRadians(90.0),
                daughterDY, daughterDX0, daughterDX1, 0.0,
                daughterDY, daughterDX0, daughterDX1, 0.0);
            
            if (isProto==true) {
                daughterVolume.setName("rg" + (iRegion + 1) + "_s" + (iSector + 1) + "_c" + (iChamber +1) +"_"+URWellConstants.CHAMBERVOLUMESNAME[i] + "_proto");
            }else{
                daughterVolume.setName("rg" + (iRegion + 1) + "_s" + (iSector + 1) + "_c" + (iChamber +1) +"_"+URWellConstants.CHAMBERVOLUMESNAME[i] );
            }
            
            daughterVolume.setMother(chamberVolume);
            daughterVolume.setPosition(0.0, daughterVolumeY,daughterVolumeZ);
        }
        return  chamberVolume;
    }
    
    
    public double[] getChamber_Dimensions(int ichamber)
    {
        
        double[] chamber_Dimensions = new double[5];
        
        if(isProto == false){  
            chamber_Dimensions[0] = (this.getChamberThickness())/2. + URWellConstants.ZENLARGEMENT/2;
            chamber_Dimensions[1] = URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS/2+0.05;
            chamber_Dimensions[2] = (ichamber*URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS)*
                                        Math.tan(Math.toRadians(URWellConstants.THOPEN/2.))+ URWellConstants.DX0CHAMBER0 +0.1; 
            chamber_Dimensions[3] = (URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS)*
                                        Math.tan(Math.toRadians(URWellConstants.THOPEN/2.))+chamber_Dimensions[2];  
            chamber_Dimensions[4] = Math.toRadians(URWellConstants.THTILT);
        }else
        {
            Line3D AB = new Line3D(URWellConstants.Apoint, URWellConstants.Bpoint);
            Line3D CD = new Line3D(URWellConstants.Cpoint, URWellConstants.Dpoint);
            
            double DX0_PROTO = URWellConstants.Cpoint.distance(URWellConstants.Dpoint);
            double DX1_PROTO = URWellConstants.Apoint.distance(URWellConstants.Bpoint);
            
            Point3D mAB = AB.midpoint();
            Point3D mCD = CD.midpoint();
            double h_proto = mAB.distance(mCD);
            
            chamber_Dimensions[0] = (this.getChamberThickness())/2. + URWellConstants.ZENLARGEMENT/2. ;
            chamber_Dimensions[1] = h_proto/2 + URWellConstants.YENLARGEMENT -0.1 ;
            chamber_Dimensions[2] = DX0_PROTO/2 + URWellConstants.XENLARGEMENT -0.1 ;
            chamber_Dimensions[3] = DX1_PROTO/2 + URWellConstants.XENLARGEMENT -0.1 ; 
            chamber_Dimensions[4]=Math.toRadians(URWellConstants.THTILT);
            
        }

        
        return chamber_Dimensions;
        
    }
    /**
     * Calculates the chamber daughter dimensions
     * 
     * @param ichamber (0, 1, 2)
     * @return an array of doubles containing trapezoid dimensions: half small base , half large base, half height
     */
    public double[] getChamber_daughter_Dimensions(int ichamber){
        double[] chamber_daughter_Dimensions = new double[3];
        
        if(isProto == false)
        {
            chamber_daughter_Dimensions[0] = URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS/2 ;
            chamber_daughter_Dimensions[1] = (ichamber*URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS)
                             * Math.tan(Math.toRadians(URWellConstants.THOPEN/2.))
                             + URWellConstants.DX0CHAMBER0 ;
    
            chamber_daughter_Dimensions[2] = (URWellConstants.SECTORHEIGHT/URWellConstants.NCHAMBERS)
                             * Math.tan(Math.toRadians(URWellConstants.THOPEN/2.))+chamber_daughter_Dimensions[1];
    
        }else
        {

            Line3D AB = new Line3D(URWellConstants.Apoint, URWellConstants.Bpoint);
            Line3D CD = new Line3D(URWellConstants.Cpoint, URWellConstants.Dpoint);
            
            double DX0_PROTO = URWellConstants.Cpoint.distance(URWellConstants.Dpoint);
            double DX1_PROTO = URWellConstants.Apoint.distance(URWellConstants.Bpoint);
            
            Point3D mAB = AB.midpoint();
            Point3D mCD = CD.midpoint();
            
            double h_proto = mAB.distance(mCD);
            
            chamber_daughter_Dimensions[0] = h_proto/2. +0.1;
            chamber_daughter_Dimensions[1] = DX0_PROTO/2.;
            chamber_daughter_Dimensions[2] = DX1_PROTO/2.+0.05;
   
        }
        return chamber_daughter_Dimensions;
    }

    /**
     * Returns the chamber volume for the chosen sector and chamber
     * 
     * @param sector (1-6)
     * @param chamber (1, 2, 3)
     * @return the chamber volume
     */
    public Geant4Basic getChamberVolume(int sector, int chamber,int layer, boolean isProto) {

        int r = (layer-1)/2 +1;
        int s = sector;
        int c = chamber;

        String volumeName;
        if(isProto==false){
            volumeName = "rg" + r + "_s" + s + "_c" + c + "_cathode_gas";
        }else{
            volumeName = "rg" + r + "_s" + s + "_c" + c + "_cathode_gas"+"_proto";
        }
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
    public Geant4Basic getSectorVolume(int region, int sector) {

        int r = region;
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
        
        URWellGeant4Factory factory = new URWellGeant4Factory(cp, true, 2);
            
        factory.getAllVolumes().forEach(volume -> {
            System.out.println(volume.gemcString());
        });
        
     

    }

}
