// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.minimap;

import org.joml.Rectanglef;
import org.joml.Rectanglei;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.settlements.SettlementsCacheComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.utilities.Assets;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.nui.Canvas;
import org.terasology.nui.util.RectUtility;

import java.util.Optional;

public class CentreOverlay implements MinimapOverlay {
    private static final int ICON_SIZE = 32;

    private final EntityRef settlementCachingEntity;
    private final Vector2i iconSize = new Vector2i(ICON_SIZE, ICON_SIZE);

    private final Logger logger = LoggerFactory.getLogger(DistrictOverlay.class);

    public CentreOverlay(EntityRef entityRef) {
        this.settlementCachingEntity = entityRef;
    }

    @Override
    public void render(Canvas canvas, Rectanglei worldRect) {
        if (!settlementCachingEntity.hasComponent(SettlementsCacheComponent.class)) {
            logger.error("No SettlementCacheComponent found!");
            return;
        }

        for (EntityRef settlement :
                settlementCachingEntity.getComponent(SettlementsCacheComponent.class).settlementEntities.values()) {
            if (!settlement.isActive()) {
                continue;
            }
            LocationComponent locationComponent = settlement.getComponent(LocationComponent.class);
            if (locationComponent == null) {
                logger.error("Cannot find location component for settlement: " + settlement.toString());
                return;
            }

            Vector2f location = new Vector2f(locationComponent.getLocalPosition().x(),
                    locationComponent.getLocalPosition().z());
            Vector2i mapPoint = RectUtility.map(worldRect, canvas.getRegion(), new Vector2i((int) location.x,
                    (int) location.y), new Vector2i());

            Vector2i min = clamp(mapPoint, canvas.getRegion());
            Rectanglei region = RectUtility.createFromMinAndSize(min.x, min.y, iconSize.x, iconSize.y);

            Optional<Texture> icon = Assets.getTexture("DynamicCities:city-icon");
            if (icon.isPresent()) {
                canvas.drawTexture(icon.get(), region);
            } else {
                logger.error("No icon found for city");
            }
        }
    }

    /**
     * Constrains a point to a specified region. Works like a vector clamp.
     *
     * @param point: the coordinates of the point to be clamped
     * @param box: limits
     * @return new clamped coordinates of point
     */
    private Vector2i clamp(Vector2i point, Rectanglei box) {
        int x;
        int y;
        Rectanglef iconRegion = RectUtility.createFromCenterAndSize(point.x, point.y, iconSize.x, iconSize.y);
        if (box.containsRectangle(iconRegion)) {
            return new Vector2i(point.x, point.y);
        } else {
            if (iconRegion.maxX >= box.maxX) {
                x = box.maxX - iconSize.x;
            } else if (iconRegion.minX <= box.minX) {
                x = box.minX;
            } else {
                x = point.x;
            }

            if (iconRegion.maxY >= box.maxY) {
                y = box.maxY - iconSize.y;
            } else if (iconRegion.minY <= box.minY) {
                y = box.minY;
            } else {
                y = point.y;
            }
        }
        return new Vector2i(x, y);
    }

    @Override
    public int getZOrder() {
        return 0;
    }

}
