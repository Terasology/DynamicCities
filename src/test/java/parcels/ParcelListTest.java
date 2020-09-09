// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package parcels;

import org.junit.Before;
import org.junit.Test;
import org.terasology.commonworld.Orientation;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.parcels.ParcelList;
import org.terasology.math.geom.Rect2i;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParcelListTest {
    final String CLERICAL = "CLERICAL";
    final String RESIDENTIAL = "RESIDENTIAL";
    final String GOVERNMENTAL = "GOVERNMENTAL";
    public ParcelList parcels;
    public Rect2i[] shapes;

    @Before
    public void setup() {
        parcels = new ParcelList(1);
        shapes = new Rect2i[5];
        shapes[0] = Rect2i.createFromMinAndSize(0, 0, 10, 10);
        shapes[1] = Rect2i.createFromMinAndSize(13, 13, 10, 10);
        shapes[2] = Rect2i.createFromMinAndSize(-13, -13, 10, 10);
        shapes[3] = Rect2i.createFromMinAndSize(-5, -5, 100, 100);
        shapes[4] = Rect2i.createFromMinAndSize(-100, -100, 10, 10);
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
