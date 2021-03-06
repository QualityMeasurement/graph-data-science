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
package org.neo4j.graphalgo.utils;

import org.neo4j.graphalgo.core.SecureTransaction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.api.KernelTransaction;

import static org.neo4j.graphalgo.utils.ExceptionUtil.throwIfUnchecked;

public abstract class StatementApi {

    public interface TxConsumer {
        void accept(KernelTransaction transaction) throws Exception;
    }

    public interface TxFunction<T> {
        T apply(KernelTransaction transaction) throws Exception;
    }

    protected final SecureTransaction tx;

    protected StatementApi(SecureTransaction tx) {
        this.tx = tx;
    }

    protected GraphDatabaseService api() {
        return tx.db();
    }

    protected final <T> T applyInTransaction(TxFunction<T> fun) {
        try {
            return tx.apply((tx, ktx) -> fun.apply(ktx));
        } catch (Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    protected final void acceptInTransaction(TxConsumer fun) {
        try {
            tx.accept((tx, ktx) -> fun.accept(ktx));
        } catch (Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    protected final int getOrCreatePropertyToken(String propertyKey) {
        return applyInTransaction(stmt -> stmt
            .tokenWrite()
            .propertyKeyGetOrCreateForName(propertyKey));
    }

    protected final int getOrCreateRelationshipToken(String relationshipType) {
        return applyInTransaction(stmt -> stmt
            .tokenWrite()
            .relationshipTypeGetOrCreateForName(relationshipType));
    }
}
