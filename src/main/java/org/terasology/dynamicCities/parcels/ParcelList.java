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


import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Rect2i;

import java.util.ArrayList;
import java.util.List;

public class ParcelList implements Component {

    public int residentialArea;
    public int commercialArea;
    public int militaryArea;
    public int clericalArea;
    public int governmentalArea;

    public int minBuildRadius;
    public List<DynParcel> parcels;

    public ParcelList() { }
    public ParcelList(int i) {
        minBuildRadius = 0;
        parcels = new ArrayList<>();
    }

    public void addParcel(DynParcel parcel) {
        parcels.add(parcel);
        switch (parcel.getZoneDyn()) {
            case CLERICAL:      clericalArea += parcel.getShape().area();
                                break;
            case RESIDENTIAL:   residentialArea += parcel.getShape().area();
                                break;
            case COMMERCIAL:    commercialArea += parcel.getShape().area();
                                break;
            case GOVERNMENTAL:  governmentalArea += parcel.getShape().area();
                                break;
            case MILITARY:      militaryArea += parcel.getShape().area();
                                break;
            default:            break;
        }
    }

    public boolean isNotIntersecting(DynParcel parcel) {
        return isNotIntersecting(parcel.shape);
    }

    public boolean isNotIntersecting(Rect2i rect) {
        for (DynParcel spawnedParcels : parcels) {
            if (spawnedParcels.getShape().overlaps(rect)) {
               return false;
            }
        }
        return true;
    }

}
