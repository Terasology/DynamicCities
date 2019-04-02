/*
 * Copyright 2019 MovingBlocks
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
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.namegenerator.town.DebugTownTheme;
import org.terasology.namegenerator.town.TownAssetTheme;
import org.terasology.namegenerator.town.TownTheme;
import org.terasology.registry.In;
import org.terasology.registry.Share;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Share(ThemeManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class ThemeManager extends BaseComponentSystem {
    private Logger logger = LoggerFactory.getLogger(ThemeManager.class);

    @In
    AssetManager assetManager;

    private Set<TownNameComponent> themeComponents = new HashSet<>();
    private Map<String, TownTheme> themes = new HashMap<>();

    @Override
    public void postBegin() {
        logger.info("Loading default themes...");

        themes.put("english", TownAssetTheme.ENGLISH);
        themes.put("fantasy", TownAssetTheme.FANTASY);

        logger.info("Obtaining town theme prefabs...");
        Set<Prefab> prefabs = assetManager.getLoadedAssets(Prefab.class);
        for (Prefab prefab : prefabs) {
            // Get theme data
            if (prefab.hasComponent(TownNameComponent.class)) {
                TownNameComponent component = prefab.getComponent(TownNameComponent.class);
                themeComponents.add(component);

                component.name = component.name.toLowerCase();
                themes.put(component.name, new TownTheme() {
                    @Override
                    public List<String> getNames() {
                        return component.nameList;
                    }

                    @Override
                    public List<String> getPrefixes() {
                        return component.prefixes;
                    }

                    @Override
                    public List<String> getPostfixes() {
                        return component.postfixes;
                    }
                });
            }
        }

        String themeNames = "[";
        Iterator<TownNameComponent> iterator = themeComponents.iterator();
        while (iterator.hasNext()) {
            themeNames += iterator.next().name;
            if (iterator.hasNext()) {
                themeNames += ", ";
            }
        }
        themeNames += "]";
        logger.info("Finished loading themes: " + themeComponents.size() + " theme types found: " + themeNames);
    }

    public TownTheme getTownTheme(String key) {
        return themes.getOrDefault(key, new DebugTownTheme());
    }
}
