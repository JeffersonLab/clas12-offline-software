package cnuphys.bCNU.graphics.component;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public abstract class CapturedPrintStream extends PrintStream {
	
	/**
	 * Capture a standard stream
	 */
	public CapturedPrintStream() {
		super(new ByteArrayOutputStream());
	}

	@Override
	public abstract void println(String s);

	@Override
	public abstract void print(String s);

	@Override
	public void println(Object o) {
		if (o != null) {
			println(o.toString());
		} else {
			println("null");
		}
	}

	@Override
	public void print(Object o) {
		if (o != null) {
			print(o.toString());
		} else {
			print("null");
		}
	}

	@Override
	public void println(int i) {
		println("" + i);
	}

	@Override
	public void print(int i) {
		print("" + i);
	}

	@Override
	public void println(double d) {
		println("" + d);
	}

	@Override
	public void print(double d) {
		print("" + d);
	}

	@Override
	public void println(float f) {
		println("" + f);
	}

	@Override
	public void print(float f) {
		print("" + f);
	}

	@Override
	public void println(long i) {
		println("" + i);
	}

	@Override
	public void print(long i) {
		print("" + i);
	}

	@Override
	public void println(boolean b) {
		println("" + b);
	}

	@Override
	public void print(boolean b) {
		print("" + b);
	}

	@Override
	public void println(char c) {
		println("" + c);
	}

	@Override
	public void print(char c) {
		print("" + c);
	}

	@Override
	public void println(char[] ca) {
		if (ca != null) {
			println(new String(ca));
		} else {
			println("null");
		}
	}

	@Override
	public void print(char[] ca) {
		if (ca != null) {
			print(new String(ca));
		} else {
			print("null");
		}
	}

	@Override
	public void println() {
		println("");
	}

}