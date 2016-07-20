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
package org.terasology.dynamicCities.buildings;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.cities.bldg.gen.BuildingGenerator;
import org.terasology.cities.bldg.gen.CommercialBuildingGenerator;
import org.terasology.cities.bldg.gen.RectHouseGenerator;
import org.terasology.cities.bldg.gen.SimpleChurchGenerator;
import org.terasology.cities.bldg.gen.TownHallGenerator;
import org.terasology.context.Context;
import org.terasology.dynamicCities.gen.GeneratorRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.utilities.random.MersenneRandom;
import org.terasology.world.WorldProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is used to keep track of possible buildings, their construction plans and attributes
 */

//TODO: If a generator name is mispelled get another building type and remove that.

@Share(value = BuildingManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class BuildingManager extends BaseComponentSystem {

    private Multimap<String, GenericBuildingComponent> buildings = MultimapBuilder.hashKeys().hashSetValues().build();
    private Logger logger = LoggerFactory.getLogger(BuildingManager.class);
    private MersenneRandom rng;
    private Map<String, EntityRef> templates = new HashMap<>();

    private CommercialBuildingGenerator commercialBuildingGenerator;
    private RectHouseGenerator rectHouseGenerator;
    private SimpleChurchGenerator simpleChurchGenerator;
    private TownHallGenerator townHallGenerator;

    private List<BuildingGenerator> generators = new ArrayList<>();
    private Map<String, Vector2i> minMaxSizePerZone = new HashMap<>();

    @In
    private Context context;

    @In
    private AssetManager assetManager;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @Override
    public void postBegin() {
        logger.info("Loading building assets");
        Set<Prefab> loadedPrefabs = assetManager.getLoadedAssets(Prefab.class);
        for (Prefab prefab : loadedPrefabs) {
            //Get building data
            if (prefab.hasComponent(GenericBuildingComponent.class)) {
                GenericBuildingComponent building = prefab.getComponent(GenericBuildingComponent.class);
                if(building.zone == null) {
                    logger.warn("Invalid zone type found for prefab " + prefab.getName() + ". Skipping building");
                    continue;
                }
                buildings.put(building.zone, building);
                logger.info("Loaded building prefab " + prefab.getName());
            }

            //Get Templates
            if (prefab.hasComponent(SpawnBlockRegionsComponent.class)) {
                if (prefab.getName().contains("DynamicCities")) {
                    EntityRef template = entityManager.create(prefab);
                    templates.put(prefab.getName().split(":")[1], template);
                    logger.info("StructuredTemplate " + prefab.getName() + " loaded successfully.");
                }
            }
        }
        logger.info("Finished loading buildings. Number of building types: " + buildings.values().size() + " | Strings found: " + buildings.keySet().toString());


        /**
         * Initialising minMaxSizes
         */
        for(String zone : getZones()) {
            minMaxSizePerZone.put(zone, getMinMaxForZone(zone));
        }
        commercialBuildingGenerator = new CommercialBuildingGenerator(worldProvider.getSeed().hashCode() / 10);
        rectHouseGenerator = new RectHouseGenerator();
        simpleChurchGenerator = new SimpleChurchGenerator(worldProvider.getSeed().hashCode() / 7);
        townHallGenerator = new TownHallGenerator();

        generators.add(commercialBuildingGenerator);
        generators.add(rectHouseGenerator);
        generators.add(simpleChurchGenerator);
        generators.add(townHallGenerator);

        rng = new MersenneRandom(assetManager.hashCode() + buildings.hashCode());
    }

    public List<GenericBuildingComponent> getBuildingsOfZone(String zone) {
        return new ArrayList<>(buildings.get(zone));
    }

    public Optional<GenericBuildingComponent> getRandomBuildingOfZone(String zone) {
        if (buildings.containsKey(zone)) {
            int max = buildings.get(zone).size();
            int index = rng.nextInt(max);
            return Optional.of((GenericBuildingComponent) buildings.get(zone).toArray()[index]);
        }
        logger.warn("No building types found for " + zone);
        return Optional.empty();
    }

    public Optional<GenericBuildingComponent> getRandomBuildingOfZone(String zone, Rect2i shape) {
        if (buildings.containsKey(zone)) {
            int parcelSize = shape.sizeX() * shape.sizeY();
            int max = buildings.get(zone).size();
            GenericBuildingComponent building;
            int iter = 0;
            do {
                int index = rng.nextInt(max);
                building = (GenericBuildingComponent) buildings.get(zone).toArray()[index];
                iter++;
            } while ((building.minSize > parcelSize || building.maxSize < parcelSize) && iter < 100);
            if (iter >= 99) {
                logger.error("No building types found for " + zone + "because no matching building for parcel size " + parcelSize + " was found!");
                return Optional.empty();
            }
            return Optional.of(building);
        }
        logger.warn("No building types found for " + zone);
        return Optional.empty();
    }


    public Optional<BuildingGenerator> getGenerator(String generatorName) {
        Class generatorClass = GeneratorRegistry.GENERATORS.get(generatorName);
        for(BuildingGenerator generator : generators) {
            if (generator.getClass() == generatorClass) {
                return Optional.of(generator);
            }
        }
        logger.error("No generator found with identifier " + generatorName);
        return Optional.empty();
    }

    public Optional<EntityRef> getTemplate(String templateName) {
        if (templates.containsKey(templateName)) {
            return Optional.of(templates.get(templateName));
        } else {
            logger.error("No template found with name " + templateName);
            return Optional.empty();
        }
    }

    public Optional<List<EntityRef>> getTemplatesForBuilding(GenericBuildingComponent building) {
        List<EntityRef> templateList = new ArrayList<>();

        if (building.templateNames == null) {
            return Optional.empty();
        }

        for (String name : building.templateNames) {
            Optional<EntityRef> templateOptional = getTemplate(name);
            if (templateOptional.isPresent()) {
                templateList.add(templateOptional.get());
            }

        }
        return Optional.of(templateList);
    }

    public Optional<List<BuildingGenerator>> getGeneratorsForBuilding(GenericBuildingComponent building) {
        List<BuildingGenerator> generatorList = new ArrayList<>();

        if (building.generatorNames == null) {
            return Optional.empty();
        }
        for (String name : building.generatorNames) {
            Optional<BuildingGenerator> generatorOptional = getGenerator(name);
            if (generatorOptional.isPresent()) {
                generatorList.add(generatorOptional.get());
            }

        }
        return Optional.of(generatorList);
    }

    private Vector2i getMinMaxForZone(String zone) {
        int min = 9999;
        int max = 0;

        for (GenericBuildingComponent building : buildings.get(zone)) {
            int minTemp = building.minSize;
            int maxTemp = building.maxSize;

            min = (minTemp < min) ? minTemp : min;
            max = (maxTemp > max) ? maxTemp : max;
        }
        if (min == 9999 || max == 0) {
            logger.error("Could not find valid min and/or max building sizes for zone " + zone);
        }
        return new Vector2i(min, max);
    }

    public Map<String, Vector2i> getMinMaxSizePerZone() {
        return Collections.unmodifiableMap(minMaxSizePerZone);
    }

    public Set<String> getZones() {
        return buildings.keySet();
    }
}
