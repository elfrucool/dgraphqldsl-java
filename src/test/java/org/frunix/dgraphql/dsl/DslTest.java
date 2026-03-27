package org.frunix.dgraphql.dsl;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DslTest {

    @Test
    void testBasicQuery() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.predicate("age")
                    ))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: eq(name, \"Alice\")) { name age } }", result.query());
        assertTrue(result.variables().isEmpty());
    }

    @Test
    void testNestedBlocks() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.nested("friend")
                            .withBlocks(List.of(
                                Block.predicate("name"),
                                Block.predicate("age")
                            ))
                    ))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: eq(name, \"Alice\")) { name friend { name age } } }", result.query());
    }

    @Test
    void testWithFilter() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.nested("friend")
                            .withDirective(Directive.filter(Filter.has("birthdate")))
                            .withBlocks(List.of(Block.predicate("name")))
                    ))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: eq(name, \"Alice\")) { name friend @filter(has(birthdate)) { name } } }", result.query());
    }

    @Test
    void testQueryWithVariables() {
        Query query = Query.query("getPerson")
            .withParameters(List.of(Variable.queryVar("name", "string")))
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", Variable.param("name")))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("query getPerson($name: string) { me(func: eq(name, $name)) { name } }", result.query());
        assertTrue(result.variables().isEmpty());
    }

    @Test
    void testQueryWithDefaultVariable() {
        Query query = Query.query("getPerson")
            .withParameters(List.of(Variable.queryVar("name", "string", "Alice")))
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", Variable.param("name")))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("query getPerson($name: string = \"Alice\") { me(func: eq(name, $name)) { name } }", result.query());
        assertEquals("Alice", result.variables().get("$name"));
    }

    @Test
    void testAllofterms() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.allofterms("name", "alice bob"))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: allofterms(name, \"alice bob\")) { name } }", result.query());
    }

    @Test
    void testHas() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("name"))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: has(name)) { name } }", result.query());
    }

    @Test
    void testOrderAndPagination() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("name"))
                    .withOrderasc("name")
                    .withFirst(10)
                    .withOffset(20)
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: has(name), orderasc: name, first: 10, offset: 20) { name } }", result.query());
    }

    @Test
    void testBooleanFilters() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.allofterms("name", "alice"))
                    .withDirective(Directive.filter(
                        Filter.and(Filter.has("friend"), Filter.ge("age", 18))
                    ))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: allofterms(name, \"alice\")) @filter((has(friend) AND ge(age, 18))) { name } }", result.query());
    }

    @Test
    void testFacets() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.nested("friend")
                            .withDirective(Directive.facets("since"))
                            .withBlocks(List.of(Block.predicate("name")))
                    ))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: eq(name, \"Alice\")) { friend @facets(since) { name } } }", result.query());
    }

    @Test
    void testAlias() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.predicate("name", "fullName"),
                        Block.predicate("age", "years")
                    ))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: eq(name, \"Alice\")) { fullName: name years: age } }", result.query());
    }

    @Test
    void testUid() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uid("0x1", "0x2", "0x3"))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: uid(0x1, 0x2, 0x3)) { name } }", result.query());
    }

    @Test
    void testTypeFunction() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.type("Person"))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: type(Person)) { name } }", result.query());
    }

    @Test
    void testCascadeDirective() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("name"))
                    .withDirective(Directive.cascade())
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: has(name)) @cascade { name } }", result.query());
    }

    @Test
    void testImmutability() {
        Query original = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
            ));

        Query modified = original.withBlock(
            QueryBlock.block("you", Func.eq("name", "Bob"))
        );

        assertEquals(1, original.blocks().size());
        assertEquals(2, modified.blocks().size());
    }

    @Test
    void testRegexpCaseInsensitive() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.regexp("name", "alice", true))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: regexp(name, \"alice\", \"i\")) { name } }", result.query());
    }

    @Test
    void testAlloftext() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.alloftext("name", "alice bob"))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: alloftext(name, \"alice bob\")) { name } }", result.query());
    }

    @Test
    void testUidIn() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uidIn("friend", "0x1", "0x2", "0x3"))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: uid_in(friend, 0x1, 0x2, 0x3)) { name } }", result.query());
    }

    @Test
    void testGeoNear() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.near("location", GeoValue.circle(40.7128, -74.0060, 10.0)))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: near(location, \"{\\\"type\\\":\\\"Circle\\\",\\\"coordinates\\\":[-74.006,40.7128],\\\"radius\\\":10.0}\")) { name } }", result.query());
    }

    @Test
    void testCount() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.predicate("friend", "numFriends"),
                        Block.nested("friend")
                            .withBlocks(List.of(
                                Block.predicate("name", "count")
                            ))
                    ))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: eq(name, \"Alice\")) { numFriends: friend friend { count: name } } }", result.query());
    }

    @Test
    void testQueryWithBindings() {
        Query query = Query.query("getPerson")
            .withParameters(List.of(Variable.queryVar("name", "string")))
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", Variable.param("name")))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql(Map.of("name", "Bob"));
        
        assertEquals("query getPerson($name: string) { me(func: eq(name, $name)) { name } }", result.query());
        assertEquals("Bob", result.variables().get("$name"));
    }

    @Test
    void testBindingsOverrideDefault() {
        Query query = Query.query("getPerson")
            .withParameters(List.of(Variable.queryVar("name", "string", "Default")))
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", Variable.param("name")))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql(Map.of("name", "Override"));
        
        assertEquals("query getPerson($name: string = \"Default\") { me(func: eq(name, $name)) { name } }", result.query());
        assertEquals("Override", result.variables().get("$name"));
    }

    @Test
    void testVarBlockWithQueryVar() {
        Query query = Query.query()
            .withVarBlock(
                VarBlock.var(Func.eq("name", "Alice"))
                    .withAssignment(VarAssignment.queryVar("friends", Func.count("friend")))
            )
            .withBlocks(List.of(
                QueryBlock.block("me", Func.uid("friends"))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ var(func: eq(name, \"Alice\")) { friends as count(friend) } me(func: uid(friends)) { name } }", result.query());
    }

    @Test
    void testVarBlockWithValueVar() {
        Query query = Query.query()
            .withVarBlock(
                VarBlock.var(Func.eq("name", "Alice"))
                    .withAssignment(VarAssignment.valueVar("score", Func.math("friend_count * 10")))
            )
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("name"))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.predicate(Func.val("score"), "computedScore")
                    ))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ var(func: eq(name, \"Alice\")) { score as math(friend_count * 10) } me(func: has(name)) { name computedScore: val(score) } }", result.query());
    }

    @Test
    void testMathExpression() {
        Query query = Query.query()
            .withVarBlock(
                VarBlock.var(Func.eq("name", "Alice"))
                    .withAssignment(VarAssignment.valueVar("score", 
                        Func.math("friend_count + age")))
            )
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("name"))
                    .withBlocks(List.of(Block.predicate(Func.val("score"), "total")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ var(func: eq(name, \"Alice\")) { score as math(friend_count + age) } me(func: has(name)) { total: val(score) } }", result.query());
    }

    @Test
    void testRecurseQuery() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("friend"))
                    .withDirective(Directive.recurse(5))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.nested("friend")
                    ))
            ));

        DqlResult result = query.dql();
        
        assertTrue(result.query().contains("@recurse(depth: 5)"));
    }

    @Test
    void testFragment() {
        Query query = Query.query()
            .withFragment(
                Fragment.fragment("PersonDetails")
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.predicate("age")
                    ))
            )
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(Block.predicate("... PersonDetails")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: eq(name, \"Alice\")) { ... PersonDetails } } fragment PersonDetails { name age }", result.query());
    }

    @Test
    void testExpand() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.predicate(Func.expand("friend")),
                        Block.predicate(Func.expandAll()),
                        Block.predicate(Func.expandReverse())
                    ))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: eq(name, \"Alice\")) { expand(friend) expand(_all_) expand(_reverse_) } }", result.query());
    }

    @Test
    void testCondMathExpression() {
        Query query = Query.query()
            .withVarBlock(
                VarBlock.var(Func.eq("name", "Alice"))
                    .withAssignment(VarAssignment.valueVar("score",
                        Func.math(MathExpr.cond(MathExpr.gt("age", 18), 10, 0).dql())))
            )
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("name"))
                    .withBlocks(List.of(Block.predicate(Func.val("score"), "adultScore")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ var(func: eq(name, \"Alice\")) { score as math(cond((age > 18), 10, 0)) } me(func: has(name)) { adultScore: val(score) } }", result.query());
    }

    @Test
    void testNeq() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("name"))
                    .withDirective(Directive.filter(Filter.neq("state", "completed")))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: has(name)) @filter(neq(state, \"completed\")) { name } }", result.query());
    }

    @Test
    void testReverseEdge() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("top_level"))
                    .withBlocks(List.of(
                        Block.nested("~top_level")
                            .withBlocks(List.of(Block.predicate("name")))
                    ))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: has(top_level)) { ~top_level { name } } }", result.query());
    }

    @Test
    void testReverseEdgeExplicit() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("top_level"))
                    .withBlocks(List.of(
                        Block.reverse("top_level")
                            .withBlocks(List.of(Block.predicate("name")))
                    ))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: has(top_level)) { ~top_level { name } } }", result.query());
    }

    @Test
    void testExactUserQuery() {
        Query query = Query.query()
            .withVarBlock(
                VarBlock.var(Func.has("top_level"))
                    .withBlock(
                        Block.reverse("top_level")
                            .withDirective(Directive.filter(Filter.neq("state", "completed")))
                            .withBlock(Block.var("blocked_top", "uid"))
                    )
            )
            .withVarBlock(
                VarBlock.var(Func.has("premium"))
                    .withBlock(
                        Block.reverse("premium")
                            .withDirective(Directive.filter(Filter.neq("state", "completed")))
                            .withBlock(Block.var("blocked_prem", "uid"))
                    )
            )
            .withBlocks(List.of(
                QueryBlock.block("candidates", Func.type("a_label"))
                    .withDirective(Directive.filter(Filter.and(
                        Filter.eq("is_draft", false),
                        Filter.eq("compilation_id", "d8002dbc-fb9d-4acf-9b79-bb1d4237323f"),
                        Filter.eq("target", "marker"),
                        Filter.or(
                            Filter.eq("node_state", "built"),
                            Filter.eq("node_state", "started")
                        ),
                        Filter.not(Filter.func(Func.uid("blocked_top"))),
                        Filter.not(Filter.func(Func.uid("blocked_prem")))
                    )))
                    .withBlocks(List.of(
                        Block.predicate("uid"),
                        Block.predicate(Func.expandAll())
                    ))
            ));

        DqlResult result = query.dql();
        
        assertTrue(result.query().contains("~top_level"));
        assertTrue(result.query().contains("~premium"));
        assertTrue(result.query().contains("blocked_top as uid"));
        assertTrue(result.query().contains("blocked_prem as uid"));
        assertTrue(result.query().contains("neq(state, \"completed\")"));
        assertTrue(result.query().contains("type(a_label)"));
        assertTrue(result.query().contains("expand(_all_)"));
    }

    @Test
    void testSetMutation() {
        Mutation mutation = Mutation.set(
            SetTriple.subject("0x123").predicate("name").value("Alice"),
            SetTriple.subject("0x123").predicate("age").value(30)
        );

        assertEquals("{ set { <0x123> name \"Alice\" . <0x123> age 30 . } }", mutation.dql());
    }

    @Test
    void testSetMutationWithBlankNode() {
        Mutation mutation = Mutation.set(
            SetTriple.subject("_:newNode").predicate("name").value("Bob")
        );

        assertEquals("{ set { _:newNode name \"Bob\" . } }", mutation.dql());
    }

    @Test
    void testSetMutationWithUidReference() {
        Mutation mutation = Mutation.set(
            SetTriple.subject("0x123").predicate("friend").value("0x456")
        );

        assertEquals("{ set { <0x123> friend <0x456> . } }", mutation.dql());
    }

    @Test
    void testDeleteMutation() {
        Mutation mutation = Mutation.delete(
            SetTriple.subject("0x123").predicate("name").value(null)
        );

        assertEquals("{ delete { <0x123> name _: . } }", mutation.dql());
    }

    @Test
    void testDeleteAllPredicate() {
        Mutation mutation = Mutation.delete(
            SetTriple.subject("0x123").predicate("name").value("*")
        );

        assertEquals("{ delete { <0x123> name * . } }", mutation.dql());
    }

    @Test
    void testUpdateMutation() {
        Mutation mutation = Mutation.update(
            Mutation.Set.of(SetTriple.subject("0x123").predicate("name").value("Alice")),
            Mutation.Delete.of(SetTriple.subject("0x123").predicate("name").value(null))
        );

        assertTrue(mutation.dql().contains("set {"));
        assertTrue(mutation.dql().contains("delete {"));
    }

    @Test
    void testConditionalMutation() {
        Mutation mutation = Mutation.ifCondition(
            "eq(name, \"Bob\")",
            Mutation.Set.of(SetTriple.subject("0x123").predicate("name").value("Alice")),
            null
        );

        String dql = mutation.dql();
        assertTrue(dql.contains("@if(eq(name, \"Bob\"))"));
        assertTrue(dql.contains("set {"));
    }

    @Test
    void testGroupByBasic() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("age"))
                    .withBlocks(List.of(
                        Block.predicate("age"),
                        Block.nested("friend")
                            .withDirective(Directive.groupby("age"))
                            .withBlock(Block.predicate("count(uid)"))
                    ))
            ));

        DqlResult result = query.dql();
        
        assertTrue(result.query().contains("@groupby(age)"));
    }

    @Test
    void testGroupByMultipleAggregations() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("age"))
                    .withBlocks(List.of(
                        Block.predicate("age"),
                        Block.nested("friend")
                            .withDirective(Directive.groupby("age"))
                            .withBlocks(List.of(
                                Block.predicate("count(uid)"),
                                Block.predicate("min(age)"),
                                Block.predicate("max(age)")
                            ))
                    ))
            ));

        DqlResult result = query.dql();
        
        assertTrue(result.query().contains("@groupby(age)"));
    }

    @Test
    void testIgnorereflexDirective() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("friend"))
                    .withDirective(Directive.recurse(5))
                    .withDirective(Directive.ignorereflex())
                    .withBlocks(List.of(
                        Block.predicate("friend")
                    ))
            ));

        DqlResult result = query.dql();
        
        assertTrue(result.query().contains("@recurse(depth: 5)"));
        assertTrue(result.query().contains("@ignorereflex"));
    }

    @Test
    void testAlterTypeDefinition() {
        Alter alter = Alter.type("Person", "name", "age");
        
        assertEquals("type Person {\n  name\n  age\n}", alter.dql());
    }

    @Test
    void testAlterPredicateSchema() {
        Alter alter = Alter.predicate("name", "string")
            .withIndex("exact");
        
        assertEquals("name: string @index(exact) .", alter.dql());
    }

    @Test
    void testAlterPredicateWithMultipleIndexes() {
        Alter alter = Alter.predicate("email", "string")
            .withIndexes(List.of("exact", "hash"));
        
        assertEquals("email: string @index(exact, hash) .", alter.dql());
    }

    @Test
    void testAlterDropAll() {
        Alter alter = Alter.dropAll();
        
        assertEquals("drop all", alter.dql());
    }

    @Test
    void testAlterDropType() {
        Alter alter = Alter.dropType("Person");
        
        assertEquals("drop type Person", alter.dql());
    }

    @Test
    void testAlterDropPredicate() {
        Alter alter = Alter.dropPredicate("name");
        
        assertEquals("drop name", alter.dql());
    }

    @Test
    void testAlterMultipleOperations() {
        Alter alter = Alter.all(List.of(
            Alter.type("Person", "name", "age"),
            Alter.predicate("name", "string").withIndex("exact")
        ));
        
        String dql = alter.dql();
        assertTrue(dql.contains("type Person"));
        assertTrue(dql.contains("name: string @index(exact)"));
    }

    @Test
    void testMultipleQueryBlocks() {
        Query query = Query.query()
            .withBlock(QueryBlock.block("getUser1", Func.eq("email", "a@b.com"))
                .withBlocks(List.of(Block.predicate("name"))))
            .withBlock(QueryBlock.block("getUser2", Func.eq("email", "c@d.com"))
                .withBlocks(List.of(Block.predicate("name"))));

        DqlResult result = query.dql();
        
        assertTrue(result.query().contains("getUser1(func: eq(email, \"a@b.com\")) { name }"));
        assertTrue(result.query().contains("getUser2(func: eq(email, \"c@d.com\")) { name }"));
    }

    @Test
    void testJsonMutationSet() {
        JsonMutation mutation = JsonMutation.set(
            Map.of("uid", "_:user", "name", "Alice", "age", 30)
        );
        
        String dql = mutation.dql();
        assertTrue(dql.contains("\"set\""));
        assertTrue(dql.contains("\"uid\": \"_:user\""));
        assertTrue(dql.contains("\"name\": \"Alice\""));
        assertTrue(dql.contains("\"age\": 30"));
    }

    @Test
    void testJsonMutationDelete() {
        JsonMutation mutation = JsonMutation.delete(
            Map.of("uid", "0x123")
        );
        
        String dql = mutation.dql();
        assertTrue(dql.contains("\"delete\""));
        assertTrue(dql.contains("\"uid\": \"0x123\""));
    }

    @Test
    void testJsonMutationWithNested() {
        JsonMutation mutation = JsonMutation.set(
            Map.of("uid", "_:user", "name", "Alice", "friend", Map.of("uid", "0x456"))
        );
        
        String dql = mutation.dql();
        assertTrue(dql.contains("\"friend\": {"));
        assertTrue(dql.contains("\"uid\": \"0x456\""));
    }

    @Test
    void testJsonMutationMultipleObjects() {
        JsonMutation mutation = JsonMutation.set(List.of(
            Map.of("name", "Alice", "age", 30),
            Map.of("name", "Bob", "age", 25)
        ));
        
        String dql = mutation.dql();
        assertTrue(dql.contains("\"name\": \"Alice\""));
        assertTrue(dql.contains("\"name\": \"Bob\""));
    }

    @Test
    void testFacetFilterWithCondition() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.nested("friend")
                            .withDirective(Directive.facets(Filter.eq("since", "2024")))
                            .withBlocks(List.of(Block.predicate("name")))
                    ))
            ));

        DqlResult result = query.dql();
        
        assertEquals("{ me(func: eq(name, \"Alice\")) { friend @facets(eq(since, \"2024\")) { name } } }", result.query());
    }

    @Test
    void testFacetFilterWithMultipleConditions() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", "Alice"))
                    .withBlocks(List.of(
                        Block.nested("friend")
                            .withDirective(Directive.facets(Filter.and(
                                Filter.eq("since", "2024"),
                                Filter.gt("rating", 3)
                            )))
                            .withBlocks(List.of(Block.predicate("name")))
                    ))
            ));

        DqlResult result = query.dql();
        
        assertTrue(result.query().contains("@facets((eq(since, \"2024\") AND gt(rating, 3)))"));
    }

    @Test
    void testRecurseDirective() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("friend"))
                    .withDirective(Directive.recurse(3))
                    .withBlocks(List.of(
                        Block.predicate("name"),
                        Block.predicate("friend")
                    ))
            ));

        DqlResult result = query.dql();
        assertTrue(result.query().contains("@recurse(depth: 3)"));
    }

    @Test
    void testGroupByDirective() {
        Query query = Query.query()
            .withBlocks(List.of(
                QueryBlock.block("me", Func.has("friend"))
                    .withBlocks(List.of(
                        Block.predicate("age"),
                        Block.nested("friend")
                            .withDirective(Directive.groupby("age"))
                            .withBlocks(List.of(Block.predicate("count(uid)")))
                    ))
            ));

        DqlResult result = query.dql();
        assertTrue(result.query().contains("@groupby(age)"));
    }

    @Test
    void testMutationToJsonList() {
        Mutation mutation = Mutation.set(List.of(
            SetTriple.subject("_:alice").predicate("name").value("Alice"),
            SetTriple.subject("_:alice").predicate("age").value(30)
        ));

        List<Map<String, Object>> jsonList = mutation.toJsonList();
        assertFalse(jsonList.isEmpty());
        assertTrue(jsonList.get(0).containsKey("uid"));
        assertEquals("Alice", jsonList.get(0).get("name"));
        assertEquals(30, jsonList.get(0).get("age"));
    }

    @Test
    void testQueryVariableWithDollarPrefix() {
        Query query = Query.query("getPerson")
            .withParameters(List.of(Variable.queryVar("name", "string")))
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", Variable.param("name")))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql(Map.of("$name", "Alice"));

        assertEquals("query getPerson($name: string) { me(func: eq(name, $name)) { name } }", result.query());
        assertEquals(Map.of("$name", "Alice"), result.variables());
    }

    @Test
    void testQueryVariableWithoutDollarPrefix() {
        Query query = Query.query("getPerson")
            .withParameters(List.of(Variable.queryVar("name", "string")))
            .withBlocks(List.of(
                QueryBlock.block("me", Func.eq("name", Variable.param("name")))
                    .withBlocks(List.of(Block.predicate("name")))
            ));

        DqlResult result = query.dql(Map.of("name", "Alice"));

        assertEquals("query getPerson($name: string) { me(func: eq(name, $name)) { name } }", result.query());
        assertEquals(Map.of("$name", "Alice"), result.variables());
    }
}
