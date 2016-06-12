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

import org.terasology.cities.common.Edges;
import org.terasology.commonworld.Orientation;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;

/**
 * A turtle has a position and direction. It can be used to define 2D shapes in a relative
 * coordinate system.
 */
public class Turtle {
    private Orientation orient;
    private Vector2i pos;

    public Turtle(BaseVector2i pos, Orientation orientation) {
        this.orient = orientation;
        this.pos = new Vector2i(pos);
    }

    /**
     * @param other the turtle to copy
     */
    public Turtle(Turtle other) {
        this(other.pos, other.orient);
    }

    /**
     * @param degrees the rotation (in 45 degree steps)
     * @return this
     */
    public Turtle rotate(int degrees) {
        orient = orient.getRotated(degrees);
        return this;
    }

    /**
     * Sets the position independent of current position/location
     * @param newPos the new coordinates
     * @return this
     */
    public Turtle setPosition(BaseVector2i newPos) {
        return setPosition(newPos.getX(), newPos.getY());
    }

    /**
     * Sets the position independent of current position/location
     * @param x the new x coordinate
     * @param y the new y coordinate
     * @return this
     */
    public Turtle setPosition(int x, int y) {
        pos.set(x, y);
        return this;
    }

    /**
     * Move the turtle relative to the current position and rotation
     * @param right amount to the right
     * @param forward amount forward
     * @return this
     */
    public Turtle move(int right, int forward) {
        ImmutableVector2i dir = orient.getDir();
        pos.addX(rotateX(dir, right, forward));
        pos.addY(rotateY(dir, right, forward));
        return this;
    }

    /**
     * @param rect the rect to inspect
     * @return the width of the rectangle wrt. the current direction
     */
    public int width(Rect2i rect) {
        return isHorz() ? rect.height() : rect.width();
    }

    /**
     * @param rect the rect to inspect
     * @return the length of the rectangle wrt. the current direction
     */
    public int length(Rect2i rect) {
        return isHorz() ? rect.width() : rect.height();
    }

    /**
     * Creates a rectangle that is centered along the current direction.
     * <pre>
     *      x------x
     * o->  |      |
     *      |      |
     *      x------x
     * </pre>
     * @param right the offset to the right
     * @param forward the offset along the direction axis
     * @param width the width of the rectangle
     * @param len the length of the rectangle
     * @return the rectangle
     */
    public Rect2i rect(int right, int forward, int width, int len) {
        ImmutableVector2i dir = orient.getDir();
        int minX = pos.getX() + rotateX(dir, right, forward);
        int minY = pos.getY() + rotateY(dir, right, forward);

        int maxX = pos.getX() + rotateX(dir, right + width - 1, forward + len - 1);
        int maxY = pos.getY() + rotateY(dir, right + width - 1, forward + len - 1);
        return Rect2i.createEncompassing(minX, minY, maxX, maxY);
    }

    /**
     * Creates a rectangle that is centered along the current direction.
     * <pre>
     *      x------x
     * o->  |      |
     *      x------x
     * </pre>
     * @param forward the offset along the direction axis
     * @param width the width of the rectangle
     * @param len the length of the rectangle
     * @return the rectangle
     */
    public Rect2i rectCentered(int forward, int width, int len) {
        return rect(-width / 2, forward, width, len);
    }

    /**
     * @param rc the rectangle to adjust
     * @param left the offset of the left edge
     * @param back the offset of the back edge
     * @param right the offset of the right edge
     * @param forward the offset of the forward edge
     * @return a new rect with adjusted coordinates
     */
    public Rect2i adjustRect(Rect2i rc, int left, int back, int right, int forward) {
        Orientation cd = orient.getRotated(45);
        Vector2i max = Edges.getCorner(rc, cd);
        Vector2i min = Edges.getCorner(rc, cd.getOpposite());

        ImmutableVector2i dir = orient.getDir();
        int minX = min.getX() + rotateX(dir, left, back);
        int minY = min.getY() + rotateY(dir, left, back);
        int maxX = max.getX() + rotateX(dir, right, forward);
        int maxY = max.getY() + rotateY(dir, right, forward);

        return Rect2i.createEncompassing(minX, minY, maxX, maxY);
    }

    /**
     * @return the current orientation
     */
    public Orientation getOrientation() {
        return orient;
    }

    /**
     * @return a copy of the current cursor location
     */
    public ImmutableVector2i getPos() {
        return new ImmutableVector2i(pos);
    }

    /**
     * Apply the current position offset and rotation to the given translation vector
     * @param right amount to the right
     * @param forward amount forward
     * @return the transformed translation
     */
    public Vector2i transform(int right, int forward) {
        int x = pos.getX() + rotateX(orient.getDir(), right, forward);
        int y = pos.getY() + rotateY(orient.getDir(), right, forward);
        return new Vector2i(x, y);
    }

    private boolean isHorz() {
        return (orient == Orientation.WEST) || (orient == Orientation.EAST);
    }

    private static int rotateX(BaseVector2i dir, int dx, int dy) {
        return -dx * dir.getY() + dy * dir.getX();
    }

    private static int rotateY(BaseVector2i dir, int dx, int dy) {
        return dx * dir.getX() + dy * dir.getY();
    }
}
