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
package org.terasology.dynamicCities.parcels;

import org.terasology.cities.parcels.Parcel;
import org.terasology.commonworld.Orientation;
import org.terasology.math.geom.Rect2i;

import java.util.HashSet;
import java.util.Set;

public class RoadParcel implements Parcel {
    private Set<Rect2i> rects = new HashSet<>();
    private Orientation orientation;

    public RoadParcel(Set<Rect2i> rects) {
        this.rects = rects;
    }

    public RoadParcel() {
    }

    public Set<Rect2i> expand(int dx, int dy) {
        Set<Rect2i> expandedRects = new HashSet<>();
        if (!rects.isEmpty()) {
            for (Rect2i rect : rects) {
                expandedRects.add(rect.expand(dx, dy));
            }
        }
        return expandedRects;
    }

    public Set<Rect2i> getRects() {
        return rects;
    }

    @Override
    public Rect2i getShape() {
        return rects.iterator().next();
    }

    @Override
    public Orientation getOrientation() {
        return orientation;
    }
}
