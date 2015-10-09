package cnuphys.ced.clasio;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import org.jlab.data.io.DataBank;
import org.jlab.data.ui.DataBankPanel;

import cnuphys.ced.frame.Ced;

public class ClasIoBankDialog extends JDialog {

	// the event manager
	private static ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	// bank name
	private String _bankName;

	// the panel from Gagik
	private DataBankPanel _dataBankPanel;

	// counter
	private static int count = 0;

	public ClasIoBankDialog(String bankName) {
		super(Ced.getInstance(), bankName, false);
		_bankName = bankName;
		setLayout(new BorderLayout(4, 4));
		setup();

		// close is like a cancel
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				setVisible(false);
			}
		};
		addWindowListener(wa);

		int y = 40 + (count % 5) * 30;
		int x = 40 + (count % 5) * 10 + (count / 5) * 40;
		setLocation(x, y);
		count++;

		pack();
	}

	public void update() {
		DataBank db = _eventManager.getCurrentEvent().getBank(_bankName);
		if (db == null) {
			setVisible(false);
		} else {
			_dataBankPanel.setBank(db);
		}
	}

	private void setup() {
		_dataBankPanel = new DataBankPanel();
		add(_dataBankPanel, BorderLayout.CENTER);
	}
}
