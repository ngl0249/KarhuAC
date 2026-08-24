package me.liwk.karhu.util;

import com.github.retrooper.packetevents.util.Vector3d;

public class VecDeltaCodec {
    private static final double TRUNCATION_STEPS = 4096.0;
    private static Vector3d base = new Vector3d();

    public static long encode(double p_238018_) {
        return Math.round(p_238018_ * 4096.0);
    }

    public static double decode(long p_238020_) {
        return (double)p_238020_ / 4096.0;
    }

    public static Vector3d decode(long p_238022_, long p_238023_, long p_238024_) {
        if (p_238022_ == 0L && p_238023_ == 0L && p_238024_ == 0L) {
            return base;
        } else {
            double d0 = p_238022_ == 0L ? base.x : decode(encode(base.x) + p_238022_);
            double d1 = p_238023_ == 0L ? base.y : decode(encode(base.y) + p_238023_);
            double d2 = p_238024_ == 0L ? base.z : decode(encode(base.z) + p_238024_);
            return new Vector3d(d0, d1, d2);
        }
    }

    public long encodeX(Vector3d p_238026_) {
        return encode(p_238026_.x) - encode(this.base.x);
    }

    public long encodeY(Vector3d p_238028_) {
        return encode(p_238028_.y) - encode(this.base.y);
    }

    public long encodeZ(Vector3d p_238030_) {
        return encode(p_238030_.z) - encode(this.base.z);
    }

    public Vector3d delta(Vector3d p_238032_) {
        return p_238032_.subtract(this.base);
    }

    public void setBase(Vector3d p_238034_) {
        this.base = p_238034_;
    }

    public Vector3d getBase() {
        return this.base;
    }
}
