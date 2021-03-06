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
import org.terasology.dynamicCities.roads.RoadSegment;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Marks regions where the road will be laid in terms of its segments
 */
public class RoadParcel implements Parcel {
    public static final int OVERLAP = 2; // Overlap between two road segments
    public static final int RECT_SIZE = 10; // Size of the diagonal of a segment
    public static final int MARGIN = 5; // Margin between settlement boundary and road start

    public Vector<RoadSegment> rects;
    public Orientation orientation;

    public RoadParcel(Vector<RoadSegment> rects) {
        this.rects = rects;
    }

    public Set<BlockArea> expand(int dx, int dy) {
        Set<BlockArea> expandedRects = new HashSet<>();
        if (!rects.isEmpty()) {
            for (RoadSegment roadRect : rects) {
                expandedRects.add(roadRect.getRect().expand(dx, dy, new BlockArea(BlockArea.INVALID)));
            }
        }
        return expandedRects;
    }

    public Set<BlockAreac> getRects() {
        return rects.stream().map(roadRect -> roadRect.getRect()).collect(Collectors.toSet());
    }

    public boolean isNotIntersecting(BlockAreac rect) {
        for (RoadSegment segment : rects) {
            if (segment.getRect().intersectsBlockArea(rect)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BlockAreac getShape() {
        return rects.iterator().next().getRect();
    }

    @Override
    public Orientation getOrientation() {
        return orientation;
    }
}
