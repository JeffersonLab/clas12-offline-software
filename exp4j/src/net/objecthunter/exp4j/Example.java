package net.objecthunter.exp4j;

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
	}
}
