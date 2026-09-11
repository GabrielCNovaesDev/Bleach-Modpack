package com.bleachmod.common.data;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
/** Purchased ranks only; derived bonuses are never accumulated in the save. */
public final class AttributeData {
    public static final String ZANJUTSU = "zanjutsu";
    public static final String HAKUDA = "hakuda";
    public static final String VITALITY = "vitality";
    public static final String RESISTANCE = "resistance";
    public static final String KIDOU = "kidou";
    public static final String RESERVE = "reserve";
    public static final String CONTROL = "control";
    public static final List<String> IDS=List.of(ZANJUTSU, HAKUDA, VITALITY, RESISTANCE, KIDOU, RESERVE, CONTROL);
    private static final int MAX_PURCHASE_COST = 1_000_000;
    private final Map<String,Integer> levels=new HashMap<>();
    public int level(String id) { return levels.getOrDefault(id,0); }
    public int cost(String id) {
        if (!IDS.contains(id) || level(id) == Integer.MAX_VALUE) return -1;
        return (int)Math.min(MAX_PURCHASE_COST, 100L * (level(id) + 1L));
    }
    public boolean purchase(String id, ResourcesData resources) {
        int cost=cost(id);
        if(cost<0||!resources.consumeTrainingPoints(cost)) return false;
        levels.put(id,level(id)+1); return true;
    }
    public CompoundTag save() { CompoundTag tag=new CompoundTag(); IDS.forEach(id->tag.putInt(id,level(id))); return tag; }
    public void load(CompoundTag tag) {
        levels.clear();
        IDS.forEach(id->levels.put(id,Math.max(0,tag.getInt(id))));
        // Version 2 used "power" for Asauchi damage. Preserve that investment as Zanjutsu.
        if (!tag.contains(ZANJUTSU) && tag.contains("power")) {
            levels.put(ZANJUTSU, Math.max(0, tag.getInt("power")));
        }
    }
}
