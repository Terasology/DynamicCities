// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.sites;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

import java.util.Objects;

/**
 * Provides information on a settlement site.
  */
@MappedContainer
public class SiteComponent implements Component<SiteComponent> {

    private Vector2i coords = new Vector2i();
    private float radius;

    /**
     * @param bx the x world coord (in blocks)
     * @param bz the z world coord (in blocks)
     */
    public SiteComponent(int bx, int bz) {
        this.coords = new Vector2i(bx, bz);
    }

    public SiteComponent() {
    }

    /**
     * @return the city center in block world coordinates
     */
    public Vector2ic getPos() {
        return coords;
    }

    /**
     * @return the radius of the settlements in blocks
     */
    public float getRadius() {
        return radius;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coords, radius);
    }

    @Override
    public boolean equals(Object obj) {
        if (SiteComponent.class == obj.getClass()) {
            SiteComponent other = (SiteComponent) obj;
            return Objects.equals(coords, other.coords)
                && Objects.equals(radius, other.radius);
        }
        return false;
    }

    @Override
    public String toString() {
        return "SiteComponent [" + coords + " (" + radius + ")]";
    }

    @Override
    public void copy(SiteComponent other) {

    }
}
