package org.jlab.rec.dc.trajectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.jlab.detector.base.DetectorLayer;
import org.jlab.detector.base.DetectorType;

import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.geom.RICH.RICHGeoFactory;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.Constants;

import java.io.PrintWriter;
import java.util.logging.Logger;
import org.jlab.geom.detector.ec.ECLayer;
import org.jlab.geom.detector.ec.ECSuperlayer;
import org.jlab.geom.detector.fmt.FMTLayer;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Trap3D;
import org.jlab.geom.prim.Triangle3D;

/**
 * A class to load the geometry constants used in the DC reconstruction. The
 * coordinate system used in the Tilted Sector coordinate system.
 *
 * @author ziegler, devita
 *
 */
public class TrajectorySurfaces {

    public static Logger LOGGER = Logger.getLogger(TrajectorySurfaces.class.getName());

    private List<ArrayList<Surface>> detectorPlanes = new ArrayList<>();

    public List<ArrayList<Surface>> getDetectorPlanes() {
        return detectorPlanes;
    }

    public void setDetectorPlanes(List<ArrayList<Surface>> planes) {
        detectorPlanes = planes;
    }

    public void loadSurface(double targetPosition, double targetLength, DCGeant4Factory dcDetector,
            FTOFGeant4Factory ftofDetector, Detector ecalDetector, Detector fmtDetector, RICHGeoFactory richDetector) {
        // creating Boundaries for MS
        Constants.getInstance().Z[0]= targetPosition;
        Constants.getInstance().Z[1]= dcDetector.getWireMidpoint(0, 0, 0, 0).z;
        Constants.getInstance().Z[2]= dcDetector.getWireMidpoint(0, 0, 5, 0).z;
        Constants.getInstance().Z[3]= dcDetector.getWireMidpoint(0, 1, 0, 0).z;
        Constants.getInstance().Z[4]= dcDetector.getWireMidpoint(0, 1, 5, 0).z;
        Constants.getInstance().Z[5]= dcDetector.getWireMidpoint(0, 2, 0, 0).z;
        Constants.getInstance().Z[6]= dcDetector.getWireMidpoint(0, 2, 5, 0).z;
        Constants.getInstance().Z[7]= dcDetector.getWireMidpoint(0, 3, 0, 0).z;
        Constants.getInstance().Z[8]= dcDetector.getWireMidpoint(0, 3, 5, 0).z;
        Constants.getInstance().Z[9]= dcDetector.getWireMidpoint(0, 4, 0, 0).z;
        Constants.getInstance().Z[10]= dcDetector.getWireMidpoint(0, 4, 5, 0).z;
        Constants.getInstance().Z[11]= dcDetector.getWireMidpoint(0, 5, 0, 0).z;
        Constants.getInstance().Z[12]= dcDetector.getWireMidpoint(0, 5, 5, 0).z;
        //DcDetector.getWireMidpoint(this.get_Sector()-1, this.get_Superlayer()-1, this.get_Layer()-1, this.get_Wire()-1).z;
        
        double d = 0;
        Vector3D n,P;
        for(int isector =0; isector<6; isector++) {
            
            int sector = isector+1;
            
            this.detectorPlanes.add(new ArrayList<>());

            // Add target center and downstream wall
            this.detectorPlanes.get(isector).add(new Surface(DetectorType.TARGET, sector, DetectorLayer.TARGET_DOWNSTREAM, new Plane3D(0, 0, targetPosition+targetLength/2, 0, 0, 1)));
            this.detectorPlanes.get(isector).add(new Surface(DetectorType.TARGET, sector, DetectorLayer.TARGET_CENTER, new Plane3D(0, 0, targetPosition, 0, 0, 1)));

            // Add FMT layers
            for (int ilayer=0; ilayer<6; ++ilayer) {
                FMTLayer fmtLayer = (FMTLayer) fmtDetector.getSector(0).getSuperlayer(0).getLayer(ilayer);
                this.detectorPlanes.get(isector).add(new Surface(DetectorType.FMT, sector, ilayer+1, fmtLayer.getTrajectorySurface(), 0));
            }

            // Add DC
            for(int isuperlayer =0; isuperlayer<6; isuperlayer++) {
                for(int ilayer =5; ilayer<6; ilayer++) { // include only layer 6
                    int layer = isuperlayer*6+ilayer+1;
                    this.detectorPlanes.get(isector).add(new Surface(DetectorType.DC, sector, layer, dcDetector.getTrajectorySurface(isector, isuperlayer, ilayer), dcDetector.getCellSize(isuperlayer)));
                }
            }
            
            // Add FTOF
            int[] ftofLayers = {DetectorLayer.FTOF2, DetectorLayer.FTOF1B, DetectorLayer.FTOF1A};
            for(int i=0; i<ftofLayers.length; i++) {
                int layer = ftofLayers[i];
                this.detectorPlanes.get(isector).add(new Surface(DetectorType.FTOF, sector, layer, ftofDetector.getTrajectorySurface(sector, layer), ftofDetector.getThickness(sector, layer, 1)));
            }
            
            // Add LTCC
            n = ftofDetector.getMidPlane(sector, DetectorLayer.FTOF1B).normal();
            this.detectorPlanes.get(isector).add(new Surface(DetectorType.LTCC, 1, Constants.LTCCPLANE, -n.x(), -n.y(), -n.z())); 
            
            // Add ECAL
            int[] ecalLayers   = {DetectorLayer.PCAL_Z+1, DetectorLayer.EC_INNER_Z+1, DetectorLayer.EC_OUTER_Z+1};
            int[] ecalLayerIds = {DetectorLayer.PCAL_U, DetectorLayer.EC_INNER_U, DetectorLayer.EC_OUTER_U};
            for(int i=0; i<ecalLayers.length; i++) {
                int layer = ecalLayers[i];
                int id    = ecalLayerIds[i];
                ECSuperlayer ecalSuperlayer = (ECSuperlayer) ecalDetector.getSector(isector).getSuperlayer(i);
                ECLayer      ecalLayer      = (ECLayer) ecalSuperlayer.getLayer(layer);
                this.detectorPlanes.get(isector).add(new Surface(DetectorType.ECAL, sector, id, (Triangle3D) ecalLayer.getTrajectorySurface(), ecalSuperlayer.getThickness()));
            }

            // Add RICH
            int[] richLayers = {DetectorLayer.RICH_MAPMT, DetectorLayer.RICH_AEROGEL_B1, DetectorLayer.RICH_AEROGEL_B2, DetectorLayer.RICH_AEROGEL_L1};
            for(int i=0; i<richLayers.length; i++) {
                Trap3D surf = richDetector.get_TrajectorySurface(sector, richLayers[i]);
                double thick = richDetector.get_TrajectoryThickness(sector,richLayers[i]);
                if(surf!=null) {
                    this.detectorPlanes.get(isector).add(new Surface(DetectorType.RICH, sector, richLayers[i], surf, thick));
                }
            }

        }
    }
    
    public Surface getSurface(int sector, DetectorType type, int layer) {
        for(Surface surface : this.detectorPlanes.get(sector-1)) {
            if(surface.getDetectorType()==type && surface.getDetectorLayer()==layer) {
                return surface;
            }
        }
        return null;
    }

    public List<Surface> getSurfaces(int sector, DetectorType type) {
        List<Surface> surfaces = new ArrayList<>();
        for(Surface surface : this.detectorPlanes.get(sector-1)) {
            if(surface.getDetectorType()==type) {
                surfaces.add(surface);
            }
        }
        return surfaces;
    }

    public void checkDCGeometry(DCGeant4Factory dcDetector) throws FileNotFoundException {
        int is = 0;
        PrintWriter pw = new PrintWriter(new File("/Users/ziegler/WireEndPoints.txt"));

        pw.printf("superlayer"+"   "+"layer"+"   "+"wire"+"   "+"xL"+"   "+"yL"+"   "+
                            "xR"+"   "+"yR"+"   "+"z"
                            );
        for(int isup =0; isup<6; isup++) {
            for(int il =5; il<6; il++) {
                for(int ic =0; ic<112; ic++) { // include only layer 6
                    double z = dcDetector.getWireMidpoint(is, isup, il, ic).z;
                    double xL = dcDetector.getWireLeftend(is, isup, il, ic).x;
                    double xR = dcDetector.getWireRightend(is, isup, il, ic).x;
                    double yL = dcDetector.getWireLeftend(is, isup, il, ic).y;
                    double yR = dcDetector.getWireRightend(is, isup, il, ic).y;
                    pw.printf("%d\t %d\t %d\t %.1f\t %.1f\t %.1f\t %.1f\t %.1f\t\n", (isup+1),(il+1),(ic+1),xL,yL,xR,yR,z
                            );
                }
            }
        }
    }
   
}
