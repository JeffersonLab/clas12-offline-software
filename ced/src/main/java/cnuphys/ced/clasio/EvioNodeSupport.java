package cnuphys.ced.clasio;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EvioCompactStructureHandler;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioNode;
import org.jlab.evio.clas12.EvioDataDictionary;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioFactory;

public class EvioNodeSupport {

    // a node comparator
    private static final EvioNodeComparator _comparator = new EvioNodeComparator();

    /**
     * Writes a node in a more useful form than its toString method
     * 
     * @param stream
     *            any print stream, such as System.out
     * @param node
     */
    public static void writeNode(PrintStream stream, EvioNode node) {
	if (node == null) {
	    stream.println("null");
	} else {
	    StringBuilder builder = new StringBuilder(100);
	    builder.append("tag = ");
	    builder.append(node.getTag());
	    builder.append(", num = ");
	    builder.append(node.getNum());
	    builder.append(", structure = ");
	    builder.append(DataType.getName(node.getType()));
	    builder.append(", dataType = ");
	    builder.append(node.getDataTypeObj());
	    builder.append(", pos = ");
	    builder.append(node.getPosition());
	    builder.append(", dataPos = ");
	    builder.append(node.getDataPosition());
	    builder.append(", totlen = ");
	    builder.append(4 * node.getLength());

	    int lastByte = node.getPosition() + (4 * node.getLength()) - 1;
	    builder.append(", lastByte: " + lastByte);

	    boolean bofb = bankOfBanks(node);
	    if (bofb) {
		builder.append(", bankOfBanks ");
	    }

	    builder.append(", name: " + getName(node));

	    stream.println(builder.toString());
	}
    }

    /**
     * Get the name (description) of a node
     * 
     * @param node
     *            the node in question
     * @return the name of the node, if it is found in the dictionary
     */
    public static String getName(EvioNode node) {
	String name = "???";
	EvioDataDictionary dataDict = EvioFactory.getDictionary();
	if (dataDict != null) {
	    name = dataDict.getNameByTagNum(node.getTag(), node.getNum());
	}
	return name;
    }

    /**
     * Check whether this is a leaf
     * 
     * @param node
     *            the node to check
     * @return <code>true</code> if the node is a leaf node
     */
    public static boolean isLeaf(EvioNode node) {
	return !bankOfBanks(node);
    }

    /**
     * Tests whether a node is a bank of banks
     * 
     * @param node
     *            the node to test
     * @return <code>true</code> if the node is a bank of banks
     */
    public static boolean bankOfBanks(EvioNode node) {
	if (node == null) {
	    return false;
	}
	DataType structureType = DataType.getDataType(node.getType());
	DataType dataType = node.getDataTypeObj();

	boolean sBank = (structureType == DataType.BANK)
		|| (structureType == DataType.ALSOBANK);
	boolean dBank = (dataType == DataType.BANK)
		|| (dataType == DataType.ALSOBANK);
	return sBank && dBank;
    }

    /**
     * Get the root node of the event
     * 
     * @param event
     *            the event in question
     * @return the root node, which will be a bank of banks
     */
    public static EvioNode getRootNode(EvioDataEvent event) {
	EvioNode root = null;

	if (event != null) {
	    EvioCompactStructureHandler handler = event.getStructureHandler();
	    if (handler != null) {
		try {
		    List<EvioNode> nodeList = handler.getNodes();
		    if ((nodeList != null) && !nodeList.isEmpty()) {

			for (EvioNode node : nodeList) {
			    if (node.getPosition() == 0) {
				root = node;
				break;
			    }
			}
			if (root == null) {
			    // WEIRD jevio puts root node at end.
			    // might be safer to look for node with pos = 0?
			    root = nodeList.get(nodeList.size() - 1);
			}
		    }
		} catch (EvioException e) {
		    e.printStackTrace();
		}

	    }
	}
	return root;
    }

    /**
     * Obtain all the nodes from an EvioDataEvent. They are returned in a sorted
     * array. The sorting is ascending, with tag as the primary and num as the
     * secondary.
     * 
     * @param event
     *            the event in question
     * @return a sorted array of nodes
     */
    public static EvioNode[] getNodes(EvioDataEvent event) {
	EvioNode array[] = null;

	if (event != null) {
	    EvioCompactStructureHandler handler = event.getStructureHandler();

	    // try {
	    // List<EvioNode> tlist = handler.getNodes();
	    // for (EvioNode node : tlist) {
	    // System.out.println(node.toString());
	    // }
	    // } catch (EvioException e1) {
	    // // TODO Auto-generated catch block
	    // e1.printStackTrace();
	    // }

	    if (handler != null) {
		try {
		    List<EvioNode> nodeList = handler.getNodes();
		    if ((nodeList != null) && !nodeList.isEmpty()) {
			array = new EvioNode[nodeList.size()];
			nodeList.toArray(array);
			Arrays.sort(array, _comparator);
		    }
		} catch (EvioException e) {
		    e.printStackTrace();
		}

	    }
	}

	return array;
    }

}
