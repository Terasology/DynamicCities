// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.construction.components;


import org.terasology.dynamicCities.construction.BufferedBlock;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.ArrayList;
import java.util.List;


public final class BlockBufferComponent implements Component<BlockBufferComponent> {
    public List<BufferedBlock> blockBuffer;

    public BlockBufferComponent() {
        blockBuffer = new ArrayList<>();
    }
}
