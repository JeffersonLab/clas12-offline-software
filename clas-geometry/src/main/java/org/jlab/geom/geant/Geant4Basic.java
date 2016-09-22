/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.geant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Transformation3D.Transform;

/**
 *
 * @author gavalian
 */
public class Geant4Basic implements IGeant4Volume {

    String volumeName = "basic_volume";
    String volumeType = "box";

    Transformation3D volumeRotation = new Transformation3D();
    Transformation3D volumeTranslation = new Transformation3D();

    String transformationOrder = "xyz";
    Point3D volumePosition = new Point3D(0.0, 0.0, 0.0);
    int[] volumeID = new int[]{};
    double[] volumeParameters = new double[]{};
    String[] volumeParUnits = new String[]{};
    String defaultUnits = "cm";

    private final List<Geant4Basic> children = new ArrayList<>();
    Geant4Basic motherVolume;

    public Geant4Basic(String name, String type, double... pars) {
        this.volumeName = name;
        this.volumeType = type;
        this.volumeParameters = new double[pars.length];
        this.volumeParUnits = new String[pars.length];
        Arrays.fill(volumeParUnits, defaultUnits);
        System.arraycopy(pars, 0, this.volumeParameters, 0, pars.length);
    }

    public String getUnits() {
        return defaultUnits;
    }

    public String getUnits(int ipar) {
        return volumeParUnits[ipar];
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

    @Override
    public String getName() {
        return this.volumeName;
    }

    @Override
    public String getType() {
        return this.volumeType;
    }

    @Override
    public void setParameters(double... pars) {
        this.volumeParameters = new double[pars.length];
        System.arraycopy(pars, 0, this.volumeParameters, 0, pars.length);
    }

    public void setParUnits(String... parUnits) {
        this.volumeParUnits = new String[parUnits.length];
        System.arraycopy(parUnits, 0, this.volumeParUnits, 0, parUnits.length);
    }

    public List<Geant4Basic> getChildren() {
        return this.children;
    }

    @Override
    public double[] getParameters() {
        return this.volumeParameters;
    }

    @Override
    public double[] getPosition() {
        return new double[]{this.volumePosition.x(), this.volumePosition.y(), this.volumePosition.z()};
    }

    @Override
    public double[] getRotation() {
        List<Transform> sequence = this.volumeRotation.transformSequence();
        double[] rotation = new double[3];
        for (int i = 0; i < sequence.size(); i++) {
            rotation[i] = sequence.get(i).getValue(0);
        }
        return rotation;
    }

    @Override
    public String getRotationOrder() {
        return this.transformationOrder;
    }

    @Override
    public int[] getId() {
        return this.volumeID;
    }

    public Transformation3D translation() {
        return this.volumeTranslation;
    }

    public Transformation3D rotation() {
        return this.volumeRotation;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.volumeTranslation.clear();
        this.volumeTranslation.translateXYZ(x, y, z);
        this.volumePosition.set(x, y, z);
    }

    @Override
    public void setRotation(String order, double r1, double r2, double r3) {
        int ro = 1;
        this.transformationOrder = order;
        this.volumeRotation.clear();
        switch (order) {
            case "xyz":
                this.volumeRotation.rotateX(r1).rotateY(r2).rotateZ(r3);
                break;
            case "xzy":
                this.volumeRotation.rotateX(r1).rotateZ(r2).rotateY(r3);
                break;
            case "yxz":
                this.volumeRotation.rotateY(r1).rotateX(r2).rotateZ(r3);
                break;
            case "yzx":
                this.volumeRotation.rotateY(r1).rotateZ(r2).rotateX(r3);
                break;
            case "zxy":
                this.volumeRotation.rotateZ(r1).rotateX(r2).rotateY(r3);
                break;
            case "zyx":
                this.volumeRotation.rotateZ(r1).rotateY(r2).rotateX(r3);
                break;
            default:
                System.out.println("[GEANT4VOLUME]---> unknown rotation " + order);
                break;
        }
    }

    @Override
    public void setId(int... id) {
        this.volumeID = new int[id.length];
        System.arraycopy(id, 0, volumeID, 0, volumeID.length);
    }

    public String gemcString() {
        StringBuilder str = new StringBuilder();
        if (this.getMother() == null) {
            str.append(String.format("%18s ", this.getName()));
        } else {
            str.append(String.format("%18s | %8s", this.getName(), this.getMother().getName()));
        }

        str.append(String.format("| %8.4f*cm %8.4f*cm %8.4f*cm",
                this.volumePosition.x(), this.volumePosition.y(), this.volumePosition.z()));
        str.append(String.format("| ordered: %s ", new StringBuilder(this.transformationOrder).reverse().toString()));
        double[] rotate = this.getRotation();
        for (int irot = 0; irot < rotate.length; irot++) {
            str.append(String.format(" %8.4f*deg ", Math.toDegrees(rotate[rotate.length - irot - 1])));
        }
        str.append(String.format("| %8s |", this.getType()));
        for (int ipar = 0; ipar < volumeParameters.length; ipar++) {
            //str.append(String.format("%12.4f*%s", volumeParameters[ipar], volumeParUnits[ipar]));
            str.append(String.format("%14.6f*%s", volumeParameters[ipar], volumeParUnits[ipar])); // pdavies 22-Sep-2016 increased precision of parameters written out to fix rounding error in gemc
        }
        str.append(" | ");
        int[] ids = this.getId();
        for (int id : ids) {
            str.append(String.format("%4d", id));
        }

        return str.toString();
    }

    public static void main(String[] args) {
        List<Geant4Basic> volumes = new ArrayList<>();
        for (int loop = 0; loop < 20; loop++) {
            Geant4Basic paddle = new Geant4Basic("paddle_" + loop, "box", 4.0, 4.0, 20 + loop * 4);
            //System.out.println("adding");
            volumes.add(paddle);
        }

        for (Geant4Basic paddle : volumes) {
            System.out.println(paddle.toString());
        }
    }
}
