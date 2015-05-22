package cnuphys.ced.clasio;

import java.util.Comparator;

import org.jlab.coda.jevio.EvioNode;

public class EvioNodeComparator implements Comparator<EvioNode> {

    @Override
    public int compare(EvioNode node1, EvioNode node2) {
	int result = Integer.compare(node1.getTag(), node2.getTag());
	if (result == 0) {
	    result = Integer.compare(node1.getNum(), node2.getNum());
	}
	return result;
    }

}
