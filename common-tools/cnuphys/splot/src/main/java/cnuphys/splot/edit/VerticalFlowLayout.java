package cnuphys.splot.edit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

public class VerticalFlowLayout implements LayoutManager2, Serializable {

	/**
	 * The center layout constraint.
	 */
	public static final String CENTER = "Center";

	/**
	 * The left layout constraint.
	 */
	public static final String LEFT = "Left";

	/**
	 * The right layout constraint.
	 */
	public static final String RIGHT = "Right";

	/**
	 * The actual components.
	 */
	private Vector<AlignedComponent> components;

	/**
	 * Use uniform widths?
	 */
	private boolean bUniformWidths;

	private int m_vGap;

	private int m_externalPadLeft;

	private int m_externalPadRight;

	private int m_externalPadTop;

	private int m_externalPadBottom;

	private int m_internalPadX;

	private int m_internalPadY;

	private int m_widestWidth;

	private Dimension m_preferredSize;

	private Insets m_insets;

	/**
	 * Dirty flag, signaling that preferred sizes need to be recalculated because a
	 * component has been added or removed.
	 */

	private boolean m_bDirty = false;

	/**
	 * This inner class pairs up a component with an alignment value. Each component
	 * in the VerticalFlowLayout can be aligned to the Left, Center, or Right.
	 */

	private class AlignedComponent implements Serializable {
		Component m_comp;

		String m_alignment;

		public AlignedComponent(Component comp, String alignment) {
			m_comp = comp;
			m_alignment = alignment;
		}

		public Component getComponent() {
			return m_comp;
		}

		public String getAlignment() {
			return m_alignment;
		}
	}// class AlignedComponent

	/**
	 * Constructs a new VerticalFlowLayout.
	 */
	public VerticalFlowLayout() {
		bUniformWidths = true;
		m_vGap = 3;
		m_externalPadLeft = 0;
		m_externalPadRight = 0;
		m_externalPadTop = 0;
		m_externalPadBottom = 0;
		m_internalPadX = 0;
		m_internalPadY = 0;

		components = new Vector<AlignedComponent>();
	}

	/**
	 * Constructs a new VerticalFlowLayout with your own width sizing type and
	 * vertical gap values.
	 * 
	 * @param uniformWidths size the widths of the component to be uniform or not
	 * @param vGap          vertical gap
	 */
	public VerticalFlowLayout(boolean uniformWidths, int vGap) {
		bUniformWidths = uniformWidths;
		m_vGap = vGap;

		components = new Vector<AlignedComponent>();
	}

	/**
	 * Adds the specified component to the layout.
	 * 
	 * @param name this is ignored
	 * @param comp the component to be added
	 * @exception IllegalArgumentException Invalid component or constraint.
	 */
	@Override
	public void addLayoutComponent(String name, Component comp) {
		// Make sure we have a valid component
		if (comp == null) {
			// No, not valid
			throw new IllegalArgumentException("Cannot add component: component is null.");
		}

		// Special case: treat null the same as "Left"
		if (name == null || name.length() == 0) {
			// D.out ("Assuming Left Layout");
			name = LEFT;
		}

		// Make sure we have a valid constraint
		if (name.equals(CENTER) || name.equals(LEFT) || name.equals(RIGHT)) {
			// Yes, valid
			AlignedComponent alcomp = new AlignedComponent(comp, name);
			components.add(alcomp);
		}
		else {
			// No, not valid
			throw new IllegalArgumentException("Cannot add component: constraint is invalid.");
		}

		// This signals the preferredLayoutSize to recalculate based on this
		// new addition
		m_bDirty = true;
	}

	/**
	 * Remove the specified component.
	 * 
	 * @param comp the component to be removed
	 * @exception IllegalArgumentException Invalid component.
	 */
	@Override
	public void removeLayoutComponent(Component comp) {
		if (comp != null) {
			Enumeration<AlignedComponent> enumer = components.elements();

			if (enumer != null) {
				while (enumer.hasMoreElements()) {
					// Get the component/alignment pair
					AlignedComponent alcomp = enumer.nextElement();

					// Pull just the component out and see if there's a match
					Component currComp = alcomp.getComponent();
					if (comp == currComp) {
						// Found the component, get rid of it
						components.remove(alcomp);

						// This signals the preferredLayoutSize to recalculate
						// based
						// on this deletion
						m_bDirty = true;
						break;
					}
				}
			}
		}
		else {
			throw new IllegalArgumentException("Cannot remove component: component is null.");
		}
	}

	/**
	 * Gets the minimum dimensions needed to lay out the component contained in the
	 * specified target container.
	 * 
	 * @param parent the Container on which to do the layout
	 * @see Container
	 * @see #preferredLayoutSize
	 * @return minimum layout size
	 */
	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return parent.getSize();
	}

	/**
	 * Gets the preferred dimensions for this layout given the components in the
	 * specified target container.
	 * <UL>
	 * <LI>For the horizontal size, this will go through all of the components and
	 * get the widest one and add the external and internal horizontal
	 * paddings.</LI>
	 * <LI>For the vertical size, this will add up the heights of all of the
	 * components and add in the internal and external vertical paddings.
	 * </UL>
	 * 
	 * @param parent the Container on which to do the layout
	 * @see Container
	 * @see #minimumLayoutSize
	 * @return preferred preferred layout size
	 */
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		// see if insets/border has changed. If so, recompute
		if (m_insets == null || m_insets != parent.getInsets()) {
			m_bDirty = true;
		}

		// if new components, recalculate preferred size
		if (m_preferredSize == null || m_bDirty) {
			int height = 0;
			m_widestWidth = 0;
			m_preferredSize = null;

			// compute size of any border/insets
			int xx = 0;
			int yy = 0;

			m_insets = parent.getInsets();
			if (m_insets != null) {
				xx = m_insets.left + m_insets.right;
				yy = m_insets.bottom + m_insets.top;
			}
			// D.out("Parent container insets: "+insets.toString());

			// Go through all of the components and add things up
			Enumeration<AlignedComponent> enumer = components.elements();
			if (enumer != null) {
				while (enumer.hasMoreElements()) {
					// Get component/alignment pair
					AlignedComponent alcomp = enumer.nextElement();

					// Pull just the component out
					Dimension compSize = alcomp.getComponent().getPreferredSize();

					int width = compSize.width;
					if (width > m_widestWidth) {
						// Keep track of the widest component
						m_widestWidth = width;
					}

					// Add the height, padding, and vertical gap to the
					// overall height tally
					height += compSize.height + (m_internalPadY * 2) + m_vGap;
				}

				// Everything is tallied; now create a new preferred size
				// figuring in the external padding.
				m_preferredSize = new Dimension(
						m_widestWidth + m_externalPadLeft + m_externalPadRight + (m_internalPadX * 2),
						height + m_externalPadTop + m_externalPadBottom);

				// add in any insets
				m_preferredSize.width += xx;
				m_preferredSize.height += yy;

			}
			else {
				// just set preferred size to size of border.
				m_preferredSize = new Dimension(xx, yy);
			}

			// Indicate preferred size is valid
			m_bDirty = false;

		} // recompute preferred size

		// D.out("Preferred size for "+parent.getName()+":
		// "+m_preferredSize.toString());
		return m_preferredSize;

	}// preferredLayoutSize

	/**
	 * Layout the components within the specified container
	 * 
	 * @param parent the container that is being layed out
	 * @see Container
	 */
	@Override
	public void layoutContainer(Container parent) {
		int yPos = m_externalPadTop;
		int yBot = m_externalPadBottom;
		int xPos = m_externalPadLeft;
		int xRight = m_externalPadRight;
		int compWidth = 0;
		int parentWidth = 0;

		// see if insets/border has changed. If so, recompute
		if (m_insets == null || m_insets != parent.getInsets()) {
			m_bDirty = true;
		}

		// get preferred size
		if (m_bDirty) {
			preferredLayoutSize(parent);
		}

		// get parent container's minimum size
		Dimension d = parent.getMinimumSize();
		if (d != null) {
			parentWidth = d.width;
		}

		// adjust starting pos for insets
		if (m_insets != null) {
			yPos += m_insets.top;
			yBot += m_insets.bottom;
			xPos += m_insets.left;
			xRight += m_insets.right;
		}

		// Go through all of the components and place them vertically.
		Enumeration<AlignedComponent> enumer = components.elements();
		if (enumer != null) {
			while (enumer.hasMoreElements()) {
				AlignedComponent alcomp = enumer.nextElement();
				Component comp = alcomp.getComponent();
				String alignment = alcomp.getAlignment();

				if (comp.isVisible()) {

					// compute height for this component = preferred height +
					// 2*internal pady
					int height = comp.getPreferredSize().height + (m_internalPadY * 2);

					// compute width for this component + 2*m_internalPadX
					if (bUniformWidths == true) {
						// get the width of the widest component
						compWidth = m_widestWidth + (m_internalPadX * 2);
					}
					else {
						// compute width based on preferred size
						compWidth = comp.getPreferredSize().width + (m_internalPadX * 2);
					}

					if ((alignment.equals(LEFT)) || (parentWidth == 0) || (compWidth > parentWidth)) {
						// left margin
						comp.setLocation(xPos, yPos);
					}
					else if (alignment.equals(RIGHT)) {
						// right margin
						comp.setLocation(parentWidth - xRight - compWidth, yPos);
					}
					else {
						// centered between margins
						int centerPos = xPos + (parentWidth - xPos - xRight) / 2;
						comp.setLocation(centerPos - (compWidth / 2), yPos);
					}

					comp.setSize(compWidth, height);

					yPos += height + m_vGap;
					// D.out("alignment = "+alignment+" height = "+height+"
					// width = "+compWidth+" xpos = "+xPos+" ypos = "+yPos);
				}
			}
		}
	}// layoutContainer

	/**
	 * Adds the specified component to the layout, using the specified constraint
	 * object.
	 * 
	 * @param comp        the component to be added
	 * @param constraints where/how the component is added to the layout.
	 */
	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if ((constraints == null) || (constraints instanceof String)) {
			addLayoutComponent((String) constraints, comp);
		}
		else {
			throw new IllegalArgumentException("cannot add to layout: constraint must be a string (or null)");
		}
	}

	/**
	 * Returns the maximum size of this component.
	 * 
	 * @see java.awt.Component#getMinimumSize()
	 * @see java.awt.Component#getPreferredSize()
	 * @see LayoutManager
	 */
	@Override
	public Dimension maximumLayoutSize(Container target) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Returns the alignment along the x axis. This specifies how the component
	 * would like to be aligned relative to other components. The value should be a
	 * number between 0 and 1 where 0 represents alignment along the origin, 1 is
	 * aligned the furthest away from the origin, 0.5 is centered, etc.
	 */
	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0.5f;
	}

	/**
	 * Returns the alignment along the y axis. This specifies how the component
	 * would like to be aligned relative to other components. The value should be a
	 * number between 0 and 1 where 0 represents alignment along the origin, 1 is
	 * aligned the furthest away from the origin, 0.5 is centered, etc.
	 */
	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0.5f;
	}

	/**
	 * Invalidates the layout, indicating that if the layout manager has cached
	 * information it should be discarded.
	 */
	@Override
	public void invalidateLayout(Container target) {
	}

	// ************************************************************************
	// Accessor methods
	// ************************************************************************

	/**
	 * See if the widths are set to uniform.
	 * 
	 * @return uniform width flag
	 */
	public boolean areWidthsUniform() {
		return bUniformWidths;
	}

	/**
	 * Get the vertical gap value.
	 * 
	 * @return vertical gap
	 */
	public int getVerticalGap() {
		return m_vGap;
	}

	/**
	 * Returns the left external horizontal padding. This is the space between the
	 * widest component in the column and the left edge.
	 * 
	 * @return Left external horizontal padding.
	 */
	public int getExternalPadLeft() {
		return m_externalPadLeft;
	}

	/**
	 * Returns the right external horizontal padding. This is the space between the
	 * widest component in the column and the left right edge.
	 * 
	 * @return Right external horizontal padding.
	 */
	public int getExternalPadRight() {
		return m_externalPadRight;
	}

	/**
	 * Returns the top external vertical padding. This is the space between the top
	 * component and the top edge.
	 * 
	 * @return Top external vertical padding.
	 */
	public int getExternalPadTop() {
		return m_externalPadTop;
	}

	/**
	 * Returns the bottom external vertical padding. This is the space between the
	 * bottom component and the bottom edge.
	 * 
	 * @return Bottom external vertical padding.
	 */
	public int getExternalPadBottom() {
		return m_externalPadBottom;
	}

	/**
	 * Get the internal horizontal padding. This is a simple way to grow the
	 * horizontal preferred size of all of the components by a certain amount. It
	 * works great on buttons, because their preferred size is usually not wide
	 * enough to look good.
	 * 
	 * @return internal horizontal padding
	 */
	public int getInternalPadX() {
		return m_internalPadX;
	}

	/**
	 * Get the internal vertical padding. This is a simple way to grow the vertical
	 * preferred size of all of the components by a certain amount. It works great
	 * on buttons, because their preferred size is usually too short.
	 * 
	 * @return internal vertical padding
	 */
	public int getInternalPadY() {
		return m_internalPadY;
	}

	/**
	 * Set the uniform width flag. If you set this to true, the alignment value is
	 * ignored.
	 * 
	 * @param uniformWidths
	 *                      <UL>
	 *                      <LI>true = make all the widths uniform (sizing to match
	 *                      the widest)</LI>
	 *                      <LI>false = let all components take their various
	 *                      preferred widths
	 *                      </UL>
	 */
	public void setUniformWidths(boolean uniformWidths) {
		bUniformWidths = uniformWidths;
	}

	/**
	 * Set the vertical gap value.
	 * 
	 * @param vGap vertical gap
	 */
	public void setVerticalGap(int vGap) {
		m_vGap = vGap;
	}

	/**
	 * Set the left external horizontal padding. This is the space between the
	 * widest component in the column and the edge.
	 * 
	 * @param padding the amount of padding in pixels.
	 */
	public void setExternalPadLeft(int padding) {
		m_externalPadLeft = padding;
	}

	/**
	 * Set the right external horizontal padding. This is the space between the
	 * widest component in the column and the edge.
	 * 
	 * @param padding the amount of padding in pixels.
	 */
	public void setExternalPadRight(int padding) {
		m_externalPadRight = padding;
	}

	/**
	 * Set the top external vertical padding. This is the space between the top and
	 * the top edge.
	 * 
	 * @param padding the amount of padding in pixels.
	 */
	public void setExternalPadTop(int padding) {
		m_externalPadTop = padding;
	}

	/**
	 * Set the bottom external vertical padding. This is the space between the
	 * bottom and the bottom edge.
	 * 
	 * @param padding the amount of padding in pixels.
	 */
	public void setExternalPadBottom(int padding) {
		m_externalPadBottom = padding;
	}

	/**
	 * Set the internal horizontal padding. This is a simple way to grow the
	 * horizontal preferred size of all of the components by a certain amount. It
	 * works great on buttons, because their preferred size is usually not wide
	 * enough to look good.
	 * 
	 * @param padding internal horizontal padding
	 */
	public void setInternalPadX(int padding) {
		m_internalPadX = padding;
	}

	/**
	 * Set the internal vertical padding. This is a simple way to grow the vertical
	 * preferred size of all of the components by a certain amount. It works great
	 * on buttons, because their preferred size is usually too short.
	 * 
	 * @param padding internal vertical padding
	 */
	public void setInternalPadY(int padding) {
		m_internalPadY = padding;
	}

}