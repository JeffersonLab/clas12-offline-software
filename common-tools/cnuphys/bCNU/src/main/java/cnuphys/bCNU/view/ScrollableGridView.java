package cnuphys.bCNU.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.lund.X11Colors;

public class ScrollableGridView extends BaseView {

	// the scroll pane
	protected JScrollPane _scrollPane;

	// row and col count
	protected int _numRow;
	protected int _numCol;

	// cell size
	protected int _cellWidth;
	protected int _cellHeight;

	protected JPanel _gridPanel;
	
	// status
	protected JLabel _status;

	/**
	 * Create a scrollable grid
	 * 
	 * @param numRow
	 * @param numCol
	 * @param cellWidth
	 * @param cellHeight
	 * @param keyVals
	 */
	protected ScrollableGridView(int numRow, int numCol, int cellWidth, int cellHeight, Object... keyVals) {
		super(keyVals);
		
		

		_gridPanel = new JPanel();
		_gridPanel.setLayout(new GridLayout(numRow, numCol, 2, 2));
		_gridPanel.setOpaque(true);
		_gridPanel.setBackground(X11Colors.getX11Color("wheat"));

		_numRow = numRow;
		_numCol = numCol;
		_cellWidth = cellWidth;
		_cellHeight = cellHeight;

		_scrollPane = new JScrollPane(_gridPanel);
		add(_scrollPane, BorderLayout.CENTER);

		addStatusLine();
	}
	
	protected void addEastPanel() {
		
	}
	protected void addWestPanel() {
		
	}


	protected void addStatusLine() {
		JPanel sp = new JPanel();
		sp.setLayout(new BorderLayout(2, 2));
		_status = new JLabel(" ");

		_status.setOpaque(true);
		_status.setFont(Fonts.commonFont(Font.PLAIN, 12));
		_status.setBackground(Color.black);
		_status.setForeground(Color.cyan);

		sp.add(_status, BorderLayout.CENTER);
		add(sp, BorderLayout.SOUTH);
	}

	/**
	 * Set the one-line status text
	 * 
	 * @param s the status text
	 */
	public void setStatus(String s) {
		_status.setText((s == null) ? "" : s);
	}

	public void addComponent(JComponent c) {
		c.setBorder(BorderFactory.createEtchedBorder());
		_gridPanel.add(c);
	}

	/**
	 * Get the number of rows in this grid.
	 * 
	 * @return the number of rows.
	 */
	public int getNumRows() {
		return _numRow;
	}

	/**
	 * Get the number of columns in this grid.
	 * 
	 * @return the number of columns.
	 */
	public int getNumColumns() {
		return _numCol;
	}

	/**
	 * Create a scrollable grid
	 * 
	 * @param title
	 * @param numRow
	 * @param numCol
	 * @param cellWidth
	 * @param cellHeight
	 * @param screenFraction
	 * @return
	 */
	public static ScrollableGridView createScrollableGridView(String title, int numRow, int numCol, int cellWidth,
			int cellHeight, double screenFraction) {

		int width = numCol * cellWidth;
		int height = numRow * cellHeight;

		final ScrollableGridView view = new ScrollableGridView(numRow, numCol, cellWidth, cellHeight,
				PropertySupport.WIDTH, width, PropertySupport.HEIGHT, height, PropertySupport.TOOLBAR, false,
				PropertySupport.VISIBLE, true, PropertySupport.TITLE, title, PropertySupport.STANDARDVIEWDECORATIONS,
				true);

		screenFraction = Math.max(0.25, Math.min(1.0, screenFraction));
		view.setSize(GraphicsUtilities.screenFraction(screenFraction));

		// test
		for (int row = 1; row <= numRow; row++) {
			for (int col = 1; col <= numCol; col++) {
				view.addComponent(testComponent(row, col, cellWidth, cellHeight));
			}
		}
		return view;
	}

	private static JComponent testComponent(final int row, final int col, final int w, final int h) {
		JComponent c = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				g.setColor(Color.lightGray);
				g.setFont(Fonts.mediumFont);
				Rectangle b = getBounds();
				g.drawString("[ " + row + ", " + col + "]", 8, 20);
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(w, h);
			}
		};

		return c;
	}
}
