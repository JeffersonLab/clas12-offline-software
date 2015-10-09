package cnuphys.bCNU.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;

/**
 * A dialog for a label and a font.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class LabelDialog extends JDialog implements ListSelectionListener,
		ItemListener {

	/**
	 * The last selected family, used for default in null constructor.
	 */
	private static Font lastFont = Fonts.commonFont(Font.PLAIN, 14);

	// italics checkbox
	JCheckBox italicCb;

	// bold checkbox
	JCheckBox boldCb;

	// sample display text
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

	// text foreground color
	private ColorLabel _textForeground;

	// text background color
	private ColorLabel _textBackground;

	// possible font sizes
	private String fontSizes[] = { " 8 ", " 10 ", " 11 ", " 12 ", " 14 ",
			" 16 ", " 18 ", " 20 ", " 24 ", " 30 ", " 36 ", " 40 ", " 48 ",
			" 60 ", " 72 " };

	/**
	 * The display area. Use a JLabel as the AWT label doesn't always honor
	 * setFont() in a timely fashion :-)
	 */
	private JLabel previewArea;

	/**
	 * The text field
	 */
	private JTextField textField;

	/**
	 * The result string.
	 */
	private String resultString = null;

	/**
	 * Null constructor will use the last font.
	 */
	public LabelDialog() {
		this(lastFont);
	}

	/**
	 * Construct a FontChooser -- Sets title and gets array of fonts on the
	 * system. Builds a GUI to let the user choose one font at one size.
	 */
	public LabelDialog(Font inFont) {
		setTitle("String Parameters");
		setModal(true);
		inputFont = inFont;

		// add the components

		Container cp = getContentPane();

		JPanel top = new JPanel();
		top.setLayout(new FlowLayout());

		// create and add the scroll lists
		createScrollLists(top);

		JPanel subPanel = new JPanel(new BorderLayout(2, 2));
		subPanel.add(top, BorderLayout.CENTER);

		subPanel.add(createTextAndColorBox(), BorderLayout.NORTH);

		cp.add(subPanel, BorderLayout.NORTH);

		// add the checkboxes for bold and italics
		top.add(createCheckBoxPanel());

		// create the preview area and place it in the center
		previewArea = new JLabel(displayText, SwingConstants.CENTER);
		previewArea.setSize(200, 50);
		cp.add(previewArea, BorderLayout.CENTER);

		// add the button panel in the south
		cp.add(createButtonPanel(), BorderLayout.SOUTH);
		previewFont(); // ensure view is up to date!

		pack();
		DialogUtilities.centerDialog(this);
	}

	/**
	 * Create the panel that holds the checkboxes for bold and italic.
	 * 
	 * @return the panel that holds the checkboxes for bold and italic.
	 */
	private JPanel createCheckBoxPanel() {
		JPanel checkBoxPanel = new JPanel();

		boldCb = new JCheckBox("Bold", false);
		italicCb = new JCheckBox("Italic", false);
		boldCb.addItemListener(this);
		italicCb.addItemListener(this);

		checkBoxPanel.setLayout(new GridLayout(0, 1));
		checkBoxPanel.add(boldCb);
		checkBoxPanel.add(italicCb);
		return checkBoxPanel;
	}

	/**
	 * Create the font scroll lists *family and size) and add them to the given
	 * panel.
	 * 
	 * @param panel
	 *            the panel to hold the lists.
	 */
	public void createScrollLists(JPanel panel) {
		// create the list of font families
		fontFamilyList = createFontList();
		JScrollPane scrollPane1 = new JScrollPane(fontFamilyList);
		fontFamilyList.setSelectedValue(inputFont.getFamily(), true);
		fontFamilyList.addListSelectionListener(this);

		// /create the list of sizes
		fontSizeList = new JList(fontSizes);
		fontSizeList.setVisibleRowCount(8);
		JScrollPane scrollPane2 = new JScrollPane(fontSizeList);
		fontSizeList.setSelectedValue(" " + inputFont.getSize() + " ", true);
		fontSizeList.addListSelectionListener(this);

		panel.add(scrollPane1);
		panel.add(scrollPane2);
	}

	/**
	 * Create the text entry field
	 * 
	 * @return the text entry field
	 */
	private void createTextEntryField() {
		textField = new JTextField();
		textField.setBorder(new CommonBorder("Enter the text"));
	}

	/**
	 * Create a box that contains the color labels.
	 * 
	 * @return a box that contains the color labels.
	 */
	private Box createTextAndColorBox() {
		Box box = Box.createVerticalBox();

		createTextEntryField();
		box.add(textField);
		box.add(Box.createVerticalStrut(8));

		JPanel subbox = new JPanel();
		subbox.setLayout(new VerticalFlowLayout());
		_textForeground = new ColorLabel(null, Color.black, "Foreground", 200);
		subbox.add(_textForeground);
		subbox.add(Box.createVerticalStrut(8));

		_textBackground = new ColorLabel(null, null, "Background", 200);
		subbox.add(_textBackground);
		subbox.setBorder(new CommonBorder("Colors"));

		box.add(subbox);
		box.add(Box.createVerticalStrut(8));

		return box;
	}

	/**
	 * Create the button panel.
	 * 
	 * @return the button panel.
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();

		// OK Button
		JButton okButton = new JButton(" OK ");
		panel.add(okButton);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				previewFont();
				lastFont = new Font(returnFont.getFamily(), returnFont
						.getStyle(), returnFont.getSize());
				dispose();
				resultString = textField.getText();
				setVisible(false);
			}
		});

		// Cancel button
		JButton canButton = new JButton("Cancel");
		panel.add(canButton);
		canButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				returnFont = inputFont;
				resultString = null;
				dispose();
				setVisible(false);
			}
		});
		return panel;
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
		int resultSize = Integer.parseInt(resultSizeName.trim());

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
	 * Gets the result string, which will be null if cancelled.
	 * 
	 * @return the result string.
	 */
	public String getText() {
		return resultString;
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

	/**
	 * Get the text foreground
	 * 
	 * @return the text foreground
	 */
	public Color getTextForeground() {
		return _textForeground.getColor();
	}

	/**
	 * Get the text background
	 * 
	 * @return the text background
	 */
	public Color getTextBackground() {
		return _textBackground.getColor();
	}

	/**
	 * Main program for testing.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		LabelDialog ld = new LabelDialog();
		ld.setVisible(true);

	}
}
