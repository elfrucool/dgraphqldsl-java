package org.frunix.dgraphql.dsl;

import java.util.ArrayList;
import java.util.List;

public record Fragment(
    String name,
    List<Block> blocks
) implements DqlElement {

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
