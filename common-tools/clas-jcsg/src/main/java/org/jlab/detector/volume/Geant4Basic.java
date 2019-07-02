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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jlab.detector.hits.DetHit;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.geometry.prim.Line3d;

/**
 *
 * @author kenjo
 */
public abstract class Geant4Basic {

    protected String volumeName;
    protected String volumeType;
    protected int[] rgb = {0x00, 0x00, 0xff};
    protected boolean sensitivity = false;
    protected boolean abstraction = false;

    protected CSG volumeCSG;
    protected final Primitive volumeSolid;

    private final Transform volumeTransformation = Transform.unity();

    protected String rotationOrder = "xyz";
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

    protected final List<Measurement> getDimensions() {
        return volumeDimensions;
    }

    public final void makeSensitive() {
        sensitivity = true;
    }

    public boolean isSensitive() {
        return sensitivity;
    }

    public final void makeAbstract() {
        abstraction = true;
    }

    public boolean isAbstract() {
        return abstraction;
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

    public Vector3d getGlobalPosition() {
        return getGlobalTransform().transform(new Vector3d(0, 0, 0));
    }

    public Vector3d getLocalPosition() {
        return volumeTransformation.transform(new Vector3d(0, 0, 0));
    }

    public String getLocalRotationOrder() {
        return rotationOrder;
    }

    public double[] getLocalRotation() {
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

    private final void updateCSGtransformation() {
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

    public Geant4Basic scale(double scalefactor) {
        volumeTransformation.prepend(Transform.unity().scale(scalefactor));
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

    
    private static class SmartFormat {
        private static DecimalFormat largef = new DecimalFormat("#.####");
        private static DecimalFormat smallf = new DecimalFormat("#.#######");
        public static String format(double num) {
            return (num < 1e-2) ? smallf.format(num) : largef.format(num);
        }
    }

    public String gemcString() {
        StringBuilder str = new StringBuilder();

        if (motherVolume == null) {
            str.append(String.format("%18s | |", volumeName));
        } else {
            str.append(String.format("%18s | %8s | ", volumeName, motherVolume.getName()));
        }

        Vector3d pos = getLocalPosition();
        str.append(String.format("%s*%s %s*%s %s*%s | ",
                SmartFormat.format(pos.x), Length.unit(), SmartFormat.format(pos.y), Length.unit(), SmartFormat.format(pos.z), Length.unit()));

        if (rotationValues[0] == 0 && rotationValues[1] == 0 && rotationValues[2] == 0) {
            str.append("0 0 0 ");
        } else {
            if (!"zyx".equals(rotationOrder)) {
                str.append(String.format("ordered: %s ", new StringBuilder(this.rotationOrder).reverse().toString()));
            }
            for (int irot = 0; irot < rotationValues.length; irot++) {
                str.append(SmartFormat.format(Math.toDegrees(rotationValues[rotationValues.length - irot - 1]))).append("*deg ");
            }
        }
        str.append(String.format("| %8s | ", this.getType()));
        volumeDimensions.stream()
                .forEach(dim -> str.append(SmartFormat.format(dim.value)).append("*").append(dim.unit).append(" "));
        str.append(" | ");

        int[] ids = this.getId();
        for (int id : ids) {
            str.append(String.format("%4d", id));
        }

        return str.toString();
    }

    public String gemcStringRecursive() {
        StringBuilder str = new StringBuilder();
        if (!isAbstract()) {
            str.append(gemcString());
            str.append(System.getProperty("line.separator"));
        }

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

    public List<Geant4Basic> getAllVolumes() {
        List<Geant4Basic> volList = new ArrayList<>(Arrays.asList(this));

        volList.addAll(children.stream()
                .flatMap(child -> child.getAllVolumes().stream())
                .collect(Collectors.toList()));

        return volList;
    }

    //returns original component before transformation (important for STL volumes)
    //use it to export volumes to STL files before transformation for GEMC
    public Primitive getPrimitive() {
        return volumeSolid;
    }

    public List<DetHit> getIntersections(Straight line) {
        if (children.isEmpty()) {
            return getIntersectedHits(line);
        } else {
            List<Vector3d> dots = volumeCSG.getIntersections(line.toLine());
            if (dots.size() > 0) {
                return children.stream()
                        .flatMap(child -> child.getIntersections(line).stream())
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    protected List<DetHit> getIntersectedHits(Straight line) {
        List<DetHit> hits = new ArrayList<>();
        if (this.isSensitive()) {

            //mainly for complicated shapes
            //if the number of polygons is large,
            //it's more efficient to test the bounds on intersections
            //before testing all polygons involved
            if (volumeCSG.getPolygons().size() > 20) {
                List<Vector3d> dots = volumeCSG.getBounds().toCSG().getIntersections(line.toLine());
                if (dots.isEmpty()) {
                    return hits;
                }
            }

            List<Vector3d> dots = volumeCSG.getIntersections(line);

            for (int ihit = 0; ihit < dots.size() / 2; ihit++) {
                DetHit hit = new DetHit(dots.get(ihit * 2), dots.get(ihit * 2 + 1), this);
                hits.add(hit);
            }
        }
        return hits;
    }

    public Line3d getLineX() {
        throw new UnsupportedOperationException("Not implemented for that particular volume class, YET...");
        //return new Line3d(new Vector3d(0, 0, 0), new Vector3d(0, 0, 0));
    }

    public Line3d getLineY() {
        throw new UnsupportedOperationException("Not implemented for that particular volume class, YET...");
        //return new Line3d(new Vector3d(0, 0, 0), new Vector3d(0, 0, 0));
    }

    public Line3d getLineZ() {
        throw new UnsupportedOperationException("Not implemented for that particular volume class, YET...");
        //return new Line3d(new Vector3d(0, 0, 0), new Vector3d(0, 0, 0));
    }

    /**
     * @author pdavies
     */
    public void setPosition(double x, double y, double z) {
        setPosition(new Vector3d(x, y, z));
    }

    /**
     * @author pdavies
     */
    public void setPosition(Vector3d position) {
        this.translate(position.minus(this.getLocalPosition()));
    }

    public Vector3d getLocal(Vector3d vec) {
        Transform trans = getGlobalTransform().invert();
        return trans.transform(vec.clone());
        //return new Line3d(new Vector3d(0, 0, 0), new Vector3d(0, 0, 0));
    }
}
