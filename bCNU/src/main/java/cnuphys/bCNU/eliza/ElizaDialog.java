package cnuphys.bCNU.eliza;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.util.Fonts;

public class ElizaDialog extends JDialog implements ActionListener {

	// singleton
	private static ElizaDialog _instance;

	private static ElizaPanel _epanel;

	private JButton _clearButton;

	private ElizaDialog(JFrame owner) {
		super(owner, "Eliza", false);
		setLayout(new BorderLayout(2, 2));
		setIconImage(ImageManager.cnuIcon.getImage());
		addCenter();
		addSouth();
		pack();
		DialogUtilities.centerDialog(this);
	}

	private void addCenter() {
		_epanel = new ElizaPanel();
		add(_epanel, BorderLayout.CENTER);
	}

	private void addSouth() {
		JPanel bp = new JPanel();
		bp.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		_clearButton = makeButton(" clear ", bp);
		add(bp, BorderLayout.SOUTH);
	}

	private JButton makeButton(String label, JPanel bp) {
		JButton button = new JButton(label);
		button.setFont(Fonts.smallFont);
		bp.add(button);
		button.addActionListener(this);
		return button;
	}

	public static void showEliza(JFrame owner) {
		if (_instance == null) {
			_instance = new ElizaDialog(owner);
		}
		_instance.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == _clearButton) {
			System.err.println("Clear Eliza text");
			_epanel.clear();
		}

	}
}
