package cnuphys.bankDictionary;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderAdapter;
import org.xml.sax.helpers.XMLReaderFactory;

public class Parser {

    // known element and attribute names (that we care about)
    public static final String BANK = "bank";
    public static final String COLUMN = "column";
    public static final String NAME = "name";
    public static final String TAG = "tag";
    public static final String INFO = "info";
    public static final String TYPE = "type";
    public static final String NUM = "num";

    private static String _currentBankName;
    private static int _currentBankTag;
    private static String _currentBankInfo;

    public static void parse(final BankDictionary bankDictionary, File file)
	    throws SAXException, IOException {

	// Create instances needed for parsing
	XMLReader reader = XMLReaderFactory.createXMLReader();

	FileReader fileReader = null;
	fileReader = new FileReader(file);

	XMLReaderAdapter contentHandler = new XMLReaderAdapter(reader) {

	    @Override
	    public void startElement(String namespaceURI, String localName,
		    String qName, Attributes atts) {
		// System.out.println("[PARSER] qName=[" + qName +
		// "]  localName = [" + localName + "]");

		if (BANK.equalsIgnoreCase(qName)) {
		    int numAtt = atts.getLength();
		    // System.out.println("[PARSER] Found bank with " + numAtt +
		    // " attributes");

		    _currentBankName = atts.getValue(NAME);
		    _currentBankTag = Integer.parseInt(atts.getValue(TAG));
		    _currentBankInfo = atts.getValue(INFO);

		    // create an entry with tag 0 just for the bank
		    DictionaryEntry entry = new DictionaryEntry(
			    _currentBankTag, _currentBankName, _currentBankInfo);
		    bankDictionary.put(entry.haskKey(), entry);

		    // System.out.println("[PARSER] bankName " +
		    // _currentBankName);
		    // System.out.println("[PARSER] bankTag " +
		    // _currentBankTag);
		    // System.out.println("[PARSER] bankInfo " +
		    // _currentBankInfo);

		} else if (COLUMN.equalsIgnoreCase(qName)) {
		    int numAtt = atts.getLength();
		    // System.out.println("[PARSER] Found column with " + numAtt
		    // + " attributes");
		    String colName = atts.getValue(NAME);
		    String colType = atts.getValue(TYPE);
		    String colInfo = "[" + colName + "] " + atts.getValue(INFO);
		    int colNum = Integer.parseInt(atts.getValue(NUM));
		    // System.out.println("[PARSER] colName " + colName);
		    // System.out.println("[PARSER] colNum " + colNum);
		    // System.out.println("[PARSER] colType " + colType);
		    // System.out.println("[PARSER] colInfo " + colInfo);

		    // make a dictionary entry
		    DictionaryEntry entry = new DictionaryEntry(
			    _currentBankTag, _currentBankName,
			    _currentBankInfo, colNum, colName, colType, colInfo);
		    bankDictionary.put(entry.haskKey(), entry);

		}
	    }

	};

	InputSource inputSource = new InputSource(fileReader);
	contentHandler.parse(inputSource);
    }

}
