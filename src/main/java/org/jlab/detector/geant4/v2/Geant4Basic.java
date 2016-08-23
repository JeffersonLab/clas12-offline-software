/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.detector.geant4.v2.SystemOfUnits.Length;

/**
 *
 * @author kenjo
 */
public abstract class Geant4Basic {

    String volumeName;
    String volumeType;

    Transform volumeTranslation = Transform.unity();
    Transform volumeRotation = Transform.unity();

    String rotationOrder = "xyz";
    double[] rotationValues = {0.0, 0.0, 0.0};
    Vector3d volumePosition = new Vector3d(0.0, 0.0, 0.0);

    int[] volumeID = new int[]{};
    protected List<Measurement> volumeDimensions;

    private final List<Geant4Basic> children = new ArrayList<>();

    private Geant4Basic motherVolume;

    public Geant4Basic(String name, String type, Measurement... pars) {
        this.volumeName = name;
        this.volumeType = type;
        volumeDimensions = Arrays.asList(pars);
    }

    public void setName(String name) {
        this.volumeName = name;
    }

    public void setMother(Geant4Basic motherVol) {
        this.motherVolume = motherVol;
        this.motherVolume.getChildren().add(this);
    }

    public Geant4Basic getMother() {
        return this.motherVolume;
    }

    public String getName() {
        return this.volumeName;
    }

    public String getType() {
        return this.volumeType;
    }

    public List<Geant4Basic> getChildren() {
        return this.children;
    }

    public Vector3d getPosition() {
        return volumePosition;
    }

    public int[] getId() {
        return this.volumeID;
    }

    public void setPosition(double x, double y, double z) {
        volumeTranslation = Transform.unity();
        this.volumeTranslation.translate(x, y, z);
        this.volumePosition.set(x, y, z);
    }

    public void setPosition(Vector3d pos) {
        volumeTranslation = Transform.unity();
        this.volumeTranslation.translate(pos);
        this.volumePosition.set(pos);
    }

    public void setRotation(String order, double r1, double r2, double r3) {
        rotationOrder = order;
        rotationValues = new double[]{r1, r2, r3};

        volumeRotation = Transform.unity();

        switch (order) {
            case "xyz":
                this.volumeRotation.rotX(r1).rotY(r2).rotZ(r3);
                break;
            case "xzy":
                this.volumeRotation.rotX(r1).rotZ(r2).rotY(r3);
                break;
            case "yxz":
                this.volumeRotation.rotY(r1).rotX(r2).rotZ(r3);
                break;
            case "yzx":
                this.volumeRotation.rotY(r1).rotZ(r2).rotX(r3);
                break;
            case "zxy":
                this.volumeRotation.rotZ(r1).rotX(r2).rotY(r3);
                break;
            case "zyx":
                this.volumeRotation.rotZ(r1).rotY(r2).rotX(r3);
                break;
            default:
                System.out.println("[GEANT4VOLUME]---> unknown rotation " + order);
                break;
        }
    }

    public void setId(int... id) {
        this.volumeID = new int[id.length];
        System.arraycopy(id, 0, volumeID, 0, volumeID.length);
    }

    public String gemcString() {
        StringBuilder str = new StringBuilder();

        if (motherVolume == null) {
            str.append(String.format("%18s | |", volumeName));
        } else {
            str.append(String.format("%18s | %8s", volumeName, motherVolume.getName()));
        }

        str.append(String.format("| %8.4f*%s %8.4f*%s %8.4f*%s",
                this.volumePosition.x, Length.unit(),
                this.volumePosition.y, Length.unit(),
                this.volumePosition.z, Length.unit()));

        str.append(String.format("| ordered: %s ", new StringBuilder(this.rotationOrder).reverse().toString()));
        for (int irot = 0; irot < rotationValues.length; irot++) {
            str.append(String.format(" %8.4f*deg ", Math.toDegrees(rotationValues[rotationValues.length - irot - 1])));
        }

        str.append(String.format("| %8s |", this.getType()));
        volumeDimensions.stream()
                .forEach(dim -> str.append(String.format("%12.4f*%s", dim.value, dim.unit)));
        str.append(" | ");

        int[] ids = this.getId();
        for (int id : ids) {
            str.append(String.format("%4d", id));
        }

        return str.toString();
    }
    
    public String gemcStringRecursive(){
        StringBuilder str = new StringBuilder();
        str.append(gemcString());
        str.append(System.getProperty("line.separator"));

        children.stream().
                forEach(child -> str.append(child.gemcStringRecursive()));
        
        return str.toString();
    }
}
