package org.jlab.service.raster;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.utils.groups.IndexedTable;

/*
 * Raster reconstruction engine:
 * converts the ADC values recorded for the raster signals 
 * into XY beam positions
 * 
 * @author devita, pilleux
 */

public class RasterEngine extends ReconstructionEngine {

    private final double udfPos = -999;
    private final int    xComponent = 0;
    private final int    yComponent = 1;
    
    public static final Logger LOGGER = Logger.getLogger(RasterEngine.class.getName());

    public RasterEngine() {
        super("RasterEngine","devita","1.0");
    }

    @Override
    public boolean init() {
        
        // register list of CCDB tables the engine will access
        List<String> tableNames = new ArrayList<>();
        tableNames.add("/calibration/raster/adc_to_position");
        this.requireConstants(tableNames);
        
        //remove raster bank in case it existed previously
        this.registerOutputBank("RASTER::position");
        
        System.out.println("["+this.getName()+"] --> raster is ready....");
        return true;
    }

    
    @Override
    public boolean processDataEvent(DataEvent event) {
        
        //For testing : Write fake ADC bank
        mcToRasterBank(event);
        
        // Read run number from RUN::config bank
        int run=-1;
        if (event.hasBank("RUN::config")) {
            run = event.getBank("RUN::config").getInt("run",0);
        }
        if (run<=0) {
            LOGGER.log(Level.WARNING,"RasterEngine:  found no run number, CCDB constants not loaded, skipping event.");
            return false;
        }        
        
        // check if input bank exist, otherwise do nothing
        if(!event.hasBank("RASTER::adc")) return true;
        
        // check if input bank has two rows, otherwise give warning
        DataBank adcBank = event.getBank("RASTER::adc");
        if(adcBank.rows()!=2) {
            LOGGER.log(Level.WARNING,"RasterEngine:  RASTER::adc bank has incorrect number of rows, skipping event.");
            return false;
        }
        
        // get calibration table
        double xpos = udfPos;
        double ypos = udfPos;
        IndexedTable adc2position = this.getConstantsManager().getConstants(run, "/calibration/raster/adc_to_position");
        for(int i=0; i<adcBank.rows(); i++) {
            int component = adcBank.getShort("component", i);
            int adc       = adcBank.getInt("ADC", i);
            if(component == xComponent) xpos = this.convertADC(adc2position, component, adc);
            if(component == yComponent) ypos = this.convertADC(adc2position, component, adc);
        }
        
        // check that both x and y are now defined
        if(xpos == udfPos || ypos == udfPos) {
            LOGGER.log(Level.WARNING,"RasterEngine:  missing entry in RASTER::adc bank, skipping event.");
            return false;            
        }
            
        DataBank outputBank = event.createBank("RASTER::position", 1);
        outputBank.setFloat("x", 0, (float) xpos);
        outputBank.setFloat("y", 0, (float) ypos);
        event.appendBank(outputBank);
        
        return true;
    }

    private double convertADC(IndexedTable adc2pos, int component, int ADC) {
        double pos = adc2pos.getDoubleValue("p0", 0, 0, component)+
                     adc2pos.getDoubleValue("p1", 0, 0, component)*ADC;
        return pos;
    }
    
    public void mcToRasterBank(DataEvent event){
        // create "fake" adc bank
        if(event.hasBank("MC::Particle")) {
        DataBank part = event.getBank("MC::Particle");
        //double[] adcs = {(part.getFloat("vx", 0)+Math.random()-0.5)*2000, 
        //                 (part.getFloat("vy", 0)+Math.random()-0.5)*2000};
        double[] adcs = {(part.getFloat("vx", 0))*1000, 
                         (part.getFloat("vy", 0))*1000};
        DataBank adc  = event.createBank("RASTER::adc", 2);
            for(int i=0; i<adcs.length;i++) {
                adc.setByte("sector", i, (byte) 0);
                adc.setByte("layer", i, (byte) 0);
                adc.setShort("component", i, (short) i);
                adc.setInt("ADC", i, (int) adcs[i]);
            }
        event.appendBank(adc);
        }
    }

    
    public static void main(String arg[]) {
        
        RasterEngine engine = new RasterEngine();
        engine.init();
 
        // open hipo file
        String input = "/vol0/pilleux-l/Bureau/dev_COATJAVA/rastersoftware/out_rastersoftware_eventsRndm9mm_updated_16.hipo";
        HipoDataSource  reader = new HipoDataSource();
        reader.open(input);
		
	// initialize histos
        H2F hx = new H2F("x","", 100, -1000, 1000, 100, -1, 1);         
        hx.setTitleX("ADC X");
        hx.setTitleY("x (cm)");
        H2F hy = new H2F("y","", 100, -1000, 1000, 100, -1, 1);         
        hy.setTitleX("ADC Y");
        hy.setTitleY("y (cm)");
          
        // loop through events
        while(reader.hasEvent()){
            DataEvent event = (DataEvent) reader.getNextEvent();
            
            // for comparison
            DataBank MC_Part = event.getBank("MC::Particle");
            System.out.print("MC position read : " + MC_Part.getFloat("vx",0) +"\n");
                        
            // create adc bank
            engine.mcToRasterBank(event);
            DataBank Raster_adc = event.getBank("RASTER::adc");
            System.out.print("ADC value written from MC bank : " + Raster_adc.getInt("ADC",0) +"\n");
            
            // run the raster engine
            engine.processDataEvent(event);
            
            // read the output bank and fill the histograms
            if(event.hasBank("RASTER::position")) {
                DataBank bank = event.getBank("RASTER::position");
                double xpos = bank.getFloat("x", 0);
                double ypos = bank.getFloat("y", 0);
                // fill histograms
                System.out.print("Raster position : " + xpos + "\n");
                hx.fill(Raster_adc.getInt("ADC",0), xpos);
                hy.fill(Raster_adc.getInt("ADC",1), ypos);
            }
            
        }
        
        reader.close();
        
        JFrame frame = new JFrame("Raster");
        frame.setSize(800,400);
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.divide(2,1);
        canvas.cd(0); canvas.draw(hx);
        canvas.cd(1); canvas.draw(hy);
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);    
    }
}
