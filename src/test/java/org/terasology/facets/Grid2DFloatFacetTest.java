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

package org.terasology.facets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.dynamicCities.facets.Grid2DFloatFacet;
import org.terasology.dynamicCities.facets.Grid2DObjectFacet;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.FieldFacet3D;

/**
 * Tests different implementations of {@link FieldFacet3D}.
 *
 */
public class Grid2DFloatFacetTest{

    private Grid2DFloatFacet facet;

    @Before
    public void setup() {
        int gridSize = 4;
        Border3D border = new Border3D(0, 0, 0).extendBy(0, 15, 10);
        Vector3i min = new Vector3i(10, 20, 30);
        Vector3i size = new Vector3i(32, 32, 32);
        Region3i region = Region3i.createFromMinAndSize(min, size);
        facet = createFacet(region, border, gridSize);
    }

    protected Grid2DFloatFacet createFacet(Region3i region, Border3D extendBy, int gridSize) {
        return new FloatTestFacet(region, extendBy, gridSize);
    }

    public class FloatTestFacet extends Grid2DFloatFacet {
        public FloatTestFacet(Region3i targetRegion, Border3D border, int gridSize) {
            super(targetRegion, border, gridSize);
        }
    }
    /**
     * Check unset values
     */

    @Test
    public void testCenter() {
        Assert.assertEquals(new Vector2i(26, 46), facet.getCenter());
    }

    @Test
    public void testUnset() {
        Assert.assertEquals(0.0f, facet.get(0, 0), 0.0);
        Assert.assertEquals(0.0f, facet.getWorld(10, 30), 0.0);
    }

    @Test
    public void testRelativeGridPoints() {
        Assert.assertEquals(new Vector2i(1, 0), facet.getRelativeGridPoint(4, 0));
        Assert.assertEquals(new Vector2i(0, 0), facet.getRelativeGridPoint(0, 0));
        Assert.assertEquals(new Vector2i(1, 1), facet.getRelativeGridPoint(4, 4));
        Assert.assertEquals(new Vector2i(2, 1), facet.getRelativeGridPoint(9, 3));
        Assert.assertEquals(new Vector2i(4, 0), facet.getRelativeGridPoint(16, 0));
        Assert.assertEquals(new Vector2i(8, 8), facet.getRelativeGridPoint(32, 32));
        Assert.assertEquals(new Vector2i(4, 4), facet.getRelativeGridPoint(17, 17));
    }

    @Test
    public void testWorldGridPoints() {
        Assert.assertEquals(new Vector2i(22, 42), facet.getWorldGridPoint(10, 30));
        Assert.assertEquals(new Vector2i(30, 50), facet.getWorldGridPoint(42, 62));
        Assert.assertEquals(new Vector2i(23, 43), facet.getWorldGridPoint(14, 34));
        Assert.assertEquals(new Vector2i(26, 46), facet.getWorldGridPoint(26, 46));
    }

    @Test
    public void testWorldPoints() {
        Assert.assertEquals(new Vector2i(10, 30), facet.getWorldPoint(22, 42));
        Assert.assertEquals(new Vector2i(42, 62), facet.getWorldPoint(30, 50));
        Assert.assertEquals(new Vector2i(26, 46), facet.getWorldPoint(26, 46));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelBounds() {
        facet.set(-19, -19, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWorldBounds() {
        facet.setWorld(0, 0, 1);
    }

    // Powers of 2 can be represented as float without rounding errors !

    @Test
    public void testPrimitiveGetSet() {
        facet.set(0, 1, 2.0f);
        Assert.assertEquals(2.0f, facet.get(0, 1), 0.0);
    }

    @Test
    public void testBoxedGetSet() {
        facet.set(0, 3, 4f);
        Assert.assertEquals(4.0f, facet.get(0, 3), 0.0);
    }

    @Test
    public void testBoxedWorldGetSet() {
        facet.setWorld(12, 35, 8f);
        Assert.assertEquals(8.0f, facet.getWorld(12, 35), 0.0);
        facet.setWorld(11, 34, 8f);
        Assert.assertEquals(8.0f, facet.getWorld(12, 35), 0.0);
    }

    @Test
    public void testMixedGetSet1() {
        facet.set(14, 12, 16f);
        Assert.assertEquals(16.0f, facet.getWorld(24, 42), 0.0);
    }

    @Test
    public void testMixedGetSet2() {
        facet.setWorld(24, 46, 32f);
        Assert.assertEquals(32.0f, facet.get(14, 16), 0.0);
    }

}
