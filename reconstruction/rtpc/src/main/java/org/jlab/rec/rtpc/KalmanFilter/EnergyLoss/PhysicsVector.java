package org.jlab.rec.rtpc.KalmanFilter.EnergyLoss;


import java.util.Arrays;
import java.util.Vector;

enum PhysicsVectorType {
    T_PhysicsFreeVector,
    T_PhysicsLinearVector,
    T_PhysicsLogVector
}

public class PhysicsVector {

    private final boolean useSpline;
    protected PhysicsVectorType type = PhysicsVectorType.T_PhysicsFreeVector;
    protected Vector<Double> binVector = new Vector<>(1);      // energy
    protected Vector<Double> dataVector = new Vector<>(1);     // crossection/energyloss
    protected Vector<Double> secDerivative = new Vector<>(1);  // second derivatives
    protected double edgeMin = 0.0;  // Energy of first point
    protected double edgeMax = 0.0;  // Energy of the last point
    protected double invdBin = 0.0;  // 1/Bin width for linear and log vectors
    protected double logemin = 0.0;  // used only for log vector
    protected int idxmax = 0;
    protected int numberOfNodes = 0;

    public PhysicsVector(boolean spline) {
        useSpline = spline;
    }

    private static int lower_bound(Object[] array, Object key) {

        int index = Arrays.binarySearch(array, key);

        if (index < 0) {
            return Math.abs(index) - 1;
        } else {
            while (index > 0) {
                if (array[index - 1] == key)
                    index--;
                else
                    return index;
            }
            return index;
        }
    }

    public int ComputeLogVectorBin(double logenergy) {
        return Math.min((int) ((logenergy - logemin) * invdBin), idxmax);
    }

    public double Value(double e) {
        double res;
        if (e > edgeMin && e < edgeMax) {
            int idx = GetBin(e);
            res = Interpolation(idx, e);
        } else if (e <= edgeMin) {
            res = dataVector.get(0);
        } else {
            res = dataVector.get(numberOfNodes - 1);
        }
        return res;
    }

    public double Energy(int index) {
        return binVector.get(index);
    }

    protected void Initialise() {
        idxmax = (int) (numberOfNodes - 2);
        if (0 < numberOfNodes) {
            edgeMin = binVector.get(0);
            edgeMax = binVector.get(numberOfNodes - 1);
        }
    }

    private double Interpolation(int idx, double e) {
        // perform the interpolation
        double x1 = binVector.get(idx);
        double dl = binVector.get(idx + 1) - x1;

        double y1 = dataVector.get(idx);
        double dy = dataVector.get(idx + 1) - y1;

        // note: all corner cases of the previous methods are covered and eventually
        //       gives b=0/1 that results in y=y0\y_{N-1} if e<=x[0]/e>=x[N-1] or
        //       y=y_i/y_{i+1} if e<x[i]/e>=x[i+1] due to small numerical errors
        double b = (e - x1) / dl;

        double res = y1 + b * dy;

        if (useSpline)  // spline interpolation
        {
            double c0 = (2.0 - b) * secDerivative.get(idx);
            double c1 = (1.0 + b) * secDerivative.get(idx + 1);
            res += (b * (b - 1.0)) * (c0 + c1) * (dl * dl * (1.0 / 6.0));
        }

        return res;
    }

    private int GetBin(double e) {
        int bin;
        switch (type) {
            case T_PhysicsLogVector:
                bin = ComputeLogVectorBin(Math.log(e));
                break;

            case T_PhysicsLinearVector:
                bin = Math.min((int) ((e - edgeMin) * invdBin), idxmax);
                break;

            default:
                // Bin location proposed by K.Genser (FNAL)
                bin = lower_bound(binVector.toArray(), e) - 1;
        }
        return bin;
    }
}
