package org.jlab.rec.rtpc.KalmanFilter;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class Hit implements Comparable<Hit> {

  private final double r, phi, z, ADC, ADCTot;

  public Hit(double r, double phi, double z, double ADC, double ADCTot) {
    this.r = r;
    this.phi = phi;
    this.z = z;
    this.ADC = ADC;
    this.ADCTot = ADCTot;
  }

  public double r() {
    return r;
  }

  public double ADC() {
    return ADC;
  }

  public double x() {
    return this.r * Math.cos(this.phi);
  }

  public double y() {
    return this.r * Math.sin(this.phi);
  }

  public double z() {
    return this.z;
  }

  public RealVector get_Vector() {
    return new ArrayRealVector(new double[] {this.r, this.phi, this.z});
  }

  public RealMatrix get_MeasurementNoise() {
    // double alpha = 0.01 // for simulation
    double alpha = 0.01;
    double relativeADC = this.ADC / this.ADCTot;
    double weight = alpha / relativeADC;

    double sigma_r = 70.0 / this.r * 2;
    double sigma_phi = Math.toRadians(2);
    double sigma_z = 1;

    return new Array2DRowRealMatrix(
            new double[][] {
              {sigma_r * sigma_r, 0, 0},
              {0, sigma_phi * sigma_phi, 0},
              {0, 0, sigma_z * sigma_z}
            })
        .scalarMultiply(weight);
  }

  private String outputMatrix(RealMatrix A) {
    final int nRows = A.getRowDimension();
    final int nCols = A.getColumnDimension();

    StringBuilder result = new StringBuilder();
    // dimensions
    result.append(String.format("%dx%d\n", nRows, nCols));

    // col headers
    result.append("\t");
    for (int j = 0; j < nCols; ++j) {
      result.append(String.format("[,%d] ", j));
    }
    result.append("\n");

    for (int i = 0; i < nRows; ++i) {
      result.append(String.format("[%d,] ", i));
      for (int j = 0; j < nCols; ++j) {
        result.append(String.format("%f, ", A.getEntry(i, j)));
      }

      if (i != nRows - 1) {
        result.append("\n");
      }
    }

    return result.toString();
  }

  @Override
  public int compareTo(Hit o) {
    return Double.compare(r, o.r());
  }

  @Override
  public String toString() {
    return "Hit{" + "r=" + this.r + ", phi=" + this.phi + ", z=" + this.z + '}';
  }
}
