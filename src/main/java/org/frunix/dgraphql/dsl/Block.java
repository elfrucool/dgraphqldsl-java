package org.frunix.dgraphql.dsl;

import java.util.ArrayList;
import java.util.List;

public sealed interface Block extends DqlElement 
    permits Block.Predicate, Block.FuncBlock, Block.Nested, Block.Reverse, Block.Var, Block.GroupByBlock, Block.Expand {

    List<Block> blocks();
    List<Directive> directives();
    default LanguageTag languageTag() { return null; }

    default Block withBlocks(List<Block> blocks) {
        return switch (this) {
            case Predicate p -> p.withBlocks(blocks);
            case FuncBlock f -> f.withBlocks(blocks);
            case Nested n -> n.withBlocks(blocks);
            case Reverse r -> r.withBlocks(blocks);
            case Var v -> v.withBlocks(blocks);
            case GroupByBlock g -> g.withBlocks(blocks);
            case Expand e -> e.withBlocks(blocks);
        };
    }

    default Block withBlock(Block block) {
        return switch (this) {
            case Predicate p -> p.withBlock(block);
            case FuncBlock f -> f.withBlock(block);
            case Nested n -> n.withBlock(block);
            case Reverse r -> r.withBlock(block);
            case Var v -> v.withBlock(block);
            case GroupByBlock g -> g.withBlock(block);
            case Expand e -> e.withBlock(block);
        };
    }

    default Block withDirective(Directive directive) {
        return switch (this) {
            case Predicate p -> p.withDirective(directive);
            case FuncBlock f -> f.withDirective(directive);
            case Nested n -> n.withDirective(directive);
            case Reverse r -> r.withDirective(directive);
            case Var v -> v;
            case GroupByBlock g -> g.withDirective(directive);
            case Expand e -> e.withDirective(directive);
        };
    }

    default Block withDirectives(List<Directive> directives) {
        return switch (this) {
            case Predicate p -> p.withDirectives(directives);
            case FuncBlock f -> f.withDirectives(directives);
            case Nested n -> n.withDirectives(directives);
            case Reverse r -> r.withDirectives(directives);
            case Var v -> v;
            case GroupByBlock g -> g.withDirectives(directives);
            case Expand e -> e.withDirectives(directives);
        };
    }

    default Block withLanguageTag(LanguageTag languageTag) {
        return switch (this) {
            case Predicate p -> p.withLanguageTag(languageTag);
            case Nested n -> n.withLanguageTag(languageTag);
            case Reverse r -> r.withLanguageTag(languageTag);
            case FuncBlock f -> f;
            case Var v -> v;
            case GroupByBlock g -> g;
            case Expand e -> e;
        };
    }

    record Predicate(
        String name,
        String alias,
        List<Block> blocks,
        List<Directive> directives,
        LanguageTag languageTag
    ) implements Block {

        public static Predicate of(String name) {
            return new Predicate(name, null, List.of(), List.of(), null);
        }

        public static Predicate of(String name, String alias) {
            return new Predicate(name, alias, List.of(), List.of(), null);
        }

        public static Predicate of(String name, LanguageTag languageTag) {
            return new Predicate(name, null, List.of(), List.of(), languageTag);
        }

        public static Predicate of(String name, String alias, LanguageTag languageTag) {
            return new Predicate(name, alias, List.of(), List.of(), languageTag);
        }

        public Predicate withBlocks(List<Block> blocks) {
            return new Predicate(this.name, this.alias, blocks, this.directives, this.languageTag);
        }

        public Predicate withBlock(Block block) {
            List<Block> newBlocks = new ArrayList<>(blocks);
            newBlocks.add(block);
            return withBlocks(newBlocks);
        }

        public Predicate withDirectives(List<Directive> directives) {
            return new Predicate(this.name, this.alias, this.blocks, directives, this.languageTag);
        }

        public Predicate withDirective(Directive directive) {
            List<Directive> newDirectives = new ArrayList<>(directives);
            newDirectives.add(directive);
            return withDirectives(newDirectives);
        }

        public Predicate withAlias(String alias) {
            return new Predicate(this.name, alias, this.blocks, this.directives, this.languageTag);
        }

        public Predicate withLanguageTag(LanguageTag languageTag) {
            return new Predicate(this.name, this.alias, this.blocks, this.directives, languageTag);
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();

            if (alias != null && !alias.isEmpty()) {
                sb.append(alias).append(": ");
            }

            sb.append(name);

            if (languageTag != null) {
                sb.append(languageTag.dql());
            }

            if (!directives.isEmpty()) {
                for (Directive d : directives) {
                    sb.append(" ").append(d.dql());
                }
            }

            if (!blocks.isEmpty()) {
                sb.append(" { ");
                for (int i = 0; i < blocks.size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(blocks.get(i).dql());
                }
                sb.append(" }");
            }

            return sb.toString();
        }
    }

    record FuncBlock(
        Func func,
        String alias,
        List<Block> blocks,
        List<Directive> directives
    ) implements Block {

        public static FuncBlock of(Func func) {
            return new FuncBlock(func, null, List.of(), List.of());
        }

        public static FuncBlock of(Func func, String alias) {
            return new FuncBlock(func, alias, List.of(), List.of());
        }

        public FuncBlock withBlocks(List<Block> blocks) {
            return new FuncBlock(this.func, this.alias, blocks, this.directives);
        }

        public FuncBlock withBlock(Block block) {
            List<Block> newBlocks = new ArrayList<>(blocks);
            newBlocks.add(block);
            return withBlocks(newBlocks);
        }

        public FuncBlock withDirectives(List<Directive> directives) {
            return new FuncBlock(this.func, this.alias, this.blocks, directives);
        }

        public FuncBlock withDirective(Directive directive) {
            List<Directive> newDirectives = new ArrayList<>(directives);
            newDirectives.add(directive);
            return withDirectives(newDirectives);
        }

        public FuncBlock withAlias(String alias) {
            return new FuncBlock(this.func, alias, this.blocks, this.directives);
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();

            if (alias != null && !alias.isEmpty()) {
                sb.append(alias).append(": ");
            }

            sb.append(func.dql());

            if (!directives.isEmpty()) {
                for (Directive d : directives) {
                    sb.append(" ").append(d.dql());
                }
            }

            if (!blocks.isEmpty()) {
                sb.append(" { ");
                for (int i = 0; i < blocks.size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(blocks.get(i).dql());
                }
                sb.append(" }");
            }

            return sb.toString();
        }
    }

    record Nested(
        String name,
        List<Block> blocks,
        List<Directive> directives,
        LanguageTag languageTag
    ) implements Block {

        public static Nested of(String name) {
            return new Nested(name, List.of(), List.of(), null);
        }

        public static Nested of(String name, List<Directive> directives) {
            return new Nested(name, List.of(), directives, null);
        }

        public static Nested of(String name, LanguageTag languageTag) {
            return new Nested(name, List.of(), List.of(), languageTag);
        }

        public static Nested of(String name, List<Directive> directives, LanguageTag languageTag) {
            return new Nested(name, List.of(), directives, languageTag);
        }

        public Nested withBlocks(List<Block> blocks) {
            return new Nested(this.name, blocks, this.directives, this.languageTag);
        }

        public Nested withBlock(Block block) {
            List<Block> newBlocks = new ArrayList<>(blocks);
            newBlocks.add(block);
            return withBlocks(newBlocks);
        }

        public Nested withDirectives(List<Directive> directives) {
            return new Nested(this.name, this.blocks, directives, this.languageTag);
        }

        public Nested withDirective(Directive directive) {
            List<Directive> newDirectives = new ArrayList<>(directives);
            newDirectives.add(directive);
            return withDirectives(newDirectives);
        }

        public Nested withLanguageTag(LanguageTag languageTag) {
            return new Nested(this.name, this.blocks, this.directives, languageTag);
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append(name);

            if (languageTag != null) {
                sb.append(languageTag.dql());
            }

            if (!directives.isEmpty()) {
                for (Directive d : directives) {
                    sb.append(" ").append(d.dql());
                }
            }

            if (!blocks.isEmpty()) {
                sb.append(" { ");
                for (int i = 0; i < blocks.size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(blocks.get(i).dql());
                }
                sb.append(" }");
            }

            return sb.toString();
        }
    }

    record Reverse(
        String name,
        List<Block> blocks,
        List<Directive> directives,
        LanguageTag languageTag
    ) implements Block {

        public static Reverse of(String name) {
            return new Reverse(name, List.of(), List.of(), null);
        }

        public static Reverse of(String name, List<Directive> directives) {
            return new Reverse(name, List.of(), directives, null);
        }

        public static Reverse of(String name, LanguageTag languageTag) {
            return new Reverse(name, List.of(), List.of(), languageTag);
        }

        public static Reverse of(String name, List<Directive> directives, LanguageTag languageTag) {
            return new Reverse(name, List.of(), directives, languageTag);
        }

        public Reverse withBlocks(List<Block> blocks) {
            return new Reverse(this.name, blocks, this.directives, this.languageTag);
        }

        public Reverse withBlock(Block block) {
            List<Block> newBlocks = new ArrayList<>(blocks);
            newBlocks.add(block);
            return withBlocks(newBlocks);
        }

        public Reverse withDirectives(List<Directive> directives) {
            return new Reverse(this.name, this.blocks, directives, this.languageTag);
        }

        public Reverse withDirective(Directive directive) {
            List<Directive> newDirectives = new ArrayList<>(directives);
            newDirectives.add(directive);
            return withDirectives(newDirectives);
        }

        public Reverse withLanguageTag(LanguageTag languageTag) {
            return new Reverse(this.name, this.blocks, this.directives, languageTag);
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append("~").append(name);

            if (languageTag != null) {
                sb.append(languageTag.dql());
            }

            if (!directives.isEmpty()) {
                for (Directive d : directives) {
                    sb.append(" ").append(d.dql());
                }
            }

            if (!blocks.isEmpty()) {
                sb.append(" { ");
                for (int i = 0; i < blocks.size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(blocks.get(i).dql());
                }
                sb.append(" }");
            }

            return sb.toString();
        }
    }

    record Var(
        String varName,
        String predicate,
        List<Block> blocks
    ) implements Block {

        public static Var of(String varName, String predicate) {
            return new Var(varName, predicate, List.of());
        }

        public Var withBlocks(List<Block> blocks) {
            return new Var(this.varName, this.predicate, blocks);
        }

        public Var withBlock(Block block) {
            List<Block> newBlocks = new ArrayList<>(blocks);
            newBlocks.add(block);
            return withBlocks(newBlocks);
        }

        @Override
        public List<Directive> directives() {
            return List.of();
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append(varName).append(" as ").append(predicate);

            if (!blocks.isEmpty()) {
                sb.append(" { ");
                for (int i = 0; i < blocks.size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(blocks.get(i).dql());
                }
                sb.append(" }");
            }

            return sb.toString();
        }
    }

    record GroupByBlock(
        GroupBy groupBy
    ) implements Block {

        public GroupByBlock withBlocks(List<Block> blocks) {
            return new GroupByBlock(groupBy.withBlocks(blocks));
        }

        public GroupByBlock withBlock(Block block) {
            return new GroupByBlock(groupBy.withBlock(block));
        }

        public GroupByBlock withDirectives(List<Directive> directives) {
            return new GroupByBlock(groupBy.withDirectives(directives));
        }

        public GroupByBlock withDirective(Directive directive) {
            return new GroupByBlock(groupBy.withDirective(directive));
        }

        @Override
        public List<Block> blocks() {
            return groupBy.blocks();
        }

        @Override
        public List<Directive> directives() {
            return groupBy.directives();
        }

        @Override
        public String dql() {
            return groupBy.dql();
        }
    }

    static Block predicate(String name) {
        return Predicate.of(name);
    }

    static Block predicate(String name, String alias) {
        return Predicate.of(name, alias);
    }

    static Block predicate(String name, LanguageTag languageTag) {
        return Predicate.of(name, languageTag);
    }

    static Block predicate(String name, String alias, LanguageTag languageTag) {
        return Predicate.of(name, alias, languageTag);
    }

    static Block predicate(Func func) {
        return FuncBlock.of(func);
    }

    static Block predicate(Func func, String alias) {
        return FuncBlock.of(func, alias);
    }

    static Block var(String varName, String predicate) {
        return Var.of(varName, predicate);
    }

    static Block nested(String name) {
        return Nested.of(name);
    }

    static Block nested(String name, List<Directive> directives) {
        return Nested.of(name, directives);
    }

    static Block nested(String name, LanguageTag languageTag) {
        return Nested.of(name, languageTag);
    }

    static Block nested(String name, List<Directive> directives, LanguageTag languageTag) {
        return Nested.of(name, directives, languageTag);
    }

    static Block reverse(String name) {
        return Reverse.of(name);
    }

    static Block reverse(String name, List<Directive> directives) {
        return Reverse.of(name, directives);
    }

    static Block reverse(String name, LanguageTag languageTag) {
        return Reverse.of(name, languageTag);
    }

    static Block reverse(String name, List<Directive> directives, LanguageTag languageTag) {
        return Reverse.of(name, directives, languageTag);
    }

    static Block groupBy(String predicate) {
        return new GroupByBlock(GroupBy.groupBy(predicate));
    }

    record Expand(
        String typeName,
        List<Block> blocks,
        List<Directive> directives
    ) implements Block {

        public static Expand type(String typeName) {
            return new Expand(typeName, List.of(), List.of());
        }

        public static Expand all() {
            return new Expand("_all_", List.of(), List.of());
        }

        public static Expand typeWithFilter(String typeName, Filter filter) {
            return new Expand(typeName, List.of(), List.of(Directive.filter(filter)));
        }

        public static Expand allWithFilter(Filter filter) {
            return new Expand("_all_", List.of(), List.of(Directive.filter(filter)));
        }

        public Expand withBlocks(List<Block> blocks) {
            return new Expand(this.typeName, blocks, this.directives);
        }

        public Expand withBlock(Block block) {
            List<Block> newBlocks = new ArrayList<>(blocks);
            newBlocks.add(block);
            return withBlocks(newBlocks);
        }

        public Expand withDirectives(List<Directive> directives) {
            return new Expand(this.typeName, this.blocks, directives);
        }

        public Expand withDirective(Directive directive) {
            List<Directive> newDirectives = new ArrayList<>(directives);
            newDirectives.add(directive);
            return withDirectives(newDirectives);
        }

        @Override
        public List<Block> blocks() {
            return blocks;
        }

        @Override
        public List<Directive> directives() {
            return directives;
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();
            sb.append("expand(").append(typeName).append(")");
            
            for (Directive d : directives) {
                sb.append(" ").append(d.dql());
            }
            
            if (!blocks.isEmpty()) {
                sb.append(" { ");
                for (int i = 0; i < blocks.size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(blocks.get(i).dql());
                }
                sb.append(" }");
            }
            
            return sb.toString();
        }
    }

    static Block expand(String typeName) {
        return new Expand(typeName, List.of(), List.of());
    }

    static Block expandAll() {
        return new Expand("_all_", List.of(), List.of());
    }

    static Block expandWithFilter(String typeName, Filter filter) {
        return new Expand(typeName, List.of(), List.of(Directive.filter(filter)));
    }

    static Block expandAllWithFilter(Filter filter) {
        return new Expand("_all_", List.of(), List.of(Directive.filter(filter)));
    }

    static Block groupBy(String predicate, String groupByPredicate) {
        return new GroupByBlock(GroupBy.groupBy(predicate, groupByPredicate));
    }
}