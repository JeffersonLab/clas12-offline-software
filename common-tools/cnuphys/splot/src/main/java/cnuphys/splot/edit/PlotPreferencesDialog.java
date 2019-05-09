package cnuphys.splot.edit;

import java.awt.BorderLayout;
import java.awt.Component;

import cnuphys.splot.plot.GeneralPlotParamPanel;
import cnuphys.splot.plot.PlotCanvas;

public class PlotPreferencesDialog extends SimpleDialog {

	protected PlotCanvas _plotCanvas;

	// button labels
	protected static final String APPLY = "Apply";
	protected static final String CLOSE = "Close";

	// plot labels and title
	protected GeneralPlotParamPanel _genPanel;
	
	//axes limits
	private AxesLimitsPanel _axesPanel;

	/**
	 * Edit the plot preferences
	 * 
	 * @param plotCanvas the plot being edited
	 */
	public PlotPreferencesDialog(PlotCanvas plotCanvas) {
		super("Plot Preferences", plotCanvas, true, APPLY, CLOSE);

		// note components already created by super constructor
		_plotCanvas = plotCanvas;
		addCenter();
		addNorth();
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
	 * Override to create the component that goes in the center. Usually this is the
	 * "main" component.
	 * 
	 * @return the component that is placed in the center
	 */
	private Component addNorth() {
		// _tabbedPane = new JTabbedPane();

		_genPanel = new GeneralPlotParamPanel(_plotCanvas);

		add(_genPanel, BorderLayout.NORTH);
		return _genPanel;
	}
	
	/**
	 * Override to create the component that goes in the center. Usually this is the
	 * "main" component.
	 * 
	 * @return the component that is placed in the center
	 */
	protected Component addCenter() {
		_axesPanel = new AxesLimitsPanel(_plotCanvas);
		add(_axesPanel, BorderLayout.CENTER);
		return _axesPanel;
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
		else if (APPLY.equals(command)) {
			_genPanel.apply();
			_axesPanel.apply();
			_plotCanvas.setWorldSystem();
			_plotCanvas.repaint();
		}
	}

}
