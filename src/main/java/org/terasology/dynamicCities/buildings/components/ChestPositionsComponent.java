// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings.components;


import org.terasology.engine.entitySystem.Component;
import org.terasology.math.geom.Vector3i;

import java.util.List;

public class ChestPositionsComponent implements Component {
    /**
     * Position of the chest in the local coordinate space of a template of which resources should be drawn out
     * Important: Add this to the structure template prefab and not to the building prefab.
     */
    public List<Vector3i> positions;
}
