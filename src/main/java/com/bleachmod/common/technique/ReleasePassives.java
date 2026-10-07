package com.bleachmod.common.technique;

import com.bleachmod.Reference;
import com.bleachmod.common.data.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Derived from current identity/form: no permanent enchantments or attribute modifiers. */
public final class ReleasePassives {
    private ReleasePassives() { }
    private static boolean released(PlayerData d) {
        return d.getStatus().hasCreatedCharacter() && Reference.RACE_SHINIGAMI.equals(d.getCharacter().getRace())
                && Reference.GROUP_ZANPAKUTO.equals(d.getCharacter().getActiveFormGroup())
                && (Reference.FORM_SHIKAI.equals(d.getCharacter().getActiveForm()) || Reference.FORM_BANKAI.equals(d.getCharacter().getActiveForm()));
    }
    public static float resistance(ServerPlayer p) {
        if (!p.isAlive() || p.isSpectator()) return 0;
        return PlayerCapability.get(p).map(d -> released(d) && CharacterData.HYORINMARU.equals(d.getCharacter().getZanpakutoIdentity())
                ? Reference.FORM_BANKAI.equals(d.getCharacter().getActiveForm()) ? 0.30F : 0.15F : 0F).orElse(0F);
    }
    public static void flameHit(ServerPlayer p, PlayerData d, LivingEntity target) {
        if (released(d) && TechniqueService.isRyujinJakkaEquipped(p) && TechniqueTargets.allowed(p, target))
            target.setSecondsOnFire(Reference.FORM_BANKAI.equals(d.getCharacter().getActiveForm()) ? 8 : 4);
    }
}
