package cnuphys.ced.event.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

import cnuphys.bCNU.util.AsciiReader;

public class PlotReader extends AsciiReader {
	
	private String plotType;

	public PlotReader(File file) throws FileNotFoundException {
		super(file);
	}

	@Override
	protected void processLine(String line) {
		//for storing cuts
		Vector<ICut> cuts = new Vector<ICut>();
		
		//get plot type
		String tokens[] = PlotDialog.getTokens(line);
		String key = tokens[0];
		
		
		if (PlotDialog.TYPE.equals(key)) {
			plotType = tokens[1];
		}
		else if (RangeCut.CUT_TYPE.equals(key)) {
			
		}
	}

	@Override
	public void done() {
	}
	
	public PlotDialog getPlotDialog() {
		return null;
	}

}
