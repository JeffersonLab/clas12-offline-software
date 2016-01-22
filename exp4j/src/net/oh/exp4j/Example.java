package net.oh.exp4j;

public class Example {

	public static void main(String arg[]) {
		Expression e = new ExpressionBuilder("3 * sin(y) - 2 / (x - 2)").variables("x", "y").build();
		e.setVariable("x", 2.3);
		e.setVariable("y", Math.PI/2);
		System.err.println(""+e.evaluate());
		e.setVariable("y", -Math.PI/2);
		System.err.println(""+e.evaluate());
		
		System.err.println("===========");
		Expression e2 = new ExpressionBuilder("toDegrees(atan2(y, x))").variables("x", "y").build();
		e2.setVariable("x", -Math.sqrt(3));
		e2.setVariable("y", -1);
		System.err.println(""+e2.evaluate());
		
		System.err.println("===========");
		Expression e3 = new ExpressionBuilder("x + 2^x").variables("x").build();
		e3.setVariable("x", 3);
		System.err.println(""+e3.evaluate());

		System.err.println("===========");
		Expression e4 = new ExpressionBuilder("8*2-10").variables().build();
		System.err.println(""+e4.evaluate());

	}
}
