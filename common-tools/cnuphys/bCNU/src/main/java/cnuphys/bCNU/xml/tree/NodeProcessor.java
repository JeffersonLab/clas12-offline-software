package cnuphys.bCNU.xml.tree;

import javax.swing.tree.TreeNode;

/**
 * @author heddle
 * 
 */
public interface NodeProcessor {

	public void processNode(SAXJTree tree, TreeNode node)
			throws BadNodeException;
}
