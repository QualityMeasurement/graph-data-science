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
package org.neo4j.gds.ml.linkmodels.logisticregression;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.neo4j.gds.embeddings.graphsage.ddl4j.ComputationContext;
import org.neo4j.gds.embeddings.graphsage.ddl4j.functions.MatrixConstant;
import org.neo4j.gds.embeddings.graphsage.ddl4j.functions.Weights;
import org.neo4j.gds.embeddings.graphsage.ddl4j.tensor.Matrix;
import org.neo4j.gds.ml.LazyBatch;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.extension.GdlExtension;
import org.neo4j.graphalgo.extension.GdlGraph;
import org.neo4j.graphalgo.extension.Inject;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@GdlExtension
class LinkLogisticRegressionBaseTest {

    @GdlGraph
    private static final String DB_QUERY =
        "CREATE " +
        "  (n1:N {a: 2.0, b: 1.2})" +
        ", (n2:N {a: 1.3, b: 0.5})" +
        ", (n3:N {a: 0.0, b: 2.8})" +
        ", (n4:N {a: 1.0, b: 0.9})" +
        ", (n1)-[:T {label: 1.0}]->(n2)" +
        ", (n3)-[:T {label: 1.0}]->(n4)" +
        ", (n1)-[:T {label: 0.0}]->(n3)" +
        ", (n2)-[:T {label: 0.0}]->(n4)";

    @Inject
    private Graph graph;

    @Test
    void shouldComputeCorrectFeatures() {
        var nodePropertyKeys = List.of("a", "b");
        double[] weightsArray = new double[nodePropertyKeys.size() + 1];
        var weights = new Weights<>(new Matrix(weightsArray, 1, weightsArray.length));
        var base = new LinkLogisticRegressionBase(LinkLogisticRegressionData.builder()
            .linkFeatureCombiner(LinkFeatureCombiner.L2)
            .nodePropertyKeys(nodePropertyKeys)
            .numberOfFeatures(3)
            .weights(weights)
            .build());

        var allNodesBatch = new LazyBatch(0, (int) graph.nodeCount(), graph.nodeCount());
        MatrixConstant features = base.features(graph, allNodesBatch);
        var expectedFeatures = new double[]{
            0.49, 0.49, 1.0,
            4.00, 2.56, 1.0,
            0.09, 0.16, 1.0,
            1.00, 3.61, 1.0
        };
        var featuresData = features.apply(new ComputationContext()).data();
        assertThat(featuresData).containsExactly(expectedFeatures, Offset.offset(1e-6));
    }

}
