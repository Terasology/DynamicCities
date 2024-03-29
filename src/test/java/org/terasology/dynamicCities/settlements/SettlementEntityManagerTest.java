// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.settlements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.terasology.dynamicCities.buildings.BuildingManager;
import org.terasology.dynamicCities.buildings.BuildingQueue;
import org.terasology.dynamicCities.districts.DistrictManager;
import org.terasology.dynamicCities.districts.DistrictType;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.parcels.ParcelList;
import org.terasology.dynamicCities.population.CultureComponent;
import org.terasology.dynamicCities.population.CultureManager;
import org.terasology.dynamicCities.settlements.components.DistrictFacetComponent;
import org.terasology.dynamicCities.sites.SiteComponent;
import org.terasology.dynamicCities.world.testbench.FlatFacetedWorld;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.integrationenvironment.ModuleTestingHelper;
import org.terasology.engine.integrationenvironment.jupiter.MTEExtension;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.namegenerator.town.TownAssetTheme;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;

import java.util.Optional;

import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@IntegrationEnvironment(dependencies={"DynamicCities"}, worldGenerator="DynamicCities:FlatFaceted")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SettlementEntityManagerTest {

    static final int MAX_PLACEMENT_ATTEMPTS = 20;
    static final String zone = "testzone";  // lowercase
    static final Vector2i buildingSize = new Vector2i(9, 9);
    static final Vector3ic siteLocation = new Vector3i(1234, FlatFacetedWorld.SURFACE_HEIGHT, -5678);
    static final int population = 3;

    @In
    Context context;

    @BeforeAll
    void initialiseZones() {
        // FIXME: mockito kludge to give us a way to define buildings.
        //   Should have a way for this test to add a building prefab,
        //   or a way to add to BuildingManager without prefabs.
        BuildingManager buildingManager = spy(context.get(BuildingManager.class));
        when(buildingManager.getMinMaxSizePerZone()).thenReturn(ImmutableMap.of(zone, ImmutableList.of(buildingSize, buildingSize)));
        context.put(BuildingManager.class, buildingManager);

        // re-inject this so it has the mock version.
        InjectionHelper.inject(context.get(SettlementEntityManager.class), context);
    }

    @BeforeAll
    void initialiseCulture(CultureManager cultures) {
        CultureComponent culture = new CultureComponent();
        culture.name = "Testers";
        culture.theme = TownAssetTheme.FANTASY.name();
        culture.availableBuildings.add("Test Building");
        culture.buildingNeedPerZone.put(zone, 1f);
        culture.residentialZones.add(zone);

        cultures.addCulture(culture);
    }

    @BeforeAll
    void initialiseDistricts(DistrictManager districts) {
        districts.addDistrict(new DistrictType("The District", zone));
    }

    EntityRef newSite() {
        SiteComponent site = new SiteComponent(siteLocation.x(), siteLocation.z());
        return context.get(EntityManager.class).create(
                site,
                new SettlementComponent(site, population),
                new LocationComponent(siteLocation)
                );
    }

    @Test
    void placeParcel(SettlementEntityManager settlements, ModuleTestingHelper mte) {
        ParcelList parcels = new ParcelList();
        BuildingQueue buildingQueue = new BuildingQueue();

        // Regions are initialized during world generation.
        mte.forceAndWaitForGeneration(siteLocation);
        // Loading a wider area requires https://github.com/Terasology/ModuleTestingEnvironment/pull/66
//        mte.runUntil(mte.makeBlocksRelevant(
//                new BlockRegion(siteLocation)
//                        .expand(Chunks.SIZE_X * 4, Chunks.SIZE_Y, Chunks.SIZE_Z * 4)
//        ));

        EntityRef site = newSite();

        EntityRef settlement = settlements.createSettlement(site);

        Vector3fc center = settlement.getComponent(LocationComponent.class).getLocalPosition();

        Optional<DynParcel> parcel = settlements.placeParcel(
                new Vector3i(center, RoundingMode.FLOOR), zone, parcels, buildingQueue,
                settlement.getComponent(DistrictFacetComponent.class), MAX_PLACEMENT_ATTEMPTS
        );
        assertThat(parcel).isPresent();
    }
}
