package com.bleachmod.common;

/** Central formulas for attributes, combat and form upkeep. */
public final class CombatBalance {
    public static final float DAMAGE_PER_RANK = 0.10F;
    public static final float HEALTH_PER_VITALITY_RANK = 2.0F;
    public static final float RESISTANCE_PER_RANK = 0.05F;
    public static final float CONTROL_PER_RANK = 0.10F;

    private CombatBalance() {}

    public static float outgoingDamage(float baseDamage, int rank, float formBonus) {
        if (!Float.isFinite(baseDamage) || baseDamage <= 0) return baseDamage;
        return baseDamage * (1.0F + DAMAGE_PER_RANK * Math.max(0, rank) + Math.max(0, formBonus));
    }

    public static float formDamageBonus(String form) {
        return "bankai".equals(form) ? 0.50F : "shikai".equals(form) ? 0.20F : 0.0F;
    }

    /** Diminishing returns keep defense useful without ever reaching damage immunity. */
    public static float incomingPhysicalDamage(float damage, int resistanceRank) {
        if (!Float.isFinite(damage) || damage <= 0) return damage;
        return damage / (1.0F + RESISTANCE_PER_RANK * Math.max(0, resistanceRank));
    }

    /** Control has diminishing returns and therefore can never turn upkeep into regeneration. */
    public static float formDrain(float baseDrain, int controlRank) {
        if (!Float.isFinite(baseDrain) || baseDrain <= 0) return 0;
        return baseDrain / (1.0F + CONTROL_PER_RANK * Math.max(0, controlRank));
    }

    public static float kidouDamage(float baseDamage, int kidouRank) {
        return outgoingDamage(baseDamage, kidouRank, 0);
    }
}
