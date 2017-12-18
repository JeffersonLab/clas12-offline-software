package cnuphys.bCNU.component;

import java.io.IOException;
import java.io.Writer;

import javax.swing.JTextArea;

public class TextAreaWriter extends Writer {

	private JTextArea _textArea;
	
	public TextAreaWriter(JTextArea textArea) {
		_textArea = textArea;
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
	}

	@Override
	public void flush() throws IOException {

	}

	@Override
	public void close() throws IOException {
	}
	
	@Override
	public void write(String s) {
		_textArea.append(s);
	}
	
	public void writeln(String s) {
		_textArea.append(s + "\n");
	}



}
