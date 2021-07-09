// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings;


import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BuildingQueue implements Component<BuildingQueue> {
    public Set<DynParcel> buildingQueue;


    public BuildingQueue() {
        buildingQueue = new HashSet<>();
    }

    public Collection<DynParcel> getParcels() {
        return Collections.unmodifiableSet(buildingQueue);
    }

    public boolean isNotIntersecting(DynParcel parcel) {
        return isNotIntersecting(parcel.shape);
    }

    public boolean isNotIntersecting(BlockAreac rect) {
        for (DynParcel spawnedParcels : buildingQueue) {
            if (spawnedParcels.getShape().intersectsBlockArea(rect)) {
                return false;
            }
        }
        return true;
    }
}
