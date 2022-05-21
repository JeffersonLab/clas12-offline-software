package org.jlab.rec.rtpc.hit;

import java.util.ArrayList;

public class HitVector {

  private int pad;
  private double z;
  private double phi;
  private double time;
  private double adc;

  private ArrayList<HitVector> listRawHit;

  public HitVector() {
    pad = 0;
    z = 0;
    phi = 0;
    time = 0;
    adc = 0;
  }

  public HitVector(int padnum, double hittime, double padadc) {
    pad = padnum;
    z = 0;
    phi = 0;
    time = hittime;
    adc = padadc;
  }

  public HitVector(int padnum, double zpad, double phipad, double hittime, double padadc) {
    pad = padnum;
    z = zpad;
    phi = phipad;
    time = hittime;
    adc = padadc;
  }

  public HitVector(HitVector hit) {
    this.pad = hit.pad();
    this.z = hit.z();
    this.phi = hit.phi();
    this.time = hit.time();
    this.adc = hit.adc();
    this.listRawHit = new ArrayList<>();
  }

  public void setpad(int padnum) {
    pad = padnum;
  }

  public void setz(double zpad) {
    z = zpad;
  }

  public void setphi(double phipad) {
    phi = phipad;
  }

  public void settime(double hittime) {
    time = hittime;
  }

  public void setadc(double padadc) {
    adc = padadc;
  }

  public void setListRawHit(ArrayList<HitVector> list) {
    this.listRawHit = new ArrayList<>();
    for (HitVector hit : list) {
      this.listRawHit.add(new HitVector(hit));
    }
  }

  public int pad() {
    return pad;
  }

  public double z() {
    return z;
  }

  public double phi() {
    return phi;
  }

  public double time() {
    return time;
  }

  public double adc() {
    return adc;
  }

  public ArrayList<HitVector> listRawHit() {
    ArrayList<HitVector> list = new ArrayList<>();
    for (HitVector hit : this.listRawHit) {
      list.add(new HitVector(hit));
    }
    return list;
  }

  @Override
  public String toString() {
    return "HitVector{"
        + "pad="
        + pad
        + ", z="
        + z
        + ", phi="
        + phi
        + ", time="
        + time
        + ", adc="
        + adc
        + '}';
  }
}
