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
package org.neo4j.gds.embeddings.graphsage.ddl4j.functions;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.neo4j.gds.embeddings.graphsage.ddl4j.FiniteDifferenceTest;
import org.neo4j.gds.embeddings.graphsage.ddl4j.GraphSageBaseTest;
import org.neo4j.gds.embeddings.graphsage.ddl4j.Variable;
import org.neo4j.gds.embeddings.graphsage.ddl4j.tensor.Matrix;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NormalizeRowsTest extends GraphSageBaseTest implements FiniteDifferenceTest {
    @Override
    public double epsilon() {
        return 1e-6;
    }

    @Test
    void testGradient() {
        double[] data = new double[] {
            3, 4,
            1, 0,
            0, 1
        };

        Weights<Matrix> w = new Weights<>(new Matrix(data, 3, 2));
        Variable<Matrix> normalizeRows = new NormalizeRows(w);

        finiteDifferenceShouldApproximateGradient(w, new ElementSum(List.of(normalizeRows)));
    }

    @Test
    void testApply() {
        double[] data = new double[]{
            3, 4,
            1, 0,
            0, 1
        };
        double[] expectedData = new double[]{
            3.0/5, 4.0/5,
            1, 0,
            0, 1
        };
        Weights<Matrix> w = new Weights<>(new Matrix(data, 3, 2));
        Variable<Matrix> normalizeRows = new NormalizeRows(w);
        assertThat(ctx.forward(normalizeRows).data()).contains(expectedData, Offset.offset(1e-8));
    }


}
