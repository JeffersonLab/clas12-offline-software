package org.jlab.clas.reco;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;
import org.jlab.clara.engine.EngineData;
import org.jlab.clara.engine.EngineDataType;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.utils.JsonUtils;
import org.json.JSONObject;
import org.jlab.logging.DefaultLogger;


/**
 *
 * @author gavalian, kenjo, baltzell
 */
public class EngineProcessor {

    private final Map<String,ReconstructionEngine>  processorEngines = 
            new LinkedHashMap<String,ReconstructionEngine>();
    ReconstructionEngine  engineDummy = null;
    private static Logger LOGGER = Logger.getLogger(EngineProcessor.class.getPackage().getName());
    private boolean updateDictionary = true;
    
    private List<String>  schemaExempt = Arrays.asList("RUN::config","DC::tdc");
    
    public EngineProcessor(){
        this.engineDummy = new DummyEngine();
    }

    /**
     * add a reconstruction engine to the chain
     * @param name name of the engine in the chain
     * @param engine engine class
     */
    public void addEngine(String name, ReconstructionEngine engine){
        engine.init();
        this.processorEngines.put(name, engine);
    }

    
    private void updateDictionary(HipoDataSource source, HipoDataSync sync){
        SchemaFactory fsync = sync.getWriter().getSchemaFactory();
        SchemaFactory fsrc  = source.getReader().getSchemaFactory();
        List<String> schemaList = fsync.getSchemaKeys();
        for(int s = 0; s < schemaList.size(); s++){
            if(schemaExempt.contains(schemaList.get(s))==false){
                fsrc.remove(schemaList.get(s));
                fsrc.addSchema(fsync.getSchema(schemaList.get(s)).getCopy());
            } else {
                LOGGER.log(Level.INFO, "[dictrionary-update] schema {0} is not being updated\n", 
                        schemaList.get(s));
            }
        }
    }
    public void initDefault(){

        String[] names = new String[]{
            "MAGFIELDS",
            "DCCR","DCHB","FTOFHB","EC","HTCC","EBHB",
            "DCTB","FTOFTB","EBTB"
        };

        String[] services = new String[]{
            "org.jlab.clas.swimtools.MagFieldsEngine",
            "org.jlab.service.dc.DCHBClustering",
            "org.jlab.service.dc.DCHBPostClusterConv",
            "org.jlab.service.ftof.FTOFHBEngine",
            "org.jlab.service.ec.ECEngine",
            "org.jlab.service.htcc.HTCCReconstructionService",
            "org.jlab.service.eb.EBHBEngine",
            "org.jlab.service.dc.DCTBEngine",
            "org.jlab.service.ftof.FTOFTBEngine",
            "org.jlab.service.eb.EBTBEngine"
        };

        for(int i = 0; i < names.length; i++){
            this.addEngine(names[i], services[i]);
        }
    }
    public void initAll(){

        String[] names = new String[]{
            "MAGFIELDS",
            "FTCAL", "FTHODO", "FTTRK", "FTEB",
            "DCCR","DCHB","FTOFHB","EC",
            "CVTFP","CTOF","CND","BAND",
            "HTCC","LTCC","EBHB",
            "DCTB","FMT","FTOFTB","CVT","EBTB",
            "RICHEB","RTPC"
        };

        String[] services = new String[]{
            "org.jlab.clas.swimtools.MagFieldsEngine",
            "org.jlab.rec.ft.cal.FTCALEngine",
            "org.jlab.rec.ft.hodo.FTHODOEngine",
            "org.jlab.rec.ft.trk.FTTRKEngine",
            "org.jlab.rec.ft.FTEBEngine",
            "org.jlab.service.dc.DCHBClustering",
            "org.jlab.service.dc.DCHBPostClusterConv",
            "org.jlab.service.ftof.FTOFHBEngine",
            "org.jlab.service.ec.ECEngine",
            "org.jlab.rec.cvt.services.CVTEngine",
            "org.jlab.service.ctof.CTOFEngine",
            //"org.jlab.service.cnd.CNDEngine",
            "org.jlab.service.cnd.CNDCalibrationEngine",
            "org.jlab.service.band.BANDEngine",
            "org.jlab.service.htcc.HTCCReconstructionService",
            "org.jlab.service.ltcc.LTCCEngine",
            "org.jlab.service.eb.EBHBEngine",
            "org.jlab.service.dc.DCTBEngine",
            "org.jlab.service.fmt.FMTEngine",
            "org.jlab.service.ftof.FTOFTBEngine",
            "org.jlab.rec.cvt.services.CVTSecondPassEngine",
            "org.jlab.service.eb.EBTBEngine",
            "org.jlab.rec.rich.RICHEBEngine",
            "org.jlab.service.rtpc.RTPCEngine"
        };

        for(int i = 0; i < names.length; i++){
            this.addEngine(names[i], services[i]);
        }
    }

     public void initCaloDebug(){

        String[] names = new String[]{
            "EC","EB"
        };

        String[] services = new String[]{
            "org.jlab.service.ec.ECEngine",
            "org.jlab.service.eb.EBEngine",
        };

        for(int i = 0; i < names.length; i++){
            this.addEngine(names[i], services[i]);
        }
    }

    /**
     * Adding engine to the map the order of the services matters, since they will
     * be executed in order added.
     * @param name name for the service
     * @param clazz class name including the package name
     * @param jsonConf string in json format with engine configuration
     */
    public void addEngine(String name, String clazz, String jsonConf) {
        Class c;
        try {
            c = Class.forName(clazz);
            if( ReconstructionEngine.class.isAssignableFrom(c)==true){
                ReconstructionEngine engine = (ReconstructionEngine) c.newInstance();
                if(!jsonConf.equals("null")) {
                    EngineData input = new EngineData();
                    input.setData(EngineDataType.JSON.mimeType(), jsonConf);
                    engine.configure(input);
                }
                this.processorEngines.put(name, engine);
            } else {
                LOGGER.log(Level.SEVERE,">>>> ERROR: class is not a reconstruction engine : " + clazz);
            }

        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Adding engine to the map the order of the services matters, since they will
     * be executed in order added.
     * @param name name for the service
     * @param clazz class name including the package name
     */
    public void addEngine(String name, String clazz) {
        Class c;
        try {
            c = Class.forName(clazz);
            if( ReconstructionEngine.class.isAssignableFrom(c)==true){
                ReconstructionEngine engine = (ReconstructionEngine) c.newInstance();
                engine.init();
                this.processorEngines.put(name, engine);
            } else {
                LOGGER.log(Level.SEVERE,">>>> ERROR: class is not a reconstruction engine : " + clazz);
            }

        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Add reconstruction engine to the chain
     * @param clazz Engine class.
     */
    public void addEngine( String clazz) {
        Class c;
        try {
            c = Class.forName(clazz);
            if( ReconstructionEngine.class.isAssignableFrom(c)==true){
                ReconstructionEngine engine = (ReconstructionEngine) c.newInstance();
                engine.init();
                this.processorEngines.put(engine.getName(), engine);
            } else {
                LOGGER.log(Level.SEVERE, ">>>> ERROR: class is not a reconstruction engine : " + clazz);
            }

        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initialize all the engines in the chain.
     */
    public void init(){
        System.out.println("\n\n\n ");
        for(Map.Entry<String,ReconstructionEngine> entry : this.processorEngines.entrySet()){
            System.out.println(String.format("   >>>>>> (*) initializing : %8s : %s",entry.getKey(),
                    entry.getValue().getClass().getName()));
            entry.getValue().init();
        }
        System.out.println("\n\n");
    }

    /**
     * process a single event through the chain.
     * @param event
     */
    public void processEvent(DataEvent event){
        for(Map.Entry<String,ReconstructionEngine>  engine : this.processorEngines.entrySet()){
            try {
                if (!engine.getValue().wroteConfig) {
                    engine.getValue().wroteConfig = true;
                    JsonUtils.extend(event, ReconstructionEngine.CONFIG_BANK_NAME, "json",
                            engine.getValue().generateConfig());
                }
                if (engine.getValue().dropOutputBanks) {
                    engine.getValue().dropBanks(event);
                }
                engine.getValue().processDataEvent(event);
            } catch (Exception e){
                LOGGER.log(Level.SEVERE,"[Exception] >>>>> engine : " + engine.getKey() + "\n\n");
                e.printStackTrace();
            }
        }
    }

    public void processFile(String file, String output){
        this.processFile(file, output, -1, -1);
    }

    /**
     * process entire file through engine chain.
     * @param file file name to process.
     * @param output
     * @param nevents
     */
    public void processFile(String file, String output, int nskip, int nevents){
        if(file.endsWith(".hipo")==true||file.endsWith(".h5")==true
                ||file.endsWith(".h4")==true){
            HipoDataSource reader = new HipoDataSource();
            reader.open(file);
            
            int eventCounter = 0;
            HipoDataSync   writer = new HipoDataSync();
            writer.setCompressionType(2);
            writer.open(output);

            if(updateDictionary==true)
                updateDictionary(reader, writer);
            
            if(nskip>0 && nevents>0) nevents += nskip;
            
            ProgressPrintout  progress = new ProgressPrintout();
            while(reader.hasEvent()==true){
                DataEvent event = reader.getNextEvent();
                if(nskip<=0 || eventCounter>nskip) {
                    processEvent(event);
                    writer.writeEvent(event);
                }
                eventCounter++;
                if(nevents>0){
                    if(eventCounter>nevents) break;
                }
                progress.updateStatus();
            }
            progress.showStatus();
            writer.close();
        } else {
            LOGGER.info("\n\n>>>> error in file extension (use .hipo,.h4 or .h5)\n>>>> how is this not simple ?\n");
        }
        
    }

    /**
     * display services registered with the processor.
     */
    public void show(){
        System.out.println("----->>> EngineProcessor:");
        for(Map.Entry<String,ReconstructionEngine> entry : this.processorEngines.entrySet()){
            System.out.println(String.format("%-24s | %s", entry.getKey(),entry.getValue().getClass().getName()));
        }
    }

    public static void main(String[] args){

        OptionParser parser = new OptionParser("recon-util");
        parser.addRequired("-o","output.hipo");
        parser.addRequired("-i","input.hipo");
        parser.setRequiresInputList(false);
        parser.addOption("-c","0","use default configuration [0 - no, 1 - yes/default, 2 - all services] ");
        parser.addOption("-s","-1","number of events to skip");
        parser.addOption("-n","-1","number of events to process");
        parser.addOption("-y","0","yaml file");
        parser.addOption("-u","true","update dictionary from writer ? ");
        parser.addOption("-d","1","Debug level [0 - OFF, 1 - ON/default]");
        parser.setDescription("previously known as notsouseful-util");

        parser.parse(args);

        if(parser.getOption("-d").intValue() == 0)
            DefaultLogger.initialize();
        else
            DefaultLogger.debug();


        if(parser.hasOption("-i")==true&&parser.hasOption("-o")==true){

            List<String> services = parser.getInputList();

            String  inputFile = parser.getOption("-i").stringValue();
            String outputFile = parser.getOption("-o").stringValue();

            EngineProcessor proc = new EngineProcessor();
            int config  = parser.getOption("-c").intValue();
            int nskip   = parser.getOption("-s").intValue();
            String update = parser.getOption("-u").stringValue();
            int nevents = parser.getOption("-n").intValue();
            String yamlFileName = parser.getOption("-y").stringValue();

            //---------------------------------------------------------------//
            // added by GG. to turn off the dictionary update                //
            //---------------------------------------------------------------//
            if(update.contains("false")==true) proc.updateDictionary = false;
            
            if(!yamlFileName.equals("0")) {
                try {
                    InputStream input = new FileInputStream(yamlFileName);
                    Yaml yaml = new Yaml();
                    Map<String, Object> yamlConf = (Map<String, Object>) yaml.load(input);
                    JSONObject jsonObject=new JSONObject(yamlConf);
                    for(Object obj: jsonObject.getJSONArray("services")) {
                        if(obj instanceof JSONObject) {
                            JSONObject service = (JSONObject) obj;
                            String name = service.getString("name");
                            String engineClass = service.getString("class");
                            JSONObject configs = JsonUtils.filterClaraYaml(jsonObject,name);
                            if(configs.length()>0) {
                              proc.addEngine(name, engineClass, configs.toString());
                            } else {
                              proc.addEngine(name, engineClass);
                            }
                        }
                    }
                } catch (FileNotFoundException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                } catch (ClassCastException | YAMLException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
            else if (config>0){
                if(config>2){
                    proc.initCaloDebug();
                } else if(config==2){
                    proc.initAll();
                } else {
                    proc.initDefault();
                }
            }
            else {
                for(String engine : services){
                    System.out.println("Adding reconstruction engine " + engine);
                    proc.addEngine(engine);
                }
            }
            proc.processFile(inputFile,outputFile,nskip,nevents);
        }
    }
}
