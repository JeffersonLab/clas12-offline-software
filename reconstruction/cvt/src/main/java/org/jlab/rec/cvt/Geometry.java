package org.jlab.rec.cvt;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.CCDBConstantsLoader;
import org.jlab.rec.cvt.measurement.Measurements;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 */
public class Geometry {
    
    private SVTGeometry       svtGeometry  = null;
    private BMTGeometry       bmtGeometry  = null;
    private CTOFGeant4Factory ctofGeometry = null;
    private Detector          cndGeometry  = null;
    private List<Surface>     cvtSurfaces  = null;
    private List<Surface>   outerSurfaces  = null;
    private double zTarget = 0;  
    private double zLength = 0;  

    private static boolean LOADED;
    
    // private constructor for a singleton
    private Geometry() {
    }
    
    // singleton
    private static Geometry instance = null;
    
    /**
     * public access to the singleton
     * 
     * @return the cvt geometry singleton
     */
    public static Geometry getInstance() {
            if (instance == null) {
                    instance = new Geometry();
            }
            return instance;
    }
    
    public synchronized static void initialize(String variation, int run, IndexedTable svtLorentz, IndexedTable bmtVoltage) {
        if(!LOADED) {
            Geometry.getInstance().load(variation, run, svtLorentz, bmtVoltage);
            LOADED = true;
        }
    }
 
    private synchronized void load(String variation, int run, IndexedTable svtLorentz, IndexedTable bmtVoltage) {
        
        // Load target
        ConstantProvider providerTG = GeometryFactory.getConstants(DetectorType.TARGET, run, variation);
        this.zTarget = providerTG.getDouble("/geometry/target/position",0)*10;
        this.zLength = providerTG.getDouble("/geometry/target/length",0)*10;
                         
        ConstantProvider providerCTOF = GeometryFactory.getConstants(DetectorType.CTOF, run, variation);
        ctofGeometry = new CTOFGeant4Factory(providerCTOF);        
        cndGeometry  =  GeometryFactory.getDetector(DetectorType.CND, run, variation);
        
        CCDBConstantsLoader.Load(new DatabaseConstantProvider(run, variation));
        DatabaseConstantProvider cp = new DatabaseConstantProvider(run, variation);
        SVTStripFactory svtFac = new SVTStripFactory(cp, true);
        svtGeometry  = new SVTGeometry(svtFac, svtLorentz);
        bmtGeometry  = new BMTGeometry(bmtVoltage);
        
        cvtSurfaces = new ArrayList<>();
        cvtSurfaces.addAll(svtGeometry.getSurfaces());
        cvtSurfaces.addAll(bmtGeometry.getSurfaces());
        
        outerSurfaces = Measurements.getOuters();
    }
    
    public double getZoffset() {
        return zTarget;
    }

    public double getZlength() {
        return zLength;
    }
    
    public SVTGeometry getSVT() {
        return svtGeometry;
    }
    

    public BMTGeometry getBMT() {
        return bmtGeometry;
    }

    public CTOFGeant4Factory getCTOF() {
        return ctofGeometry;
    }

    public Detector getCND() {
        return cndGeometry;
    }

    public List<Surface> getCVTSurfaces() {
        return cvtSurfaces;
    }

    public List<Surface> geOuterSurfaces() {
        return outerSurfaces;
    }


}
