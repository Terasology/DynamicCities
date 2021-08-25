// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.roads;

import com.google.common.collect.Sets;
import org.terasology.cities.parcels.Parcel;
import org.terasology.dynamicCities.parcels.RoadParcel;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds all parcels that are yet to be completed.
 */
public class RoadQueue implements Component<RoadQueue> {
    public Set<RoadParcel> roadQueue = new HashSet<>();

    public RoadQueue() {
    }

    public Collection<RoadParcel> getParcels() {
        return Collections.unmodifiableSet(roadQueue);
    }

    public boolean isNotIntersecting(Parcel parcel) {
        return isNotIntersecting(parcel.getShape());
    }

    public boolean isNotIntersecting(BlockAreac rect) {
        for (RoadParcel parcel : roadQueue) {
            for (BlockAreac roadRect : parcel.getRects()) {
                if (rect.intersectsBlockArea(roadRect)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void copyFrom(RoadQueue other) {
        this.roadQueue = Sets.newHashSet(other.roadQueue);
    }
}
