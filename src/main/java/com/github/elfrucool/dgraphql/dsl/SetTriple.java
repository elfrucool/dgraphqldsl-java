package com.github.elfrucool.dgraphql.dsl;

/**
 * A RDF triple for mutations (subject-predicate-object).
 *
 * <p>Used in Set and Delete mutations to specify what data to add or remove.
 * Uses a fluent builder pattern:</p>
 *
 * <pre>
 * SetTriple.subject("0x123").predicate("name").value("Alice")
 * SetTriple.subject("_:newUser").predicate("email").value("alice@example.com")
 * </pre>
 *
 * <p>Supported subject forms:</p>
 * <ul>
 *   <li>UID: {@code 0x123}, {@code <0x123>}</li>
 *   <li>Blank node: {@code _:newNode}</li>
 *   <li>Name: {@code user} (no prefix)</li>
 *   <li>UID function: {@code uid(0x123)}</li>
 * </ul>
 *
 * @see Mutation.Set
 * @see Mutation.Delete
 */
public record SetTriple(String subject, String predicate, Object value, LanguageTag languageTag) implements DqlElement {

    /**
     * Creates a triple with just a subject (for blank nodes).
     *
     * <p>Example: {@code SetTriple.subject("_:newUser")}</p>
     */
    public static SetTriple subject(String subject) {
        return new SetTriple(subject, null, null, null);
    }

    /**
     * Sets the predicate for this triple.
     *
     * <p>Example: {@code SetTriple.subject("0x123").predicate("name")}</p>
     */
    public SetTriple predicate(String predicate) {
        return new SetTriple(this.subject, predicate, this.value, this.languageTag);
    }

    /**
     * Sets the value (object) for this triple.
     *
     * <p>Example: {@code SetTriple.subject("0x123").predicate("name").value("Alice")}</p>
     *
     * @param value The object value (String, Number, Boolean, or UID)
     */
    public SetTriple value(Object value) {
        return new SetTriple(this.subject, this.predicate, value, this.languageTag);
    }

    /**
     * Sets the language tag for language-tagged predicates.
     *
     * <p>Example: {@code SetTriple.subject("0x123").predicate("name").withLanguageTag(LanguageTag.en)}</p>
     *
     * @see LanguageTag
     */
    public SetTriple withLanguageTag(LanguageTag languageTag) {
        return new SetTriple(this.subject, this.predicate, this.value, languageTag);
    }

    @Override
    public String dql() {
        return formatSubject() + " " + formatPredicate() + " " + formatValue() + " .";
    }

    private String formatSubject() {
        if (subject == null) return "_:";
        if (subject.startsWith("_:")) return subject;
        if (subject.startsWith("uid(")) return subject;
        if (subject.startsWith("0x") || subject.startsWith("0X")) return "<" + subject + ">";
        if (subject.contains(":")) return "<" + subject + ">";
        return subject;
    }

    private String formatPredicate() {
        if (predicate == null) return "";
        String pred = predicate;
        if (languageTag != null) {
            pred = predicate + languageTag.dql();
        }
        if ("*".equals(pred)) return "*";
        if (pred.startsWith("0x") || pred.startsWith("0X")) return "<" + pred + ">";
        if (pred.contains(":")) return "<" + pred + ">";
        return pred;
    }

    private String formatValue() {
        if (value == null) return "_:";
        if (value instanceof String s) {
            if (s.equals("*")) return "*";
            if (s.startsWith("_:")) return s;
            if (s.startsWith("0x") || s.startsWith("0X")) return "<" + s + ">";
            if (s.contains(":")) return "<" + s + ">";
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return value.toString();
    }
}