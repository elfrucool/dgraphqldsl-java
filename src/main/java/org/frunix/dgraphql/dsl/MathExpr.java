package org.frunix.dgraphql.dsl;

public record MathExpr(String expression) implements DqlElement {

    public static MathExpr of(String expression) {
        return new MathExpr(expression);
    }

    public static MathExpr add(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " + " + formatOperand(right));
    }

    public static MathExpr subtract(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " - " + formatOperand(right));
    }

    public static MathExpr multiply(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " * " + formatOperand(right));
    }

    public static MathExpr divide(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " / " + formatOperand(right));
    }

    public static MathExpr mod(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " % " + formatOperand(right));
    }

    public static MathExpr neg(Object operand) {
        return new MathExpr("-" + formatOperand(operand));
    }

    public static MathExpr cond(Object condition, Object trueVal, Object falseVal) {
        return new MathExpr("cond(" + formatOperand(condition) + ", " + formatOperand(trueVal) + ", " + formatOperand(falseVal) + ")");
    }

    public static MathExpr min(Object... operands) {
        return new MathExpr("min(" + join(operands) + ")");
    }

    public static MathExpr max(Object... operands) {
        return new MathExpr("max(" + join(operands) + ")");
    }

    public static MathExpr sqrt(Object operand) {
        return new MathExpr("sqrt(" + formatOperand(operand) + ")");
    }

    public static MathExpr ln(Object operand) {
        return new MathExpr("ln(" + formatOperand(operand) + ")");
    }

    public static MathExpr exp(Object operand) {
        return new MathExpr("exp(" + formatOperand(operand) + ")");
    }

    public static MathExpr since(Object datePredicate) {
        return new MathExpr("since(" + formatOperand(datePredicate) + ")");
    }

    public static MathExpr gt(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " > " + formatOperand(right));
    }

    public static MathExpr lt(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " < " + formatOperand(right));
    }

    public static MathExpr ge(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " >= " + formatOperand(right));
    }

    public static MathExpr le(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " <= " + formatOperand(right));
    }

    public static MathExpr eq(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " == " + formatOperand(right));
    }

    public static MathExpr ne(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " != " + formatOperand(right));
    }

    public static MathExpr and(Object left, Object right) {
        return new MathExpr(formatOperand(left) + " && " + formatOperand(right));
    }

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
