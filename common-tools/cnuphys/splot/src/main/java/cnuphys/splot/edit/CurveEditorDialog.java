package cnuphys.splot.edit;

import java.awt.BorderLayout;
import cnuphys.splot.plot.PlotCanvas;

public class CurveEditorDialog extends SimpleDialog {

	// the plot canvas
	protected PlotCanvas _plotCanvas;

	// button labels
	protected static final String CLOSE = "Close";

	// curve editor panel
	protected CurveEditorPanel _curvePanel;

	/**
	 * Edit the plot preferences
	 * 
	 * @param plotCanvas the plot being edited
	 */
	public CurveEditorDialog(PlotCanvas plotCanvas) {
		super("Curve Editor", plotCanvas, true, CLOSE);

		// note components already created by super constructor
		_plotCanvas = plotCanvas;

		_curvePanel = new CurveEditorPanel(_plotCanvas);
		add(_curvePanel, BorderLayout.CENTER);
		pack();
	}

	/**
	 * can do preparation--for example a component might be added on
	 * "createCenterComponent" but a reference needed in "addNorthComponent"
	 */
	@Override
	protected void prepare() {
		_plotCanvas = (PlotCanvas) _userObject;
	}

	/**
	 * Select the first curve
	 */
	public void selectFirstCurve() {
		if (_curvePanel != null) {
			_curvePanel.selectFirstCurve();
		}
	}

	/**
	 * A button was hit. The default behavior is to shutdown the dialog.
	 * 
	 * @param command the label on the button that was hit.
	 */
	@Override
	protected void handleCommand(String command) {
		if (CLOSE.equals(command)) {
			setVisible(false);
		}
	}

}
