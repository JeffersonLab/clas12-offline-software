/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

/**
 *
 * @author kenjo
 */
public class Ray3d extends Straight{
    public Ray3d(Vector3d origin, Vector3d end){
        super(origin, end);
    }
    
    public Ray3d(Straight line){
        super(line.origin(), line.end());
    }
    
    @Override
    public boolean contains(double parametricT){
        System.out.println("it's Ray");
        return parametricT>=0;
    }
}
