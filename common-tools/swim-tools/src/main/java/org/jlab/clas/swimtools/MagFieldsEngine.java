package org.jlab.clas.swimtools;

import cnuphys.magfield.MagneticFields;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.CLASResources;
import org.jlab.utils.groups.IndexedTable;

public class MagFieldsEngine extends ReconstructionEngine {

    private String solShift =null;
    
    public MagFieldsEngine() {
        super("MagFields","ziegler","1.0");
    }
    AtomicInteger Run = new AtomicInteger(0);
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
        
         // Field Shifts
        solShift = this.getEngineConfigString("solenoidShift");
        
        if (solShift!=null) {
            System.out.println("["+this.getName()+"] run with solenoid z shift in tracking config chosen based on yaml ="+solShift);
            Swimmer.set_zShift(Float.valueOf(solShift));
        }
        else {
            solShift = System.getenv("SOLSHIFT");
            if (solShift!=null) {
                System.out.println("["+this.getName()+"] run with solenoid z shift in tracking config chosen based on env ="+solShift);
                Swimmer.set_zShift(Float.valueOf(solShift));
            }
        }
        if (solShift==null) {
            System.out.println("["+this.getName()+"] run with solenoid z shift based on CCDB CD position");
            // this.solenoidShift = (float) 0;
        }
        //torus:
        String TorX = this.getEngineConfigString("torusXShift");
        
        if (TorX!=null) {
            System.out.println("["+this.getName()+"] run with torus x shift in tracking config chosen based on yaml ="+TorX);
            Swimmer.setTorXShift(Float.valueOf(TorX));
        }
        else {
            TorX = System.getenv("TORXSHIFT");
            if (TorX!=null) {
                System.out.println("["+this.getName()+"] run with torus x shift in tracking config chosen based on env ="+TorX);
                Swimmer.setTorXShift(Float.valueOf(TorX));
            }
        }
        if (TorX==null) {
            System.out.println("["+this.getName()+"] run with torus x shift in tracking set to 0 cm");
            // this.solenoidShift = (float) 0;
        }
        
        String TorY = this.getEngineConfigString("torusYShift");
        
        if (TorY!=null) {
            System.out.println("["+this.getName()+"] run with torus y shift in tracking config chosen based on yaml ="+TorY);
            Swimmer.setTorYShift(Float.valueOf(TorY));
        }
        else {
            TorY = System.getenv("TORYSHIFT");
            if (TorY!=null) {
                System.out.println("["+this.getName()+"] run with torus y shift in tracking config chosen based on env ="+TorY);
                Swimmer.setTorYShift(Float.valueOf(TorY));
            }
        }
        if (TorY==null) {
            System.out.println("["+this.getName()+"] run with torus y shift in tracking set to 0 cm");
            // this.solenoidShift = (float) 0;
        }
        
        String TorZ = this.getEngineConfigString("torusZShift");
        
        if (TorZ!=null) {
            System.out.println("["+this.getName()+"] run with torus z shift in tracking config chosen based on yaml ="+TorZ);
            Swimmer.setTorZShift(Float.valueOf(TorZ));
        }
        else {
            TorZ = System.getenv("TORZSHIFT");
            if (TorZ!=null) {
                System.out.println("["+this.getName()+"] run with torus z shift in tracking config chosen based on env ="+TorZ);
                Swimmer.setTorZShift(Float.valueOf(TorZ));
            }
        }
        if (TorZ==null) {
            System.out.println("["+this.getName()+"] run with torus z shift in tracking set to 0 cm");
            // this.solenoidShift = (float) 0;
        }
    }
    
    private void loadTables() {
        String[]  ccdbTables = new String[]{ "/geometry/target"};

        requireConstants(Arrays.asList(ccdbTables));
        this.getConstantsManager().setVariation("default");
                
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        DataBank bank = event.getBank("RUN::config");
        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);
        if(newRun==0)
            return true;
        
        if(solShift==null) { // if no shift is set in the yaml file or environment, read from CCDB
            // will read target position and assume that is representative of the shift of the whole CD
            IndexedTable targetPosition = this.getConstantsManager().getConstants(newRun, "/geometry/target");
            Swimmer.set_zShift((float) targetPosition.getDoubleValue("position", 0,0,0));
        }
        
        Swimmer.setMagneticFieldsScales(bank.getFloat("solenoid", 0), bank.getFloat("torus", 0), 
                (double) 0.0, (double) 0.0, (double)Swimmer.get_zShift(),
                (double)Swimmer.getTorXShift(), (double)Swimmer.getTorYShift(), (double)Swimmer.getTorZShift());
        
        //FastMath.setMathLib(FastMath.MathLib.SUPERFAST);
        return true;
    }

    @Override
    public boolean init() {
        this.loadTables();
        this.initializeMagneticFields();
        return true;
    }

   
}
