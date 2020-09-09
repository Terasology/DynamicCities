// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings;


import org.terasology.engine.entitySystem.Component;
import org.terasology.math.geom.Vector2i;

import java.util.List;

public class GenericBuildingComponent implements Component {

    /**
     * Can either store a composite genericBuildingData from the module cities or a structured template
     */
    public String name;
    public List<String> templateNames;
    public List<String> generatorNames;
    public String zone;
    public Vector2i minSize;
    public Vector2i maxSize;
    public boolean isEntity;
    public boolean isScaledDown;
    public String resourceUrn;
}
