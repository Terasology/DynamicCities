// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.minimap;

import org.terasology.joml.geom.Rectanglei;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.districts.DistrictType;
import org.terasology.dynamicCities.parcels.ParcelList;
import org.terasology.dynamicCities.settlements.SettlementsCacheComponent;
import org.terasology.dynamicCities.settlements.components.DistrictFacetComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Circlef;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.nui.Canvas;
import org.terasology.nui.Color;
import org.terasology.nui.util.RectUtility;

public class DistrictOverlay implements MinimapOverlay {


    private EntityRef settlementCachingEntity;
    private Logger logger = LoggerFactory.getLogger(DistrictOverlay.class);


    public DistrictOverlay(EntityRef entityRef) {
        this.settlementCachingEntity = entityRef;
    }

    @Override
    public void render(Canvas canvas, Rectanglei worldRect) {
        /**
         * Iterate through all known cities in the settlement manager and check if the boundaries of the screenRect are
         * within city reach.
         * Color rectangles of the district grid in a zone specific color.
         *
         */
        if (!settlementCachingEntity.hasComponent(SettlementsCacheComponent.class)) {
            logger.error("No SettlementCacheComponent found!");
            return;
        }
        for (EntityRef settlement : settlementCachingEntity.getComponent(SettlementsCacheComponent.class).settlementEntities.values()) {
            if (!settlement.isActive()) {
                continue;
            }
            LocationComponent locationComponent = settlement.getComponent(LocationComponent.class);
            ParcelList parcelList = settlement.getComponent(ParcelList.class);
            DistrictFacetComponent districtFacet = settlement.getComponent(DistrictFacetComponent.class);
            if (locationComponent == null) {
                logger.error("Cannot find location component for settlement: " + settlement.toString());
                return;
            }
            if (parcelList == null) {
                logger.error("Cannot find parcel-list component for settlement: " + settlement.toString());
                return;
            }
            if (districtFacet == null) {
                logger.error("Cannot find districtFacet component for settlement: " + settlement.toString());
                return;
            }
            Vector2f pos = new Vector2f(locationComponent.getLocalPosition().x(), locationComponent.getLocalPosition().z());
            Circlef cityRadius = new Circlef(pos.x, pos.y, parcelList.builtUpRadius);

            Rectanglei worldRectExpanded = RectUtility.expand(worldRect, districtFacet.getGridSize(), districtFacet.getGridSize());
            if (new Vector2f(worldRect.minX, worldRect.minY).distance(pos) <= settlement.getComponent(ParcelList.class).builtUpRadius + new Vector2i(worldRect.maxX - worldRect.minX, worldRect.maxY - worldRect.minY).length()) {

                Rectanglei gridWorldRegion = districtFacet.getGridWorldRegion();
                for (int x = gridWorldRegion.minX; x < gridWorldRegion.maxX; x++) {
                    for (int y = gridWorldRegion.minY; y < gridWorldRegion.maxY; y++) {
                        Vector2i worldPoint = districtFacet.getWorldPoint(x, y);

                        if (worldRectExpanded.containsPoint(worldPoint) && new Vector2f(worldPoint).distance(cityRadius.x, cityRadius.y) < cityRadius.r) {
                            Vector2i gridPos = RectUtility.map(worldRect, canvas.getRegion(), new Vector2i(worldPoint.x, worldPoint.y), new Vector2i());
                            int sizeX = Math.round(districtFacet.getGridSize() * (float) canvas.getRegion().lengthX() /  (float) worldRect.lengthX());
                            int sizeY = Math.round(districtFacet.getGridSize() * (float) canvas.getRegion().lengthY() /  (float) worldRect.lengthY());
                            DistrictType districtType = districtFacet.getDistrict(worldPoint.x(), worldPoint.y());
                            Color districtColor = districtType.getColor().alterAlpha(130);
                            Rectanglei gridRect = JomlUtil.rectangleiFromMinAndSize(gridPos.x, gridPos.y, sizeX, sizeY);
                            canvas.drawFilledRectangle(gridRect, districtColor);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getZOrder() {
        return 0;
    }


}

