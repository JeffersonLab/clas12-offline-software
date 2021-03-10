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
	 * A label for displaying the ordinal number of the event from an event file.
	 */
	private NamedLabel seqEventNumberLabel;
	
	/**
	 * A label for displaying the true number of the event from the RUN::config bank.
	 */
	private NamedLabel trueEventNumberLabel;

	/** holds the source field */
	private JPanel _eventSourcePanel;

	/** holds the event count field */
	private JPanel _numPanel;

	/** run number */
	private NamedLabel runLabel;

	/**
	 * Create the panel that goes in the north - top of the GUI. This will hold 4
	 * labels. One showing the current event source. The second showing the current
	 * dictionary source. The third showing the event number, and the fourth showing
	 * the number of events.
	 *
	 * @return the panel.
	 */
	public EventInfoPanel() {

		setLayout(new BorderLayout()); // rows, cols, hgap, vgap
		setBorder(new EmptyBorder(5, 0, 5, 0)); // top, left, bot, right

		eventSourceLabel = new NamedLabel("source", "event_source", 400);
		seqEventNumberLabel = new NamedLabel("seq #", "true #", 65);
		trueEventNumberLabel = new NamedLabel("true #", "true #", 65);
		numEventsLabel = new NamedLabel("count", "true #", 65);
		runLabel = new NamedLabel("run #", "true #", 65);

		// limit size of labels
		Dimension d1 = eventSourceLabel.getPreferredSize();
		Dimension d2 = trueEventNumberLabel.getPreferredSize();

		eventSourceLabel.setMaximumSize(d1);
		seqEventNumberLabel.setMaximumSize(d2);
		trueEventNumberLabel.setMaximumSize(d2);
		numEventsLabel.setMaximumSize(d2);
		runLabel.setMaximumSize(d2);

		// panels

		_eventSourcePanel = new JPanel();
		_eventSourcePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		_eventSourcePanel.add(eventSourceLabel);

		_numPanel = new JPanel();
		_numPanel.setLayout(new BorderLayout(4, 4));
		
		JPanel sPanel = new JPanel();
		sPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		sPanel.add(seqEventNumberLabel);
		sPanel.add(Box.createHorizontalStrut(3));
		sPanel.add(trueEventNumberLabel);
		sPanel.add(Box.createHorizontalStrut(3));
		sPanel.add(numEventsLabel);
		sPanel.add(Box.createHorizontalStrut(3));
		sPanel.add(runLabel);
		_numPanel.add(sPanel, BorderLayout.SOUTH);

		add(_eventSourcePanel, BorderLayout.NORTH);
		add(_numPanel, BorderLayout.SOUTH);
	}

	/**
	 * Get the panel that holds the event source
	 * 
	 * @return the event source panel
	 */
	public JPanel getEventSourcePanel() {
		return _eventSourcePanel;
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
	 * @param source event source.
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
	 * Set the displayed sequential event number value.
	 * The sequential event number is just the number in the file.
	 * @param seqEventNum event number.
	 */
	public void setSeqEventNumber(int seqEventNum) {
		if (seqEventNum > -1) {
			seqEventNumberLabel.setText("" + seqEventNum);
		}
	}
	
	/**
	 * Set the displayed true event number value.
	 * The true event number comes from the RUN::config bank.
	 * @param trueEventNum event number.
	 */
	public void setTrueEventNumber(int trueEventNum) {
		if (trueEventNum > -1) {
			trueEventNumberLabel.setText("" + trueEventNum);
		}
		else {
			trueEventNumberLabel.setText("n/a");
		}
	}

	/**
	 * Set the displayed run number value.
	 * 
	 * @param runNumber the run number.
	 */
	public void setRunNumber(int runNumber) {
		if (runNumber > -1) {
			runLabel.setText("" + runNumber);
		} else {
			runLabel.setText("");
		}
	}

	/**
	 * Get the displayed event number value.
	 * 
	 * @return the displayed event number value.
	 */
	public int getEventNumber() {
		return Integer.parseInt(seqEventNumberLabel.getText());
	}

	/**
	 * Set the displayed number-of-events value.
	 * 
	 * @param numberOfEvents number of events.
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