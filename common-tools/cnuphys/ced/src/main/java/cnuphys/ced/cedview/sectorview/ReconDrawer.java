package cnuphys.ced.cedview.sectorview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.AllEC;
import cnuphys.ced.event.data.Cluster;
import cnuphys.ced.event.data.ClusterList;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DCHit;
import cnuphys.ced.event.data.DCHitList;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.event.data.Hit1;
import cnuphys.ced.event.data.Hit1List;
import cnuphys.ced.frame.CedColors;

public class ReconDrawer extends SectorViewDrawer {

	/**
	 * Reconstructed hits drawer
	 * 
	 * @param view
	 */
	public ReconDrawer(SectorView view) {
		super(view);
	}

	@Override
	public void draw(Graphics g, IContainer container) {

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		if (!_view.isSingleEventMode()) {
			return;
		}

		// DC HB and TB Hits
		drawDCReconAndDOCA(g, container);

		// Reconstructed FTOF hits
		if (_view.showReconHits()) {
			drawFTOFReconHits(g, container);
		}

		// Reconstructed clusters
		if (_view.showClusters()) {
			drawClusters(g, container);
		}

		if (_view.showDCHBSegments()) {
			for (int supl = 1; supl <= 6; supl++) {
				_view.getSuperLayerDrawer(0, supl).drawHitBasedSegments(g, container);
			}
		}

		if (_view.showDCTBSegments()) {
			for (int supl = 1; supl <= 6; supl++) {
				_view.getSuperLayerDrawer(0, supl).drawTimeBasedSegments(g, container);
			}
		}

	}

	// draw reconstructed clusters
	private void drawClusters(Graphics g, IContainer container) {
		drawClusterList(g, container, AllEC.getInstance().getClusters());
	}

	// draw FTOF reconstructed hits
	private void drawFTOFReconHits(Graphics g, IContainer container) {
		drawReconHitList(g, container, FTOF.getInstance().getHits());
	}

	// draw reconstructed DC hit based hits
	private void drawDCReconAndDOCA(Graphics g, IContainer container) {
		if (_view.showHB()) {
			drawDCHitList(g, container, CedColors.HB_COLOR, DC.getInstance().getHBHits(), false);
		}
		if (_view.showTB()) {
			drawDCHitList(g, container, CedColors.TB_COLOR, DC.getInstance().getTBHits(), true);
		}
	}

	/**
	 * Use what was drawn to generate feedback strings
	 * 
	 * @param container       the drawing container
	 * @param screenPoint     the mouse location
	 * @param worldPoint      the corresponding world location
	 * @param feedbackStrings add strings to this collection
	 * @param option          0 for hit based, 1 for time based
	 */
	@Override
	public void vdrawFeedback(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings, int option) {

		if (_view.showClusters()) {
			// EC
			ClusterList clusters = AllEC.getInstance().getClusters();
			if ((clusters != null) && !clusters.isEmpty()) {
				for (Cluster cluster : clusters) {
					if (_view.containsSector(cluster.sector)) {
						if (cluster.contains(screenPoint)) {
							cluster.getFeedbackStrings("EC", feedbackStrings);
							return;
						}
					}
				}
			}
		}

		if (_view.showReconHits()) {
			// FTOF
			Hit1List hits = FTOF.getInstance().getHits();
			if ((hits != null) && !hits.isEmpty()) {
				for (Hit1 hit : hits) {
					if (_view.containsSector(hit.sector)) {
						if (hit.contains(screenPoint)) {
							String hitStr1 = String.format("TOF hit sect %d panel %s  paddle %d", hit.sector,
									FTOF.getInstance().getBriefPanelName(hit.layer), hit.component);
							feedbackStrings.add("$red$" + hitStr1);
							String hitStr2 = String.format(
									"TOF hit energy  %7.3f MeV; hit phi %7.3f" + UnicodeSupport.DEGREE, hit.energy,
									hit.phi());
							feedbackStrings.add("$red$" + hitStr2);
							return;
						}
					}
				}

			}
		}

		// DC HB Recon Hits
		if (_view.showDCHBHits()) {
			DCHitList hits = DC.getInstance().getHBHits();
			if ((hits != null) && !hits.isEmpty()) {
				for (DCHit hit : hits) {
					if (_view.containsSector(hit.sector)) {
						if (hit.contains(screenPoint)) {
							hit.getFeedbackStrings("HB", feedbackStrings);
							return;
						}
					}
				}
			}
		}

	}

	// for writing out a vector
	private String vecStr(String prompt, double vx, double vy, double vz) {
		return vecStr(prompt, vx, vy, vz, 2);
	}

	// for writing out a vector
	private String vecStr(String prompt, double vx, double vy, double vz, int ndig) {
		return prompt + " (" + DoubleFormat.doubleFormat(vx, ndig) + ", " + DoubleFormat.doubleFormat(vy, ndig) + ", "
				+ DoubleFormat.doubleFormat(vz, ndig) + ")";
	}

	// draw a reconstructed cluster list
	private void drawClusterList(Graphics g, IContainer container, ClusterList clusters) {
		if ((clusters == null) || clusters.isEmpty()) {
			return;
		}

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();

		for (Cluster cluster : clusters) {
			if (_view.containsSector(cluster.sector)) {
				_view.projectClasToWorld(cluster.x, cluster.y, cluster.z, _view.getProjectionPlane(), wp);
				container.worldToLocal(pp, wp);
				cluster.setLocation(pp);
				DataDrawSupport.drawReconCluster(g, pp);
			}
		}
	}

	// draw a reconstructed hit list
	private void drawReconHitList(Graphics g, IContainer container, Hit1List hits) {
		if ((hits == null) || hits.isEmpty()) {
			return;
		}

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();

		for (Hit1 hit : hits) {
			if (_view.containsSector(hit.sector)) {
				_view.projectClasToWorld(hit.x, hit.y, hit.z, _view.getProjectionPlane(), wp);
				container.worldToLocal(pp, wp);
				hit.setLocation(pp);
				DataDrawSupport.drawReconHit(g, pp);
			}
		}

	}

	// draw a reconstructed hit list
	private void drawDCHitList(Graphics g, IContainer container, Color fillColor, DCHitList hits, boolean isTimeBased) {
		if ((hits == null) || hits.isEmpty()) {
			return;
		}

		for (DCHit hit : hits) {
			if (_view.containsSector(hit.sector)) {
				_view.drawDCHit(g, container, fillColor, Color.black, hit, isTimeBased);
			}
		}

	}

}
