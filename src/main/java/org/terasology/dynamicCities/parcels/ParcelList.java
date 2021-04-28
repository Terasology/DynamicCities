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


import org.terasology.cities.parcels.Parcel;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.reflection.MappedContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MappedContainer
public class ParcelList implements Component {

    public Map<String, Integer> areaPerZone;
    //The area in which buildings can currently be placed
    public float cityRadius;
    //The distance of the currently farthest away building (from city center)
    @Replicate
    public float builtUpRadius;

    public List<Parcel> parcels;

    public ParcelList() { }
    public ParcelList(int i) {
        builtUpRadius = 0;
        cityRadius = 60;
        parcels = new ArrayList<>();
        areaPerZone = new HashMap<>();
    }

    public void addParcel(Parcel parcel) {
        parcels.add(parcel);
        if (parcel instanceof DynParcel) {
            DynParcel dynParcel = ((DynParcel) parcel);
            String zone = dynParcel.getZone();
            if (areaPerZone.containsKey(zone)) {
                areaPerZone.put(zone, areaPerZone.get(zone) + dynParcel.getShape().area());
            } else {
                areaPerZone.put(zone, dynParcel.getShape().area());
            }
        }
    }

    public boolean isNotIntersecting(DynParcel parcel) {
        return isNotIntersecting(parcel.shape);
    }

    public boolean isNotIntersecting(BlockAreac rect) {
        for (Parcel spawnedParcels : parcels) {
            if (spawnedParcels instanceof RoadParcel) {
                return ((RoadParcel) spawnedParcels).isNotIntersecting(rect);
            } else if (spawnedParcels.getShape().intersectsBlockArea(rect)) {
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
