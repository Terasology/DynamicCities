// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.cities.bldg.gen.BuildingGenerator;
import org.terasology.cities.bldg.gen.CommercialBuildingGenerator;
import org.terasology.cities.bldg.gen.RectHouseGenerator;
import org.terasology.cities.bldg.gen.SimpleChurchGenerator;
import org.terasology.cities.bldg.gen.TownHallGenerator;
import org.terasology.commonworld.Orientation;
import org.terasology.dynamicCities.gen.GeneratorRegistry;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.population.CultureComponent;
import org.terasology.dynamicCities.utilities.Toolbox;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.utilities.random.MersenneRandom;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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

    public Optional<GenericBuildingComponent> getRandomBuildingOfZone(String zone, BlockAreac shape) {
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

    public Optional<GenericBuildingComponent> getRandomBuildingOfZoneForCulture(String zone, BlockAreac shape, CultureComponent cultureComponent) {
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
                logger.debug("Found building \"{}\" for zone \"{}\" and size ({}, {})", selectedBuilding.name, zone, shape.getSizeX(), shape.getSizeY());
                return Optional.of(entityManager.getComponentLibrary().copy(selectedBuilding));
            }
        }
        logger.warn("No building types found for zone \"{}\" with size ({}, {})", zone, shape.getSizeX(), shape.getSizeY());
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

    /**
     * Attempt to resolve the given {@code names} with the given {@code resolver}.
     *
     * @param names the names to resolve with the resolver; may be null
     * @param resolver function to resolve an object of type {@code T} from a name
     * @param <T> the type of the object to resolve
     * @return {@link Optional#empty()} if {@code names} is null, or some list containing the resolved objects.
     */
    static <T> Optional<List<T>> resolveFromNames(List<String> names, Function<String, Optional<T>> resolver) {
        return Optional.ofNullable(names)
                .map(templateNames ->
                        templateNames.stream()
                                .map(resolver)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList())
                );
    }

    /**
     * Some list of entities describing structure templates if {@link GenericBuildingComponent#templateNames} is set, or {@link
     * Optional#empty()} otherwise.
     *
     * @param building the generic building component to derive the templates from
     */
    public Optional<List<EntityRef>> getTemplatesForBuilding(GenericBuildingComponent building) {
        return resolveFromNames(building.templateNames, this::getTemplate);
    }

    /**
     * Some list of building generators if {@link GenericBuildingComponent#generatorNames} is set, or {@link Optional#empty()} otherwise.
     *
     * @param building the generic building component to derive the generators from
     */
    public Optional<List<BuildingGenerator>> getGeneratorsForBuilding(GenericBuildingComponent building) {
        return resolveFromNames(building.generatorNames, this::getGenerator);
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

    private GenericBuildingComponent findBiggestFittingBuilding(BlockAreac shape, String zone) {
        int maxX = 0;
        int maxY = 0;
        GenericBuildingComponent fittingBuilding = null;
        for (GenericBuildingComponent buildingComponent : buildings.get(zone)) {
            int tempX = buildingComponent.minSize.x;
            int tempY = buildingComponent.minSize.y;

            if (tempY <= shape.getSizeY() && tempX <= shape.getSizeX()) {
                if (maxX < tempX && maxY < tempY) {
                    maxX = tempX;
                    maxY = tempY;
                    fittingBuilding = buildingComponent;
                }
            }
        }

        return fittingBuilding;
    }

    private boolean isFitting(BlockAreac shape, GenericBuildingComponent building) {
        boolean checkNorthSouth = building.minSize.x < shape.getSizeX() && building.minSize.y < shape.getSizeY()
                && building.maxSize.x > shape.getSizeX() && building.maxSize.y > shape.getSizeY();
        boolean checkEastWest = building.minSize.x < shape.getSizeY() && building.minSize.y < shape.getSizeX()
                && building.maxSize.x > shape.getSizeY() && building.maxSize.y > shape.getSizeX();
        return checkEastWest || checkNorthSouth;
    }

    //Checks whether the building needs to be rotated in order to fit on the parcel
    public boolean needsRotation(DynParcel parcel, GenericBuildingComponent building) {
        BlockAreac shape = parcel.shape;
        Orientation orientation = parcel.orientation;
        if (orientation == Orientation.NORTH || orientation == Orientation.SOUTH) {
            return !(building.minSize.x < shape.getSizeX() && building.minSize.y < shape.getSizeY()
                    && building.maxSize.x > shape.getSizeX() && building.maxSize.y > shape.getSizeY());
        } else {
            return building.minSize.x < shape.getSizeX() && building.minSize.y < shape.getSizeY()
                    && building.maxSize.x > shape.getSizeX() && building.maxSize.y > shape.getSizeY();
        }
    }
}
