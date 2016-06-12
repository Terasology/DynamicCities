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

import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.bldg.Building;
import org.terasology.cities.bldg.DefaultBuilding;
import org.terasology.cities.bldg.HollowBuildingPart;
import org.terasology.cities.common.Edges;
import org.terasology.cities.deco.SingleBlockDecoration;
import org.terasology.cities.model.roof.HipRoof;
import org.terasology.cities.parcels.Parcel;
import org.terasology.commonworld.Orientation;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.*;
import org.terasology.utilities.procedural.WhiteNoise;

/**
 *
 */
public class CommercialBuildingGenerator {

    private long seed;

    /**
     * @param seed
     */
    public CommercialBuildingGenerator(long seed) {
        this.seed = seed;
    }

    public Building generate(Parcel parcel, HeightMap hm) {
        Orientation o = parcel.getOrientation();
        DefaultBuilding bldg = new DefaultBuilding(o);

        Rect2i rc = parcel.getShape().expand(-4, -4);
        Rect2i roofRc = rc.expand(2, 2);

        int wallHeight = 8;
        int arcRadius = 4;

        int centerX = (rc.minX() + rc.maxX()) / 2;
        int centerY = (rc.minY() + rc.maxY()) / 2;
        int baseHeight = TeraMath.floorToInt(hm.apply(centerX, centerY)) + 1;
        int roofBaseHeight = baseHeight + wallHeight - 1; // 1 block overlap

        HipRoof roof = new HipRoof(roofRc, roofRc, roofBaseHeight, 0.5f, roofBaseHeight + 1);

        HollowBuildingPart hall = new HollowBuildingPart(rc, roof, baseHeight, wallHeight, arcRadius);
        bldg.addPart(hall);

        WhiteNoise noiseGen = new WhiteNoise(seed);

        float fillFactor = 0.3f;
        Rect2i storeRc = rc.expand(-3, -3);

        for (BaseVector2i v : storeRc.contents()) {
            if (noiseGen.noise(v.getX(), v.getY()) * 0.5f + 0.5f < fillFactor) {
                BaseVector3i pos = new ImmutableVector3i(v.getX(), baseHeight, v.getY());
                hall.addDecoration(new SingleBlockDecoration(DefaultBlockType.BARREL, pos, Side.FRONT));
            }
        }

        Rect2i inner = rc.expand(-1, -1);
        for (int i = 0; i < 4; i++) {
            Vector2i pos = Edges.getCorner(inner, Orientation.NORTHEAST.getRotated(i * 90));
            BaseVector3i pos3d = new ImmutableVector3i(pos.x(), roofBaseHeight - 2, pos.y());
            hall.addDecoration(new SingleBlockDecoration(DefaultBlockType.TORCH, pos3d, Side.FRONT));
        }

        return bldg;
    }

}
