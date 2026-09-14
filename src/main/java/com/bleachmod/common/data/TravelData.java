package com.bleachmod.common.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

/** Persistent state only. A missing tag in a partial sync must not clear this object. */
public final class TravelData {
    public record Location(String dimension, double x, double y, double z, float yaw, float pitch) {
        public Location {
            if (dimension == null || ResourceLocation.tryParse(dimension) == null ||
                !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) ||
                Math.abs(x) > 29_999_984 || Math.abs(z) > 29_999_984 || y < -64 || y > 319 ||
                !Float.isFinite(yaw) || !Float.isFinite(pitch)) throw new IllegalArgumentException("Invalid travel location");
        }
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("dimension", dimension); tag.putDouble("x", x); tag.putDouble("y", y); tag.putDouble("z", z);
            tag.putFloat("yaw", yaw); tag.putFloat("pitch", pitch);
            return tag;
        }
        public static Location load(CompoundTag tag) {
            for (String key : new String[]{"x", "y", "z", "yaw", "pitch"})
                if (!tag.contains(key, Tag.TAG_ANY_NUMERIC)) throw new IllegalArgumentException("Missing location field");
            return new Location(tag.getString("dimension"), tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"), tag.getFloat("yaw"), tag.getFloat("pitch"));
        }
    }
    private Location returnLocation;
    private long nextTravelAt;
    public Location getReturnLocation() { return returnLocation; }
    public void setReturnLocation(Location location) { returnLocation = location; }
    public void setCooldown(long now, int ticks) { nextTravelAt = now + Math.max(0, Math.min(72_000, ticks)); }
    public int remaining(long now) {
        if (nextTravelAt > now + 72_000) nextTravelAt = now + 72_000;
        return (int)Math.max(0, nextTravelAt - now);
    }
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("nextTravelAt", nextTravelAt);
        if (returnLocation != null) tag.put("returnLocation", returnLocation.save());
        return tag;
    }
    public void load(CompoundTag tag) {
        nextTravelAt = Math.max(0, tag.getLong("nextTravelAt"));
        returnLocation = null;
        if (tag.contains("returnLocation", Tag.TAG_COMPOUND)) {
            try { returnLocation = Location.load(tag.getCompound("returnLocation")); }
            catch (IllegalArgumentException ignored) { /* Invalid legacy data has no usable return. */ }
        }
    }
}
