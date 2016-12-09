package org.jlab.service.pid;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.physics.Vector3;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.clas.detector.DetectorEvent;


/**
 *
 * @author jnewton
 */
public class TIDResult {
    

   private int score;
   private TIDExamination tidexam = new TIDExamination();


    
    public TIDResult(){
        
    }
    

    public void   setTIDExamination(TIDExamination exam){this.tidexam = exam;}
    public void   setScore(int x ){this.score = x;} 

    
    public int getScore(){return this.score;}
    public TIDExamination getTIDExamination(){return this.tidexam;}

    
}
