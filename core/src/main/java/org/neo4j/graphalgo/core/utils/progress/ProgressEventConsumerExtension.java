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
package org.neo4j.graphalgo.core.utils.progress;

import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.kernel.api.procedure.GlobalProcedures;
import org.neo4j.kernel.extension.ExtensionFactory;
import org.neo4j.kernel.extension.ExtensionType;
import org.neo4j.kernel.extension.context.ExtensionContext;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.logging.internal.LogService;
import org.neo4j.monitoring.Monitors;
import org.neo4j.scheduler.JobScheduler;

@ServiceProvider
public final class ProgressEventConsumerExtension extends ExtensionFactory<ProgressEventConsumerExtension.Dependencies> {
    public ProgressEventConsumerExtension() {
        super(ExtensionType.DATABASE, "gds.progress.logger");
    }

    @Override
    public Lifecycle newInstance(ExtensionContext context, ProgressEventConsumerExtension.Dependencies dependencies) {
        var progressEventConsumerComponent = new ProgressEventConsumerComponent(
            dependencies.logService().getInternalLog(ProgressEventConsumerComponent.class),
            dependencies.jobScheduler(),
            dependencies.globalMonitors()
        );

        var registry = dependencies.globalProceduresRegistry();
        registry.registerComponent(ProgressEventTracker.class, progressEventConsumerComponent, true);
        registry.registerComponent(
            ProgressEventConsumer.class,
            ctx -> progressEventConsumerComponent.progressEventConsumer(),
            true
        );
        return progressEventConsumerComponent;
    }

    interface Dependencies {
        LogService logService();

        JobScheduler jobScheduler();

        Monitors globalMonitors();

        GlobalProcedures globalProceduresRegistry();
    }
}