/*
 * Copyright (c) 2017-2020 "Neo4j,"
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
package org.neo4j.gds.splitting;

import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.extension.GdlExtension;
import org.neo4j.graphalgo.extension.GdlGraph;
import org.neo4j.graphalgo.extension.Inject;
import org.neo4j.graphalgo.extension.TestGraph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.neo4j.gds.splitting.EdgeSplitter.NEGATIVE;
import static org.neo4j.gds.splitting.EdgeSplitter.POSITIVE;

@GdlExtension
class EdgeSplitterTest {

    @GdlGraph
    static String gdl = "(:A)-[:T]->(:A)-[:T]->(:A)-[:T]->(:A)-[:T]->(:A)-[:T]->(:A)";

    @Inject
    TestGraph graph;

    @Test
    void split() {
        var splitter = new EdgeSplitter(12L);

        // select 20%, which is 1 rel in this graph
        var result = splitter.split(graph, .2);

        var remainingRels = result.remainingRels();
        // 1 positive selected reduces remaining
        assertEquals(4L, remainingRels.topology().elementCount());
        assertEquals(Orientation.NATURAL, remainingRels.topology().orientation());
        assertFalse(remainingRels.topology().isMultiGraph());
        assertThat(remainingRels.properties()).isEmpty();

        var selectedRels = result.selectedRels();
        assertThat(selectedRels.topology()).satisfies(topology -> {
            // it selected 4,1 (neg) and 4,5 (pos) relationships
            assertEquals(2L, topology.elementCount());
            var cursor = topology.list().decompressingCursor(topology.offsets().get(4L));
            assertEquals(1L, cursor.nextVLong());
            assertEquals(5L, cursor.nextVLong());
            assertEquals(Orientation.NATURAL, topology.orientation());
            assertFalse(topology.isMultiGraph());
        });
        assertThat(selectedRels.properties()).isPresent().get().satisfies(p -> {
            assertEquals(2L, p.elementCount());
            var cursor = p.list().cursor(p.offsets().get(4L));
            // 4,1 is negative
            assertEquals(NEGATIVE, Double.longBitsToDouble(cursor.nextLong()));
            // 4,5 is positive
            assertEquals(POSITIVE, Double.longBitsToDouble(cursor.nextLong()));
        });
    }

    @Test
    void negativeEdgeSampling() {
        var splitter = new EdgeSplitter(42L);

        var sum = 0;
        for (int i = 0; i < 100; i++) {
            var prev = splitter.samplesPerNode(i, 1000 - sum, 100 - i);
            sum += prev;
        }

        assertEquals(1000, sum);
    }

    @Test
    void samplesWithinBounds() {
        var splitter = new EdgeSplitter(42L);

        assertEquals(1, splitter.samplesPerNode(1, 100, 10));
        assertEquals(1, splitter.samplesPerNode(100, 1, 1));
    }

}