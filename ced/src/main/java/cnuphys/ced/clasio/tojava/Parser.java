package cnuphys.ced.clasio.tojava;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    private static String _currentSectionName;

    public static void parse(final JTextArea textArea, File file)
	    throws SAXException, IOException {

	holders = new Vector<Holder>();

	// Create instances needed for parsing
	XMLReader reader = XMLReaderFactory.createXMLReader();

	FileReader fileReader = null;
	fileReader = new FileReader(file);

	XMLReaderAdapter contentHandler = new XMLReaderAdapter(reader) {

	    @Override
	    public void startElement(String namespaceURI, String localName,
		    String qName, Attributes atts) {

		if (BANK.equalsIgnoreCase(qName)) {
		    _currentBankName = atts.getValue(NAME);

		}
		if (SECTION.equalsIgnoreCase(qName)) {
		    _currentSectionName = atts.getValue(NAME);

		} else if (COLUMN.equalsIgnoreCase(qName)) {
		    String colName = atts.getValue(NAME);
		    String colType = atts.getValue(TYPE);

		    if (colType == null) {
			colType = atts.getValue("tpye"); // common typo
		    }

		    String colInfo = atts.getValue(INFO);

		    Holder holder = new Holder(textArea, _currentBankName,
			    _currentSectionName, colName, colInfo, colType);

		    holders.add(holder);

		}
	    }

	};

	InputSource inputSource = new InputSource(fileReader);
	contentHandler.parse(inputSource);
	System.out.println("\n\n//Holder count: " + holders.size());

	// declarations
	textArea.append("\n");
	for (Holder h : holders) {
	    h.declaration();
	}

	// getters
	textArea.append("\n");
	textArea.append("public void load(EvioDataEvent event) {\n");
	textArea.append("  if (event != null) {\n");
	for (Holder h : holders) {
	    h.getter();
	}
	textArea.append("  }\n");
	textArea.append("}\n");

	// nullify
	textArea.append("\n");
	textArea.append("public void clear() {\n");
	for (Holder h : holders) {
	    h.nullify();
	}
	textArea.append("}\n");
    }

}
