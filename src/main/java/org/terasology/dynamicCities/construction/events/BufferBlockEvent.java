/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.dynamicCities.construction.events;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.event.ConsumableEvent;
import org.terasology.engine.world.block.Block;

/**
 * Sent when a building has been rasterized and a given block should be buffered
 */
public class BufferBlockEvent implements ConsumableEvent {
    public final Block block;
    private Vector3i pos;
    private boolean consumed;

    public BufferBlockEvent(Vector3ic pos, Block block) {
        this.pos = new Vector3i(pos);
        this.block = block;
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public void consume() {
        consumed = true;
    }

    public Vector3i getPos() {
        return new Vector3i(pos);
    }

    public void setPos(Vector3i pos) {
        this.pos.set(pos);
    }
}
