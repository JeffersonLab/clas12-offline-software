package cnuphys.cnf.export;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	
	//the data output stream
	private DataOutputStream _dos;
	

	//the columns being exported
	private List<ColumnData> _columnData;
	
	//used to write column data names
	private boolean _first = true;

	@Override
	public String getMenuName() {
		return "CSV";
	}

	@Override
	public boolean prepareToExport() {
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

			try {
				_dos = new DataOutputStream(new FileOutputStream(_exportFile));
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
		if ((_columnData != null) && !_columnData.isEmpty()) {
			
			if (_first) {
				writeColumnNames();
				_first  = false;
			}
			
			int minNum = Integer.MAX_VALUE;
			int count = _columnData.size();
			
			ArrayList<double[]> doubleArrays = new ArrayList<>();
			
			//get the minimum common number of rows
			for (int i = 0; i < count; i++) {
				ColumnData cdata = _columnData.get(i);
				doubleArrays.add(cdata.getAsDoubleArray(event));
				int num = cdata.getLength(event);
				minNum = Math.min(minNum, num);
			}
			
			if (minNum < 1) {
				return;
			}

			// the data

			for (int index = 0; index < minNum; index++) {
				StringBuffer sb = new StringBuffer(512);
				for (int i = 0; i < count; i++) {
					double data[] = doubleArrays.get(i);

					if (i > 0) {
						sb.append(",");
					}
					sb.append(data[index]);
				}
				stringLn(_dos, sb.toString());
			}
		}
	}
	
	private void writeColumnNames() {
		int count = _columnData.size();
		StringBuffer sb = new StringBuffer(512);
		for (int i = 0; i < count; i++) {
			ColumnData cdata = _columnData.get(i);
			if (i > 0) {
				sb.append(",");
			}
			sb.append(cdata.getFullName());
		}
		stringLn(_dos, sb.toString());
	}

	@Override
	public void done() {
		System.out.println("CSV: I am done");
		
		if (_dos != null) {
			try {
				_dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		_dos = null;
		
	}
	
	private static void stringLn(DataOutputStream dos, String str) {
		
		try {
			dos.writeChars(str);
			
			dos.writeChars("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

}
