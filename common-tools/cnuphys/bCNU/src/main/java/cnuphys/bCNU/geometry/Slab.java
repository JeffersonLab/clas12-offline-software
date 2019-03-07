package cnuphys.bCNU.geometry;

/**
 * A slab is a rectangular solid. It is a polytube with exactly four lines,
 * no more no less
 * @author heddle
 *
 */
public class Slab extends Polytube {

	//coords used in 3D OpenGL drawing
	private float _coords[];
	
	/**
	 * Create a Slab made from 4 lines in rotational order
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
	 * Add a line. Trying to add past four causes an exception
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
	 * @param object the line you are mistakenly trying to remove
	 * @return false
	 */
	@Override
	public boolean remove(Object object) {
		return false;
	}
	
	/**
	 * Slabs are immutable. You cannot remove lines
	 * @param index the index of line you are mistakenly trying to remove
	 * @return null
	 */
	@Override	public Line remove(int index) {
		return null;
	}
	
	/**
	 * Get the corners into a coordinate array of the type needed for
	 * OpenGL drawing
	 * @return a float array 24 entries (x1,y1,z1), ...
	 * (x8, y8, z8) for the 8 corners of the slab
	 */
	public float[] getCoords() {
		if (_coords == null) {
			//8 corners --> 24 numbers
			_coords = new float[24];
			
			for (int i = 0; i < 8; i++) {
				//first half start point, second half end point
				int end = (i < 4) ? Constants.START : Constants.END;
				int index = i*3;
				Line line = get(i % 4);
				insert(_coords, index, line.getEndpoint(end));
			}
		}
		return _coords;
	}
	
	//convenience method for stuffing an array
	private void insert(float coords[], int index, Point p) {
		coords[index++] = (float)p.x;
		coords[index++] = (float)p.y;
		coords[index] = (float)p.z;
	}
	
	/**
	 * Get a quad for OpenGL drawing. This will be one of the faces based on the index.
	 * With a canonical rectangular solid in mind:
	 * index = 0 front
	 * index = 1 top
	 * index = 2 right
	 * index = 3 bottom
	 * index = 4 left
	 * index = 5 back
	 * @param index the index
	 * @param quad should have at least 4*3 = 12 entries  for the 4 corners of the quad
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
}
