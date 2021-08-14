// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings;


import com.google.common.collect.Lists;
import org.joml.Vector2i;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

public class GenericBuildingComponent implements Component<GenericBuildingComponent> {

    /**
     *  Can either store a composite genericBuildingData from the module cities
     *  or a structured template
     */
    public String name;
    public List<String> templateNames = Lists.newArrayList();
    public List<String> generatorNames = Lists.newArrayList();
    public String zone;
    public Vector2i minSize = new Vector2i();
    public Vector2i maxSize = new Vector2i();
    public boolean isEntity;
    public boolean isScaledDown;
    public String resourceUrn;

    @Override
    public void copyFrom(GenericBuildingComponent other) {
        this.name = other.name;
        this.templateNames = Lists.newArrayList(other.templateNames);
        this.generatorNames = Lists.newArrayList(other.generatorNames);
        this.zone = other.zone;
        this.minSize.set(other.minSize);
        this.maxSize.set(other.maxSize);
        this.isEntity = other.isEntity;
        this.isScaledDown = other.isScaledDown;
        this.resourceUrn = other.resourceUrn;
    }
}
