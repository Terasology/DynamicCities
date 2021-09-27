// Copyright 2021 The Terasology Foundation
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
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.moduletestingenvironment.MTEExtension;
import org.terasology.moduletestingenvironment.extension.Dependencies;
import org.terasology.namegenerator.town.TownAssetTheme;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@Tag("MteTest")
@ExtendWith(MTEExtension.class)
@Dependencies("DynamicCities")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SettlementEntityManagerTest {

    static final int MAX_PLACEMENT_ATTEMPTS = 20;
    static final String zone = "TestZone";
    static final Vector2i buildingSize = new Vector2i(9, 9);
    static final Vector3ic siteLocation = new Vector3i(1234, 0, -5678);
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
    void placeParcel(SettlementEntityManager settlements) {
        ParcelList parcels = new ParcelList();
        BuildingQueue buildingQueue = new BuildingQueue();

        EntityRef site = newSite();
        EntityRef settlement = settlements.createSettlement(site);
        Vector3fc center = settlement.getComponent(LocationComponent.class).getLocalPosition();

        Optional<DynParcel> parcel = settlements.placeParcel(
                new Vector3i(center, RoundingMode.FLOOR), zone, parcels, buildingQueue,
                settlement.getComponent(DistrictFacetComponent.class), MAX_PLACEMENT_ATTEMPTS
        );
        assertTrue(parcel.isPresent());
    }
}
