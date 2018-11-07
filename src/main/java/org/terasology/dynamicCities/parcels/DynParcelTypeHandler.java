/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.dynamicCities.parcels;

import com.google.common.collect.ImmutableMap;
import org.terasology.commonworld.Orientation;
import org.terasology.math.geom.Rect2i;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.RegisterTypeHandler;
import org.terasology.persistence.typeHandling.*;

import java.util.Map;
import java.util.Optional;

@RegisterTypeHandler
public class DynParcelTypeHandler extends TypeHandler<DynParcel> {

    @Override
    public PersistedData serialize(DynParcel parcel, PersistedDataSerializer serializer) {
        Map<String, PersistedData> data = new ImmutableMap.Builder()
                .put("height", serializer.serialize(parcel.getHeight()))
                .put("posX", serializer.serialize(parcel.getShape().minX()))
                .put("posY", serializer.serialize(parcel.getShape().minY()))
                .put("sizeX", serializer.serialize(parcel.getShape().sizeX()))
                .put("sizeY", serializer.serialize(parcel.getShape().sizeY()))
                .put("zone", serializer.serialize(parcel.getZone()))
                .put("orientation", serializer.serialize(parcel.getOrientation().name()))
                .build();
        return serializer.serialize(data);
    }

    @Override
    public Optional<DynParcel> deserialize(PersistedData data) {
        PersistedDataMap root = data.getAsValueMap();
        Rect2i shape = Rect2i.createFromMinAndSize(root.getAsInteger("posX"), root.getAsInteger("posY"),
                root.getAsInteger("sizeX"), root.getAsInteger("sizeY"));
        return Optional.ofNullable
                (new DynParcel(shape, Orientation.valueOf(root.getAsString("orientation")),
                        root.getAsString("zone"), root.getAsInteger("height")));
    }

    @Override
    public PersistedData serializeNonNull(DynParcel parcel, PersistedDataSerializer serializer) {
        return serializer.serializeNull();
    }
}
