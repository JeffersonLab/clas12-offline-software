package org.jlab.geom.detector.alert.ATOF;

import java.awt.Color;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.base.Sector;
import org.jlab.geom.base.Component;
import org.jlab.geom.visualizer.CLASVisualizer;
/**
 * A Low Energy Recoil Tracker (ALERT)
 * Visualisation main
 * Comment/Uncomment the necessary lines to visualize ATOF or AHDC detectors
 * @author gavalian
 * @author sergeyev
 */
public class CLAS12Vis {
    public static void main(String[] args){
        CLASVisualizer viz = new CLASVisualizer(0,0,1200,900);

        viz.setBackgroundColor(new Color(255,255,255));

        DatabaseConstantProvider  provider = new DatabaseConstantProvider(11,"default");
        
        //provider.loadTable("/geometry/cnd/cndgeom");
        /*
        provider.loadTable("/geometry/dc/dc");
        provider.loadTable("/geometry/dc/region");
        provider.loadTable("/geometry/dc/superlayer");
        provider.loadTable("/geometry/dc/layer");
        provider.loadTable("/geometry/dc/alignment");
        provider.loadTable("/geometry/dc/ministagger");
        */
        
        //MYFactory factory = new MYFactory();
        //MYFactory_DC factory = new MYFactory_DC();
        
        MYFactory_ATOF factory = new MYFactory_ATOF();
        //MYFactory_AHDC1 factory = new MYFactory_AHDC1();
        //MYFactory_AHDCPrismaticCellWire factory = new MYFactory_AHDCPrismaticCellWire();
        //MYFactory_ALERTDCWire factory = new MYFactory_ALERTDCWire();
      
        Detector cnd = factory.createDetectorCLAS(provider);
        //Detector DC = factory.createDetectorCLAS(provider);
        
        viz.add(cnd, Color.pink); // ATOF for ALERT
        //viz.add(DC, Color.cyan); // AHDC for ALERT
        //viz.add(DC, Color.green); // DC from CLAS
        
        
        System.out.println("It's MYFactory_ATOF?");
        //System.out.println("It's MYFactory_AHDC1?");
        //System.out.println("It's MYFactory_ALERTDCWire?");
        System.out.println("It's me!");
        
        viz.setVisible(true);
    }
}
