package cnuphys.toJava;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JTextArea;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderAdapter;
import org.xml.sax.helpers.XMLReaderFactory;

public class Parser {

    private static Vector<Holder> holders;

    // known element and attribute names (that we care about)
    public static final String BANK = "bank";
    public static final String COLUMN = "column";
    public static final String NAME = "name";
    public static final String TAG = "tag";
    public static final String INFO = "info";
    public static final String TYPE = "type";
    public static final String NUM = "num";
    public static final String SECTION = "section";

    private static String _currentBankName;
    private static int _currentBankTag;
    private static String _currentBankInfo;

    private static String _currentSectionName;
    private static int _currentSectionTag;
    private static String _currentSectionInfo;
    private static String _currentBaseBankName = null;

    public static void parse(final JTextArea textArea, File file)
	    throws SAXException, IOException {

	holders = new Vector<Holder>();

	// Create instances needed for parsing
	XMLReader reader = XMLReaderFactory.createXMLReader();

	FileReader fileReader = null;
	fileReader = new FileReader(file);

	_currentBaseBankName = null;

	XMLReaderAdapter contentHandler = new XMLReaderAdapter(reader) {

	    @Override
	    public void startElement(String namespaceURI, String localName,
		    String qName, Attributes atts) {

		if (BANK.equalsIgnoreCase(qName)) {
		    int numAtt = atts.getLength();

		    _currentBankName = atts.getValue(NAME);

		    System.err
			    .println("Current Bank Name: " + _currentBankName);

		    _currentBankTag = Integer.parseInt(atts.getValue(TAG));
		    _currentBankInfo = atts.getValue(INFO);

		}
		if (SECTION.equalsIgnoreCase(qName)) {
		    int numAtt = atts.getLength();

		    _currentSectionName = atts.getValue(NAME);

		    if ((_currentBaseBankName == null)
			    && ("true".equalsIgnoreCase(_currentSectionName))) {
			_currentBaseBankName = (new String(_currentBankName))
				.toLowerCase();
			System.err.println("BASE BANK [" + _currentBaseBankName
				+ "]");
		    }

		    System.err.println("Current Section Name: "
			    + _currentSectionName);

		    _currentSectionTag = Integer.parseInt(atts.getValue(TAG));
		    _currentSectionInfo = atts.getValue(INFO);

		} else if (COLUMN.equalsIgnoreCase(qName)) {
		    int numAtt = atts.getLength();
		    // System.out.println("Found column with " + numAtt
		    // + " attributes");
		    String colName = atts.getValue(NAME);
		    String colType = atts.getValue(TYPE);

		    if (colType == null) {
			colType = atts.getValue("tpye"); // common typo
		    }

		    String colInfo = atts.getValue(INFO);
		    int colNum = Integer.parseInt(atts.getValue(NUM));

		    Holder holder = new Holder(textArea, _currentBankName,
			    _currentSectionName, colName, colInfo, colType);

		    holders.add(holder);
		}
	    }

	};

	InputSource inputSource = new InputSource(fileReader);
	contentHandler.parse(inputSource);
	System.out.println("\n\n//Holder count: " + holders.size());

	// sort 'em
	if (holders != null) {
	    System.err.println("Sorting holders");
	    Collections.sort(holders);
	}

	// array declarations
	textArea.append("\n");
	for (Holder h : holders) {
	    h.declaration();
	}

	impelementAbstractHitCount(textArea);

	// getters (for load method)
	textArea.append("\n");
	textArea.append("\n@Override");
	textArea.append("\npublic void load(EvioDataEvent event) {\n");
	textArea.append("  if (event == null) {\n    return;\n  }\n\n");

	String currentBSName = null;
	boolean foundany = false;
	for (Holder h : holders) {

	    boolean newname = !(h.banksectname.equalsIgnoreCase(currentBSName));
	    if (newname && (currentBSName != null)) {
		textArea.append("  }  " + "//" + currentBSName + "\n\n");
	    }

	    if (newname) {
		currentBSName = new String(h.banksectname);
		foundany = true;
		textArea.append(ifStatement(currentBSName));
	    }

	    h.getter();

	}

	if (foundany) {
	    textArea.append("  }  " + "//" + currentBSName + "\n\n");
	}
	textArea.append("} //load\n");

	// nullify
	textArea.append("\n");
	textArea.append("\n@Override");
	textArea.append("\npublic void clear() {\n");
	for (Holder h : holders) {
	    h.nullify();
	}
	textArea.append("}  //clear\n");
    }

    public static void impelementAbstractHitCount(JTextArea textArea) {
	textArea.append("\n@Override");
	textArea.append("\n  public int getHitCount(int option) {\n");
	String name = _currentBaseBankName + "_dgtz_sector";
	textArea.append("    int hitCount = (" + name + " == null) ? 0 : "
		+ name + ".length;\n");
	textArea.append("    return hitCount;\n");
	textArea.append("  }\n");
    }

    private static String ifStatement(String bsname) {
	return "  if (event.hasBank(\"" + bsname + "\")) {\n";
    }

}
