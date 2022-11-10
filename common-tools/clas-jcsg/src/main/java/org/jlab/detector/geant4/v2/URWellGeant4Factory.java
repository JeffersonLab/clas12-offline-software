package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Line3D;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author bondi
 */

///////////////////////////////////////////////////
public final class URWellGeant4Factory extends Geant4Factory {

//    GEMdatabase dbref = GEMdatabase.getInstance();

  //  private final HashMap<String, String> properties = new HashMap<>();
    private final int    nSectors  = 6;
    private final int    nLayers   = 2;
    private final int    nChambers = 3;

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
    private final double w2tgt = dist_2_tgt/Math.cos(Math.toRadians(th_tilt-th_min));
    private final double y_min = w2tgt*Math.sin(Math.toRadians(th_min)); // distance from the base chamber1 and beamline
    private final double z_min = w2tgt*Math.cos(Math.toRadians(th_min));    

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
  
    // strip parameters
    private int[]    nStrips     = {542, 628, 714};
    private double   pitch       = 0.1;
    private double[] stereoAngle = {-10, 10};

    private IndexedList<Line3D> stripLines = new IndexedList<>(3);
    
    public URWellGeant4Factory(ConstantProvider provider) {
        motherVolume = new G4World("fc");

        for (int iregion = 0; iregion < 1; iregion++) {
            for (int isector = 0; isector < nSectors; isector++) {
                Geant4Basic regionVolume = createRegion(isector, iregion);
                regionVolume.setMother(motherVolume);
//                G4Trap chamber = (G4Trap) regionVolume.getChildren().get(0);
//                chamber.getVertex(isector)
            }
        }
    }

    public int getNStrip(int iChamber) {
        return nStrips[iChamber];
    }
    
    public double getChamberThickness() {
        double chamber_t = 0;
        for (int i = 0; i < chamber_volumes_thickness.length; i++) {
            chamber_t += chamber_volumes_thickness[i];
        }
        return chamber_t;
    }

    public Geant4Basic createRegion(int isector, int iregion) {

       
       double reg_dz = (this.getChamberThickness())/2. + z_enlargement ;
       double reg_dy = sector_height/2 + y_enlargement ;
       double reg_dx0 = dx0_chamber0 + x_enlargement ;
       double reg_dx1 = (reg_dy*2)*Math.tan(Math.toRadians(th_open/2))+reg_dx0 ;  
       double reg_thtilt = Math.toRadians(th_tilt);

        // gravity center coordinate in CLAS12 frame 
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
            double y_chamber = (2 * ich + 1) * (sector_height / nChambers / 2 + 0.05);
            //double y_chamber = (2*ich+1)*(sector_height/nChambers/2);
            Geant4Basic chamberVolume = this.createChamber(ich, iregion, isector);
            chamberVolume.setName("rg" + (iregion + 1) + "_s" + (isector + 1) + "_c" + (ich + 1));
            chamberVolume.setMother(sectorVolume);
            chamberVolume.translate(0.0, y_chamber - sector_height / 2, 0.);
            chamberVolume.setId(isector + 1, iregion + 1, ich + 1, 0);
        }

        return sectorVolume;
    }

    ///////////////////////////////////////////////////
    
    public Geant4Basic createChamber(int iChamber, int iRegion, int iSector) {
        
  
        double ch_dz = (this.getChamberThickness())/2. + z_enlargement/2;
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

       for (int i = 0; i < chamber_volumes_thickness.length; i++) {

            if (i == 0) {
                daughter_volume_z_pos = chamber_volumes_thickness[i] / 2 - (this.getChamberThickness()) / 2.;
            } else {
                daughter_volume_z_pos += chamber_volumes_thickness[i - 1] / 2 + chamber_volumes_thickness[i] / 2;
            }

            daughter_volume_y_pos = -daughter_volume_z_pos * Math.tan(Math.toRadians(th_tilt));

            Geant4Basic daughter_volume = new G4Trap("daughter_volume",
                    chamber_volumes_thickness[i] / 2, -ch_thtilt, Math.toRadians(90.0),
                    ch_dy_daughter, ch_dx0_daughter, ch_dx1_daughter, 0.0,
                    ch_dy_daughter, ch_dx0_daughter, ch_dx1_daughter, 0.0);

            daughter_volume.setName("rg" + (iRegion + 1) + "_s" + (iSector + 1) + "_c" + (iChamber + 1) + "_" + chamber_volumes_string[i]);
            daughter_volume.setMother(chamberVolume);
            daughter_volume.setPosition(0.0, daughter_volume_y_pos, daughter_volume_z_pos);

        }


        return  chamberVolume;
    }

//    private Line3D getStrip(int sector, int layer, int strip) {
//        
//
//	//strip straight line -> y = mx +c;
//	double m = Math.tan(Math.toRadians(stereoAngle[layer-1]));
//	double c = strip*pitch/Math.cos(Math.toRadians(stereoAngle[layer-1]));
//
//   // Trapezoid coordinates
//	G4ThreeVector A = {uRwellc.Xhalf_base, -uRwellc.Yhalf, uRwellc.Zhalf};
//	G4ThreeVector B =  {-uRwellc.Xhalf_base, -uRwellc.Yhalf, uRwellc.Zhalf};
//	G4ThreeVector C = {uRwellc.Xhalf_Largebase, uRwellc.Yhalf, uRwellc.Zhalf};
//	G4ThreeVector D =  {-uRwellc.Xhalf_Largebase, uRwellc.Yhalf, uRwellc.Zhalf};
//
//
//	// C-------------D //
//    //  -------------  //
//	//   -----------   //
//	//    A-------B   //
//	// Intersection points between strip straight line and Trapezoid straight lines
//
//         G4ThreeVector AB_strip = intersectionPoint(m,c,A,B);
//         G4ThreeVector BD_strip = intersectionPoint(m,c,B,D);
//         G4ThreeVector CD_strip = intersectionPoint(m,c,C,D);
//         G4ThreeVector AC_strip = intersectionPoint(m,c,A,C);
//
//         vector< G4ThreeVector> strip_points ; // intersection point between strip and the trapezoid sides;
//
//         // geometrical characteristic
//         double length_strip=0;
//         double lenght_strip_temp=0;
//         G4ThreeVector first_point;
//         G4ThreeVector second_point;
//
//   // check if the intersection point is on the segment defined by two points (i.e A and B)
//
//         if(uRwellc.find_strip_kind()=="strip_u"){
//             if(pointOnsegment(AC_strip, A, C)) {
//          	   first_point=AC_strip;
//
//                 if(pointOnsegment(BD_strip, B, D)) second_point = BD_strip;
//                 if(pointOnsegment(CD_strip, C, D)) second_point = CD_strip;
//
//             }else if(pointOnsegment(AB_strip, A, B)){
//          	   first_point=AB_strip;
//
//             	 if(pointOnsegment(BD_strip, B, D)) second_point = BD_strip;
//                 if(pointOnsegment(CD_strip, C, D)) second_point = CD_strip;
//
//             }else{
//          	   return false;
//             }
//         }
//
//
//
//         if(uRwellc.find_strip_kind()=="strip_v"){
//
//             if(pointOnsegment(BD_strip, B, D)){
//          	   first_point=BD_strip;
//
//                 if(pointOnsegment(AC_strip, A, C)) second_point = AC_strip;
//                 if(pointOnsegment(CD_strip, C, D)) second_point = CD_strip;
//             }else if (pointOnsegment(AB_strip, A, B)){
//            	   first_point=AB_strip;
//            	   if(pointOnsegment(AC_strip, A, C)) second_point = AC_strip;
//                   if(pointOnsegment(CD_strip, C, D)) second_point = CD_strip;
//             }else{
//          	   return false;
//             }
//
//         }
//
//         length_strip = cal_length(first_point, second_point);
//
//      strip_length = length_strip;
//      strip_endpoint1 = first_point;
//      strip_endpoint2 = second_point;
//
//      G4ThreeVector strip_endpoint1_stripFrame = change_of_coordinates(strip_endpoint1, uRwellc );
//      G4ThreeVector strip_endpoint2_stripFrame = change_of_coordinates(strip_endpoint2, uRwellc );
//
//      strip_y = strip_endpoint1_stripFrame.y();
//      strip_x = (strip_endpoint1_stripFrame.x() + strip_endpoint2_stripFrame.x())/2;
//
//      return true;
//}

    public static void main(String[] args) {
        ConstantProvider cp = GeometryFactory.getConstants(DetectorType.DC, 11, "default");
        URWellGeant4Factory factory = new URWellGeant4Factory(cp);
            
        for(Geant4Basic volume : factory.getAllVolumes()) {
            System.out.println(volume.gemcString());
        }
    }
}
