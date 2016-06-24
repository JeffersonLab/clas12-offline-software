/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.geant;

/**
 *
 * @author gavalian
 */
public interface IGeant4Volume {
    String  getName();
    String  getType();
    void      setParameters(double... pars);
    double[]  getParameters();
    double[]  getPosition();
    double[]  getRotation();
    int[]     getId();
    
    String    getRotationOrder();
    
    void setPosition(double x, double y, double z);
    void setRotation(String order, double r1, double r2, double r3);
    void setId(int... id);
}
