// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.rasterizer.decoration;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.fences.SimpleFence;
import org.terasology.cities.surface.InfiniteSurfaceHeightFacet;
import org.terasology.commonworld.Orientation;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;

import java.util.EnumSet;

/**
 *
 */
public class SimpleFenceRasterizer {

    private final BlockTheme theme;

    /**
     * @param theme
     */
    public SimpleFenceRasterizer(BlockTheme theme) {
        this.theme = theme;
    }

    private static Side getSide(Orientation orientation) {
        switch (orientation) {
            case WEST:
                return Side.LEFT;
            case NORTH:
                return Side.FRONT;
            case EAST:
                return Side.RIGHT;
            case SOUTH:
                return Side.BACK;
            default:
                return null;
        }
    }

    private void raster(CoreChunk chunk, SimpleFence fence, InfiniteSurfaceHeightFacet heightFacet) {
        Rect2i fenceRc = fence.getRect();
        Region3i brushRc = chunk.getRegion();

        int fleft = fenceRc.minX();
        int ftop = fenceRc.minY();
        int fright = fenceRc.maxX();
        int fbot = fenceRc.maxY();

        int bleft = brushRc.minX();
        int btop = brushRc.minZ();
        int bright = brushRc.maxX();
        int bbot = brushRc.maxZ();

        int wallX1 = Math.max(fleft + 1, bleft);
        int wallX2 = Math.min(fright - 1, bright);

        int wallZ1 = Math.max(ftop + 1, btop);
        int wallZ2 = Math.min(fbot - 1, bbot);

        // top wall is in brush area
        if (ftop >= btop && ftop <= bbot) {
            Block block = theme.apply(DefaultBlockType.FENCE, EnumSet.of(Side.LEFT, Side.RIGHT));
            wallX(chunk, heightFacet, wallX1, wallX2, ftop, block);
        }

        // bottom wall is in brush area
        if (fbot >= btop && fbot <= bbot) {
            Block block = theme.apply(DefaultBlockType.FENCE, EnumSet.of(Side.LEFT, Side.RIGHT));
            wallX(chunk, heightFacet, wallX1, wallX2, fbot, block);
        }

        // left wall is in brush area
        if (fleft >= bleft && fleft <= bright) {
            Block block = theme.apply(DefaultBlockType.FENCE, EnumSet.of(Side.FRONT, Side.BACK));
            wallZ(chunk, heightFacet, fleft, wallZ1, wallZ2, block);
        }

        // right wall is in brush area
        if (fright >= bleft && fright <= bright) {
            Block block = theme.apply(DefaultBlockType.FENCE, EnumSet.of(Side.FRONT, Side.BACK));
            wallZ(chunk, heightFacet, fright, wallZ1, wallZ2, block);
        }

        post(chunk, heightFacet, fleft, ftop, Orientation.NORTHWEST);  // 1/1 - BACK/RIGHT
        post(chunk, heightFacet, fleft, fbot, Orientation.SOUTHWEST);  // 1/-1 - FRONT/RIGHT
        post(chunk, heightFacet, fright, fbot, Orientation.SOUTHEAST);  // -1/-1 - FRONT/LEFT
        post(chunk, heightFacet, fright, ftop, Orientation.NORTHEAST);  // -1/1 - BACK/LEFT

        // insert gate
        Vector2i gatePos = fence.getGate();
        if (gatePos.x() >= brushRc.minX() && gatePos.x() <= brushRc.maxX()
                && gatePos.y() >= brushRc.minZ() && gatePos.y() <= brushRc.maxZ()) {
            int y = TeraMath.floorToInt(heightFacet.getWorld(gatePos.x(), gatePos.y())) + 1;
            if (brushRc.minY() <= y && brushRc.maxY() >= y) {
                Side side = getSide(fence.getGateOrientation());

                if (side != null) {
                    Block gateBlock = theme.apply(DefaultBlockType.FENCE_GATE, EnumSet.of(side));
                    chunk.setBlock(gatePos.x() - brushRc.minX(), y - brushRc.minY(), gatePos.y() - brushRc.minZ(),
                            gateBlock);
                }
            }
        }
    }

    private void post(CoreChunk chunk, InfiniteSurfaceHeightFacet hm, int x, int z, Orientation o) {
        Region3i region = chunk.getRegion();
        int y = TeraMath.floorToInt(hm.getWorld(x, z)) + 1;
        Orientation a = o.getRotated(180 - 45);
        Orientation b = o.getRotated(180 + 45);
        Block cornerPost = theme.apply(DefaultBlockType.FENCE, EnumSet.of(getSide(a), getSide(b)));
        if (region.encompasses(x, y, z)) {
            chunk.setBlock(x - region.minX(), y - region.minY(), z - region.minZ(), cornerPost);
        }

        if (y + 1 >= region.minY() && y + 1 <= region.maxY()) {
            if (hm.getWorld(x + a.getDir().getX(), z + a.getDir().getY()) >= y
                    || hm.getWorld(x + b.getDir().getX(), z + b.getDir().getY()) >= y) {
                chunk.setBlock(x - region.minX(), y + 1 - region.minY(), z - region.minZ(), cornerPost);
            }
        }
    }

    private void wallX(CoreChunk chunk, InfiniteSurfaceHeightFacet hm, int x1, int x2, int z, Block block) {
        int minY = chunk.getRegion().minY();
        int maxY = chunk.getRegion().maxY();

        int minX = chunk.getRegion().minX();
        int minZ = chunk.getRegion().minZ();

        for (int x = x1; x <= x2; x++) {
            int y = TeraMath.floorToInt(hm.getWorld(x, z)) + 1;  // one block above surface level

            if (y >= minY) {
                if (y <= maxY) {
                    chunk.setBlock(x - minX, y - minY, z - minZ, block);
                }
                // if one of the neighbors is at least one block higher, add one fence block on top
                if (y + 1 <= maxY && (hm.getWorld(x - 1, z) >= y || hm.getWorld(x + 1, z) >= y)) {
                    chunk.setBlock(x - minX, y + 1 - minY, z - minZ, block);
                }
            }
        }
    }

    private void wallZ(CoreChunk chunk, InfiniteSurfaceHeightFacet hm, int x, int z1, int z2, Block block) {
        int minY = chunk.getRegion().minY();
        int maxY = chunk.getRegion().maxY();

        int minX = chunk.getRegion().minX();
        int minZ = chunk.getRegion().minZ();

        for (int z = z1; z <= z2; z++) {
            int y = TeraMath.floorToInt(hm.getWorld(x, z)) + 1;  // one block above surface level

            if (y >= minY) {
                if (y <= maxY) {
                    chunk.setBlock(x - minX, y - minY, z - minZ, block);
                }

                // if one of the neighbors is at least one block higher, add one fence block on top
                if (y + 1 <= maxY && (hm.getWorld(x, z - 1) >= y || hm.getWorld(x, z + 1) >= y)) {
                    chunk.setBlock(x - minX, y + 1 - minY, z - minZ, block);
                }
            }
        }
    }

}

