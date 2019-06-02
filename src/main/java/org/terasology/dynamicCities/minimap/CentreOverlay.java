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
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2fTransformer;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.Canvas;
import org.terasology.utilities.Assets;

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

        Rect2fTransformer t = new Rect2fTransformer(worldRect, screenRect);

        for (EntityRef settlement : settlementCachingEntity.getComponent(SettlementsCacheComponent.class).settlementEntities.values()) {
            if (!settlement.isActive()) {
                continue;
            }
            LocationComponent locationComponent = settlement.getComponent(LocationComponent.class);
            if (locationComponent == null) {
                logger.error("Cannot find location component for settlement: " + settlement.toString());
                return;
            }

            Vector2f pos = new Vector2f(locationComponent.getLocalPosition().x(), locationComponent.getLocalPosition().z());// TODO
            float width = iconSize.getX();
            float height = iconSize.getY();
            if (worldRect.contains(pos)) {
                Vector2i worldPoint = new Vector2i((int) pos.x, (int) pos.y);
                int lx = TeraMath.floorToInt(t.applyX(worldPoint.getX()));
                int ly = TeraMath.floorToInt(t.applyY(worldPoint.getY()));
                Rect2i region = Rect2i.createFromMinAndSize(lx, ly, (int) width, (int) height);
                Texture icon = Assets.getTexture("DynamicCities:city-icon").get();
                canvas.drawTexture(icon, region);
            }
        }
    }

    @Override
    public int getZOrder() {
        return 0;
    }

}
