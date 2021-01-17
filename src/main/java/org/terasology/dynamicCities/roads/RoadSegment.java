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

import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.world.block.BlockAreac;

/**
 * A rectangular piece of the road with a start and an end point
 */
public class RoadSegment {
    public BlockAreac rect;
    public int height;
    public ImmutableVector2i start;
    public ImmutableVector2i end;

    public RoadSegment(BlockAreac rect, int height) {
        this.rect = rect;
        this.height = height;
    }

    public RoadSegment(BlockAreac rect, int height, ImmutableVector2i start, ImmutableVector2i end) {
        this.rect = rect;
        this.height = height;
        this.start = start;
        this.end = end;
    }

    /**
     * @return the length of the road segment in blocks
     */
    public float getLength() {
        return rect.getSizeX();
    }

    /**
     * @return the width of the road in blocks
     */
    public float getWidth() {
        return rect.getSizeY();
    }

    /**
     * @return a unit vector from start to end
     */
    public ImmutableVector2f getRoadDirection() {
        return new ImmutableVector2f(
                new Vector2i(end.sub(start)).toVector2f().normalize()
        );
    }
}
