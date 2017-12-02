package cnuphys.bayes;

/**
 * A base class for exceptions in the Bayes package
 * @author heddle
 *
 */
public class BayesException extends Exception {
	
	/**
	 * A general exception in the Bayes package
	 * @param message some descriptive text of the specific problem
	 */
	public BayesException(String message)  {
		super(message);
	}

}
