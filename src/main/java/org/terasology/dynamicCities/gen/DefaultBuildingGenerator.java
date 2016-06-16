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

package org.terasology.dynamicCities.gen;

import com.google.common.math.IntMath;
import org.terasology.cities.bldg.Building;
import org.terasology.cities.door.SimpleDoor;
import org.terasology.cities.parcels.Parcel;
import org.terasology.cities.window.SimpleWindow;
import org.terasology.commonworld.Orientation;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.Set;

/**
 *
 */
public class DefaultBuildingGenerator implements BuildingGenerator {

    private long seed;
    private final RectHouseGenerator gen = new RectHouseGenerator();

    public DefaultBuildingGenerator(long seed)  {
        this.seed = seed;
    }

    @Override
    public Set<Building> generate(Parcel parcel, HeightMap hm) {
        Random rng = new FastRandom(parcel.getShape().hashCode() ^ seed);
        Building b;
        switch (parcel.getZone()) {
        case RESIDENTIAL:
            if (rng.nextFloat() < 0.2f) {
                b = generateRoundHouse(parcel, hm);
            } else {
                b = gen.apply(parcel, hm);
            }
            break;

        case GOVERNMENTAL:
            b = new TownHallGenerator().generate(parcel, hm);
            break;

        case COMMERCIAL:
            b = new CommercialBuildingGenerator(seed).generate(parcel, hm);
            break;

        case CLERICAL:
            b = new SimpleChurchGenerator(seed).apply(parcel, hm);
            break;

        default:
            return Collections.emptySet();
        }

        return Collections.singleton(b);
    }

    private Building generateRoundHouse(Parcel parcel, HeightMap hm) {

        // make build-able area 1 block smaller, so make the roof stay inside
        Rect2i lotRc = parcel.getShape().expand(new Vector2i(-1, -1));

        int centerX = lotRc.minX() + IntMath.divide(lotRc.width(), 2, RoundingMode.HALF_DOWN); // width() is 1 too much
        int centerY = lotRc.minY() + IntMath.divide(lotRc.height(), 2, RoundingMode.HALF_DOWN);

        int towerSize = Math.min(lotRc.width(), lotRc.height());
        int towerRad = towerSize / 2 - 1;

        int entranceHeight = 2;
        ImmutableVector2i doorPos = new ImmutableVector2i(centerX + towerRad, centerY);
        Orientation orient = Orientation.EAST;

        ImmutableVector2i doorDir = orient.getDir();
        Vector2i probePos = new Vector2i(doorPos.x() + doorDir.getX(), doorPos.y() + doorDir.getY());

        int baseHeight = TeraMath.floorToInt(hm.apply(probePos)) + 1;
        int sideHeight = 4;

        SimpleRoundHouse house = new SimpleRoundHouse(orient, new ImmutableVector2i(centerX, centerY), towerRad, baseHeight, sideHeight);

        SimpleDoor entrance = new SimpleDoor(orient, doorPos, baseHeight, baseHeight + entranceHeight);
        house.getRoom().addDoor(entrance);

        int wndOff = 1;
        ImmutableVector2i wndPos1 = new ImmutableVector2i(centerX - towerRad, centerY);
        ImmutableVector2i wndPos2 = new ImmutableVector2i(centerX, centerY - towerRad);
        ImmutableVector2i wndPos3 = new ImmutableVector2i(centerX, centerY + towerRad);
        SimpleWindow wnd1 = new SimpleWindow(Orientation.WEST, wndPos1, baseHeight + wndOff);
        SimpleWindow wnd2 = new SimpleWindow(Orientation.NORTH, wndPos2, baseHeight + wndOff);
        SimpleWindow wnd3 = new SimpleWindow(Orientation.SOUTH, wndPos3, baseHeight + wndOff);

        house.getRoom().addWindow(wnd1);
        house.getRoom().addWindow(wnd2);
        house.getRoom().addWindow(wnd3);

        return house;
    }

}
