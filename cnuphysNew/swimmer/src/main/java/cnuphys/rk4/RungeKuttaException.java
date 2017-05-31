package cnuphys.rk4;

@SuppressWarnings("serial")
public class RungeKuttaException extends Exception {

	public RungeKuttaException(String message) {
		super("[Runge Kutta] " + message);
	}
}
