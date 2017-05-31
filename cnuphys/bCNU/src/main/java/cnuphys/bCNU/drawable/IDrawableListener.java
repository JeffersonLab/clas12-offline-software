package cnuphys.bCNU.drawable;

import java.util.EventListener;

public interface IDrawableListener extends EventListener {
	/**
	 * One of the drawables in the set has changed.
	 * 
	 * @param list
	 *            the list containing the changed drawble.
	 * @param drawable
	 *            the IDrawable in question. It wil be <code>null</code> for
	 *            events that apply to the entire list.
	 * @param type
	 *            the type of change.
	 */
	public void drawableChanged(DrawableList list, IDrawable drawable,
			DrawableChangeType type);
}
