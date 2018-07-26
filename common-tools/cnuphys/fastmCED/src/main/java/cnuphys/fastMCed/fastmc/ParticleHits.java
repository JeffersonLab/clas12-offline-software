package cnuphys.fastMCed.fastmc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import org.jlab.geom.DetectorId;
import org.jlab.geom.prim.Path3D;

import cnuphys.fastMCed.geometry.DCGeometry;
import cnuphys.fastMCed.geometry.FTOFGeometry;
import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.lund.LundId;
import cnuphys.splot.plot.DoubleFormat;

/**
 * These are the hits as determined by the fastMC engine for a single
 * trajectory.
 * 
 * @author heddle
 *
 */
public class ParticleHits {

	// the particle lund id object
	private LundId _lundId;

	// the 3D path that generated the hits
	private Path3D _path;
	
	//contains the vertex and moment information
	private GeneratedParticleRecord _genParticleRecord;

	/**
	 * A mapping of the detector type to a list of augmented detector hits
	 */
	private EnumMap<DetectorId, HitHolder> hits;

	/**
	 * The particle hits for a single trajectory as determined by fastMC
	 * 
	 * @param lundId
	 *            the Lund Id
	 * @ parm particleRec
	 *            contains the vertex and momentum information
	 * @param path
	 *            the path
	 */
	public ParticleHits(LundId lundId, GeneratedParticleRecord particleRec, Path3D path) {
		_lundId = lundId;
		_genParticleRecord = particleRec;
		_path = path;
		int charge = lundId.getCharge();

		hits = new EnumMap<>(DetectorId.class);
		if (charge != 0) {
			HitHolder dcHH = new HitHolder(6, 6);
			HitHolder ftofHH = new HitHolder(6, 0);
			
			dcHH.fill(DCGeometry.getHits(path));
			ftofHH.fill(FTOFGeometry.getHits(path));
			hits.put(DetectorId.DC, dcHH);
			hits.put(DetectorId.FTOF, ftofHH);
		}
	}

	/**
	 * This obtains the path that FastMC used to generate the hits
	 * 
	 * @return the path that FastMC used to generate the hits
	 */
	public Path3D getPath() {
		return _path;
	}

	/**
	 * Get the total number of hits across all sectors, superLayers
	 * and Layers for a given detector type
	 * @param id the DetorId enum value
	 * @return the total number of hits
	 */
	public int totalHitCount(DetectorId id) {
		HitHolder holder = hits.get(id);
		if (holder == null) {
			return 0;
		}
		else {
			return holder.totalHitCount();
		}
	}
	
	/**
	 * Get the total hit count for a given sector
	 * @param id the DetorId enum value
	 * @param sect0
	 *            the zero based sector
	 * @return the total hit count for a sector
	 */
	public int sectorHitCount(DetectorId id, int sect0) {
		HitHolder holder = hits.get(id);
		if (holder == null) {
			return 0;
		}
		else {
			return holder.sectorHitCount(sect0);
		}
	}
	
	/**
	 * Get the total hit count for a given sector
	 * @param id the DetorId enum value
	 * @param sect0
	 *            the zero based sector
	 * @param supl0 the zero based superlayer
	 * @return the the total hit count for a given sector and superLayer
	 */
	public int superLayerHitCount(DetectorId id, int sect0, int supl0) {
		HitHolder holder = hits.get(id);
		if (holder == null) {
			return 0;
		}
		else {
			return holder.hitCount(sect0, supl0);
		}
	}

	/**
	 * Get all the hits, all sectors and superLayers
	 * @return all the hits, all sectors and superLayers
	 */
	public ArrayList<AugmentedDetectorHit> getAllHits(DetectorId id) {
		HitHolder holder = hits.get(id);
		if (holder == null) {
			return null;
		}
		else {
			return holder.getAllHits();
		}
	}
	/**
	 * Get the HitHolder
	 * 
	 * @param id
	 *            the detectorId enum value
	 */
	public HitHolder getHitHolder(DetectorId id) {
		return hits.get(id);
	}

	/**
	 * Get the list of hits
	 * 
	 * @param id
	 *            the detectorId enum value
	 * @param sect0
	 *            the zero based sector
	 * @param supl0
	 *            the zero based superLayer
	 * @return of list of hits with matching sector and superLayer and a mix of
	 *         layers and components
	 */
	public List<AugmentedDetectorHit> getHits(DetectorId id, int sect0, int supl0) {
		HitHolder holder = hits.get(id);
		if (holder == null) {
			return null;
		} else {
			return hits.get(id).getHits(sect0, supl0);
		}
	}

	/**
	 * Get the Lund Id object
	 * 
	 * @return the LundId object
	 */
	public LundId getLundId() {
		return _lundId;
	}
	
	/**
	 * Get the generated particle record for this trajectory/track.
	 * It contains information like the vertex and momentum.
	 * @return the generated particle record for this trajectory/track.
	 */
	public GeneratedParticleRecord getGeneratedParticleRecord() {
		return _genParticleRecord;
	}

	/**
	 * Get the integer Lund Id
	 * 
	 * @return the integer Lund Id
	 */
	public int lundId() {
		return ((_lundId != null) ? _lundId.getId() : 0);
	}

	/**
	 * Get the line color for this particle
	 * 
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
	 * 
	 * @return the fill color for this particle
	 */
	public Color getFillColor() {
		if (_lundId == null) {
			return Color.gray;
		}
		return _lundId.getStyle().getFillColor();
	}


	/**
	 * Add hit feedback data to the feedback strings
	 * 
	 * @param hit
	 *            the hit that the mouse is over
	 * @param lid
	 *            the Lund Id
	 * @param feedbackStrings
	 *            the list of feedbackstrings being added to
	 */
	public static void addHitFeedback(AugmentedDetectorHit hit, LundId lid, List<String> feedbackStrings) {
		if (hit != null) {
			String lidName = (lid != null) ? lid.getName() : "???";
			int lidId = (lid != null) ? lid.getId() : -99999;
			feedbackStrings.add("$yellow$pid " + lidName + " (" + lidId + ")");
			feedbackStrings.add("$yellow$energy dep " + DoubleFormat.doubleFormat(hit.getEnergy(), 4));
			feedbackStrings.add("$yellow$time " + DoubleFormat.doubleFormat(hit.getTime(), 4));

			if (hit.isDetectorHit(DetectorId.DC)) {
				feedbackStrings.add("$white$called noise by SNR: " + (hit.isNoise() ? "yes" : "no"));
			}
		}
	}

}
