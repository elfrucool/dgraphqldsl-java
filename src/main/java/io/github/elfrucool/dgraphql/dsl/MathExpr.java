package io.github.elfrucool.dgraphql.dsl;

/**
 * A mathematical expression for math functions.
 *
 * <p>Used with {@code Func.math()} to compute values in queries:</p>
 *
 * <ul>
 *   <li><b>Arithmetic</b>: add, subtract, multiply, divide, mod, neg</li>
 *   <li><b>Conditional</b>: cond</li>
 *   <li><b>Aggregation</b>: min, max</li>
 *   <li><b>Math functions</b>: sqrt, ln, exp, since</li>
 *   <li><b>Comparison</b>: gt, lt, ge, le, eq, ne</li>
 *   <li><b>Logical</b>: and, or</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * MathExpr.add("count", 1)
 * MathExpr.multiply("price", "quantity")
 * MathExpr.cond(MathExpr.gt("score", 100), "high", "low")
 * </pre>
 *
 * @see Func#math(String)
 */
public record MathExpr(String expression) implements DqlElement {

    /**
     * Creates a math expression from a string.
     */
    public static MathExpr of(String expression) {
        return new MathExpr(expression);
    }

    /**
     * Addition: left + right.
     */
    public static MathExpr add(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " + " + formatOperand(right));
    }

    /**
     * Subtraction: left - right.
     */
    public static MathExpr subtract(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " - " + formatOperand(right));
    }

    /**
     * Multiplication: left * right.
     */
    public static MathExpr multiply(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " * " + formatOperand(right));
    }

    /**
     * Division: left / right.
     */
    public static MathExpr divide(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " / " + formatOperand(right));
    }

    /**
     * Modulo: left % right.
     */
    public static MathExpr mod(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " % " + formatOperand(right));
    }

    /**
     * Negation: -operand.
     */
    public static MathExpr neg(Object operand) {
        return new MathExpr("-" + formatOperand(operand));
    }

    /**
     * Conditional: cond(condition, trueValue, falseValue).
     */
    public static MathExpr cond(Object condition, Object trueVal, Object falseVal) {
        return new MathExpr("cond(" + formatOperand(condition) + ", " + formatOperand(trueVal) + ", " + formatOperand(falseVal) + ")");
    }

    /**
     * Minimum of multiple values.
     */
    public static MathExpr min(Object... operands) {
        return new MathExpr("min(" + join(operands) + ")");
    }

    /**
     * Maximum of multiple values.
     */
    public static MathExpr max(Object... operands) {
        return new MathExpr("max(" + join(operands) + ")");
    }

    /**
     * Square root.
     */
    public static MathExpr sqrt(Object operand) {
        return new MathExpr("sqrt(" + formatOperand(operand) + ")");
    }

    /**
     * Natural logarithm.
     */
    public static MathExpr ln(Object operand) {
        return new MathExpr("ln(" + formatOperand(operand) + ")");
    }

    /**
     * Exponential (e^x).
     */
    public static MathExpr exp(Object operand) {
        return new MathExpr("exp(" + formatOperand(operand) + ")");
    }

    /**
     * Seconds since a date predicate.
     */
    public static MathExpr since(Object datePredicate) {
        return new MathExpr("since(" + formatOperand(datePredicate) + ")");
    }

    /**
     * Greater than: left > right.
     */
    public static MathExpr gt(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " > " + formatOperand(right));
    }

    /**
     * Less than: left < right.
     */
    public static MathExpr lt(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " < " + formatOperand(right));
    }

    /**
     * Greater than or equal: left >= right.
     */
    public static MathExpr ge(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " >= " + formatOperand(right));
    }

    /**
     * Less than or equal: left <= right.
     */
    public static MathExpr le(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " <= " + formatOperand(right));
    }

    /**
     * Equal: left == right.
     */
    public static MathExpr eq(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " == " + formatOperand(right));
    }

    /**
     * Not equal: left != right.
     */
    public static MathExpr ne(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " != " + formatOperand(right));
    }

    /**
     * Logical AND: left && right.
     */
    public static MathExpr and(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " && " + formatOperand(right));
    }

    /**
     * Logical OR: left || right.
     */
    public static MathExpr or(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " || " + formatOperand(right));
    }

    @Override
    public String dql() {
        return expression;
    }

    private static String formatOperand(Object o) {
        if (o instanceof MathExpr m) {
            return "(" + m.expression + ")";
        }
        if (o instanceof String s) {
            if (s.startsWith("var(") || s.startsWith("val(") || s.startsWith("count(") || s.startsWith("math(") || s.contains("(")) {
                return s;
            }
            return s;
        }
        if (o instanceof Number || o instanceof Boolean) {
            return o.toString();
        }
        if (o instanceof Variable v) {
            return v.dql();
        }
        return o.toString();
    }

    private static String join(Object[] operands) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < operands.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(formatOperand(operands[i]));
        }
        return sb.toString();
    }
}
