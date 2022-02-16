package cnuphys.snr.test;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;

import cnuphys.snr.SNRCluster;
import cnuphys.snr.SNRClusterFinder;
import cnuphys.snr.ExtendedWord;
import cnuphys.snr.NoiseReductionParameters;

public class ChamberTest {
	
	private static final Color[] _missingColors = {Color.red, Color.orange, Color.yellow};
	
	private static final Color _clusterColor = new Color(46, 169, 87);
	private static final Color[] _clusterBorders = {Color.black, Color.gray, Color.yellow, Color.cyan, Color.magenta, Color.white};

	
	public static final Color _maskFillLeft = new Color(255, 128, 0, 48);
	public static final Color _maskFillRight = new Color(0, 128, 255, 48);
	public static final Color _almostTransparent = new Color(0, 0, 0, 16);

	private Color _fillColor = new Color(244, 244, 244);
	private Color _lineColor = Color.gray;

	private static final Font _smallFont = new Font("SanSerif", Font.BOLD, 9);
	private static FontMetrics _fm;
	
	private static final Stroke _fatStroke = TestParameters.getStroke(2.0f, LineStyle.SOLID);
	
	/**
	 * The parent detector that presumably contains multiple chambers.
	 */
	private DetectorTest _detectorTest;

	/**
	 * A world coordinate rectangular boundary.
	 */
	public Rectangle2D.Double boundary;

	/**
	 * The name of the chamber.
	 */
	private String _name;

	/**
	 * A collection of all the hits.
	 */
	private Vector<HitTest> _hits = new Vector<HitTest>(100);

	/**
	 * Parameters for testing noise reduction. chambers.
	 */
	private NoiseReductionParameters _parameters;

	/**
	 * Raw data in extended words for noise reduction
	 */
	private ExtendedWord _data[];

	//chamber index
	public int index;

	/**
	 * Create a test chamber--rectangular with uniform rectangular cells.
	 * 
	 * @param index    the index in the DetectorTest collection
	 * @param name     the name of the chamber.
	 * @param boundary the rectangular boundary in world coordinates.
	 */
	public ChamberTest(DetectorTest detectorTest, int index, NoiseReductionParameters parameters,
			Rectangle2D.Double boundary) {
		_detectorTest = detectorTest;
		_name = "Superlayer " + (index + 1);
		this.boundary = boundary;
		_parameters = parameters;
		this.index = index;
		initializeSpace();
	}

	/**
	 * Draw the chamber.
	 * 
	 * @param g     the Graphics context.
	 * @param world the world system
	 * @param local the local system
	 */
	public void draw(Graphics g, Rectangle2D.Double world, Rectangle local) {
		
		if (_fm == null) {
			_fm = g.getFontMetrics(_smallFont);
		}

		Rectangle cell = new Rectangle();
		drawCellOutlines(g, world, local, _fillColor);
		drawLeftSegmentOutline(g, world, local, _fillColor);
		drawRightSegmentOutline(g, world, local, _fillColor);


		drawString(g, world, local, _name, boundary.x, boundary.y + boundary.height);

		// mark segments

		if (_detectorTest.getDisplayOption() == DetectorTest.DisplayOption.ANALYZED) {

			for (int wire = 0; wire < _parameters.getNumWire(); wire++) {
				boolean left = _parameters.getLeftSegments().checkBit(wire);
				boolean right = _parameters.getRightSegments().checkBit(wire);
				if (left || right) {
					cellBounds(world, local, 0, wire, cell);

					if (left) {
						drawMask(g, world, local, wire, _parameters.getLeftLayerShifts(), 1);
					}
					if (right) {
						drawMask(g, world, local, wire, _parameters.getRightLayerShifts(), -1);
					}

				}
			}
		} // end analyzed

		// draw hits
		for (HitTest th : _hits) {
			cellBounds(world, local, th.getLayer(), th.getWire(), cell);
			g.setColor(getHitColor(th));
			g.fillRect(cell.x, cell.y, cell.width, cell.height);
			g.setColor(_lineColor);
			g.drawRect(cell.x, cell.y, cell.width, cell.height);

			if (_detectorTest.getDisplayOption() == DetectorTest.DisplayOption.ANALYZED) {

				// is it a noise hit that is being preserved?
				if (th.getComputedHitType() == HitTest.HitType.TRACK
						&& th.getActualHitType() == HitTest.HitType.NOISE) {
					g.setColor(Color.blue);
					g.drawRect(cell.x + 1, cell.y + 1, cell.width - 2, cell.height - 2);
					g.setColor(Color.darkGray);
					g.drawRect(cell.x + 2, cell.y + 2, cell.width - 4, cell.height - 4);
				}
			}
		}
		
		//draw clusters?
		
		if (TestParameters.showClusters) {
			
			SNRClusterFinder cf = _parameters.getClusterFinder();
			if (cf != null) {
				drawClusters(g, world, local, cf.getClusters(), _clusterColor);
			}
		}
		
		
	}
	
	/**
	 * Draw the chamber.
	 * 
	 * @param g     the Graphics context.
	 * @param world the world system
	 * @param local the local system
	 */
	public void drawAfter(Graphics g, Rectangle2D.Double world, Rectangle local) {
		//draw segment candidates
		drawLeftSegmentCandidates(g, world, local);
		drawRightSegmentCandidates(g, world, local);
		

		
	}
	
	//draw the clusters
	private void drawClusters(Graphics g, Rectangle2D.Double world, Rectangle local, List<SNRCluster>clusters, Color color) {

		Rectangle cell = new Rectangle();
		Graphics2D g2 = (Graphics2D)g;
		
		int numLayer = _parameters.getNumLayer();
		
		int count = 0;
		for (SNRCluster cluster : clusters) {
			Area area = new Area();

			for (int lay = 0; lay < numLayer; lay++) {
				for (int wire : cluster.wireLists[lay]) {
					cellBounds(world, local, lay, wire, cell);
					area.add(new Area(cell));
				}
			}
			
			if (!area.isEmpty()) {
				g2.setColor(color);
				g2.fill(area);
				
				Color borderColor = _clusterBorders[count % _clusterBorders.length];
				
				count++;
				g2.setColor(borderColor);
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(_fatStroke);
				g2.draw(area);
				
				//draw best fit line
				
				double wire1 = cluster.getWirePosition(0);
				double wire2 = cluster.getWirePosition(numLayer-1);
				
				int iwire1 =  (int)wire1;
				int iwire2 =  (int)wire2;
				
				double fwire1 = wire1 -  iwire1;
				double fwire2 = wire2 -  iwire2;
								
				cellBounds(world, local, 0, iwire1, cell);
				int y1 = (int)(cell.getCenterY());
				int x1 = (int)(cell.getCenterX() - fwire1*cell.getWidth());
				
				cellBounds(world, local, numLayer-1, iwire2, cell);
				int y2 = (int)(cell.getCenterY());
				int x2 = (int)(cell.getCenterX() - fwire2*cell.getWidth());
				
				

				g2.setColor (Color.darkGray);
				g2.drawLine(x1-1, y1, x2-1, y2);
				g2.setColor (Color.darkGray);
				g2.drawLine(x1+1, y1, x2+1, y2);
				g2.setColor (Color.yellow);
				g2.drawLine(x1, y1, x2, y2);
				
				g2.setStroke(oldStroke);
				
				
				g2.setColor(Color.cyan);
				g2.fillOval(x1-3, y1-3, 6, 6);
				g2.fillOval(x2-3, y2-3, 6, 6);
				
				g2.setColor(Color.black);
				g2.drawOval(x1-3, y1-3, 6, 6);
				g2.drawOval(x2-3, y2-3, 6, 6);

			}

		}
	}

	//draw a string
	private void drawString(Graphics g, Rectangle2D.Double world, Rectangle local, String s, double wx, double wy) {
		Point pp = new Point();
		TestSupport.toLocal(world, local, pp, wx, wy);
		g.setFont(_smallFont);
		g.setColor(Color.blue);
		g.drawString(s, pp.x, pp.y - 2);
	}

	// Draw the mask
	private void drawMask(Graphics g, Rectangle2D.Double world, Rectangle local, int wire, int shifts[], int sign) {
		if (sign == 1) {
			g.setColor(_maskFillLeft);
		} else {
			g.setColor(_maskFillRight);
		}

		Rectangle tr = new Rectangle();

		for (int layer = 0; layer < _parameters.getNumLayer(); layer++) {
			cellBounds(world, local, layer, wire, tr);
			g.fillRect(tr.x, tr.y, tr.width, tr.height);
			for (int shift = 1; shift <= shifts[layer]; shift++) {
				int tempWire = wire + sign * shift;
				if (tempWire >= 0 && (tempWire < _parameters.getNumWire()))
					cellBounds(world, local, layer, tempWire, tr);
				g.fillRect(tr.x, tr.y, tr.width, tr.height);
			}
		}

	}

	/**
	 * Draw all the cell outlines efficiently.
	 * 
	 * @param g     the graphics context.
	 * @param world the world system.
	 * @param local the local system.
	 * @param fc    the fill color, can be <code>null</code>.
	 */
	protected void drawCellOutlines(Graphics g, Rectangle2D.Double world, Rectangle local, Color fc) {
		Rectangle cell = new Rectangle();

		cellBounds(world, local, 0, 0, cell); // bottom-right
		int bottom = cell.y + cell.height;
		int right = cell.x + cell.width;

		cellBounds(world, local, _parameters.getNumLayer() - 1, _parameters.getNumWire() - 1, cell); // top-left
		int top = cell.y;
		int left = cell.x;

		int width = right - left;
		int height = bottom - top;

		if (fc != null) {
			g.setColor(fc);
			g.fillRect(left, top, width, height);
		}

		g.setColor(_lineColor);
		g.drawRect(left, top, width, height);

		for (int wire = 0; wire < (_parameters.getNumWire() - 1); wire++) {
			cellBounds(world, local, 0, wire, cell);
			g.drawLine(cell.x, bottom, cell.x, top);
		}

		for (int layer = 0; layer < (_parameters.getNumLayer() - 1); layer++) {
			cellBounds(world, local, layer, 0, cell);
			g.drawLine(left, cell.y, right, cell.y);
		}

	}
	
	/**
	 * Draw the segment candidate indications
	 * @param g
	 * @param world
	 * @param local
	 * @param candidates
	 */
	protected void drawLeftSegmentCandidates(Graphics g, Rectangle2D.Double world, Rectangle local) {
		
		ExtendedWord candidates = _parameters.getLeftSegments();
		
		Rectangle cell = new Rectangle();
		
		for (int wire = 0; wire < _parameters.getNumWire(); wire++) {
			if (candidates.checkBit(wire)) {
				leftSegmentBounds(world, local, wire, cell);
				
				int nml = _parameters.missingLayersUsed(0, wire);
				Color fc = _missingColors[Math.min(nml, _missingColors.length-1)];

				
				g.setColor(fc);
				g.fillOval(cell.x, cell.y, cell.width, cell.height);
				g.setColor(Color.blue);
				g.drawOval(cell.x, cell.y, cell.width, cell.height);
				
				//System.err.println("NML: " + nml);
			}
		}
	}
	
	/**
	 * Draw the segment candidate indications
	 * @param g
	 * @param world
	 * @param local
	 * @param candidates
	 */
	protected void drawRightSegmentCandidates(Graphics g, Rectangle2D.Double world, Rectangle local) {
		
		ExtendedWord candidates = _parameters.getRightSegments();
		
		Rectangle cell = new Rectangle();
		
		for (int wire = 0; wire < _parameters.getNumWire(); wire++) {
			if (candidates.checkBit(wire)) {
				rightSegmentBounds(world, local, wire, cell);
				
				int nml = _parameters.missingLayersUsed(1, wire);
				Color fc = _missingColors[Math.min(nml, _missingColors.length-1)];

				
				g.setColor(fc);
				g.fillOval(cell.x, cell.y, cell.width, cell.height);
				g.setColor(Color.blue);
				g.drawOval(cell.x, cell.y, cell.width, cell.height);
				
				//System.err.println("NML: " + nml);
			}
		}
	}
	
	//left segments drawn just below
	protected void drawLeftSegmentOutline(Graphics g, Rectangle2D.Double world, Rectangle local, Color fc) {
		Rectangle cell = new Rectangle();

		for (int wire = 0; wire < _parameters.getNumWire(); wire++) {
			leftSegmentBounds(world, local, wire, cell);
			
			if (fc != null) {
				g.setColor(fc);
				g.fillOval(cell.x, cell.y, cell.width, cell.height);
			}

			g.setColor(Color.blue);
			g.drawOval(cell.x, cell.y, cell.width, cell.height);
		}

		g.setFont(_smallFont);
		g.setColor(Color.black);
		
		String label = "L";
		g.drawString(label, cell.x - _fm.stringWidth(label)-4, cell.y + (cell.height + _fm.getHeight())/2);
	}
	
	//right segments drawn just below left segments
	protected void drawRightSegmentOutline(Graphics g, Rectangle2D.Double world, Rectangle local, Color fc) {
		Rectangle cell = new Rectangle();
		
		for (int wire = 0; wire < _parameters.getNumWire(); wire++) {
			rightSegmentBounds(world, local, wire, cell);

			if (fc != null) {
				g.setColor(fc);
				g.fillOval(cell.x, cell.y, cell.width, cell.height);
			}

			g.setColor(Color.blue);
            g.drawOval(cell.x, cell.y, cell.width, cell.height);
		}

		g.setFont(_smallFont);
		g.setColor(Color.black);
		
		String label = "R";
		g.drawString(label, cell.x - _fm.stringWidth(label)-4, cell.y + (cell.height + _fm.getHeight())/2);
	}
	
	

	/**
	 * Get the color for a hit
	 * 
	 * @param ht the hit in question
	 * @return the fill color to use.
	 */
	protected Color getHitColor(HitTest ht) {
		boolean noNoise = TestParameters.noiseOff; // no noise == cleaned data

		if (_detectorTest.getDisplayOption() == DetectorTest.DisplayOption.REALITY) {
			if (ht.getActualHitType() == HitTest.HitType.NOISE) {
				return noNoise ? _almostTransparent : TestParameters.getRealityNoiseColor();
			}
			if (ht.getActualHitType() == HitTest.HitType.TRACK) {
				return TestParameters.getRealityTrackColor();
			}
		} else if (_detectorTest.getDisplayOption() == DetectorTest.DisplayOption.DATA) {
			return TestParameters.getGenericHitColor();
		} else if (_detectorTest.getDisplayOption() == DetectorTest.DisplayOption.ANALYZED) {
			if (ht.getComputedHitType() == HitTest.HitType.TRACK && ht.getActualHitType() == HitTest.HitType.NOISE) {
				return TestParameters.getSavedNoiseColor();
			} else if (ht.getComputedHitType() == HitTest.HitType.NOISE) {
				return noNoise ? _almostTransparent : TestParameters.getAnalyzedNoiseColor();
			} else if (ht.getComputedHitType() == HitTest.HitType.TRACK) {
				return TestParameters.getAnalyzedTrackColor();
			}
		}
		return Color.yellow;

	}

	/**
	 * Fill in a pixel rectangle with the cell pixel.
	 * 
	 * @param world the world system.
	 * @param local the local system.
	 * @param layer the layer index [0..]
	 * @param wire  the wire index [0..]
	 * @param cell  the Rectangle that will be set to the cell boundary.
	 */
	protected void cellBounds(Rectangle2D.Double world, Rectangle local, int layer, int wire, Rectangle cell) {
		Rectangle2D.Double wr = new Rectangle2D.Double();
		cellWorldBounds(layer, wire, wr);
		TestSupport.toLocal(world, local, cell, wr);
	}
	
	/**
	 * Get the bounds for drawing whether there is a potential left segment here
	 * @param world the world system.
	 * @param local the local system.	 * @param layer the layer index [0..]
	 * @param wire  the wire index [0..]
	 * @param cell  the Rectangle that will be set to the cell boundary.
	 */
	protected void leftSegmentBounds(Rectangle2D.Double world, Rectangle local, int wire, Rectangle cell) {
		cellBounds(world, local, -1, wire, cell);
		cell.x += 1;
		cell.y += 1;
		cell.width -= 2;
		cell.height -= 2;
	}
	
	/**
	 * Get the bounds for drawing whether there is a potential right segment here
	 * @param world the world system.
	 * @param local the local system.	 * @param layer the layer index [0..]
	 * @param wire  the wire index [0..]
	 * @param cell  the Rectangle that will be set to the cell boundary.
	 */
	protected void rightSegmentBounds(Rectangle2D.Double world, Rectangle local, int wire, Rectangle cell) {
		cellBounds(world, local, -2, wire, cell);
		cell.x += 1;
		cell.y += 1;
		cell.width -= 2;
		cell.height -= 2;
	}

	/**
	 * Get the world boundary of a cell.
	 * 
	 * @param layer the layer index [0..]
	 * @param wire  the wire index [0..]
	 * @param wr    will be set to the cell boundary.
	 */
	protected void cellWorldBounds(int layer, int wire, Rectangle2D.Double wr) {
		double dy = boundary.height / _parameters.getNumLayer();
		double dx = boundary.width / _parameters.getNumWire();
		// note layers counted from bottom, wires counted from right
		double xmin = boundary.x + boundary.width - dx * (wire + 1);
		double ymin = boundary.y + dy * layer;
		wr.setFrame(xmin, ymin, dx, dy);
	}

	/**
	 * Get the world boundary of a range of cells.
	 * 
	 * @param layer   the layer index [0..]
	 * @param minwire the min wire index [0..]
	 * @param maxwire the max wire index [minwire..]
	 * @param wr      will be set to the cell range boundary.
	 */
	protected void cellRangeWorldBounds(int layer, int minwire, int maxwire, Rectangle2D.Double wr) {
		double dy = boundary.height / _parameters.getNumLayer();
		double dx = boundary.width / _parameters.getNumWire();
		int nc = maxwire - minwire + 1;
		// note layers counted from bottom, wires counted from right
		double xmin = boundary.x + boundary.width - dx * (maxwire + 1);
		double ymin = boundary.y + dy * layer;
		wr.setFrame(xmin, ymin, nc * dx, dy);
	}

	/**
	 * Get the world boundary of a layer.
	 * 
	 * @param layer the layer index [0..]
	 * @param wr    will be set to the layer boundary.
	 */
	protected void layerWorldBounds(int layer, Rectangle2D.Double wr) {
		double dy = boundary.height / _parameters.getNumLayer();
		// note layers counted from bottom
		double ymin = boundary.y + dy * layer;
		wr.setFrame(boundary.x, ymin, boundary.width, dy);
	}

	/**
	 * Get a mouse over feedback string.
	 * 
	 * @param pp    the mouse location.
	 * @param world the world system.
	 * @param local the local system.
	 * @return the feedback string, or null if not over any cell.
	 */
	public String feedback(Point pp, Rectangle2D.Double world, Rectangle local) {
		StringBuilder sb = new StringBuilder(500);

		sb.append("[" + _name + "] ");

		if (!contains(pp, world, local)) {
			return null;
		}
		Rectangle bounds = getBounds(world, local);

		int layer = -1;
		int wire = -1;

		int bottom = bounds.y + bounds.height;
		int right = bounds.x + bounds.width;

		for (int tlayer = 0; tlayer < _parameters.getNumLayer(); tlayer++) {
			int ymin = bottom - (tlayer + 1) * bounds.height / _parameters.getNumLayer();
			if (pp.y > ymin) {
				layer = tlayer;
				// use non C indices
				sb.append("Layer " + (layer + 1) + " ");
				break;
			}
		}

		for (int twire = 0; twire < _parameters.getNumWire(); twire++) {
			int xmin = right - (twire + 1) * bounds.width / _parameters.getNumWire();
			if (pp.x > xmin) {
				wire = twire;
				// use non C indices
				sb.append("Wire " + (wire + 1) + " ");
				break;
			}
		}


		// occupancy
		
		sb.append(String.format("  #hits %d Occ %4.1f%%  RedOcc %4.1f%%", getNumHits(), 100.0 * getOccupancy(),   100.0 * getReducedOccupancy()));

		// allowed missing layers
		sb.append("  Missing Layers Allowed " + _parameters.getAllowedMissingLayers());

		// layers shifts
		sb.append(shiftString("  Left Shifts", _parameters.getLeftLayerShifts()));
		sb.append(shiftString("  Right Shifts", _parameters.getRightLayerShifts()));
		
		if ((layer >= 0) && (wire >= 0)) {
			HitTest ht = findHit(layer, wire);
			if (ht == null) {
				sb.append("\n [No Hit] ");
			} else {
				sb.append("\n Reality: " + ht.getActualHitType() + "  Computed: " + ht.getComputedHitType());
			}
			
			int adjacency = _parameters.computeAdjacency(layer, wire);
			sb.append(" Adjacency: " + adjacency);
		}

		if (_parameters.getClusterFinder() != null) {
			List<SNRCluster> clusters = _parameters.getClusterFinder().getClusters();

			if (clusters != null) {
				sb.append("\n#Clusters [" + clusters.size() + "] ");
				for (SNRCluster cluster : clusters) {
					sb.append("    " + cluster);
				}
			}
		}

		return sb.toString();
	}

	protected String shiftString(String prompt, int shifts[]) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(" ");
		sb.append(prompt);
		sb.append(" [");
		for (int i = 0; i < shifts.length; i++) {
			sb.append(shifts[i]);
			if (i != (shifts.length - 1)) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Obtains the bounding rectangle.
	 * 
	 * @param world the world system.
	 * @param local the local system.
	 * @return the bounding screen rectangle.
	 */
	public Rectangle getBounds(Rectangle2D.Double world, Rectangle local) {
		Rectangle r = new Rectangle();
		TestSupport.toLocal(world, local, r, boundary);
		return r;
	}

	/**
	 * See if the pixel point is within the screen boundary of the chamber.
	 * 
	 * @param pp    the pixel point.
	 * @param world the world system.
	 * @param local the local system.
	 * @return <code>true</code> if the point is conatined within the cjamber
	 *         boundary.
	 */
	public boolean contains(Point pp, Rectangle2D.Double world, Rectangle local) {
		Point2D.Double wp = new Point2D.Double();
		TestSupport.toWorld(world, local, pp, wp);
		return boundary.contains(wp);
	}

	/**
	 * Get the rectangular boundary of the chamber in world coordinates.
	 * 
	 * @return the rectangular boundary of the chamber in world coordinates.
	 */
	public Rectangle2D.Double getBoundary() {
		return boundary;
	}

	/**
	 * Get the number of name in this chamber.
	 * 
	 * @return the name of layers.
	 */
	public String getName() {
		return _name;
	}

	public void forceHit(int layerOneBased, int wireOneBased) {
		HitTest ht = createHit(layerOneBased - 1, wireOneBased - 1, HitTest.HitType.TRACK);
		_hits.add(ht);
	}

	/**
	 * Generate random noise
	 */
	public void generateNoise() {
		for (int layer = 0; layer < _parameters.getNumLayer(); layer++) {
			for (int wire = 0; wire < _parameters.getNumWire(); wire++) {
				if (DetectorTest.getRandom().nextDouble() < TestParameters.getNoiseRate()) {
					HitTest ht = createHit(layer, wire, HitTest.HitType.NOISE);
					_hits.add(ht);
				}
			}

		}

		// add a blob?
		if (DetectorTest.getRandom().nextDouble() < TestParameters.getProbBlob()) {
			int blobSize = TestParameters.getBlobSize();

			// random central wire
			int ranWire = (int) (_parameters.getNumWire() * DetectorTest.getRandom().nextDouble());
			for (int layer = 0; layer < _parameters.getNumLayer(); layer++) {
				int minWire = Math.max(0, ranWire - blobSize);
				int maxWire = Math.min(_parameters.getNumWire() - 1, ranWire + blobSize);
				for (int w = minWire; w <= maxWire; w++) {
					if (DetectorTest.getRandom().nextDouble() > TestParameters.getProbBadWire()) {
						HitTest ht = createHit(layer, w, HitTest.HitType.NOISE);
						_hits.add(ht);
					}
				}
			} // layer loop
		}
	}

	/**
	 * Load all the hits into the bit data for noise reduction.
	 */
	public void loadBitData() {
		for (int i = 0; i < _data.length; i++) {
			_data[i].clear();
		}

		for (HitTest ht : _hits) {
			_data[ht.getLayer()].setBit(ht.getWire());
		}
	}

	/**
	 * Apply the noise reduction algorithm.
	 */
	public void removeNoise() {
		_parameters.createWorkSpace();
		_parameters.setPackedData(_data);
		_parameters.removeNoise();
	}

	/**
	 * Mark the hit type for each hit
	 */
	public void markHits() {
		// mark hits according to result
		for (HitTest ht : _hits) {
			int layer = ht.getLayer();
			int wire = ht.getWire();

			if (_data[layer].checkBit(wire)) {

				if (_parameters.isNoiseHit(layer, wire)) {
					ht.setComputedHitType(HitTest.HitType.NOISE);
				} else {
					ht.setComputedHitType(HitTest.HitType.TRACK);
				}
			}
		}
	}

	/**
	 * Create a hit
	 * 
	 * @param layer the layer in question.
	 * @param wire  the wire in question.
	 * @param type  the type of hit.
	 * @return the created hit.
	 */
	protected HitTest createHit(int layer, int wire, HitTest.HitType type) {
		HitTest ht = new HitTest(layer, wire, type);
		return ht;
	}

	/**
	 * Initialize space
	 */
	public void initializeSpace() {

		// space for bitwise data
		_data = new ExtendedWord[_parameters.getNumLayer()];
		for (int i = 0; i < _parameters.getNumLayer(); i++) {
			_data[i] = new ExtendedWord(_parameters.getNumWire());
		}

	}

	/**
	 * See if there is a hit at that spot.
	 * 
	 * @param layer the layer to check.
	 * @param wire  the wire to check.
	 * @return the hit at the given layer and wire, or <code>null</code> if there is
	 *         none.
	 */
	public HitTest findHit(int layer, int wire) {
		for (HitTest ht : _hits) {
			if ((ht.getLayer() == layer) && (ht.getWire() == wire)) {
				return ht;
			}
		}
		return null;
	}

	/**
	 * Generate hits from a straight line track
	 * 
	 * @param world the world system.
	 * @param local the local system.
	 * @param tt    the track used to generate hits.
	 */
	public void hitsFromTrack(TrackTest tt) {

		Point2D.Double wp0 = tt.getStartPoint();
		Point2D.Double wp1 = tt.getEndPoint();
		int bracket[] = new int[2];

		// intersects the overall boundary?

		if (boundary.intersectsLine(wp0.x, wp0.y, wp1.x, wp1.y)) {

			Rectangle.Double wcell = new Rectangle.Double();
			for (int layer = 0; layer < _parameters.getNumLayer(); layer++) {
				// intersects layer?
				if (trackIntersectsLayer(layer, tt)) {

					this.bracketTrack(tt, layer, bracket);
					for (int wire = bracket[0]; wire <= bracket[1]; wire++) {
						cellWorldBounds(layer, wire, wcell);

						if (wcell.intersectsLine(wp0.x, wp0.y, wp1.x, wp1.y)) {
							// remove noise hit
							HitTest ht = findHit(layer, wire);
							if (ht != null) {
								_hits.remove(ht);
							}

							if (DetectorTest.getRandom().nextDouble() > TestParameters.getProbBadWire()) {
								HitTest htnew = createHit(layer, wire, HitTest.HitType.TRACK);
								_hits.add(htnew);
							}
						}
					}
				} // intersects layer
			}
		} // intersects boundary
	}

	/**
	 * Bracket the range of wires that might be intersected by the track. This is to
	 * speed up the process.
	 * 
	 * @param tt      the track in question.
	 * @param layer   the layer--already assumed that the layer test has been
	 *                passed.
	 * @param bracket int array with two elements. Upon return, search from
	 *                bracket[0] to bracket[1].
	 */
	protected void bracketTrack(TrackTest tt, int layer, int[] bracket) {
		int left = 0;
		int right = _parameters.getNumWire() - 1;
		bracket[0] = left;
		bracket[1] = right;

		while ((right - left) > 1) {
			int nmid = (right + left) / 2;
			boolean inLeft = trackIntersectsRange(layer, left, nmid - 1, tt);
			boolean inRight = trackIntersectsRange(layer, nmid, right, tt);
			if (inLeft && inRight) {
				bracket[0] = left;
				bracket[1] = right;
				return;
			}
			if (inLeft) {
				right = nmid - 1;
			} else if (inRight) {
				left = nmid;
			} else { // shouldn't happen of layer test passed
				System.out.println("That's rarely a good sign");
				bracket[0] = -1;
				bracket[1] = -2;
				return;
			}
		}
		bracket[0] = left;
		bracket[1] = right;
	}

	/**
	 * Check if a track intersects a layer.
	 * 
	 * @param layer the layer to check.
	 * @param tt    the track to test.
	 * @return <code>true</code> if the track intersects the layer.
	 */
	protected boolean trackIntersectsLayer(int layer, TrackTest tt) {
		Point2D.Double wp0 = tt.getStartPoint();
		Point2D.Double wp1 = tt.getEndPoint();

		Rectangle2D.Double wr = new Rectangle2D.Double();
		layerWorldBounds(layer, wr);

		return wr.intersectsLine(wp0.x, wp0.y, wp1.x, wp1.y);
	}

	/**
	 * Check if a track intersects a range of wires.
	 * 
	 * @param layer   the layer to check.
	 * @param minwire the min wire of the range.
	 * @param maxwire the max wire of the range.
	 * @param tt      the track to test.
	 * @return <code>true</code> if the track intersects the layer.
	 */
	protected boolean trackIntersectsRange(int layer, int minwire, int maxwire, TrackTest tt) {
		Point2D.Double wp0 = tt.getStartPoint();
		Point2D.Double wp1 = tt.getEndPoint();

		Rectangle2D.Double wr = new Rectangle2D.Double();
		cellRangeWorldBounds(layer, minwire, maxwire, wr);

		return wr.intersectsLine(wp0.x, wp0.y, wp1.x, wp1.y);
	}

	/**
	 * clear all the hits
	 */
	public void clearHits() {
		_hits.removeAllElements();
	}

	/**
	 * Get the fractional raw occupancy of this chamber.
	 * 
	 * @return the fractional occupancy of this chamber.
	 */
	public double getOccupancy() {
		return ((double) getNumHits() / getTotalNumWires());
	}
	
	/**
	 * Get the fractional cleaned occupancy of this chamber.
	 * 
	 * @return the fractional occupancy of this chamber.
	 */
	public double getReducedOccupancy() {
		return ((double) (getNumHits()-this.getNumRemovedNoiseHits()) / getTotalNumWires());
	}


	/**
	 * Get the total number of hits.
	 * 
	 * @return the number of hits;
	 */
	public int getNumHits() {
		return _hits.size();
	}

	// get number of noise hits
	public int getNumNoiseHits() {
		int num = 0;
		for (HitTest th : _hits) {
			if (th.getActualHitType() == HitTest.HitType.NOISE) {
				num++;
			}
		}
		return num;
	}

	// get number of noise hits not removed
	public int getNumSavedNoiseHits() {
		int num = 0;
		for (HitTest th : _hits) {
			if (th.getComputedHitType() == HitTest.HitType.TRACK && th.getActualHitType() == HitTest.HitType.NOISE) {
				num++;
			}
		}
		return num;
	}

	public int getNumRemovedNoiseHits() {
		int num = 0;
		for (HitTest th : _hits) {
			if (th.getComputedHitType() != HitTest.HitType.TRACK && th.getActualHitType() == HitTest.HitType.NOISE) {
				num++;
			}
		}
		return num;
	}

	// get number of hits from segments
	public int getNumTrackHits() {
		int num = 0;
		for (HitTest th : _hits) {

			if (th.getComputedHitType() == HitTest.HitType.TRACK && th.getActualHitType() == HitTest.HitType.TRACK) {
				num++;
			}
		}
		return num;
	}

	/**
	 * Get the total number of wires.
	 * 
	 * @return the total number of wires in all layers.
	 */
	public int getTotalNumWires() {
		return _parameters.getNumLayer() * _parameters.getNumWire();
	}

}
