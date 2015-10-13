package item3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;

public class SpriteSet extends Item3D {

    //the points as [x1, y1, z1, ..., xn, yn, zn]
    private float _coords[];
    
    //the texture
    private Texture _sprite;
    
    private URL _url;
    
    /**
     * Create a set of points that show a sprite for use on a Panel3D.
     * @param panel3D the owner 3D panel
     * @param coords the points as [x1, y1, z1, ..., xn, yn, zn]
     * @param imageURL the url locating the image
     */
    public SpriteSet(Panel3D panel3D, float[] coords, URL imageURL) {
	super(panel3D);
	_coords = coords;
	_url = imageURL;
    }
    
    
    @Override
    public void draw(GLAutoDrawable drawable) {
//	if (_sprite == null) {
//		try {
//		    _sprite = TextureIO.newTexture(_url, false, "yomama.png");
//		} catch (GLException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		} catch (IOException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//	}
//
//	//still null, cave
//	if (_sprite == null) {
//	    return;
//	}
	
	System.err.println("drawing sprites");
	try {
	    BufferedImage image = ImageIO.read(_url);
	    _sprite = AWTTextureIO.newTexture(GLProfile.getDefault(),  image, false);
	    _sprite.setTexParameteri(drawable.getGL().getGL2(), GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
	    _sprite.setTexParameteri(drawable.getGL().getGL2(), GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

	    System.err.println("Sprite image");
//	    _sprite = TextureIO.newTexture(_url, false, ".png");
	} catch (GLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
//	System.err.println("Sprite image h: " + _sprite.getImageHeight());
//	System.err.println("Sprite image w: " + _sprite.getImageWidth());
	
	Support3D.drawSprites(drawable, _coords, _sprite, Math.max(_sprite.getImageHeight(), _sprite.getImageWidth()));
    }
    

}
