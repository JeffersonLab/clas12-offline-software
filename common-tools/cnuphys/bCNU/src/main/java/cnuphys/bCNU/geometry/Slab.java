package cnuphys.bCNU.geometry;

/**
 * A slab is a rectangular solid. It is a polytube with exactly four lines, no
 * more no less
 * 
 * @author heddle
 *
 */
public class Slab extends Polytube {

	// coords used in 3D OpenGL drawing
	private float _coords[];

	/**
	 * Create a Slab made from 4 lines in rotational order. The front of the slab
	 * will be quad made of the start points of the lines. The back will be the end
	 * points.
	 * 
	 * @param l1
	 * @param l2
	 * @param l3
	 * @param L4
	 */
	public Slab(Line l1, Line l2, Line l3, Line l4) {
		add(l1);
		add(l2);
		add(l3);
		add(l4);
	}

	/**
	 * Make a slab from 8 points
	 * 
	 * @param s1 the start of line (edge) 1
	 * @param e1 the end of line (edge) 1
	 * @param s2 the start of line (edge) 2
	 * @param e2 the end of line (edge) 2
	 * @param s3 the start of line (edge) 3
	 * @param e3 the end of line (edge) 3
	 * @param s4 the start of line (edge) 4
	 * @param e4 the end of line (edge) 4
	 */
	public Slab(Point s1, Point e1, Point s2, Point e2, Point s3, Point e3, Point s4, Point e4) {
		this(new Line(s1, e1), new Line(s2, e2), new Line(s3, e3), new Line(s4, e4));
	}

	/**
	 * Add a line. Trying to add past four causes an exception
	 * 
	 * @param line the line to add
	 */
	@Override
	public boolean add(Line line) {
		if (size() == 4) {
			throw new IndexOutOfBoundsException("Only four lines in a Slab");
		}

		return super.add(line);
	}

	/**
	 * Slabs are immutable. You cannot remove lines
	 * 
	 * @param object the line you are mistakenly trying to remove
	 * @return false
	 */
	@Override
	public boolean remove(Object object) {
		return false;
	}

	/**
	 * Slabs are immutable. You cannot remove lines
	 * 
	 * @param index the index of line you are mistakenly trying to remove
	 * @return null
	 */
	@Override
	public Line remove(int index) {
		return null;
	}

	/**
	 * Get the corners into a coordinate array of the type needed for OpenGL drawing
	 * 
	 * @return a float array 24 entries (x1,y1,z1), ... (x8, y8, z8) for the 8
	 *         corners of the slab
	 */
	public float[] getCoords() {
		if (_coords == null) {
			// 8 corners --> 24 numbers
			_coords = new float[24];

			for (int i = 0; i < 8; i++) {
				// first half start point, second half end point
				int end = (i < 4) ? Constants.START : Constants.END;
				int index = i * 3;
				Line line = get(i % 4);
				insert(_coords, index, line.getEndpoint(end));
			}
		}
		return _coords;
	}

	// convenience method for stuffing an array
	private void insert(float coords[], int index, Point p) {
		coords[index++] = (float) p.x;
		coords[index++] = (float) p.y;
		coords[index] = (float) p.z;
	}

	/**
	 * Get a quad for OpenGL drawing. This will be one of the faces based on the
	 * index. With a canonical rectangular solid in mind: index = 0 front (start
	 * points of all 4 lines) index = 1 top endpoints (start and end) of lines 1 and
	 * 2 index = 2 right endpoints (start and end) of lines 2 and 3 index = 3 bottom
	 * endpoints (start and end) of lines 3 and 4 index = 4 left endpoints (start
	 * and end) of lines 4 and 1 index = 5 back (end points of all 4 lines)
	 * 
	 * @param index the index
	 * @param quad  should have at least 4*3 = 12 entries for the 4 corners of the
	 *              quad
	 */
	public void getQuad(int index, float quad[]) {
		if ((index < 0) || (index > 5)) {
			throw new IllegalArgumentException("Bad index in Slab.getQuad: " + index);
		}

		switch (index) {
		case 0:
			insert(quad, 0, get(0).getP0());
			insert(quad, 3, get(1).getP0());
			insert(quad, 6, get(2).getP0());
			insert(quad, 9, get(3).getP0());
			break;

		case 1:
			insert(quad, 0, get(0).getP0());
			insert(quad, 3, get(0).getP1());
			insert(quad, 6, get(1).getP1());
			insert(quad, 9, get(1).getP0());
			break;

		case 2:
			insert(quad, 0, get(1).getP0());
			insert(quad, 3, get(1).getP1());
			insert(quad, 6, get(2).getP1());
			insert(quad, 9, get(2).getP0());
			break;

		case 3:
			insert(quad, 0, get(2).getP0());
			insert(quad, 3, get(2).getP1());
			insert(quad, 6, get(3).getP1());
			insert(quad, 9, get(3).getP0());
			break;

		case 4:
			insert(quad, 0, get(3).getP0());
			insert(quad, 3, get(3).getP1());
			insert(quad, 6, get(0).getP1());
			insert(quad, 9, get(0).getP0());
			break;

		case 5:
			insert(quad, 0, get(0).getP1());
			insert(quad, 3, get(1).getP1());
			insert(quad, 6, get(2).getP1());
			insert(quad, 9, get(3).getP1());
			break;

		}
	}
	
	/**
	 * Get all six quads at once
	 * @param quads all the quads in canonical order
	 */
	public void getQuads(float quads[]) {
		insert(quads, 0, get(0).getP0());
		insert(quads, 3, get(1).getP0());
		insert(quads, 6, get(2).getP0());
		insert(quads, 9, get(3).getP0());
		
		insert(quads, 12, get(0).getP0());
		insert(quads, 15, get(0).getP1());
		insert(quads, 18, get(1).getP1());
		insert(quads, 21, get(1).getP0());

		insert(quads, 24, get(1).getP0());
		insert(quads, 27, get(1).getP1());
		insert(quads, 30, get(2).getP1());
		insert(quads, 33, get(2).getP0());
		
		insert(quads, 36, get(2).getP0());
		insert(quads, 39, get(2).getP1());
		insert(quads, 42, get(3).getP1());
		insert(quads, 45, get(3).getP0());

		insert(quads, 48, get(3).getP0());
		insert(quads, 51, get(3).getP1());
		insert(quads, 54, get(0).getP1());
		insert(quads, 57, get(0).getP0());
		
		insert(quads, 60, get(0).getP1());
		insert(quads, 63, get(1).getP1());
		insert(quads, 66, get(2).getP1());
		insert(quads, 69, get(3).getP1());


	}
}
