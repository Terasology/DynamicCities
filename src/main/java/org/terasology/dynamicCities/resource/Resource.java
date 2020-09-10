// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.resource;

import org.terasology.nui.reflection.MappedContainer;

/**
 * This is not to be confused with the economy resource definition, although they can possibly converted into such later
 * or replaced by them. This is to keep track of a region's resources as per actual blocks.
 */
@MappedContainer
public class Resource {

    public int amount;
    private ResourceType type;

    public Resource(ResourceType type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public Resource(ResourceType type) {
        this.type = type;
        this.amount = 1;
    }

    public Resource() {
    }

    public ResourceType getType() {
        return type;
    }


}

