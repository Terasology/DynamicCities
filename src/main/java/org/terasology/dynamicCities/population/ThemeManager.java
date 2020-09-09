// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.population;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.namegenerator.town.DebugTownTheme;
import org.terasology.namegenerator.town.TownAssetTheme;
import org.terasology.namegenerator.town.TownTheme;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This class loads a city's theme, which is a naming scheme for the city. A theme can be defined as a list of possible
 * names in a prefab.
 */
@Share(ThemeManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class ThemeManager extends BaseComponentSystem {
    private final Logger logger = LoggerFactory.getLogger(ThemeManager.class);
    private final Set<TownNameComponent> townNameSchemes = new HashSet<>();
    private final Map<String, TownTheme> themes = new HashMap<>();
    @In
    AssetManager assetManager;

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
                townNameSchemes.add(component);

                component.themeName = component.themeName.toLowerCase();
                themes.put(component.themeName, new TownTheme() {
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

        String themeNames = townNameSchemes
                .stream()
                .map(c -> c.themeName)
                .collect(Collectors.joining(", ", "[", "]"));

        logger.info("Finished loading themes: " + townNameSchemes.size() + " theme types found: " + themeNames);
    }

    public TownTheme getTownTheme(String key) {
        return themes.getOrDefault(key, new DebugTownTheme());
    }
}
