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
public class Intersection{

    public Vector3d pos;
    private boolean exist;

    public Intersection() {
        exist = false;
    }

    public boolean exist() {
        return exist;
    }

    public void setPosition(Vector3d pos) {
        exist = true;
        this.pos = pos;
    }

}
