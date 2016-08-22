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

public class RegionEntitiesTest {
/*
    private RegionEntitiesComponent regionEntitiesComponent;
    private EntityRef[] test;

    private Vector3f[] pos = new Vector3f[4];
    @Before
    public void setup() {
        pos[0] = new Vector3f(16, 0, 16);
        pos[1] = new Vector3f(16, 0, -16);
        pos[2] = new Vector3f(-16, 0, 16);
        pos[3] = new Vector3f(-16, 0, -16);

        regionEntitiesComponent = new RegionEntitiesComponent(64);
        test = new EntityRef[4];
        LocationComponent[] loc = new LocationComponent[test.length];
        for (int i = 0; i < test.length; i++) {
            test[i] = Mockito.mock(EntityRef.class);
            loc[i] = new LocationComponent(pos[i]);
            Mockito.when(test[i].getComponent(LocationComponent.class)).thenReturn(loc[i]);
            regionEntitiesComponent.add(test[i]);
        }


    }

    @Test
    public void testSimpleGet() {
        for (int i = 0; i < test.length; i++) {
            assertEquals(test[i], regionEntitiesComponent.get(new Vector2i(pos[i].x(), pos[i].z())));
        }
    }

    @Test
    public void testNearestGet() {
        assertEquals(test[0], regionEntitiesComponent.getNearest(new Vector2i(21, 13)));
        assertEquals(test[1], regionEntitiesComponent.getNearest(new Vector2i(14, -13)));
        assertEquals(test[2], regionEntitiesComponent.getNearest(new Vector2i(-22, 19)));
        assertEquals(test[3], regionEntitiesComponent.getNearest(new Vector2i(-13, -19)));
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
        for (EntityRef region : test) {
            testList.add(region);
        }
        assertEquals(testList, regionEntitiesComponent.getRegionsInCell(new Vector2i(0, 0)));
    }
*/
}
