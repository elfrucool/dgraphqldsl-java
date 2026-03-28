package org.frunix.dgraphql.dsl;

public record LanguageTag(String... tags) implements DqlElement {

    public static LanguageTag en() {
        return new LanguageTag("en");
    }

    public static LanguageTag fr() {
        return new LanguageTag("fr");
    }

    public static LanguageTag de() {
        return new LanguageTag("de");
    }

    public static LanguageTag es() {
        return new LanguageTag("es");
    }

    public static LanguageTag of(String... tags) {
        return new LanguageTag(tags);
    }

    @Override
    public String dql() {
        return "@" + String.join(":", tags);
    }

    public String applyTo(String predicate) {
        return predicate + dql();
    }
}
