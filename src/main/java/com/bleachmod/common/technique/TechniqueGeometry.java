package com.bleachmod.common.technique;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Hitboxes intersect a finite cylinder; the enclosing query box alone is not a hit. */
public final class TechniqueGeometry {
    private TechniqueGeometry() { }

    public static double tornadoAngle(int age, int sample, int arm) {
        return age * 0.3 + sample * 0.5 + arm * Math.PI;
    }

    public static boolean intersectsAnnulus(Vec3 center, double inner, double outer, double height, AABB box) {
        double x = Math.max(Math.abs(box.minX - center.x), Math.abs(box.maxX - center.x));
        double z = Math.max(Math.abs(box.minZ - center.z), Math.abs(box.maxZ - center.z));
        return intersectsCylinder(center, outer, 0, height, box) && x * x + z * z >= inner * inner;
    }

    public static AABB wallBounds(Vec3 origin, Vec3 forward, double length, double width, double height) {
        Vec3 end = origin.add(forward.scale(length));
        double xPad = Math.abs(forward.z) * width / 2;
        double zPad = Math.abs(forward.x) * width / 2;
        return new AABB(Math.min(origin.x, end.x) - xPad, origin.y, Math.min(origin.z, end.z) - zPad,
                Math.max(origin.x, end.x) + xPad, origin.y + height, Math.max(origin.z, end.z) + zPad);
    }

    /** Separating-axis test for an oriented horizontal rectangle against an entity's AABB. */
    public static boolean intersectsWall(Vec3 origin, Vec3 forward, double length, double width,
                                         double height, AABB box) {
        if (box.maxY < origin.y || box.minY > origin.y + height) return false;
        Vec3 side = new Vec3(-forward.z, 0, forward.x);
        Vec3 delta = box.getCenter().subtract(origin.add(forward.scale(length / 2)));
        double hx = box.getXsize() / 2;
        double hz = box.getZsize() / 2;
        return Math.abs(delta.x) <= hx + Math.abs(forward.x) * length / 2 + Math.abs(side.x) * width / 2
                && Math.abs(delta.z) <= hz + Math.abs(forward.z) * length / 2 + Math.abs(side.z) * width / 2
                && Math.abs(delta.dot(forward)) <= length / 2 + hx * Math.abs(forward.x) + hz * Math.abs(forward.z)
                && Math.abs(delta.dot(side)) <= width / 2 + hx * Math.abs(side.x) + hz * Math.abs(side.z);
    }

    public static boolean intersectsCylinder(Vec3 center, double radius, double below, double above, AABB box) {
        if (box.maxY < center.y - below || box.minY > center.y + above) return false;
        double x = Math.max(box.minX, Math.min(center.x, box.maxX));
        double z = Math.max(box.minZ, Math.min(center.z, box.maxZ));
        double dx = x - center.x;
        double dz = z - center.z;
        return dx * dx + dz * dz <= radius * radius;
    }
}
