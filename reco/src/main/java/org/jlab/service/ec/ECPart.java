package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;

import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.physics.Vector3;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;


public class ECPart {
	
    public static double distance11,distance12,distance21,distance22;
    public static double e1,e2,e1c,e2c,cth,cth1,cth2,X,tpi2,cpi0;
    static double mpi0 = 0.1349764;
    public static String geom = "2.4";
    public static double SF1 = 0.27;
    public static double SF2 = 0.27;
    
    public List<DetectorResponse>  readEC(EvioDataEvent event){
        List<DetectorResponse>  ecResponse = new ArrayList<DetectorResponse>();
        if(event.hasBank("ECDetector::clusters")==true){
            EvioDataBank ecCL = (EvioDataBank) event.getBank("ECDetector::clusters");
            int nrows = ecCL.rows();
            for(int i = 0; i < nrows; i++){
                DetectorResponse response = new DetectorResponse();
                response.getDescriptor().setType(DetectorType.EC);
                response.getDescriptor().setSectorLayerComponent(
                        ecCL.getInt("sector", i),
                        ecCL.getInt("layer", i),
                        0
                        );
                response.setPosition(
                        ecCL.getDouble("X", i),
                        ecCL.getDouble("Y", i),
                        ecCL.getDouble("Z", i)
                        );
                response.setEnergy(ecCL.getDouble("energy", i));
                ecResponse.add(response);
            }            
        }
        return ecResponse;
    }
    
    public static List<DetectorResponse>  getResponseForLayer(List<DetectorResponse> res, int layer){
        List<DetectorResponse>  ecr = new ArrayList<DetectorResponse>();
        for(DetectorResponse r : res){
            if(r.getDescriptor().getLayer()==layer){
                ecr.add(r);
            }
        }
        return ecr;        
    }
    
    public double getTwoPhoton(PhysicsEvent gen, List<DetectorResponse> ecResponses){
        
        List<DetectorResponse>        rPCAL = ECPart.getResponseForLayer(ecResponses, 1);
        
        if(rPCAL.size()!=2) return   0.0;
        List<DetectorParticle>  particles = new ArrayList<DetectorParticle>();
        
        for(int i = 0; i < rPCAL.size(); i++){
            DetectorParticle p = new DetectorParticle();
            p.setCross(0.0, 0.0, 0.0, 
                    rPCAL.get(i).getPosition().x(),
                    rPCAL.get(i).getPosition().y(),
                    rPCAL.get(i).getPosition().z()
                    );
            p.addResponse(rPCAL.get(i));
            particles.add(p);
            //System.out.println(p);
        }
        
        List<DetectorResponse> rECIN  = ECPart.getResponseForLayer(ecResponses, 4);
        List<DetectorResponse> rECOUT = ECPart.getResponseForLayer(ecResponses, 7);
        
        distance11=distance12=distance21=distance22=-10;
        
        int index_ecin  = particles.get(0).getDetectorHitIndex(rECIN);
        
        if(index_ecin>=0&&index_ecin<rECIN.size()){
            distance11 = particles.get(0).getDistance(rECIN.get(index_ecin)).length();
            if(distance11<15.0){
                particles.get(0).addResponse(rECIN.get(index_ecin));
                rECIN.remove(index_ecin);
            }
        }
        
        int index_ecout  = particles.get(0).getDetectorHitIndex(rECOUT);
        
        if(index_ecout>=0&&index_ecout<rECOUT.size()){
            distance12 = particles.get(0).getDistance(rECOUT.get(index_ecout)).length();
            if(distance12<15.0){
                particles.get(0).addResponse(rECOUT.get(index_ecout));
                rECOUT.remove(index_ecout);
            }
        }
        
        index_ecin  = particles.get(1).getDetectorHitIndex(rECIN);
        
        if(index_ecin>=0&&index_ecin<rECIN.size()){
            distance21 = particles.get(1).getDistance(rECIN.get(index_ecin)).length();
            if(distance21<15.0){
                particles.get(1).addResponse(rECIN.get(index_ecin));
                rECIN.remove(index_ecin);
            }
        }
        
        index_ecout  = particles.get(1).getDetectorHitIndex(rECOUT);
        
        if(index_ecout>=0&&index_ecout<rECOUT.size()){
            distance22 = particles.get(1).getDistance(rECOUT.get(index_ecout)).length();
            if(distance22<15.0){
                particles.get(1).addResponse(rECOUT.get(index_ecout));
                rECOUT.remove(index_ecout);
            }
        }
        
        
        //System.out.println("--------------  EVENT -------------");
        for(DetectorParticle p : particles){
            double energy = p.getEnergy(DetectorType.EC);
            //p.getPhysicsParticle(22);
            //System.out.println(" energy = " + energy);
            //System.out.println(p);
        }
        
        Vector3 n1 = particles.get(0).getCrossDir();
        Vector3 n2 = particles.get(1).getCrossDir();
        
        e1 = particles.get(0).getEnergy(DetectorType.EC);
        e2 = particles.get(1).getEnergy(DetectorType.EC);
        
        n1.unit();
        n2.unit();
        
        SF1 = getSF(geom,e1); e1c=e1/SF1;
        Particle g1 = new Particle(22,
                n1.x()*e1c,
                n1.y()*e1c,
                n1.z()*e1c
        );
        
        SF2 = getSF(geom,e2); e2c = e2/SF2;
        Particle g2 = new Particle(22,
                n2.x()*e2c,
                n2.y()*e2c,
                n2.z()*e2c
        );
        
        X    =  1e3;
        tpi2 =  1e9;
        cpi0 =  -1;
        cth1 = Math.cos(g1.theta());
        cth2 = Math.cos(g2.theta());
         cth = g1.cosTheta(g2);
        
        if(particles.get(0).getResponse(DetectorType.EC, 1)!=null&&
           particles.get(0).getResponse(DetectorType.EC, 4)!=null&&
           particles.get(1).getResponse(DetectorType.EC, 1)!=null&&
           particles.get(1).getResponse(DetectorType.EC, 4)!=null                
                ){
            /*
            System.out.println("  ENERGIES = " + (e1/0.27) + "  " + (e2/0.27));
            System.out.println(particles.get(0));
            System.out.println(particles.get(1));
            System.out.println(gen);*/
            X = (e1c-e2c)/(e1c+e2c);
            tpi2 = 2*mpi0*mpi0/(1-cth)/(1-X*X);
            cpi0 = (e1c*cth1+e2c*cth2)/Math.sqrt(e1c*e1c+e2c*e2c+2*e1c*e2c*cth);
            g1.combine(g2, +1);
            return g1.vector().mass2();
        }
        
        return  0.0;

        //System.out.println("  ENERGIES = " + (e1/0.27) + "  " + (e2/0.27));
        //System.out.println(particles.get(0));
        //System.out.println(particles.get(1));
        //System.out.println(gen);
    }
    
    public static double getSF(String geom, double e) {
        switch (geom) {
        case "2.4": return 0.268*(1.0510 - 0.0104/e - 0.00008/e/e); 
        case "2.5": return 0.250*(1.0286 - 0.0150/e + 0.00012/e/e);
        }
        return Double.parseDouble(geom);
    }    
    
    public static void getPhoton(PhysicsEvent gen, List<DetectorResponse>  ecResponses){

        List<DetectorResponse>  rPCAL  = ECPart.getResponseForLayer(ecResponses, 1);
        List<DetectorResponse>  rECIN  = ECPart.getResponseForLayer(ecResponses, 4);
        List<DetectorResponse>  rECOUT = ECPart.getResponseForLayer(ecResponses, 7);
        
        if(rPCAL.size()!=1) return;
        List<DetectorParticle>  particles = new ArrayList<DetectorParticle>();
        
        DetectorParticle g = new DetectorParticle();
        g.setCross(0.0, 0.0, 0.0, 
                    rPCAL.get(0).getPosition().x(),
                    rPCAL.get(0).getPosition().y(),
                    rPCAL.get(0).getPosition().z()
                    );
        g.addResponse(rPCAL.get(0));
        
        int index_ecin  = g.getDetectorHitIndex(rECIN);
        int index_ecout = g.getDetectorHitIndex(rECOUT);
        
        if(index_ecin>=0&&rECIN.size()>0)  g.addResponse(rECIN.get(index_ecin));
        if(index_ecout>=0&&rECOUT.size()>0) g.addResponse(rECOUT.get(index_ecout));
        double energy = g.getEnergy(DetectorType.EC)/0.27;
        
        Vector3  dir = g.getCrossDir();
        dir.unit();
        /*
        System.out.println(" INDEX = " + index_ecin + "  " + index_ecout 
                + "  energy = " + g.getEnergy(DetectorType.EC)
                + "  " + energy + "  " + String.format("%8.5f %8.5f", dir.theta()*57.29,dir.phi()*57.29));
        */
        
        //System.out.println(g);
        
        Particle gamma = gen.getParticle("[22]");
        System.out.println(String.format("%8.5f %8.5f %8.5f %8.5f %8.5f %8.5f", energy,
                dir.theta()*57.29,dir.phi()*57.29,gamma.vector().p(),
                gamma.vector().theta()*57.29,gamma.vector().phi()*57.29));
        
        
    }
    
    public static void main(String[] args){
        
        ECEngine   engine = new ECEngine();
        EvioSource reader = new EvioSource();
        
        //String evioPath = "/Users/colesmith/coatjava/data/pizero/";
        String evioPath = "/Users/gavalian/Work/Software/Release-9.0/Distribution/clas12detector-ec/";
        // GEMC file: 10k 2.0 GeV pizeros thrown at 25 deg into Sector 2 using GEMC 2.4 geometry
        // JLAB: evioPath = "/lustre/expphy/work/hallb/clas12/lcsmith/clas12/forcar/gemc/evio/";
      
        if (args.length == 1) {
            reader.open(evioPath+"fc-pizero-10k-s2-25deg-oldgeom.evio");
        } else {
            String inputFile = args[1];
            reader.open(inputFile);
        }
                
        engine.init();
        engine.setStripThresholds(10,9,8);
        engine.setPeakThresholds(18,20,15);
        engine.setClusterCuts(7,15,20);
        
        H1F h1 = new H1F("Invariant Mass",50,10.,200);         
        h1.setOptStat(Integer.parseInt("1100")); h1.setTitleX("Pizero Invariant Mass (MeV)");
        H1F h2 = new H1F("Energy Asymmetry",50,-1.0,1.0);      
        h2.setOptStat(Integer.parseInt("1100")); h2.setTitleX("X:(E1-E2)/(E1+E2)");
        H1F h3 = new H1F("Pizero Energy Error",50,-500.,500.); 
        h3.setOptStat(Integer.parseInt("1100")); h3.setTitleX("Pizero Energy Error (MeV)");
        H1F h4 = new H1F("Pizero Theta Error",50,-1.,1.);      
        h4.setOptStat(Integer.parseInt("1100")); h4.setTitleX("Pizero Theta Error (deg)");
        
        while(reader.hasEvent()){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            //engine.debug = true;
            engine.processDataEvent((EvioDataEvent) event);      
            
            ECPart                        part = new ECPart(); part.geom = "2.4";
            GenericKinematicFitter      fitter = new GenericKinematicFitter(11);
            PhysicsEvent                   gen = fitter.getGeneratedEvent((EvioDataEvent)event);
            List<DetectorResponse>  ecClusters = part.readEC((EvioDataEvent)event);           
            double                     invmass = 1e3*Math.sqrt(part.getTwoPhoton(gen, ecClusters));
            
            h1.fill((float)invmass,1.);                          //Two-photon invariant mass
            if (invmass>60 && invmass<200) {
                h2.fill((float)part.X);                          //Pizero energy asymmetry
                h3.fill((float)1e3*(Math.sqrt(part.tpi2)-2.0));  //Pizero total energy error
                h4.fill(Math.acos(part.cpi0)*180/3.14159-25.);   //Pizero theta angle error
            }
        }
        
        JFrame frame = new JFrame("Pizero Reconstruction");
        frame.setSize(800,800);
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.divide(2,2);
        canvas.cd(0); canvas.draw(h1);
        canvas.cd(1); canvas.draw(h2);
        canvas.cd(2); canvas.draw(h3);
        canvas.cd(3); canvas.draw(h4);
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);     
    }
}
