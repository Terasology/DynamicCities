// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings;


import org.joml.Vector2i;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

public class GenericBuildingComponent implements Component<GenericBuildingComponent> {

    /**
     *  Can either store a composite genericBuildingData from the module cities
     *  or a structured template
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
