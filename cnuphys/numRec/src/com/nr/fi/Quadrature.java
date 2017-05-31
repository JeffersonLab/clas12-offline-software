package com.nr.fi;

public abstract class Quadrature {
    /**
     * Current level of refinement.
     */
    int n;

    /**
     * The function next() must be defined in the derived class.
     * 
     * @return Returns the value of the integral at the nth stage of refinement.
     */
    public abstract double next();

}
