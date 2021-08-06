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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.clas.fastmc.Clas12FastMC;
import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.CLASResources;

/**
 *
 * @author gavalian
 */
public class SwimEngine extends ReconstructionEngine {
    
    GenericKinematicFitter fitter = new GenericKinematicFitter(11.0);
    
    Clas12FastMC    fastMC = null;
    public static boolean isSimulation = true;
    
    public static List<CompositeField>  field = new ArrayList<CompositeField>();
    public  List<Torus>           ft    = new ArrayList<Torus>();
    public static MagF                  mg = new MagF("tata");
    public List<MagneticField>          magfield = new ArrayList<MagneticField>();
    
    public SwimEngine(){
        super("SWIMMER","gavalian","1.0");
        System.out.println("[swimmer] ----> constructor initialization");
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        //this.test_caching_index();
        //this.test_caching();
        this.swimParticles(event);
        //this.test_magfield();
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

    
    private void test_caching_index(){
        int mapSize = 2905308;
        Random rand = new Random();
        double integral = 0.0;
        float x,y,z;
        float[] result = new float[3];
        
        for(int i = 0; i < 350000; i++){ 
            x = (float) (rand.nextDouble()*3.0+1.0);
            y = (float) (rand.nextDouble()*3.0+1.0);
            z = (float) (rand.nextDouble()*5.0+2.0);
            int index_x = magfield.get(0).getQ1Coordinate().getIndex(x);
            int index_y = magfield.get(0).getQ1Coordinate().getIndex(y);
            int index_z = magfield.get(0).getQ1Coordinate().getIndex(z);
            if(index_x>0&&index_y>0&&index_z>0){
                int index = index_x * (magfield.get(0).getQ2Coordinate().getNumPoints() * 
                        magfield.get(0).getQ3Coordinate().getNumPoints()) 
				+ index_y * magfield.get(0).getQ3Coordinate().getNumPoints() + index_z;
                //float value = this.magfield.get(0).getBuffer().get(index);
                //integral += value;
            }
                       
            /*float rho = (float) Math.hypot(x, y);
            float phi = (float) Math.toDegrees(Math.atan2(y, x));
            fieldCylindrical(phi, rho, z, result);
            */
            //System.out.println("index = " + index_x + " " + index_y + " " + index_z);
            //float value = this.magfield.get(0).getBuffer().get(xb);
            //integral += value;
        }
    }
    
    private void swimParticles(DataEvent event){
                
        PhysicsEvent physEvent = fitter.getGeneratedEvent(event);
        for(int j = 0; j < 10; j++){
            
                int count = physEvent.count();
                for(int i = 0; i < count; i++){
                    //if(SwimEngine.isSimulation==true){
                        fastMC.checkParticle(physEvent.getParticle(i));
                    //}
                }
        }
    }
    
    private void test_caching(){
        //int mapSize = 2905308;
        int mapSize = 22869363;
        Random rand = new Random();
        double integral = 0.0;
        for(int i = 0; i < 850000; i++){ 
            int xb = rand.nextInt()%mapSize;
            xb = Math.abs(xb);
            //float value = this.magfield.get(0).getBuffer().get(xb);
            //integral += value;
        }
    }
    
    private void test_magfield(){
        Random rand = new Random();
        float[] result = new float[3];
        float x,y,z;
        double integral = 0.0;
        
        for(int i = 0; i < 750000; i++){
            x = (float) (rand.nextDouble()*3.0+1.0);
            y = (float) (rand.nextDouble()*3.0+1.0);
            z = (float) (rand.nextDouble()*5.0+2.0);
            //ft.get(0).field(x, y, z, result);
            //float rho = (float) Math.hypot(x, y);
            float rho2 = (float) Math.sqrt(x*x + y*y);
            float mag  = (float) Math.sqrt(x*x + y*y + z*z);
            result[0] = rho2;
            float phi = (float) CoatFastMath.atan2(x, y);
            //float phi = (float) Math.atan2(x, y);
            //this.ft.get(0).fieldCylindrical(phi, rho, z, result);            
            //this.ft.get(0).interpolateField(phi,rho,z, result);
            integral += Math.sqrt(result[0]*result[0]+result[1]*result[1]+result[2]*result[2]);            
        }
    }
    
    @Override
    public boolean init() {
        System.out.println("[swimmer] -------> ****** swimmer initialization ******");
        //this.initField();
        //this.initMagField();
        fastMC = new Clas12FastMC(-1.0,1.0);
        //MagneticField.setMathLib(MagneticField.MathLib.FAST);
        //System.out.println("[swimmer] -------> FAST MONTE CARLO TEST. USING FAST MATH = " + MagneticField.MathLib.SUPERFAST);
        return true;
    }
    
    
    private synchronized void initMagField(){
        String clasDictionaryPath = CLASResources.getResourcePath("etc");		
        String torusFileName = clasDictionaryPath + "/data/magfield/clas12-fieldmap-torus.dat";		
        File torusFile = new File(torusFileName);
        MagneticField mf = new MagneticField(){

            @Override
	    public boolean isActive() {
                return true;            
	    }
    
            @Override
            public String getName() {
                return "TORUS";
            }

            public void fieldCylindrical(double d, double d1, double d2, float[] floats) {
                
            }            

            @Override
            public void printConfiguration(PrintStream ps) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        try {
            mf.readBinaryMagneticField(torusFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SwimEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        magfield.add(mf);
        //System.out.println(" MAGNETIC FIELD CAPACITY = " + mf.getBuffer().capacity());
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
             solenoid.readBinaryMagneticField(solenoidFile);
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
