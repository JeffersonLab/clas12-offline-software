/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.process;

import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.Vector3;

/**
 *
 * @author gavalian
 */
public class KinematicsFactory {
    
    public static double MP = 0.93827;
    /**
     * returns mu for given kinematics variables
     * @param q2 transferred momentum
     * @param xb Bjorken x
     * @return 
     */
    public static double getMu(double q2, double xb){
        //double pmass = PDGDatabase.getParticleMass(2212);
        return q2/(2.0*0.93827*xb);
    }
    /**
     * returns E' for given energy and kinematics variables.
     * @param E initial electron energy
     * @param q2 transferred momentum to proton
     * @param xb Bjorken x
     * @return 
     */
    public static double getEprime(double E, double q2, double xb){
        return E - KinematicsFactory.getMu(q2, xb);
    }
    /**
     * returns scattered electron angle for given kinematics
     * @param E initial electron energy
     * @param q2 transfered 4 momentum
     * @param xb Bjorken x
     * @return 
     */
    public static double getTheta(double E, double q2, double xb){
        double eprime = KinematicsFactory.getEprime(E, q2, xb);
        double left_side = q2/(4.0*E*eprime);
        double value = Math.sqrt(left_side);
        return 2.0*Math.asin(value);
    }
    
    public static double getEpsilon(double q2, double xb){
        return 2.0*xb*0.93827/q2;
    }
    
    public static double getQ2(double E, double x, double y){
        return 2.0*MP*E*y*x;
    }
    
    /**
     * returns an electron for given beam energy, q2 and xb
     * @param E
     * @param q2
     * @param xb
     * @return 
     */
    public static Particle getElectron(double E, double q2, double xb){        
        double eprime = KinematicsFactory.getEprime( E, q2, xb);
        double theta  = KinematicsFactory.getTheta(  E, q2, xb);
        //System.out.println("E-prime = " + eprime + "  theta = " + Math.toDegrees(theta));
        Vector3  vec = new Vector3();
        vec.setMagThetaPhi(eprime, theta, 0.0);
        return new Particle(11,vec.x(),vec.y(),vec.z(),0.0,0.0,0.0);
    }
    /**
     * returns magnetic moment of the proton
     * @param del2
     * @return 
     */
    public static double getGM_p(double del2){
        double denom = (1.0-del2/(0.84*0.84));
        double dipol = 1.0/(denom*denom);
        return (1.0+1.79)*dipol;
    }
    /**
     * returns magnetic moment for neutron
     * @param del2
     * @return 
     */
    public static double getGM_n(double del2){
        double denom = (1.0-del2/(0.84*0.84));
        double dipol = 1.0/(denom*denom);
        return -1.91*dipol;
    }
    /**
     * returns electric moment of neutron, it's
     * always 0, unless something has changed since
     * writing of this code.
     * @param del2
     * @return 
     */
    public static double getGE_n(double del2){
        return 0;
    }
    /**
     * returns electric moment of the proton
     * @param del2
     * @return 
     */
    public static double getGE_p(double del2){
        double denom = (1.0-del2/(0.84*0.84));
        double dipol = 1.0/(denom*denom);
        return dipol;
    }
    /**
     * Returns F1 function for U quark
     * @param del2
     * @return 
     */
    public static double getF1_u(double del2){
        double delim = del2/(4.0*MP*MP);
        double f1pn_1 = (KinematicsFactory.getGE_p(del2) - 
                delim*KinematicsFactory.getGM_p(del2))/(1.0-delim);
        double f1pn_2 = (KinematicsFactory.getGE_n(del2) - 
                delim*KinematicsFactory.getGM_n(del2))/(1.0-delim);
        return 2.0*f1pn_1 + f1pn_2; 
    }
    /**
     * returns F1 function for d quark
     * @param del2
     * @return 
     */
    public static double getF1_d(double del2){
        double delim = del2/(4.0*MP*MP);
        double f1pn_1 = (KinematicsFactory.getGE_p(del2) - 
                delim*KinematicsFactory.getGM_p(del2))/(1.0-delim);
        double f1pn_2 = (KinematicsFactory.getGE_n(del2) - 
                delim*KinematicsFactory.getGM_n(del2))/(1.0-delim);
        return 2.0*f1pn_2 + f1pn_1;
    }
}
