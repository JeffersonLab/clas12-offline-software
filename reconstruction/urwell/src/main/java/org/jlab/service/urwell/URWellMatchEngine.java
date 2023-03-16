 package org.jlab.service.urwell;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.banks.RawOrderType;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedPad;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.utils.benchmark.ProgressPrintout;

/**
 *
 * @author devita
 */
public class URWellMatchEngine extends ReconstructionEngine {

    private static double TARGET = 0;
    private final static Line3D[][] DCWIRES = new Line3D[36][112];
    private final static Transformation3D[] TOLOCAL = new Transformation3D[6];
    private final static double TILT = 25;

    private final static double MAXDIST = 1;

    private boolean dropHits = false;

    public static Logger LOGGER = Logger.getLogger(URWellEngine.class.getName());

    
    
    public URWellMatchEngine(){
        super("URWellMatch","devita","1.0");
    }

    @Override
    public boolean init() {
        
        if(this.getEngineConfigString("dropHits")!=null)
            dropHits = Boolean.getBoolean(this.getEngineConfigString("dropHits"));
        
        String variationName = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
        this.loadGeometry(variationName);
        
        LOGGER.log(Level.INFO, "--> URWells are ready...");

        return true;
    }
    
    private void loadGeometry(String variation) {
        // Load target
        ConstantProvider providerTG = GeometryFactory.getConstants(DetectorType.TARGET, 11, variation);
        TARGET = providerTG.getDouble("/geometry/target/position",0);
        
        // Load DC geometry
        ConstantProvider provider   = GeometryFactory.getConstants(DetectorType.DC, 11, variation);
        DCGeant4Factory  dcDetector = new DCGeant4Factory(provider, DCGeant4Factory.MINISTAGGERON, false);
        for(int is=0; is<6; is++) {
            TOLOCAL[is] = new Transformation3D(); 
            TOLOCAL[is].rotateZ(-is*Math.toRadians(60));
            TOLOCAL[is].rotateY(-Math.toRadians(TILT));
        }

        for(int il=0; il<36; il++) {
            for(int iw=0; iw<112; iw++) {
                int isl=il/6;
                int ily=il%6;
                Vector3d left  = dcDetector.getWireLeftend(0,isl,ily,iw);
                Vector3d right = dcDetector.getWireRightend(0,isl,ily,iw);
                DCWIRES[il][iw] = new Line3D(left.x,left.y,left.z,right.x,right.y,right.z);
            }
        }
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        
        List<URWellCross> crosses  = this.readURWellCrosses(event);
        List<DCHit>       hits     = this.readDCHits(event);
    
        for(DCHit hit : hits) {
            if(hit.sector()!=0 && hit.layer()<13) { // R1 only
                for(URWellCross cross : crosses) {
                    if(cross.getStatus()==0 && this.areMatched(cross, hit))
                        hit.setMatchStatus(true);
                }
            }
 
        }
                
        if(this.dropHits)
            this.rewriteDCBank(event, hits);
        else
            this.writeMatchBank(event, hits);
        
        return true;
    }

    private List<URWellCross> readURWellCrosses(DataEvent event) {
        
        List<URWellCross> crosses  = new ArrayList<>();
        
        if(event.hasBank("URWELL::crosses")) {
                        
            DataBank bank = event.getBank("URWELL::crosses");
        
            for(int i=0; i<bank.rows(); i++) {
                int    sector = bank.getByte("sector", i);
                double x      = bank.getFloat("x", i);
                double y      = bank.getFloat("y", i);
                double z      = bank.getFloat("z", i);                        
                double energy = bank.getFloat("energy", i);
                double time   = bank.getFloat("time", i);
                int    status = bank.getShort("status", i);
                URWellCross cross = new URWellCross(sector, x, y, z, energy, time, status);
                crosses.add(cross);
            }
        }
        return crosses;
    }   

    private List<DCHit> readDCHits(DataEvent event) {
        
        List<DCHit> hits = new ArrayList<>();
     
        if(event.hasBank("DC::tdc")) {
                        
            DataBank bank = event.getBank("DC::tdc");
            
            for(int j=0; j<bank.rows(); j++) {
                int id        = j+1;
                int sector    = bank.getByte("sector", j);
                int layer     = bank.getByte("layer", j);
                int component = bank.getShort("component", j);
                int order     = bank.getByte("order", j);
                int tdc       = bank.getInt("TDC", j);

                DCHit wire = new DCHit(id, sector, layer, component, order, tdc);
                wire.setLine(DCWIRES[wire.layer()-1][component -1]);
                hits.add(wire);
            }
        }
        return hits;
    } 
    
    private DataEvent rewriteDCBank(DataEvent event, List<DCHit> hits) {

        String name = "DC::tdc";
        event.removeBank(name);
        
        List<DCHit> selectedHits = new ArrayList<>();
        for(DCHit hit : hits) {
            if(hit.isMatched()) selectedHits.add(hit);
        }
        
        if(!selectedHits.isEmpty()) {
            DataBank bank = event.createBank(name, selectedHits.size());
            for(int i=0; i<selectedHits.size(); i++) {

                bank.setByte("sector",     i, (byte)  selectedHits.get(i).sector());
                bank.setByte("layer",      i, (byte)  selectedHits.get(i).layer());
                bank.setShort("component", i, (short) selectedHits.get(i).component());
                bank.setByte("order",      i, (byte)  selectedHits.get(i).order());
                bank.setInt("TDC",         i, selectedHits.get(i).tdc());
            }
            event.appendBank(bank);
        }
        
        return event;
    }

    private DataEvent writeMatchBank(DataEvent event, List<DCHit> hits) {

        String name = "URWELL::match";
        event.removeBank(name);
        
        if(hits.isEmpty()) {
            DataBank bank = event.createBank(name, hits.size());
            for(int i=0; i<hits.size(); i++) {

                if(hits.get(i).isMatched()) 
                    bank.setByte("status", i, (byte) 0);
                else
                    bank.setByte("status", i, (byte) 1);
                
            }
            event.appendBank(bank);
        }
        
        return event;
    }

    private boolean areMatched(URWellCross cross, DCHit hit) {
        if(cross.getSector()!=hit.sector()) return false;
        return this.crossToHitDistance(cross, hit)<MAXDIST;
    }            

    private double crossToHitDistance(URWellCross cross, DCHit hit) {
        Line3D crossLine = new Line3D(cross.point(), cross.point().vectorFrom(0, 0, TARGET).asUnit());
        TOLOCAL[cross.getSector()-1].apply(crossLine);
        Plane3D dcLayer = new Plane3D(0, 0, hit.line().origin().z(), 0, 0, 1);
        Point3D crossProjection = new Point3D();
        int nint = dcLayer.intersection(crossLine, crossProjection);
        if(nint>0) {
            return hit.line().distance(crossProjection).length();
        }
        else {
            return Double.MAX_VALUE;
        }
    }  

    public class DCHit {
        
        private int id = 0;
        private int sector = 0;
        private int layer = 0;
        private int component = 0;
        private int order = 0;
        private int tdc = 0;
        private Line3D line;
        private boolean match=false;
    
        public DCHit(int id, int sector, int layer, int component, int order, int tdc){
            this.id = id;
            this.sector = sector;
            this.layer = layer;
            this.component = component;
            this.order = order;
            this.tdc = tdc;
            if(sector==0 || layer>12)
                this.match = true;
        }

        public void setLine(Line3D line) {
            this.line = line;
        }

        public int id() {
            return this.id;
        }

        public int sector() {
            return this.sector;
        }

        public int layer() {
            return this.layer;
        }

        public int superlayer() {
            return (int) (this.layer-1)/6+1;
        }

        public int component() {
            return this.component;
        }

        public int order() {
            return order;
        }

        public int tdc() {
            return tdc;
        }

        public Line3D line() {
            return line;
        }

        public boolean isMatched() {
            return match;
        }

        public void setMatchStatus(boolean match) {
            this.match = match;
        }

    }
    
    
    public static void main(String[] args) {

        DataGroup dg = new DataGroup(4, 2);
        for(int il=0; il<2; il++) {
            int layer = il+1;
            H2F h3 = new H2F("hiDCL"+layer, "", 100, -100, 100, 56, 0, 112);
            h3.setTitleX("uRWell cross x (mm)");
            h3.setTitleY("DC SL" + layer + " hit wire");
            H2F h3c = new H2F("hiDCCutL"+layer, "", 100, -100, 100, 56, 0, 112);
            h3c.setTitleX("uRWell cross x (mm)");
            h3c.setTitleY("DC SL" + layer + " hit wire");
            H1F h4 = new H1F("hiDCtoUR"+layer, "Distance (cm)", "Counts", 100, 0.0, 10.0);         
            h4.setOptStat(Integer.parseInt("1111")); 
            H1F h5 = new H1F("hiWireL"+layer, "DC SL" + layer + " cluster wire", "Counts", 112, 0, 112);       
            h5.setOptStat(Integer.parseInt("1111")); 
            H1F h5c = new H1F("hiWireCutL"+layer, "DC SL" + layer + " cluster Wire", "Counts", 112, 0, 112);        
            h5c.setOptStat(Integer.parseInt("1111")); 
            h5c.setLineColor(2);
            dg.addDataSet(h3,  il*4 + 0);
            dg.addDataSet(h3c, il*4 + 1);
            dg.addDataSet(h4,  il*4 + 2);
            dg.addDataSet(h5,  il*4 + 3);
            dg.addDataSet(h5c, il*4 + 3);
        }
    
 
            
        URWellEngine engine = new URWellEngine();        
        engine.init();

        URWellMatchEngine matchEngine = new URWellMatchEngine();        
        matchEngine.init();

        HipoDataSource  reader = new HipoDataSource();
        reader.open("/users/devita/urWell_nobgxMrgxExt_00090-0099.hipo");

        ProgressPrintout progress = new ProgressPrintout();
        int counter = -1;
        while(reader.hasEvent()) {

            counter++;

            DataEvent event = reader.getNextEvent();
            engine.dropBanks(event);
            engine.processDataEvent(event);
            matchEngine.processDataEvent(event);

            List<URWellCross> crosses = matchEngine.readURWellCrosses(event);
            List<DCHit>       hits    = matchEngine.readDCHits(event);

            for(DCHit hit : hits) {
                if(hit.sector()!=0 && hit.layer()<13) {
                    dg.getH1F("hiWireL" + hit.superlayer()).fill(hit.component());
                    for(URWellCross cross : crosses) {
                        if(cross.getSector()==hit.sector()) {
                            Point3D local = new Point3D(cross.point());
                            TOLOCAL[cross.getSector()-1].apply(local);
                            dg.getH2F("hiDCL" + hit.superlayer()).fill(local.x(), hit.component());
                            dg.getH1F("hiDCtoUR" + hit.superlayer()).fill(matchEngine.crossToHitDistance(cross, hit));
                            if(matchEngine.areMatched(cross, hit)) 
                                dg.getH2F("hiDCCutL" + hit.superlayer()).fill(local.x(), hit.component());
                        }
                    }
                    if(hit.order()==0) 
                        dg.getH1F("hiWireCutL" + hit.superlayer()).fill(hit.component());
                }
            }
            progress.updateStatus();
        }

        progress.showStatus();
        reader.close();
        
        JFrame frame = new JFrame("URWell Matching");
        frame.setSize(1500,800);
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.draw(dg);
        for(EmbeddedPad pad : canvas.getCanvasPads()) {
            pad.getAxisZ().setLog(true);
        }
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);         
    }
}
