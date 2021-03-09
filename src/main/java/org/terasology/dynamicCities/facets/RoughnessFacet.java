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
package org.terasology.dynamicCities.facets;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseFieldFacet2D;
import org.terasology.math.TeraMath;


public class RoughnessFacet extends Grid2DFloatFacet {


    public RoughnessFacet(BlockRegion targetRegion, Border3D border, int gridSize) {
        super(targetRegion, border, gridSize);
    }

    //Determines the std. deviation of the height in a given cell and saves it to the facet
    public void calcRoughness(Vector2ic gridPoint, BaseFieldFacet2D facet) {

        int halfGridSize = Math.round(gridSize / 2);
        BlockArea gridCell = new BlockArea(gridPoint.sub(halfGridSize, halfGridSize, new Vector2i()), gridPoint.add(halfGridSize, halfGridSize, new Vector2i()));
        float deviation = 0;
        float meanValue = meanHeight(gridCell, facet);
        for(Vector2ic pos : gridCell) {
            deviation += Math.pow(facet.getWorld(pos) - meanValue,2);
        }

        deviation = TeraMath.sqrt(deviation / (gridCell.area()));
        setWorld(gridPoint, deviation);
    }

    //Speed up calculating mean value of the height in a grid cell
    private float meanHeight(BlockAreac gridCell, BaseFieldFacet2D facet) {

        Vector2i max = gridCell.getMax(new Vector2i());
        Vector2i min = gridCell.getMin(new Vector2i());

        Vector2i[] positions = new Vector2i[5];
        positions[0] = new Vector2i(max.x(), max.y());
        positions[1] = new Vector2i(min.x(), min.y());
        positions[2] = new Vector2i(min.x() + gridCell.getSizeX(), min.y());
        positions[3] = new Vector2i(min.x(), min.y() + gridCell.getSizeY());
        positions[4] = new Vector2i(min.x() + Math.round(0.5f * gridCell.getSizeX()), min.y() + Math.round(0.5f * gridCell.getSizeY()));

        float mean = 0;

        for (int i = 0; i < positions.length; i++) {
            mean += facet.getWorld(positions[i]);
        }

        return mean / positions.length;
    }

    public float getMeanDeviation() {
        float mean = 0;
        for(Vector2ic pos : getWorldArea()) {
            mean += getWorld(pos);
        }
        mean /= getGridRelativeRegion().area();

        return mean;
    }
}


