/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patternrec;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.tracking.patternrec.CircleHoughTrans;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author ziegler
 */
public class TestCHT {
    
    public static void main(String arg[]) {
        double p = 1.0;
        double phi = Math.toRadians(18);
        double theta = Math.toRadians(85.);
        
        Helix H = new Helix(0, 0, 0, p*Math.sin(theta)*Math.cos(phi), 
                p*Math.sin(theta)*Math.sin(phi), p*Math.cos(theta),
            -1, 5);
        //System.out.println(H._yc);
        //System.out.println(H.getHelixPointAtR(66.55));
        //System.out.println(H.getHelixPointAtPlane(68.5, 1.5, 56.2, 39.3, 
        //    10.));
        
        List<Double> X = new ArrayList<Double>();
        List<Double> Y = new ArrayList<Double>();
        
        CircleHoughTrans cht = new CircleHoughTrans();
        Point3D h = new Point3D(0,0,0);
        for(int i = 0; i < 6; i++) {
            h = H.getHelixPointAtR(65+(double)i * 5);
            X.add(h.x());
            Y.add(h.y());
        }
        
        cht.findCircles(X, Y);
        
        for(int i = 0; i < cht.set.size(); i++) {
            for(int j = 0; j < cht.set.get(i).size(); j++) {
                float x = (float) cht.set.get(i).get(j).getX();
                float y = (float) cht.set.get(i).get(j).getY();
                System.out.println(" x = "+x+" y = "+y);
            }
        }
    }
}
