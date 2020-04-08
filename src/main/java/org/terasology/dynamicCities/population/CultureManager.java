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
package org.terasology.dynamicCities.population;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.dynamicCities.utilities.Toolbox;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.utilities.random.MersenneRandom;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Share(CultureManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class CultureManager extends BaseComponentSystem {
    private Logger logger = LoggerFactory.getLogger(CultureComponent.class);
    private Set<CultureComponent> cultureComponents = new HashSet<>();
    private MersenneRandom rng;
    @In
    private AssetManager assetManager;

    @Override
    public void postBegin() {
        logger.info("Obtaining culture prefabs...");
        Set<Prefab> prefabs = assetManager.getLoadedAssets(Prefab.class);
        for (Prefab prefab : prefabs) {
            //Get building data
            if (prefab.hasComponent(CultureComponent.class)) {
                CultureComponent cultureComponent = prefab.getComponent(CultureComponent.class);
                if (cultureComponent.theme != null) {
                    cultureComponent.theme = cultureComponent.theme.toLowerCase();
                } else {
                    logger.warn("No theme defined for culture " + cultureComponent.name);
                }
                if (!cultureComponent.buildingNeedPerZone.isEmpty()) {
                    cultureComponents.add(cultureComponent);
                    cultureComponent.buildingNeedPerZone = Toolbox.stringsToLowerCase(cultureComponent.buildingNeedPerZone);
                } else {
                    logger.warn("Found culture prefab with empty buildingNeedPerZone list");
                }
                if (cultureComponent.availableBuildings != null) {
                    Toolbox.stringsToLowerCase(cultureComponent.availableBuildings);
                } else {
                    logger.warn("No available Buildings defined for culture " + cultureComponent.name);
                }
                if (cultureComponent.residentialZones != null) {
                    Toolbox.stringsToLowerCase(cultureComponent.residentialZones);
                } else {
                    logger.warn("No residential zones defined for culture " + cultureComponent.name);
                }
            }
        }

        String cultureNames = cultureComponents
                .stream()
                .map(c -> c.name)
                .collect(Collectors.joining(", ", "[", "]"))
                .toString();

        logger.info("Finished loading cultures: " + cultureComponents.size() + " culture types found: " + cultureNames);
        rng = new MersenneRandom(assetManager.hashCode() * 5 + this.hashCode());
    }

    public CultureComponent getRandomCulture() {
        if (!cultureComponents.isEmpty()) {
            int max = cultureComponents.size();
            int index = rng.nextInt(max);
            return (CultureComponent) cultureComponents.toArray()[index];
        }
        logger.error("No culture found...barbarians..." );
        return null;
    }


}
