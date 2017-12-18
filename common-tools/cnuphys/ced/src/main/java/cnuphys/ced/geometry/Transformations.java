package cnuphys.ced.geometry;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.detector.ec.ECDetector;
import org.jlab.geom.detector.ec.ECFactory;
import org.jlab.geom.detector.ec.ECLayer;
import org.jlab.geom.detector.ec.ECSector;
import org.jlab.geom.detector.ec.ECSuperlayer;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformable;
import org.jlab.geom.prim.Transformation3D;

public class Transformations {

	private DetectorType _detectorType;

	private Transformation3D _localToSector;
	private Transformation3D _sectorToLocal;

	public Transformations(DetectorType dtype) {
		_detectorType = dtype;

		// BST, DC, EC_INNER, EC_OUTER, FTOT, PCAL
		switch (dtype) {

		case BST:
			break;

		case DC:
			break;

		case EC_INNER:
			initCal(1); // 1 for ec inner
			break;

		case EC_OUTER:
			initCal(2); // 2 for ec outer
			break;
			
		case FTOT:
			break;

		case PCAL:
			initCal(0); // 0 for pcal
			break;
		}

	}

	// init for cal superlayer = (0,1,2) for PCAL, EC_IN, EC_OUT
	private void initCal(int superlayer) {
		ConstantProvider provider = 
				GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.ECAL);
		ECFactory ecFactory = new ECFactory();

		// detector in sector coordinates
		ECDetector clasDetector = ecFactory.createDetectorSector(provider);

		ECSector clas_sector = clasDetector.getSector(0);

		// superlayer 0 for pcal
		ECSuperlayer clas_ecSuperlayer = clas_sector.getSuperlayer(superlayer);

		// layer 0 for U
		ECLayer clas_ecLayerU = clas_ecSuperlayer.getLayer(0);

		_localToSector = clas_ecLayerU.getTransformation();
		_sectorToLocal = _localToSector.inverse();
	}

	/**
	 * Convert from the local system to the sector system
	 * 
	 * @param txf
	 *            a Transferable in the local system that will be modified to be
	 *            in the sector system
	 */
	public void localToSector(Transformable txf) {
		_localToSector.apply(txf);
	}

	/**
	 * Convert from the sector system to the local system
	 * 
	 * @param txf
	 *            a Transferable in the sector system that will be modified to
	 *            be in the local system
	 */
	public void sectorToLocal(Transformable txf) {
		_sectorToLocal.apply(txf);
	}

	/**
	 * Convert from the local system to the sector system
	 * 
	 * @param localP
	 *            a point in the local system (not modified)
	 * @param sectorP
	 *            a point in the sector system (modified)
	 */
	public void localToSector(Point3D localP, Point3D sectorP) {
		sectorP.set(localP.x(), localP.y(), localP.z());
		_localToSector.apply(sectorP);
	}

	/**
	 * Convert from the sector system to the local system
	 * 
	 * @param localP
	 *            a point in the local system (modified)
	 * @param sectorP
	 *            a point in the sector system (not modified)
	 */
	public void sectorToLocal(Point3D localP, Point3D sectorP) {
		localP.set(sectorP.x(), sectorP.y(), sectorP.z());
		_sectorToLocal.apply(localP);
	}

	/**
	 * Convert from the sector system to the local system
	 * 
	 * @param localP
	 *            a point in the local system (modified)
	 * @param clasP
	 *            a point in the clas (lab) system (not modified)
	 */
	public void clasToLocal(Point3D localP, Point3D clasP) {
		Point3D sectorP = new Point3D();
		GeometryManager.clasToSector(clasP, sectorP);
		sectorToLocal(localP, sectorP);
	}

	/**
	 * Convert from the clas (lab) system to the local system
	 * 
	 * @param localP
	 *            a point in the local system (not modified)
	 * @param clasP
	 *            a point in the clas (lab) system (modified)
	 */
	public void localToClas(int sector, Point3D localP, Point3D clasP) {
		Point3D sectorP = new Point3D();
		localToSector(localP, sectorP);
		GeometryManager.sectorToClas(sector, clasP, sectorP);
	}

	/**
	 * Get the detector type of the transformation
	 * 
	 * @return the detector type
	 */
	public DetectorType getDetectorType() {
		return _detectorType;
	}
}
