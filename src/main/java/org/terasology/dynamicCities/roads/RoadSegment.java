/*
 * Copyright 2015 MovingBlocks
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

import org.terasology.math.geom.ImmutableVector2i;

/**
 * A road segment is a part of a road with direction. One segment's end is the next segment's start.
 */
public class RoadSegment {

    private final ImmutableVector2i start;
    private final ImmutableVector2i end;
    private final float width;

    public RoadSegment(ImmutableVector2i start, ImmutableVector2i end, float width) {
        this.start = start;
        this.end = end;
        this.width = width;
    }

    /**
     * @return the start point
     */
    public ImmutableVector2i getStart() {
        return start;
    }

    /**
     * @return the end point
     */
    public ImmutableVector2i getEnd() {
        return end;
    }

    /**
     * @return the length of the road segment in blocks
     */
    public float getLength() {
        return (float) start.distance(end);
    }

    /**
     * @return the width of the road in blocks
     */
    public float getWidth() {
        return width;
    }
}
