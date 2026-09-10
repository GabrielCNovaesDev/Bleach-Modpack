package com.bleachmod.common.data;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
/** Purchased ranks only; derived bonuses are never accumulated in the save. */
public final class AttributeData {
    public static final List<String> IDS=List.of("power","reserve","control");
    public static final int MAX_LEVEL=5;
    private final Map<String,Integer> levels=new HashMap<>();
    public int level(String id) { return levels.getOrDefault(id,0); }
    public int cost(String id) { return IDS.contains(id)&&level(id)<MAX_LEVEL ? 100*(level(id)+1) : -1; }
    public boolean purchase(String id, ResourcesData resources) {
        int cost=cost(id);
        if(cost<0||!resources.consumeTrainingPoints(cost)) return false;
        levels.put(id,level(id)+1); return true;
    }
    public CompoundTag save() { CompoundTag tag=new CompoundTag(); IDS.forEach(id->tag.putInt(id,level(id))); return tag; }
    public void load(CompoundTag tag) { levels.clear(); IDS.forEach(id->levels.put(id,Math.max(0,Math.min(MAX_LEVEL,tag.getInt(id))))); }
}
