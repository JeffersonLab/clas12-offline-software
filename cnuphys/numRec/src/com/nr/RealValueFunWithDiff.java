package com.nr;

public interface RealValueFunWithDiff extends RealValueFun {

    public void df(double[] x, double[] df);
}
