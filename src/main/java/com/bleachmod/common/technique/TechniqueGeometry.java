package com.bleachmod.common.technique;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Hitboxes intersect a finite cylinder; the enclosing query box alone is not a hit. */
public final class TechniqueGeometry {
    private TechniqueGeometry() { }

    public static boolean intersectsCylinder(Vec3 center, double radius, double below, double above, AABB box) {
        if (box.maxY < center.y - below || box.minY > center.y + above) return false;
        double x = Math.max(box.minX, Math.min(center.x, box.maxX));
        double z = Math.max(box.minZ, Math.min(center.z, box.maxZ));
        double dx = x - center.x;
        double dz = z - center.z;
        return dx * dx + dz * dz <= radius * radius;
    }
}
