package org.jlab.clas.swimtools;

import cnuphys.magfield.MagneticFields;
import java.util.concurrent.atomic.AtomicInteger;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.CLASResources;

public class MagFieldsEngine extends ReconstructionEngine {

    public MagFieldsEngine() {
        super("MagFields","ziegler","1.0");
    }
    AtomicInteger Run = new AtomicInteger(0);
    public static final double tilt = 25.;
    /**
     * 
     * determine torus and solenoid map name from yaml, else env, else crash
     */
    public void initializeMagneticFields() {
        String torusMap=this.getEngineConfigString("torusMap");
        String solenoidMap=this.getEngineConfigString("solenoidMap");
        if (torusMap!=null) {
            System.out.println("["+this.getName()+"] Torus Map chosen based on yaml: "+torusMap);
        }
        else {
            torusMap = System.getenv("TORUSMAP");
            if (torusMap!=null) {
                System.out.println("["+this.getName()+"] Torus Map chosen based on env: "+torusMap);
            }
        }
        if (torusMap==null) {
            throw new RuntimeException("["+this.getName()+"]  Failed to find torus map name in yaml or env.");
        }
        if (solenoidMap!=null) {
            System.out.println("["+this.getName()+"] solenoid Map chosen based on yaml: "+solenoidMap);
        }
        else {
            solenoidMap = System.getenv("SOLENOIDMAP");
            if (solenoidMap!=null) {
                System.out.println("["+this.getName()+"] solenoid Map chosen based on env: "+solenoidMap);
            }
        }
        if (solenoidMap==null) {
            throw new RuntimeException("["+this.getName()+"]  Failed to find solenoid map name in yaml or env.");
        }
        String mapDir = CLASResources.getResourcePath("etc")+"/data/magfield";
        try {
            MagneticFields.getInstance().initializeMagneticFields(mapDir,torusMap,solenoidMap);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        DataBank bank = event.getBank("RUN::config");
        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);
        if(newRun==0)
            return true;
        
        double shift =0;
        if(newRun>1890) {
            shift = -1.9;
        }
        Swimmer.setMagneticFieldsScales(bank.getFloat("solenoid", 0), bank.getFloat("torus", 0), shift);
        
        //FastMath.setMathLib(FastMath.MathLib.SUPERFAST);
        return true;
    }

    @Override
    public boolean init() {
        this.initializeMagneticFields();
        return true;
    }

   
}
