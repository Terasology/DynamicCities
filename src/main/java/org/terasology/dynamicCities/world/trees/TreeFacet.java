// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.world.trees;


import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.SparseObjectFacet3D;

/**
 * Stores a random seed for a tree to be planted
 */
public class TreeFacet extends SparseObjectFacet3D<TreeGenerator> {

    public TreeFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
