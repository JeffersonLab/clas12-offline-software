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
    
    public static Particle getElectron(double E, double q2, double xb){        
        double eprime = KinematicsFactory.getEprime( E, q2, xb);
        double theta  = KinematicsFactory.getTheta(  E, q2, xb);
        System.out.println("E-prime = " + eprime + "  theta = " + Math.toDegrees(theta));
        Vector3  vec = new Vector3();
        vec.setMagThetaPhi(eprime, theta, 0.0);
        return new Particle(11,vec.x(),vec.y(),vec.z(),0.0,0.0,0.0);
    }
}
