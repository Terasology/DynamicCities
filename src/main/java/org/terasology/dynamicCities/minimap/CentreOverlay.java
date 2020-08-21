// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.minimap;

import org.joml.Rectanglei;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.settlements.SettlementsCacheComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2fTransformer;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.nui.Canvas;
import org.terasology.utilities.Assets;

import java.util.Optional;

public class CentreOverlay implements MinimapOverlay {
    private static final float ICON_SIZE = 32f;

    private EntityRef settlementCachingEntity;
    private Vector2f iconSize = new Vector2f(ICON_SIZE, ICON_SIZE);

    private Logger logger = LoggerFactory.getLogger(DistrictOverlay.class);

    public CentreOverlay(EntityRef entityRef) {
        this.settlementCachingEntity = entityRef;
    }

    @Override
    public void render(Canvas canvas, Rect2f worldRect) {
        if (!settlementCachingEntity.hasComponent(SettlementsCacheComponent.class)) {
            logger.error("No SettlementCacheComponent found!");
            return;
        }

        Rect2f screenRect = Rect2f.createFromMinAndSize(
                new Vector2f(canvas.getRegion().minX, canvas.getRegion().minY),
                new Vector2f(canvas.getRegion().maxX, canvas.getRegion().maxY)
        );

        Rect2fTransformer transformer = new Rect2fTransformer(worldRect, screenRect);

        for (EntityRef settlement : settlementCachingEntity.getComponent(SettlementsCacheComponent.class).settlementEntities.values()) {
            if (!settlement.isActive()) {
                continue;
            }
            LocationComponent locationComponent = settlement.getComponent(LocationComponent.class);
            if (locationComponent == null) {
                logger.error("Cannot find location component for settlement: " + settlement.toString());
                return;
            }

            Vector2f location = new Vector2f(locationComponent.getLocalPosition().x(), locationComponent.getLocalPosition().z());
            Vector2f mapPoint = new Vector2f(
                    transformer.applyX(location.x),
                    transformer.applyY(location.y)
            );

            Vector2i min = clamp(mapPoint, screenRect);
            Rectanglei region = JomlUtil.rectangleiFromMinAndSize(min.x, min.y, (int) iconSize.x, (int) iconSize.y);

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
     * @param point: the coordinates of the point to be clamped
     * @param box: limits
     * @return new clamped coordinates of point
     */
    private Vector2i clamp(Vector2f point, Rect2f box) {
        float x;
        float y;
        Rect2f iconRegion = Rect2f.createFromMinAndSize(point, iconSize);
        if (box.contains(iconRegion)) {
            return new Vector2i(point.x, point.y);
        } else {
            if (iconRegion.maxX() >= box.maxX()) {
                x = (int) box.maxX() - iconSize.x;
            } else if (iconRegion.minX() <= box.minX()) {
                x = (int) box.minX();
            } else {
                x = point.x;
            }

            if (iconRegion.maxY() >= box.maxY()) {
                y = (int) box.maxY() - iconSize.y;
            } else if (iconRegion.minY() <= box.minY()) {
                y = (int) box.minY();
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
