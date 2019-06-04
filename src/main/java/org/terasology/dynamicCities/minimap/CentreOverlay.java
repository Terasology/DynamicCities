/*
 * Copyright 2019 MovingBlocks
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
import org.terasology.dynamicCities.settlements.SettlementsCacheComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2fTransformer;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.Canvas;
import org.terasology.utilities.Assets;

import java.util.Optional;

public class CentreOverlay implements MinimapOverlay {

    private EntityRef settlementCachingEntity;
    private Logger logger = LoggerFactory.getLogger(DistrictOverlay.class);

    private Vector2f iconSize = new Vector2f(32f, 32f);


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
                new Vector2f(canvas.getRegion().minX(), canvas.getRegion().minY()),
                new Vector2f(canvas.getRegion().maxX(), canvas.getRegion().maxY())
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
            Rect2i region = Rect2i.createFromMinAndSize(min.x, min.y, (int) iconSize.x, (int) iconSize.y);

            Optional<Texture> icon = Assets.getTexture("DynamicCities:city-icon");
            if (icon.isPresent()) {
                canvas.drawTexture(icon.get(), region);
            } else {
                logger.error("No icon found for city");
            }
        }
    }

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
