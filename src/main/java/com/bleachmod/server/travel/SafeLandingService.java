package com.bleachmod.server.travel;

import com.bleachmod.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public final class SafeLandingService {
    public static final TagKey<Block> HAZARDS = TagKey.create(Registries.BLOCK, Reference.id("travel_hazards"));
    public static Optional<Vec3> find(ServerLevel level, ServerPlayer player, BlockPos center) {
        // Nearest first, including the supplied spawn itself. All required chunks must already be loaded.
        for (int radius = 0; radius <= 4; radius++) {
            for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                for (int dy : new int[]{0, 1, -1, 2, -2, 3, -3}) {
                    var pos = center.offset(dx, dy, dz);
                    Vec3 feet = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    if (safe(level, player, feet)) return Optional.of(feet);
                }
            }
        }
        return Optional.empty();
    }
    public static boolean safe(ServerLevel level, ServerPlayer player, Vec3 feet) {
        AABB body = player.getDimensions(net.minecraft.world.entity.Pose.STANDING).makeBoundingBox(feet);
        if (body.minY < level.getMinBuildHeight() + 1 || body.maxY >= level.getMaxBuildHeight() || !level.getWorldBorder().isWithinBounds(body)) return false;
        for (BlockPos pos : BlockPos.betweenClosed((int)Math.floor(body.minX), (int)Math.floor(body.minY) - 1, (int)Math.floor(body.minZ),
            (int)Math.floor(body.maxX), (int)Math.floor(body.maxY), (int)Math.floor(body.maxZ))) {
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4) || level.getBlockState(pos).is(HAZARDS) || !level.getFluidState(pos).isEmpty()) return false;
        }
        BlockPos floor = BlockPos.containing(feet).below();
        if (!level.getBlockState(floor).isFaceSturdy(level, floor, net.minecraft.core.Direction.UP)) return false;
        return level.noCollision(player, body) && level.getEntities(player, body, entity -> entity.isAlive() && !entity.isSpectator()).isEmpty();
    }
    private SafeLandingService() {}
}
