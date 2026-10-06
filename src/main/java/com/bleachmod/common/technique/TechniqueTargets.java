package com.bleachmod.common.technique;

import com.bleachmod.entity.QuestNpcEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Shared target policy for both Zanpakuto kits and the older Ryujin executors. */
public final class TechniqueTargets {
    private TechniqueTargets() { }
    public static boolean allowed(ServerPlayer owner, LivingEntity target) {
        if (target == owner || !target.isAlive() || target.isSpectator() || owner.isAlliedTo(target)
                || target instanceof QuestNpcEntity || target.getPersistentData().getBoolean("bleachmod_flame_bat")) return false;
        return !(target instanceof Player other)
                || (owner.getServer().isPvpAllowed() && owner.canHarmPlayer(other));
    }
}
