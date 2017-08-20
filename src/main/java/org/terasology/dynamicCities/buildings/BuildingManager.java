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
import org.terasology.commonworld.Orientation;
import org.terasology.context.Context;
import org.terasology.dynamicCities.gen.GeneratorRegistry;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.population.CultureComponent;
import org.terasology.dynamicCities.utilities.Toolbox;
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
import java.util.stream.Collectors;

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

    private List<BuildingGenerator> generators = new ArrayList<>();
    private Map<String, List<Vector2i>> minMaxSizePerZone = new HashMap<>();
    private final int maxIterationsForBuildingSelection = 100;
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
                if (building.zone == null) {
                    logger.warn("Invalid zone type found for prefab " + prefab.getName() + ". Skipping building");
                    continue;
                }

                building.resourceUrn = prefab.getUrn().toString();
                building.name = building.name.toLowerCase();
                building.zone = building.zone.toLowerCase();
                if (building.generatorNames != null) {
                    Toolbox.stringsToLowerCase(building.generatorNames);
                }
                if (building.templateNames != null) {
                    Toolbox.stringsToLowerCase(building.templateNames);
                }
                if (buildings.containsKey(prefab.getName().toLowerCase())) {
                    logger.warn("Overwritten building with name " + prefab.getName());
                }
                buildings.put(building.zone, building);
                logger.info("Loaded building prefab " + prefab.getName());
            }

            //Get Templates
            if (prefab.hasComponent(SpawnBlockRegionsComponent.class)) {
                EntityRef template = entityManager.create(prefab);
                if (templates.containsKey(prefab.getName().toLowerCase())) {
                    logger.warn("Overwritten template with name " + prefab.getName());
                }
                templates.put(prefab.getName().toLowerCase(), template);
                logger.info("StructuredTemplate " + prefab.getName() + " loaded successfully.");

            }
        }
        logger.info("Finished loading buildings. Number of building types: " + buildings.values().size() + " | Strings found: " + buildings.keySet().toString());


        // Initialising minMaxSizes
        for (String zone : getZones()) {
            minMaxSizePerZone.put(zone, getMinMaxForZone(zone));
        }

        CommercialBuildingGenerator commercialBuildingGenerator = new CommercialBuildingGenerator(worldProvider.getSeed().hashCode() / 10);
        RectHouseGenerator rectHouseGenerator = new RectHouseGenerator();
        SimpleChurchGenerator simpleChurchGenerator = new SimpleChurchGenerator(worldProvider.getSeed().hashCode() / 7);
        TownHallGenerator townHallGenerator = new TownHallGenerator();

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
            int max = buildings.get(zone).size();
            GenericBuildingComponent building;
            int iter = 0;
            do {
                int index = rng.nextInt(max);
                building = (GenericBuildingComponent) buildings.get(zone).toArray()[index];
                iter++;
            } while ((building.minSize.x > shape.minX() || building.minSize.y > shape.minY()
                    || building.maxSize.x < shape.maxX() || building.maxSize.y < shape.maxY()) && iter < 100);
            if (iter >= 99) {
                logger.error("No building types found for " + zone + "because no matching building for parcel " + shape.toString() +  " was found!");
                return Optional.empty();
            }
            return Optional.of(building);
        }
        logger.warn("No building types found for " + zone);
        return Optional.empty();
    }

    public Optional<GenericBuildingComponent> getRandomBuildingOfZoneForCulture(String zone, Rect2i shape, CultureComponent cultureComponent) {
        if (buildings.containsKey(zone)) {
            // TODO: needs to be adapted if buildings have a specific spawn chance
            List<GenericBuildingComponent> availableBuildings =
                    buildings.get(zone).stream().filter(b -> cultureComponent.availableBuildings.contains(b.name)).collect(Collectors.toList());
            int numberOfBuildingsForZone = availableBuildings.size();

            GenericBuildingComponent selectedBuilding = null;
            int iter = 0;
            int index;
            do { // improve iteration by removing non-fitting buildings from the list
                index = rng.nextInt(numberOfBuildingsForZone);
                GenericBuildingComponent candidateBuilding = availableBuildings.get(index);
                if (isFitting(shape, candidateBuilding)) {
                    selectedBuilding = candidateBuilding;
                } else {
                    availableBuildings.remove(candidateBuilding);
                    numberOfBuildingsForZone--;
                }
                iter++;
            } while (!availableBuildings.isEmpty() && selectedBuilding == null && iter < maxIterationsForBuildingSelection);

            if (selectedBuilding == null) { // no building was found - try to find alternative
                GenericBuildingComponent biggestFittingBuilding = findBiggestFittingBuilding(shape, zone);
                if (biggestFittingBuilding != null) {
                    selectedBuilding = entityManager.getComponentLibrary().copy(biggestFittingBuilding);
                    selectedBuilding.isScaledDown = true;
                }
            }

            if (selectedBuilding != null) {
                logger.debug("Found building \"{}\" for zone \"{}\" and size ({}, {})", selectedBuilding.name, zone, shape.width(), shape.height());
                return Optional.of(entityManager.getComponentLibrary().copy(selectedBuilding));
            }
        }
        logger.warn("No building types found for zone \"{}\" with size ({}, {})", zone, shape.width(), shape.height());
        return Optional.empty();
    }


    private Optional<BuildingGenerator> getGenerator(String generatorName) {
        Class generatorClass = GeneratorRegistry.GENERATORS.get(generatorName);
        for (BuildingGenerator generator : generators) {
            if (generator.getClass() == generatorClass) {
                return Optional.of(generator);
            }
        }
        logger.error("No generator found with identifier " + generatorName);
        return Optional.empty();
    }

    private Optional<EntityRef> getTemplate(String templateName) {
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

    private List<Vector2i> getMinMaxForZone(String zone) {
        int minX = 9999;
        int maxX = 0;
        int minY = 9999;
        int maxY = 0;

        for (GenericBuildingComponent building : buildings.get(zone)) {
            int minTempX = building.minSize.x;
            int minTempY = building.minSize.y;
            int maxTempX = building.maxSize.x;
            int maxTempY = building.maxSize.y;

            minX = (minTempX < minX) ? minTempX : minX;
            maxX = (maxTempX > maxX) ? maxTempX : maxX;
            minY = (minTempY < minY) ? minTempY : minY;
            maxY = (maxTempY > maxY) ? maxTempY : maxY;
        }
        if (minX == 9999 || maxX == 0 || minY == 9999 || maxY == 0) {
            logger.error("Could not find valid min and/or max building sizes for zone " + zone);
        }

        List<Vector2i> minMaxSize = new ArrayList<>(2);
        minMaxSize.add(new Vector2i(minX, minY));
        minMaxSize.add(new Vector2i(maxX, maxY));
        return minMaxSize;
    }

    public Map<String, List<Vector2i>> getMinMaxSizePerZone() {
        return Collections.unmodifiableMap(minMaxSizePerZone);
    }

    public Set<String> getZones() {
        return buildings.keySet();
    }

    private GenericBuildingComponent findBiggestFittingBuilding(Rect2i shape, String zone) {
        int maxX = 0;
        int maxY = 0;
        GenericBuildingComponent fittingBuilding = null;
        for (GenericBuildingComponent buildingComponent : buildings.get(zone)) {
            int tempX = buildingComponent.minSize.x;
            int tempY = buildingComponent.minSize.y;

            if (tempY <= shape.sizeY() && tempX <= shape.sizeX()) {
                if (maxX < tempX && maxY < tempY) {
                    maxX = tempX;
                    maxY = tempY;
                    fittingBuilding = buildingComponent;
                }
            }
        }

        return fittingBuilding;
    }

    private boolean isFitting(Rect2i shape, GenericBuildingComponent building) {
        boolean checkNorthSouth = building.minSize.x < shape.sizeX() && building.minSize.y < shape.sizeY()
                && building.maxSize.x > shape.sizeX() && building.maxSize.y > shape.sizeY();
        boolean checkEastWest = building.minSize.x < shape.sizeY() && building.minSize.y < shape.sizeX()
                && building.maxSize.x > shape.sizeY() && building.maxSize.y > shape.sizeX();
        return checkEastWest || checkNorthSouth;
    }

    //Checks whether the building needs to be rotated in order to fit on the parcel
    public boolean needsRotation(DynParcel parcel, GenericBuildingComponent building) {
        Rect2i shape = parcel.shape;
        Orientation orientation = parcel.orientation;
        if (orientation == Orientation.NORTH || orientation == Orientation.SOUTH) {
            return !(building.minSize.x < shape.sizeX() && building.minSize.y < shape.sizeY()
                    && building.maxSize.x > shape.sizeX() && building.maxSize.y > shape.sizeY());
        } else {
            return building.minSize.x < shape.sizeX() && building.minSize.y < shape.sizeY()
                    && building.maxSize.x > shape.sizeX() && building.maxSize.y > shape.sizeY();
        }
    }
}
