/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.swim;

import cnuphys.magfield.CompositeField;
import cnuphys.magfield.MagneticField;
import cnuphys.magfield.Solenoid;
import cnuphys.magfield.Torus;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.jlab.clas.fastmc.Clas12FastMC;
import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.CLASResources;

/**
 *
 * @author gavalian
 */
public class SwimEngine extends ReconstructionEngine {
    
    GenericKinematicFitter fitter = new GenericKinematicFitter(11.0);
    //Clas12FastMC    fastMC = new Clas12FastMC(-1.0,1.0);
    public static List<CompositeField>  field = new ArrayList<CompositeField>();
    public static List<Torus>           ft    = new ArrayList<Torus>();
    public static MagF                  mg = new MagF("tata");
    
    public SwimEngine(){
        super("SWIMMER","gavalian","1.0");
        System.out.println("[swimmer] ----> constructor initialization");
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        
        this.test_magfield();
        /*
        Random rand = new Random();
        int mapSize = 2*1024*1024;
        float[] result = new float[3];
        float x,y,z;
        int xb,yb,zb;
        double integral = 0.0;
        for(int i = 0; i < 850000; i++){                    
            xb = rand.nextInt()%mapSize;
            yb = rand.nextInt()%mapSize;
            zb = rand.nextInt()%mapSize;
            xb = Math.abs(xb);
            yb = Math.abs(yb);
            zb = Math.abs(zb);

            double scale = mg.filed(xb,yb,zb);
            integral += scale;
        }*/
        
        return true;
    }

    private void test_magfield(){
        Random rand = new Random();
        float[] result = new float[3];
        float x,y,z;
        double integral = 0.0;
        
        for(int i = 0; i < 35000; i++){
            x = (float) (rand.nextDouble()*3.0+1.0);
            y = (float) (rand.nextDouble()*3.0+1.0);
            z = (float) (rand.nextDouble()*5.0+2.0);
            field.get(0).field(x, y, z, result);
            integral += Math.sqrt(result[0]*result[0]+result[1]*result[1]+result[2]*result[2]);            
        }
    }
    
    @Override
    public boolean init() {
        System.out.println("[swimmer] -------> ****** swimmer initialization ******");
        initField();
        return true;
    }
    
    private synchronized void initField(){
        if(field.isEmpty()){
            Torus torus = null;
         Solenoid solenoid = null;
         //will read mag field assuming 
         String clasDictionaryPath = CLASResources.getResourcePath("etc");		
         String torusFileName = clasDictionaryPath + "/data/magfield/clas12-fieldmap-torus.dat";		
         File torusFile = new File(torusFileName);
         try {
             torus = Torus.fromBinaryFile(torusFile);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
		
         //OK, see if we can create a Solenoid
         String solenoidFileName = clasDictionaryPath + "/data/magfield/clas12-fieldmap-solenoid.dat";
         //OK, see if we can create a Torus
         if(clasDictionaryPath == "../clasJLib")
             solenoidFileName = clasDictionaryPath + "/data/solenoid/v1.0/solenoid-srr.dat";
         
         File solenoidFile = new File(solenoidFileName);
         try {
             solenoid = Solenoid.fromBinaryFile(solenoidFile);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
         
         MagneticField.setInterpolate(false);
         CompositeField compositeField = new CompositeField();
         
         if (torus != null) {             
             torus.setScaleFactor(-1.0);
             
             compositeField.add(torus);
             ft.add(torus);
         }
         
         if (solenoid != null) {
             solenoid.setScaleFactor(1.0);             
             compositeField.add(solenoid);             
         }
         field.add(compositeField);
        } else {
            System.out.println("[swimmer] ---> there is no reason to load the field");
        }
    }
}
