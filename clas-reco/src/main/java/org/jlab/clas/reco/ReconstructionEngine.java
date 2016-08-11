/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reco;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;
import org.jlab.clara.base.ClaraUtil;
import org.jlab.clara.engine.Engine;
import org.jlab.clara.engine.EngineData;
import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.engine.EngineStatus;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioFactory;

/**
 *
 * @author gavalian
 */
public abstract class ReconstructionEngine implements Engine {
    
    ConstantsManager   constantsManager  = new ConstantsManager();
    String             engineName        = "UnknownEngine";
    String             engineAuthor      = "N.T.";
    String             engineVersion     = "0.0";
    String             engineDescription = "CLARA Engine";
    
    public ReconstructionEngine(String name, String author, String version){
        engineName    = name;
        engineAuthor  = author;
        engineVersion = version;
    }

    abstract public boolean processDataEvent(DataEvent event);        
    abstract public boolean init();    
    
    public ConstantsManager  getConstantsManager(){
        return this.constantsManager;
    }    
    
    /**
     * 
     * @param ed
     * @return 
     */
    
    public EngineData configure(EngineData ed) {
        //EngineData data = new EngineData();
        System.out.println("--- engine configuration is called ");
        return ed;
    }

    public EngineData execute(EngineData input) {
        
        EngineData output = input;
        
        //return output;
        
        String mt = input.getMimeType();
        /*
        if (!mt.equalsIgnoreCase()) {
            String msg = String.format("Wrong input type: %s", mt);
            output.setStatus(EngineStatus.ERROR);
            output.setDescription(msg);
            return output;
        }*/
        
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
        } catch (Exception e) {
            String msg = String.format("Error processing input event%n%n%s", ClaraUtil.reportException(e));
            output.setStatus(EngineStatus.ERROR);
            output.setDescription(msg);
            return output;
        }
        
        return output;        
        
    }

    public EngineData executeGroup(Set<EngineData> set) {
        return null;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Set<EngineDataType> getInputDataTypes() {
        return ClaraUtil.buildDataTypes(Clas12Types.EVIO,
                                        EngineDataType.JSON,
                                        EngineDataType.STRING);
        //Set<EngineDataType> types = new HashSet<EngineDataType>();
        //types.add(EngineDataType.BYTES);
        //return types;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Set<EngineDataType> getOutputDataTypes() {
        return ClaraUtil.buildDataTypes(Clas12Types.EVIO,
                                        EngineDataType.JSON,
                                        EngineDataType.STRING);
        //Set<EngineDataType> types = new HashSet<EngineDataType>();
        //types.add(EngineDataType.BYTES);
        //return types;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Set<String> getStates() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return new HashSet<String>();
    }

    public ReconstructionEngine setDescription(String desc){
        this.engineDescription = desc;
        return this;
    }
    
    public String getDescription() {
        return this.engineDescription;
    }

    public String getVersion() {
        return this.engineVersion;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getAuthor() {
        return this.engineAuthor;
        // new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void reset() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void destroy() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
