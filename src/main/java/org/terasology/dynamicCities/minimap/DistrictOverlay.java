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
package org.terasology.dynamicCities.minimap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.districts.DistrictType;
import org.terasology.dynamicCities.parcels.ParcelList;
import org.terasology.dynamicCities.settlements.SettlementsCacheComponent;
import org.terasology.dynamicCities.settlements.components.DistrictFacetComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Circle;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2fTransformer;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;

public class DistrictOverlay implements MinimapOverlay {


    private EntityRef settlementCachingEntity;
    private Logger logger = LoggerFactory.getLogger(DistrictOverlay.class);


    public DistrictOverlay(EntityRef entityRef) {
        this.settlementCachingEntity = entityRef;


    }

    @Override
    public void render(Canvas canvas, Rect2f worldRect) {
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
        Rect2f screenRect = Rect2f.createFromMinAndSize(new Vector2f(canvas.getRegion().minX(), canvas.getRegion().minY()),
                new Vector2f(canvas.getRegion().maxX(), canvas.getRegion().maxY()));
        Rect2fTransformer t = new Rect2fTransformer(worldRect, screenRect);
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
            Circle cityRadius = new Circle(pos.x, pos.y, parcelList.builtUpRadius);
            Rect2f worldRectExpanded = worldRect.expand(districtFacet.getGridSize(), districtFacet.getGridSize());
            if (Vector2f.distance(worldRect.min(), pos) <= settlement.getComponent(ParcelList.class).builtUpRadius + worldRect.size().length()) {
                for (BaseVector2i point : districtFacet.getGridWorldRegion().contents()) {
                    Vector2i worldPoint = districtFacet.getWorldPoint(point.x(), point.y());

                    if (worldRectExpanded.contains(worldPoint) && cityRadius.contains(worldPoint)) {
                        int lx = TeraMath.floorToInt(t.applyX(worldPoint.getX()));
                        int ly = TeraMath.floorToInt(t.applyY(worldPoint.getY()));
                        int sizeX = Math.round(districtFacet.getGridSize() * t.getScaleX());
                        int sizeY = Math.round(districtFacet.getGridSize() * t.getScaleY());
                        DistrictType districtType = districtFacet.getDistrict(worldPoint.x(), worldPoint.y());
                        Color districtColor = districtType.getColor().alterAlpha(130);
                        Rect2i gridRect = Rect2i.createFromMinAndSize(lx, ly, sizeX, sizeY);
                        canvas.drawFilledRectangle(gridRect, districtColor);
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

