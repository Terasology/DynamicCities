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
import org.terasology.cities.bldg.gen.*;
import org.terasology.context.Context;
import org.terasology.dynamicCities.gen.DefaultBuildingGenerator;
import org.terasology.dynamicCities.gen.GeneratorRegistry;
import org.terasology.dynamicCities.parcels.Zone;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.utilities.random.MersenneRandom;
import org.terasology.world.WorldProvider;

import java.util.*;

/**
 * This is used to keep track of possible buildings, their construction plans and attributes
 */

//TODO: If a generator name is mispelled get another building type and remove that.

@Share(value = BuildingManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class BuildingManager extends BaseComponentSystem {

    private Multimap<Zone, GenericBuildingComponent> buildings = MultimapBuilder.enumKeys(Zone.class).hashSetValues().build();
    private Logger logger = LoggerFactory.getLogger(BuildingManager.class);
    private MersenneRandom rng;
    private Map<String, EntityRef> templates = new HashMap<>();

    private CommercialBuildingGenerator commercialBuildingGenerator;
    private RectHouseGenerator rectHouseGenerator;
    private SimpleChurchGenerator simpleChurchGenerator;
    private TownHallGenerator townHallGenerator;
    private DefaultBuildingGenerator defaultBuildingGenerator;

    private List<BuildingGenerator> generators = new ArrayList<>();

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
            if (prefab.hasComponent(GenericBuildingComponent.class)) {
                GenericBuildingComponent building = prefab.getComponent(GenericBuildingComponent.class);
                if(Zone.valueOf(building.zone) == null) {
                    logger.warn("Invalid zone type found for prefab " + prefab.getName() + ". Skipping building");
                    continue;
                }
                buildings.put(Zone.valueOf(building.zone), building);
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
        logger.info("Finished loading buildings. Number of building types: " + buildings.values().size() + " | Zones found: " + buildings.keySet().toString());


        commercialBuildingGenerator = new CommercialBuildingGenerator(worldProvider.getSeed().hashCode() / 10);
        rectHouseGenerator = new RectHouseGenerator();
        simpleChurchGenerator = new SimpleChurchGenerator(worldProvider.getSeed().hashCode() / 7);
        townHallGenerator = new TownHallGenerator();
        defaultBuildingGenerator = new DefaultBuildingGenerator(worldProvider.getSeed().hashCode() / 3);

        generators.add(commercialBuildingGenerator);
        generators.add(rectHouseGenerator);
        generators.add(simpleChurchGenerator);
        generators.add(townHallGenerator);

        rng = new MersenneRandom(assetManager.hashCode() + buildings.hashCode());
    }

    public List<GenericBuildingComponent> getBuildingsOfZone(Zone zone) {
        return new ArrayList<>(buildings.get(zone));
    }

    public Optional<GenericBuildingComponent> getRandomBuildingOfZone(Zone zone) {
        if (buildings.containsKey(zone)) {
            int max = buildings.get(zone).size();
            int index = rng.nextInt(max);
            return Optional.of((GenericBuildingComponent) buildings.get(zone).toArray()[index]);
        }
        logger.warn("No building types found for " + zone.toString());
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
}
