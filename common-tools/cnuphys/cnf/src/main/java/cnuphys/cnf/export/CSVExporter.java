package cnuphys.cnf.export;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jlab.io.base.DataEvent;

import cnuphys.cnf.alldata.graphics.ColumnsDialog;

/**
 * A exporter to CSV. The use must choose what bank, and then what columns to export.
 * @author heddle
 *
 */
public class CSVExporter extends AExporter {
	
	private DataOutputStream _dos;

	@Override
	public String getMenuName() {
		return "CSV";
	}

	@Override
	public boolean prepareToExport() {
		System.out.println("CSV: I have been told to export");
		
		//what columns to export?
		ColumnsDialog cd = new ColumnsDialog("Select Bank and Columns to Export");
		cd.setVisible(true);
		
		//open a file for writing
		_exportFile = getFile("CSV Files", "csv", "csv", "CSV");
		if (_exportFile != null) {
			System.out.println("CSV: I will write to [" + _exportFile.getAbsolutePath() + "]");

			try {
				_dos = new DataOutputStream(new FileOutputStream(_exportFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
		
		return (_exportFile != null);
	}

	@Override
	public void nextEvent(DataEvent event) {
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
