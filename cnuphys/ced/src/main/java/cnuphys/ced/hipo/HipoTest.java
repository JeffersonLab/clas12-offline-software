package cnuphys.ced.hipo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.detector.DetectorResponse;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.RunData;

public class HipoTest {

	//data directory
	private static File _dataDir;
	private static File _testFile;
	
	//hippo dictionary
//	private static HipoDataDictionary _dictionary;

	private static RunData _runData = new RunData();
	
	//test peeking at some events
	private static boolean eventTest() {
		HipoDataSource source = new HipoDataSource();
		System.out.println("TEST FILE: " + _testFile.getAbsolutePath());
		source.open(_testFile.getAbsolutePath());
		System.out.println("event file size: " + source.getSize());
		
		DetectorResponse response;

		int eventCount = 0;
		while((eventCount < 3) && source.hasEvent()){
			eventCount++;
			DataEvent dataEvent = source.getNextEvent();
			System.out.println("GOT EVENT: " + source.getCurrentIndex() + "   with banks: ");
			
//			for (String bankName : _dictionary.getDescriptorList()) {
//				DataBank bank = dataEvent.getBank(bankName);
//				
//				System.out.println("  Data bank for [" + bankName + "]  found: " + (bank != null));
//			}
			
			String currentbankList[] = dataEvent.getBankList();
			for (String bankName : currentbankList) {
				System.out.println("   " + bankName);
//				String columnList[] = dataEvent.getColumnList(bankName);
				List<ColumnData> columnsWithData = DataManager.getInstance().hasData(dataEvent);
				
				for (ColumnData cd : columnsWithData) {
					System.out.println("        " + cd.getFullName() + "   count: " + cd.length(dataEvent));
				}
//				for (String columnName : columnList) {
//					ColumnData cd = DataManager.getInstance().getColumnData(bankName, columnName);
//					System.out.println("        " + columnName + "   count: " + cd.length(dataEvent));
//				}
			}
			
//			if (_runData.set(dataEvent)) {
//				System.out.println(_runData.toString());
//			}
//			else {
//				System.out.println("No Run Bank data");
//			}
		} 
		
		return true;
	}
	
	
	//test the hippo dictionary
	private static boolean testDictionary() {
//		EvioDataDictionary dataDict = EvioFactory.getDictionary();
//		if (dataDict != null) {
//			String[] _knownBanks = dataDict.getDescriptorList();
//			Arrays.sort(_knownBanks);
//			for (String evioBank : _knownBanks) {
//				System.out.println("   EVIO BANK: " + evioBank);
//			}
//		}

		DataManager dataManager = DataManager.getInstance();
		ArrayList<ColumnData> columns = dataManager.getColumnData();
		if (columns != null) {
			for (ColumnData cd : columns) {
				System.out.println(cd.toString());
			}
		}
		else {
			System.out.println("NO COLUMNS!");
		}
//		_dictionary = new HipoDataDictionary();
//		String hipoBanks[] = _dictionary.getDescriptorList();
//		Arrays.sort(hipoBanks);
//		System.out.println("Size of hipo bank list: " + hipoBanks.length);
//		for (String s : hipoBanks) {
//			System.out.println(" " + s);
//			DataDescriptor dd = _dictionary.getDescriptor(s);
//			String entries[] = dd.getEntryList();
//			Arrays.sort(entries);
//			for (String es : entries) {
//				System.out.println("    " + es);
//			}
//		}

		return true;
	}
	
	//look for necessary directories
	private static boolean findDirectories() {
		_dataDir = new File("../../../data/HippoData");
		if (!_dataDir.exists() || !_dataDir.isDirectory()) {
			System.out.println("Could not find data dir at: " + _dataDir.getAbsolutePath());
			return false;
		}
		
		_testFile = new File(_dataDir, "test.hipo");
		if (!_testFile.exists())  {
			System.out.println("Could not open test file at: " + _testFile.getAbsolutePath());
			return false;
		}
		
		String clas12dir = System.getProperty("CLAS12DIR");
		if (clas12dir == null) {
			System.out.println("No CLAS12DIR property");
			return false;
		}
		System.out.println("CLAS12DIR property: " + clas12dir);
		File clas12 = new File(clas12dir);
		if (!clas12.exists() || !clas12.isDirectory()) {
			System.out.println("Could not find clas12 dir at: " + clas12.getAbsolutePath());
			return false;
		}

		
		System.out.println("Good. All directories found.");
		return true;
	}
	
	
	/**
	 * main program for hippo test
	 * @param arg command line arguments
	 */
	public static void main(String arg[]) {
		System.out.println("HIPPO Test.");
		if (!findDirectories()) {
			System.out.println("Could not find necessary directories. Exiting.");
			System.exit(1);
		}
		if (!testDictionary()) {
			System.out.println("Failed dictionary test. Exiting.");
			System.exit(1);
		}
		
//		if (!eventTest()) {
//			System.out.println("Failed event test. Exiting.");
//			System.exit(1);
//		}

	}
}
