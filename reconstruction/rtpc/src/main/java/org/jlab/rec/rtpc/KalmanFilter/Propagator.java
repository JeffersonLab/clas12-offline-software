package org.jlab.rec.rtpc.KalmanFilter;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.FileWriter;

public class Propagator {

  private final RungeKutta4 RK4;
  private final double Bz;

  public Propagator(RungeKutta4 rungeKutta4) {
    this.RK4 = rungeKutta4;
    this.Bz = rungeKutta4.Bz();
  }

  void propagate(Stepper stepper, double R, FileWriter writer) throws Exception {
    int maxNbOfStep = 1000;

    double r = stepper.r();
    if (R > 30.005 && stepper.direction) {
      stepper.h = R - r;
      stepper.is_in_drift = true;
    }
    if (R > 30.005 && !stepper.direction) stepper.h = (r - R);

    if (writer != null) writer.write("" + stepper + '\n');

    // System.out.println("R = " + R);
    // stepper.print();

    for (int nbStep = 0; nbStep < maxNbOfStep; nbStep++) {

      double previous_r = stepper.r();
      RK4.doOneStep(stepper);
      r = stepper.r();

      // stepper.print();

      if (!stepper.isStepCorrect()) throw new Exception("Step have failed !");
      if (stepper.direction) {
        if (r >= R - 1.5 * stepper.h) stepper.h = R - r;
        if (stepper.h < 1e-4) stepper.h = 1e-4;
        if (r >= R) {
          break;
        }
      } else {
        if (R == 0) {
          if (r <= R + 2 * stepper.h) stepper.h = stepper.h / 10;
          if (stepper.h < 1e-4) stepper.h = 1e-4;
          if ((previous_r - r) < 0) {
            break;
          }
        } else {
          if (r <= R + 1.5 * stepper.h) stepper.h = r - R;
          if (stepper.h < 1e-4) stepper.h = 1e-4;
          if (r <= R) {
            break;
          }
          if ((previous_r - r) < 0)
            throw new Exception("previous r < r in backward propagation !!!");
        }
      }
      if (writer != null) writer.write("" + stepper + '\n');
    }
    if (writer != null) writer.write("" + stepper + '\n');
  }

  void propagate(Stepper stepper, double R) throws Exception {
    propagate(stepper, R, null);
  }

  RealVector f(Stepper stepper, double R) throws Exception {
    propagate(stepper, R, null);
    return new ArrayRealVector(stepper.y);
  }

  RealVector f(Stepper stepper, double R, FileWriter writer) throws Exception {
    propagate(stepper, R, writer);
    return new ArrayRealVector(stepper.y);
  }

  public double Bz() {
    return this.Bz;
  }
}
