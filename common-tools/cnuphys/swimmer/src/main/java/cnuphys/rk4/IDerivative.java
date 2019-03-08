package cnuphys.rk4;

/**
 * This interface is where the specificity of the problem enters. Any integrator
 * such as RungeKutta will solve 1st order ODEs of the form dy/dt = f(t). The
 * vector y is often a six-D vector [x, y, z, dx/dy, dy/dt, dz/dt]. The
 * specificity of the problem is implemented by filling the dydt vector.
 * 
 * @author heddle
 *
 */
public interface IDerivative {

	/**
	 * Compute the derivatives given the value of the independent variable and the
	 * values of the function. Think of the Differential Equation as being dydt =
	 * f[y,t].
	 * 
	 * @param t    the value of the independent variable (usually t) (input).
	 * @param y    the values of the state vector (usually [x,y,z,vx,vy,vz]) at t
	 *             (input).
	 * @param dydt will be filled with the values of the derivatives at t (output).
	 */
	public void derivative(double t, double y[], double dydt[]);

}
