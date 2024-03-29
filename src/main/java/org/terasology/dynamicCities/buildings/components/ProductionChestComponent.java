// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings.components;


import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

public class ProductionChestComponent implements Component<ProductionChestComponent> {
    /**
     * Position of the chest of which resources should be stored in
     */
    public List<Vector3i> positions = Lists.newArrayList();

    @Override
    public void copyFrom(ProductionChestComponent other) {
        this.positions = Lists.newArrayList(other.positions);
    }
}
