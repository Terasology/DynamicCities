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
package org.terasology.dynamicCities.buildings;


import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.BlockAreac;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BuildingQueue implements Component {
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
