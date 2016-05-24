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

package org.terasology.dynamicCities.settlements;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.world.generation.Region;
import org.terasology.world.viewer.layers.AbstractFacetLayer;
import org.terasology.world.viewer.layers.Renders;
import org.terasology.world.viewer.layers.ZOrder;

/**
 * Draws the generated graph on a AWT graphics instance
 */
@Renders(value = SettlementFacet.class, order = ZOrder.BIOME + 3)
public class SettlementFacetLayer extends AbstractFacetLayer {

    private final Font font = new Font("SansSerif", Font.BOLD, 16);

    @Override
    public void render(BufferedImage img, org.terasology.world.generation.Region region) {
        SettlementFacet settlementFacet = region.getFacet(SettlementFacet.class);

        Graphics2D g = img.createGraphics();
        int dx = region.getRegion().minX();
        int dy = region.getRegion().minZ();
        g.translate(-dx, -dy);

        g.setFont(font);
        g.setColor(Color.BLACK);

        FontMetrics fm = g.getFontMetrics(font);

        for (Settlement settlement : settlementFacet.getSettlements()) {
            String text = settlement.getName();
            int width = fm.stringWidth(text);
            ImmutableVector2i center = settlement.getSite().getPos();
            g.drawString(text, center.getX() - width / 2, center.getY() - 5);
        }

        g.dispose();
    }

    @Override
    public String getWorldText(Region region, int wx, int wy) {
        return null;
    }
}
