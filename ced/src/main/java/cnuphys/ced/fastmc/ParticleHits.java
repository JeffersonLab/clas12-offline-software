package cnuphys.ced.fastmc;

import java.awt.Color;
import java.util.List;

import org.jlab.geom.DetectorHit;
import org.jlab.geom.prim.Path3D;

import cnuphys.ced.geometry.DCGeometry;
import cnuphys.ced.geometry.FTOFGeometry;
import cnuphys.lund.LundId;

public class ParticleHits {

	private LundId _lundId;
	
	//drift chamber hits
	private List<DetectorHit> _dcHits;
	
	//ftof hits
	private List<DetectorHit> _ftofHits;
	
	//ftof layer hits
	private List<DetectorHit>[][] _ftofLayerHits = new List[6][3];
	
	public ParticleHits(LundId lundId, Path3D path) {
		_lundId = lundId;
		int charge = lundId.getCharge();

		if (charge != 0) {
			_dcHits = DCGeometry.getHits(path);
			_ftofHits = FTOFGeometry.getHits(path);
//			System.err.println("DC HIT COUNT: " + DCHitCount());
//			System.err.println("FTOF HIT COUNT: " + FTOFHitCount());
		
			for (int sect0 = 0; sect0 < 6; sect0++) {
				for (int ptype = 0; ptype < 3; ptype++) {
					_ftofLayerHits[sect0][ptype] = FTOFGeometry.getHits(sect0, ptype, path);
//					System.err.println("FTOF LAYER HIT COUNT SECT: " + (sect0+1) + "  PANEL TYPE: " + ptype + " count: " + FTOFLayerHitCount(sect0, ptype));
				}
			}
			
		}
	}
	
	public int FTOFLayerHitCount(int sect0, int ptype) {
		 List<DetectorHit> hits = _ftofLayerHits[sect0][ptype];
		 return (hits == null) ? 0 : hits.size();
	}
	
	/**
	 * Get the number of DC Hits
	 * @return the number of DC Hits
	 */
	public int DCHitCount() {
		return (_dcHits == null) ? 0 : _dcHits.size();
	}
	
	/**
	 * Get the number of FTOF Hits
	 * @return the number of FTOF Hits
	 */
	public int FTOFHitCount() {
		return (_ftofHits == null) ? 0 : _ftofHits.size();
	}
	
	public List<DetectorHit> getTOFLayerHits(int sect0, int ptype) {
		return _ftofLayerHits[sect0][ptype];
	}
	
	/**
	 * Get the list of ftof hits
	 * @return list of ftof hits
	 */
	public List<DetectorHit> getFTOFHits() {
		return _ftofHits;
	}

	/**
	 * Get the list of drift chamber hits
	 * @return list of drift chamber hits
	 */
	public List<DetectorHit> getDCHits() {
		return _dcHits;
	}

	/**
	 * Get the Lund Id
	 * @return rge LundId obhject
	 */
	public LundId getLundId() {
		return _lundId;
	}
	
	/**
	 * Get the integer Lund Id
	 * @return the integer Lund Id
	 */
	public int lundId() {
		return ((_lundId != null) ? _lundId.getId() : 0);
	}

	/**
	 * Get the line color for this particle
	 * @return the line color for this particle
	 */
     public Color getLineColor() {
		if (_lundId == null) {
			return Color.black;
		}
		return _lundId.getStyle().getLineColor();
	}

	/**
	 * Get the fill color for this particle
	 * @return the fill color for this particle
	 */
	public Color getFillColor() {
		if (_lundId == null) {
			return Color.gray;
		}
		return _lundId.getStyle().getFillColor();
	}
}
