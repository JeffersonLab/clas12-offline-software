package cnuphys.ced.ced3d;

import java.io.IOException;

import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class Sprites {

	// singleton
	private static Sprites _instance;

	// textures for points
	private Texture _greenOn;

	private Sprites() {
		try {
			_greenOn = TextureIO.newTexture(
					getClass().getResource("/images/green-on-16.png"), true,
					null);
		} catch (GLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Sprites getInstance() {
		if (_instance == null) {
			_instance = new Sprites();
		}
		return _instance;
	}

	public Texture getGreenOn() {
		return _greenOn;
	}
}
