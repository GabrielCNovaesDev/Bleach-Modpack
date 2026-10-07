package com.bleachmod.common.technique;

import com.bleachmod.common.data.*;
import com.bleachmod.common.network.SyncHelper;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Temporary armor has no inventory items, saved buff or permanent attribute modifier. */
public final class IceArmorService {
    public static final int DURATION = 120;
    public static final float REDUCTION = 0.25F;
    private record Armor(ServerPlayer owner, long expires) { }
    private static final Map<UUID, Armor> ACTIVE = new HashMap<>();
    private IceArmorService() { }
    public static void begin(ServerPlayer p, PlayerData d) {
        ACTIVE.put(p.getUUID(), new Armor(p, p.level().getGameTime() + DURATION));
        d.getCharacter().setIceArmorVisual(true); SyncHelper.appearance(p);
    }
    public static boolean active(ServerPlayer p) {
        Armor armor = ACTIVE.get(p.getUUID());
        return armor != null && armor.owner == p && p.isAlive() && !p.isSpectator() && armor.expires > p.level().getGameTime()
                && PlayerCapability.get(p).map(d -> d.getStatus().hasCreatedCharacter() && "shinigami".equals(d.getCharacter().getRace()) && HyorinmaruTechniqueService.equipped(p, d)
                && "sealed".equals(d.getCharacter().getActiveForm())).orElse(false);
    }
    public static void tick(ServerPlayer p) { if (ACTIVE.containsKey(p.getUUID()) && !active(p)) cancel(p); }
    public static void cancel(ServerPlayer p) {
        if (ACTIVE.remove(p.getUUID()) != null) {
            PlayerCapability.get(p).ifPresent(d -> d.getCharacter().setIceArmorVisual(false)); SyncHelper.appearance(p);
        }
    }
    public static void clear() { for (Armor armor : java.util.List.copyOf(ACTIVE.values())) cancel(armor.owner); }
    public static void unload(net.minecraft.world.level.Level level) {
        for (Armor armor : java.util.List.copyOf(ACTIVE.values())) if (armor.owner.level() == level) cancel(armor.owner);
    }
}
