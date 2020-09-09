// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.construction.events;

import org.terasology.engine.entitySystem.event.ConsumableEvent;
import org.terasology.engine.world.block.Block;
import org.terasology.math.geom.Vector3i;

/**
 * Sent when a building has been rasterized and a given block should be set directly
 */
public class SetBlockEvent implements ConsumableEvent {
    private final Vector3i pos;
    public Block block;
    private boolean consumed;

    public SetBlockEvent(Vector3i pos, Block block) {
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
