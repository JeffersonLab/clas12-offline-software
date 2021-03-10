package cnuphys.ced.frame;

import cnuphys.bCNU.view.HistoGridView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.ced.geometry.FTOFGeometry;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.PlotPanel;

public class CedHistoFiller implements IAccumulationListener {

	// parent
	protected Ced ced;

	// ftof paddle counts

	public CedHistoFiller(Ced ced) {
		this.ced = ced;
		AccumulationManager.getInstance().addAccumulationListener(this);
	}

	/**
	 * Notification of accumulation events
	 */
	@Override
	public void accumulationEvent(int reason) {
		switch (reason) {

		case AccumulationManager.ACCUMULATION_CLEAR:
			clearHisto(ced.dcHistoGrid);
			clearHisto(ced.ftofHistoGrid);
			clearHisto(ced.bstHistoGrid);
			clearHisto(ced.pcalHistoGrid);
			clearHisto(ced.ecHistoGrid);
			break;

		case AccumulationManager.ACCUMULATION_STARTED:
			break;

		case AccumulationManager.ACCUMULATION_CANCELLED:
		case AccumulationManager.ACCUMULATION_FINISHED:
			Ced.setEventNumberLabel(ClasIoEventManager.getInstance().getSequentialEventNumber());

			fillDcHistogramGrid(ced.dcHistoGrid);
			fillFtofHistogramGrid(ced.ftofHistoGrid);
			fillBstHistogramGrid(ced.bstHistoGrid);
			fillPcalHistogramGrid(ced.pcalHistoGrid);
			fillEcHistogramGrid(ced.ecHistoGrid);
			break;
		}
	}

	// clear a grid
	private void clearHisto(HistoGridView grid) {
		if (grid != null) {
			grid.clear();
		}
	}

	// fill the pcal histo grid (view)
	private void fillPcalHistogramGrid(HistoGridView pcalGrid) {
		if (pcalGrid == null) {
			return;
		}

		// sect, plane (view) uvw, strip
		int hits[][][] = AccumulationManager.getInstance().getAccumulatedPCALData();

		for (int sect0 = 0; sect0 < 6; sect0++) {
			int row = sect0 + 1;
			for (int plane = 0; plane < 3; plane++) {
				int col = plane + 1;

				PlotPanel ppan = pcalGrid.getPlotPanel(row, col);
				HistoData hd = pcalGrid.getHistoData(row, col);

				for (int strip0 = 0; strip0 < PCALGeometry.PCAL_NUMSTRIP[plane]; strip0++) {
					hd.setCount(strip0, // do not add 1
							hits[sect0][plane][strip0]);
				}
				ppan.getCanvas().needsRedraw(true);
			}
		}

	}

	// fill the ec histo grid (view)
	private void fillEcHistogramGrid(HistoGridView ecGrid) {
		if (ecGrid == null) {
			return;
		}

		// sect, stack inner-outer, plane (view) uvw, strip
		int hits[][][][] = AccumulationManager.getInstance().getAccumulatedECALData();

		for (int sect0 = 0; sect0 < 6; sect0++) {
			int row = sect0 + 1;
			for (int stack = 0; stack < 2; stack++) {
				for (int plane = 0; plane < 3; plane++) {
					int col = (3 * stack) + plane + 1;

					PlotPanel ppan = ecGrid.getPlotPanel(row, col);
					HistoData hd = ecGrid.getHistoData(row, col);

					for (int strip0 = 0; strip0 < ECGeometry.EC_NUMSTRIP; strip0++) {
						hd.setCount(strip0, // do not add 1
								hits[sect0][stack][plane][strip0]);
					}
					ppan.getCanvas().needsRedraw(true);
				}
			}
		}

	}

	// fill the bst histo grid (view)
	private void fillBstHistogramGrid(HistoGridView bstGrid) {
		if (bstGrid == null) {
			return;
		}

		int hits[][][] = AccumulationManager.getInstance().getAccumulatedBSTFullData();
		for (int lay0 = 0; lay0 < 8; lay0++) {
			int row = lay0 + 1;
			int supl0 = lay0 / 2;
			for (int sect0 = 0; sect0 < BSTGeometry.sectorsPerSuperlayer[supl0]; sect0++) {
				int col = sect0 + 1;

				PlotPanel ppan = bstGrid.getPlotPanel(row, col);
				HistoData hd = bstGrid.getHistoData(row, col);

				for (int strip0 = 0; strip0 < 256; strip0++) {
					hd.setCount(strip0, // do not add 1
							hits[lay0][sect0][strip0]);

				}
				ppan.getCanvas().needsRedraw(true);
			}
		}
	}

	// fill the ftof histo grid (view)
	private void fillFtofHistogramGrid(HistoGridView ftofGrid) {
		if (ftofGrid == null) {
			return;
		}

		int hits[][] = null;
		for (int sect0 = 0; sect0 < 6; sect0++) {
			int row = sect0 + 1;

			for (int panelType = 0; panelType < 3; panelType++) {
				int col = panelType + 1;

				PlotPanel ppan = ftofGrid.getPlotPanel(row, col);
				HistoData hd = ftofGrid.getHistoData(row, col);

				if (panelType == 0) {
					hits = AccumulationManager.getInstance().getAccumulatedFTOF1AData();
				} else if (panelType == 1) {
					hits = AccumulationManager.getInstance().getAccumulatedFTOF1BData();
				} else if (panelType == 2) {
					hits = AccumulationManager.getInstance().getAccumulatedFTOF2Data();
				}

				for (int paddle0 = 0; paddle0 < FTOFGeometry.numPaddles[panelType]; paddle0++) {
					hd.setCount(paddle0, // do not add 1
							hits[sect0][paddle0]);

				}

				ppan.getCanvas().needsRedraw(true);
			}
		}

	}

	// fill the dc histo grid (view)
	private void fillDcHistogramGrid(HistoGridView dcGrid) {
		if (dcGrid == null) {
			return;
		}

		int dchits[][][][] = AccumulationManager.getInstance().getAccumulatedDCData();
		for (int sect0 = 0; sect0 < 6; sect0++) {
			for (int supl0 = 0; supl0 < 6; supl0++) {
				int row = 6 * sect0 + supl0 + 1;
				for (int lay0 = 0; lay0 < 6; lay0++) {
					int col = lay0 + 1;

					PlotPanel ppan = dcGrid.getPlotPanel(row, col);
					HistoData hd = dcGrid.getHistoData(row, col);

					for (int wire0 = 0; wire0 < 112; wire0++) {
						hd.setCount(wire0, // do not add 1
								dchits[sect0][supl0][lay0][wire0]);

					}

					ppan.getCanvas().needsRedraw(true);
				}

			}
		}

	}

}
