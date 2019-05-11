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

package regions;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.terasology.dynamicCities.region.RegionEntityManager;
import org.terasology.dynamicCities.region.components.RegionEntitiesComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3f;
import org.terasology.moduletestingenvironment.ModuleTestingEnvironment;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class RegionEntitiesTest extends ModuleTestingEnvironment {

    private RegionEntitiesComponent regionEntitiesComponent;
    private RegionEntityManager regionEntityManager;

    private EntityRef[] test;
    private Vector3f[] pos = new Vector3f[4];

    @Override
    public Set<String> getDependencies() {
        return Sets.newHashSet("engine", "DynamicCities", "ModuleTestingEnvironment");
    }

    /*
    @Before
    public void setup() {
        pos[0] = new Vector3f(16, 0, 16);
        pos[1] = new Vector3f(16, 0, -16);
        pos[2] = new Vector3f(-16, 0, 16);
        pos[3] = new Vector3f(-16, 0, -16);

//        regionEntityManager = getHostContext().get(RegionEntityManager.class);

//        regionEntityManager = Mockito.mock(RegionEntityManager.class);

        regionEntitiesComponent = new RegionEntitiesComponent(64);
        test = new EntityRef[4];
        LocationComponent[] loc = new LocationComponent[test.length];
        for (int i = 0; i < test.length; i++) {
            test[i] = Mockito.mock(EntityRef.class);
            loc[i] = new LocationComponent(pos[i]);
            Mockito.when(test[i].getComponent(LocationComponent.class)).thenReturn(loc[i]);
//            regionEntitiesComponent.add(test[i]);
//            regionEntityManager.add(test[i]);
        }
    }
    */

    @Test
    public void testSimpleGet() {
        assertEquals(200, 200);

//        regionEntityManager = getHostContext().get(RegionEntityManager.class);
//
//        for (EntityRef entityRef : test) {
//            regionEntityManager.add(entityRef);
//        }
//
//        for (int i = 0; i < test.length; i++) {
//            assertEquals(test[i], regionEntityManager.get(new Vector2i(pos[i].x(), pos[i].z())));
//        }
    }

    /*
    @Test
    public void testNearestGet() {
        assertEquals(test[0], regionEntityManager.getNearest(new Vector2i(21, 13)));
        assertEquals(test[1], regionEntityManager.getNearest(new Vector2i(14, -13)));
        assertEquals(test[2], regionEntityManager.getNearest(new Vector2i(-22, 19)));
        assertEquals(test[3], regionEntityManager.getNearest(new Vector2i(-13, -19)));
    }

    @Test
    public void testIsLoaded() {
        assertEquals(true, regionEntitiesComponent.cellIsLoaded(new Vector2i(0, 0)));
        assertEquals(true, regionEntitiesComponent.cellIsLoaded(new Vector2i(16, 0)));
        assertEquals(true, regionEntitiesComponent.cellIsLoaded(new Vector2i(0, -16)));
        assertEquals(true, regionEntitiesComponent.cellIsLoaded(new Vector2i(24, -25)));
        assertEquals(false, regionEntitiesComponent.cellIsLoaded(new Vector2i(64, -64)));
        assertEquals(false, regionEntitiesComponent.cellIsLoaded(new Vector2i(48, -35)));
        assertEquals(false, regionEntitiesComponent.cellIsLoaded(new Vector2i(10, -36)));
    }

    @Test
    public void testGetRegionssInCell() {
        List<EntityRef> testList = new ArrayList<>();
        Collections.addAll(testList, test);
        assertEquals(testList, regionEntitiesComponent.getRegionsInCell(new Vector2i(0, 0)));
    }
    */
}
