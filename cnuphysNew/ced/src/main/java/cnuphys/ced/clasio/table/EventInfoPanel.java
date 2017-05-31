package cnuphys.ced.clasio.table;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class EventInfoPanel extends JPanel {

	/** A label for displaying the current event source name. */
	private NamedLabel eventSourceLabel;

	/**
	 * A label for displaying the number of events in an event file.
	 */
	private NamedLabel numEventsLabel;

	/**
	 * A label for displaying the ordinal number of the event from an event
	 * file.
	 */
	private NamedLabel eventNumberLabel;

	/** holds the source field */
	private JPanel _sourcePanel;

	/** holds the event count field */
	private JPanel _numPanel;

	/** Panel in which to place event viewing controls. */
	JPanel controlPanel;
	
	/** run number */
	private NamedLabel runLabel;

	/**
	 * Create the panel that goes in the north - top of the GUI. This will hold
	 * 4 labels. One showing the current event source. The second showing the
	 * current dictionary source. The third showing the event number, and the
	 * fourth showing the number of events.
	 *
	 * @return the panel.
	 */
	public EventInfoPanel() {

		setLayout(new BorderLayout()); // rows, cols, hgap, vgap
		setBorder(new EmptyBorder(5, 0, 5, 0)); // top, left, bot, right

		eventSourceLabel = new NamedLabel("source", "event_source", 400);
		eventNumberLabel = new NamedLabel("event #", "event #", 65);
		numEventsLabel = new NamedLabel("count",     "event #", 65);
		runLabel = new NamedLabel("run #", "event #", 65);

		// limit size of labels
		Dimension d1 = eventSourceLabel.getPreferredSize();
		Dimension d2 = eventNumberLabel.getPreferredSize();

		eventSourceLabel.setMaximumSize(d1);
		eventNumberLabel.setMaximumSize(d2);
		numEventsLabel.setMaximumSize(d2);
		runLabel.setMaximumSize(d2);

		// panels

		_sourcePanel = new JPanel();
		_sourcePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		_sourcePanel.add(eventSourceLabel);

		_numPanel = new JPanel();
		_numPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		_numPanel.add(eventNumberLabel);
		_numPanel.add(Box.createHorizontalStrut(3));
		_numPanel.add(numEventsLabel);
		_numPanel.add(Box.createHorizontalStrut(3));
		_numPanel.add(runLabel);

		controlPanel = new JPanel();
		controlPanel.setPreferredSize(new Dimension(200, 1)); // width to be
		// same as data
		// view

		add(controlPanel, BorderLayout.WEST);
		add(_sourcePanel, BorderLayout.NORTH);
		add(_numPanel, BorderLayout.SOUTH);
	}

	/**
	 * Get the panel that holds the event source
	 * 
	 * @return the event source panel
	 */
	public JPanel getSourcePanel() {
		return _sourcePanel;
	}

	/**
	 * Get the panel that holds the event number
	 * 
	 * @return the number panel
	 */
	public JPanel getNumberPanel() {
		return _numPanel;
	}

	/**
	 * Set the displayed event source value.
	 * 
	 * @param source
	 *            event source.
	 */
	public void setSource(String source) {
		if (source != null) {
			eventSourceLabel.setText(source);
		}
	}

	/**
	 * Get the displayed event source value.
	 * 
	 * @return the displayed event source value.
	 */
	public String getSource() {
		return eventSourceLabel.getText();
	}

	/**
	 * Set the displayed event number value.
	 * 
	 * @param eventNumber
	 *            event number.
	 */
	public void setEventNumber(int eventNumber) {
		if (eventNumber > -1) {
			eventNumberLabel.setText("" + eventNumber);
		}
	}
	
	/**
	 * Set the displayed run number value.
	 * 
	 * @param runNumber
	 *            the run number.
	 */
	public void setRunNumber(int runNumber) {
		if (runNumber > -1) {
			runLabel.setText("" + runNumber);
		}
		else {
			runLabel.setText("");
		}
	}


	/**
	 * Get the displayed event number value.
	 * 
	 * @return the displayed event number value.
	 */
	public int getEventNumber() {
		return Integer.parseInt(eventNumberLabel.getText());
	}

	/**
	 * Set the displayed number-of-events value.
	 * 
	 * @param numberOfEvents
	 *            number of events.
	 */
	public void setNumberOfEvents(int numberOfEvents) {
		if (numberOfEvents > -1) {
			numEventsLabel.setText("" + numberOfEvents);
		} else {
			numEventsLabel.setText("???");
		}
	}

	/**
	 * Get the displayed number-of-events value.
	 * 
	 * @return the displayed number-of-events value.
	 */
	public int getNumberOfEvents() {
		return Integer.parseInt(numEventsLabel.getText());
	}

}