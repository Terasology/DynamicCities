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

import com.google.common.base.Preconditions;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.ImmutableVector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Road {

    private final List<ImmutableVector2i> segmentPoints;
    private final List<RoadSegment> segments;
    private final float width;
    private final float length;

    /**
     * The point list goes from start to end, but does not contain them
     * @param end0 one end point
     * @param end1 the other end point
     * @param width the width of the road
     */
    public Road(BaseVector2i end0, BaseVector2i end1, float width) {
        this(Arrays.asList(end0, end1), width);
    }

    public Road(List<? extends BaseVector2i> segPoints, float width) {
        Preconditions.checkArgument(segPoints.size() >= 2, "must contain at least two points");

        segmentPoints = new ArrayList<>(segPoints.size());

        float tmpLength = 0;
        BaseVector2i prev = segPoints.get(0);
        for (BaseVector2i segPoint : segPoints) {
            tmpLength += segPoint.distance(prev);
            prev = segPoint;
            segmentPoints.add(ImmutableVector2i.createOrUse(segPoint));
        }

        segments = new ArrayList<>(segPoints.size() - 1);
        for (int i = 1; i < segPoints.size(); i++) {
            ImmutableVector2i p = segmentPoints.get(i - 1);
            ImmutableVector2i c = segmentPoints.get(i);
            segments.add(new RoadSegment(p, c, width));
        }

        this.length = tmpLength;
        this.width = width;
    }

    /**
     * @return the other end point
     */
    public ImmutableVector2i getEnd1() {
        return segmentPoints.get(segmentPoints.size() - 1);
    }

    /**
     * @return one end point
     */
    public ImmutableVector2i getEnd0() {
        return segmentPoints.get(0);
    }

    /**
     * @return an unmodifiable view on the segment points (at least two points)
     */
    public List<ImmutableVector2i> getPoints() {
        return Collections.unmodifiableList(segmentPoints);
    }

    /**
     * @return an unmodifiable view on the segment points (at least one segment)
     */
    public List<RoadSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    /**
     * @param pos the coordinate to test
     * @return true, if the road ends at the given coordinate
     */
    public boolean endsAt(BaseVector2i pos) {
        return getEnd0().equals(pos) || getEnd1().equals(pos);
    }

    /**
     * @param pos one end of the road
     * @return the other end
     * @throws IllegalArgumentException if not an end point of the road
     */
    public ImmutableVector2i getOtherEnd(BaseVector2i pos) {
        if (getEnd0().equals(pos)) {
            return getEnd1();
        }
        if (getEnd1().equals(pos)) {
            return getEnd0();
        }
        throw new IllegalArgumentException("not an end point of the road");
    }

    /**
     * @return the length of the road in blocks
     */
    public float getLength() {
        return length;
    }

    /**
     * @return the width of the road in blocks
     */
    public float getWidth() {
        return width;
    }
}
