package basic;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

public class BasicManager3D {

	//the singleton
	private static BasicManager3D _instance;
	
	//the profile
	private final GLProfile _glProfile;
	
	//the capabilities
	private GLCapabilities _glCapabilities;
	
	
	//private constructor for the singleton
	private BasicManager3D() {
		
		System.err.println("Default profile: " +  GLProfile.getDefault());
		_glProfile = GLProfile.getDefault();
		_glCapabilities = new GLCapabilities(_glProfile);
	}
	
	/**
	 * Public access to the singleton
	 * @return the singleton
	 */
	public static BasicManager3D getInstance() {
		if (_instance == null) {
			_instance = new BasicManager3D();
		}
		return _instance;
	}
}







