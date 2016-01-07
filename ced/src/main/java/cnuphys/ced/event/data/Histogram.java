package cnuphys.ced.event.data;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.splot.pdata.HistoData;

public class Histogram extends PlotDialog {
	
	/**
	 * Create an on-the-fly histogram
	 * @param histoData
	 */
	public Histogram(HistoData histoData) {
		super(histoData.getName());
	}


	@Override
	public void accumulationEvent(int reason) {
		switch (reason) {
		case AccumulationManager.ACCUMULATION_STARTED:
			break;

		case AccumulationManager.ACCUMULATION_CLEAR:
			break;

		case AccumulationManager.ACCUMULATION_CANCELLED:
			break;

		case AccumulationManager.ACCUMULATION_FINISHED:
			break;
		}

	}

	@Override
	public void newClasIoEvent(EvioDataEvent event) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			
		}
	}

	@Override
	public void openedNewEventFile(String path) {
	}
	
}
