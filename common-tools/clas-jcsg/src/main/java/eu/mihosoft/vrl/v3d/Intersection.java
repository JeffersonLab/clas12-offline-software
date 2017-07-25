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
public class Intersection {

    public Vector3d pos;
    public Vector3d normal;
    private boolean exist;
    private double parametricT;

    public Intersection() {
        exist = false;
    }

    public boolean isPresent() {
        return exist;
    }

    public void setPosition(Vector3d pos) {
        exist = true;
        this.pos = pos.clone();
    }

    public void setPosition(Vector3d pos, double parametricT) {
        setPosition(pos);
        this.parametricT = parametricT;
    }
    
    public double getParametricT(){
        return parametricT;
    }

    public void setNormal(Vector3d normal){
        this.normal = new Vector3d(normal);
    }
}
