// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.parcels;


import org.terasology.cities.parcels.Parcel;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.math.geom.Rect2i;
import org.terasology.nui.reflection.MappedContainer;

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

    public ParcelList() {
    }

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

    public boolean isNotIntersecting(Rect2i rect) {
        for (Parcel spawnedParcels : parcels) {
            if (spawnedParcels instanceof RoadParcel) {
                return ((RoadParcel) spawnedParcels).isNotIntersecting(rect);
            } else if (spawnedParcels.getShape().overlaps(rect)) {
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
