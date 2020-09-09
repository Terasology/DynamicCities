// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.construction.components;


import org.terasology.dynamicCities.construction.BufferedBlock;
import org.terasology.engine.entitySystem.Component;

import java.util.ArrayList;
import java.util.List;


public final class BlockBufferComponent implements Component {
    public List<BufferedBlock> blockBuffer;

    public BlockBufferComponent() {
        blockBuffer = new ArrayList<>();
    }
}
