package org.jlab.rec.cvt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.logging.DefaultLogger;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.bmt.CCDBConstantsLoader;
import org.jlab.rec.cvt.measurement.Measurements;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.utils.options.OptionParser;

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

    /**
     * Get the list of SVT strips for geometry defined by the selected run and variation
     * @param run
     * @param region
     * @param variation
     * @param lorentz
     * @return list of strips as Line3D
     */
    public static List<Line3D> getSVT(int run, int region, String variation, IndexedTable lorentz) {
        
        DatabaseConstantProvider cp = new DatabaseConstantProvider(run, variation);
        SVTStripFactory svtFac = new SVTStripFactory(cp, true);
        SVTGeometry geometry  = new SVTGeometry(svtFac, lorentz);

        int lmin= 0;
        int lmax = SVTGeometry.NLAYERS;
        if(region>0) {
            lmin = (region-1)*2;
            lmax = region*2;
        }
        List<Line3D> svt = new ArrayList<>();        
        for(int il=lmin; il<lmax; il++) {
            for(int is=0; is<SVTGeometry.NSECTORS[il]; is++) {
                for(int ic=0; ic<SVTGeometry.NSTRIPS; ic++) {
                    Line3D strip = geometry.getStrip(il+1, is+1, ic+1);
                    if(strip.origin().z()>strip.end().z())
                        svt.add(new Line3D(strip.end(), strip.origin()));
                    else
                        svt.add(strip);
                }
            }
        }
        return svt;
    }
    
    /**
     * Get the list of BMT strips for the selected BMT tile type and the 
     * geometry defined by the selected run and variation
     * @param type
     * @param run
     * @param variation
     * @param voltage
     * @return list of strips as Line3D, corresponding to the actual strip for 
     * Z tiles and for lines connecting the upstream and downstream circular 
     * edges of C tiles at the same local phi
     */
    public static List<Line3D> getBMT(BMTType type, int run, String variation, IndexedTable voltage) {
        
        List<Line3D> bmt = new ArrayList<>();
        
        CCDBConstantsLoader.Load(new DatabaseConstantProvider(run, variation));
        BMTGeometry bmtGeometry  = new BMTGeometry(voltage);
        
        for(int ir=0; ir<BMTGeometry.NLAYERS/2; ir++) {
            int layer = bmtGeometry.getLayer(ir+1, type);
            for(int is=0; is<BMTGeometry.NSECTORS; is++) {
                if(type==BMTType.Z) {
                    for(int ic=0; ic<bmtGeometry.getNStrips(layer); ic++) {
                        bmt.add(bmtGeometry.getZstrip(ir+1, is+1, ic+1));
                    }
                }
                else {
                    Arc3D origin = bmtGeometry.getCstrip(ir+1, is+1, 1);
                    Arc3D end    = bmtGeometry.getCstrip(ir+1, is+1, bmtGeometry.getNStrips(layer));
                    for(int ic=0; ic<1000; ic++) {
                        double t = (double) ic*origin.theta()/1000;
                        bmt.add(new Line3D(origin.point(t), end.point(t)));
                    }                    
                }
            }
        }
        return bmt;
    }
    
    /**
     * Computes the average offset of the origins of the two strip lists
     * @param geo1
     * @param geo2
     * @return the offset (1-2) as a Point3D
     */
    public static Point3D getOffset(List<Line3D> geo1, List<Line3D> geo2) {
        Point3D center = new Point3D(0,0,0);
        if(geo1.size()!=geo2.size())
            return center;
        for(int i=0; i<geo1.size(); i++) {
            Point3D origin1 = geo1.get(i).origin();
            Point3D origin2 = geo2.get(i).origin();
            center.translateXYZ((origin1.x()-origin2.x())/geo1.size(), (origin1.y()-origin2.y())/geo1.size(), (origin1.z()-origin2.z())/geo1.size());
        }
        return center;
    }
    
    /**
     * Draws representative plots of the differences between the two lists of 
     * strips, compensating for a global offset
     * @param geo1
     * @param geo2
     * @param offset applied to the second geometry
     * @return a DataGroup with the relevant plots
     */
    public static DataGroup compareStrips(List<Line3D> geo1, List<Line3D> geo2, Point3D offset) {
        DataGroup dg = new DataGroup(4,2); 
        String[] xyz = {"x", "y", "z", "xy1", "xy2"};
        String[] oe = {"upstream", "downstream"};
        for(int coord=0; coord<xyz.length; coord++) {
            for(int side=0; side<oe.length; side++) {
                GraphErrors delta = new GraphErrors(oe[side]+xyz[coord]);
                delta.setTitleX("#phi (rad)");
                delta.setTitleY(oe[side] + " #Delta" + xyz[coord] + " (mm)");
                delta.setMarkerSize(2);
                delta.setMarkerColor(4);
                if(coord>2) {
                    delta.setMarkerColor(coord-2);
                    delta.setTitleX(oe[side] + " x (mm)");
                    delta.setTitleY(oe[side] + " y (mm)");
                }
                dg.addDataSet(delta, Math.min(3, coord)+side*4);
            }
        }
        for(int i=0; i<Math.min(geo1.size(), geo2.size()); i++) {
            Line3D strip1 = geo1.get(i);
            Line3D strip2 = geo2.get(i);
            strip2.translateXYZ(offset.x(), offset.y(), offset.z());
            dg.getGraph(oe[0]+xyz[0]).addPoint(strip1.origin().toVector3D().phi(), strip1.origin().x()-strip2.origin().x(), 0, 0);
            dg.getGraph(oe[0]+xyz[1]).addPoint(strip1.origin().toVector3D().phi(), strip1.origin().y()-strip2.origin().y(), 0, 0);
            dg.getGraph(oe[0]+xyz[2]).addPoint(strip1.origin().toVector3D().phi(), strip1.origin().z()-strip2.origin().z(), 0, 0);
            dg.getGraph(oe[0]+xyz[3]).addPoint(strip1.origin().x(), strip1.origin().y(), 0, 0);
            dg.getGraph(oe[0]+xyz[4]).addPoint(strip2.origin().x(), strip2.origin().y(), 0, 0);
            dg.getGraph(oe[1]+xyz[0]).addPoint(strip1.end().toVector3D().phi(), strip1.end().x()-strip2.end().x(), 0, 0);
            dg.getGraph(oe[1]+xyz[1]).addPoint(strip1.end().toVector3D().phi(), strip1.end().y()-strip2.end().y(), 0, 0);
            dg.getGraph(oe[1]+xyz[2]).addPoint(strip1.end().toVector3D().phi(), strip1.end().z()-strip2.end().z(), 0, 0);
            dg.getGraph(oe[1]+xyz[3]).addPoint(strip1.end().x(), strip1.end().y(), 0, 0);
            dg.getGraph(oe[1]+xyz[4]).addPoint(strip2.end().x(), strip2.end().y(), 0, 0);
        }
        return dg;
    }

    /**
     * Compares the CVT geometry for two geometry variations selected from 
     * command line, compensating for the average offset between the SVT-R1 
     * strip upstream endpoints
     * @param args
     */
    public static void main(String[] args) {
        DefaultLogger.debug();
        OptionParser parser = new OptionParser("Compare CVT geometries from two variations");
        parser.setRequiresInputList(false);
        parser.addOption("-var1",  "default",        "geometry variation 1");
        parser.addOption("-var2",  "rgb_spring2019", "geometry variation 2");
        parser.parse(args);
        
        int run = 11;
        String var1 = parser.getOption("-var1").stringValue();
        String var2 = parser.getOption("-var2").stringValue();
        
        ConstantsManager ccdb = new ConstantsManager();
        ccdb.init(Arrays.asList("/calibration/svt/lorentz_angle", "/calibration/mvt/bmt_voltage"));
        ccdb.setVariation(var1);
        IndexedTable svtLorentz = ccdb.getConstants(run, "/calibration/svt/lorentz_angle");
        IndexedTable bmtVoltage = ccdb.getConstants(run, "/calibration/mvt/bmt_voltage");
        
        GStyle.getH1FAttributes().setOptStat("1111");
        GStyle.getAxisAttributesX().setTitleFontSize(24);
        GStyle.getAxisAttributesX().setLabelFontSize(18);
        GStyle.getAxisAttributesY().setTitleFontSize(24);
        GStyle.getAxisAttributesY().setLabelFontSize(18);
        GStyle.getAxisAttributesZ().setLabelFontSize(14);
        GStyle.getAxisAttributesX().setLabelFontName("Arial");
        GStyle.getAxisAttributesY().setLabelFontName("Arial");
        GStyle.getAxisAttributesZ().setLabelFontName("Arial");
        GStyle.getAxisAttributesX().setTitleFontName("Arial");
        GStyle.getAxisAttributesY().setTitleFontName("Arial");
        GStyle.getAxisAttributesZ().setTitleFontName("Arial");
        GStyle.setGraphicsFrameLineWidth(1);
        GStyle.getH1FAttributes().setLineWidth(2);

        EmbeddedCanvasTabbed canvas = new EmbeddedCanvasTabbed("SVT-R1", "SVT-R2", "SVT-R3", "BMT-C", "BMT-Z");
        Point3D offset = new Point3D(0,0,0);
        for(int i=0; i<SVTGeometry.NREGIONS; i++) {
            List<Line3D> svt1 = Geometry.getSVT(run, i+1, var1, svtLorentz);
            List<Line3D> svt2 = Geometry.getSVT(run, i+1, var2, svtLorentz);
            if(i==0) offset = Geometry.getOffset(svt1, svt2);
            canvas.getCanvas("SVT-R" + (i+1)).draw(Geometry.compareStrips(svt1, svt2, offset));
        }
        canvas.getCanvas("BMT-Z").draw(Geometry.compareStrips(Geometry.getBMT(BMTType.Z, run, var1, bmtVoltage), Geometry.getBMT(BMTType.Z, run, var2, bmtVoltage), offset));
        canvas.getCanvas("BMT-C").draw(Geometry.compareStrips(Geometry.getBMT(BMTType.C, run, var1, bmtVoltage), Geometry.getBMT(BMTType.C, run, var2, bmtVoltage), offset));
        
        JFrame frame = new JFrame("CVT Geometry");
        frame.setSize(1500,800);
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);   
    }
}
