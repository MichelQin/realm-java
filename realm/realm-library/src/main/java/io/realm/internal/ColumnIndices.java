/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.internal;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import io.realm.RealmModel;


/**
 * Utility class used to cache the mapping between object field names and their column indices.
 * <p>
 * This class can be mutated, after construction, in two ways:
 * <ul>
 * <li>the {@code copyFrom} method</li>
 * <li>mutating one of the ColumnInfo object to which this instance holds a reference</li>
 * </ul>
 * Immutable instances of this class protect against the first possibility by throwing on calls
 * to {@code copyFrom}.  {@see ColumnInfo} for its mutability contract.
 *
 * There are two, redundant, lookup methods, for schema members: by Class and by String.
 * Query lookups must be done by the name of the class and use the String-keyed lookup table.
 * Although it would be possible to use the same table to look up ColumnInfo by Class, the
 * class lookup is very fast and on a hot path, so we maintain the redundant table.
 */
public final class ColumnIndices {
    // Class name to ColumnInfo map
    private final Map<Class<? extends RealmModel>, ColumnInfo> classToColumnInfoMap =
            new HashMap<Class<? extends RealmModel>, ColumnInfo>();

    private final RealmProxyMediator mediator;
    private final OsSchemaInfo osSchemaInfo;


    /**
     * Create a mutable ColumnIndices initialized with the ColumnInfo objects in the passed map.
     *
     * @param mediator
     * @param osSchemaInfo
     */
    public ColumnIndices(RealmProxyMediator mediator, OsSchemaInfo osSchemaInfo) {
        this.mediator = mediator;
        this.osSchemaInfo = osSchemaInfo;
    }

    /**
     * Returns the {@link ColumnInfo} for the passed class or ({@code null} if there is no such class).
     *
     * @param clazz the class for which to get the ColumnInfo.
     * @return the corresponding {@link ColumnInfo} object, or {@code null} if not found.
     */
    @Nullable
    public ColumnInfo getColumnInfo(Class<? extends RealmModel> clazz) {
        ColumnInfo columnInfo = classToColumnInfoMap.get(clazz);
        if (columnInfo == null) {
            columnInfo = mediator.createColumnInfo(clazz, osSchemaInfo);
        }
        return columnInfo;
    }

    /**
     * Returns the {@link ColumnInfo} for the passed class ({@code null} if there is no such class).
     *
     * @param simpleClassName the simple name of the class for which to get the ColumnInfo.
     * @return the corresponding {@link ColumnInfo} object, or {@code null} if not found.
     */
    public ColumnInfo getColumnInfo(String simpleClassName) {
        Set<Class<? extends RealmModel>> modelClasses = mediator.getModelClasses();
        for (Class<? extends RealmModel> modelClass : modelClasses) {
            if (modelClass.getSimpleName().equals(simpleClassName)) {
                return getColumnInfo(modelClass);
            }
        }
        return null;
    }

    public void refresh() {
        for (Map.Entry<Class<? extends RealmModel>, ColumnInfo> entry : classToColumnInfoMap.entrySet()) {
            ColumnInfo newColumnInfo = mediator.createColumnInfo(entry.getKey(), osSchemaInfo);
            entry.getValue().copyFrom(newColumnInfo);
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("ColumnIndices[");
        boolean commaNeeded = false;
        for (Map.Entry<Class<? extends RealmModel>, ColumnInfo> entry : classToColumnInfoMap.entrySet()) {
            if (commaNeeded) { buf.append(","); }
            buf.append(entry.getKey().getSimpleName()).append("->").append(entry.getValue());
            commaNeeded = true;
        }
        return buf.append("]").toString();
    }
}
