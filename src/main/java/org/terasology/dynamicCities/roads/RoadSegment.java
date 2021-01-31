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

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;

/**
 * A rectangular piece of the road with a start and an end point
 */
public class RoadSegment {
    public BlockArea rect = new BlockArea(BlockArea.INVALID);
    public int height;
    public Vector2i start = new Vector2i();
    public Vector2i end = new Vector2i();

    public RoadSegment(BlockAreac rect, int height) {
        this.rect.set(rect);
        this.height = height;
    }

    public RoadSegment(BlockAreac rect, int height, Vector2ic start, Vector2ic end) {
        this.rect.set(rect);
        this.height = height;
        this.start.set(start);
        this.end.set(end);
    }

    public int getHeight() {
        return height;
    }

    public Vector2ic getStart() {
        return start;
    }

    public Vector2ic getEnd() {
        return end;
    }

    public BlockAreac getRect() {
        return rect;
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

}
