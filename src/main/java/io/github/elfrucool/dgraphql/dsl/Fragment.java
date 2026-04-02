package io.github.elfrucool.dgraphql.dsl;

import java.util.ArrayList;
import java.util.List;

/**
 * A query fragment for reusable query definitions.
 *
 * <p>Fragments define reusable sets of blocks that can be referenced
 * in queries using {@link FragmentRef}:</p>
 *
 * <pre>
 * Fragment.fragment("userFields").withBlock(Block.predicate("name")).withBlock(Block.predicate("email"))
 * </pre>
 *
 * @see FragmentRef
 * @see Query#withFragments(List)
 */
public record Fragment(
    String name,
    List<Block> blocks
) implements DqlElement {

    /**
     * Creates a fragment with the given name.
     *
     * <p>Example: {@code Fragment.fragment("userFields")}</p>
     */
    public static Fragment fragment(String name) {
        return new Fragment(name, List.of());
    }

    public Fragment withBlocks(List<Block> blocks) {
        return new Fragment(this.name, blocks);
    }

    public Fragment withBlock(Block block) {
        List<Block> newBlocks = new ArrayList<>(blocks);
        newBlocks.add(block);
        return withBlocks(newBlocks);
    }

    @Override
    public String dql() {
        StringBuilder sb = new StringBuilder();
        sb.append("fragment ").append(name).append(" { ");

        for (int i = 0; i < blocks.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(blocks.get(i).dql());
        }

        sb.append(" }");
        return sb.toString();
    }
}
