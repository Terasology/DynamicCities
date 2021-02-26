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

import org.joml.Vector2i;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.dynamicCities.facets.Grid2DObjectFacet;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.ObjectFacet3D;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests different implementations of {@link ObjectFacet3D}.
 */
public class Grid2DObjectFacetTest {

    private Grid2DObjectFacet<Integer> facet;

    @BeforeEach
    public void setup() {
        int gridSize = 4;
        Border3D border = new Border3D(0, 0, 0).extendBy(0, 15, 10);
        Vector3i min = new Vector3i(10, 20, 30);
        Vector3i size = new Vector3i(32, 32, 32);
        BlockRegion region = new BlockRegion(min).setSize(size);
        facet = createFacet(region, border, gridSize);
    }

    private class IntegerTestFacet extends Grid2DObjectFacet<Integer> {
        public IntegerTestFacet(BlockRegion targetRegion, Border3D border, int gridSize) {
            super(targetRegion, border, gridSize, Integer.class);
        }
    }

    protected IntegerTestFacet createFacet(BlockRegion region, Border3D extendBy, int gridSize) {
        return new IntegerTestFacet(region, extendBy, gridSize);
    }

    @Test
    public void testRelativeGridPoints() {
        assertEquals(new Vector2i(1, 0), facet.getRelativeGridPoint(4, 0));
        assertEquals(new Vector2i(0, 0), facet.getRelativeGridPoint(0, 0));
        assertEquals(new Vector2i(1, 1), facet.getRelativeGridPoint(4, 4));
        assertEquals(new Vector2i(2, 1), facet.getRelativeGridPoint(9, 3));
        assertEquals(new Vector2i(4, 0), facet.getRelativeGridPoint(16, 0));
        assertEquals(new Vector2i(8, 8), facet.getRelativeGridPoint(32, 32));
        assertEquals(new Vector2i(4, 4), facet.getRelativeGridPoint(17, 17));
    }

    @Test
    public void testWorldGridPoints() {
        assertEquals(new Vector2i(22, 42), facet.getWorldGridPoint(10, 30));
        assertEquals(new Vector2i(30, 50), facet.getWorldGridPoint(42, 62));
        assertEquals(new Vector2i(23, 43), facet.getWorldGridPoint(14, 34));
        assertEquals(new Vector2i(26, 46), facet.getWorldGridPoint(26, 46));
    }

    @Test
    public void testWorldPoints() {
        assertEquals(new Vector2i(10, 30), facet.getWorldPoint(22, 42));
        assertEquals(new Vector2i(42, 62), facet.getWorldPoint(30, 50));
        assertEquals(new Vector2i(26, 46), facet.getWorldPoint(26, 46));
    }

    /**
     * Check unset values
     */
    @Test
    public void testUnset() {
        assertNull(facet.get(0, 0));
        assertNull(facet.getWorld(10, 30));
    }

    @Test
    public void testRelBounds() {
        assertThrows(IllegalArgumentException.class, () -> {
            facet.set(-19, -19, 1);
        });
    }

    @Test
    public void testWorldBounds() {
        assertThrows(IllegalArgumentException.class, () -> {
            facet.setWorld(0, 0, 1);
        });
    }

    // Powers of 2 can be represented as float without rounding errors !

    @Test
    public void testPrimitiveGetSet() {
        facet.set(0, 2, 2);
        assertEquals(Integer.valueOf(2), facet.get(0, 2));
    }

    @Test
    public void testBoxedGetSet() {
        facet.set(0, 3, 4);
        assertEquals(Integer.valueOf(4), facet.get(0, 3));
    }

    @Test
    public void testBoxedWorldGetSet() {
        facet.setWorld(12, 35, 8);
        assertEquals(Integer.valueOf(8), facet.getWorld(12, 35));
    }

    @Test
    public void testMixedGetSet1() {
        facet.set(14, 12, 16);
        assertEquals(Integer.valueOf(16), facet.getWorld(24, 42));
    }

    @Test
    public void testMixedGetSet2() {
        facet.setWorld(24, 46, 32);
        assertEquals(Integer.valueOf(32), facet.get(14, 16), 0.0);
    }
}
