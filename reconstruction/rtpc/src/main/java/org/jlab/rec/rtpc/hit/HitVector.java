package org.jlab.rec.rtpc.hit;

import java.util.ArrayList;

public class HitVector {

  private int pad;
  private double z;
  private double phi;
  private double time;
  private double adc;
  private int flag;
  private ArrayList<HitVector> listRawHit;

  public HitVector() {
    pad = 0;
    z = 0;
    phi = 0;
    time = 0;
    adc = 0;
    flag = 0;
  }

  public HitVector(int padnum, double hittime, double padadc) {
    pad = padnum;
    z = 0;
    phi = 0;
    time = hittime;
    adc = padadc;
    flag = 0;
  }

  public HitVector(int padnum, double zpad, double phipad, double hittime, double padadc) {
    pad = padnum;
    z = zpad;
    phi = phipad;
    time = hittime;
    adc = padadc;
    flag = 0;
  }

  public HitVector(HitVector hit) {
    this.pad = hit.pad();
    this.z = hit.z();
    this.phi = hit.phi();
    this.time = hit.time();
    this.adc = hit.adc();
    this.listRawHit = new ArrayList<>();
    this.flag = 0;
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

  public void flagHit(int f) {
    flag = f;
  }

  public int flag() {
    return flag;
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

  public void setListRawHit(ArrayList<HitVector> list) {
    this.listRawHit = new ArrayList<>();
    for (HitVector hit : list) {
      this.listRawHit.add(new HitVector(hit));
    }
  }

  public ArrayList<HitVector> listRawHit() {
    ArrayList<HitVector> list = new ArrayList<>();
    for (HitVector hit : this.listRawHit) {
      list.add(new HitVector(hit));
    }
    return list;
  }
}
