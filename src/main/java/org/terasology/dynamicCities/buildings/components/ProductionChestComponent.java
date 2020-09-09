// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings.components;


import org.terasology.engine.entitySystem.Component;
import org.terasology.math.geom.Vector3i;

import java.util.List;

public class ProductionChestComponent implements Component {
    /**
     * Position of the chest of which resources should be stored in
     */
    public List<Vector3i> positions;
}
