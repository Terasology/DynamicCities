// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.construction.components;

import com.google.common.collect.Lists;
import org.terasology.dynamicCities.construction.BufferedBlock;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

public final class BlockBufferComponent implements Component<BlockBufferComponent> {
    public List<BufferedBlock> blockBuffer = Lists.newArrayList();

    public BlockBufferComponent() {

    }

    @Override
    public void copy(BlockBufferComponent other) {
        this.blockBuffer = Lists.newArrayList(other.blockBuffer);
    }
}
