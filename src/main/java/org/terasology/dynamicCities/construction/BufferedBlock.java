// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.construction;

import org.terasology.engine.world.block.Block;
import org.terasology.math.geom.Vector3i;
import org.terasology.reflection.MappedContainer;

@MappedContainer
public final class BufferedBlock {
    public Vector3i pos;
    public Block blockType;

    public BufferedBlock(Vector3i pos, Block blockType) {
        this.pos = pos;
        this.blockType = blockType;
    }

    public BufferedBlock() {

    }
}
