package org.jlab.clas.pdg;

/**
 *
 * @author gavalian
 */
public class PhysicsConstants {
    
    public static double speedOfLight(){
        return 29.9792458;
    }
    
    public static double massProton(){
        return 0.938272046;
    }
    
    public static double massNeutron(){
        return 0.939565379;
    }
    
    public static double massElectron(){
        return 0.000511;
    }
    
    public static double massMuon() {
        return 0.1056583755;
    }
    
    public static double massPionCharged(){
        return 0.13957018;
    }
    
    public static double massPionNeutral(){
        return 0.1349766;
    }
    
    public static double massKaonCharged(){
        return 0.49367716;
    }
    
    public static double massKaonNeutral(){
        return 0.49761424;
    }
    
    public static double getRandomGauss(double mean, double width){
        double u = 0;
        double v = 0;
        double r = 2.0;
        
        while(r<=0.0||r>1.0){
            u = Math.random()*2.0 - 1.0;
            v = Math.random()*2.0 - 1.0;
            r = u*u + v*v;
        }

        double c = Math.sqrt(-2*Math.log(r)/r);
        //System.out.println("u = " + u + "  r = " + r + " c = " + c);
        return mean + u*c*width;
    }
}
