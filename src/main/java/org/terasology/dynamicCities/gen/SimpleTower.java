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

import org.terasology.cities.bldg.DefaultBuilding;
import org.terasology.cities.bldg.RectBuildingPart;
import org.terasology.cities.bldg.StaircaseBuildingPart;
import org.terasology.cities.bldg.Tower;
import org.terasology.cities.common.Edges;
import org.terasology.cities.door.SimpleDoor;
import org.terasology.cities.model.roof.BattlementRoof;
import org.terasology.cities.model.roof.Roof;
import org.terasology.commonworld.Orientation;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;

/**
 * A simple tower
 */
public class SimpleTower extends DefaultBuilding implements Tower {

    private Rect2i shape;
    private RectBuildingPart room;

    /**
     * @param orient the orientation of the building
     * @param layout the building layout
     * @param baseHeight the height of the floor level
     * @param wallHeight the building height above the floor level
     */
    public SimpleTower(Orientation orient, Rect2i layout, int baseHeight, int wallHeight) {
        super(orient);
        this.shape = layout;

        Rect2i roofArea = layout.expand(new Vector2i(1, 1));
        Roof roof = new BattlementRoof(layout, roofArea, baseHeight + wallHeight, 1);
        room = new StaircaseBuildingPart(layout, orient, roof, baseHeight, wallHeight);
        Vector2i doorPos = new Vector2i(Edges.getCorner(layout, orient));
        room.addDoor(new SimpleDoor(orient, doorPos, baseHeight, baseHeight + 2));
        addPart(room);
    }

    public Rect2i getShape() {
        return shape;
    }

    public RectBuildingPart getStaircase() {
        return room;
    }

}
