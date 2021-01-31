/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.dynamicCities.sites;

import org.joml.Vector2i;
import org.terasology.entitySystem.Component;
import org.terasology.reflection.MappedContainer;

import java.util.Objects;

/**
 * Provides information on a settlement site.
  */
@MappedContainer
public class SiteComponent implements Component {

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
    public Vector2i getPos() {
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
}
