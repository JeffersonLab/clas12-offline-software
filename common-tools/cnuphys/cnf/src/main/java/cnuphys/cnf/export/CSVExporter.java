package cnuphys.cnf.export;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.log.Log;
import cnuphys.cnf.alldata.ColumnData;
import cnuphys.cnf.alldata.DataManager;
import cnuphys.cnf.alldata.graphics.ColumnsDialog;

/**
 * A exporter to CSV. The use must choose what bank, and then what columns to export.
 * @author heddle
 *
 */
public class CSVExporter extends AExporter {
	
	private PrintWriter _printWriter;

	//the columns being exported
	private List<ColumnData> _columnData;
	
	//used to write column data names
	private boolean _first = true;

	@Override
	public String getMenuName() {
		return "CSV";
	}
	
	//just counts "events"
	private int _totalCount;
	
	//a filter
	private IExportFilter _filter;

	@Override
	public boolean prepareToExport(IExportFilter filter) {
		_filter = filter;
		_totalCount = 0;

		Log.getInstance().info("CSV export requested");
		
		//reset
		_columnData = null;
		
		//what columns to export?
		ColumnsDialog cd = new ColumnsDialog("Select Bank and Columns to Export");
		cd.setVisible(true);
		
		int reason = cd.getReason();
		if (reason == DialogUtilities.CANCEL_RESPONSE) {
			Log.getInstance().info("CSV Export was cancelled.");
			return false;
		}
		
		//see what I have selected
		String bankName = cd.getSelectedBank();
		if (bankName == null) {
			Log.getInstance().error("null bankname in CSV prepareToExport. That should not have happened.");
			return false;
		}
		
		//list of columns (their names) to be exported
		List<String> colNames = cd.getSelectedColumns();
		
		if ((colNames == null) || colNames.isEmpty()) {
			Log.getInstance().error("null or empty column names in CSV prepareToExport. That should not have happened.");
			return false;
		}
		Log.getInstance().info("CSV exporting bank [" + bankName + "]");
		
		StringBuffer sb = new StringBuffer(256);
		sb.append("CSV exporting column[s] ");
		for (String c : colNames) {
			sb.append(" [" + c + "]");
		}
		
		Log.getInstance().info(sb.toString());
		
		
		//open a file for writing
		_exportFile = getFile("CSV Files", "csv", "csv", "CSV");
		if (_exportFile != null) {
			Log.getInstance().info("CSV: export to [" + _exportFile.getAbsolutePath() + "]");
			
			//generate a list of column data objects corresponding to the 
			//columns that will be exported
			_columnData = new ArrayList<ColumnData>();
			
			for (String c : colNames) {
				ColumnData cdata = DataManager.getInstance().getColumnData(bankName, c);
				if (cdata == null) {
					Log.getInstance().error("null ColumnData in CSV prepareToExport. Bank [" + bankName + "] column [" + c + "]");
					return false;
				}
				Log.getInstance().info("CSV adding ColumnData [" + cdata.getFullName() + "]");
				_columnData.add(cdata);
			}

			//get a data output stream for the file
			try {
				_printWriter = new PrintWriter(_exportFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		} //export file != null
		else {
			return false;
		}
		
		_first = true;
		return true;
	}

	@Override
	public void nextEvent(DataEvent event) {
		_totalCount++;
		if ((_totalCount % 1000) == 0) {
			System.err.println("Export count: " + _totalCount);
		}
		
		if ((_columnData != null) && !_columnData.isEmpty()) {
			
			//first row should be column names
			if (_first) {
				writeColumnNames();
				_first  = false;
			}
			
			int numRows = Integer.MAX_VALUE;
			int numCols = _columnData.size();
			
			//one double array for each column
			ArrayList<double[]> doubleArrays = new ArrayList<>();
			
			//get the minimum common number of rows
			for (int i = 0; i < numCols; i++) {
				ColumnData cdata = _columnData.get(i);
				doubleArrays.add(cdata.getAsDoubleArray(event));
				int num = cdata.getLength(event);
				numRows = Math.min(numRows, num);
			}
			
			if (numRows < 1) {
				return;
			}

			// the data

			double currentRowsData[] = new double[numCols];
			
			for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
				StringBuffer sb = new StringBuffer(512);
				for (int colIndex = 0; colIndex < numCols; colIndex++) {
					double data[] = doubleArrays.get(colIndex);
					
					currentRowsData[colIndex] = data[rowIndex];

					if (colIndex > 0) {
						sb.append(",");
					}
					
					String s = String.format("%5.4g", currentRowsData[colIndex]);
					sb.append(s);
					
					//sb.append(data[index]);
				}
				
				if ((_filter == null) || (_filter.pass(currentRowsData))) {
					stringLn(_printWriter, sb.toString());
				}
				else {
					_totalCount--;
				}
				
				
			}
		}
	}
	
	//write the column names, separated by commas
	private void writeColumnNames() {
		int count = _columnData.size();
		StringBuffer sb = new StringBuffer(512);
		for (int i = 0; i < count; i++) {
			ColumnData cdata = _columnData.get(i);
			if (i > 0) {
				sb.append(",");
			}
			sb.append(cdata.getColumnName());
		}
		stringLn(_printWriter, sb.toString());
	}

	@Override
	public void done() {
		System.out.println("CSV: I am done. Total count: " + _totalCount);
		
		if (_printWriter != null) {
			_printWriter.flush();
			_printWriter.close();
			_printWriter = null;
		}
				
	}
	
	private static void stringLn(PrintWriter pw, String str) {
		pw.print(str);
		pw.print("\n");
	}
	

}
