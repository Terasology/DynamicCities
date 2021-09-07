// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.region;

import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.moduletestingenvironment.MTEExtension;
import org.terasology.moduletestingenvironment.extension.Dependencies;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("MteTest")
@ExtendWith({MockitoExtension.class, MTEExtension.class})
@Dependencies("DynamicCities")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegionEntitiesTest {
    @In
    public RegionEntityManager regionEntityManager;

    private final Vector3fc[] pos = new Vector3f[] {
        new Vector3f(16, 0, 16),
        new Vector3f(16, 0, -16),
        new Vector3f(-16, 0, 16),
        new Vector3f(-16, 0, -16),
        new Vector3f(0, 0, 0),
        new Vector3f(32, 0, 32),
        new Vector3f(32, 0, -32),
        new Vector3f(-32, 0, 32),
        new Vector3f(-32, 0, -32),
        new Vector3f(97, 0, -97)
    };
    private final EntityRef[] test = new EntityRef[pos.length];

    @BeforeAll
    public void setupEntityRefs() {
        for (int i = 0; i < test.length; i++) {
            test[i] = Mockito.mock(EntityRef.class, "mock EntityRef#"  + i);
            Mockito.when(test[i].getComponent(LocationComponent.class))
                    .thenReturn(new LocationComponent(pos[i]));
            regionEntityManager.add(test[i]);
        }
    }

    @Test
    public void testSimpleGet() {
        for (int i = 0; i < test.length; i++) {
            Vector2i position = new Vector2i(pos[i].x(), pos[i].z(), RoundingMode.FLOOR);
            assertEquals(test[i], regionEntityManager.get(position));
        }
    }

    @ParameterizedTest(name = "{1}, {2} is nearest pos[{0}]")
    @CsvSource({
            "0, 21, 13",
            "1, 14, -13",
            "2, -22, 19",
            "3, -13, -19"
    })
    public void testNearestGet(int index, int x, int y) {
        assertEquals(test[index], regionEntityManager.getNearest(new Vector2i(x, y)));
    }

    @ParameterizedTest(name = "cell loaded at {0}, {1}")
    @CsvSource({"0, 0", "16, 0", "0, -25"})
    public void testCellsInRegionAreLoaded(int x, int y) {
        assertTrue(regionEntityManager.cellIsLoaded(new Vector2i(x, y)));
    }

    @ParameterizedTest(name = "cell not loaded at {0}, {1}")
    @CsvSource({"98, -124", "351, 234", "153, -134"})
    public void testCellsOutsideRegionAreNotLoaded(int x, int y) {
        assertFalse(regionEntityManager.cellIsLoaded(new Vector2i(x, y)));
    }

    @Disabled
    @Test
    public void testGetRegionsInCell() {
        List<EntityRef> testList = Arrays.asList(test).subList(0, 5);
        assertEquals(testList, regionEntityManager.getRegionsInCell(new Vector2i(0, 0)));
    }
}
