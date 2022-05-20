package org.jlab.clas.reco;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.clara.base.ClaraUtil;
import org.jlab.clara.engine.Engine;
import org.jlab.clara.engine.EngineData;
import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.engine.EngineStatus;
import org.jlab.detector.calib.utils.ConstantsManager;

import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioFactory;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.utils.JsonUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author gavalian
 */
public abstract class ReconstructionEngine implements Engine {

    Logger LOGGER = Logger.getLogger(ReconstructionEngine.class.getName());

    public static final String CONFIG_BANK_NAME = "COAT::config";
    
    volatile ConstantsManager                       constantsManager;
    volatile ConcurrentMap<String,ConstantsManager> constManagerMap;
    volatile SchemaFactory                          engineDictionary;

    volatile ConcurrentMap<String,String>           engineConfigMap;
    volatile String                                 engineConfiguration = null;
  
    volatile private boolean fatalError = false;
    
    volatile boolean wroteConfig = false;

    volatile boolean dropOutputBanks = false;
    private final Set<String> outputBanks = new HashSet<String>();

    String             engineName        = "UnknownEngine";
    String             engineAuthor      = "N.T.";
    String             engineVersion     = "0.0";
    String             engineDescription = "CLARA Engine";

    public ReconstructionEngine(String name, String author, String version){
        engineName    = name;
        engineAuthor  = author;
        engineVersion = version;
        constManagerMap   = new ConcurrentHashMap<>();
        engineDictionary  = new SchemaFactory();
        engineConfigMap   = new ConcurrentHashMap<>();
        String env = System.getenv("CLAS12DIR");
        engineDictionary.initFromDirectory( env +  "/etc/bankdefs/hipo4");
    }
   
    public ReconstructionEngine(String name, String author){ 
        this(name,author,"0.0");
        engineVersion = this.getClass().getPackage().getImplementationVersion();
    }

    public Map<String,String> getConfigMap() {
        return new ConcurrentHashMap<>(engineConfigMap);
    }

    public void registerOutputBank(String... bankName) {
        outputBanks.addAll(Arrays.asList(bankName));
        if (this.dropOutputBanks) {
            System.out.println(String.format("[%s]  dropping banks:  %s",this.getName(), Arrays.toString(bankName)));
        }
    }

    abstract public boolean processDataEvent(DataEvent event);
    abstract public boolean init();
   
    /**
     * Use a map just to avoid name clash in ConstantsManager.
     * @param tables map of table names to #indices
     */
    public void requireConstants(Map<String,Integer> tables){
        if(constManagerMap.containsKey(this.getClass().getName())==false){
            System.out.println("[ConstantsManager] ---> create a new one for module : " + this.getClass().getName());
            ConstantsManager manager = new ConstantsManager();
            manager.init(tables);
            constManagerMap.put(this.getClass().getName(), manager);
        }
    }

    public void requireConstants(List<String> tables){
        if(constManagerMap.containsKey(this.getClass().getName())==false){
            LOGGER.log(Level.INFO,"[ConstantsManager] ---> create a new one for module : " + this.getClass().getName());
            ConstantsManager manager = new ConstantsManager();
            manager.init(tables);
            constManagerMap.put(this.getClass().getName(), manager);
        }
    }

    public final String getEngineConfiguration(){
        return this.engineConfiguration;
    }
    
    public ConstantsManager  getConstantsManager(){
        return constManagerMap.get(this.getClass().getName());
    }

    public String getEngineConfigString(String key) {
        String val=null;
        if (this.engineConfigMap.containsKey(key)) {
            val=this.engineConfigMap.get(key);
        }
        return val;
    }

    /**
     *
     * @param ed
     * @return
     */
    @Override
    public EngineData configure(EngineData ed) {
        
        if (ed.getMimeType().equals(EngineDataType.JSON.toString())) {
            this.engineConfiguration = (String) ed.getData();
            LOGGER.log(Level.INFO,"[CONFIGURE][" + this.getName() + "] ---> JSON Data : " + this.engineConfiguration);
        } else {
            this.engineConfiguration = "";
            LOGGER.log(Level.INFO,"[CONFIGURE][" + this.getName() + "] *** WARNING *** ---> NO JSON Data provided");
        }
       
        // store yaml contents for easy access by engines:
        engineConfigMap = new ConcurrentHashMap<>();
        try {
            JSONObject base = new JSONObject(this.engineConfiguration);
            for (String key : base.keySet()) {
                engineConfigMap.put(key,base.getString(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        
      if(constManagerMap == null)
          constManagerMap = new ConcurrentHashMap<>();
      if(engineDictionary == null)
          engineDictionary = new SchemaFactory();
      LOGGER.log(Level.INFO,"--- engine configuration is called " + this.getDescription());
      try {
          if (this.getEngineConfigString("dropBanks")!=null &&
                  this.getEngineConfigString("dropBanks").equals("true")) {
              dropOutputBanks=true;
          }
          this.init();
      } catch (Exception e){
          LOGGER.log(Level.SEVERE,"[Wooops] ---> something went wrong with " + this.getDescription());
          e.printStackTrace();
      }
      System.out.println("----> I am doing nothing");
        
        try {
            if(engineConfiguration.length()>2){
//                String variation = this.getStringConfigParameter(engineConfiguration, "services", "variation");
                String variation = this.getStringConfigParameter(engineConfiguration, "variation");
                LOGGER.log(Level.INFO,"[CONFIGURE]["+ this.getName() +"] ---->  Setting variation : " + variation);
                if(variation.length()>2) this.setVariation(variation);
                String timestamp = this.getStringConfigParameter(engineConfiguration, "timestamp");
                LOGGER.log(Level.INFO,"[CONFIGURE]["+ this.getName() +"] ---->  Setting timestamp : " + timestamp);
                if(timestamp.length()>2) this.setTimeStamp(timestamp);
            } else {
                LOGGER.log(Level.WARNING,"[CONFIGURE][" + this.getName() +"] *** WARNING *** ---> configuration string is too short ("
                 + this.engineConfiguration + ")");
            }
        } catch (Exception e){
            LOGGER.log(Level.SEVERE,"[Engine] " + getName() + " failed to set variation", e);
        }
        return ed;
    }
    
    protected String getStringConfigParameter(String jsonString,                                             
            String key)  throws Exception {
        Object js;
        String variation = "";
        try {
            JSONObject base = new JSONObject(jsonString);
            
            if(base.has(key)==true){
                variation = base.getString(key);
            } else {
                LOGGER.log(Level.WARNING,"[JSON]" + this.getName() + " **** warning **** does not contain key = " + key);
            }
            /*
            js = base.get(key);
            if (js instanceof String) {
                return (String) js;
            } else {
                throw new Exception("JSONObject[" +  "] not a string.");
            }*/
        } catch (JSONException e) {
            throw new Exception(e.getMessage());
        }
        return variation;
    }

    /**
     * Method helps to extract configuration parameters defined in the Clara YAML file.
     *
     * @param jsonString JSon configuration object (passed to the userInit method).
     * @param group      config parameter group.
     * @param key        the key of the config parameter.
     * @return parameter: String value
     * @throws ClasEngineException org.jlab.clara.clas engine exception
     */
    protected String getStringConfigParameter(String jsonString,
                                              String group,
                                              String key)  throws Exception {
        Object js;
        try {
            JSONObject base = new JSONObject(jsonString);
            js = base.getJSONObject(group).get(key);
            if (js instanceof String) {
                return (String) js;
            } else {
                throw new Exception("JSONObject[" +  "] not a string.");
            }
        } catch (JSONException e) {
            throw new Exception(e.getMessage());
        }
    }
    
    public void setVariation(String variation){
       for(Map.Entry<String,ConstantsManager> entry : constManagerMap.entrySet()){
           LOGGER.log(Level.INFO,"[MAP MANAGER][" + this.getName() + "] ---> Setting " + entry.getKey() + " : variation = "
                   + variation );
           entry.getValue().setVariation(variation);
       }
    }
    
    public void setTimeStamp(String timestamp){
        for(Map.Entry<String,ConstantsManager> entry : constManagerMap.entrySet()){
            LOGGER.log(Level.INFO,"[MAP MANAGER][" + this.getName() + "] ---> Setting " + entry.getKey() + " : variation = "
                   + timestamp );
           entry.getValue().setTimeStamp(timestamp);
       }
    }
    
    protected boolean constantManagerStatus(){
        for(Map.Entry<String,ConstantsManager> entry : this.constManagerMap.entrySet()){
            if(entry.getValue().getRequestStatus()<0) return false;
        }
        return true;
    }
  
    /**
     * Generate a configuration section to drop in a HIPO bank, as the
     * engineConfigMap appended with the software version.  Here the top level
     * is "yaml" to facilitate merging JSON banks later.
     * @return 
     */
    public Map<String,Object> generateConfig() {
        Map<String,String> cfg = new HashMap<>(this.engineConfigMap);
        cfg.put("version",this.getClass().getPackage().getImplementationVersion());
        cfg.put("class",this.getClass().getCanonicalName());
        Map<String,Object> service = new HashMap<>();
        service.put(this.engineName,cfg);
        Map<String,Object> coatjava = new HashMap<>();
        coatjava.put("version",ConstantsManager.class.getPackage().getImplementationVersion());
        Map<String,Object> ret = new HashMap<>();
        service.put("COATJAVA", coatjava);
        ret.put("yaml", service);
        return ret;
    }
    
    public void dropBanks(DataEvent event) {
        for (String bankName : this.outputBanks) {
            if (event.hasBank(bankName)) {
                event.removeBank(bankName);
            }
        }
    }
    
    
    @Override
    public EngineData execute(EngineData input) {

        EngineData output = input;

        String mt = input.getMimeType();
        HipoDataEvent dataEventHipo = null;
        
        if(constantManagerStatus()==false){
            String msg = String.format("["+this.getName()+"] HALT : DATABASE CONNECTION ERROR");           
            output.setStatus(EngineStatus.ERROR, 13);
            output.setDescription(msg);
            return output;
        }

        if (fatalError) {
            String msg = String.format("["+this.getName()+"] HALT : FATAL ERROR");
            output.setStatus(EngineStatus.ERROR, 13);
            output.setDescription(msg);
            return output;
        }

        if(mt.compareTo("binary/data-hipo")==0){
            try {
                Event hipoEvent = (Event) input.getData();
                dataEventHipo = new HipoDataEvent(hipoEvent,engineDictionary);
            } catch (Exception e) {
                String msg = String.format("Error reading input event%n%n%s", ClaraUtil.reportException(e));
                output.setStatus(EngineStatus.ERROR);
                output.setDescription(msg);
                return output;
            }
                    
            try {
                if (!this.wroteConfig) {
                    this.wroteConfig = true;
                    JsonUtils.extend(dataEventHipo, CONFIG_BANK_NAME, "json", this.generateConfig());
                }
                if (this.dropOutputBanks) {
                    this.dropBanks(dataEventHipo);
                }
                this.processDataEvent(dataEventHipo);
                output.setData(mt, dataEventHipo.getHipoEvent());
            } catch (Exception e) {
                String msg = String.format("Error processing input event%n%n%s", ClaraUtil.reportException(e));
                output.setStatus(EngineStatus.ERROR);
                output.setDescription(msg);
                return output;
            }

            return output;
        }

        EvioDataEvent dataevent = null;

        if(mt.compareTo("binary/data-evio")==0){
            try {
                ByteBuffer bb = (ByteBuffer) input.getData();
                byte[] buffer = bb.array();
                ByteOrder endianness = bb.order();
                dataevent = new EvioDataEvent(buffer, endianness, EvioFactory.getDictionary());
            } catch (Exception e) {
                String msg = String.format("Error reading input event%n%n%s", ClaraUtil.reportException(e));
                output.setStatus(EngineStatus.ERROR);
                output.setDescription(msg);
                return output;
            }

            try {
                this.processDataEvent(dataevent);
                ByteBuffer  bbo = dataevent.getEventBuffer();
                //byte[] buffero = bbo.array();
                output.setData(mt, bbo);
            } catch (Exception e) {
                String msg = String.format("Error processing input event%n%n%s", ClaraUtil.reportException(e));
                output.setStatus(EngineStatus.ERROR);
                output.setDescription(msg);
                return output;
            }
            return output;
        }

        return input;
        /*
        if (!mt.equalsIgnoreCase()) {
            String msg = String.format("Wrong input type: %s", mt);
            output.setStatus(EngineStatus.ERROR);
            output.setDescription(msg);
            return output;
        }*/
        /*
        EvioDataEvent dataevent = null;

        try {
            ByteBuffer bb = (ByteBuffer) input.getData();
            byte[] buffer = bb.array();
            ByteOrder endianness = bb.order();
            dataevent = new EvioDataEvent(buffer, endianness, EvioFactory.getDictionary());
        } catch (Exception e) {
            String msg = String.format("Error reading input event%n%n%s", ClaraUtil.reportException(e));
            output.setStatus(EngineStatus.ERROR);
            output.setDescription(msg);
            return output;
        }

        try {
            this.processDataEvent(dataevent);
            ByteBuffer  bbo = dataevent.getEventBuffer();
            //byte[] buffero = bbo.array();
            output.setData(mt, bbo);
        } catch (Exception e) {
            String msg = String.format("Error processing input event%n%n%s", ClaraUtil.reportException(e));
            output.setStatus(EngineStatus.ERROR);
            output.setDescription(msg);
            return output;
        }

        return output;
        */
    }

    @Override
    public EngineData executeGroup(Set<EngineData> set) {
        return null;
    }

    @Override
    public Set<EngineDataType> getInputDataTypes() {
        return ClaraUtil.buildDataTypes(Clas12Types.EVIO,
                Clas12Types.HIPO,
                EngineDataType.JSON,
                EngineDataType.STRING);
    }

    @Override
    public Set<EngineDataType> getOutputDataTypes() {
        return ClaraUtil.buildDataTypes(Clas12Types.EVIO,
                Clas12Types.HIPO,
                EngineDataType.JSON,
                EngineDataType.STRING);
    }

    @Override
    public Set<String> getStates() {
        return new HashSet<String>();
    }

    public ReconstructionEngine setDescription(String desc){
        this.engineDescription = desc;
        return this;
    }

    @Override
    public String getDescription() {
        return this.engineDescription;
    }

    public String getName(){
        return this.engineName;
    }

    @Override
    public String getVersion() {
        return this.engineVersion;
    }

    @Override
    public String getAuthor() {
        return this.engineAuthor;
    }

    @Override
    public void reset() {
    }

    @Override
    public void destroy() {
    }
    
    public void setFatal() {
        this.fatalError = true;
    }

    public boolean getFatal() {
        return this.fatalError;
    }
    
    
    public static class Reco extends ReconstructionEngine {
        public Reco(){
            super("a","b","c");
        }
        @Override
        public boolean processDataEvent(DataEvent event) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean init() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    
}
    public static void main(String[] args){
        System.setProperty("CLAS12DIR", "/Users/gavalian/Work/Software/project-3a.0.0/Distribution/clas12-offline-software/coatjava");
        try {
            String json = "{\n" +
                    "\"ccdb\":\n" +
                    "{\n" +
                    "\"run\":101,\n" +
                    "\"variation\":\"custom\"\n" +
                    "},\n" +
                    "\"runmode\":\"calibration\",\n" +
                    "\"runtype\":\"mc\",\n" +
                    "\"magnet\":\n" +
                    "{\n" +
                    "\"torus\":-1,\n" +
                    "\"solenoid\":-1\n" +
                    "},\n" +                    
                    "\"variation\":\"cosmic\",\n" +
                    "\"timestamp\":333\n" +
                    "}";
            System.out.println(json);
            //json = "{ \"ccdb\":{\"run\":10,\"variation\":\"default\"}, \"variation\":\"cosmic\"}";
            Reco reco = new Reco();
            String variation =  reco.getStringConfigParameter(json, "variation");
            System.out.println(" Variation : " + variation);
        } catch (Exception ex) {
            Logger.getLogger(ReconstructionEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
