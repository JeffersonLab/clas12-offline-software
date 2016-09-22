/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.volume;

import org.jlab.detector.units.Measurement;
import eu.mihosoft.vrl.v3d.CSG;
import org.jlab.geometry.prim.Straight;
import eu.mihosoft.vrl.v3d.Primitive;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jlab.detector.hits.DetHit;
import org.jlab.detector.units.SystemOfUnits.Length;

/**
 *
 * @author kenjo
 */
public abstract class Geant4Basic {

    String volumeName;
    String volumeType;

    protected CSG volumeCSG;
    protected final Primitive volumeSolid;

    private final Transform volumeTransformation = Transform.unity();

    String rotationOrder = "xyz";
    double[] rotationValues = {0.0, 0.0, 0.0};

    int[] volumeId = new int[]{};
    protected List<Measurement> volumeDimensions = new ArrayList<>();

    private final List<Geant4Basic> children = new ArrayList<>();

    private Geant4Basic motherVolume;

    protected Geant4Basic(Primitive volumeSolid) {
        this.volumeSolid = volumeSolid;
        updateCSGtransformation();
    }

    protected final void setDimensions(Measurement... pars) {
        volumeDimensions = Arrays.asList(pars);
    }

    public final List<Measurement> getDimensions(){
        return volumeDimensions;
    }

    public final void setName(String name) {
        this.volumeName = name;
    }

    protected final void setType(String type) {
        this.volumeType = type;
    }

    public void setMother(Geant4Basic motherVol) {
        this.motherVolume = motherVol;
        this.motherVolume.getChildren().add(this);

        updateCSGtransformation();
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

    public Vector3d getLocalPosition() {
        return volumeTransformation.transform(new Vector3d(0, 0, 0));
    }

    public String getLocalRotationOrder(){
        return rotationOrder;
    }

    public double[] getLocalRotation(){
        return rotationValues;
    }

    public int[] getId() {
        return this.volumeId;
    }

    public Transform getLocalTransform() {
        return volumeTransformation;
    }

    public Transform getGlobalTransform() {
        Transform globalTransform = Transform.unity();
        if (motherVolume != null) {
            globalTransform.apply(motherVolume.getGlobalTransform());
        }
        globalTransform.apply(volumeTransformation);

        return globalTransform;
    }

    protected final void updateCSGtransformation() {
        children.stream()
                .forEach(child -> child.updateCSGtransformation());

        if (volumeSolid != null) {
            volumeCSG = volumeSolid.toCSG().transformed(getGlobalTransform());
        }
    }

    public Geant4Basic translate(double x, double y, double z) {
        volumeTransformation.prepend(Transform.unity().translate(x, y, z));
        updateCSGtransformation();

        return this;
    }

    public Geant4Basic translate(Vector3d pos) {
        return translate(pos.x, pos.y, pos.z);
    }

    public Geant4Basic rotate(String order, double r1, double r2, double r3) {
        rotationOrder = order;
        rotationValues = new double[]{r1, r2, r3};

        Transform volumeRotation = Transform.unity();

        switch (order) {
            case "xyz":
                volumeRotation.rotZ(r3).rotY(r2).rotX(r1);
                break;
            case "xzy":
                volumeRotation.rotY(r3).rotZ(r2).rotX(r1);
                break;
            case "yxz":
                volumeRotation.rotZ(r3).rotX(r2).rotY(r1);
                break;
            case "yzx":
                volumeRotation.rotX(r3).rotZ(r2).rotY(r1);
                break;
            case "zxy":
                volumeRotation.rotY(r3).rotX(r2).rotZ(r1);
                break;
            case "zyx":
                volumeRotation.rotX(r3).rotY(r2).rotZ(r1);
                break;
            default:
                System.out.println("[GEANT4VOLUME]---> unknown rotation " + order);
                break;
        }

        volumeTransformation.prepend(volumeRotation);
        updateCSGtransformation();

        return this;
    }

    public void setId(int... id) {
        this.volumeId = new int[id.length];
        System.arraycopy(id, 0, volumeId, 0, volumeId.length);
    }

    public String gemcString() {
        StringBuilder str = new StringBuilder();

        if (motherVolume == null) {
            str.append(String.format("%18s | |", volumeName));
        } else {
            str.append(String.format("%18s | %8s | ", volumeName, motherVolume.getName()));
        }

        Vector3d pos = getLocalPosition();
        str.append(pos.x + "*" + Length.unit() + " "
                + pos.y + "*" + Length.unit() + " "
                + pos.z + "*" + Length.unit() + " | ");

        if(!"zyx".equals(rotationOrder)){
            str.append(String.format("ordered: %s ", new StringBuilder(this.rotationOrder).reverse().toString()));
        }
        for (int irot = 0; irot < rotationValues.length; irot++) {
            str.append(Math.toDegrees(rotationValues[rotationValues.length - irot - 1])).append("*deg ");
        }

        str.append(String.format("| %8s | ", this.getType()));
        volumeDimensions.stream()
                .forEach(dim -> str.append(dim.value).append("*").append(dim.unit).append(" "));
        str.append(" | ");

        int[] ids = this.getId();
        for (int id : ids) {
            str.append(String.format("%4d", id));
        }

        return str.toString();
    }

    public String gemcStringRecursive() {
        StringBuilder str = new StringBuilder();
        str.append(gemcString());
        str.append(System.getProperty("line.separator"));

        children.stream().
                forEach(child -> str.append(child.gemcStringRecursive()));

        return str.toString();
    }

    public final CSG toCSG() {
        return volumeCSG;
    }

    public List<Geant4Basic> getComponents() {
        if (children.isEmpty()) {
            return Arrays.asList(this);
        }

        return children.stream()
                .flatMap(child -> child.getComponents().stream())
                .collect(Collectors.toList());
    }

    public List<DetHit> getIntersections(Straight line) {
        List<DetHit> hits = new ArrayList<>();

        if (children.isEmpty()) {
            List<Vector3d> dots = volumeCSG.getIntersections(line);

            for (int ihit = 0; ihit < dots.size() / 2; ihit++) {
                DetHit hit = new DetHit(dots.get(ihit * 2), dots.get(ihit * 2 + 1), this);
                hits.add(hit);
            }
        } else {
            List<Vector3d> dots = volumeCSG.getIntersections(line.toLine());
            if (dots.size() > 0) {
                return children.stream()
                        .flatMap(child -> child.getIntersections(line).stream())
                        .collect(Collectors.toList());
            }
        }

        return hits;
    }

}
