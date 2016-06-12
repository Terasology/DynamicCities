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

import org.terasology.cities.bldg.Building;
import org.terasology.cities.bldg.DefaultBuilding;
import org.terasology.cities.bldg.RectBuildingPart;
import org.terasology.cities.common.Edges;
import org.terasology.cities.door.SimpleDoor;
import org.terasology.cities.model.roof.HipRoof;
import org.terasology.cities.model.roof.Roof;
import org.terasology.cities.parcels.Parcel;
import org.terasology.cities.surface.InfiniteSurfaceHeightFacet;
import org.terasology.cities.window.SimpleWindow;
import org.terasology.commonworld.Orientation;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;

/**
 *
 */
public class TownHallGenerator {

    public Building generate(Parcel parcel, InfiniteSurfaceHeightFacet hm) {

        Orientation o = parcel.getOrientation();
        DefaultBuilding bldg = new DefaultBuilding(o);

        Rect2i rc = parcel.getShape().expand(-2, -2);

        ImmutableVector2i doorDir = o.getOpposite().getDir();
        Vector2i doorPos = Edges.getCorner(rc, o.getOpposite());
        Vector2i probePos = new Vector2i(doorPos.getX() + doorDir.getX(), doorPos.getY() + doorDir.getY());

        Turtle turtle = new Turtle(doorPos, o);
        turtle.move(0, 2);
        int width = turtle.width(rc);
        int length = turtle.length(rc);

        width -= width % 6;
        length -= length % 6;

        // we add +1, because the building starts at 1 block above the terrain
        int floorHeight = TeraMath.floorToInt(hm.getWorld(probePos)) + 1;
        int wallHeight = 6;

        // Create entry hall
        Rect2i hallRect = turtle.rectCentered(0, width, length / 3);
        RectBuildingPart hall = createHall(hallRect, o, floorHeight, wallHeight);

        bldg.addPart(hall);

        Rect2i leftRect = turtle.rect(-width / 2, length / 3 - 1, width / 3, length / 3); // -1 to overlap
        Rect2i rightRect = turtle.rect(width * 1 / 6, length / 3 - 1, width / 3, length / 3); // -1 to overlap

        RectBuildingPart left = createHallway(leftRect, o, floorHeight, wallHeight - 2);
        RectBuildingPart right = createHallway(rightRect, o, floorHeight, wallHeight - 2);

        bldg.addPart(left);
        bldg.addPart(right);

        hallRect = turtle.rectCentered(length * 2 / 3 - 2, width, length / 3);
        hall = createHall(hallRect, o, floorHeight, wallHeight);

        bldg.addPart(hall);

        return bldg;
    }

    private RectBuildingPart createHall(Rect2i rc, Orientation o, int floorHeight, int wallHeight) {
        Roof roof = new HipRoof(rc, rc.expand(1, 1), floorHeight + wallHeight, 1f);
        RectBuildingPart hall = new RectBuildingPart(rc, roof, floorHeight, wallHeight);
        Vector2i doorPos = Edges.getCorner(rc, o.getOpposite());
        hall.addDoor(new SimpleDoor(o, doorPos, floorHeight, floorHeight + 2));
        hall.addWindow(new SimpleWindow(o.getRotated(90), Edges.getCorner(rc, o.getRotated(90)), floorHeight + 1));
        hall.addWindow(new SimpleWindow(o.getRotated(270), Edges.getCorner(rc, o.getRotated(270)), floorHeight + 1));
        hall.addWindow(new SimpleWindow(o, Edges.getCorner(rc, o), floorHeight + 1));
        return hall;
    }

    private RectBuildingPart createHallway(Rect2i rc, Orientation o, int floorHeight, int wallHeight) {
        HipRoof roof = new HipRoof(rc, rc, floorHeight + wallHeight, 1f);
        RectBuildingPart hallway = new RectBuildingPart(rc, roof, floorHeight, wallHeight);
        hallway.addDoor(new SimpleDoor(o, Edges.getCorner(rc, o.getOpposite()), floorHeight, floorHeight + 2));
        hallway.addDoor(new SimpleDoor(o, Edges.getCorner(rc, o), floorHeight, floorHeight + 2));
        hallway.addWindow(new SimpleWindow(o.getRotated(90), Edges.getCorner(rc, o.getRotated(90)), floorHeight + 1));
        hallway.addWindow(new SimpleWindow(o.getRotated(270), Edges.getCorner(rc, o.getRotated(270)), floorHeight + 1));
        return hallway;
    }

}
