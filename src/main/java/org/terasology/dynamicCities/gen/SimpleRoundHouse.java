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
import org.terasology.cities.bldg.RoundBuildingPart;
import org.terasology.cities.model.roof.ConicRoof;
import org.terasology.commonworld.Orientation;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Circle;

/**
 * A round house with a conic roof
 */
public class SimpleRoundHouse extends DefaultBuilding {

    private Circle layout;
    private RoundBuildingPart room;

    /**
     * @param orient the orientation of the building
     * @param center the center of the tower
     * @param radius the radius
     * @param baseHeight the height of the floor level
     * @param wallHeight the building height above the floor level
     */
    public SimpleRoundHouse(Orientation orient, BaseVector2i center, int radius, int baseHeight, int wallHeight) {
        super(orient);

        layout = new Circle(center.x(), center.y(), radius);
        room = new RoundBuildingPart(
                layout,
                new ConicRoof(center, radius + 1, baseHeight + wallHeight, 1),
                baseHeight,
                wallHeight);
        addPart(room);
    }

    public Circle getShape() {
        return layout;
    }

    public RoundBuildingPart getRoom() {
        return room;
    }
}
