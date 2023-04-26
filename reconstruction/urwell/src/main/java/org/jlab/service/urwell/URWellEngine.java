package org.jlab.service.urwell;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.URWELL.URWellStripFactory;
import org.jlab.geom.prim.Point3D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;
import org.jlab.io.hipo.HipoDataSource;

/**
 *
 * URWell reconstruction engine
 * 
 * @author bondi, devita
 */
public class URWellEngine extends ReconstructionEngine {

    public static Logger LOGGER = Logger.getLogger(URWellEngine.class.getName());

    public static URWellStripFactory factory = new URWellStripFactory();

    public URWellEngine() {
        super("URWell","bondi","1.0");
    }

    @Override
    public boolean init() {

        // init ConstantsManager to read constants from CCDB
        String variationName = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
        DatabaseConstantProvider cp = new DatabaseConstantProvider(11, variationName);
        factory.init(cp);
        // register output banks for drop option        
        this.registerOutputBank("URWELL::hits");
        this.registerOutputBank("URWELL::clusters");
        this.registerOutputBank("URWELL::crosses");

        LOGGER.log(Level.INFO, "--> URWells are ready...");
        return true;
    }




    @Override
    public boolean processDataEvent(DataEvent event) {
        
        List<URWellStrip>     strips = URWellStrip.getStrips(event, factory, this.getConstantsManager());
        List<URWellCluster> clusters = URWellCluster.createClusters(strips);
        List<URWellCross>    crosses = URWellCross.createCrosses(clusters);
        
        this.writeHipoBanks(event, strips, clusters, crosses);
        
        return true;
    }

    
    private void writeHipoBanks(DataEvent de, 
                                List<URWellStrip>     strips, 
                                List<URWellCluster> clusters, 
                                List<URWellCross>    crosses){
	    
        DataBank bankS = de.createBank("URWELL::hits", strips.size());
        for(int h = 0; h < strips.size(); h++){
            bankS.setShort("id",        h, (short) strips.get(h).getId());
            bankS.setByte("sector",     h,  (byte) strips.get(h).getDescriptor().getSector());
            bankS.setByte("layer",      h,  (byte) strips.get(h).getDescriptor().getLayer());
            bankS.setShort("strip",     h, (short) strips.get(h).getDescriptor().getComponent());
            bankS.setFloat("energy",    h, (float) strips.get(h).getEnergy());
            bankS.setFloat("time",      h, (float) strips.get(h).getTime());                
            bankS.setShort("status",    h, (short) strips.get(h).getStatus());
            bankS.setShort("clusterId", h, (short) strips.get(h).getClusterId());
        }
        
        DataBank bankC = de.createBank("URWELL::clusters", clusters.size());        
        for(int c = 0; c < clusters.size(); c++){
            bankS.setShort("id",       c, (short) clusters.get(c).getId());
            bankC.setByte("sector",    c,  (byte) clusters.get(c).get(0).getDescriptor().getSector());
            bankC.setByte("layer",     c,  (byte) clusters.get(c).get(0).getDescriptor().getLayer());
            bankC.setShort("strip",    c, (short) clusters.get(c).getMaxStrip());
            bankC.setFloat("energy",   c, (float) clusters.get(c).getEnergy());
            bankC.setFloat("time",     c, (float) clusters.get(c).getTime());
            bankC.setFloat("xo",       c, (float) clusters.get(c).getLine().origin().x());
            bankC.setFloat("yo",       c, (float) clusters.get(c).getLine().origin().y());
            bankC.setFloat("zo",       c, (float) clusters.get(c).getLine().origin().z());
            bankC.setFloat("xe",       c, (float) clusters.get(c).getLine().end().x());
            bankC.setFloat("ye",       c, (float) clusters.get(c).getLine().end().y());
            bankC.setFloat("ze",       c, (float) clusters.get(c).getLine().end().z());
            bankC.setShort("size",     c, (short) clusters.get(c).size());
            bankC.setShort("status",   c, (short) clusters.get(c).getStatus()); 
        }       
        
        DataBank bankX = de.createBank("URWELL::crosses", crosses.size());        
        for(int c = 0; c < crosses.size(); c++){
            bankX.setShort("id",       c, (short) crosses.get(c).getId());
            bankX.setByte("sector",    c,  (byte) crosses.get(c).getSector());
            bankX.setFloat("energy",   c, (float) crosses.get(c).getEnergy());
            bankX.setFloat("time",     c, (float) crosses.get(c).getTime());
            bankX.setFloat("x",        c, (float) crosses.get(c).point().x());
            bankX.setFloat("y",        c, (float) crosses.get(c).point().y());
            bankX.setFloat("z",        c, (float) crosses.get(c).point().z());
            bankX.setShort("cluster1", c, (short) crosses.get(c).getCluster1()); 
            bankX.setShort("cluster2", c, (short) crosses.get(c).getCluster2()); 
            bankX.setShort("status",   c, (short) crosses.get(c).getStatus()); 
        }       
        de.appendBanks(bankS,bankC,bankX);
    }

    
    public static void fitGauss(H1F histo) {
        double mean  = histo.getMean();
        double amp   = histo.getBinContent(histo.getMaximumBin());
        double rms   = histo.getRMS();
        double sigma = rms/2;
        double min = mean - 3*rms;
        double max = mean + 3*rms;
        
        F1D f1   = new F1D("f1res","[amp]*gaus(x,[mean],[sigma])", min, max);
        f1.setLineColor(2);
        f1.setLineWidth(2);
        f1.setOptStat("1111");
        f1.setParameter(0, amp);
        f1.setParameter(1, mean);
        f1.setParameter(2, sigma);
            
        if(amp>5) {
            f1.setParLimits(0, amp*0.2,   amp*1.2);
            f1.setParLimits(1, mean*0.5,  mean*1.5);
            f1.setParLimits(2, sigma*0.2, sigma*2);
//            System.out.print("1st...");
            DataFitter.fit(f1, histo, "Q");
            mean  = f1.getParameter(1);
            sigma = f1.getParameter(2);
            f1.setParLimits(0, 0, 2*amp);
            f1.setParLimits(1, mean-sigma, mean+sigma);
            f1.setParLimits(2, 0, sigma*2);
            f1.setRange(mean-2.0*sigma,mean+2.0*sigma);
            DataFitter.fit(f1, histo, "Q");
        }
    }    
    
    public static void main (String arg[])  {

        URWellEngine engine = new URWellEngine();
        engine.init();

        String input = "/Users/devita/urwell3d.hipo";

        DataGroup dg = new DataGroup(3, 2);
        String[] axes = {"x", "y"};
        for(int il=0; il<URWellConstants.NLAYER; il++) {
            int layer = il+1;
            H1F h1 = new H1F("hiEnergyL"+layer, "Cluster Energy (eV)", "Counts", 100, 0., 1500.);         
            h1.setOptStat(Integer.parseInt("1111")); 
            H1F h2 = new H1F("hiTimeL"+layer, "Cluster Time (ns)", "Counts", 100, 0., 400.);         
            h2.setOptStat(Integer.parseInt("1111")); 
            H1F h3 = new H1F("hiSpace"+axes[il], "Cross #Delta" + axes[il] + " (mm)", "Counts", 100, -2.0, 2.0);         
            h3.setOptStat(Integer.parseInt("1111")); 
            dg.addDataSet(h1, il*3 + 0);
            dg.addDataSet(h2, il*3 + 1);
            dg.addDataSet(h3, il*3 + 2);
        }

        HipoDataSource  reader = new HipoDataSource();
        reader.open(input);

        while(reader.hasEvent()) {
            DataEvent event = reader.getNextEvent();

            engine.processDataEvent(event);
            
            double xtrue = 0;
            double ytrue = 0;
            double ztrue = 0;
            Point3D mc = new Point3D();
            if(event.hasBank("MC::True")) {
                DataBank bankMC = event.getBank("MC::True");
                for(int i=0; i<bankMC.rows(); i++) {
                    int detector = bankMC.getByte("detector",i);  
                    if(detector==DetectorType.URWELL.getDetectorId()) {
                        xtrue = bankMC.getFloat("avgX",i);
                        ytrue = bankMC.getFloat("avgY",i);
                        ztrue = bankMC.getFloat("avgZ",i);
                        mc = new Point3D(xtrue, ytrue, ztrue);
                        break;
                    }
                }
            }
            mc.rotateY(-Math.toRadians(25.0));
            System.out.println(mc);
            if(event.hasBank("URWELL::clusters")) {
                DataBank bankC = event.getBank("URWELL::clusters");
                bankC.show();
                for(int i=0; i<bankC.rows(); i++) {
                    int    layer  = bankC.getByte("layer", i);
                    double energy = bankC.getFloat("energy", i);
                    double time   = bankC.getFloat("time", i);
                    dg.getH1F("hiEnergyL"+layer).fill(energy);
                    dg.getH1F("hiTimeL"+layer).fill(time);
                }
            }
            if(event.hasBank("URWELL::crosses")) {
                DataBank bankX = event.getBank("URWELL::crosses");
                bankX.show();
                for(int i=0; i<bankX.rows(); i++) {
                    double x = bankX.getFloat("x", i);
                    double y = bankX.getFloat("y", i);
                    double z = bankX.getFloat("z", i);
                    Point3D rec = new Point3D(x*10, y*10, z*10);
                    rec.rotateY(-Math.toRadians(25));
                    dg.getH1F("hiSpace"+axes[0]).fill(rec.x()-mc.x());
                    dg.getH1F("hiSpace"+axes[1]).fill(rec.y()-mc.y());
                }
            }

        }
        reader.close();
        
        for(int i=0; i<URWellConstants.NLAYER; i++) {
           URWellEngine.fitGauss(dg.getH1F("hiTimeL"+(i+1)));
           URWellEngine.fitGauss(dg.getH1F("hiSpace"+axes[i]));
        }
        JFrame frame = new JFrame("URWell Reconstruction");
        frame.setSize(800,800);
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.draw(dg);
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);     

    }
}
