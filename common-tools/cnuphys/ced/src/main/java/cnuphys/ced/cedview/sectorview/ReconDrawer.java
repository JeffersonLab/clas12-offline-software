package cnuphys.ced.cedview.sectorview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.view.FBData;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.AIDC;
import cnuphys.ced.event.data.AllEC;
import cnuphys.ced.event.data.Cluster;
import cnuphys.ced.event.data.ClusterList;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DCCluster;
import cnuphys.ced.event.data.DCClusterList;
import cnuphys.ced.event.data.DCReconHit;
import cnuphys.ced.event.data.DCReconHitList;
import cnuphys.ced.event.data.DCTdcHit;
import cnuphys.ced.event.data.DCTdcHitList;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.event.data.Hit1;
import cnuphys.ced.event.data.Hit1List;
import cnuphys.ced.event.data.RECCalorimeter;
import cnuphys.ced.frame.CedColors;
import cnuphys.lund.LundId;

public class ReconDrawer extends SectorViewDrawer {
	
	// the event manager
	ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();
	
	//the current event
	private DataEvent _currentEvent;
	
	// cached for feedback
	private ArrayList<FBData> _fbData = new ArrayList<>();


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
		
		_fbData.clear();


		if (_eventManager.isAccumulating()) {
			return;
		}

		if (!_view.isSingleEventMode()) {
			return;
		}
		
		_currentEvent = _eventManager.getCurrentEvent();


		// DC HB and TB Hits
		drawDCReconAndDOCA(g, container);

		// neural net overlays
		if (_view.showNN()) {
			drawNeuralNetOverlays(g, container);
		}

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
		
		if (_view.showAIDCHBSegments()) {
			for (int supl = 1; supl <= 6; supl++) {
				_view.getSuperLayerDrawer(0, supl).drawAIHitBasedSegments(g, container);
			}
		}

		if (_view.showAIDCTBSegments()) {
			for (int supl = 1; supl <= 6; supl++) {
				_view.getSuperLayerDrawer(0, supl).drawAITimeBasedSegments(g, container);
			}
		}
		
		if (_view.showRecCal()) {
			drawRecCal(g, container);
		}

	}
	
	// draw data from the REC::Calorimeter bank
	private void drawRecCal(Graphics g, IContainer container) {
		
		RECCalorimeter recCal = RECCalorimeter.getInstance();
		if (recCal.isEmpty()) {
			return;
		}

		
		Point pp = new Point();
		Point2D.Double wp = new Point2D.Double();
		Rectangle2D.Double wr = new Rectangle2D.Double();

		
		for (int i = 0; i < recCal.count; i++) {
			if (_view.containsSector(recCal.sector[i])) {
				
				_view.projectClasToWorld(recCal.x[i], recCal.y[i], recCal.z[i], _view.getProjectionPlane(), wp);
				container.worldToLocal(pp, wp);
				SymbolDraw.drawDavid(g, pp.x, pp.y, 4, Color.black, Color.red);			
				
				double r = Math.sqrt(recCal.x[i]*recCal.x[i] + recCal.y[i]*recCal.y[i] + recCal.z[i]*recCal.z[i]);
				double theta = Math.toDegrees(Math.acos(recCal.z[i]/r));
				double phi = Math.toDegrees(Math.atan2(recCal.y[i], recCal.x[i]));
				
				
				float radius = recCal.getRadius(recCal.energy[i]);
				if (radius > 0) {
					container.localToWorld(pp, wp);
					wr.setRect(wp.x - radius, wp.y - radius, 2 * radius, 2 * radius);
					
					LundId lid = recCal.getLundId(i);


					if (recCal.layer[i] < 4) { // pcal
						Color color = (lid == null) ? CedColors.RECPcalFill : lid.getStyle().getTransparentFillColor();
						WorldGraphicsUtilities.drawWorldOval(g, container, wr, color, null);
					} else {
						Color color = (lid == null) ? CedColors.RECEcalFill : lid.getStyle().getTransparentFillColor();
						WorldGraphicsUtilities.drawWorldOval(g, container, wr, color, null);
					}
				}
				
				_fbData.add(new FBData(pp,
						String.format("$magenta$REC xyz (%-6.3f, %-6.3f, %-6.3f) cm", recCal.x[i], recCal.y[i],
								recCal.z[i]),
						String.format("$magenta$REC %s (%-6.3f, %-6.3f, %-6.3f)", CedView.rThetaPhi, r, theta, phi), 
						String.format("$magenta$REC layer %d", recCal.layer[i]),
						String.format("$magenta$%s", recCal.getPIDStr(i)),
						String.format("$magenta$REC Energy %-7.4f GeV", recCal.energy[i])));

			}
		} //for i
		
	}

	// draw neural net overlays
	private void drawNeuralNetOverlays(Graphics g, IContainer container) {
		DCTdcHitList hits = DC.getInstance().getTDCHits();
		if ((hits != null) && !hits.isEmpty()) {

			for (DCTdcHit hit : hits) {
				if (hit.nnHit) {
					if (_view.containsSector(hit.sector)) {
						_view.drawDCRawHit(g, container, CedColors.NN_TRANS, Color.black, hit);				}
				}
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

	// draw reconstructed DC hit Hit based and time based based hits
	private void drawDCReconAndDOCA(Graphics g, IContainer container) {
		if (_view.showDCHBHits()) {
			drawDCHitList(g, container, CedColors.HB_COLOR, DC.getInstance().getHBHits(), false);
		}
		if (_view.showDCTBHits()) {
			drawDCHitList(g, container, CedColors.TB_COLOR, DC.getInstance().getTBHits(), true);
		}
		if (_view.showAIDCHBHits()) {
			drawDCHitList(g, container, CedColors.AIHB_COLOR, AIDC.getInstance().getAIHBHits(), false);
		}
		if (_view.showAIDCTBHits()) {
			drawDCHitList(g, container, CedColors.AITB_COLOR, AIDC.getInstance().getAITBHits(), true);
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
		
		
		//data from REC::Calorimeter
		if (_view.showRecCal()) {
			for (FBData fbdata : _fbData) {
				boolean added = fbdata.addFeedback(screenPoint, feedbackStrings);
				if (added) {
					break;
				}
			}
		}

		// DC HB Recon Hits
		if (_view.showDCHBHits()) {
			DCReconHitList hits = DC.getInstance().getHBHits();
			DCClusterList clusters = DC.getInstance().getHBClusters();
			
			if ((hits != null) && !hits.isEmpty()) {
				for (DCReconHit hit : hits) {
					if (_view.containsSector(hit.sector)) {
						if (hit.contains(screenPoint)) {
							hit.getFeedbackStrings("HB", feedbackStrings);
							
							
							//possibly have cluster info
							short clusterId = hit.clusterID;
							
							if (clusterId > 0) {
								DCCluster cluster = clusters.fromClusterId(clusterId);
								
								String str1;
								if (cluster == null) {
									str1 = String.format("$red$" + "HB clusterID %d", clusterId);
								} else {
									str1 = String.format("$red$" + "HB clusterID %d size %d", clusterId, cluster.size);
								}
								feedbackStrings.add(str1);
							}
							
							return;
						}
					}
				}
			}
		}  //show hb hits
		
		// DC TB Recon Hits
		if (_view.showDCTBHits()) {
			DCReconHitList hits = DC.getInstance().getTBHits();
			DCClusterList clusters = DC.getInstance().getTBClusters();
			
			if ((hits != null) && !hits.isEmpty()) {
				for (DCReconHit hit : hits) {
					if (_view.containsSector(hit.sector)) {
						if (hit.contains(screenPoint)) {
							hit.getFeedbackStrings("TB", feedbackStrings);
							
							
							//possibly have cluster info
							short clusterId = hit.clusterID;
							
							if (clusterId > 0) {
								DCCluster cluster = clusters.fromClusterId(clusterId);
								
								String str1;
								if (cluster == null) {
									str1 = String.format("$red$" + "TB clusterID %d", clusterId);
								} else {
									str1 = String.format("$red$" + "TB clusterID %d size %d", clusterId, cluster.size);
								}
								feedbackStrings.add(str1);
							}
							
							return;
						}
					}
				}
			}
		}  //show tb hits



		// AI DC HB Recon Hits
		if (_view.showAIDCHBHits()) {
			DCReconHitList hits = AIDC.getInstance().getAIHBHits();
			DCClusterList clusters = AIDC.getInstance().getAIHBClusters();
			
			if ((hits != null) && !hits.isEmpty()) {
				for (DCReconHit hit : hits) {
					if (_view.containsSector(hit.sector)) {
						if (hit.contains(screenPoint)) {
							hit.getFeedbackStrings("AI HB", feedbackStrings);
							
							
							//possibly have cluster info
							short clusterId = hit.clusterID;
							
							if (clusterId > 0) {
								DCCluster cluster = clusters.fromClusterId(clusterId);
								
								String str1;
								if (cluster == null) {
									str1 = String.format("$red$" + "HB clusterID %d", clusterId);
								} else {
									str1 = String.format("$red$" + "HB clusterID %d size %d", clusterId, cluster.size);
								}
								feedbackStrings.add(str1);
							}
							
							return;
						}
					}
				}
			}
		}  //show AI hb hits
		
		// AI DC TB Recon Hits
		if (_view.showAIDCTBHits()) {
			DCReconHitList hits = AIDC.getInstance().getAITBHits();
			DCClusterList clusters = AIDC.getInstance().getAITBClusters();
			
			if ((hits != null) && !hits.isEmpty()) {
				for (DCReconHit hit : hits) {
					if (_view.containsSector(hit.sector)) {
						if (hit.contains(screenPoint)) {
							hit.getFeedbackStrings("AI TB", feedbackStrings);
							
							
							//possibly have cluster info
							short clusterId = hit.clusterID;
							
							if (clusterId > 0) {
								DCCluster cluster = clusters.fromClusterId(clusterId);
								
								String str1;
								if (cluster == null) {
									str1 = String.format("$red$" + "TB clusterID %d", clusterId);
								} else {
									str1 = String.format("$red$" + "TB clusterID %d size %d", clusterId, cluster.size);
								}
								feedbackStrings.add(str1);
							}
							
							return;
						}
					}
				}
			}
		}  //show AI tb hits


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
	private void drawDCHitList(Graphics g, IContainer container, Color fillColor, DCReconHitList hits, boolean isTimeBased) {
		if ((hits == null) || hits.isEmpty()) {
			return;
		}

		for (DCReconHit hit : hits) {
			if (_view.containsSector(hit.sector)) {
				_view.drawDCReconHit(g, container, fillColor, Color.black, hit, isTimeBased);
			}
		}

	}

}
