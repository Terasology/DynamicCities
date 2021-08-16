// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings;


import org.joml.Vector2i;
import org.terasology.engine.entitySystem.Component;

import java.util.List;

/**
 * Describes a single building that can be constructed from structure templates or building generators.
 *
 * All referenced templates or generators contribute to the same composite building.
 */
public class GenericBuildingComponent implements Component {

    /**
     *  Can either store a composite genericBuildingData from the module cities
     *  or a structured template
     */
    public String name;

    /**
     * Multiple templates are interpreted as parts of a composite building, and will all be applied in the order they are defined.
     *
     * Templates will be applied AFTER {@link #generatorNames}.
     */
    public List<String> templateNames;

    /**
     * Multiple generators are interpreted as parts of a composite building, and will all be applied in the order they are defined.
     *
     * Generators will be applied BEFORE {@link #templateNames}.
     */
    public List<String> generatorNames;
    public String zone;
    public Vector2i minSize;
    public Vector2i maxSize;
    public boolean isEntity;
    public boolean isScaledDown;
    public String resourceUrn;
}
