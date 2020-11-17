/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reco;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ClassCastException;
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
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clara.engine.EngineData;
import org.jlab.clara.engine.EngineDataType;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import java.io.InputStream;
import java.io.FileInputStream;
import org.json.JSONObject;


/**
 *
 * @author gavalian, kenjo
 */
public class EngineProcessor {

    private final Map<String,ReconstructionEngine>  processorEngines = new LinkedHashMap<String,ReconstructionEngine>();
    ReconstructionEngine  engineDummy = null;

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

    public void initDefault(){

        String[] names = new String[]{
            "MAGFIELDS",
            "DCHB","FTOFHB","EC","HTCC","EBHB",
            "DCTB","FTOFTB","EBTB"
        };

        String[] services = new String[]{
            "org.jlab.clas.swimtools.MagFieldsEngine",
            "org.jlab.service.dc.DCHBEngine",
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
        /*
        for(String service : services){
            this.addEngine(service);
        }*/
    }
    public void initAll(){

        String[] names = new String[]{
            "MAGFIELDS",
            "FTCAL", "FTHODO", "FTEB",
            "DCHB","FTOFHB","EC",
            "CVT","CTOF","CND","BAND",
            "HTCC","LTCC","EBHB",
            "DCTB","FTOFTB","EBTB","RICHEB","RTPC"
        };

        String[] services = new String[]{
            "org.jlab.clas.swimtools.MagFieldsEngine",
            "org.jlab.rec.ft.cal.FTCALEngine",
            "org.jlab.rec.ft.hodo.FTHODOEngine",
            "org.jlab.rec.ft.FTEBEngine",
            "org.jlab.service.dc.DCHBEngine",
            "org.jlab.service.ftof.FTOFHBEngine",
            "org.jlab.service.ec.ECEngine",
            "org.jlab.rec.cvt.services.CVTReconstruction",
            "org.jlab.service.ctof.CTOFEngine",
            //"org.jlab.service.cnd.CNDEngine",
            "org.jlab.service.cnd.CNDCalibrationEngine",
            "org.jlab.service.band.BANDEngine",
            "org.jlab.service.htcc.HTCCReconstructionService",
            "org.jlab.service.ltcc.LTCCEngine",
            "org.jlab.service.eb.EBHBEngine",
            "org.jlab.service.dc.DCTBEngine",
            "org.jlab.service.ftof.FTOFTBEngine",
            "org.jlab.service.eb.EBTBEngine",
            "org.jlab.rec.rich.RICHEBEngine",
            "org.jlab.service.rtpc.RTPCEngine"
        };

        for(int i = 0; i < names.length; i++){
            this.addEngine(names[i], services[i]);
        }
        /*
        for(String service : services){
            this.addEngine(service);
        }*/
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
        /*
        for(String service : services){
            this.addEngine(service);
        }*/
    }
    /**
     * Adding engine to the map the order of the services matters, since they will
     * be executed in order added.
     * @param name name for the service
     * @param clazz class name including the package name
     * @param jsonConfig string in json format with engine configuration
     */
    public void addEngine(String name, String clazz, String jsonConf) {
        Class c = null;
        try {
            c = Class.forName(clazz);
            if( ReconstructionEngine.class.isAssignableFrom(c)==true){
                ReconstructionEngine engine = (ReconstructionEngine) c.newInstance();
                engine.init();
                if(jsonConf!="null") {
                    EngineData input = new EngineData();
                    input.setData(EngineDataType.JSON.mimeType(), jsonConf);
                    engine.configure(input);
                }
                this.processorEngines.put(name, engine);
            } else {
                System.out.println(">>>> ERROR: class is not a reconstruction engine : " + clazz);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Adding engine to the map the order of the services matters, since they will
     * be executed in order added.
     * @param name name for the service
     * @param clazz class name including the package name
     */
    public void addEngine(String name, String clazz) {
        Class c = null;
        try {
            c = Class.forName(clazz);
            if( ReconstructionEngine.class.isAssignableFrom(c)==true){
                ReconstructionEngine engine = (ReconstructionEngine) c.newInstance();
                engine.init();
                this.processorEngines.put(name, engine);
            } else {
                System.out.println(">>>> ERROR: class is not a reconstruction engine : " + clazz);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Add reconstruction engine to the chain
     * @param clazz Engine class.
     */
    public void addEngine( String clazz) {
        Class c = null;
        try {
            c = Class.forName(clazz);
            if( ReconstructionEngine.class.isAssignableFrom(c)==true){
                ReconstructionEngine engine = (ReconstructionEngine) c.newInstance();
                engine.init();
                this.processorEngines.put(engine.getName(), engine);
            } else {
                System.out.println(">>>> ERROR: class is not a reconstruction engine : " + clazz);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
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
                //System.out.println("processing engine : " + engine.getKey());
                //System.out.println("processing event");
                engine.getValue().processDataEvent(event);
            } catch (Exception e){

                System.out.println("[Exception] >>>>> engine : " + engine.getKey());
                System.out.println();
                e.printStackTrace();
            }
        }
    }

    public void processFile(String file, String output){
        this.processFile(file, output, -1);
    }
    /**
     * process entire file through engine chain.
     * @param file file name to process.
     * @param output
     */
    public void processFile(String file, String output, int nevents){
        if(file.endsWith(".hipo")==true){
            HipoDataSource reader = new HipoDataSource();
            reader.open(file);

            int eventCounter = 0;
            HipoDataSync   writer = new HipoDataSync();
            writer.setCompressionType(2);
            writer.open(output);

            ProgressPrintout  progress = new ProgressPrintout();
            while(reader.hasEvent()==true){
                DataEvent event = reader.getNextEvent();
                processEvent(event);
                writer.writeEvent(event);
                eventCounter++;
                if(nevents>0){
                    if(eventCounter>nevents) break;
                }
                progress.updateStatus();
            }
            progress.showStatus();
            writer.close();
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
        parser.addOption("-n","-1","number of events to process");
        parser.addOption("-y","0","yaml file");
        parser.setDescription("previously known as notsouseful-util");

        parser.parse(args);

        if(parser.hasOption("-i")==true&&parser.hasOption("-o")==true){

            List<String> services = parser.getInputList();

            String  inputFile = parser.getOption("-i").stringValue();
            String outputFile = parser.getOption("-o").stringValue();

            /*for(int i =1; i < args.length; i++){
                services.add(args[i]);
            }*/

            EngineProcessor proc = new EngineProcessor();
            int config  = parser.getOption("-c").intValue();
            int nevents = parser.getOption("-n").intValue();
            String yamlFileName = parser.getOption("-y").stringValue();
            if(yamlFileName!="0") {
                try {
                    InputStream input = new FileInputStream(yamlFileName);
                    Yaml yaml = new Yaml();
                    Map<String, Object> yamlConf = (Map<String, Object>) yaml.load(input);
                    JSONObject jsonObject=new JSONObject(yamlConf);
                    System.out.println(jsonObject.getJSONObject("configuration").getJSONObject("services"));
                    for(Object obj: jsonObject.getJSONArray("services")) {
                        if(obj instanceof JSONObject) {
                            JSONObject service = (JSONObject) obj;
                            String name = service.getString("name");
                            String engineClass = service.getString("class");
                            System.out.println(name);
                            System.out.println(engineClass);
                            JSONObject configs = jsonObject.getJSONObject("configuration").getJSONObject("services");
                            if(configs.has(name)) {
                              proc.addEngine(name, engineClass, configs.getJSONObject(name).toString());
                            } else {
                              proc.addEngine(name, engineClass);
                            }
                        }
//                      System.out.println(yamlConf.get("configuration").get("services"));
//                      String jsonConf = gson.toJson(yamlConf["configuration"]["services"][service["name"]]);
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassCastException | YAMLException ex) {
                    Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (config>0){
                if(config>2){
                    proc.initCaloDebug();
                } else if(config==2){
                    proc.initAll();
                } else {
                    proc.initDefault();
                }
            } else {
                for(String engine : services){
                    System.out.println("Adding reconstruction engine " + engine);
                    proc.addEngine(engine);
                }
            }
            proc.processFile(inputFile,outputFile,nevents);
        }
    }
}
