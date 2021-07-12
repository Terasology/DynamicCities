// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings.components;


import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

public class ChestPositionsComponent implements Component<ChestPositionsComponent> {
    /**
     * Position of the chest in the local coordinate space of a template of which resources should be drawn out
     * Important: Add this to the structure template prefab and not to the building prefab.
     */
    public List<Vector3i> positions = Lists.newArrayList();

    @Override
    public void copy(ChestPositionsComponent other) {
        this.positions = Lists.newArrayList(other.positions);
    }
}
