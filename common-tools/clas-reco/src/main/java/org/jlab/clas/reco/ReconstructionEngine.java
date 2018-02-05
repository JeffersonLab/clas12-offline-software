/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reco;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
import org.jlab.jnp.hipo.data.HipoEvent;
import org.jlab.jnp.hipo.schema.SchemaFactory;
import org.json.JSONException;
import org.json.JSONObject;
import static org.json.JSONObject.quote;

/**
 *
 * @author gavalian
 */
public abstract class ReconstructionEngine implements Engine {

    volatile ConstantsManager                       constantsManager;
    volatile ConcurrentMap<String,ConstantsManager> constManagerMap;
    volatile SchemaFactory                          engineDictionary;

    volatile String                                 engineConfiguration = null;
    
    String             engineName        = "UnknownEngine";
    String             engineAuthor      = "N.T.";
    String             engineVersion     = "0.0";
    String             engineDescription = "CLARA Engine";

    public ReconstructionEngine(String name, String author, String version){
        engineName    = name;
        engineAuthor  = author;
        engineVersion = version;
        constManagerMap   = new ConcurrentHashMap<String,ConstantsManager>();
        engineDictionary  = new SchemaFactory();
        engineDictionary.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        //System.out.println("[Engine] >>>>> constants manager : " + getConstantsManager().toString());
    }

    abstract public boolean processDataEvent(DataEvent event);
    abstract public boolean init();
    
    public void requireConstants(List<String> tables){
        if(constManagerMap.containsKey(this.getClass().getName())==false){
            System.out.println("[ConstantsManager] ---> create a new one for module : " + this.getClass().getName());
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

    /**
     *
     * @param ed
     * @return
     */
    @Override
    public EngineData configure(EngineData ed) {
        
        if (ed.getMimeType().equals(EngineDataType.JSON.toString())) {
            this.engineConfiguration = (String) ed.getData();
            System.out.println("[CONFIGURE][" + this.getName() + "] ---> JSON Data : " + this.engineConfiguration);
        } else {
            this.engineConfiguration = "";
            System.out.println("[CONFIGURE][" + this.getName() + "] *** WARNING *** ---> NO JSON Data provided");
        }
        
      if(constManagerMap == null)
      constManagerMap   = new ConcurrentHashMap<String,ConstantsManager>();
      if(engineDictionary == null)
      engineDictionary  = new SchemaFactory();
        //EngineData data = new EngineData();
        System.out.println("--- engine configuration is called " + this.getDescription());
        try {
            this.init();
        } catch (Exception e){
            System.out.println("[Wooops] ---> something went wrong with " + this.getDescription());
            e.printStackTrace();
        }
        System.out.println("----> I am doing nothing");
        
        try {
            if(engineConfiguration.length()>2){
                String variation = this.getStringConfigParameter(engineConfiguration, "services", "variation");
                System.out.println("[CONFIGURE]["+ this.getName() +"] ---->  Setting variation : " + variation);
                this.setVariation(variation);
            } else {
                System.out.println("[CONFIGURE][" + this.getName() +"] *** WARNING *** ---> configuration string is too short ("
                 + this.engineConfiguration + ")");
            }
        } catch (Exception e){
            System.out.println("[Engine] " + getName() + " failet to set variation");
        }
        return ed;
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
           System.out.println("[MAP MANAGER][" + this.getName() + "] ---> Setting " + entry.getKey() + " : variation = "
                   + variation );
           entry.getValue().setVariation(variation);
       }
    }
    
    @Override
    public EngineData execute(EngineData input) {

        EngineData output = input;

        //return output;

        String mt = input.getMimeType();
        //System.out.println(" DATA TYPE = [" + mt + "]");
        HipoDataEvent dataEventHipo = null;
        if(mt.compareTo("binary/data-hipo")==0){
            try {
                //ByteBuffer bb = (ByteBuffer) input.getData();
                HipoEvent hipoEvent = (HipoEvent) input.getData();
                hipoEvent.setSchemaFactory(engineDictionary, false);
                dataEventHipo = new HipoDataEvent(hipoEvent);
                
                //dataEventHipo.initDictionary(engineDictionary);
                //dataEventHipo = new HipoDataEvent(bb.array(),this.engineDictionary);
            } catch (Exception e) {
                String msg = String.format("Error reading input event%n%n%s", ClaraUtil.reportException(e));
                output.setStatus(EngineStatus.ERROR);
                output.setDescription(msg);
                return output;
            }

            try {
                this.processDataEvent(dataEventHipo);
                ByteBuffer  bbo = dataEventHipo.getEventBuffer();
                //byte[] buffero = bbo.array();
                //output.setData(mt, bbo);
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
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<EngineDataType> getInputDataTypes() {
        return ClaraUtil.buildDataTypes(Clas12Types.EVIO,
                Clas12Types.HIPO,
                EngineDataType.JSON,
                EngineDataType.STRING);
        //Set<EngineDataType> types = new HashSet<EngineDataType>();
        //types.add(EngineDataType.BYTES);
        //return types;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<EngineDataType> getOutputDataTypes() {
        return ClaraUtil.buildDataTypes(Clas12Types.EVIO,
                Clas12Types.HIPO,
                EngineDataType.JSON,
                EngineDataType.STRING);
        //Set<EngineDataType> types = new HashSet<EngineDataType>();
        //types.add(EngineDataType.BYTES);
        //return types;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> getStates() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAuthor() {
        return this.engineAuthor;
        // new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reset() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
