package cnuphys.lund;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.JComponent;

/**
 * Used to store a Lund ID
 * 
 * @author heddle
 *
 */
public class LundId implements Comparable<LundId> {

	// Speed of light in m/s
	private static final double C = 299792458.0; // m/s

	// charge unit in Coloumbs
	private static final double QE = 1.602176487e-19; // coulombs

	private String _type;
	private String _name;

	private int _id;
	private double _mass; // will store in GeV/c^2
	private int _spinX2; // two times the spin
	private int _chargeX3; // three times the charge

	private static final Font labelFont = new Font("SansSerif", Font.PLAIN, 11);
	// used for legend label
	private static Dimension labelSize = new Dimension(22, 14);

	private static int labelRectSize = 10;
	// shouldn't happen, unrecognized lund id
	public static final JComponent unknownLabel = getLabelForLegend(Color.black, Color.cyan, "???");

	/**
	 * Create a Lund ID
	 * 
	 * @param type     "Lepton", "InterBoson", "Nucleus", "Baryon", or "Meson"
	 * @param name     e.g., "pi+"
	 * @param id       the Lund Id
	 * @param mass     the mass in GeV/c^2
	 * @param chargeX3 three times the charge
	 * @param spinX2   twice the spin
	 */
	public LundId(String type, String name, int id, double mass, int chargeX3, int spinX2) {
		super();
		_type = type;
		_name = name;
		_id = id;
		_mass = mass; // in GeV/c^2
		_spinX2 = spinX2;
		_chargeX3 = chargeX3;
	}

	/**
	 * Get the type, one of {"Lepton","InterBoson", "Nucleus","Baryon", "Meson"}
	 * 
	 * @return the type
	 */
	public String getType() {
		return _type;
	}

	/**
	 * Get the name of the particle.
	 * 
	 * @return the name of the particle.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Get the Lund Id of the particle.
	 * 
	 * @return the Lund id
	 */
	public int getId() {
		return _id;
	}

	/**
	 * Get the spin of the particle.
	 * 
	 * @return the spin.
	 */
	public double getSpin() {
		// twice the spin is what is stored
		return (_spinX2) / 2.0;
	}

	/**
	 * Get the charge of the particle in e's.
	 * 
	 * @return the charge of the particle in units of e.
	 */
	public int getCharge() {
		// three times the charge is what is stored
		return _chargeX3 / 3;
	}

	/**
	 * Get the mass in GeV/c^2
	 * 
	 * @return the mass in GeV/c^2
	 */
	public double getMass() {
		return _mass;
	}

	/**
	 * Return three times the charge, in units of e.
	 * 
	 * @return three times the charge, in units of e.
	 */
	public int getChargeX3() {
		return _chargeX3;
	}

	/**
	 * Obtain a string representation.
	 */
	@Override
	public String toString() {
		String massStr = DoubleFormat.doubleFormat(_mass, 3, 3);
		return String.format("%-20s %-11s  %-7d  %-8s q = %2d 2s = %-2d", _name, _type, _id, massStr, _chargeX3 / 3,
				_spinX2);
	}

	/**
	 * Compare based on Id.
	 * 
	 * @return -1, 0, or 1 for less than, equal, or greater than.
	 */
	@Override
	public int compareTo(LundId o) {
		if (_id < o._id) {
			return -1;
		} else if (_id > o._id) {
			return 1;
		}

		return 0;
	}

	/**
	 * Obtain a small string representation.
	 * 
	 * @return a small string representation.
	 */
	public String smallString() {
		return _name + " [" + _id + "]";
	}

	/**
	 * Get the momentum from the kinetic energy
	 * 
	 * @param t the kinetic energy in GeV
	 * @return the momentum in Gev/c
	 */
	public double pFromT(double t) {
		double e = t + _mass;
		return Math.sqrt(e * e - _mass * _mass);
	}

	/**
	 * Get the charge in Coulombs
	 * 
	 * @return the charge in Coulombs
	 */
	public double getChargeCoulombs() {
		return QE * _chargeX3 / 3.;
	}

	/**
	 * Get the mass in kilograms
	 * 
	 * @return the mass in kilograms
	 */
	public double getMassKg() {
		return _mass * 1.0e9 * QE / (C * C);
	}

	/**
	 * Get a component to place on a legend.
	 * 
	 * @return a component for the legend.
	 */
	public JComponent getLabelForLegend() {

		LundStyle style = LundStyle.getStyle(this);
		if (style == null) {
			return getLabelForLegend(Color.lightGray, Color.darkGray, _name);
		} else {
			return getLabelForLegend(style.getFillColor(), style.getLineColor(), _name);
		}
	}

	/**
	 * Obtain a fixed size label to place on a legend.
	 * 
	 * @param fillColor the fill color
	 * @param lineColor the line color
	 * @param name      the name of the particle
	 * @return the label component
	 */
	@SuppressWarnings("serial")
	private static JComponent getLabelForLegend(final Color fillColor, final Color lineColor, final String name) {
		JComponent component = new JComponent() {

			@Override
			public void paintComponent(Graphics g) {
				FontMetrics fm = getFontMetrics(labelFont);
				g.setFont(labelFont);
				// g.setColor(fillColor);
				// g.fillRect(2, 2, labelRectSize, labelRectSize);

				g.setColor(fillColor);
				g.fillRect(2, 2, labelRectSize, labelRectSize);

				// g.setColor(lineColor);
				// g.drawRect(2, 2, labelRectSize, labelRectSize);
				g.setColor(Color.black);
				g.drawString(name, labelRectSize + 6, fm.getHeight() - 4);
			}

			@Override
			public Dimension getPreferredSize() {
				return labelSize;
			}
		};
		return component;
	}

	/**
	 * Draw a line for use on a toolbar user component, most likely
	 * 
	 * @param g  the graphics context
	 * @param x  the horizontal staring point
	 * @param yc the central vertical position
	 * @return the offset
	 */
	public int drawLineForLegend(Graphics g, int x, int yc) {

		Graphics2D g2 = (Graphics2D) g;
		Stroke oldStroke = g2.getStroke();
		LundStyle style = LundStyle.getStyle(this);
		g.setColor(style.getLineColor());
		g2.setStroke(style.getStroke());

		int linelen = 30;

		g2.drawLine(x, yc, x + linelen, yc);
		x += linelen + 3;

		g2.setStroke(oldStroke);

		// now the name
		g.setFont(labelFont);
		FontMetrics fm = g.getFontMetrics(labelFont);
		g.setColor(Color.black);
		g.drawString(getName(), x, yc + fm.getAscent() / 2 - 3);

		return linelen + fm.stringWidth(getName()) + 9;
	}

	/**
	 * Get the LundStyle for this id
	 * 
	 * @return the LundStyle for this id
	 */
	public LundStyle getStyle() {
		return LundStyle.getStyle(this);
	}

}