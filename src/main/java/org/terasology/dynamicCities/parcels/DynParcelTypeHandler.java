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
import org.terasology.persistence.typeHandling.*;

import java.util.Map;

@RegisterTypeHandler
public class DynParcelTypeHandler extends SimpleTypeHandler<DynParcel> {

    @Override
    public PersistedData serialize(DynParcel parcel, SerializationContext context) {
        Map<String, PersistedData> data = new ImmutableMap.Builder()
                .put("height", context.create(parcel.getHeight()))
                .put("posX", context.create(parcel.getShape().minX()))
                .put("posY", context.create(parcel.getShape().minY()))
                .put("sizeX", context.create(parcel.getShape().sizeX()))
                .put("sizeY", context.create(parcel.getShape().sizeY()))
                .put("zone", context.create(parcel.getZoneDyn().name()))
                .put("orientation", context.create(parcel.getOrientation().name()))
                .build();
        return context.create(data);
    }

    @Override
    public DynParcel deserialize(PersistedData data, DeserializationContext context) {
        PersistedDataMap root = data.getAsValueMap();
        Rect2i shape = Rect2i.createFromMinAndSize(root.getAsInteger("posX"), root.getAsInteger("posY"),
                root.getAsInteger("sizeX"), root.getAsInteger("sizeY"));
        return new DynParcel(shape, Orientation.valueOf(root.getAsString("orientation")), Zone.valueOf(root.getAsString("zone")), root.getAsInteger("height"));
    }
}
