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
package org.terasology.dynamicCities.region;


import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector2i;

import java.util.HashMap;
import java.util.Map;

public class RegionEntities implements Component {

    private Map<Vector2i, EntityRef> regionEntities;

    public RegionEntities() {
        regionEntities = new HashMap<>();
    }

    public void add(EntityRef region) {
        LocationComponent location = region.getComponent(LocationComponent.class);
        Vector2i position = new Vector2i(location.getWorldPosition().x(), location.getWorldPosition().z());
        regionEntities.put(position, region);
    }

    public EntityRef get(Vector2i position) {
        return regionEntities.get(position);
    }
}
