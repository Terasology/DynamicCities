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

package org.terasology.dynamicCities.gen;

import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.bldg.Building;
import org.terasology.cities.bldg.BuildingPart;
import org.terasology.cities.bldg.DefaultBuilding;
import org.terasology.cities.bldg.RectBuildingPart;
import org.terasology.cities.common.Edges;
import org.terasology.cities.deco.Ladder;
import org.terasology.cities.deco.Pillar;
import org.terasology.cities.deco.SingleBlockDecoration;
import org.terasology.cities.door.WingDoor;
import org.terasology.cities.model.roof.HipRoof;
import org.terasology.cities.model.roof.PentRoof;
import org.terasology.cities.model.roof.SaddleRoof;
import org.terasology.cities.window.RectWindow;
import org.terasology.cities.window.SimpleWindow;
import org.terasology.commonworld.Orientation;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.LineSegment;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.utilities.random.MersenneRandom;
import org.terasology.utilities.random.Random;

import java.math.RoundingMode;

/**
 * Creates building models of a simple church.
 */
public class SimpleChurchGenerator implements BuildingGenerator {

    private final long seed;

    /**
     * @param seed the seed
     */
    public SimpleChurchGenerator(long seed) {
        this.seed = seed;
    }

    /**
     * @param lot the lot to use
     * @param hm the height map to define the floor level
     * @return a generated building model of a simple church
     */
    public Building generate(DynParcel lot, HeightMap hm) {

        Random rand = new MersenneRandom(seed ^ lot.getShape().hashCode());

        // make build-able area 1 block smaller, so make the roof stay inside
        Rect2i lotRc = lot.getShape().expand(new Vector2i(-1, -1));

        boolean alignEastWest = (lotRc.width() > lotRc.height());
        Orientation o = alignEastWest ? Orientation.EAST : Orientation.NORTH;

        if (rand.nextBoolean()) {
            o = o.getOpposite();
        }

        Turtle turtle = new Turtle(Edges.getCorner(lotRc, o.getOpposite()), o);
        int length = turtle.length(lotRc);
        int width = turtle.width(lotRc);

        turtle.move(0, 1);

        double relationWidth = 2.0;

        int towerSize = (int) (length * 0.2);  // tower size compared to nave size

        // make it odd, so that the tented roof looks nice (1 block thick at the center)
        if (towerSize % 2 == 0) {
            towerSize++;
        }

        int sideOff = 3;
        int sideWidth = 5;

        int naveLen = length - towerSize;
        int naveWidth = (int) Math.min(width - 2 * sideWidth, towerSize * relationWidth);
        int sideLen = naveLen - 2 * sideOff;

        // make it odd, so it looks symmetric with the tower - make it smaller though
        if (naveWidth % 2 == 0) {
            naveWidth--;
        }

        int entranceWidth = 3;  // odd number to center properly
        Rect2i entranceRect = turtle.rectCentered(0, entranceWidth, 1);

        Rect2i naveRect = turtle.rectCentered(0, naveWidth, naveLen);
        Rect2i towerRect = turtle.rectCentered(naveLen - 1, towerSize, towerSize); // the -1 makes tower and nave overlap
        int baseHeight = getMaxHeight(entranceRect, hm) + 1; // 0 == terrain

        DefaultBuilding church = new DefaultBuilding(turtle.getOrientation());
        church.addPart(createNave(new Turtle(turtle), naveRect, entranceRect, baseHeight));
        church.addPart(createTower(new Turtle(turtle), towerRect, baseHeight));

        Rect2i aisleLeftRc = turtle.rect(-naveWidth / 2 - sideWidth + 1, sideOff, sideWidth, sideLen);  // make them overlap
        Rect2i aisleRightRc = turtle.rect(naveWidth / 2, sideOff, sideWidth, sideLen); // make them overlap

        church.addPart(createAisle(new Turtle(turtle).rotate(-90), aisleLeftRc, baseHeight));
        church.addPart(createAisle(new Turtle(turtle).rotate(90), aisleRightRc, baseHeight));

        return church;
    }

    private BuildingPart createNave(Turtle cur, Rect2i naveRect, Rect2i doorRc, int baseHeight) {
        int entranceHeight = 4;

        int hallHeight = 10;
        int topHeight = baseHeight + hallHeight;

        Rect2i naveRoofRect = naveRect.expand(1, 1);
        SaddleRoof naveRoof = new SaddleRoof(naveRect, naveRoofRect, topHeight, cur.getOrientation(), 1);

        RectBuildingPart nave = new RectBuildingPart(naveRect, naveRoof, baseHeight, hallHeight);

        WingDoor entrance = new WingDoor(cur.getOrientation(), doorRc, baseHeight, baseHeight + entranceHeight);
        nave.addDoor(entrance);

        int wallDist = cur.width(naveRect) / 2;
        for (int i = 4; i < cur.length(naveRect) - 4; i += 3) {
            Orientation left = cur.getOrientation().getRotated(-90);
            Orientation right = cur.getOrientation().getRotated(90);
            nave.addWindow(new SimpleWindow(left, cur.transform(-wallDist, i), topHeight - 3));
            nave.addWindow(new SimpleWindow(right, cur.transform(wallDist, i), topHeight - 3));
        }

        Vector2i colLeft = cur.transform(-wallDist + 2, cur.length(naveRect) - 2);
        Vector2i colRight = cur.transform(wallDist - 2, cur.length(naveRect) - 2);
        Vector3i colLeft3d = new Vector3i(colLeft.getX(), baseHeight, colLeft.getY());
        Vector3i colRight3d = new Vector3i(colRight.getX(), baseHeight, colRight.getY());
        nave.addDecoration(new Pillar(colLeft3d, hallHeight - 3));
        nave.addDecoration(new Pillar(colRight3d, hallHeight - 3));
        return nave;
    }

    private BuildingPart createTower(Turtle turtle, Rect2i rect, int baseHeight) {
        int towerHeight = 22;
        int doorHeight = 8;

        Orientation dir = turtle.getOrientation();
        Rect2i towerRoofRect = rect.expand(1, 1);
        int topHeight = baseHeight + towerHeight;
        HipRoof towerRoof = new HipRoof(rect, towerRoofRect, topHeight, 2);
        RectBuildingPart tower = new RectBuildingPart(rect, towerRoof, baseHeight, towerHeight);

        turtle.setPosition(Edges.getCorner(rect, dir.getOpposite()));

        int width = turtle.width(rect) - 2;
        tower.addDoor(new WingDoor(dir, turtle.rectCentered(0, width, 1), baseHeight, baseHeight + doorHeight));

        // create and add tower windows
        for (int i = 0; i < 3; i++) {
            // use the other three cardinal directions to place windows
            Orientation orient = dir.getRotated(90 * (i - 1)); // left, forward, right
            LineSegment towerBorder = Edges.getEdge(rect, orient);
            Vector2i towerPos = new Vector2i(towerBorder.lerp(0.5f), RoundingMode.HALF_UP);

            Rect2i wndRect = Rect2i.createFromMinAndSize(towerPos.getX(), towerPos.getY(), 1, 1);
            tower.addWindow(new RectWindow(orient, wndRect, topHeight - 4, topHeight - 1, DefaultBlockType.AIR));
        }

        Vector2i torchPos2d = turtle.transform(0, turtle.length(rect) - 2);
        Vector3i torchPos3d = new Vector3i(torchPos2d.x(), baseHeight + 4, torchPos2d.y());
        tower.addDecoration(new SingleBlockDecoration(DefaultBlockType.TORCH, torchPos3d, Side.FRONT));

        Orientation ladderDir = dir.getRotated(270);
        Vector2i ladderPos2d = Edges.getCorner(rect.expand(-1, -1), ladderDir);
        Vector3i ladderPos3d = new Vector3i(ladderPos2d.getX(), baseHeight + 1, ladderPos2d.getY());
        tower.addDecoration(new Ladder(ladderPos3d, ladderDir, towerHeight - 3));

        return tower;
    }

    private RectBuildingPart createAisle(Turtle turtle, Rect2i rect, int baseHeight) {
        Rect2i roofRect = turtle.adjustRect(rect, -1, 1, 1, 1);  // back overlap +1 to not intersect with nave

        int sideWallHeight = 4;
        int doorHeight = sideWallHeight - 1;
        Orientation dir = turtle.getOrientation();
        Orientation roofOrient = dir.getOpposite();
        PentRoof roof = new PentRoof(rect, roofRect, baseHeight + sideWallHeight, roofOrient, 0.333f);
        RectBuildingPart aisle = new RectBuildingPart(rect, roof, baseHeight, sideWallHeight);

        turtle.setPosition(Edges.getCorner(rect, dir.getOpposite()));

        int len = 8;
        aisle.addDoor(new WingDoor(dir, turtle.rect(-len + 2, 0, 3, 1), baseHeight, baseHeight + doorHeight));
        aisle.addDoor(new WingDoor(dir, turtle.rect(-1, 0, 3, 1), baseHeight, baseHeight + doorHeight));
        aisle.addDoor(new WingDoor(dir, turtle.rect(len - 4, 0, 3, 1), baseHeight, baseHeight + doorHeight));

        return aisle;
    }

    private int getMaxHeight(Rect2i rc, HeightMap hm) {
        int maxHeight = Integer.MIN_VALUE;

        for (int z = rc.minY(); z <= rc.maxY(); z++) {
            for (int x = rc.minX(); x <= rc.maxX(); x++) {
                int height = TeraMath.floorToInt(hm.apply(x, z));
                if (maxHeight < height) {
                    maxHeight = height;
                }
            }
        }

        return maxHeight;
    }
}
