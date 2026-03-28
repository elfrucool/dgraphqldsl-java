package org.frunix.dgraphql.dsl;

public record SetTriple(String subject, String predicate, Object value, LanguageTag languageTag) implements DqlElement {

    public static SetTriple subject(String subject) {
        return new SetTriple(subject, null, null, null);
    }

    public SetTriple predicate(String predicate) {
        return new SetTriple(this.subject, predicate, this.value, this.languageTag);
    }

    public SetTriple value(Object value) {
        return new SetTriple(this.subject, this.predicate, value, this.languageTag);
    }

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