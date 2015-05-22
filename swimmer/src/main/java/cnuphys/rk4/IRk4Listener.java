package cnuphys.rk4;

public interface IRk4Listener {

    /**
     * The integration has advanced one step
     * 
     * @param newT
     *            the new value of the independent variable (e.g., time)
     * @param newY
     *            the new value of the dependent variable, e.g. often a vector
     *            with six elements: x, y, z, vx, vy, vz
     * @param h
     *            the stepsize used for this advance
     */
    public void nextStep(double newT, double newY[], double h);
}
