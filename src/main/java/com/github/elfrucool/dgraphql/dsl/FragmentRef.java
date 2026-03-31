package com.github.elfrucool.dgraphql.dsl;

/**
 * A reference to a query fragment.
 *
 * <p>Used to include a fragment's blocks in a query:</p>
 *
 * <pre>
 * FragmentRef.of("userFields")  // ...userFields
 * </pre>
 *
 * @see Fragment
 */
public record FragmentRef(String fragmentName) implements DqlElement {

    /**
     * Creates a fragment reference.
     *
     * <p>Example: {@code FragmentRef.of("userFields")}</p>
     */
    public static FragmentRef of(String fragmentName) {
        return new FragmentRef(fragmentName);
    }

    @Override
    public String dql() {
        return "... " + fragmentName;
    }
}
