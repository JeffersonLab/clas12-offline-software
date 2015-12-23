package cnuphys.ced.frame;

import cnuphys.bCNU.view.HistoGridView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.ced.geometry.FTOFGeometry;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.PlotPanel;

public class CedHistoFiller implements IAccumulationListener {
		
	//parent
	protected Ced ced;
	
	//ftof paddle counts
	
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
			break;

		case AccumulationManager.ACCUMULATION_STARTED:
			break;

		case AccumulationManager.ACCUMULATION_CANCELLED: case AccumulationManager.ACCUMULATION_FINISHED:
			Ced.setEventNumberLabel(
					ClasIoEventManager.getInstance().getEventNumber());

			fillDcHistogramGrid(ced.dcHistoGrid);
			fillFtofHistogramGrid(ced.ftofHistoGrid);
			break;
		}
	}
	
	//clear a grid
	private void clearHisto(HistoGridView grid) {
		if (grid != null) {
			grid.clear();
		}
	}
	
	//fill the dc histo grid (view)
	private void fillFtofHistogramGrid(HistoGridView ftofGrid) {
		if (ftofGrid == null) {
			return;
		}
		
		int hits[][] = null;
		for (int sect0 = 0; sect0 < 6; sect0++) {
			int row = sect0+1;

			for (int panelType = 0; panelType < 3; panelType++) {
				int col = panelType+1;
				
				PlotPanel ppan = ftofGrid.getPlotPanel(row,
						col);
				HistoData hd = ftofGrid.getHistoData(row, col);

				
				if (panelType == 0) {
					hits = AccumulationManager.getInstance().getAccumulatedDgtzFtof1aData();
				}
				else if (panelType == 1) {
					hits = AccumulationManager.getInstance().getAccumulatedDgtzFtof1bData();
				}
				else if (panelType == 2) {
					hits = AccumulationManager.getInstance().getAccumulatedDgtzFtof2Data();
				}
				
				for (int paddle0 = 0; paddle0 < FTOFGeometry.numPaddles[panelType]; paddle0++) {
					hd.setCount(paddle0, //do not add 1
							hits[sect0][paddle0]);
					
				}

				
				ppan.getCanvas().needsRedraw(true);

			}
		}
		
	}
	
	//fill the dc histo grid (view)
	private void fillDcHistogramGrid(HistoGridView dcGrid) {
		if (dcGrid == null) {
			return;
		}
		
		int dchits[][][][] = AccumulationManager.getInstance()
				.getAccumulatedDgtzDcData();
		for (int sect0 = 0; sect0 < 6; sect0++) {
			for (int supl0 = 0; supl0 < 6; supl0++) {
				int row = 6 * sect0 + supl0 + 1;
				for (int lay0 = 0; lay0 < 6; lay0++) {
					int col = lay0 + 1;

					PlotPanel ppan = dcGrid.getPlotPanel(row,
							col);
					HistoData hd = dcGrid.getHistoData(row, col);

					for (int wire0 = 0; wire0 < 112; wire0++) {
						hd.setCount(wire0, //do not add 1
								dchits[sect0][supl0][lay0][wire0]);
						
					}

					ppan.getCanvas().needsRedraw(true);
				}
				
			}
		}
		
	}
	
	

}
