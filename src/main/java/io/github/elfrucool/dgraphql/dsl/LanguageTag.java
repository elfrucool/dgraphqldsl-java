package io.github.elfrucool.dgraphql.dsl;

/**
 * A language tag for language-tagged predicates.
 *
 * <p>Used to query specific language variations of string predicates:</p>
 *
 * <pre>
 * LanguageTag.en()      // @en
 * LanguageTag.fr()      // @fr
 * LanguageTag.of("en", "us")  // @en:us
 * </pre>
 *
 * <p>Example usage:</p>
 * <pre>
 * Block.predicate("name", LanguageTag.en)
 * </pre>
 */
public record LanguageTag(String... tags) implements DqlElement {

    /**
     * English language tag (@en).
     */
    public static LanguageTag en() {
        return new LanguageTag("en");
    }

    /**
     * French language tag (@fr).
     */
    public static LanguageTag fr() {
        return new LanguageTag("fr");
    }

    /**
     * German language tag (@de).
     */
    public static LanguageTag de() {
        return new LanguageTag("de");
    }

    /**
     * Spanish language tag (@es).
     */
    public static LanguageTag es() {
        return new LanguageTag("es");
    }

    /**
     * Creates a custom language tag.
     *
     * <p>Example: {@code LanguageTag.of("en", "us")} produces {@code @en:us}</p>
     */
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
