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


import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Rect2i;
import org.terasology.reflection.MappedContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MappedContainer
public class ParcelList implements Component {

    public Map<String, Integer> areaPerZone;

    public float minBuildRadius;
    public float maxBuildRadius;
    public List<DynParcel> parcels;

    public ParcelList() { }
    public ParcelList(int i) {
        maxBuildRadius = 0;
        minBuildRadius = 60;
        parcels = new ArrayList<>();
        areaPerZone = new HashMap<>();
    }

    public void addParcel(DynParcel parcel) {
        parcels.add(parcel);
        String zone = parcel.getZone();
        if (areaPerZone.containsKey(zone)) {
            areaPerZone.put(zone, areaPerZone.get(zone) + parcel.getShape().area());
        } else {
            areaPerZone.put(zone, parcel.getShape().area());
        }
    }

    public boolean isNotIntersecting(DynParcel parcel) {
        return isNotIntersecting(parcel.shape);
    }

    public boolean isNotIntersecting(Rect2i rect) {
        for (DynParcel spawnedParcels : parcels) {
            if (spawnedParcels.getShape().overlaps(rect)) {
               return false;
            }
        }
        return true;
    }

    public ParcelList copy() {
        ParcelList copy = new ParcelList(1);
        copy.parcels.addAll(parcels);
        return copy;
    }

}
