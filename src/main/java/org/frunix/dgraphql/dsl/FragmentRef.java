package org.frunix.dgraphql.dsl;

public record FragmentRef(String fragmentName) implements DqlElement {

    public static FragmentRef of(String fragmentName) {
        return new FragmentRef(fragmentName);
    }

    @Override
    public String dql() {
        return "... " + fragmentName;
    }
}
