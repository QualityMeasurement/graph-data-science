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
package org.neo4j.graphalgo.core.utils.export.db;

import org.immutables.value.Value;
import org.neo4j.cli.Converters;
import org.neo4j.configuration.helpers.DatabaseNameValidator;
import org.neo4j.configuration.helpers.NormalizedDatabaseName;
import org.neo4j.graphalgo.annotation.Configuration;
import org.neo4j.graphalgo.annotation.ValueClass;
import org.neo4j.graphalgo.core.CypherMapWrapper;
import org.neo4j.graphalgo.core.utils.export.GraphStoreExporterBaseConfig;

import java.util.Optional;

@ValueClass
@Configuration
@SuppressWarnings("immutables:subtype")
public interface GraphStoreToDatabaseExporterConfig extends GraphStoreExporterBaseConfig {

    String DB_NAME_KEY = "dbName";

    @Configuration.Key(DB_NAME_KEY)
    String dbName();

    @Value.Default
    default boolean enableDebugLog() {
        return false;
    }

    @Value.Default
    @Configuration.Ignore
    default Optional<Long> pageCacheMemory() {
        return Optional.empty();
    }

    @Value.Check
    default void validate() {
        DatabaseNameValidator.validateExternalDatabaseName(new NormalizedDatabaseName(dbName()));
    }

    static GraphStoreToDatabaseExporterConfig of(String username, CypherMapWrapper config) {
        if (config.containsKey(DB_NAME_KEY)) {
            var dbName = new Converters.DatabaseNameConverter().convert(config.getString(DB_NAME_KEY).get()).name();
            config = config.withString(DB_NAME_KEY, dbName);
        }
        return new GraphStoreToDatabaseExporterConfigImpl(username, config);
    }
}
