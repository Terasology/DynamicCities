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
package parcels;

import org.junit.Before;
import org.junit.Test;
import org.terasology.commonworld.Orientation;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.parcels.ParcelList;
import org.terasology.world.block.BlockArea;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParcelListTest {
    public ParcelList parcels;
    public BlockArea[] shapes;

    public static final String CLERICAL = "CLERICAL";
    public static final String RESIDENTIAL = "RESIDENTIAL";
    public static final String GOVERNMENTAL = "GOVERNMENTAL";

    @Before
    public void setup() {
        parcels = new ParcelList(1);
        shapes = new BlockArea[5];
        shapes[0] = new BlockArea(0, 0).setSize(10, 10);
        shapes[1] = new BlockArea(13, 13).setSize(10, 10);
        shapes[2] = new BlockArea(-13, -13).setSize(10, 10);
        shapes[3] = new BlockArea(-5, -5).setSize(100, 100);
        shapes[4] = new BlockArea(-100, -100).setSize(10, 10);
        parcels.addParcel(new DynParcel(shapes[0], Orientation.EAST, CLERICAL, 0));
        parcels.addParcel(new DynParcel(shapes[1], Orientation.EAST, RESIDENTIAL, 0));
        parcels.addParcel(new DynParcel(shapes[2], Orientation.EAST, GOVERNMENTAL, 0));
    }

    @Test
    public void testIntersection() {
        assertTrue(parcels.isNotIntersecting(shapes[4]));
        assertFalse(parcels.isNotIntersecting(shapes[3]));
    }

    @Test
    public void testZoneArea() {
        Map<String, Integer> areas = parcels.areaPerZone;
        assertEquals(100, (long) areas.get(CLERICAL));
        assertEquals(100, (long) areas.get(RESIDENTIAL));
        assertEquals(100, (long) areas.get(GOVERNMENTAL));
    }
}
