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

package org.terasology.dynamicCities.rasterizer.window;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.cities.window.SimpleWindow;
import org.terasology.commonworld.heightmap.HeightMap;

/**
 * Converts {@link SimpleWindow} into blocks (or air actually)
 */
public class SimpleWindowRasterizer extends WindowRasterizer<SimpleWindow> {

    /**
     * @param theme the block theme to use
     */
    public SimpleWindowRasterizer(BlockTheme theme) {
        super(theme, SimpleWindow.class);
    }

    @Override
    public void raster(RasterTarget target, SimpleWindow wnd, HeightMap hm) {
        int x = wnd.getPos().x();
        int y = wnd.getHeight();
        int z = wnd.getPos().y();

        if (target.getAffectedRegion().encompasses(x, y, z)) {
            target.setBlock(x, y, z, DefaultBlockType.WINDOW_GLASS);
        }
    }

}
