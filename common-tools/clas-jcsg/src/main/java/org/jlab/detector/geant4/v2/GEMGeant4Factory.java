/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.HashMap;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geometry.prim.Line3d;

/**
 *
 * @author kenjo
 */
/*
final class GEMdatabase {

    private final int nSectors = 6;
    private final int nRegions = 3;
    private final int nSupers = 6;


    private final double dist2tgt[] = new double[nRegions];
    private final double xdist[] = new double[nRegions];
    private final double frontgap[] = new double[nRegions];
    private final double midgap[] = new double[nRegions];
    private final double backgap[] = new double[nRegions];
    private final double thopen[] = new double[nRegions];
    private final double thtilt[] = new double[nRegions];

    private final double thmin[] = new double[nSupers];
    private final double thster[] = new double[nSupers];
    private final double wpdist[] = new double[nSupers];
    private final double cellthickness[] = new double[nSupers];
    private final int nsenselayers[] = new int[nSupers];
    private final int nguardlayers[] = new int[nSupers];
    private final int nfieldlayers[] = new int[nSupers];
    private final double superwidth[] = new double[nSupers];

    private final double align_dx[][] = new double[nSectors][nRegions];
    private final double align_dy[][] = new double[nSectors][nRegions];
    private final double align_dz[][] = new double[nSectors][nRegions];

    private final double align_dthetax[][] = new double[nSectors][nRegions];
    private final double align_dthetay[][] = new double[nSectors][nRegions];
    private final double align_dthetaz[][] = new double[nSectors][nRegions];
    
    private int nsensewires;
    private int nguardwires;
    
  

    
    private final String dcdbpath = "/geometry/dc/";
    private static GEMdatabase instance = null;

    private GEMdatabase() {
    }

    public static GEMdatabase getInstance() {
        if (instance == null) {
            instance = new GEMdatabase();
        }
        return instance;
    }

    public void connect(ConstantProvider cp) {

        nguardwires = cp.getInteger(dcdbpath + "layer/nguardwires", 0);
        nsensewires = cp.getInteger(dcdbpath + "layer/nsensewires", 0);
        
        for (int ireg = 0; ireg < nRegions; ireg++) {
            dist2tgt[ireg] = cp.getDouble(dcdbpath + "region/dist2tgt", ireg)*Length.cm;
            xdist[ireg] = cp.getDouble(dcdbpath + "region/xdist", ireg)*Length.cm;
            frontgap[ireg] = cp.getDouble(dcdbpath + "region/frontgap", ireg)*Length.cm;
            midgap[ireg] = cp.getDouble(dcdbpath + "region/midgap", ireg)*Length.cm;
            backgap[ireg] = cp.getDouble(dcdbpath + "region/backgap", ireg)*Length.cm;
            thopen[ireg] = Math.toRadians(cp.getDouble(dcdbpath + "region/thopen", ireg));
            thtilt[ireg] = Math.toRadians(cp.getDouble(dcdbpath + "region/thtilt", ireg));
        }
        for (int isuper = 0; isuper < nSupers; isuper++) {
            thmin[isuper] = Math.toRadians(cp.getDouble(dcdbpath + "superlayer/thmin", isuper));
            thster[isuper] = Math.toRadians(cp.getDouble(dcdbpath + "superlayer/thster", isuper));
            wpdist[isuper] = cp.getDouble(dcdbpath + "superlayer/wpdist", isuper)*Length.cm;
            cellthickness[isuper] = cp.getDouble(dcdbpath + "superlayer/cellthickness", isuper);
            nsenselayers[isuper] = cp.getInteger(dcdbpath + "superlayer/nsenselayers", isuper);
            nguardlayers[isuper] = cp.getInteger(dcdbpath + "superlayer/nguardlayers", isuper);
            nfieldlayers[isuper] = cp.getInteger(dcdbpath + "superlayer/nfieldlayers", isuper);

            superwidth[isuper] = wpdist[isuper] * (nsenselayers[isuper] + nguardlayers[isuper] - 1) * cellthickness[isuper];
        }

        int alignrows = cp.length(dcdbpath+"alignment/dx");
        for(int irow = 0; irow< alignrows; irow++) {
               int isec = cp.getInteger(dcdbpath + "alignment/sector",irow)-1;
               int ireg = cp.getInteger(dcdbpath + "alignment/region",irow)-1;

               align_dx[isec][ireg]=cp.getDouble(dcdbpath + "alignment/dx",irow);
               align_dy[isec][ireg]=cp.getDouble(dcdbpath + "alignment/dy",irow);
               align_dz[isec][ireg]=cp.getDouble(dcdbpath + "alignment/dz",irow);

               align_dthetax[isec][ireg]=cp.getDouble(dcdbpath + "alignment/dtheta_x",irow);
               align_dthetay[isec][ireg]=cp.getDouble(dcdbpath + "alignment/dtheta_y",irow);
               align_dthetaz[isec][ireg]=cp.getDouble(dcdbpath + "alignment/dtheta_z",irow);
        }
        
    }
    
    public double dist2tgt(int ireg) {
        return dist2tgt[ireg];
    }

    public double xdist(int ireg) {
        return xdist[ireg];
    }

    public double frontgap(int ireg) {
        return frontgap[ireg];
    }

    public double midgap(int ireg) {
        return midgap[ireg];
    }

    public double backgap(int ireg) {
        return backgap[ireg];
    }

    public double thopen(int ireg) {
        return thopen[ireg];
    }

    public double thtilt(int ireg) {
        return thtilt[ireg];
    }

    public double thmin(int isuper) {
        return thmin[isuper];
    }

    public double thster(int isuper) {
        return thster[isuper];
    }

    public double wpdist(int isuper) {
        return wpdist[isuper];
    }

    public double cellthickness(int isuper) {
        return cellthickness[isuper];
    }

    public int nsenselayers(int isuper) {
        return nsenselayers[isuper];
    }

    public int nguardlayers(int isuper) {
        return nguardlayers[isuper];
    }

    public int nfieldlayers(int isuper) {
        return nfieldlayers[isuper];
    }

    public double superwidth(int isuper) {
        return superwidth[isuper];
    }

    public int nsensewires() {
        return nsensewires;
    }

    public int nguardwires() {
        return nguardwires;
    }

    public int nsuperlayers() {
        return nSupers;
    }

    public int nregions() {
        return nRegions;
    }

    public int nsectors() {
        return nSectors;
    }
    
    public double getAlignmentThetaX(int isec, int ireg) {
        return align_dthetax[isec][ireg];
    }

    public double getAlignmentThetaY(int isec, int ireg) {
        return align_dthetay[isec][ireg];
    }

    public double getAlignmentThetaZ(int isec, int ireg) {
        return align_dthetaz[isec][ireg];
    }

    public Vector3d getAlignmentShift(int isec, int ireg) {
        return new Vector3d(align_dx[isec][ireg], align_dy[isec][ireg], align_dz[isec][ireg]);
    }
}
*/
/*
final class Strip extends Line3d {

    public Strip(Vector3d origin, Vector3d end) {
        super(origin, end);
    }


    public Vector3d dir() {
        Vector3d dir = this.end().minus(this.origin()).normalized();
        return dir;
    }

    public Vector3d top() {
        if (this.origin().y < this.end().y) {
            return new Vector3d(this.end());
        }
        return new Vector3d(this.origin());
    }

    public Vector3d bottom() {
        if (this.origin().y < this.end().y) {
            return new Vector3d(this.origin());
        }
        return new Vector3d(this.end());
    }

    public double length() {
        Vector3d length = new Vector3d(this.end());
        return length.minus(this.origin()).magnitude();
    }

    public Vector3d center() {
        Vector3d center = this.origin().plus(this.end()).dividedBy(2.0);
        return center;
    }
}
*/
///////////////////////////////////////////////////
public final class GEMGeant4Factory extends Geant4Factory {

//    GEMdatabase dbref = GEMdatabase.getInstance();

  //  private final HashMap<String, String> properties = new HashMap<>();
    private final double x_enlargement = 0.5;
    private final double y_enlargement = 1.;
    private final double z_enlargement = 0.1;
    private final double microgap = 0.01;

    private final double th_open = 54.; // opening angle between endplate planes
    private final double th_tilt = 25; // theta tilt
    private final double th_min = 4.694; // polar angle to the base of first chamber
    private final double distance_urwell2dc0 = 4;
    private final double dist_2_tgt = (228.078-distance_urwell2dc0); //206.93 distance from the target to the first chamber - 228.078 is DC-region1
    private final double sector_height = 146.21;  //height of each sector 
    private final double dx0_chamber0 = 5.197;    // halfbase of chamber 1 
    private final int nChambers = 3;              //number of chamber
    private final double  w2tgt = dist_2_tgt/Math.cos(Math.toRadians(th_tilt-th_min));
    private final double  y_min = w2tgt*Math.sin(Math.toRadians(th_min)); // distance from the base chamber1 and beamline
    private final double  z_min = w2tgt*Math.cos(Math.toRadians(th_min));    

    private final double[] chamber_volumes_thickness = {0.0025, 0.0005, 0.3,        // window
        0.0025, 0.0005,0.4,                                                        // cathode
        0.0005, 0.005, 0.0005,                                                      //uRWell + DlC
        0.0005, 0.005, 0.0005,                                                      // Capacitive sharing layer1
        0.0005, 0.005, 0.0005,                                                      // Capacitive sharing layer2
        0.005,  0.0005,0.005, 0.005,  0.0005,0.005, 0.005,                           // Readout
        0.0127, 0.3, 0.0125};                                                       // support
    private final String[] chamber_volumes_string = {"window_kapton", "window_Al", "window_gas",
           "cathode_kapton", "cathode_Al", "cathode_gas",
           "muRwell_Cu", "muRwell_kapton", "muRwell_dlc", 
           "capa_sharing_layer1_glue","capa_sharing_layer1_Cr","capa_sharing_layer1_kapton",
           "capa_sharing_layer2_glue","capa_sharing_layer2_Cr","capa_sharing_layer2_kapton",
           "readout1_glue", "readout1_Cu", "readout1_kapton", "readout2_glue", "readout2_Cu", "readout2_kapton", "readout3_glue",
           "support_skin1_g10", "support_honeycomb_nomex", "support_skin2_g10"};
  
    
    ///////////////////////////////////////////////////
    public GEMGeant4Factory(ConstantProvider provider) {
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
         for (int i=0; i< chamber_volumes_thickness.length; i++ )chamber_t+=chamber_volumes_thickness[i];
         return chamber_t;
}

    ///////////////////////////////////////////////////
    public Geant4Basic createRegion(int isector, int iregion) {

       
       double reg_dz = (this.chamber_thickness())/2. + z_enlargement ;
       double reg_dy = sector_height/2 + y_enlargement ;
       double reg_dx0 = dx0_chamber0 + x_enlargement ;
       double reg_dx1 = (reg_dy*2)*Math.tan(Math.toRadians(th_open/2))+reg_dx0 ;  
       double reg_thtilt = Math.toRadians(th_tilt);

        // baricenter coordinate in CLAS12 frame 

        Vector3d vcenter = new Vector3d(0, 0, 0);
        vcenter.x = 0 ;
        vcenter.y =sector_height/2*Math.cos(Math.toRadians(th_tilt))+y_min;
        vcenter.z =-sector_height/2*Math.sin(Math.toRadians(th_tilt))+z_min;
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
         for (int ich = 0; ich < nChambers; ich++) {
             double y_chamber = (2*ich+1)*(sector_height/nChambers/2+0.05);
             //double y_chamber = (2*ich+1)*(sector_height/nChambers/2);
             Geant4Basic chamberVolume = this.createChamber(ich, iregion, isector);
             chamberVolume.setName("rg" + (iregion + 1) + "_s" + (isector + 1) + "_c" + (ich +1));
             chamberVolume.setMother(sectorVolume);
             chamberVolume.translate(0.0, y_chamber-sector_height/2,0. );
             chamberVolume.setId(isector + 1, iregion + 1, ich +1, 0);
         }
       

        
        return sectorVolume;
    }

    ///////////////////////////////////////////////////
    
    public Geant4Basic createChamber(int iChamber, int iRegion, int iSector) {
        
  
        double ch_dz = (this.chamber_thickness())/2. + z_enlargement/2;
        double ch_dx0 = (iChamber*sector_height/nChambers)*Math.tan(Math.toRadians(th_open/2.))+ dx0_chamber0 +0.1; 
 
        double ch_dx1 = (sector_height/nChambers)*Math.tan(Math.toRadians(th_open/2.))+ch_dx0;  
        double ch_dy = sector_height/nChambers/2+0.05;
        double ch_thtilt = Math.toRadians(th_tilt);
        Geant4Basic chamberVolume = new G4Trap("r" + (iRegion + 1) + "_s" + (iSector + 1) + "_c" + (iChamber+1),
                ch_dz, -ch_thtilt, Math.toRadians(90.0),
                ch_dy, ch_dx0, ch_dx1, 0.0,
                ch_dy, ch_dx0, ch_dx1, 0.0);

       double  daughter_volume_z_pos =0;
       double  daughter_volume_y_pos =0;

       double ch_dx0_daughter = (iChamber*sector_height/nChambers)*Math.tan(Math.toRadians(th_open/2.))+ dx0_chamber0 ;
       double ch_dx1_daughter = (sector_height/nChambers)*Math.tan(Math.toRadians(th_open/2.))+ch_dx0_daughter;
       double ch_dy_daughter = sector_height/nChambers/2 ;

       for (int i=0; i< chamber_volumes_thickness.length; i++ ){
 
        if(i==0) {daughter_volume_z_pos = chamber_volumes_thickness[i]/2 - (this.chamber_thickness())/2.;
         } else daughter_volume_z_pos += chamber_volumes_thickness[i-1]/2 + chamber_volumes_thickness[i]/2;
            
            daughter_volume_y_pos = -daughter_volume_z_pos *Math.tan(Math.toRadians(th_tilt));
          
         Geant4Basic daughter_volume = new G4Trap("daughter_volume",
                chamber_volumes_thickness[i]/2, -ch_thtilt, Math.toRadians(90.0),
                ch_dy_daughter, ch_dx0_daughter, ch_dx1_daughter, 0.0,
                ch_dy_daughter, ch_dx0_daughter, ch_dx1_daughter, 0.0);
        
        daughter_volume.setName("rg" + (iRegion + 1) + "_s" + (iSector + 1) + "_c" + (iChamber +1) +"_"+chamber_volumes_string[i] );
        daughter_volume.setMother(chamberVolume);
        daughter_volume.setPosition(0.0, daughter_volume_y_pos,daughter_volume_z_pos);

          
        }


        return  chamberVolume;
    }


    public static void main(String[] args) {
        ConstantProvider cp = GeometryFactory.getConstants(DetectorType.DC, 11, "default");
        GEMGeant4Factory factory = new GEMGeant4Factory(cp);
            
        for(Geant4Basic volume : factory.getAllVolumes()) {
            System.out.println(volume.gemcString());
        }
    }
}
