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

package org.terasology.dynamicCities.parcels;

import org.terasology.cities.parcels.Parcel;
import org.terasology.commonworld.Orientation;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;

/**
 * A parcel where buildings can be placed on.
 */

public class DynParcel implements Parcel {

    public int height;
    public String zone;

    public BlockArea shape = new BlockArea(BlockArea.INVALID);
    public Orientation orientation;
    public String buildingTypeName;
    public EntityRef buildingEntity;
    /**
     * Try to resolve differences between Cities parcels and DynParcel, especially the zone...
     * @param shape the shape of the lot
     * @param orientation the orientation of the parcel (e.g. towards the closest street)
     */
    public DynParcel(BlockAreac shape, Orientation orientation, String zone, int height) {
        this.zone = zone;
        this.shape.set(shape);
        this.orientation = orientation;
        this.height = height;
    }


    /**
     * @return the layout shape
     */
    @Override
    public BlockAreac getShape() {
        return this.shape;
    }

    /**
     * @return the orientation of the parcel
     */
    @Override
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * @return the zone type that was assigned to this parcel
     */

    public String getZone() {
        return zone;
    }

    public int getHeight() {
        return height;
    }

    public void setBuildingTypeName(String name) {
        buildingTypeName = name;
    }


}
