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
import org.terasology.dynamicCities.parcels.Zone;
import org.terasology.math.geom.Rect2i;

import static org.junit.Assert.assertEquals;

public class ParcelListTest {

    public ParcelList parcels;
    public Rect2i[] shapes;
    @Before
    public void setup() {
        parcels = new ParcelList();
        shapes = new Rect2i[5];
        shapes[0] = Rect2i.createFromMinAndSize(0, 0, 10, 10);
        shapes[1] = Rect2i.createFromMinAndSize(13, 13, 10, 10);
        shapes[2] = Rect2i.createFromMinAndSize(-13, -13, 10, 10);
        shapes[3] = Rect2i.createFromMinAndSize(-5, -5, 100, 100);
        shapes[4] = Rect2i.createFromMinAndSize(-100, -100, 10, 10);
        parcels.addParcel(new DynParcel(shapes[0], Orientation.EAST, Zone.CLERICAL, 0));
        parcels.addParcel(new DynParcel(shapes[1], Orientation.EAST, Zone.RESIDENTIAL, 0));
        parcels.addParcel(new DynParcel(shapes[2], Orientation.EAST, Zone.GOVERNMENTAL, 0));
    }

    @Test
    public void testIntersection() {
        assertEquals(true, parcels.isNotIntersecting(shapes[4]));
        assertEquals(false, parcels.isNotIntersecting(shapes[3]));
    }

    @Test
    public void testZoneArea() {
        assertEquals(100, parcels.clericalArea);
        assertEquals(100, parcels.residentialArea);
        assertEquals(100, parcels.governmentalArea);
    }
}
