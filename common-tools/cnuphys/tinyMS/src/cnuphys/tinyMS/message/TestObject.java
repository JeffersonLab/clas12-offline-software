package cnuphys.tinyMS.message;

import java.io.Serializable;

/**
 * This is just used to test sending and receiving a serialized object.
 * @author heddle
 *
 */

public class TestObject implements Serializable {
	private static final long serialVersionUID = -1100726909997382408L;
	private String string = "The meaning of life is: ";
	private int vi = 42;
	private double vd = 3.14159;
	private boolean vb = false;
	
	public String toString() {
		return string + vi + ".  Pi exactly equals " + vd + ": " + vb;
	}

}
