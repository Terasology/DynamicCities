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
package org.terasology.dynamicCities.districts;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.dynamicCities.utilities.Toolbox;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Share(DistrictManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class DistrictManager extends BaseComponentSystem {

    private Logger logger = LoggerFactory.getLogger(DistrictManager.class);
    private Set<DistrictType> districts = new HashSet<>();

    @In
    private AssetManager assetManager;

    @Override
    public void postBegin() {
        logger.info("Obtaining district prefabs...");
        Set<Prefab> prefabs = assetManager.getLoadedAssets(Prefab.class);
        for (Prefab prefab : prefabs) {
            //Get building data
            if (prefab.hasComponent(DistrictType.class)) {
                DistrictType districtType = prefab.getComponent(DistrictType.class);
                if (!districtType.zones.isEmpty()) {
                    Toolbox.stringsToLowerCase(districtType.zones);
                    districts.add(districtType);
                } else {
                    logger.warn("Found district prefab with empty zone list");
                }
            }
        }

        String districtNames = "[";
        Iterator<DistrictType> iter = districts.iterator();
        while (iter.hasNext()) {
            districtNames += iter.next().name;
            if (iter.hasNext()) {
                districtNames += ", ";
            }
        }
        districtNames += "]";
        logger.info("Finished loading districts: " + districts.size() + " district types found: " + districtNames);
    }

    public Optional<DistrictType> getDistrictFromName(String name) {
        for (DistrictType districtType : districts) {
            if (districtType.name.equals(name)) {
                return Optional.of(districtType);
            }
        }
        logger.warn("No district found with name " + name);
        return Optional.empty();
    }

    public List<DistrictType> getDistrictTypes() {
        return new ArrayList<>(districts);
    }
}
