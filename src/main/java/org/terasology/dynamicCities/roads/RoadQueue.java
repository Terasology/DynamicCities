/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.dynamicCities.roads;

import org.terasology.cities.parcels.Parcel;
import org.terasology.dynamicCities.parcels.RoadParcel;
import org.terasology.entitySystem.Component;
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
