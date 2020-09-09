// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.roads;

import org.terasology.cities.parcels.Parcel;
import org.terasology.dynamicCities.parcels.RoadParcel;
import org.terasology.engine.entitySystem.Component;
import org.terasology.math.geom.Rect2i;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds all parcels that are yet to be completed.
 */
public class RoadQueue implements Component {
    public Set<RoadParcel> roadQueue;

    public RoadQueue() {
        roadQueue = new HashSet<>();
    }

    public Collection<RoadParcel> getParcels() {
        return Collections.unmodifiableSet(roadQueue);
    }

    public boolean isNotIntersecting(Parcel parcel) {
        return isNotIntersecting(parcel.getShape());
    }

    public boolean isNotIntersecting(Rect2i rect) {
        for (RoadParcel parcel : roadQueue) {
            for (Rect2i roadRect : parcel.getRects()) {
                if (rect.overlaps(roadRect)) {
                    return false;
                }
            }
        }
        return true;
    }
}
