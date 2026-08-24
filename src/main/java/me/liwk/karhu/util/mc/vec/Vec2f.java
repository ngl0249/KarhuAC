package me.liwk.karhu.util.mc.vec;

public class Vec2f {
    /**
     * The x coordinate.
     */
    public float x;

    /**
     * The y coordinate.
     */
    public float y;

    public Vec2f() { }

    public Vec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2f(Vec2f v) {
        this.x = v.x;
        this.y = v.y;
    }


    /**
     * Sets the location of this Vec2f to the same
     * coordinates as the specified Vec2f object.
     * @param v the specified Vec2f to which to set
     * this Vec2f
     */
    public void set(Vec2f v) {
        this.x = v.x;
        this.y = v.y;
    }

    /**
     * Sets the location of this Vec2f to the
     * specified float coordinates.
     *
     * @param x the new X coordinate of this {@code Vec2f}
     * @param y the new Y coordinate of this {@code Vec2f}
     */
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the square of the distance between two points.
     *
     * @param x1 the X coordinate of the first specified point
     * @param y1 the Y coordinate of the first specified point
     * @param x2 the X coordinate of the second specified point
     * @param y2 the Y coordinate of the second specified point
     * @return the square of the distance between the two
     * sets of specified coordinates.
     */
    public static float distanceSq(float x1, float y1, float x2, float y2) {
        x1 -= x2;
        y1 -= y2;
        return (x1 * x1 + y1 * y1);
    }

    /**
     * Returns the distance between two points.
     *
     * @param x1 the X coordinate of the first specified point
     * @param y1 the Y coordinate of the first specified point
     * @param x2 the X coordinate of the second specified point
     * @param y2 the Y coordinate of the second specified point
     * @return the distance between the two sets of specified
     * coordinates.
     */
    public static float distance(float x1, float y1, float x2, float y2) {
        x1 -= x2;
        y1 -= y2;
        return (float) Math.sqrt(x1 * x1 + y1 * y1);
    }

    public float lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }

    /**
     * Returns the square of the distance from this
     * Vec2f to a specified point.
     *
     * @param vx the X coordinate of the specified point to be measured
     *           against this Vec2f
     * @param vy the Y coordinate of the specified point to be measured
     *           against this Vec2f
     * @return the square of the distance between this
     * Vec2f and the specified point.
     */
    public float distanceSq(float vx, float vy) {
        vx -= x;
        vy -= y;
        return (vx * vx + vy * vy);
    }

    /**
     * Returns the square of the distance from this
     * Vec2f to a specified Vec2f.
     *
     * @param v the specified point to be measured
     *           against this Vec2f
     * @return the square of the distance between this
     * Vec2f to a specified Vec2f.
     */
    public float distanceSq(Vec2f v) {
        float vx = v.x - this.x;
        float vy = v.y - this.y;
        return (vx * vx + vy * vy);
    }

    /**
     * Returns the distance from this Vec2f to
     * a specified point.
     *
     * @param vx the X coordinate of the specified point to be measured
     *           against this Vec2f
     * @param vy the Y coordinate of the specified point to be measured
     *           against this Vec2f
     * @return the distance between this Vec2f
     * and a specified point.
     */
    public float distance(float vx, float vy) {
        vx -= x;
        vy -= y;
        return (float) Math.sqrt(vx * vx + vy * vy);
    }

    /**
     * Returns the distance from this Vec2f to a
     * specified Vec2f.
     *
     * @param v the specified point to be measured
     *           against this Vec2f
     * @return the distance between this Vec2f and
     * the specified Vec2f.
     */
    public float distance(Vec2f v) {
        float vx = v.x - this.x;
        float vy = v.y - this.y;
        return (float) Math.sqrt(vx * vx + vy * vy);
    }

    public Vec2f scale(float scale) {
        return new Vec2f(this.x * scale, this.y * scale);
    }

    /**
     * Returns the hashcode for this Vec2f.
     * @return      a hash code for this Vec2f.
     */
    @Override
    public int hashCode() {
        int bits = 7;
        bits = 31 * bits + java.lang.Float.floatToIntBits(x);
        bits = 31 * bits + java.lang.Float.floatToIntBits(y);
        return bits;
    }

    /**
     * Determines whether or not two 2D points or vectors are equal.
     * Two instances of Vec2f are equal if the values of their
     * x and y member fields, representing
     * their position in the coordinate space, are the same.
     * @param obj an object to be compared with this Vec2f
     * @return true if the object to be compared is
     *         an instance of Vec2f and has
     *         the same values; false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Vec2f) {
            Vec2f v = (Vec2f) obj;
            return (x == v.x) && (y == v.y);
        }
        return false;
    }

    /**
     * Returns a String that represents the value
     * of this Vec2f.
     * @return a string representation of this Vec2f.
     */
    @Override
    public String toString() {
        return "Vec2f[" + x + ", " + y + "]";
    }
}


