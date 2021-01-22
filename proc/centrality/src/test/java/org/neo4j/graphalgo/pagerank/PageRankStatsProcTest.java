/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.pagerank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.graphalgo.AlgoBaseProc;
import org.neo4j.graphalgo.GdsCypher;
import org.neo4j.graphalgo.catalog.GraphCreateProc;
import org.neo4j.graphalgo.core.CypherMapWrapper;
import org.neo4j.graphalgo.extension.Neo4jGraph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PageRankStatsProcTest extends PageRankProcTest<PageRankStatsConfig> {

    @Neo4jGraph
    private static final String DB_CYPHER = "CREATE " +
           "  (a:Label1 {name: 'a'})" +
           ", (b:Label1 {name: 'b'})" +
           ", (a)-[:TYPE1]->(b)";

    @BeforeEach
    void setupGraph() throws Exception {
        registerProcedures(GraphCreateProc.class, PageRankStatsProc.class);
        runQuery("CALL gds.graph.create('graphLabel1', '*', '*')");
    }

    @Override
    public Class<? extends AlgoBaseProc<PageRank, PageRank, PageRankStatsConfig>> getProcedureClazz() {
        return PageRankStatsProc.class;
    }

    @Override
    public PageRankStatsConfig createConfig(CypherMapWrapper mapWrapper) {
        return PageRankStatsConfig.of(getUsername(), Optional.empty(), Optional.empty(), mapWrapper);
    }

    @Test
    void testStatsYields() {
        String query = GdsCypher
            .call()
            .withAnyLabel()
            .withAnyRelationshipType()
            .algo("pageRank")
            .statsMode()
            .yields(
                "createMillis",
                "computeMillis",
                "postProcessingMillis",
                "didConverge",
                "ranIterations",
                "centralityDistribution",
                "configuration"
            );

        runQueryWithRowConsumer(
            query,
            row -> {
                assertThat(-1L, lessThan(row.getNumber("createMillis").longValue()));
                assertThat(-1L, lessThan(row.getNumber("computeMillis").longValue()));
                assertThat(-1L, lessThan(row.getNumber("postProcessingMillis").longValue()));

                assertEquals(true, row.get("didConverge"));
                assertEquals(2L, row.get("ranIterations"));

                assertNotNull(row.get("centralityDistribution"));
            }
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("org.neo4j.graphalgo.pagerank.PageRankProcTest#graphVariations")
    void statsShouldNotHaveWriteProperties(GdsCypher.ModeBuildStage queryBuilder, String testCaseName) {
        String query = queryBuilder
            .statsMode()
            .yields();

        List<String> forbiddenResultColumns = Arrays.asList(
            "writeMillis",
            "nodePropertiesWritten",
            "relationshipPropertiesWritten"
        );
        List<String> forbiddenConfigKeys = Collections.singletonList("writeProperty");
        runQueryWithResultConsumer(query, result -> {
            List<String> badResultColumns = result.columns()
                .stream()
                .filter(forbiddenResultColumns::contains)
                .collect(Collectors.toList());
            assertEquals(Collections.emptyList(), badResultColumns);
            assertTrue(result.hasNext(), "Result must not be empty.");
            Map<String, Object> config = (Map<String, Object>) result.next().get("configuration");
            List<String> badConfigKeys = config.keySet()
                .stream()
                .filter(forbiddenConfigKeys::contains)
                .collect(Collectors.toList());
            assertEquals(Collections.emptyList(), badConfigKeys);
        });
    }
}
