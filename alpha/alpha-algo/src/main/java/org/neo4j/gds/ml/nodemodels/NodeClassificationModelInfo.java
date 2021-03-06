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
package org.neo4j.gds.ml.nodemodels;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.neo4j.gds.ml.nodemodels.metrics.Metric;
import org.neo4j.graphalgo.annotation.ValueClass;
import org.neo4j.graphalgo.core.model.Model;

import java.util.List;
import java.util.Map;

import static org.neo4j.gds.ml.util.ObjectMapperSingleton.OBJECT_MAPPER;

@ValueClass
@JsonSerialize
@JsonDeserialize
public interface NodeClassificationModelInfo extends Model.Mappable {

    /**
     * The distinct values of the target property which represent the
     * allowed classes that the model can predict.
     * @return
     */
    List<Long> classes();

    /**
     * The parameters that yielded the best fold-averaged validation score
     * for the selection metric.
     * @return
     */
    Map<String, Object> bestParameters();
    Map<Metric, MetricData> metrics();

    @Override
    default Map<String, Object> toMap() {
        try {
            String jsonString = OBJECT_MAPPER.writeValueAsString(this);
            return OBJECT_MAPPER.readValue(jsonString, Map.class);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to serialize/deserialize NodeClassificationModelInfo.", e);
        }
    }

    static NodeClassificationModelInfo of(
        List<Long> classes,
        Map<String, Object> bestParameters,
        Map<Metric, MetricData> metrics
    ) {
        return ImmutableNodeClassificationModelInfo.of(classes, bestParameters, metrics);
    }
}

