package cnuphys.bCNU.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cnuphys.bCNU.util.Fonts;

@SuppressWarnings("serial")
public class FontDialog extends JDialog implements ListSelectionListener,
		ItemListener {

	/**
	 * The last selected family, used for default in null constructor.
	 */
	private static Font lastFont = Fonts.commonFont(Font.PLAIN, 14);

	/**
	 * Selects italic
	 */
	JCheckBox italicCb = null;

	/**
	 * Selects bold
	 */
	JCheckBox boldCb = null;

	/**
	 * Sample isplay text
	 */
	protected String displayText = "Sample Text 123";

	/**
	 * The font family name list
	 */
	private JList fontFamilyList;

	/**
	 * The font size list
	 */
	private JList fontSizeList;

	/**
	 * The return font.
	 */
	private Font returnFont;

	/**
	 * The input font
	 */
	private final Font inputFont;

	/**
	 * The list of font sizes
	 */
	private String fontSizes[] = { "8", "10", "11", "12", "14", "16", "18",
			"20", "24", "30", "36", "40", "48", "60", "72" };

	/**
	 * The display area. Use a JLabel as the AWT label doesn't always honor
	 * setFont() in a timely fashion :-)
	 */
	private JLabel previewArea;

	/**
	 * Null constructor will use the last font.
	 */
	public FontDialog() {
		this(lastFont);
	}

	/**
	 * Construct a FontChooser -- Sets title and gets array of fonts on the
	 * system. Builds a GUI to let the user choose one font at one size.
	 */
	public FontDialog(Font inFont) {
		setTitle("Font Selection");
		setModal(true);
		this.inputFont = inFont;

		Container cp = getContentPane();

		Panel top = new Panel();
		top.setLayout(new FlowLayout());

		// create the list of font families
		fontFamilyList = createFontList();
		JScrollPane scrollPane1 = new JScrollPane(fontFamilyList);
		top.add(scrollPane1);
		fontFamilyList.setSelectedValue(inputFont.getFamily(), true);
		System.out.println(inputFont.getFamily());
		fontFamilyList.addListSelectionListener(this);

		// /create the list of sizes
		fontSizeList = new JList(fontSizes);
		fontSizeList.setVisibleRowCount(8);
		JScrollPane scrollPane2 = new JScrollPane(fontSizeList);
		top.add(scrollPane2);
		fontSizeList.setSelectedValue("" + inputFont.getSize(), true);
		fontSizeList.addListSelectionListener(this);

		cp.add(top, BorderLayout.NORTH);

		Panel attrs = new Panel();
		top.add(attrs);

		boldCb = new JCheckBox("Bold", false);
		italicCb = new JCheckBox("Italic", false);
		boldCb.addItemListener(this);
		italicCb.addItemListener(this);

		attrs.setLayout(new GridLayout(0, 1));
		attrs.add(boldCb);
		attrs.add(italicCb);

		// create the preview area
		previewArea = new JLabel(displayText, SwingConstants.CENTER);
		previewArea.setSize(200, 50);
		cp.add(previewArea, BorderLayout.CENTER);

		Panel bot = new Panel();

		// OK Button
		JButton okButton = new JButton(" OK ");
		bot.add(okButton);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				previewFont();
				lastFont = new Font(returnFont.getFamily(), returnFont
						.getStyle(), returnFont.getSize());
				dispose();
				setVisible(false);
			}
		});

		// Cancel button
		JButton canButton = new JButton("Cancel");
		bot.add(canButton);
		canButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				returnFont = inputFont;
				dispose();
				setVisible(false);
			}
		});

		cp.add(bot, BorderLayout.SOUTH);

		previewFont(); // ensure view is up to date!

		pack();
		setLocation(100, 100);
	}

	/**
	 * Create the font list.
	 * 
	 * @return the font family selection list.
	 */
	private JList createFontList() {

		String[] fontList = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAvailableFontFamilyNames();

		JList list = new JList(fontList);

		list.setVisibleRowCount(8);
		return list;
	}

	/**
	 * Called from the action handlers to get the font info, build a font, and
	 * set it.
	 */
	protected void previewFont() {
		String resultName = (String) (fontFamilyList.getSelectedValue());
		String resultSizeName = (String) (fontSizeList.getSelectedValue());
		int resultSize = Integer.parseInt(resultSizeName);

		boolean isBold = boldCb.isSelected();
		boolean isItalic = italicCb.isSelected();

		int attrs = Font.PLAIN;
		if (isBold)
			attrs = Font.BOLD;
		if (isItalic)
			attrs |= Font.ITALIC;
		returnFont = new Font(resultName, attrs, resultSize);
		previewArea.setFont(returnFont);
		pack(); // ensure Dialog is big enough.
	}

	/** Retrieve the selected font */
	public Font getSelectedFont() {
		return returnFont;
	}

	/**
	 * One of the list's was selected. Redo the preview.
	 * 
	 * @param lse
	 *            the list selection event.
	 */
	@Override
	public void valueChanged(ListSelectionEvent lse) {
		previewFont();
	}

	/**
	 * One of check boxes was selected.
	 * 
	 * @param ise
	 *            the state changed event.
	 */
	@Override
	public void itemStateChanged(ItemEvent ise) {
		previewFont();
	}

}
