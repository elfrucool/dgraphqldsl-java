package com.github.elfrucool.dgraphql.dsl;

import java.util.ArrayList;
import java.util.List;

/**
 * A query block that defines a root function and nested predicates for a DQL query.
 *
 * <p>QueryBlock is a sealed interface with two variants:</p>
 * <ul>
 *   <li>{@link Named} - A query block with a name (e.g., {@code me(func: eq(name, "Alice"))})</li>
 *   <li>{@link Anonymous} - An anonymous query block (e.g., {@code func: eq(name, "Alice")})</li>
 * </ul>
 *
 * <p>Use factory methods to create query blocks:</p>
 *
 * <pre>
 * QueryBlock.block("me", Func.eq("name", "Alice"))
 *     .withFirst(10)
 *     .withOrderasc("name")
 *     .withDirective(Directive.filter(...))
 * </pre>
 *
 * @see Query
 * @see Block
 * @see Func
 * @see Directive
 */
public sealed interface QueryBlock extends DqlElement 
    permits QueryBlock.Named, QueryBlock.Anonymous {

    String name();
    Func func();
    List<Block> blocks();
    List<Directive> directives();
    String orderasc();
    String orderdesc();
    Integer first();
    Integer offset();
    String after();

    default QueryBlock withBlocks(List<Block> blocks) {
        return switch (this) {
            case Named n -> n.withBlocks(blocks);
            case Anonymous a -> a.withBlocks(blocks);
        };
    }

    default QueryBlock withBlock(Block block) {
        List<Block> newBlocks = new ArrayList<>(blocks());
        newBlocks.add(block);
        return withBlocks(newBlocks);
    }

    default QueryBlock withDirectives(List<Directive> directives) {
        return switch (this) {
            case Named n -> n.withDirectives(directives);
            case Anonymous a -> a.withDirectives(directives);
        };
    }

    default QueryBlock withDirective(Directive directive) {
        List<Directive> newDirectives = new ArrayList<>(directives());
        newDirectives.add(directive);
        return withDirectives(newDirectives);
    }

    default QueryBlock withOrderasc(String predicate) {
        return switch (this) {
            case Named n -> n.withOrderasc(predicate);
            case Anonymous a -> a.withOrderasc(predicate);
        };
    }

    default QueryBlock withOrderdesc(String predicate) {
        return switch (this) {
            case Named n -> n.withOrderdesc(predicate);
            case Anonymous a -> a.withOrderdesc(predicate);
        };
    }

    default QueryBlock withFirst(int count) {
        return switch (this) {
            case Named n -> n.withFirst(count);
            case Anonymous a -> a.withFirst(count);
        };
    }

    default QueryBlock withOffset(int count) {
        return switch (this) {
            case Named n -> n.withOffset(count);
            case Anonymous a -> a.withOffset(count);
        };
    }

    default QueryBlock withAfter(String uid) {
        return switch (this) {
            case Named n -> n.withAfter(uid);
            case Anonymous a -> a.withAfter(uid);
        };
    }

    /**
     * A named query block with a name and root function.
     *
     * <p>Example: {@code me(func: eq(name, "Alice")) { name age }}</p>
     */
    record Named(
        String name,
        Func func,
        List<Block> blocks,
        List<Directive> directives,
        String orderasc,
        String orderdesc,
        Integer first,
        Integer offset,
        String after
    ) implements QueryBlock {

        /**
         * Creates a named query block with the given name and root function.
         *
         * @param name the block name (e.g., "me")
         * @param func the root function (e.g., Func.eq("name", "Alice"))
         * @return a new Named query block
         */
        public static Named of(String name, Func func) {
            return new Named(name, func, List.of(), List.of(), null, null, null, null, null);
        }

        public Named withBlocks(List<Block> blocks) {
            return new Named(this.name, this.func, blocks, this.directives,
                            this.orderasc, this.orderdesc, this.first, this.offset, this.after);
        }

        public Named withDirectives(List<Directive> directives) {
            return new Named(this.name, this.func, this.blocks, directives,
                            this.orderasc, this.orderdesc, this.first, this.offset, this.after);
        }

        public Named withOrderasc(String predicate) {
            return new Named(this.name, this.func, this.blocks, this.directives,
                            predicate, this.orderdesc, this.first, this.offset, this.after);
        }

        public Named withOrderdesc(String predicate) {
            return new Named(this.name, this.func, this.blocks, this.directives,
                            this.orderasc, predicate, this.first, this.offset, this.after);
        }

        public Named withFirst(int count) {
            return new Named(this.name, this.func, this.blocks, this.directives,
                            this.orderasc, this.orderdesc, count, this.offset, this.after);
        }

        public Named withOffset(int count) {
            return new Named(this.name, this.func, this.blocks, this.directives,
                            this.orderasc, this.orderdesc, this.first, count, this.after);
        }

        public Named withAfter(String uid) {
            return new Named(this.name, this.func, this.blocks, this.directives,
                            this.orderasc, this.orderdesc, this.first, this.offset, uid);
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();

            sb.append(name).append("(func: ").append(func.dql());

            if (orderasc != null) {
                sb.append(", orderasc: ").append(orderasc);
            }
            if (orderdesc != null) {
                sb.append(", orderdesc: ").append(orderdesc);
            }
            if (first != null) {
                sb.append(", first: ").append(first);
            }
            if (offset != null) {
                sb.append(", offset: ").append(offset);
            }
            if (after != null) {
                sb.append(", after: ").append(after);
            }

            sb.append(")");

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

    /**
     * An anonymous query block with only a root function (no name).
     *
     * <p>Example: {@code func: eq(name, "Alice") { name age }}</p>
     */
    record Anonymous(
        Func func,
        List<Block> blocks,
        List<Directive> directives,
        String orderasc,
        String orderdesc,
        Integer first,
        Integer offset,
        String after
    ) implements QueryBlock {

        @Override
        public String name() {
            return null;
        }

        /**
         * Creates an anonymous query block with the given root function.
         *
         * @param func the root function (e.g., Func.eq("name", "Alice"))
         * @return a new Anonymous query block
         */
        public static Anonymous of(Func func) {
            return new Anonymous(func, List.of(), List.of(), null, null, null, null, null);
        }

        public Anonymous withBlocks(List<Block> blocks) {
            return new Anonymous(this.func, blocks, this.directives,
                                this.orderasc, this.orderdesc, this.first, this.offset, this.after);
        }

        public Anonymous withDirectives(List<Directive> directives) {
            return new Anonymous(this.func, this.blocks, directives,
                                this.orderasc, this.orderdesc, this.first, this.offset, this.after);
        }

        public Anonymous withOrderasc(String predicate) {
            return new Anonymous(this.func, this.blocks, this.directives,
                                predicate, this.orderdesc, this.first, this.offset, this.after);
        }

        public Anonymous withOrderdesc(String predicate) {
            return new Anonymous(this.func, this.blocks, this.directives,
                                this.orderasc, predicate, this.first, this.offset, this.after);
        }

        public Anonymous withFirst(int count) {
            return new Anonymous(this.func, this.blocks, this.directives,
                                this.orderasc, this.orderdesc, count, this.offset, this.after);
        }

        public Anonymous withOffset(int count) {
            return new Anonymous(this.func, this.blocks, this.directives,
                                this.orderasc, this.orderdesc, this.first, count, this.after);
        }

        public Anonymous withAfter(String uid) {
            return new Anonymous(this.func, this.blocks, this.directives,
                                this.orderasc, this.orderdesc, this.first, this.offset, uid);
        }

        @Override
        public String dql() {
            StringBuilder sb = new StringBuilder();

            sb.append("(func: ").append(func.dql());

            if (orderasc != null) {
                sb.append(", orderasc: ").append(orderasc);
            }
            if (orderdesc != null) {
                sb.append(", orderdesc: ").append(orderdesc);
            }
            if (first != null) {
                sb.append(", first: ").append(first);
            }
            if (offset != null) {
                sb.append(", offset: ").append(offset);
            }
            if (after != null) {
                sb.append(", after: ").append(after);
            }

            sb.append(")");

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

    static QueryBlock block(String name, Func func) {
        return Named.of(name, func);
    }

    static QueryBlock block(Func func) {
        return Anonymous.of(func);
    }
}