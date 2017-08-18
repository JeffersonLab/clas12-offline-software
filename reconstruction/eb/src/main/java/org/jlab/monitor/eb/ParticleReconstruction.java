package org.jlab.monitor.eb;

import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.pdg.PDGParticle;
import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.RecEvent;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author gavalian
 */
public class ParticleReconstruction {
    
    public String directoryName = "";
    public GenericKinematicFitter fitter = new GenericKinematicFitter(11.0);
    public int particleId = 0;
    
    public ParticleReconstruction(TDirectory directory, int pid){
        PDGParticle p = PDGDatabase.getParticleById(pid);
        directoryName = "/reconstruction/particles/" + p.name();
        particleId = pid;
        this.initHistograms(directory);
    }
    
    public final void initHistograms(TDirectory dir){
        dir.cd();
        dir.mkdir(directoryName);
        dir.cd(directoryName);
        dir.pwd();
        
        H2F h2p   = new H2F("PRES",40,0.0,10.0,100,-0.2,0.2);
        H2F h2t   = new H2F("THRES",40,0.0,10.0,180,-5.0,5.0);
        H2F h2phi = new H2F("PHIRES",40,0.0,10.0,180,-5.0,5.0);
        
        H1F h1p   = new H1F("PRES_1D","Momentum Resolution",200,-0.2,0.2);
        H1F h1t   = new H1F("THRES_1D","Theta Resolution",180,-5.0,5.0);
        H1F h1phi = new H1F("PHIRES_1D","Phi Resolution",180,-5.0,5.0);
        
        h1p.setTitleX("(p-p^g)/p^g");
        h1t.setTitleX("#theta^g-#theta");
        h1phi.setTitleX("#phi^g-#phi");
        dir.addDataSet(h2p,h2t,h2phi);
        dir.addDataSet(h1p,h1t,h1phi);
        dir.ls();
        dir.cd();
    }
    
    public void process(DataEvent event, TDirectory directory){
        
        RecEvent recEvent = fitter.getRecEvent(event);
        int np = recEvent.getGenerated().count();
        for(int i = 0; i < np; i++){
            Particle gp = recEvent.getGenerated().getParticle(i);
            if(gp.pid()==particleId){
                Particle rp = recEvent.getMatched(i);
                if(rp.vector().p()>0){
                    //System.out.println("found event " );
                    double res_p   = (gp.vector().p()-rp.vector().p())/gp.vector().p();
                    double res_t   = (gp.vector().theta()-rp.vector().theta())*57.29;
                    double res_phi = (gp.vector().phi()-rp.vector().phi())*57.29;
                    
                    IDataSet dsP = directory.getObject(directoryName+"/PRES");
                    if(dsP != null) ( (H2F) dsP).fill(gp.vector().p(), res_p);
                    IDataSet dsT = directory.getObject(directoryName+"/THRES");
                    if(dsT != null) ( (H2F) dsT).fill(gp.vector().p(), res_t);
                    IDataSet dsF = directory.getObject(directoryName+"/PHIRES");
                    if(dsF != null) ( (H2F) dsF).fill(gp.vector().p(), res_phi);
                    
                    IDataSet dsP1 = directory.getObject(directoryName+"/PRES_1D");
                    if(dsP1 != null) ( (H1F) dsP1).fill(res_p);
                    IDataSet dsT1 = directory.getObject(directoryName+"/THRES_1D");
                    if(dsT1 != null) ( (H1F) dsT1).fill(res_t);
                    IDataSet dsF1 = directory.getObject(directoryName+"/PHIRES_1D");
                    if(dsF1 != null) ( (H1F) dsF1).fill(res_phi);
                    
                }
            } 
        }
    }
}
