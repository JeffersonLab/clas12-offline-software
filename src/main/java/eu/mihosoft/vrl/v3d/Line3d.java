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
public class Line3d {

    private Vector3d origin, end;

    public Line3d(Vector3d origin, Vector3d end) {
        this.origin = origin;
        this.end = end;
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
}
