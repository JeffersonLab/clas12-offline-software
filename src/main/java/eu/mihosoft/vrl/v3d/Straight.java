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
public abstract class Straight {

    private final Vector3d origin, end;

    public Straight(Vector3d origin, Vector3d end) {
        this.origin = origin.clone();
        this.end = end.clone();
    }
    
    public Vector3d origin(){
        return origin;
    }
    
    public Vector3d end(){
        return end;
    }
    
    public Vector3d diff(){
        return end.minus(origin);
    }
    
    public abstract boolean contains(double parametricT);
}
