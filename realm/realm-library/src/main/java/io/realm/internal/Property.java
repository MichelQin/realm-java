/*
 * Copyright 2016 Realm Inc.
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


import io.realm.RealmFieldType;


/**
 * Class for handling properties/fields.
 */

public class Property implements NativeObject {
    public static final boolean PRIMARY_KEY = true;
    public static final boolean REQUIRED = true;
    public static final boolean INDEXED = true;

    private long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    Property(String name, RealmFieldType type, boolean isPrimary, boolean isIndexed, boolean isRequired) {
        this(nativeCreatePersistedProperty(name, type.getNativeValue(), isPrimary, isIndexed, !isRequired));
    }

    Property(String name, RealmFieldType type, String linkedClassName) {
        this(nativeCreatePersistedLinkProperty(name, type.getNativeValue(), linkedClassName));
    }

    Property(String name, String sourceClassName, String sourceFieldName) {
        this(nativeCreateComputedLinkProperty(name, sourceClassName, sourceFieldName));
    }

    Property(long nativePtr) {
        this.nativePtr = nativePtr;
        NativeContext.dummyContext.addReference(this);
    }

    public RealmFieldType getType() {
        return RealmFieldType.fromNativeValue(nativeGetType(nativePtr));
    }

    public String getLinkedObjectName() {
        return nativeGetLinkedObjectName(nativePtr);
    }

    public long getColumnIndex() {
        return nativeGetColumnIndex(nativePtr);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    private static native long nativeGetFinalizerPtr();

    private static native long nativeCreatePersistedProperty(
            String name, int type, boolean isPrimary, boolean isIndexed, boolean isNullable);

    private static native long nativeCreatePersistedLinkProperty(String name, int type, String linkedToName);

    private static native long nativeCreateComputedLinkProperty(
            String name, String sourceClassName, String sourceFieldName);

    private static native int nativeGetType(long nativePtr);

    private static native long nativeGetColumnIndex(long nativePtr);

    // Return null if the property is not OBJECT, LIST or LINKING_OBJECT type.
    private static native String nativeGetLinkedObjectName(long nativePtr);
}
