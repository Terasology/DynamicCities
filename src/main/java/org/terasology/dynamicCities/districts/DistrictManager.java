// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.districts;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.utilities.Toolbox;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.gestalt.assets.management.AssetManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Share(DistrictManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class DistrictManager extends BaseComponentSystem {

    private final Logger logger = LoggerFactory.getLogger(DistrictManager.class);
    private final Set<DistrictType> districts = new HashSet<>();

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
