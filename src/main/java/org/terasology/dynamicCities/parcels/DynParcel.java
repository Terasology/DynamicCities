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
import org.terasology.dynamicCities.buildings.GenericBuildingData;
import org.terasology.math.geom.Rect2i;

import java.util.HashSet;
import java.util.Set;

/**
 * A parcel where buildings can be placed on.
 */

public class DynParcel implements Parcel {

    public Set<GenericBuildingData> genericBuildingData;
    public int height;
    public Zone zone;

    public Rect2i shape;
    public final Orientation orientation;

    /**
     * Try to resolve differences between Cities parcels and DynParcel, especially the zone...
     * @param shape the shape of the lot
     * @param orientation the orientation of the parcel (e.g. towards the closest street)
     */
    public DynParcel(Rect2i shape, Orientation orientation, Zone zone, int height) {
        this.zone = zone;
        this.shape = shape;
        this.orientation = orientation;
        this.height = height;

        genericBuildingData = new HashSet<>();
    }


    /**
     * @return the layout shape
     */
    @Override
    public Rect2i getShape() {
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

    public Zone getZone() {
        return zone;
    }

    public int getHeight() {
        return height;
    }

    /**
     * @param bldg the genericBuildingData to add
     */
    public void addGenericBuilding(GenericBuildingData bldg) {
        genericBuildingData.add(bldg);
    }

    /**
     * @return an unmodifiable view on all buildings in this lot
     */
    public Set<GenericBuildingData> getGenericBuildings() {
        return genericBuildingData;
    }

    public DynParcel copy() {
        DynParcel parcel = new DynParcel(shape, orientation, zone, height);
        parcel.genericBuildingData = genericBuildingData;
        return parcel;

    }


}
