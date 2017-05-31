package cnuphys.bCNU.drawable;

import java.awt.Graphics;

import cnuphys.bCNU.graphics.container.IContainer;

/**
 * @author heddle
 * 
 */
public class DrawableAdapter implements IDrawable {

	private boolean _visible = true;
	private boolean _enabled = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see cnuphys.bCNU.drawable.IDrawable#draw(java.awt.Graphics,
	 * cnuphys.bCNU.graphics.container.IContainer)
	 */
	@Override
	public void draw(Graphics g, IContainer container) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cnuphys.bCNU.drawable.IDrawable#prepareForRemoval()
	 */
	@Override
	public void prepareForRemoval() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cnuphys.bCNU.drawable.IDrawable#setDirty(boolean)
	 */
	@Override
	public void setDirty(boolean dirty) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cnuphys.bCNU.visible.IVisible#getName()
	 */
	@Override
	public String getName() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cnuphys.bCNU.visible.IVisible#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return _visible;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cnuphys.bCNU.visible.IVisible#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		_visible = visible;
	}

	@Override
	public boolean isEnabled() {
		return _enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}

}
