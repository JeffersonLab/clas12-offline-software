/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.hits;

import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.volume.Geant4Basic;

/**
 *
 * @author kenjo
 */
public class DetHit {

    protected Vector3d origin = new Vector3d(0, 0, 0);
    protected Vector3d end = new Vector3d(0, 0, 0);
    protected int[] detId;
    protected Geant4Basic detectorComponent;

    public DetHit() {

    }

    public DetHit(int[] id) {
        setId(id);
    }

    public DetHit(Vector3d origin, Vector3d end, int[] id) {
        this.origin.set(origin);
        this.end.set(end);
        setId(id);
    }

    public DetHit(Vector3d origin, Vector3d end, Geant4Basic component) {
        this(origin, end, component.getId());
        detectorComponent = component;
    }

    public Vector3d origin() {
        return origin;
    }

    public Vector3d end() {
        return end;
    }

    public Vector3d mid() {
        return origin.plus(end).dividedBy(2);
    }
    
    public int[] getId() {
        return detId;
    }

    public final void setOrigin(Vector3d origin) {
        this.origin.set(origin);
    }

    public final void setEnd(Vector3d end) {
        this.end.set(end);
    }

    public final void setId(int[] id) {
        this.detId = new int[id.length];
        System.arraycopy(id, 0, detId, 0, id.length);
    }

    public double length() {
        return end.minus(origin).magnitude();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Detector id: ");
        for (int id : detId) {
            str.append(id);
        }
        str.append(System.getProperty("line.separator"));

        return str.toString();
    }

    public Geant4Basic getComponent() {
        return detectorComponent;
    }
    
    public Vector3d getLocal(Vector3d vec){
        Transform trans = detectorComponent.getGlobalTransform().invert();
        return trans.transform(vec.clone());
    }
}
