package cnuphys.bCNU.view;


import cnuphys.bCNU.graphics.container.BaseContainer;
import cnuphys.bCNU.graphics.container.ContainerPanel;
import cnuphys.bCNU.graphics.toolbar.ToolBarToggleButton;

/**
 * A view that contains a grid of BaseContainers.
 * @author heddle
 *
 */
public class ContainerGridView extends ScrollableGridView {
	
	// the containers
	protected ContainerPanel _panels[][];
	public final int numRow;
	public final int numCol;

	// which cell is selected
	protected BaseContainer _hotContainer;

	/**
	 * Create a grid of BaseContainers each with independent drawing.
	 * @param numRow the number of rows
	 * @param numCol the number of columns
	 * @param cellWidth
	 * @param cellHeight
	 * @param keyVals
	 */
	protected ContainerGridView(int numRow, int numCol, int cellWidth, int cellHeight, Object... keyVals) {
		super(numRow, numCol, cellWidth, cellHeight, keyVals);
		this.numRow = numRow;
		this.numCol = numCol;
		_panels = new ContainerPanel[numRow][numCol];
	}
		

	/**
	 * Check whether the pointer bar is active on the tool bar
	 * 
	 * @return <code>true</code> if the Pointer button is active.
	 */
	protected boolean isPointerButtonActive() {
		ToolBarToggleButton mtb = getContainer().getActiveButton();
		return (mtb == getContainer().getToolBar().getPointerButton());
	}

	/**
	 * Set all three labels
	 * @param row the 0-based row
	 * @param col the 0-based column
	 * @param title the title
	 * @param xlabel the x label
	 * @param yLabel the y label
	 */
	public void setLabels(int row, int col, String title, String xlabel, String ylabel) {
		ContainerPanel panel = getContainerPanel(row, col);
		
		if (panel != null) {
			panel.setLabels(title, xlabel, ylabel);
		}
	}
	


	/**
	 * Set the title for one of the cells
	 * @param row the 0-based row
	 * @param col the 0-based column
	 * @param title the title
	 */
	public void setTitle(int row, int col, String title) {
		ContainerPanel panel = getContainerPanel(row, col);
		
		if (panel != null) {
			panel.setTitle(title);
		}
	}
	
	/**
	 * Set the x label for one of the cells
	 * @param row the 0-based row
	 * @param col the 0-based column
	 * @param label the label
	 */
	public void setXLabel(int row, int col, String label) {
		ContainerPanel panel = getContainerPanel(row, col);
		
		if (panel != null) {
			panel.setXLabel(label);
		}
	}

	/**
	 * Set the y label for one of the cells
	 * @param row the 0-based row
	 * @param col the 0-based column
	 * @param label the label
	 */
	public void setYLabel(int row, int col, String label) {
		ContainerPanel panel = getContainerPanel(row, col);
		
		if (panel != null) {
			panel.setYLabel(label);
		}
	}

	/**
	 * Get the container panel in the given cell
	 * 
	 * @param row the 0-based row
	 * @param col the 0-based column
	 * @return the container panel (might be <code>null</code>);
	 */
	public ContainerPanel getContainerPanel(int row, int col) {
		if ((row < 0) || (row >= numRow)) {
			return null;
		}
		if ((col < 0) || (col >= numCol)) {
			return null;
		}

		return _panels[row][col];
	}
	
	/**
	 * Get the container in the given cell
	 * 
	 * @param row the 0-based row
	 * @param col the 0-based column
	 * @return the container  (might be <code>null</code>);
	 */
	public BaseContainer getContainer(int row, int col) {
        ContainerPanel panel = getContainerPanel(row, col);
		return (panel == null) ? null : panel.getBaseContainer();
	}

}